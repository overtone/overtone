(ns
    ^{:doc "An interface to the SuperCollider synthesis server.
          This is at heart an OSC client library for the SuperCollider
          scsynth DSP engine."
      :author "Jeff Rose"}
  overtone.sc.core
  (:import
   [java.net InetSocketAddress]
   [java.util.regex Pattern]
   [java.util.concurrent TimeUnit TimeoutException]
   [java.io BufferedInputStream])
  (:require [overtone.log :as log]
            [scsynth :as sc])
  (:use
   [overtone event config log setup util time-utils deps]
   [overtone.sc allocator]
   [clojure.contrib.java-utils :only [file]]
   [clojure.contrib pprint]
   [clojure.contrib shell-out]
   osc))

(def SERVER-HOST "127.0.0.1")
(def SERVER-PORT nil) ; nil means a random port
(def N-RETRIES 20)

;; Max number of milliseconds to wait for a reply from the server
(def REPLY-TIMEOUT 500)

(def MAX-OSC-SAMPLES 8192)
(def ROOT-GROUP 0)

(defonce sc* (ref {:type       nil           ; :internal or :external
                   :connection nil           ; osc connection
                   :thread     nil           ; server thread
                   :world      nil           ; internal sc pointer
                   :log        []            ; log
                   :status     :disconnected ; current status
                   }))

(defonce synth-group*   (ref nil))

;; The base handler for receiving osc messages just forwards the message on
;; as an event using the osc path as the event key.
(on-sync-event :osc-msg-received ::osc-receiver
  (fn [{{path :path args :args} :msg}]
    (event path :path path :args args)))

;; ## Basic communication with the synth server

(defmacro at
  "All messages sent within the body will be sent in the same timestamped OSC
  bundle.  This bundling is thread-local, so you don't have to worry about
  accidentally scheduling packets into a bundle started on another thread."
  [time-ms & body]
  `(in-osc-bundle (:connection @sc*) ~time-ms (do ~@body)))

(defn connected? []
  (= :connected (:status @sc*)))

(defn snd
  "Sends an OSC message."
  [path & args]
  (log/debug "(snd " path args ")")
  (apply osc-send (:connection @sc*) path args)
  (if (not (connected?))
    (log/debug "### trying to snd while disconnected! ###")))

;; ## Event notifications from the server
;;
;; These messages are sent as notification of some event to all clients who have registered via the /notify command .
;; All of these have the same arguments:
;;   int - node ID
;;   int - the node's parent group ID
;;   int - previous node ID, -1 if no previous node.
;;   int - next node ID, -1 if no next node.
;;   int - 1 if the node is a group, 0 if it is a synth
;;
;; The following two arguments are only sent if the node is a group:
;;   int - the ID of the head node, -1 if there is no head node.
;;   int - the ID of the tail node, -1 if there is no tail node.
;;
;;   /n_go   - a node was created
;;   /n_end  - a node was destroyed
;;   /n_on   - a node was turned on
;;   /n_off  - a node was turned off
;;   /n_move - a node was moved
;;   /n_info - in reply to /n_query

(defn notify
  "Turn on notification messages from the audio server.  This lets us free
  synth IDs when they are automatically freed with envelope triggers.  It also
  lets us receive custom messages from various trigger ugens."
  [notify?]
  (snd "/notify" (if (false? notify?) 0 1)))

(defn connect-jack-ports
  "Connect the jack input and output ports as best we can.  If jack ports are
  always different names with different drivers or hardware then we need to find
  a better strategy to auto-connect."
  ([] (connect-jack-ports 2))
  ([n-channels]
     (let [port-list (sh "jack_lsp")
           sc-ins         (re-seq #"SuperCollider.*:in_[0-9]*" port-list)
           sc-outs        (re-seq #"SuperCollider.*:out_[0-9]*" port-list)
           system-ins     (re-seq #"system:capture_[0-9]*" port-list)
           system-outs    (re-seq #"system:playback_[0-9]*" port-list)
           interface-ins  (re-seq #"system:AC[0-9]*_dev[0-9]*_.*In.*" port-list)
           interface-outs (re-seq #"system:AP[0-9]*_dev[0-9]*_LineOut.*" port-list)
           connections (partition 2 (concat
                                     (interleave sc-outs system-outs)
                                     (interleave sc-outs interface-outs)
                                     (interleave system-ins sc-ins)
                                     (interleave interface-ins sc-ins)))]
       (doseq [[src dest] connections]
         (sh "jack_connect" src dest)
         (log/info "jack_connect " src dest)))))

;; We have to do this to handle the change in SC, where they added a "/" to the
;; status.reply messsage, which it should have had in the first place.
(defn- setup-connect-handlers []
  (let [handler-fn
        (fn []
          (dosync (alter sc* assoc :status :connected))
          (notify true) ; turn on notifications now that we can communicate
          (satisfy-deps :connected)
          (event :connected)
          (remove-handler "status.reply" ::connected-handler1)
          (remove-handler "/status.reply" ::connected-handler2))]
    (on-sync-event "status.reply" ::connected-handler1 handler-fn)
    (on-sync-event "/status.reply" ::connected-handler2 handler-fn)))

(defn connect-internal
  []
  (log/debug "Connecting to internal SuperCollider server")
  (let [reply-fn (sc/make-reply-callback
                   (fn [buf]
                     (let [msg (osc-decode-packet buf)]
                       (event :osc-msg-received
                              :msg msg))))
        send-fn (fn [peer-obj buffer]
                  (sc/send-packet (:world @sc*) reply-fn buffer))
        peer (assoc (osc-peer) :send-fn send-fn)]
    (dosync (alter sc* assoc :connection peer))
    (setup-connect-handlers)
    (snd "/status")))

(defn connect-external
  [host port]
  (log/debug "Connecting to external SuperCollider server: " host ":" port)
  (let [sc-server (osc-client host port)]
    (osc-listen sc-server #(event :osc-msg-received :msg %))
    (dosync
     (alter sc* assoc :connection sc-server))

    (setup-connect-handlers)
    (snd "/status")

;; Send /status in a loop until we get a reply
    (loop [cnt 0]
      (log/debug "connect loop...")
      (when (and (< cnt N-RETRIES)
                 (= (:status @sc*) :connecting))
        (log/debug "sending status...")
        (snd "/status")
        (Thread/sleep 100)
        (recur (inc cnt))))))

;; TODO: setup an error-handler in the case that we can't connect to the server
(defn connect
  "Connect to an running SC audio server. Either an external server if host and
  port are passed or an internal server in the case of no args."
  ([] (connect-internal))
  ([port] (connect "127.0.0.1" port))
  ([host port]
   (dosync (alter sc* assoc :status :connecting))
   (.run (Thread. #(connect-external host port)))))

(defn server-log
  "Print the server log."
  []
  (doseq [msg (:log @sc*)]
    (print msg)))

(defn recv
  "Register your intent to wait for a message associated with given path to be
  received from the server. Returns a promise that will contain the message once
  it has been received. Does not block current thread (this only happens once
  you try and look inside the promise and the reply has not yet been received)."
  [path]
  (let [p (promise)]
    (on-sync-event path (uuid) #(do (deliver p %) :done))
    p))

(defn- parse-status [args]
  (let [[_ ugens synths groups loaded avg peak nominal actual] args]
    {:n-ugens ugens
     :n-synths synths
     :n-groups groups
     :n-loaded-synths loaded
     :avg-cpu avg
     :peak-cpu peak
     :nominal-sample-rate nominal
     :actual-sample-rate actual}))

(def STATUS-TIMEOUT 500)

;;Replies to sender with the following message.
;;status.reply
;;      int - 1. unused.
;;      int - number of unit generators.
;;      int - number of synths.
;;      int - number of groups.
;;      int - number of loaded synth definitions.
;;      float - average percent CPU usage for signal processing
;;      float - peak percent CPU usage for signal processing
;;      double - nominal sample rate
;;      double - actual sample rate
(defn status
  "Check the status of the audio server."
  []
  (if (connected?)
    (let [p (promise)
          handler (fn [event]
                    (deliver p (parse-status (:args event)))
                    (remove-handler "status.reply" ::status-check)
                    (remove-handler "/status.reply" ::status-check))]
      (on-event "/status.reply" ::status-check handler)
      (on-event "status.reply" ::status-check handler)

      (snd "/status")
      (try
        (.get (future @p) STATUS-TIMEOUT TimeUnit/MILLISECONDS)
        (catch TimeoutException t
          :timeout)))
    (:status @sc*)))

(defn wait-sync
  "Wait until the audio server has completed all asynchronous commands
  currently in execution."
  [& [timeout]]
  (let [sync-id (rand-int 999999)
        reply-p (recv "/synced")
        _ (snd "/sync" sync-id)
        reply (await-promise! reply-p (if timeout timeout REPLY-TIMEOUT))
        reply-id (first (:args reply))]
    (= sync-id reply-id)))

(def SC-PATHS {:linux "scsynth"
               :windows "C:/Program Files/SuperCollider/scsynth.exe"
               :mac  "/Applications/SuperCollider/scsynth" })

(def SC-ARGS  {:linux []
               :windows []
               :mac   ["-U" "/Applications/SuperCollider/plugins"] })

(if (= :linux (@config* :os))
  (on-deps :connected ::connect-jack-ports #(connect-jack-ports)))

;;TODO: make use of the port or remove it as a param.
;;      should we be able to get the internal server to listen
;;      for external processes on a given port?
(defn- internal-booter [port]
  (log/info "booting internal audio server listening on port: " port)
  (let [server (sc/start "native/linux/x86/ugens")]
    (dosync (alter sc* assoc :world server))
    (future
      (Thread/sleep 3000)
      (satisfy-deps :booted))))

(defn- boot-internal
  ([] (boot-internal (+ (rand-int 50000) 2000)))
  ([port]
     (log/info "boot-internal: " port)
     (when (not (connected?))
       (on-deps :booted ::connect-internal connect)
       (let [sc-thread (Thread. #(internal-booter port))]
         (.setDaemon sc-thread true)
         (log/debug "Booting SuperCollider internal server (scsynth)...")
         (.start sc-thread)
         (dosync (alter sc* assoc :thread sc-thread))
         :booting))))

(defn- sc-log
  "Pull audio server log data from a pipe and store for later printing."
  [stream read-buf]
  (while (pos? (.available stream))
    (let [n (min (count read-buf) (.available stream))
          _ (.read stream read-buf 0 n)
          msg (String. read-buf 0 n)]
      (dosync (alter sc* assoc :log (conj (:log @sc*) msg)))
      (log/info (String. read-buf 0 n)))))

(defn- external-booter
  "Boot thread to start the external audio server process and hook up to
  STDOUT for log messages."
  [cmd]
  (log/debug "booting external audio server...")
  (let [proc (.exec (Runtime/getRuntime) cmd)
        in-stream (BufferedInputStream. (.getInputStream proc))
        err-stream (BufferedInputStream. (.getErrorStream proc))
        read-buf (make-array Byte/TYPE 256)]
    (while (not (= :disconnected (:status @sc*)))
      (sc-log in-stream read-buf)
      (sc-log err-stream read-buf)
      (Thread/sleep 250))
    (.destroy proc)))

(defn- boot-external
  "Boot the audio server in an external process and tell it to listen on a
  specific port."
  ([port]
     (if (not (connected?))
       (let [cmd (into-array String (concat [(SC-PATHS (@config* :os)) "-u" (str port)] (SC-ARGS (@config* :os))))
             sc-thread (Thread. #(external-booter cmd))]
         (.setDaemon sc-thread true)
         (log/debug "Booting SuperCollider server (scsynth)...")
         (.start sc-thread)
         (dosync (alter sc* assoc :thread sc-thread))
         (connect "127.0.0.1" port)
         :booting))))

(defn boot
  "Boot either the internal or external audio server."
  ([]
     (boot (get @config* :connection :internal) SERVER-HOST SERVER-PORT))
  ([which & [port]]
     (let [port (if (nil? port) (+ (rand-int 50000) 2000) port)]
       (cond
        (= :internal which) (boot-internal port)
        (= :external which) (boot-external port)))))

(defn wait-until-connected
  "Makes the current thread sleep until scsynth has successfully connected"
  []
  (while (not (connected?))
    (Thread/sleep 100)))

(defn quit
  "Quit the SuperCollider synth process."
  []
  (log/info "quiting...")
  (sync-event :shutdown)
  (snd "/quit")
  (if (:connection @sc*)
    (osc-close (:connection @sc*) true))
  (dosync
   (alter sc* assoc
          :connection nil
          :status :disconnected)
   (unsatisfy-all-dependencies)))

(defonce _shutdown-hook
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. quit)))

(defn clear-msg-queue
  "Remove any scheduled OSC messages from the run queue."
  []
  (snd "/clearSched"))

;; The /done message just has a single argument:
;; "/done" "s" <completed-command>
;;
;; where the command would be /b_alloc and others.
(defn on-done
  "Runs a one shot handler that takes no arguments when an OSC /done
  message from scsynth arrives with a matching path.  Look at load-sample
  for an example of usage.
  "
  [path handler]
  (on-event "/done" (uuid)
            #(if (= path (first (:args %)))
               (do
                 (handler)
                 :done))))

(defn stop []
  (event :reset))

(def osc-log* (atom []))

(defn osc-log [on?]
  (if on?
    (on-sync-event :osc-msg-received ::osc-logger
                   (fn [{:keys [path args] :as msg}]
                     (swap! osc-log* #(conj % msg))))
    (remove-handler :osc-msg-received ::osc-logger)))

(defn sc-debug
  "Control debug output from both the Overtone and the audio server."
  [on?]
  (if on?
    (do
      (log/level :debug)
      (osc-debug true)
      (snd "/dumpOSC" 1))
    (do
      (log/level :error)
      (osc-debug false)
      (snd "/dumpOSC" 0))))


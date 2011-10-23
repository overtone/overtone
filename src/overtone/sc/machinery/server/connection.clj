(ns overtone.sc.machinery.server.connection
  (:import [java.io BufferedInputStream]
           [supercollider ScSynth ScSynthStartedListener MessageReceivedListener])
  (:use [clojure.java shell]
        [overtone.config.store]
        [overtone.libs event deps]
        [overtone.util.lib :only [print-ascii-art-overtone-logo]]
        [overtone.sc.machinery defaults]
        [overtone.sc.machinery.server comms]
        [overtone.osc]
        [overtone.osc.decode :only [osc-decode-packet]])
  (:require [overtone.util.log :as log]))

(defonce server-thread*       (ref nil))
(defonce sc-world*            (ref nil))
(defonce external-server-log* (ref []))
(defonce connection-info*     (ref {}))
(defonce connection-status*   (ref :disconnected))

(defn- server-notifications-on
  "Turn on notification messages from the audio server.  This lets us free
  synth IDs when they are automatically freed with envelope triggers.  It also
  lets us receive custom messages from various trigger ugens.

  These messages are sent as notification of some event to all clients who have
  registered via the /notify command .

  All of these have the same arguments:
   int - node ID
   int - the node's parent group ID
   int - previous node ID, -1 if no previous node.
   int - next node ID, -1 if no next node.
   int - 1 if the node is a group, 0 if it is a synth

  The following two arguments are only sent if the node is a group:
   int - the ID of the head node, -1 if there is no head node.
   int - the ID of the tail node, -1 if there is no tail node.

   /n_go   - a node was created
   /n_end  - a node was destroyed
   /n_on   - a node was turned on
   /n_off  - a node was turned off
   /n_move - a node was moved
   /n_info - in reply to /n_query"
  []
  (server-snd "/notify" 1))

(defn- connect-jack-ports
  "Connect the jack input and output ports as best we can.  If jack ports are
  always different names with different drivers or hardware then we need to find
  a better strategy to auto-connect. (For Linux users)"
  ([] (connect-jack-ports 2))
  ([n-channels]
     (let [port-list (:out (sh "jack_lsp"))
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

(if (= :linux (@config* :os))
  (on-deps :server-connected ::connect-jack-ports #(connect-jack-ports)))

;; We have to do this to handle the change in SC, where they added a "/" to the
;; status.reply messsage, which it should have had in the first place.
(defn- setup-connect-handlers []
  (let [handler-fn
        (fn []
          (dosync
           (ref-set connection-status* :connected))
          (server-notifications-on) ; turn on notifications now that we can communicate
          (satisfy-deps :server-connected)
          (event :connection-complete)
          (remove-handler "status.reply" ::connected-handler1)
          (remove-handler "/status.reply" ::connected-handler2))]
    (on-sync-event "status.reply" handler-fn ::connected-handler1)
    (on-sync-event "/status.reply" handler-fn ::connected-handler2)))

(defn- connect-internal
  []
  (log/debug "Connecting to internal SuperCollider server")
  (let [send-fn (fn [peer-obj buffer]
                  (.send @sc-world* buffer))
        peer (assoc (osc-peer) :send-fn send-fn)]
    (.addMessageReceivedListener @sc-world*
                                 (proxy [MessageReceivedListener] []
                                   (messageReceived [buf size]
                                     (event :osc-msg-received
                                            :msg (osc-decode-packet buf)))))
    (dosync (ref-set server-osc-peer* peer))
    (setup-connect-handlers)
    (server-snd "/status")))

(defn- external-connection-runner
  [host port]
  (log/debug "Connecting to external SuperCollider server: " host ":" port)
  (let [sc-server (osc-client host port)]
    (osc-listen sc-server #(event :osc-msg-received :msg %))
    (dosync
     (ref-set server-osc-peer* sc-server))

    (setup-connect-handlers)
    (server-snd "/status")

    ;; Send /status in a loop until we get a reply
    (loop [cnt 0]
      (log/debug "connect loop...")
      (when-not (= :connected @connection-status*)
        (if (< cnt N-RETRIES)
          (do
            (log/debug (str "sending status... (" cnt ")"  ))
            (server-snd "/status")
            (Thread/sleep 100)
            (recur (inc cnt)))
          (throw (Exception. (str "Error: unable to connect to externally booted server after " N-RETRIES " attempts.")))))))
  (print-ascii-art-overtone-logo))

;; TODO: setup an error-handler in the case that we can't connect to the server
(defn connect
  "Connect to an externally running SC audio server.

  (connect 57710)                  ;=> connect to an external server on the
                                       localhost listening to port 57710
  (connect \"192.168.1.23\" 57110) ;=> connect to an external server with ip
                                       address 192.168.1.23 listening to port
                                       57110"
  ([port] (connect "127.0.0.1" port))
  ([host port]
     (.run (Thread. #(external-connection-runner host port)))))

(defn- internal-booter
  "Fn to actually boot internal server. Typically called within a thread."
  []
  (log/info "booting internal audio server")
  (on-deps :internal-server-booted ::connect-internal connect-internal)
  (let [server (ScSynth.)
        listener (reify ScSynthStartedListener
                   (started [this]
                     (log/info "Boot listener has detected the internal server has booted...")
                     (satisfy-deps :internal-server-booted)))]
    (.addScSynthStartedListener server listener)
    (dosync (ref-set sc-world* server))
    (.run server)))

(defn- boot-internal-server
  "Boots internal server by executing it on a daemon thread."
  []
  (let [sc-thread (Thread. internal-booter)]
    (.setDaemon sc-thread true)
    (log/debug "Booting SuperCollider internal server (scsynth)...")
    (.start sc-thread)
    (dosync (ref-set server-thread* sc-thread))
    :booting))

(defn- sc-log-external
  "Pull audio server log data from a pipe and store for later printing."
  [stream read-buf]
  (while (pos? (.available stream))
    (let [n (min (count read-buf) (.available stream))
          _ (.read stream read-buf 0 n)
          msg (String. read-buf 0 n)]
      (dosync (alter external-server-log* conj msg))
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
    (while (not (= :disconnected @connection-status*))
      (sc-log-external in-stream read-buf)
      (sc-log-external err-stream read-buf)
      (Thread/sleep 250))
    (.destroy proc)))

(defn- find-sc-path
  "Find the path for SuperCollider. If linux don't check for a file as it should
  be in the PATH list."
  []
  (let [os    (@config* :os)
        paths (SC-PATHS os)
        path  (if (= :linux os)
                (first paths)
                (first (filter #(.exists (java.io.File. %)) paths)))]
    (when-not path
      (throw (Exception. (str "Unable to locate a valid scsynth executable on your system. I looked in the following places: " paths))))

    path))

(defn- boot-external-server
  "Boot the audio server in an external process and tell it to listen on a
  specific port."
  ([port]
     (when-not (= :connected @connection-status*)
       (log/debug "booting external server")
       (let [sc-path (find-sc-path)
             cmd (into-array String (concat [sc-path "-u" (str port)] (SC-ARGS (@config* :os))))
             sc-thread (Thread. #(external-booter cmd))]
         (.setDaemon sc-thread true)
         (log/debug (str "Booting SuperCollider server (scsynth) with cmd: " cmd))
         (.start sc-thread)
         (dosync (ref-set server-thread* sc-thread))
         (connect "127.0.0.1" port)
         :booting))))

(defn boot
  "Boot either the internal or external audio server. If specified port is nil
  will choose a random port.

   (boot) ; uses the default settings defined in your config
   (boot :internal) ; boots the internal server
   (boot :external) ; boots an external server on a random port
   (boot :external 57110) ; boots an external server listening on port 57110"
  ([]                (boot (get @config* :server :internal) SERVER-PORT))
  ([connection-type] (boot connection-type SERVER-PORT))
  ([connection-type port]
     (locking connection-info*
       (when-not (= :disconnected @connection-status*)
         (throw (Exception. "Can't boot as a server is already connected/connecting!")))

       (dosync
        (ref-set connection-status* :connecting))

       (let [port (if (nil? port) (+ (rand-int 50000) 2000) port)]
         (cond
          (= :internal connection-type) (boot-internal-server)
          (= :external connection-type) (boot-external-server port))
         (wait-until-deps-satisfied :server-ready)

         (dosync
          (cond
           (= :internal connection-type)
           (ref-set connection-info* {:connection connection-type})

           (= :external connection-type)
           (ref-set connection-info* {:connection connection-type
                                      :port port
                                      :host "127.0.0.1"})))))))

(defn shutdown-server
  "Quit the SuperCollider synth process."
  []
  (locking connection-info*
    (when-not (= :connected @connection-status*)
      (throw (Exception. "Can't kill unconnected server.")))

    (log/info "quiting...")
    (sync-event :shutdown)
    (server-snd "/quit")

    (when @server-osc-peer*
      (osc-close @server-osc-peer* true))

    (dosync
     (ref-set server-osc-peer* nil)
     (ref-set connection-info* {})
     (ref-set connection-status* :disconnected)
     (unsatisfy-all-dependencies))))

(defonce _shutdown-hook
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. shutdown-server)))

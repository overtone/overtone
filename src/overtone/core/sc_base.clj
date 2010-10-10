(ns
  ^{:doc "Handle booting scsynth, connecting audio ports, and low-level communications."
     :author "Jeff Rose"}
  overtone.core.sc-base
  (:import
    (java.net InetSocketAddress)
    (java.util.regex Pattern)
    (java.util.concurrent TimeUnit TimeoutException)
    (java.io BufferedInputStream)
    (supercollider ScSynth ScSynthStartedListener MessageReceivedListener)
    (java.util BitSet))
  (:require [overtone.core.log :as log])
  (:use
    (overtone.core event config setup util time-utils)
    [clojure.contrib.java-utils :only [file]]
    (clojure.contrib shell-out)
    osc))

(def N-RETRIES 20)

(defonce server*        (ref nil))
(defonce server-thread* (ref nil))
(defonce server-log*    (ref []))

(defonce sc-world*      (ref nil))
(defonce running?*      (atom false))
(defonce status*        (ref :no-audio))

(defn snd*
  [path & args]
  (apply osc-send @server* path args))

; Notifications from Server
; These messages are sent as notification of some event to all clients who have registered via the /notify command .
; All of these have the same arguments:
;   int - node ID
;   int - the node's parent group ID
;   int - previous node ID, -1 if no previous node.
;   int - next node ID, -1 if no next node.
;   int - 1 if the node is a group, 0 if it is a synth
;
; The following two arguments are only sent if the node is a group:
;   int - the ID of the head node, -1 if there is no head node.
;   int - the ID of the tail node, -1 if there is no tail node.
;
;   /n_go   - a node was created
;   /n_end  - a node was destroyed
;   /n_on   - a node was turned on
;   /n_off  - a node was turned off
;   /n_move - a node was moved
;   /n_info - in reply to /n_query

(defn notify
  "Turn on notification messages from the audio server.  This lets us free
  synth IDs when they are automatically freed with envelope triggers.  It also lets
  us receive custom messages from various trigger ugens."
  [notify?]
  (snd* "/notify" (if (false? notify?) 0 1)))

(defn connect-internal
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
    (dosync (ref-set server* peer))
    (snd* "/status")
    (dosync (ref-set status* :connected))
    (notify true) ; turn on notifications now that we can communicate
    (event :reset)
    (event :connected)))

(defn connect-external
  [host port]
  (log/debug "Connecting to external SuperCollider server: " host ":" port)
  (let [sc-server (osc-client host port)]
    (osc-listen sc-server #(event :osc-msg-received :msg %))
    (dosync
      (ref-set server* sc-server)
      (ref-set status* :connecting))

    ; Runs once when we receive the first status.reply message
    (on-event "status.reply" ::connected-handler
        #(do
           (dosync (ref-set status* :connected))
           (notify true) ; turn on notifications now that we can communicate
           (event :reset)
           (event :connected)
           :done))

    ; Send /status in a loop until we get a reply
    (loop [cnt 0]
      (log/debug "connect loop...")
      (when (and (< cnt N-RETRIES)
                 (= @status* :connecting))
        (log/debug "sending status...")
        (snd* "/status")
        (Thread/sleep 100)
        (recur (inc cnt))))))

(defn connect-jack-ports
  "Connect the jack input and output ports as best we can.  If jack ports are always different
  names with different drivers or hardware then we need to find a better strategy to auto-connect."
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

(def SC-PATHS {:linux "scsynth"
               :windows "C:/Program Files/SuperCollider/scsynth.exe"
               :mac  "/Applications/SuperCollider/scsynth" })

(def SC-ARGS  {:linux []
               :windows []
               :mac   ["-U" "/Applications/SuperCollider/plugins"] })

(if (= :linux (@config* :os))
  (on-event :connected ::jack-connector
            #(connect-jack-ports)))

(defonce scsynth-server* (ref nil))

(defn internal-booter [port]
  (reset! running?* true)
  (log/info "booting internal audio server listening on port: " port)
  (let [server (ScSynth.)]
    (.addScSynthStartedListener server (proxy [ScSynthStartedListener] []
                                         (started [] (event :booted))))
    (dosync (ref-set sc-world* server))
    (.run server)))

(defn boot-internal
  ([] (boot-internal (+ (rand-int 50000) 2000)))
  ([port]
   (log/info "boot-internal: " port)
   (if (not @running?*)
     (let [sc-thread (Thread. #(internal-booter port))]
       (.setDaemon sc-thread true)
       (log/debug "Booting SuperCollider internal server (scsynth)...")
       (.start sc-thread)
       (dosync (ref-set server-thread* sc-thread))
       (on-event :booted ::internal-boot-connector #(event :connect))
       :booting))))

(defn- sc-log
  "Pull audio server log data from a pipe and store for later printing."
  [stream read-buf]
  (while (pos? (.available stream))
    (let [n (min (count read-buf) (.available stream))
          _ (.read stream read-buf 0 n)
          msg (String. read-buf 0 n)]
      (dosync (alter server-log* conj msg))
      (log/info (String. read-buf 0 n)))))

(defn- external-booter
  "Boot thread to start the external audio server process and hook up to
  STDOUT for log messages."
  [cmd]
  (reset! running?* true)
  (log/debug "booting external audio server...")
  (let [proc (.exec (Runtime/getRuntime) cmd)
        in-stream (BufferedInputStream. (.getInputStream proc))
        err-stream (BufferedInputStream. (.getErrorStream proc))
        read-buf (make-array Byte/TYPE 256)]
    (while @running?*
      (sc-log in-stream read-buf)
      (sc-log err-stream read-buf)
      (Thread/sleep 250))
    (.destroy proc)))

(defn boot-external
  "Boot the audio server in an external process and tell it to listen on a
  specific port."
  ([port]
   (if (not @running?*)
     (let [cmd (into-array String (concat [(SC-PATHS (@config* :os)) "-u" (str port)] (SC-ARGS (@config* :os))))
           sc-thread (Thread. #(external-booter cmd))]
       (.setDaemon sc-thread true)
       (log/debug "Booting SuperCollider server (scsynth)...")
       (.start sc-thread)
       (dosync (ref-set server-thread* sc-thread))
       (event :connect :host "127.0.0.1" :port port)
       :booting))))

;/g_queryTree				get a representation of this group's node subtree.
;	[
;		int - group ID
;		int - flag: if not 0 the current control (arg) values for synths will be included
;	] * N
;
; Request a representation of this group's node subtree, i.e. all the groups and
; synths contained within it. Replies to the sender with a /g_queryTree.reply
; message listing all of the nodes contained within the group in the following
; format:
;
;	int - flag: if synth control values are included 1, else 0
;	int - node ID of the requested group
;	int - number of child nodes contained within the requested group
;	then for each node in the subtree:
;	[
;		int - node ID
;		int - number of child nodes contained within this node. If -1this is a synth, if >=0 it's a group
;		then, if this node is a synth:
;		symbol - the SynthDef name for this node.
;		then, if flag (see above) is true:
;		int - numControls for this synth (M)
;		[
;			symbol or int: control name or index
;			float or symbol: value or control bus mapping symbol (e.g. 'c1')
;		] * M
;	] * the number of nodes in the subtree

(def *data* nil)

(defn- parse-synth-tree
  [id ctls?]
  (let [sname (first *data*)]
    (if ctls?
      (let [n-ctls (second *data*)
            [ctl-data new-data] (split-at (* 2 n-ctls) (nnext *data*))
            ctls (apply hash-map ctl-data)]
        (set! *data* new-data)
        {:synth sname
         :id id
         :controls ctls})
      (do
        (set! *data* (next *data*))
        {:synth sname
         :id id}))))

(defn- parse-node-tree-helper [ctls?]
  (let [[id n-children & new-data] *data*]
    (set! *data* new-data)
    (cond
      (neg? n-children) (parse-synth-tree id ctls?) ; synth
      (= 0 n-children) {:group id :children nil}
      (pos? n-children)
      {:group id
       :children (doall (map (fn [i] (parse-node-tree-helper ctls?)) (range n-children)))})))

(defn parse-node-tree [data]
  (let [ctls? (= 1 (first data))]
    (binding [*data* (next data)]
      (parse-node-tree-helper ctls?))))


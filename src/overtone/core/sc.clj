(ns
  ^{:doc "An interface to the SuperCollider synthesis server.
          This is at heart an OSC client library for the SuperCollider
          scsynth DSP engine."
     :author "Jeff Rose"}
  overtone.core.sc
  (:import
    (java.net InetSocketAddress)
    (java.util.regex Pattern)
    (java.util.concurrent TimeUnit TimeoutException)
    (java.io BufferedInputStream)
    (supercollider ScSynth ScSynthStartedListener MessageReceivedListener)
    (java.util BitSet))
  (:require [overtone.core.log :as log])
  (:use
    (overtone.core event config setup util time-utils synthdef)
    [clojure.contrib.java-utils :only [file]]
    (clojure.contrib shell-out pprint)
    osc))

; TODO: Make this work correctly
; NOTE: "localhost" doesn't work, at least on my laptopt
(defonce SERVER-HOST "127.0.0.1")
(defonce SERVER-PORT nil) ; nil means a random port

; Max number of milliseconds to wait for a reply from the server
(defonce REPLY-TIMEOUT 500)

(defonce server-thread* (ref nil))
(defonce server*        (ref nil))
(defonce status*        (ref :no-audio))
(defonce sc-world*      (ref nil))

; Server limits
(defonce MAX-NODES 1024)
(defonce MAX-BUFFERS 1024)
(defonce MAX-SDEFS 1024)
(defonce MAX-AUDIO-BUS 128)
(defonce MAX-CONTROL-BUS 4096)
(defonce MAX-OSC-SAMPLES 8192)

; We use bit sets to store the allocation state of resources on the audio server.
; These typically get allocated on usage by the client, and then freed either by
; client request or automatically by receiving notifications from the server.
; (e.g. When an envelope trigger fires to free a synth.)
(defonce allocator-bits
  {:node         (BitSet. MAX-NODES)
   :audio-buffer (BitSet. MAX-BUFFERS)
   :audio-bus    (BitSet. MAX-NODES)
   :control-bus  (BitSet. MAX-NODES)})

(defonce allocator-limits
  {:node        MAX-NODES
   :sdefs       MAX-SDEFS
   :audio-bus   MAX-AUDIO-BUS
   :control-bus MAX-CONTROL-BUS})

(defn alloc-id
  "Allocate a new ID for the type corresponding to key."
  [k]
  (let [bits  (get allocator-bits k)
        limit (get allocator-limits k)]
    (locking bits
      (let [id (.nextClearBit bits 0)]
        (if (= limit id)
          (throw (Exception. (str "No more " (name k) " ids available!")))
          (do
            (.set bits id)
            id))))))

(defn free-id
  "Free the id of type key."
  [k id]
  (let [bits (get allocator-bits k)
        limit (get allocator-limits k)]
    (locking bits
      (.clear bits id))))

(defn all-ids
  "Get all of the currently allocated ids for key."
  [k]
  (let [bits (get allocator-bits k)
        limit (get allocator-limits k)]
    (locking bits
      (loop [ids []
             idx 0]
        (let [id (.nextSetBit bits idx)]
          (if (and (> id -1) (< idx limit))
            (recur (conj ids id) (inc id))
            ids))))))

(defn clear-ids
  "Clear all ids allocated for key."
  [k]
  (println "clear-ids........................")
  (locking (get allocator-bits k)
    (doseq [id (all-ids k)]
      (free-id k id))))

(defonce ROOT-GROUP 0)
(defonce SYNTH-GROUP 1)

(defn connected? []
  (= :connected @status*))

(declare boot)

; The base handler for receiving osc messages just forwards the message on
; as an event using the osc path as the event key.
(on ::osc-msg-received (fn [{{path :path args :args} :msg}]
                         (event path :path path :args args)))

(defn snd
  "Sends an OSC message."
  [path & args]
  (if (connected?)
    (apply osc-send @server* path args)
    (log/debug "### trying to snd while disconnected! ###"))
  (log/debug "(snd " path args ")"))

(defmacro at
  "All messages sent within the body will be sent in the same timestamped OSC
  bundle.  This bundling is thread-local, so you don't have to worry about
  accidentally scheduling packets into a bundle started on another thread."
  [time-ms & body]
  `(in-osc-bundle @server* ~time-ms (do ~@body)))

(defn debug
  "Control debug output from both the Overtone and the audio server."
  [& [on-off]]
  (if (or on-off (nil? on-off))
    (do
      (log/level :debug)
      (osc-debug true)
      (snd "/dumpOSC" 1))
    (do
      (log/level :error)
      (osc-debug false)
      (snd "/dumpOSC" 0))))

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
;
; Trigger Notifications
;
; This command is the mechanism that synths can use to trigger events in
; clients.  The node ID is the node that is sending the trigger. The trigger ID
; and value are determined by inputs to the SendTrig unit generator which is
; the originator of this message.
;
; /tr a trigger message
;
;   int - node ID
;   int - trigger ID
;   float - trigger value

(defn notify
  "Turn on notification messages from the audio server.  This lets us free
  synth IDs when they are automatically freed with envelope triggers.  It also lets
  us receive custom messages from various trigger ugens."
  [notify?]
  (snd "/notify" (if (false? notify?) 0 1)))

(defn- node-destroyed
  "Frees up a synth node to keep in sync with the server."
  [id]
  (log/debug (format "node-destroyed: %d" id))
  (free-id :node id))

(defn- node-created
  "Called when a node is created on the synth."
  [id]
  (log/debug (format "node-created: %d" id)))

; Setup the feedback handlers with the audio server.
(on "/n_end" #(node-destroyed (first (:args %))))
(on "/n_go" #(node-created (first (:args %))))

(def N-RETRIES 20)

(declare reset)

(defn- connect-internal
  []
  (log/debug "Connecting to internal SuperCollider server")
  (let [send-fn (fn [peer-obj buffer]
                  (.send @sc-world* buffer))
        peer (assoc (osc-peer) :send-fn send-fn)]
    (.addMessageReceivedListener @sc-world*
                                 (proxy [MessageReceivedListener] []
                                   (messageReceived [buf size]
                                                    (event ::osc-msg-received
                                                           :msg (osc-decode-packet buf)))))
    (dosync (ref-set server* peer))
    (snd "/status")
    (dosync (ref-set status* :connected))
    (notify true) ; turn on notifications now that we can communicate
    (reset)
    (event :connected)))

(defn- connect-external
  [host port]
  (log/debug "Connecting to external SuperCollider server: " host ":" port)
  (let [sc-server (osc-client host port)]
    (osc-listen sc-server #(event ::osc-msg-received :msg %))
    (dosync
      (ref-set server* sc-server)
      (ref-set status* :connecting))

    ; Runs once when we receive the first status.reply message
    (on "status.reply"
        #(do
           (dosync (ref-set status* :connected))
           (notify true) ; turn on notifications now that we can communicate
           (reset)
           (event :connected)
           :done))

    ; Send /status in a loop until we get a reply
    (loop [cnt 0]
      (log/debug "connect loop...")
      (when (and (< cnt N-RETRIES)
                 (= @status* :connecting))
        (log/debug "sending status...")
        (snd "/status")
        (Thread/sleep 100)
        (recur (inc cnt))))))

; TODO: setup an error-handler in the case that we can't connect to the server
(defn connect
  "Connect to an external SC audio server on the specified host and port."
  [& [host port]]
   (if (and host port)
     (.run (Thread. #(connect-external host port)))
     (connect-internal))

  )

(defonce running?* (atom false))

(def server-log* (ref []))

(defn server-log
  "Print the server log."
  []
  (doseq [msg @server-log*]
    (print msg)))

;Replies to sender with the following message.
;status.reply
;	int - 1. unused.
;	int - number of unit generators.
;	int - number of synths.
;	int - number of groups.
;	int - number of loaded synth definitions.
;	float - average percent CPU usage for signal processing
;	float - peak percent CPU usage for signal processing
;	double - nominal sample rate
;	double - actual sample rate

(defn recv [path & [timeout]]
  (let [p (promise)]
    (on path #(do (deliver p %) :done))
    (if timeout
      (try
        (.get (future @p) timeout TimeUnit/MILLISECONDS)
        (catch TimeoutException t
          :timeout))
      @p)))

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

(defn status
  "Check the status of the audio server."
  []
  (if (= :connected @status*)
    (let [p (promise)]
      (on "/status.reply" #(do
                             (deliver p (parse-status (:args %)))
                             :done))
      (snd "/status")
      (try
        (.get (future @p) STATUS-TIMEOUT TimeUnit/MILLISECONDS)
        (catch TimeoutException t
          :timeout)))
    @status*))

(defn wait-sync
  "Wait until the audio server has completed all asynchronous commands currently in execution."
  [& [timeout]]
  (let [sync-id (rand-int 999999)
        _ (snd "/sync" sync-id)
        reply (recv "/synced" (if timeout timeout REPLY-TIMEOUT))
        reply-id (first (:args reply))]
    (= sync-id reply-id)))

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

(defonce _jack_connector_
  (if (= :linux (@config* :os))
    (on :connected #(connect-jack-ports))))

(defonce scsynth-server*        (ref nil))

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
       (on :booted connect)
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
       (connect "127.0.0.1" port)
       :booting))))

(defn boot
  "Boot either the internal or external audio server."
  ([] (boot (get @config* :server :internal) SERVER-HOST SERVER-PORT))
  ([which & [host port]]
   (let [port (if (nil? port) (+ (rand-int 50000) 2000) port)]
     (cond
       (= :internal which) (boot-internal port)
       (= :external which) (boot-external host port)))))

(defn quit
  "Quit the SuperCollider synth process."
  []
  (log/info "quiting supercollider")
  (sync-event :quit)
  (when (connected?)
    (snd "/quit")
    (log/debug "SERVER: " @server*)
    (osc-close @server* true))
  (reset! running?* false)
  (dosync (ref-set server* nil)
    (ref-set status* :no-audio)))

; TODO: Come up with a better way to delay shutdown until all of the :quit event handlers
; have executed.  For now we just use 500ms.
(defonce _shutdown-hook (.addShutdownHook (Runtime/getRuntime) (Thread. #(do (quit) (Thread/sleep 500)))))

; Synths, Busses, Controls and Groups are all Nodes.  Groups are linked lists
; and group zero is the root of the graph.  Nodes can be added to a group in
; one of these 5 positions relative to either the full list, or a specified node.
(def POSITION
  {:head         0
   :tail         1
   :before-node  2
   :after-node   3
   :replace-node 4})

;; Sending a synth-id of -1 lets the server choose an ID
(defn node
  "Instantiate a synth node on the audio server.  Takes the synth name and a set of
  argument name/value pairs.  Optionally use :target <node/group-id> and :position <pos>
  to specify where the node should be located.  The position can be one of :head, :tail
  :before-node, :after-node, or :replace-node.

  (node \"foo\")
  (node \"foo\" :pitch 60)
  (node \"foo\" :pitch 60 :target 0)
  (node \"foo\" :pitch 60 :target 2 :position :tail)
  "
  [synth-name & args]
  (if (not (connected?))
    (throw (Exception. "Not connected to synthesis engine.  Please boot or connect.")))
  (let [id (alloc-id :node)
        argmap (apply hash-map args)
        position ((get argmap :position :tail) POSITION)
        target (get argmap :target 0)
        args (flatten (seq (-> argmap (dissoc :position) (dissoc :target))))
        args (stringify (floatify args))]
    ;(println "node " synth-name id position target args)
    (apply snd "/s_new" synth-name id position target args)
    id))

(defn node-free
  "Remove a synth node"
  [& node-ids]
  {:pre [(connected?)]}
  (apply snd "/n_free" node-ids)
  (doseq [id node-ids] (free-id :node id)))

(defn node-run
  "Start a stopped synth node."
  [node-id]
  {:pre [(connected?)]}
  (snd "/n_run" node-id 1))

(defn node-stop
  "Stop a running synth node."
  {:pre [(connected?)]}
  [node-id]
  (snd "/n_run" node-id 0))

(defn node-place
  "Place a node :before or :after another node."
  [node-id position target-id]
  {:pre [(connected?)]}
  (cond
    (= :before position) (snd "/n_before" node-id target-id)
    (= :after  position) (snd "/n_after" node-id target-id)))

(defn node-control
  "Set control values for a node."
  [node-id & name-values]
  {:pre [(connected?)]}
  (apply snd "/n_set" node-id (stringify name-values))
  node-id)

; This can be extended to support setting multiple ranges at once if necessary...
(defn node-control-range
  "Set a range of controls all at once, or if node-id is a group control
  all nodes in the group."
  [node-id ctl-start & ctl-vals]
  {:pre [(connected?)]}
  (apply snd "/n_setn" node-id ctl-start (count ctl-vals) ctl-vals))

(defn node-map-controls
  "Connect a node's controls to a control bus."
  [node-id & names-busses]
  {:pre [(connected?)]}
  (apply snd "/n_map" node-id names-busses))

(defn group
  "Create a new group as a child of the target group."
  [position target-id]
  {:pre [(connected?)]}
  (let [id (alloc-id :node)]
    (snd "/g_new" id (get POSITION position) target-id)
    id))

(defn group-free
  "Free the specified group."
  [& group-ids]
  {:pre [(connected?)]}
  (apply node-free group-ids)
  )


(defn post-tree
  "Posts a representation of this group's node subtree, i.e. all the groups and
  synths contained within it, optionally including the current control values
  for synths."
  [id & [with-args?]]
  {:pre [(connected?)]}
  (snd "/g_dumpTree" id with-args?))

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

(defn- parse-node-tree [data]
  (let [ctls? (= 1 (first data))]
    (binding [*data* (next data)]
      (parse-node-tree-helper ctls?))))

; N.B. The order of nodes corresponds to their execution order on the server.
; Thus child nodes (those contained within a group) are listed immediately
; following their parent. See the method Server:queryAllNodes for an example of
; how to process this reply.
(defn node-tree
  "Returns a data structure representing the current arrangement of groups and synthesizer
  instances residing on the audio server."
  ([] (node-tree 0))
  ([id & [ctls?]]
   (let [ctls? (if (or (= 1 ctls?) (= true ctls?)) 1 0)]
    (snd "/g_queryTree" id ctls?)
    (let [tree (:args (recv "/g_queryTree.reply" REPLY-TIMEOUT))]
      (with-meta (parse-node-tree tree)
                 {:type ::node-tree})))))

(defn prepend-node
  "Add a synth node to the end of a group list."
  [g n]
  (snd "/g_head" g n))

(defn append-node
  "Add a synth node to the end of a group list."
  [g n]
  (snd "/g_tail" g n))

(defn group-clear
  "Free all child synth nodes in a group."
  [group-id]
  (snd "/g_freeAll" group-id))

(defn clear-msg-queue
  "Remove any scheduled OSC messages from the run queue."
  []
  (snd "/clearSched"))

(defn sync-all
  "Wait until all asynchronous server operations have been completed."
  []
  (recv "/synced"))

; The /done message just has a single argument:
; "/done" "s" <completed-command>
;
; where the command would be /b_alloc and others.
(defn on-done
  "Runs a one shot handler that takes no arguments when an OSC /done
  message from scsynth arrives with a matching path.  Look at load-sample
  for an example of usage.
  "
  [path handler]
  (on "/done" #(if (= path (first (:args %)))
                 (do
                   (handler)
                   :done))))

; TODO: Look into multi-channel buffers.  Probably requires adding multi-id allocation
; support to the bit allocator too...
; size is in samples
(defn buffer
  "Allocate a new buffer for storing audio data."
  [size & [channels]]
  (let [channels (or channels 1)
        id (alloc-id :audio-buffer)
        ready? (atom false)]
    (on-done "/b_alloc" #(reset! ready? true))
    (snd "/b_alloc" id size channels)
    (with-meta {:id id
                :size size
                :ready? ready?}
               {:type ::buffer})))

(defn ready?
  "Check whether a sample or a buffer has completed allocating and/or loading data."
  [buf]
  @(:ready? buf))

(defn buffer? [buf]
  (isa? (type buf) ::buffer))

(defn- buf-or-id [b]
  (cond
    (buffer? b) (:id b)
    (number? b) b
    :default (throw (Exception. "Not a valid buffer: " b))))

(defn buffer-free
  "Free an audio buffer and the memory it was consuming."
  [buf]
  (snd "/b_free" (:id buf))
  (free-id :audio-buffer (:id buf))
  :done)

; TODO: Test me...
(defn buffer-read
  "Read a section of an audio buffer."
  [buf start len]
  (assert (buffer? buf))
  (loop [reqd 0]
    (when (< reqd len)
      (let [to-req (min MAX-OSC-SAMPLES (- len reqd))]
        (snd "/b_getn" (:id buf) (+ start reqd) to-req)
        (recur (+ reqd to-req)))))
  (let [samples (float-array len)]
    (loop [recvd 0]
      (if (= recvd len)
        samples
        (let [msg (recv "/b_setn" REPLY-TIMEOUT)
              ;_ (println "b_setn msg: " (take 3 (:args msg)))
              [buf-id bstart blen & samps] (:args msg)]
          (loop [idx bstart
                 samps samps]
            (when samps
              (aset-float samples idx (first samps))
              (recur (inc idx) (next samps))))
          (recur (+ recvd blen)))))))

;; TODO: test me...
(defn buffer-write
  "Write into a section of an audio buffer."
  [buf start len data]
  (assert (buffer? buf))
  (snd "/b_setn" (:id buf) start len data))

(defn save-buffer
  "Save the float audio data in an audio buffer to a wav file."
  [buf path & args]
  (assert (buffer? buf))
  (let [arg-map (merge (apply hash-map args)
                       {:header "wav"
                        :samples "float"
                        :n-frames -1
                        :start-frame 0
                        :leave-open 0})
        {:keys [header samples n-frames start-frame leave-open]} arg-map]
    (snd "/b_write" (:id buf) path header samples
         n-frames start-frame
         leave-open)
    :done))

(defmulti buffer-id type)
(defmethod buffer-id java.lang.Integer [id] id)
(defmethod buffer-id ::buffer [buf] (:id buf))

(defn buffer-data
  "Get the floating point data for a buffer on the internal server."
  [buf]
  (let [buf-id (buffer-id buf)
        snd-buf (.getSndBufAsFloatArray @sc-world* buf-id)]
    snd-buf))

(defn buffer-info [buf]
  (snd "/b_query" (buffer-id buf))
  (let [msg (recv "/b_info" REPLY-TIMEOUT)
        [buf-id n-frames n-channels rate] (:args msg)]
    {:n-frames n-frames
     :n-channels n-channels
     :rate rate}))

(defn sample-info [s]
  (buffer-info (:buf s)))

(defonce loaded-synthdefs* (ref {}))

(defn load-synthdef
  "Load a Clojure synth definition onto the audio server."
  [sdef]
  (assert (synthdef? sdef))
  (dosync (alter loaded-synthdefs* assoc (:name sdef) sdef))
  (if (connected?)
    (snd "/d_recv" (synthdef-bytes sdef))))

(defn- load-all-synthdefs []
  (doseq [[sname sdef] @loaded-synthdefs*]
    (println "loading synthdef: " sname)
    (snd "/d_recv" (synthdef-bytes sdef))))

(defonce _synthdef-handler_ (on :connected load-all-synthdefs))

(defn load-synth-file
  "Load a synth definition file onto the audio server."
  [path]
  (snd "/d_recv" (synthdef-bytes (synthdef-read path))))

; TODO: need to clear all the buffers and busses
;  * Think about a sane policy for setting up state, especially when we are connected
; with many peers on one or more servers...
(defn reset
  "Clear all synthesizers, groups and pending messages from the audio server
  and then recreates the active synth groups."
  []
  (clear-msg-queue)
  (group-clear SYNTH-GROUP) ; clear the synth group
  (sync-event :reset))

(defonce _connect-handler_ (on :connected #(group :tail ROOT-GROUP)))

(defn restart
  "Reset everything and restart the SuperCollider process."
  []
  (reset)
  (quit)
  (boot))

(defmulti hit-at (fn [& args] (type (second args))))

(defmethod hit-at String [time-ms synth & args]
  (at time-ms (apply node synth args)))

(defmethod hit-at clojure.lang.Keyword [time-ms synth & args]
  (at time-ms (apply node (name synth) args)))

(defmethod hit-at ::sample [time-ms synth & args]
  (apply hit-at time-ms "granular" :buf (get-in synth [:buf :id]) args))

(defmethod hit-at :default [& args]
  (throw (Exception. (str "Hit doesn't know how to play the given synth type: " args))))

; Turn hit into a multimethod
; Convert samples to be a map object instead of an ID
(defn hit
  "Fire off a synth or sample at a specified time.
  These are the same:
  (hit :kick)
  (hit \"kick\")
  (hit (now) \"kick\")

  Or you can get fancier like this:
  (hit (now) :sin :pitch 60)
  (doseq [i (range 10)] (hit (+ (now) (* i 250)) :sin :pitch 60 :dur 0.1))

  "
  ([] (hit-at (now) "ping" :pitch (choose [60 65 72 77])))
  ([& args]
   (apply hit-at (if (isa? (type (first args)) Number)
                   args
                   (cons (now) args)))))

(defmacro check
  "Try out an anonymous synth definition.  Useful for experimentation.  If the
  root node is not an out ugen, then it will add one automatically."
  [body]
  `(do
     (load-synthdef (synth "audition-synth" {} ~body))
     (let [note# (hit (now) "audition-synth")]
       (at (+ (now) 1000) (node-free note#)))))

(defn ctl
  "Modify synth parameters, optionally at a specified time.

  (hit :sin :pitch 50) => 1000
  (ctl 1000 :pitch 40)
  (ctl (+ (now) 2000) 1000 :pitch 60)

  "
  [& args]
  (let [[time-ms synth-id ctls] (if (odd? (count args))
                                  [(now) (first args) (next args)]
                                  [(first args) (second args) (drop 2 args)])]
    ;(println time-ms synth-id ": " ctls)
    (at time-ms
        (apply node-control synth-id (stringify ctls)))))

(defn kill
  "Free one or more synth nodes.
  Functions that create instance of synth definitions, such as hit, return
  a handle for the synth node that was created.
  (let [handle (hit :sin)] ; returns => synth-handle
  (kill (+ 1000 (now)) handle))

  ; a single handle without a time kills immediately
  (kill handle)

  ; or a bunch of synth handles can be removed at once
  (kill (hit) (hit) (hit))

  ; or a seq of synth handles can be removed at once
  (kill [(hit) (hit) (hit)])
  "
  [& ids]
  (apply node-free (flatten ids))
  :killed)

(defn load-instruments []
  (doseq [synth (filter #(synthdef? %1)
                        (map #(var-get %1)
                             (vals (ns-publics 'overtone.instrument))))]
    ;(println "loading synth: " (:name synth))
    (load-synthdef synth)))

;(defn update
;  "Update a voice or standalone synth with new settings."
;  [voice & args]
;  (let [[names vals] (synth-args (apply hash-map args))
;        synth        (if (voice? voice) (:synth voice) voice)]
;    (.set synth names vals)))

;(defmethod play-note :synth [voice note-num dur & args]
;  (let [args (assoc (apply hash-map args) :note note-num)
;        synth (trigger (:synth voice) args)]
;    (schedule #(release synth) dur)
;    synth))

(defn- name-synth-args [args names]
  (loop [args args
         names names
         named []]
    (if args
      (recur (next args)
             (next names)
             (concat named [(first names) (first args)]))
      named)))

(defn synth-player
  "Returns a player function for a named synth.  Used by (synth ...) internally, but can be
  used to generate a player for a pre-compiled synth.  The function generated will accept two
  optional arguments that must come first, the :target and :position (see the node function docs).

  (foo)
  (foo :target 0 :position :tail)

  or if foo has two arguments:
  (foo 440 0.3)
  (foo :target 0 :position :tail 440 0.3)
  at the head of group 2:
  (foo :target 2 :position :head 440 0.3)

  These can also be abbreviated:
  (foo :tgt 2 :pos :head)
  "
  [sname arg-names]
  (fn [& args]
    (let [[args sgroup] (if (or (= :target (first args))
                                (= :tgt    (first args)))
                          [(drop 2 args) (second args)]
                          [args ROOT-GROUP])
          [args pos]    (if (or (= :position (first args))
                                (= :pos      (first args)))
                          [(drop 2 args) (second args)]
                          [args :tail])
          controller    (partial node-control sgroup)
          player        (partial node sname :target sgroup :position pos)
          [tgt-fn args] (if (= :ctl (first args))
                          [controller (rest args)]
                          [player args])
          args (map #(if (buffer? %) (:id %) %) args)
          named-args (if (keyword? (first args))
                       args
                       (name-synth-args args arg-names))]
        (apply tgt-fn named-args))))

(defonce _auto-boot_ (boot))

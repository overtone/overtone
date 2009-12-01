(ns overtone.sc
 (:import
     (java.net InetSocketAddress)
     (java.util.regex Pattern)
     (java.util.concurrent TimeUnit TimeoutException)
     (java.io BufferedInputStream))
  (:require [overtone.log :as log])
  (:use
     clojure.contrib.shell-out
     clojure.contrib.seq-utils
     (overtone config util voice osc time synthdef)))

; This is at heart an OSC client library for the SuperCollider scsynth engine.

; TODO: Make this work correctly
; NOTE: "localhost" doesn't work, at least on my laptopt
(def SERVER-HOST "127.0.0.1")
(def SERVER-PORT nil) ; nil means a random port

; Max number of milliseconds to wait for a reply from the server
(def REPLY-TIMEOUT 300)

(def START-GROUP-ID 1)
(def START-BUFFER-ID 1)
(def START-NODE-ID 1000)

(def DEFAULT-GROUP 0)

(defonce server-thread* (ref nil))
(defonce server*        (ref nil))

(defonce *counters (ref {}))
(defonce *counter-defaults (ref {}))

(defmacro defcounter [counter-name start-id]
  (let [next-fn (symbol (str "next-" (name counter-name) "-id"))]
    `(do
       (dosync
         (alter *counters assoc ~counter-name (ref ~start-id))
         (alter *counter-defaults assoc ~counter-name ~start-id))
       (defn ~next-fn []
         (dec
           (dosync (alter (~counter-name @*counters) inc)))))))

(defn reset-id-counters []
  (doseq [[cname counter] @*counters]
    (dosync
    (ref-set counter (cname @*counter-defaults)))))

(defcounter :group START-GROUP-ID)
(defcounter :node START-NODE-ID)
(defcounter :buffer START-BUFFER-ID)

(defn connected? []
  (not (nil? @server*)))

(declare boot)

(defn snd
  "Creates an OSC message and either sends it to the server immediately
  or if a bundle is currently being formed it adds it to the list of messages."
  [path & args]
  (if (nil? @server*)
    (throw (Exception. "Not connected to a SuperCollider server.")))
      (osc-snd-msg @server*
                   (apply osc-msg path (osc-type-tag args) args)))

(defmacro at
  "Schedule the messages sent in body at a single time."
  [time-ms & body]
  `(in-osc-bundle @server* ~time-ms ~@body))

(defn recv
  [path & [timeout]]
  (osc-recv @server* path timeout))

(defn connect
  ([] (connect SERVER-HOST SERVER-PORT))
  ([host port]
   (log/info "(connect " host ":" port)
   (dosync (ref-set server* (osc-client host port)))
   (comment osc-listen @server* synth-listener)))

(defonce running?* (atom false))
(def *server-out* *out*)

(def server-log* (ref []))

(defn print-server-log []
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
(defn status []
  (snd "/status")
  (let [sts (osc-recv @server* "status.reply" REPLY-TIMEOUT)]
    (log/debug "got status: " (:args sts))
    (if-let [[_ ugens synths groups loaded avg peak nominal actual] (:args sts)]
      {:n-ugens ugens
       :n-synths synths
       :n-groups groups
       :n-loaded-synths loaded
       :avg-cpu avg
       :peak-cpu peak
       :nominal-sample-rate nominal
       :actual-sample-rate actual})))

; Wait until SuperCollider has completed all asynchronous commands currently in execution.
(defn wait-sync [& [timeout]]
  (let [sync-id (rand-int 999999)
        _ (snd "/sync" sync-id)
        reply (osc-recv @server* "/synced" (if timeout timeout REPLY-TIMEOUT))
        reply-id (first (:args reply))]
    (= sync-id reply-id)))

(defn server-log [stream read-buf]
  (while (pos? (.available stream))
    (let [n (min (count read-buf) (.available stream))
          _ (.read stream read-buf 0 n)
          msg (String. read-buf 0 n)]
      (dosync (alter server-log* conj msg))
      (log/info (String. read-buf 0 n)))))
;      (.write *server-out* (String. read-buf 0 n)))))

(defn- boot-thread [cmd]
  (reset! running?* true)
  (let [proc (.exec (Runtime/getRuntime) cmd)
        in-stream (BufferedInputStream. (.getInputStream proc))
        err-stream (BufferedInputStream. (.getErrorStream proc))
        read-buf (make-array Byte/TYPE 256)]
    (while @running?*
      (server-log in-stream read-buf)
      (server-log err-stream read-buf)
      (Thread/sleep 250))
    (.destroy proc)))

(defn connect-jack-ports
  "Maybe this isn't necessary, since we can use the SC_JACK_DEFAULT_OUTPUTS
  environment variable..."
  [n-channels]
  (let [port-list (sh "jack_lsp")
        sc-outputs (re-find #"SuperCollider.*:out_" port-list)]
  (doseq [i (range n-channels)]
    (sh "jack_connect"
        (str sc-outputs (+ i 1))
        (str "system:playback_" (+ i 1))))))

(def SC-PATHS {:linux "scsynth"
               :windows "C:/Program Files/SuperCollider/scsynth.exe"
               :mac  "/Applications/SuperCollider/scsynth" })

(def SC-ARGS  {:linux []
               :windows []
               :mac   ["-U" "/Applications/SuperCollider/plugins"] })

(def SC-PATH (SC-PATHS (config :os)))
(def SC-ARG (SC-ARGS (config :os)))

(defn boot
  ([] (boot SERVER-HOST SERVER-PORT))
  ([host port]
  (if (not @running?*)
    (let [port (if (nil? port) (+ (rand-int 50000) 2000) port)
          cmd (into-array String (concat [SC-PATH "-u" (str port)] SC-ARG))
          sc-thread (Thread. #(boot-thread cmd))]
      (.setDaemon sc-thread true)
      (log/info "Booting SuperCollider server (scsynth)...")
      (.start sc-thread)
      (dosync (ref-set server-thread* sc-thread))
      (Thread/sleep 1000)
      (log/info "Connecting to server...")
      (connect host port)
      (log/info "status check: " (status))))))

(defn quit
  "Quit the SuperCollider synth process."
  []
  (log/info "quiting supercollider")
  (if (connected?)
    (snd "/quit"))
  (reset! running?* false)
  (dosync (ref-set server* nil)))

(defn notify [& [notify?]]
  (snd "/notify" (if (false? notify?) 0 1)))

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
(defn node [synth-name id & args]
  (let [argmap (apply hash-map args)
        position ((get argmap :position :tail) POSITION)
        target (get argmap :target 0)
        args (flatten (seq (-> argmap (dissoc :position) (dissoc :target))))
        args (stringify (floatify args))]
    (apply snd "/s_new" synth-name id position target args)
    id))

(defn node-free
  "Instantly remove a node from the graph."
  [node-id & node-ids]
  (apply snd "/n_free" node-id node-ids))

(defn node-run
  "Start a stopped node."
  [node-id]
  (snd "/n_run" node-id 1))

(defn node-stop
  "Stop a running node."
  [node-id]
  (snd "/n_run" node-id 0))

(defn node-place
  "Place a node :before or :after another node."
  [node-id position target-id]
  (cond
    (= :before position) (snd "/n_before" node-id target-id)
    (= :after  position) (snd "/n_after" node-id target-id)))

(defn node-control
  "Set control values for a node."
  [node-id & name-values]
  (apply snd "/n_set" node-id (stringify name-values)))

; This can be extended to support setting multiple ranges at once if necessary...
(defn node-control-range
  "Set a range of controls all at once, or if node-id is a group control
  all nodes in the group."
  [node-id ctl-start & ctl-vals]
  (apply snd "/n_setn" node-id ctl-start (count ctl-vals) ctl-vals))

(defn node-map-controls
  "Connect a node's controls to a control bus."
  [node-id & names-busses]
  (apply snd "/n_map" node-id names-busses))

(defn group
  "Create a new group as a child of the target group."
  [position target-id]
  (let [id (next-group-id)]
    (snd "/g_new" id (get POSITION position) target-id)
    id))

(defn group-free
  "Free the specified groups."
  [group-id & group-ids]
  (apply node-free group-id group-ids))

(defn node-free
  "Instantly remove a node from the graph."
  [node-id & node-ids]
  (apply snd "/n_free" node-id node-ids))

(defn post-tree
  "Posts a representation of this group's node subtree, i.e. all the groups and
  synths contained within it, optionally including the current control values
  for synths."
  [id & [with-args?]]
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

(defn- parse-synth-tree [ctls?]
  (let [sname (first *data*)]
    (if ctls?
      (let [n-ctls (second *data*)
            [ctl-data new-data] (split-at (* 2 n-ctls) (nnext *data*))
            ctls (apply hash-map ctl-data)]
        (set! *data* new-data)
        {:synth sname
         :controls ctls})
      (do
        (set! *data* (next *data*))
        {:synth sname}))))

(defn- parse-node-tree-helper [ctls?]
  (let [[id n-children & new-data] *data*]
    (set! *data* new-data)
    (cond
      (neg? n-children) (parse-synth-tree ctls?) ; synth
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
  [id & [ctls?]]
  (let [ctls? (if (or (= 1 ctls?) (= true ctls?)) 1 0)]
    (snd "/g_queryTree" id ctls?)
    (let [tree (:args (osc-recv @server* "/g_queryTree.reply" REPLY-TIMEOUT))]
      (parse-node-tree tree))))

(defn prepend-node
  "Add a node to the end of a group list."
  [g n]
  (snd "/g_head" g n))

(defn append-node
  "Add a node to the end of a group list."
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

; size is in samples
(defn buffer
  "Allocate a new buffer for storing audio data."
  [size]
  (let [id (next-buffer-id)]
    (snd "/b_alloc" id size)
    {:type :buffer
     :id id
     :size size}))

(defn buffer? [buf]
  (and (map? buf) (= (:type buf) :buffer)))

(defn buffer-read [buf start len]
  (assert (buffer? buf))
  (snd "/b_getn" (:id buf) start len))

(defn buffer-write [buf start len data]
  (snd "/b_setn" (:id buf) start len data))

(defn save-buffer
  "Save the float audio data in an audio buffer to a wav file."
  [buf path]
  (assert (buffer? buf))
  (snd "/b_write" (:id buf) path "wav" "float"))

(defn load-sample
  "Load a wav file into memory so it can be played as a sample."
  [path]
  (let [id (next-buffer-id)]
    (snd "/b_allocRead" id path)
    {:type :sample
     :buf {:type :buffer
           :id id}
     :path path}))

(defn sample? [s]
  (and (map? s) (= :sample (:type s))))

(defn load-synth
  "Load a Clojure synth definition onto the audio server."
  [sdef]
  (assert (synthdef? sdef))
  (snd "/d_recv" (synthdef-bytes sdef)))

(defn load-synth-file
  "Load a synth definition file onto the audio server."
  [path]
  (snd "/d_recv" (synthdef-bytes (synthdef-read path))))

(defn reset
  "Clear all synthesizers, groups and pending messages from the audio server."
  []
  (try
    (group-clear 0)
    (catch Exception e nil))
  (clear-msg-queue)
  (reset-id-counters))

;  Maybe it's better to keep the server log around???
;  (dosync (ref-set server-log* [])))

(defn debug [& [on-off]]
  (if (or on-off (nil? on-off))
    (do
      (log/level :debug)
      (snd "/dumpOSC" 1))
    (do
      (log/level :error)
      (snd "/dumpOSC" 0))))

(defn restart
  "Reset everything and restart the SuperCollider process."
  []
  (reset)
  (quit)
  (boot))

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
  ([] (hit (now) "sin" :pitch (+ 30 (rand-int 40))))
  ([& args]
   (let [[time-ms synth args] (if (= Long (type (first args)))
                                [(first args) (second args) (nnext args)]
                                [(now) (first args) (next args)])
         id (next-node-id)
         synth (cond
                 (synthdef? synth) (as-str (:name synth))
                 (sample? synth) synth
                 (keyword? synth) (name synth)
                 (string? synth) synth
                 :default (throw
                            (Exception. "Hit doesn't know how to play the given synth: " synth)))
         args (-> args (stringify) (floatify))]
     (if (sample? synth)
       (apply hit time-ms "granular" :buf (get-in synth [:buf :id]) args)
       (at time-ms (apply node synth id args)))
     id)))

(defmacro check
  "Try out an anonymous synth definition.  Useful for experimentation.  If the
  root node is not an out ugen, then it will add one automatically."
  [body]
  `(do
     (load-synth (synth "audition-synth" {} ~body))
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
    (println time-ms synth-id ": " ctls)
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

      ; or a seq of synth handles can be removed at once
      (kill (+ (now) 1000) [(hit) (hit) (hit)])
  "
  [& args]
  (let [[time-ms ids] (if (= 1 (count args))
                        [(now) (flatten args)]
                        [(first args) (flatten (next args))])]
        (at time-ms
            (apply node-free ids))))

(defn load-instruments []
  (doseq [synth (filter #(synthdef? %1) 
                        (map #(var-get %1) 
                             (vals (ns-publics 'overtone.instrument))))]
    (println "loading synth: " (:name synth))
    (load-synth synth)))

;;(defn effect [synthdef & args]
;;  (let [arg-map (assoc (apply hash-map args) "bus" FX-BUS)
;;        new-effect {:def synthdef
;;                    :effect (trigger synthdef arg-map)}]
;;    (dosync (alter *fx conj new-effect))
;;    new-effect))
;
;(defn update
;  "Update a voice or standalone synth with new settings."
;  [voice & args]
;  (let [[names vals] (synth-args (apply hash-map args))
;        synth        (if (voice? voice) (:synth voice) voice)]
;    (.set synth names vals)))

;(defn synth-voice [synth-name & args]
;  {:type :voice
;   :voice-type :synth
;   :synth synth-name
;   :args args})
;
;(defmethod play-note :synth [voice note-num dur & args]
;  (let [args (assoc (apply hash-map args) :note note-num)
;        synth (trigger (:synth voice) args)]
;    (schedule #(release synth) dur)
;    synth))


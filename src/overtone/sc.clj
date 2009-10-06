(ns overtone.sc
  (:import 
     (java.net InetSocketAddress)
     (java.util.regex Pattern)
     (java.util.concurrent TimeUnit))
  (:use 
     clojure.contrib.shell-out
     clojure.contrib.seq-utils
     (overtone utils voice osc rhythm)))

; TODO: Make this work correctly
; NOTE: "localhost" doesn't work, at least on my laptopt
(def SERVER-HOST "127.0.0.1")
(def SERVER-PORT 57110)
(def SERVER-PROTOCOL "tcp")

(def START-GROUP-ID 1)
(def START-BUFFER-ID 1)
(def START-NODE-ID 1000)

(defonce *synth-thread (ref nil))
(defonce *synth        (ref nil))
(defonce *synth-listeners (ref {}))

(defonce *counters (ref {}))
(defonce *counter-defaults (ref {}))

;; We use a binding to *msg-bundle* for handling groups of messages that
;; need to be bundled together with an OSC timestamp.
(def *msg-bundle* nil)


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

(defn running? []
  (not (nil? @*synth)))

(declare boot)

(defn snd 
  "Creates an OSC message and either sends it to the server immediately 
  or if a bundle is currently being formed it adds it to the list of messages."
  [& args]
  (if (nil? @*synth)
    (throw (Exception. "Not connected to a SuperCollider server.")))
  (let [msg (apply osc-msg args)]
    (if *msg-bundle*
      (swap! *msg-bundle* #(conj %1 msg))
      (osc-snd @*synth msg))))

(def *msg-log (ref []))

; TODO: The listener system will currently support a single listener
; per message type.  Not sure if we would ever need to have multiple
; listeners for the same message...
(defn synth-listener [msg]
  (dosync (alter *msg-log conj msg))

  (let [msg-name (:name msg)]
    (if-let [recvr (msg-name @*synth-listeners)]
      (send recvr (fn [] msg)))))

(defn recv [cmd & [timeout]]
  (let [p (promise)]
    (dosync (alter *synth-listeners assoc cmd p))
    (if timeout 
      (.get (future @p) timeout TimeUnit/MILLISECONDS)
      (:args @p))
    (dosync (alter *synth-listeners dissoc cmd))))

(defn connect 
  ([] (connect SERVER-HOST SERVER-PORT SERVER-PROTOCOL))
  ([host port proto]
   (dosync (ref-set *synth (osc-client host port proto)))
   (osc-listen @*synth synth-listener)))

(defn boot
  ([] (boot SERVER-HOST SERVER-PORT SERVER-PROTOCOL))
  ([host port proto]
   (let [sc-thread (Thread. #(sh "scsynth" "-t" (str port)))]
     (.setDaemon sc-thread true)
     (.start sc-thread)

     (dosync (ref-set *synth-thread sc-thread))
     (connect host port proto))))

(defmacro at-time [timestamp & body]
  `(binding [*msg-bundle* (atom [])]
     (let [retval# ~@body]
       (osc-snd @*synth (osc-bundle @*msg-bundle* ~timestamp))
       retval#)))

(defn quit 
  "Quit the SuperCollider synth process."
  []
  (if (running?)
    (snd "/quit"))
  (dosync (ref-set *synth nil))
  (if @*synth-thread
    (.stop @*synth-thread)))

(defn notify [notify?]
  (snd "/notify" (if notify? 1 0)))

(defn status []
  (snd "/status")
  (let [sts (recv "status.reply" 250)]
    (println "status: " sts)
    (if-let [{[ugens synths groups loaded avg peak nominal actual] :args} sts]
      {:num-ugens ugens
       :num-synths synths
       :num-groups groups
       :num-loaded-synths loaded
       :avg-cpu avg
       :peak-cpu peak
       :nominal-sample-rate nominal
       :actual-sample-rate actual})))

; Synths, Busses, Controls and Groups are all Nodes.  Groups are linked lists,
; and group zero is the root of the graph.  Nodes can be added to a group in
; one of these 5 positions relative to either the full list, or a specified node.
(def POSITION
  {:head         0
   :tail         1
   :before-node  2
   :after-node   3
   :replace-node 4})

;; Sending a synth-id of -1 lets the server choose an ID
(defn node [synth-name & args]
  (let [argmap (apply hash-map args)
        id (next-node-id)
        position ((get argmap :position :tail) POSITION)
        target (get argmap :target 0)
        argmap (-> argmap (dissoc :position) (dissoc :target))]
    (apply snd "/s_new" synth-name id position target (flatten (seq argmap)))
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
  (apply snd "/n_set" node-id name-values))

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

(defn prepend-node 
  "Add a node to the end of a group list."
  [g n]
  (snd "/g_head" g n))

(defn append-node 
  "Add a node to the end of a group list."
  [g n]
  (snd "/g_tail" g n)) 

(defn group-free-children 
  "Free all synth nodes from the processing graph."
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
(defn buffer [size]
  (let [id (next-buffer-id)]
    (snd "/b_alloc" id size)
    id))

(defn load-sample [path]
  (let [id (next-buffer-id)]
    (snd "/b_allocRead" id path)
    id))

(defn save-buffer [buf-id path]
  (snd "/b_write" buf-id path "wav" "float"))
  
(defn reset []
  (try
    (group-free-children 0)
    (catch Exception e nil))
  (clear-msg-queue)
  (reset-id-counters)
  (dosync (ref-set *synth-listeners {})))

(defn debug [& [on-off]]
  (if (or on-off (nil? on-off))
    (do 
      (osc-debug @*synth true)
      (snd "/dumpOSC" 1))
    (do 
      (osc-debug @*synth false)
      (snd "/dumpOSC" 0))))

(defn restart 
  "Reset everything and restart the SuperCollider process."
  []
  (reset)
  (quit)
  (boot))
;  (stop-players true))

(defn hit 
  "Fire off the named synth or loaded sample (by id) at a specified time."
  [time-ms syn & args]
  (if (number? syn)
    (apply hit time-ms "granular" :buf syn args)
    (at-time time-ms (apply node syn (stringify args)))))

(defn ctl
  "Modify a synth parameter at the specified time."
  [time-ms node-id & args]
  (at-time time-ms (apply node-control node-id (stringify args))))

;(defn status []
;  (let [stat (.getStatus @*s)]
;    {:sample-rate (.sampleRate stat)
;     :actual-sample-rate (.actualSampleRate stat)
;
;     :num-groups (.numGroups stat)
;     :num-synth-defs (.numSynthDefs stat)
;     :num-synths (.numSynths stat)
;     :num-nodes  (.numUGens stat)
;
;     :avg-cpu (.avgCPU stat)
;     :peak-cpu (.peakCPU stat)}))
;
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
;

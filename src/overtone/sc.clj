(ns overtone.sc
 (:import 
     (java.net InetSocketAddress)
     (java.util.regex Pattern)
     (java.util.concurrent TimeUnit TimeoutException)
     (java.io BufferedInputStream))
  (:require [org.enclojure.commons.c-slf4j :as log])
  (:use 
     clojure.contrib.shell-out
     clojure.contrib.seq-utils
     clj-backtrace.repl
     (overtone utils voice osc rhythm synthdef)))

(log/ensure-logger)

; TODO: Make this work correctly
; NOTE: "localhost" doesn't work, at least on my laptopt
(def SERVER-HOST "127.0.0.1")
(def SERVER-PORT nil) ; nil means a random port 

(def START-GROUP-ID 1)
(def START-BUFFER-ID 1)
(def START-NODE-ID 1000)

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

(defn recv
  [path & [timeout]]
  {:path "foo" :args []})

(defn connect 
  ([] (connect SERVER-HOST SERVER-PORT))
  ([host port]
   (log/info (str "(connect " host ":" port))
   (dosync (ref-set server* (osc-client host port)))
   (comment osc-listen @server* synth-listener)))

(defonce running?* (atom false))
(def *server-out* *out*)

(defn server-log [stream read-buf]
  (while (pos? (.available stream))
    (let [n (min (count read-buf) (.available stream))]
      (.read stream read-buf 0 n)
      (.write *server-out* (String. read-buf 0 n)))))

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

(defn connect-jack-ports [n-channels]
  (let [port-list (sh "jack_lsp")
        sc-outputs (re-find #"SuperCollider.*:out_" port-list)]
  (doseq [i (range n-channels)]
    (sh "jack_connect" 
        (str sc-outputs (+ i 1))
        (str "system:playback_" (+ i 1))))))

(defn boot
  ([] (boot SERVER-HOST SERVER-PORT))
  ([host port]
   (let [port (if (nil? port) (+ (rand-int 50000) 2000) port)
         cmd (into-array String ["scsynth" "-u" (str port)])
         sc-thread (Thread. #(boot-thread cmd))]
     (.setDaemon sc-thread true)
     (.start sc-thread)
     (dosync (ref-set server-thread* sc-thread))
     (Thread/sleep 1000)
     (connect-jack-ports 2)
     (connect host port))))

(defn quit 
  "Quit the SuperCollider synth process."
  []
  (log/info "quiting supercollider")
  (if (connected?)
    (snd "/quit"))
  (reset! running?* false)
  (dosync (ref-set server* nil)))

(defn notify [notify?]
  (snd "/notify" (if notify? 1 0)))

(defn status []
  (snd "/status")
  (let [sts (osc-recv @server*)]
    (log/debug "got status: " (:args sts))
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

(defn save-buffer [buf-id path]
  (snd "/b_write" buf-id path "wav" "float"))
  
(defn load-sample [path]
  (let [id (next-buffer-id)]
    (snd "/b_allocRead" id path)
    id))

(defn load-synth [sdef]
  (assert (or (synthdef-file? sdef) (synthdef? sdef)))
  (snd "/d_recv" (synthdef-bytes sdef))) 

(defn reset []
  (try
    (group-free-children 0)
    (catch Exception e nil))
  (clear-msg-queue)
  (reset-id-counters))

(defn debug [& [on-off]]
  (if (or on-off (nil? on-off))
      (snd "/dumpOSC" 1)
      (snd "/dumpOSC" 0)))

(defn restart 
  "Reset everything and restart the SuperCollider process."
  []
  (reset)
  (quit)
  (boot))

(defn hit 
  "Fire off the named synth or loaded sample (by id) at a specified time."
  ([] (hit (now) "sin" :pitch (+ 30 (rand-int 40))))
  ([time-ms syn & args]
   (when (odd? (count args))
     (throw (IllegalArgumentException. "Arguments to hit must come in key-value pairs.")))
   (if (number? syn)
     (apply hit time-ms "granular" :buf syn args)
     (in-osc-bundle @server* time-ms (apply node syn args)))))

(defn ctl
  "Modify a synth parameter at the specified time."
  [time-ms node-id & args]
  (in-osc-bundle @server* time-ms (apply node-control node-id (stringify args))))

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

(ns overtone.sc
  (:import 
     (java.net InetSocketAddress)
     (java.util.regex Pattern)
     (javax.sound.midi Receiver)
     (de.sciss.jcollider Server Constants UGenInfo UGen
                         Group Node Control Constant 
                         GraphElem GraphElemArray
                         Synth SynthDef UGenChannel))
  (:use (overtone voice osc time)))

(def SERVER-NAME "Overtone Audio Server")
(def START-ID 2000)

(def *s (ref (Server. SERVER-NAME)))
(def *node-id-counter (ref START-ID))

(defn reset-id-counter []
  (dosync (ref-set *node-id-counter START-ID)))

(defn next-id []
  (dosync (alter *node-id-counter inc)))

(UGenInfo/readDefinitions)

(defn server [] @*s)

(defn running? []
  (and @*s (.isRunning @*s)))

(defn start-synth
  ([]
   (if (not (running?))
     (.boot @*s)))
  ([host port]
   (start-synth SERVER-NAME host port))
  ([server-name host port]
   (dosync (ref-set *s (Server. server-name (InetSocketAddress. host port))))
   (.boot @*s)))

(defn stop-synth []
  (.quit @*s))

(defn root []
  (.getDefaultGroup @*s))

(defn target []
  (.asTarget @*s))

(defn reset []
  (.freeAll (root))
  (stop-players true))

(defn debug []
  (.dumpOSC @*s Constants/kDumpBoth))

(defn debug-off []
  (.dumpOSC @*s Constants/kDumpOff))

(defn status []
  (let [stat (.getStatus @*s)]
    {:sample-rate (.sampleRate stat)
     :actual-sample-rate (.actualSampleRate stat)

     :num-groups (.numGroups stat)
     :num-synth-defs (.numSynthDefs stat)
     :num-synths (.numSynths stat)
     :num-nodes  (.numUGens stat)

     :avg-cpu (.avgCPU stat)
     :peak-cpu (.peakCPU stat)}))

(defn- synth-args [arg-map]
  (if (empty? arg-map) 
    [(make-array String 0) (make-array (. Float TYPE) 0)]
    [(into-array (for [k (keys arg-map)] 
                   (cond 
                     (keyword? k) (name k)
                     (string? k) k)))
     (float-array (for [v (vals arg-map)] (float v)))]))

(defn trigger 
  "Triggers the named synth by creating a new instance."
  [synth-name arg-map]
  (let [[arg-names arg-vals] (synth-args arg-map)]
      (Synth. synth-name arg-names arg-vals (target))))

; Add-node actions:
; 0 - add to the the head of the group specified by the target ID.
; 1 - add to the the tail of the group specified by the target ID.
; 2 - add just before the node specified by the target ID.
; 3 - add just after the node specified by the target ID.
; 4 - replace the node specified by the target ID. (target is freed)

; Sending a synth-id of -1 lets the server choose an ID

(defn trigger-at [synth-name time-ms]
  (let [msg (osc-msg "/s_new" synth-name 
                               -1 ; (next-id) 
                               1 
                               (.getNodeID (target)))]
  (.sendBundle @*s (osc-bundle [msg] time-ms))))

;(defn effect [synthdef & args]
;  (let [arg-map (assoc (apply hash-map args) "bus" FX-BUS)
;        new-effect {:def synthdef
;                    :effect (trigger synthdef arg-map)}]
;    (dosync (alter *fx conj new-effect))
;    new-effect))

(defn update 
  "Update a voice or standalone synth with new settings."
  [voice & args]
  (let [[names vals] (synth-args (apply hash-map args))
        synth        (if (voice? voice) (:synth voice) voice)]
    (.set synth names vals)))

(defn release 
  [synth]
  (.release synth))

(defn synth-voice [synth-name & args]
  {:type :voice
   :voice-type :synth
   :synth (Synth/basicNew synth-name (server))
   :args args})

(defmethod play-note :synth [voice note-num dur & args]
  (let [args (assoc (apply hash-map args) :note note-num)
        synth (trigger (:synth voice) args)]
    (schedule #(release synth) dur)
    synth))


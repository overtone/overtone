(ns overtone.sc
  (:import 
     (java.net InetSocketAddress)
     (java.util.regex Pattern)
     (javax.sound.midi Receiver)
     (de.sciss.jcollider Server Constants UGenInfo UGen
                         Group Node Control Constant 
                         GraphElem GraphElemArray
                         Synth SynthDef UGenChannel)
     (de.sciss.net OSCClient OSCBundle OSCMessage))
  (:use (overtone rhythm)))

(def SERVER-NAME "Overtone Audio Server")
(def *s (ref (Server. SERVER-NAME)))
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


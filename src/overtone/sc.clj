(ns overtone.sc
  (:import 
     (de.sciss.jcollider Server Constants UGenInfo UGen
                         Group Node 
                         GraphElem Control Constant
                         Synth SynthDef UGenChannel)
     (de.sciss.jcollider.gui ServerPanel)
     (de.sciss.net OSCClient OSCBundle OSCMessage)))

(defonce *s* (Server. "Overtone Audio Server"))
(UGenInfo/readDefinitions)

(def *window* (ref nil))

(defn running? []
  (and *s* (.isRunning *s*)))

(defn start []
  (if (not (running?))
    (.boot *s*)))

(defn stop []
  (.quit *s*))

(defn root []
  (.getDefaultGroup *s*))

(defn reset []
  (.freeAll (.getDefaultGroup *s*)))

(defn show []
  (dosync (ref-set *window* (ServerPanel/makeWindow *s* (or ServerPanel/BOOTQUIT 
                                  ServerPanel/CONSOLE 
                                  ServerPanel/COUNTS 
                                  ServerPanel/DUMP)))))

(defn hide []
  (.hide @*window*))

(defn debug []
  (.dumpOSC *s* Constants/kDumpBoth))

(defn debug-off []
  (.dumpOSC *s* Constants/kDumpOff))

(defn status []
  (let [stat (.getStatus *s*)]
    {:sample-rate (.sampleRate stat)
     :actual-sample-rate (.actualSampleRate stat)

     :num-groups (.numGroups stat)
     :num-synth-defs (.numSynthDefs stat)
     :num-synths (.numSynths stat)
     :num-nodes  (.numUGens stat)

     :avg-cpu (.avgCPU stat)
     :peak-cpu (.peakCPU stat)}))


(ns overtone.sc
  (:import 
     (java.net InetSocketAddress)
     (de.sciss.jcollider Server Constants UGenInfo UGen
                         Group Node 
                         GraphElem Control Constant
                         Synth SynthDef UGenChannel)
     (de.sciss.jcollider.gui ServerPanel)
     (de.sciss.jcollider.gui SynthDefDiagram)
     (de.sciss.net OSCClient OSCBundle OSCMessage))
  (:use (overtone music)))

(def SERVER-NAME "Overtone Audio Server")
(defonce *s* (Server. SERVER-NAME))
(UGenInfo/readDefinitions)

(def *window* (ref nil))

(defn running? []
  (and *s* (.isRunning *s*)))

(defn start 
  ([]
   (if (not (running?))
     (.boot *s*)))
  ([host port]
   (start SERVER-NAME host port))
  ([server-name host port]
   (def *s* (Server. server-name (InetSocketAddress. host port)))
   (.boot *s*)))

(defn stop []
  (.quit *s*))

(defn root []
  (.getDefaultGroup *s*))

(defn reset []
  (.freeAll (.getDefaultGroup *s*))
  (stop-players true))

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

;; NOTES
;; It seems that the classes in sclang often times generate multiple UGen nodes for what seems like a single one.  For example, in reality a SinOsc ugen only has two inputs for frequency and phase, and then a MulAdd ugen is used to support the amplitude and dc-offset arguments.  Also, I think the control rate ugens that optionally take a completion action, for example envelopes that can free their containing synth once they are done, are also implemented using an additional ugen that is made to do just this freeing.  We should think about this and do something convenient to make our API as easy as possible.

;; Hmmmmmmm, not even sure how we can use these
(def action 
  {; free the enclosing synth
   :free Constants/kDoneFree 

   ; free synth and all other nodes in this group (before and after)
   :free-all Constants/kDoneFreeAll

   ; free this synth and all preceding nodes in this group
   :free-all-pre Constants/kDoneFreeAllPred 

   ; free this synth and all following nodes in this group
   :free-all-after Constants/kDoneFreeAllSucc 

   ; free the enclosing group and all nodes within it (including this synth)
   :free-group Constants/kDoneFreeGroup 

   ; free this synth and pause the preceding node
   :free-pause-pre Constants/kDoneFreePausePred 

   ; free this synth and pause the following node
   :free-pause-next Constants/kDoneFreePauseSucc 

   ; free both this synth and the preceding node
   :free-free-pre Constants/kDoneFreePred 

   ; free this synth; if the preceding node is a group then do g_freeAll on it, else free it
   :free-pre-group Constants/kDoneFreePredGroup 

   ; free this synth and if the preceding node is a group then do g_deepFree on it, else free it
   :free-pre-group-deep Constants/kDoneFreePredGroupDeep 

   ; free both this synth and the following node
   :free-free-next Constants/kDoneFreeSucc 

   ; free this synth; if the following node is a group then do g_freeAll on it, else free it
   :free-next-group Constants/kDoneFreeSuccGroup 

   ; free this synth and if the following node is a group then do g_deepFree on it, else free it
   :free-next-group-deep Constants/kDoneFreeSuccGroupDeep 

   ; do nothing when the UGen is finished
   :nothing Constants/kDoneNothing 

   ; pause the enclosing synth, but do not free it
   :pause Constants/kDonePause
   })

(defn ugen-arg [arg]
  {:name (.name arg)
   :min (.min arg)
   :max (.max arg)})

(defn ugen-info [name info]
  {:name name 
   :args (for [arg (.args info)] (ugen-arg arg))})

(defn ugens []
  (for [[name info] (UGenInfo/infos)] (ugen-info name info)))

(defn print-ugens []
  (doseq [ugen (ugens)]
    (println (:display-name ugen) ": [" (for [arg (:args ugen)] (:name arg)) "]")))

(defn find-ugen [name]
  (filter #(= name (:name %1)) (ugens)))

;; TODO: OK, this is getting repetitive... Make a macro or something.
(defn ugen-ir [args]
  (clojure.lang.Reflector/invokeStaticMethod "de.sciss.jcollider.UGen" "ir" (to-array args)))

(defn ugen-kr [args]
  (clojure.lang.Reflector/invokeStaticMethod "de.sciss.jcollider.UGen" "kr" (to-array args)))

(defn ugen-ar [args]
  (clojure.lang.Reflector/invokeStaticMethod "de.sciss.jcollider.UGen" "ar" (to-array args)))

(defn ugen-dr [args]
  (clojure.lang.Reflector/invokeStaticMethod "de.sciss.jcollider.UGen" "dr" (to-array args)))

(defn ugen-array [args]
  (clojure.lang.Reflector/invokeStaticMethod "de.sciss.jcollider.UGen" "array" (to-array args)))

(defn ugen-ctl-ir [args]
  (clojure.lang.Reflector/invokeStaticMethod "de.sciss.jcollider.Control" "ir" (to-array args)))

(defn ugen-ctl-kr [args]
  (clojure.lang.Reflector/invokeStaticMethod "de.sciss.jcollider.Control" "kr" (to-array args)))


; Convert various components of a synthdef to UGen objects
; * numbers to constants 
; * vectors to arrays 
(defn ugenify [args]
  (map (fn [arg]
         (cond
           (number? arg) (ugen-ir [(float arg)])
           (vector? arg) (ugen-array (ugenify arg))
           true          arg))
       args))

(defn kr [& args]
  (ugen-kr (ugenify args)))

(defn ar [& args]
  (ugen-ar (ugenify args)))

(defn dr [& args]
  (ugen-dr (ugenify args)))

(defn ctl-ir [args]
  (ugen-ctl-ir (ugenify args)))

(defn ctl-kr [args]
  (ugen-ctl-kr (ugenify args)))
  
;; TODO: Look into doing things with simple OSC messaging if creating client-side representations of synths is slow.  We can do it like this:
;s.sendMsg("/s_new", "MyFavoriteSynth", n = s.nextNodeID;);
;s.sendMsg("/n_free", n);
  
(defmacro defsynth [name node]
  `(def ~(symbol (str name)) (SynthDef. ~(str name) ~node)))

(defn synth-args [arg-map]
  (if (empty? arg-map) 
    [(make-array String 0) (make-array (. Float TYPE) 0)]
    [(into-array (for [k (keys arg-map)] 
                   (cond 
                     (keyword? k) (name k)
                     (string? k) k)))
     (float-array (for [v (vals arg-map)] (float v)))]))

(defn trigger [synth arg-map]
  (let [[arg-names arg-vals] (synth-args arg-map)]
    (cond 
      (string? synth) (Synth. synth arg-names arg-vals (.asTarget *s*))
      (= de.sciss.jcollider.SynthDef (type synth)) (.play synth (.asTarget *s*) arg-names arg-vals)
      true (throw (Exception. "Play can take either a synthdef object or the string name of a previously
                               defined and stored synthdef available from the SuperCollider environment.")))))

(defn update [synth & args]
  (let [[names vals] (synth-args (apply hash-map args))]
    (.set synth names vals)))

(defn release [synth]
  (.release synth))

(defn play-note [voice note-num dur args]
  (let [synth (trigger voice (assoc (apply hash-map args) :note note-num))]
    (schedule #(release synth) dur)
    synth))

(defn note [voice note-num dur & args]
  (let [synth (trigger voice (assoc (apply hash-map args) :note note-num))]
    (schedule #(release synth) dur)
    synth))

(defn now []
  (System/currentTimeMillis))

; Can't figure out why this isn't working... need sleep.
(defn play [time-ms voice note-num dur & args]
  (let [on-time  (- time-ms (now))
        rel-time (+ on-time dur)]
    (println "ont: " on-time "\ntms: " time-ms "\nrel: " rel-time)
    (if (<= on-time 0)
      (let [synth (trigger voice (assoc (apply hash-map args) :note note-num))]
        (schedule #(release synth) rel-time))
      (schedule #(apply note voice note-num dur args) on-time))))

(def *drums (ref []))
(def *drum-count (ref 0))
(defn drum [voice pattern]
  (dosync (alter *drums conj [voice pattern])))

(defn clear-drums []
  (dosync (ref-set *drums [])))

(defn play-drums [tempo beat-count]
  (periodic (fn []
              (let [num (rand)
                    i   @*drum-count]
                (doseq [[voice pattern] @*drums]
                  (if (< num (nth pattern i))
                    (note voice 50 200)))
                (dosync (ref-set *drum-count (mod (inc @*drum-count) beat-count)))))
            tempo))

;; Env -> Envelope specification for use with EnvGen
;; Make a <list> for use with the EnvGen UGen. `levels' is a <list>
;; containing the left to right gain values for the envelope, it has
;; one more element than the <list> `times', having the delta times
;; for each envelope segment. `curve' is either a string or a number
;; or a <list> of such, in either case it is expanded to a list of the
;; same length as `times'. `release-node' is the index of the
;; 'release' stage of the envelope, `loop-node' is the index of the
;; 'loop' stage of the envelope. These indices are set as invalid, by
;; convention -1, to indicate there is no such node.

;(defn play [sdef & args]
;  (.play sdef (root)))

(defn free [sdef]
  (.free sdef (root)))

(defn visualize [sdef]
  (SynthDefDiagram. sdef))

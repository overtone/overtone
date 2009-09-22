(ns overtone.synth
  (:import 
     (java.util.regex Pattern)
     (de.sciss.jcollider Server Constants UGenInfo UGen
                         Group Node Control Constant 
                         GraphElem GraphElemArray
                         Synth SynthDef UGenChannel))
  (:use 
     (clojure walk inspector)
     (overtone sc)))

;; NOTES
;; 
;; sclang will sometimes generate multiple ugens when you think it is
;; just making one.  Need to add support for something similar to make
;; it easy to use: multiplier, adder, completion-actions

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
  (str (.name arg)
       (if (.isArray arg)
         "[]"
         (str "=" (.def arg)))))

(defn ugen-info [name info]
  {:name name 
   :args (for [arg (.args info)] (ugen-arg arg))})

(defn ugens []
  (for [[name info] (UGenInfo/infos)] (ugen-info name info)))

(defn print-ugens 
  ([] (print-ugens (ugens)))
  ([ugens]
   (doseq [ugen ugens]
     (println (str (:name ugen) ": ") (vec (for [arg (:args ugen)] 
                                             (str (:name arg) (if (:array? arg) "." nil))))))))

(defn find-ugen 
  "Returns the ugens with a name or description containing phrase."
  [phrase & exact]
  (let [filt-fun (if exact
                   #(= phrase (:name %1)) 
                   #(re-find (Pattern/compile phrase Pattern/CASE_INSENSITIVE) (:name %1)))]
      (filter filt-fun (ugens))))

(defn ugen-ir [args]
  (clojure.lang.Reflector/invokeStaticMethod "de.sciss.jcollider.UGen" "ir" (to-array args)))

(defn ugen-array [args]
  (GraphElemArray. (into-array GraphElem args)))
;  (clojure.lang.Reflector/invokeStaticMethod "de.sciss.jcollider.UGen" "array" (into-array GraphElem args)))

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

;; NOTE: We do this weird invokeStaticMethod stuff because using
;;       (apply UGen/kr args) doesn't work...
  
(defn kr [& args]
  (clojure.lang.Reflector/invokeStaticMethod UGen "kr" (to-array (ugenify args))))

;; TODO:
;;  It sucks to have this hack in order to support UGens that for some reason
;;  require a number of channels to be specified when we instantiate it using
;;  jCollider.  Figure out a better way to deal with this... Maybe by getting
;;  rid of jCollider?
(def NEED-CHAN 
  {"DiskIn" true
   "PlayBuf" true})

(defn ar [& args]
  (let [args (ugenify args)
        args (if (contains? NEED-CHAN (first args)) 
               (concat [(first args) (int 1)] (rest args)) 
               args)]
  (clojure.lang.Reflector/invokeStaticMethod UGen "ar" (to-array args))))

(defn dr [& args]
  (clojure.lang.Reflector/invokeStaticMethod 
    "de.sciss.jcollider.UGen" 
    "dr" (to-array (ugenify args))))

(defn ctl-ir [args]
  (clojure.lang.Reflector/invokeStaticMethod 
    "de.sciss.jcollider.Control" 
    "ir" (to-array (ugenify args))))

(defn ctl-kr [args]
  (clojure.lang.Reflector/invokeStaticMethod 
    "de.sciss.jcollider.Control" 
    "kr" (to-array (ugenify args))))
  
;; TODO: Look into doing things with simple OSC messaging if creating client-side representations of synths is slow.  We can do it like this:
;s.sendMsg("/s_new", "MyFavoriteSynth", n = s.nextNodeID;);
;s.sendMsg("/n_free", n);
  
(defn normalize-ugen-name [n]
  (.replaceAll (.toLowerCase (str n)) "[-|_]" ""))

(defn ugen-map [names]
  (apply hash-map (mapcat #(vector (normalize-ugen-name %1) %1) names)))

(def UGEN-MAP (ugen-map (map :name (ugens))))

(defn ugen-match [word]
  (get UGEN-MAP (normalize-ugen-name (.substring word 0 (- (count word) 3)))))

(defn replace-name [l]
  (let [word (str (first l))]
    (concat 
      (cond 
        (.endsWith word ".ar") ['ar (ugen-match word)]
        (.endsWith word ".kr") ['kr (ugen-match word)]
        (.endsWith word ".dr") ['dr (ugen-match word)]
        true [(symbol word)]) 
      (rest l))))

(defn replace-ugens
  "Find all the forms starting with a valid ugen identifier, and convert it to a function argument to
  a ugen constructor compabitible with JCollider."
  [form]
  (postwalk (fn [x] (if (and (seq? x) 
                             (symbol? (first x)))
                      (replace-name x) 
                      x)) 
            form))

(defmacro defsynth [name & body]
  (let [renamed (replace-ugens body)]
    `(def ~(symbol (str name)) (SynthDef. ~(str name) ~@renamed))))

(defmacro syn [& body]
  (first (replace-ugens body)))

(def ENV-CURVES
  {:step        0
   :lin         1
   :linear      1
   :exp         2
   :exponential 2
   :sin         3
   :sine        3
   :wel         4
   :welch       4
   :sqr         6
   :squared     6
   :cub         7
   :cubed       7
 })

(defn- curve-to-shapes 
  "Create the shapes list corresponding to either a curve type or a set of curve types."
  [c]
  (cond 
    (keyword? c) (repeat (c ENV-CURVES))
    (or
      (seq? c) 
      (number? c))  (repeat 5)))

(defn- curve-to-curves
  "Create the curves list for this curve type."
  [c]
  (repeat (if (number? c) c 0)))

;; Envelope spec for use with EnvGen
;;   We provide a description of the envelope curve to EnvGen.  
;;   It uses an array with values organized like this:
;;
;;  [ <initialLevel>, <numberOfSegments>, <releaseNode>, <loopNode>, 
;;    <segment1TargetLevel>, <segment1Duration>, <segment1Shape>, <segment1Curve>, 
;;    <segment2...> ]

(defn envelope 
  "Create an envelope curve description array suitable for the EnvGen ugen."
  [levels durations & [curve release-node loop-node]]
  (let [curve (or curve :linear)
        reln  (or release-node -99)
        loopn (or loop-node -99)
        shapes (curve-to-shapes curve)
        curves (curve-to-curves curve)]
    (apply vector 
      (concat [(first levels) (count durations) reln loopn]
            (interleave (rest levels) durations shapes curves)))))

(defn triangle [& [dur level]]
  (let [dur   (or dur 1)
        dur   (* dur 0.5)
        level (or level 1)]
    (envelope [0 level 0] [dur dur])))

(defn sine [& [dur level]]
  (let [dur   (or dur 1)
        dur   (* dur 0.5)
        level (or level 1)]
    (envelope [0 level 0] [dur dur] :sine)))

(defn perc [& [attack release level curve]]
  (let [attack  (or attack 0.01)
        release (or release 1)
        level   (or level 1)
        curve   (or curve -4)]
    (envelope [0 level 0] [attack release] curve)))

(defn linen [& [attack sustain release level curve]]
  (let [attack  (or attack 0.01)
        sustain (or sustain 1)
        release (or release 1)
        level   (or level 1)
        curve   (or curve :linear)]
    (envelope [0 level level 0] [attack sustain release] curve)))

(defn cutoff [& [release level curve]]
  (let [release (or release 0.1)
        level   (or level 1)
        curve   (or curve :linear)]
    (envelope [level 0] [release] curve 0)))

(defn dadsr [& [delay-t attack decay sustain release level curve bias]]
  (let [delay-t (or delay-t 0.1)
        attack  (or attack 0.01)
        decay   (or decay 0.3)
        sustain (or sustain 0.5)
        release (or release 1)
        level   (or level 1)
        curve   (or curve -4)
        bias    (or bias 0)]
    (envelope 
      (map #(+ %1 bias) [0 0 level (* level sustain) 0])
      [delay-t attack decay release] curve)))

(defn adsr [& [attack decay sustain release level curve bias]]
  (let [attack  (or attack 0.01)
        decay   (or decay 0.3)
        sustain (or sustain 1)
        release (or release 1)
        level   (or level 1)
        curve   (or curve -4)
        bias    (or bias 0)]
    (envelope 
      (map #(+ %1 bias) [0 level (* level sustain) 0])
      [attack decay release] curve 2)))

(defn asr [& [attack sustain release curve]]
  (let [attack  (or attack 0.01)
        sustain (or sustain 1)
        release (or release 1)
        curve   (or curve -4)]
    (envelope [0 sustain 0] [attack release] curve)))

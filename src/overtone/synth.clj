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
   :array? (.isArray arg) 
   :min (.min arg)
   :max (.max arg)})

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

(defn ar [& args]
  (clojure.lang.Reflector/invokeStaticMethod UGen "ar" (to-array (ugenify args))))

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


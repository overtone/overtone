(ns overtone.synth
  (:import 
     (java.util.regex Pattern)
     (de.sciss.jcollider Constants UGenInfo UGen
                         Group Node Control Constant 
                         GraphElem GraphElemArray
                         Synth SynthDef UGenChannel)
     (de.sciss.jcollider.gui SynthDefDiagram))
  (:use 
     clojure.contrib.seq-utils
     (clojure walk inspector)
     (overtone sc utils)))

(UGenInfo/readDefinitions)

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
  {:name (.name arg)
   :array? (.isArray arg)
   :default (.def arg)})

(defn ugen-info [name info]
  {:name name 
   :args (doall (for [arg (.args info)] (ugen-arg arg)))
   :rates (set (.rates info))
   :fixed-outs (.outputVal info)
   :out-type (.outputType info)})

(defn ugens []
  (for [[name info] (UGenInfo/infos)] (ugen-info name info)))

(def UGEN-PATH "/home/rosejn/projects/overtone/src/overtone/ugens.clj")
(comment defn scrape-ugens []
  (binding [*out* (clojure.contrib.duck-streams/writer UGEN-PATH)]
    (println 
"(ns overtone.ugens)
         
;; SuperCollider ugen data scraped from JCollider. Thanks sciss!!! 
;; The file still requires some massaging, since Float/POSITIVE_INFINITY and
;; friends don't seem to serialize correctly.  Also need to quote args lists...
(def UGENS [")
    (doseq [ugen (sort-by :name (ugens))]
      (prn ugen)
      (println ""))
    (println "])")))

  ;; Initially scraped from JCollider
  ;; (writer "/home/rosejn/projects/overtone/ugens.clj")
  ;;

(defmethod print-method Constant [const w]
  (.write w (str "#<Const: " (.getValue const) " >")))

(defmethod print-method UGen [ugen w]
  (.write w (str "#<" (.getName ugen) " >")))

(defn print-ugens 
  ([] (print-ugens (ugens)))
  ([ugens]
   (doseq [ugen ugens]
     (println (str (:name ugen) ": ") (vec (:args ugen))))))
                                        ;(doseq [arg (:args ugen)] 
                                         ;    (str (:name arg) (if (:array? arg) "." nil))))))))

(defn find-ugen 
  "Returns the ugens with a name or description containing phrase."
  [phrase & exact]
  (let [filt-fun (if exact
                   #(= phrase (:name %1)) 
                   #(re-find (Pattern/compile phrase Pattern/CASE_INSENSITIVE) (:name %1)))]
      (filter filt-fun (ugens))))

(defn show [& args]
  (print-ugens (apply find-ugen args)))

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
  (let [ugen-name (first args)
        args (rest args)
        chans (if (contains? NEED-CHAN ugen-name)
                (first args)
                -1)
        args (if chans (ugenify (rest args)) (ugenify args))] 
    (println "ar ugen: " ugen-name " chans: " chans "args: " args)
    (UGen/construct ugen-name "audio" chans (into-array GraphElem args))))

(defn dr [& args]
  (clojure.lang.Reflector/invokeStaticMethod 
    "de.sciss.jcollider.UGen" 
    "dr" (to-array (ugenify args))))

(defn ctl-ir [args]
  (clojure.lang.Reflector/invokeStaticMethod 
    "de.sciss.jcollider.Control" 
    "ir" (to-array (ugenify args))))

(defn ctl-args [keyvals]
  (let [[names defaults] (loop [keyvals keyvals
                                names   []
                                defs    []]
                           (if (empty? keyvals)
                             [names defs]
                             (recur (rest (rest keyvals))
                                    (conj names (name  (first keyvals)))
                                    (conj defs  (float (second keyvals))))))
        names (into-array String names)
        defaults (into-array Float/TYPE defaults)]
    [names defaults]))

(defn ctl-chan-map [ctl names]
  (apply hash-map (flatten 
                    (for [i (range (count names)) n names] 
                      [(keyword n) (.getChannel ctl i)]))))


; Takes a series of key-value pairs, and returns a map of key -> control-channel suitable for
; inserting in synthdefs.
(defn ctl-kr [& keyvals]
  (let [[names defaults] (ctl-args keyvals)
        ctl (Control/kr names defaults)]
    (ctl-chan-map ctl names)))
  
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

;(def *synths (ref {}))

(defmacro defsynth [name & body]
  (let [renamed (replace-ugens body)]
    `(def ~(symbol (str name)) 
       (let [sdef# (SynthDef. ~(str name) ~@renamed)]
         (snd-to-synth (.recvMsg sdef#))
         ~(str name)))))

;(dosync (alter *synths assoc ~(str name) sdef#))

(defmacro syn [& body]
  (first (replace-ugens body)))

(defn- build-mix 
  [cur inputs]
  (if (empty? inputs) 
    cur
    (recur (syn (mul-add.ar cur 0.8 (first inputs)))
           (rest inputs))))

(defn mix 
  "Mix any number of input channels down to one.
  (mix (sin-osc 440) (sin-osc 400))"
  [in & chans] (build-mix in chans))

(defn stereo [in & args]
  (let [args (merge {:vol 0.2 :pan 0} (apply hash-map args))
        ctl (ctl-kr :vol (:vol args) :pan (:pan args))]
    (syn (out.ar 0 (pan2.ar in))))); (:pan ctl) (:vol ctl))))))

(defn view [sdef]
  (SynthDefDiagram. sdef))


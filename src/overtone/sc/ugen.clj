(ns
    ^{:doc "UGens, or Unit Generators, are the functions that act as DSP nodes in the synthesizer definitions used by SuperCollider.  We generate the UGen functions based on hand written metadata about each ugen (ugen directory). (Eventually we hope to get this information dynamically from the server.)"
      :author "Jeff Rose & Christophe McKeon"}
  overtone.sc.ugen
  (:use
   [overtone util]
   [overtone.sc.ugen sc-ugen defaults specs special-ops]
   [clojure.contrib.generic :only (root-type)]
   [overtone.sc.ugen.metadata unaryopugen binaryopugen])
  (:require [clojure.contrib.generic.arithmetic :as ga]
            [clojure.contrib.generic.comparison :as gc]
            [clojure.contrib.generic.math-functions :as gm]
            [overtone.sc.ugen.doc :as doc]))

(defn- op-rate
  "Lookup the rate of an input ugen, otherwise use IR because the operand
  must be a constant float."
  [arg]
  (if (sc-ugen? arg)
    (:rate arg)
    (get RATES :ir)))

(defn inf!
  "users use this to tag infinite sequences for use as
   expanded arguments. needed or else multichannel
   expansion will blow up"
  [sq]
  (with-meta sq
    (merge (meta sq) {:infinite-sequence true})))

(defn- inf? [obj]
  (:infinite-sequence (meta obj)))

(defn- expandable? [arg]
  (and (coll? arg)
       (not (map? arg))))

(defn- multichannel-expand
  "Does sc style multichannel expansion.
  * does not expand seqs flagged infinite
  * note that this returns a list even in the case of a single expansion

  Takes expand-flags, a seq of boolean values representing whether a given arg
  should be expanded. Each nth expand-flag boolean corresponds to each nth arg
  where arg is a seq of arguments passed into the ugen fn.
  "
  [expand-flags args]
  (if (zero? (count args))
    [[]]
    (let [gc-seqs (fn [[gcount seqs flags] arg]
                    (cond
                     ;; Infinite seqs can be used to generate values for expansion
                     (inf? arg) [gcount
                                 (conj seqs arg)
                                 (next flags)]

                     ;; Regular, non-infinite and non-map collections get expanded
                     (and (expandable? arg)
                          (first flags)) [(max gcount (count arg))
                                          (conj seqs (cycle arg))
                                          (next flags)]

                          :else ;; Basic values get used for all expansions
                          [gcount
                           (conj seqs (repeat arg))
                           (next flags)]))
          [greatest-count seqs] (reduce gc-seqs [1 [] expand-flags] args)]
      (take greatest-count (parallel-seqs seqs)))))

(defn make-expanding
  "Takes a function and returns a multi-channel-expanding version of the
  function."
  [f expand-flags]
  (fn [& args]
    (let [expanded (mapply f (multichannel-expand expand-flags args))]
      (if (= (count expanded) 1)
        (first expanded)
        expanded))))

;; TODO: Finish me!
;; Need to execute the check predicates to do things like verify argument rates, etc...
(defn check-ugen-args [spec rate special args]
  (if (vector? (:check spec))
    (doseq [check (:check spec)]
      (check rate special args))))



(def *ugens* nil)
(def *constants* nil)

(defn ugen [spec rate special args]
  "Create a SCUGen with the specified spec, rate, special and args"
  ;;(check-ugen-args spec rate special args)
  (let [rate (or (get RATES rate) rate)
        args (if args args [])
        ug (sc-ugen
            (next-id :ugen)
            (:name spec)
            rate
            (REVERSE-RATES rate)
            special
            args
            (or (:num-outs spec) 1))
        ug (if (contains? spec :init) ((:init spec) ug) ug)]
    (when (and *ugens* *constants*)
      (set! *ugens* (conj *ugens* ug))
      (doseq [const (filter number? (:args ug))]
        (set! *constants* (conj *constants* const))))

    (if (> (:n-outputs ug) 1)
      (map-indexed (fn [idx _] (output-proxy ug idx)) (range (:n-outputs ug)))
      ug)))

(defn- ugen-base-fn [spec rate special]
  (fn [& args]
    (ugen spec rate special args)))

(defn- make-ugen
  "Create a callable map representing a ugen."
  [spec rate ugen-fn]
  (callable-map {:name       (overtone-ugen-name (:name spec))
                 :summary    (:summary spec)
                 :doc        (:doc spec)
                 :full-doc   (:full-doc spec)
                 :categories (:categories spec)
                 :rate       rate
                 :src        "Implemented in C code"
                 :type       ::ugen
                 :params     (:args spec)}
                ugen-fn))

(defn- make-ugen-fn
  "Make a function representing the given ugen that will fill in default
  arguments, rates, etc."
  [spec rate special]
  (let [expand-flags (map #(:expands? %) (:args spec))]
    (make-expanding
     (ugen-base-fn spec rate special) expand-flags)))

;; TODO: Figure out the complete list of control types
;; This is used to determine the controls we list in the synthdef, so we need
;; all ugens which should be specified as external controls.
(def CONTROLS #{"control" "trig-control audio-control"})

(defn mk-generic-control-ugen
  [name rate n-outputs offset]
  (with-meta {:id (next-id :ugen)
              :name name
              :rate (rate RATES)
              :rate-name (REVERSE-RATES (rate RATES))
              :special offset
              :args nil
              :n-outputs n-outputs
              :outputs (repeat n-outputs {:rate (rate RATES)})
              :n-inputs 0
              :inputs []}
    {:type ::ugen}))

(defn control-ugen
  "Creates a new control ugen at control rate.
  Used as the standard control proxy for synths"
  [n-outputs offset]
  (mk-generic-control-ugen "Control" :kr  n-outputs offset))

(defn trig-control-ugen
  "Creates a new trigger control ugen at control rate.
  Used as a trigger-style control proxy for synths"
  [n-outputs offset]
  (mk-generic-control-ugen "TrigControl" :kr n-outputs offset))

(defn audio-control-ugen
  "Creates a new control ugen at audio rate.
  Used for audio-rate modulation of synth params"
  [n-outputs offset]
  (mk-generic-control-ugen "AudioControl" :ar n-outputs offset))

(defn inst-control-ugen
  "Creates a new control ugen at instrument rate
  Used for instrument-rate modulation of synth params"
  [n-outputs offset]
  (mk-generic-control-ugen "Control" :ir  n-outputs offset))

(defn control? [obj]
  (isa? (type obj) ::control))


(defn- overload-ugen-op [ns ugen-name ugen-fn]
  (let [original-fn (ns-resolve ns ugen-name)]
    (ns-unmap ns ugen-name)
    (intern ns ugen-name (fn [& args]
                           (if (some #(or (sc-ugen? %) (not (number? %))) args)
                             (apply ugen-fn args)
                             (apply original-fn args))))))

(defn- def-ugen
  "Create and intern a set of functions for a given ugen-spec.
    * base name function using default rate and no suffix (e.g. env-gen )
    * base-name plus rate suffix functions for each rate (e.g. env-gen:ar,
      env-gen:kr)
  "
  [to-ns spec special]
  (let [metadata {:doc (:full-doc spec)
                  :arglists (list (vec (map #(symbol (:name %))
                                            (:args spec))))}
        ugen-fns (map (fn [[uname rate]] [(with-meta (symbol uname) metadata)
                                         (make-ugen
                                          spec
                                          rate
                                          (make-ugen-fn spec rate special))])
                      (:fn-names spec))]
    (doseq [[ugen-name ugen-fn] ugen-fns]
      (intern to-ns ugen-name ugen-fn))))

(defn- def-unary-op
  "def a unary op ugen (this is handled separately due to the fact that the
  unaryopugen represents multiple functionality represented by multple fns
  in overtone)."
  [to-ns op-name special]
  (let [orig-spec (get UGEN-SPECS "unaryopugen")
        doc-spec  (get unaryopugen-docspecs op-name {})
        full-spec (merge orig-spec doc-spec {:name op-name
                                             :categories [["Unary Operations"]]})
        full-spec (doc/with-full-doc full-spec)
        metadata  {:doc (:full-doc full-spec)
                   :arglists (list (vec (map #(symbol (:name %))
                                             (:args full-spec))))}
        ugen-name (symbol (overtone-ugen-name op-name))
        ugen-name (with-meta ugen-name metadata)
        ugen-fn   (fn [arg]
                    (ugen orig-spec (op-rate arg) special (list arg)))
        ugen-fn   (make-expanding ugen-fn [true])
        ugen      (make-ugen full-spec :auto ugen-fn)]

    (if (ns-resolve to-ns ugen-name)
      (overload-ugen-op to-ns ugen-name ugen)
      (intern to-ns ugen-name ugen))))

(defn- def-binary-op
  "def a binary op ugen (this is handled separately due to the fact that the
  binaryopugen represents multiple functionality represented by multple fns
  in overtone)."
  [to-ns op-name special]
  (let [
        orig-spec (get UGEN-SPECS "binaryopugen")
        doc-spec  (get binaryopugen-docspecs op-name {})
        full-spec (merge orig-spec doc-spec {:name op-name
                                             :categories [["Binary Operations"]]})
        full-spec (doc/with-full-doc full-spec)
        metadata  {:doc (:full-doc full-spec)
                   :arglists (list (vec (map #(symbol (:name %))
                                             (:args full-spec))))}
        ugen-name (symbol (overtone-ugen-name op-name))
        ugen-name (with-meta ugen-name metadata)
        ugen-fn   (fn [a b]
                    (ugen orig-spec (max (op-rate a) (op-rate b)) special (list a b)))
        ugen-fn   (make-expanding ugen-fn [true true])
        ugen (make-ugen full-spec :auto ugen-fn )]
    (if (ns-resolve to-ns ugen-name)
      (overload-ugen-op to-ns ugen-name ugen)
      (intern to-ns ugen-name ugen))))

;; We define this uniquely because it has to be smart about its rate.
;; TODO: I think this should probably be handled by one of the ugen modes
;; that is currently not yet implemented...
(def mul-add-ugen
  (fn [in mul add]
    (ugen {:name "MulAdd",
           :args [{:name "in"}
                  {:name "mul", :default 1.0}
                  {:name "add", :default 0.0}]
           :doc "Multiply and add, equivalent to (+ add (* mul in))"}
          (op-rate in) 0 (list in mul add))))

(derive :overtone.sc.ugen.sc-ugen/sc-ugen root-type)

(def generics
  { "+"  :arithmetic
    "-"  :arithmetic
    "*"  :arithmetic
    "/"  :arithmetic
    ">"  :comparison
    "<"  :comparison
    "="  :comparison
    "<=" :comparison
    ">=" :comparison
;;TODO addme when available in gc    "!=" :comparison
    })

(def generic-namespaces
  { :arithmetic "ga"
    :comparison "gc"})

(defmacro mk-binary-op-ugen [sym]
  (let [generic-kind (get generics (to-str sym))
        gen-nspace   (get generic-namespaces generic-kind)
        sym (to-str sym)]
    `(do
      (defmethod ~(symbol gen-nspace sym) [:overtone.sc.ugen.sc-ugen/sc-ugen :overtone.sc.ugen.sc-ugen/sc-ugen]
         [a# b#]
         (ugen (get UGEN-SPECS "binaryopugen")
               (max (op-rate a#) (op-rate b#))
               ~(get BINARY-OPS-FULL sym)
               (list a# b#)))

      (defmethod ~(symbol gen-nspace sym) [root-type :overtone.sc.ugen.sc-ugen/sc-ugen]
         [a# b#]
         (ugen (get UGEN-SPECS "binaryopugen")
               (op-rate b#)
               ~(get BINARY-OPS-FULL sym)
               (list a# b#)))

      (defmethod ~(symbol gen-nspace sym) [:overtone.sc.ugen.sc-ugen/sc-ugen root-type]
         [a# b#]
         (ugen (get UGEN-SPECS "binaryopugen")
               (op-rate a#)
               ~(get BINARY-OPS-FULL sym)
               (list a# b#))))))

(mk-binary-op-ugen :+)
(mk-binary-op-ugen :-)
(mk-binary-op-ugen :*)
(mk-binary-op-ugen :<)
(mk-binary-op-ugen :>)
(mk-binary-op-ugen :<=)
(mk-binary-op-ugen :>=)
(mk-binary-op-ugen :=)
;; TODO addme when available in gc (mk-binary-op-ugen :!=)

(def div-meth (var-get (resolve (symbol "clojure.contrib.generic.arithmetic" "/"))))

(defmethod div-meth [:overtone.sc.ugen.sc-ugen/sc-ugen :overtone.sc.ugen.sc-ugen/sc-ugen]
  [a b]
  (ugen (get UGEN-SPECS "binaryopugen")
        (max (op-rate a) (op-rate b))
        4
        (list a b)))

(defmethod div-meth [:overtone.sc.ugen.sc-ugen/sc-ugen root-type]
  [a b]
  (ugen (get UGEN-SPECS "binaryopugen")
        (op-rate a)
        4
        (list a b)))

(defmethod div-meth [root-type :overtone.sc.ugen.sc-ugen/sc-ugen]
  [a b]
  (ugen (get UGEN-SPECS "binaryopugen")
        (op-rate b)
        4
        (list a b)))


(defmethod ga/- :overtone.sc.ugen.sc-ugen/sc-ugen
  [a]
  (ugen (get UGEN-SPECS "unaryopugen")
        (op-rate a)
        0
        (list a)))


(defn intern-ugens
  "Iterate over all UGen meta-data, generate the corresponding functions and
  intern them in the current or otherwise specified namespace."
  ([] (intern-ugens *ns*))
  ([to-ns]
     (doseq [ugen (filter #(not (or (= "UnaryOpUGen" (:name %))
                                    (= "BinaryOpUGen" (:name %))))
                          (vals UGEN-SPECS))]
       (def-ugen to-ns ugen 0))
     (doseq [[op-name special] UNARY-OPS]
       (def-unary-op to-ns op-name special))
     (doseq [[op-name special] BINARY-OPS]
       (def-binary-op to-ns op-name special))
     (intern to-ns 'mul-add (make-expanding mul-add-ugen [true true true]))))

(defn intern-ugens-collide
  "Intern the ugens that collide with built-in clojure functions to the current
  or otherwise specified namespace"
  ([] (intern-ugens-collide *ns*))
  ([to-ns]
     (doseq [[op kind] generics]
       (let [func (var-get (resolve (symbol (str "clojure.contrib.generic." (to-str kind)) op)))]
         (ns-unmap to-ns (symbol op))
         (intern to-ns (symbol op) (make-expanding func [true true]))))

     ;; intern div-meth so we can access the division operator since / is special cased
     (intern to-ns 'div-meth
             (make-expanding (var-get (resolve
                                       (symbol "clojure.contrib.generic.arithmetic" "/")))
                             [true true]))

     (doseq [[op-name special] UNARY-OPS-COLLIDE]
       (def-unary-op to-ns op-name special))
     (doseq [[op-name special] BINARY-OPS-COLLIDE]
       (def-binary-op to-ns op-name special))))


;; We refer all the ugen functions here so they can be access by other parts
;; of the Overtone system using a fixed namespace.  For example, to automatically
;; stick an Out ugen on synths that don't explicitly use one.
(defonce _ugens (intern-ugens))
(defonce _colliders (intern-ugens-collide (create-ns 'overtone.ugen-collide)))

(defmacro with-ugens [& body]
  `(let [~'+ overtone.ugen-collide/+
         ~'- overtone.ugen-collide/-
         ~'* overtone.ugen-collide/*
         ~'< overtone.ugen-collide/<
         ~'> overtone.ugen-collide/>
         ~'= overtone.ugen-collide/=
         ~'/ overtone.ugen-collide/div-meth
         ~'>= overtone.ugen-collide/>=
         ~'<= overtone.ugen-collide/<=
;;TODO addme when available in gc  ~'!= overtone.ugen-collide/!=
         ~'rand overtone.ugen-collide/rand
         ~'mod overtone.ugen-collide/mod
         ~'bit-not overtone.ugen-collide/bit-not]
     ~@body))

(defn mix
  "Mix down (sum) a list of input channels into a single channel."
  [ins]
  (apply overtone.ugen-collide/+ ins))

(defn- splay-pan
  "Given n channels and a center point, returns a position in a stereo field
  for each channel, evenly distributed from the center +- spread."
  [n center spread]
  (for [i (range n)]
    (+ center
       (* spread
          (- (* i
                (/ 2 (dec n)))
             1)))))

(defn splay
  "Spread input channels across a stereo field, with control over the center point
  and spread width of the target field, and level compensation that lowers the volume
  for each additional input channel."
  [in-array & {:as options}]
  (with-ugens
    (let [options (merge {:spread 1 :level 1 :center 0 :level-comp true} options)
          {:keys [spread level center level-comp]} options
           n (count in-array)
          level (if level-comp
                  (* level (Math/sqrt (/ 1 (dec n))))
                  level)
          positions (splay-pan n center spread)
          pans (pan2 in-array positions level)]
      (map + (parallel-seqs pans)))))

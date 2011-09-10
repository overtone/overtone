(ns
    ^{:doc "UGens, or Unit Generators, are the functions that act as DSP nodes in the synthesizer definitions used by SuperCollider.  We generate the UGen functions based on hand written metadata about each ugen (ugen directory). (Eventually we hope to get this information dynamically from the server.)"
      :author "Jeff Rose & Christophe McKeon"}
  overtone.sc.ugen
  (:use [overtone.util lib]
        [overtone.sc.ugen sc-ugen defaults specs special-ops]
        [overtone.sc.ugen.metadata unaryopugen binaryopugen])
  (:require [overtone.sc.ugen.doc :as doc]))

;;Create a ns to store all ugens that collide with standard ugen fns
(defonce ugen-collide-ns (create-ns 'overtone.ugen-collide))

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

(def ^{:dynamic true} *ugens* nil)
(def ^{:dynamic true} *constants* nil)

(defn ugen [spec rate special args]
  "Create a SCUGen with the specified spec, rate, special and args"
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

(defn- overload-binary-ugen-op [src-ns target-ns ugen-name ugen-fn]
  (let [original-fn (ns-resolve src-ns ugen-name)
        ugen-name   (if (= '/ ugen-name) 'binary-div-op ugen-name)]

    (ns-unmap target-ns ugen-name)
    (intern target-ns ugen-name (fn [& args]
                                  (if (some #(or (sc-ugen? %) (not (number? %))) args)
                                    (let [x    (first args)
                                          y    (second args)
                                          more (drop 2 args)]
                                      (reduce ugen-fn (ugen-fn x y) more))
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

(defonce special-op-specs* (atom {}))

(defn- def-unary-op
  "def a unary op ugen (this is handled separately due to the fact that the
  unaryopugen represents multiple functionality represented by multple fns
  in overtone)."
  [to-ns op-name special]
  (let [normalized-n (normalize-ugen-name op-name)
        orig-spec (get UGEN-SPECS "unaryopugen")
        doc-spec  (get unaryopugen-docspecs normalized-n {})
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

    (swap! special-op-specs* assoc normalized-n full-spec)
    (if (ns-resolve to-ns ugen-name)
      (throw (Exception. (str "Attempted to define a unary op with the same name as a function which already exists: " op-name)))
      (intern to-ns ugen-name ugen))))

(defn- def-binary-op
  "def a binary op ugen (this is handled separately due to the fact that the
  binaryopugen represents multiple functionality represented by multple fns
  in overtone)."
  [to-ns op-name special]
  (let [
        normalized-n (normalize-ugen-name op-name)
        orig-spec    (get UGEN-SPECS "binaryopugen")
        doc-spec     (get binaryopugen-docspecs normalized-n {})
        full-spec    (merge orig-spec doc-spec {:name op-name
                                                :categories [["Binary Operations"]]})
        full-spec    (doc/with-full-doc full-spec)
        metadata     {:doc (:full-doc full-spec)
                      :arglists (list (vec (map #(symbol (:name %))
                                                (:args full-spec))))}
        ugen-name    (symbol (overtone-ugen-name op-name))
        ugen-name    (with-meta ugen-name metadata)
        ugen-fn      (fn [a b]
                       (ugen orig-spec (max (op-rate a) (op-rate b)) special (list a b)))
        ugen-fn      (make-expanding ugen-fn [true true])
        ugen         (make-ugen full-spec :auto ugen-fn )]
    (swap! special-op-specs* assoc normalized-n full-spec)
    (if (ns-resolve to-ns ugen-name)
      (overload-binary-ugen-op to-ns ugen-collide-ns ugen-name ugen)
      (intern to-ns ugen-name ugen))))

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
     (doseq [[op-name special] BINARY-OPS-FULL]
       (def-binary-op to-ns op-name special))))


;; We refer all the ugen functions here so they can be access by other parts
;; of the Overtone system using a fixed namespace.  For example, to automatically
;; stick an Out ugen on synths that don't explicitly use one.
(defonce _ugens (intern-ugens))

(defmacro with-ugens [& body]
  `(let [~'+ overtone.ugen-collide/+
         ~'- overtone.ugen-collide/-
         ~'* overtone.ugen-collide/*
         ~'< overtone.ugen-collide/<
         ~'> overtone.ugen-collide/>
         ~'= overtone.ugen-collide/=
         ~'/ overtone.ugen-collide/binary-div-op
         ~'>= overtone.ugen-collide/>=
         ~'<= overtone.ugen-collide/<=
;;TODO addme when available in gc  ~'!= overtone.ugen-collide/!=
         ~'mod overtone.ugen-collide/mod
         ~'min overtone.ugen-collide/min
         ~'max overtone.ugen-collide/max]
     ~@body))

(defn combined-specs
  []
  (merge UGEN-SPECS @special-op-specs*))

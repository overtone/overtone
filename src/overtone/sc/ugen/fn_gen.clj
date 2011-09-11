(ns
    ^{:doc "Code to generate the ugen fns"
      :author "Jeff Rose, Christophe McKeon and Sam Aaron"}
  overtone.sc.ugen.fn-gen
  (:use [overtone.util lib]
        [overtone.sc.ugen sc-ugen defaults specs special-ops]
        [overtone.sc.ugen.metadata unaryopugen binaryopugen])
  (:require [overtone.sc.ugen.doc :as doc]))


;;Create a ns to store all ugens that collide with standard ugen fns
(def ugen-collide-ns-str "overtone.sc.ugen-collide")
(defonce ugen-collide-ns (create-ns (symbol ugen-collide-ns-str)))
(defonce overloaded-ugens* (atom {}))
(defonce special-op-specs* (atom {}))

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

(defn mk-scugen
  "Create a SCUGen with the specified spec, rate, special and args"
  [spec rate special args]
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
    (mk-scugen spec rate special args)))

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

(defn- args-list-is-a-map?
  "Returns true if args list contains one element which is a map"
  [args]
  (and (= (count args) 1)
       (map? (first args))))

(defn- foldable-binary-ugen?
  "Is this binary ugen foldable? i.e. should it be allowed to take more than
  two arguments, resulting in a folded nesting of linked ugens. Ugen must be
  defined as being foldable and the args must not be a map or a combination of
  ordered params keyword args"
  [ug-name args]
  (and
   (FOLDABLE-BINARY-OPS (str ug-name))
   (not (args-list-is-a-map? args))
   (not (some keyword? args)))  )

(defn- treat-as-binary-ugen?
  "Checks the arglist to see whether the args contain other ugens or
  non-numerical elements (binary ugen fns may only be called with numbers,
  ugens sequences and keywords or simply passed an arg map). Used to determine
  whether the ugen fn should be called or the original fn it collided with."
  [args]
  (or
   (and (some sc-ugen? args)
        (every? #(or (sc-ugen? %) (number? %) (sequential? %) (keyword? %)) args))
   (args-list-is-a-map? args)))


(defn- foldable-binary-ugen
  "Create a foldable binary ugen - a binary ugen which may accept more than two
  args - each additional arg will result in an extra ugen added/folded into the
  chain of ugens."
  [ugen-fn args]
  (when (< (count args) 2)
    (throw (IllegalArgumentException. (str "Attempted to call foldable binary op ugen with fewer than 2 args (" (count args) "). You passed: " [args] ))))

  (let [x    (first args)
        y    (second args)
        more (drop 2 args)]
    (reduce ugen-fn (ugen-fn x y) more)))

(defn- mk-overloaded-binary-ugen-fn
  "Returns a fn which implements an overloaded binary ugen. Checks whether the
  ugen is foldable and the returning fn either implements folding or ensures
  that there are only 2 params."
  [ugen-name ugen-fn]
  (fn [& args]
    (if (foldable-binary-ugen? ugen-name args)
        (foldable-binary-ugen ugen-fn args)
        (apply ugen-fn args))))

(defn- overload-binary-ugen-op
  "Overload the binary op by placing the overloaded fn definition in a separate
  namespace. This overloaded fn will check incoming args on application to
  determine whether the original fn or overloaded fn should be called. The
  overloaded fns are then made available through the use of the macro
  with-overloaded-ugens"
  [src-ns target-ns ugen-name ugen-fn]
  (let [original-fn   (ns-resolve src-ns ugen-name)
        overload-name (if (= '/ ugen-name) 'binary-div-op ugen-name)
        ugen-name-str (str ugen-name)
        overloaded-fn (mk-overloaded-binary-ugen-fn ugen-name-str ugen-fn)]
    (swap! overloaded-ugens* assoc ugen-name overload-name)
    (ns-unmap target-ns overload-name)
    (intern target-ns overload-name (fn [& args]
                                      (if (treat-as-binary-ugen? args)
                                        (apply overloaded-fn args)
                                        (apply original-fn args))))))

(defn- def-ugen
  "Create and intern a set of functions for a given ugen-spec.
    * base name function using default rate and no suffix (e.g. env-gen )
    * base-name plus rate suffix functions for each rate (e.g. env-gen:ar,
      env-gen:kr)"
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
        ugen-fn   (make-ugen-fn orig-spec :auto special)
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
        ugen-fn      (make-ugen-fn orig-spec :auto special)
        ugen         (make-ugen full-spec :auto ugen-fn)]
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
     (doseq [[op-name special] BINARY-OPS]
       (def-binary-op to-ns op-name special))))

(defmacro with-overloaded-ugens
  "Bind symbols for all overloaded ugens (i.e. + - / etc.) to the overloaded fn
  in the ns overtone.sc.ugen-colliders. These fns will revert back to original
  (Clojure) semantics if not passed with ugen args."
  [& body]
  (let [bindings (flatten (map (fn [[orig overload]]
                                 [orig (symbol ugen-collide-ns-str (str overload))])
                               @overloaded-ugens*))]
    `(let [~@bindings]
      ~@body)))

(defn combined-specs
  "Return a combination of ugen specs and the auto-generated special-op specs."
  []
  (merge UGEN-SPECS @special-op-specs*))

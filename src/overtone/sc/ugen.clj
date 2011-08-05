(ns
    ^{:doc "UGens, or Unit Generators, are the functions that act as DSP nodes in the synthesizer definitions used by SuperCollider.  We generate the UGen functions based on hand written metadata about each ugen (ugen directory). (Eventually we hope to get this information dynamically from the server.)"
      :author "Jeff Rose & Christophe McKeon"}
  overtone.sc.ugen
  (:refer-clojure :exclude (deftype))

  (:use
   clojure.contrib.pprint
   overtone.sc.ugen.defaults
   [overtone util]
   [overtone.sc buffer bus]
   [overtone.sc.ugen special-ops common categories constants]
   [clojure.contrib.types :only (deftype)]
   [clojure.contrib.generic :only (root-type)])
  (:require
   overtone.sc.core
   [overtone.sc.ugen.doc :as doc]
   [clojure.set :as set]
   [clojure.contrib.generic.arithmetic :as ga]
   [clojure.contrib.generic.comparison :as gc]
   [clojure.contrib.generic.math-functions :as gm]))

(def UGEN-SPEC-EXPANSION-MODES
  {:not-expanded false
   :array false
   :append-sequence false
   :append-sequence-set-num-outs false
   :num-outs false
   :action false
   :as-ar true ;; This should still expand right?
   :standard true
   })

(defn normalize-ugen-name
  "Normalizes both SuperCollider and overtone-style names to squeezed lower-case.

  This produces strings that may be used to represent unique ugen keys that can be
  generated from both SC and Overtone names.

  (normalize-ugen-name \"SinOsc\")  ;=> \"sinosc\"
  (normalize-ugen-name \"sin-osc\") ;=> \"sinosc\""
  [n]
  (.replaceAll (.toLowerCase (str n)) "[-|_]" ""))

(defn overtone-ugen-name
  "A basic camelCase to with-dash name converter tuned to convert SuperCollider
  names to Overtone names. Most likely needs improvement.

  (overtone-ugen-name \"SinOsc\") ;=> \"sin-osc\""
  [n]
  (let [n (.replaceAll n "([a-z])([A-Z])" "$1-$2")
        n (.replaceAll n "([A-Z])([A-Z][a-z])" "$1-$2")
        n (.replaceAll n "_" "-")
        n (.toLowerCase n)]
    n))

(defn- derived?
  "Determines whether the supplied ugen spec is derived from another ugen spec.

   This means that the ugen needs to inherit some properties from its parent.
   (The ugen spec's parent is specified using the key :extends)"
  [spec]
  (contains? spec :extends))

(defn- derive-ugen-specs
  "Merge the specified ugen spec maps to give children their parent's attributes
   by recursively reducing the specs to support arbitrary levels of derivation."
  ([specs] (derive-ugen-specs specs {} 0))
  ([children adults depth]
     ;; Make sure a bogus UGen doesn't spin us off into infinity... ;-)
     {:pre [(< depth 8)]}

     (let [[adults children]
           (reduce (fn [[full-specs new-children] spec]
                     (if (derived? spec)
                       (if (contains? full-specs (:extends spec))
                         [(assoc full-specs (:name spec)
                                 (merge (get full-specs (:extends spec)) spec))
                          new-children]
                         [full-specs (conj new-children spec)])
                       [(assoc full-specs (:name spec) spec) new-children]))
                   [adults []]
                   children)]
       (if (empty? children)
         (vals adults)
         (recur children adults (inc depth))))))

(defn- with-rates
  "Add the default ugen rates to any ugen that doesn't explicitly set it."
  [spec]
  (assoc spec :rates (get spec :rates UGEN-DEFAULT-RATES)))

(defn- with-default-rate
  "Calculates the default rate which will be used when the rate isn't explicitly
  used in the fn name (i.e. ugen:kr) or if :ir is available in the rate options"
  [spec]
  (let [rates (:rates spec)
        rate (cond
              (contains? spec :default-rate) (:default-rate spec)
              (= 1 (count rates)) (first rates)
              :default (first (filter rates
                                      UGEN-DEFAULT-RATE-PRECEDENCE)))
        rate (if (or (= :ir rate) (:auto-rate spec))
               :auto
               rate)]
    (assoc spec :default-rate rate)))

(defn- with-categories
  "Adds a :categories attribute to a ugen-spec for later use in documentation
  GUI and REPL interaction."
  [spec]
  (assoc spec :categories (get UGEN-CATEGORIES (overtone-ugen-name (:name spec)) [])))

(defn- with-expands
  "Sets the :expands? attribute for ugen-spec arguments, which will inform the
  automatic channel expansion system when to expand argument."
  [spec]
  (assoc spec :args
         (map (fn [arg]
                (let [expands? (if (:array arg)
                                 false
                                 (get UGEN-SPEC-EXPANSION-MODES
                                      (get arg :mode :standard)))]
                  (assoc arg :expands? expands?)))
              (:args spec))))

(defn- with-fn-names
  "Generates all the function names for this ugen and adds a :fn-names map
  that maps function names to rates, representing the output rate.

  All available rates get an explicit function name of the form <fn-name>:<rate>
  like this:
  * (env-gen:ar ...)
  * (env-gen:kr ...)

  UGens will also have a base-name without a rate suffix that uses the default
  rate. If the ugen spec contains the key :internal-name with a true value,
  the base-name will contain the prefix internal: This is to allow cgens with
  the same name to subsume the role of a specific ugen whilst allowing it to
  reference the original via the prefixed name."
  [spec]
  (let [rates (:rates spec)
        rate-vec (vec rates)
        base-name (overtone-ugen-name (:name spec))
        internal-name? (:internal-name spec)
        base-name (if internal-name? (str "internal:" base-name) base-name)
        base-rate (:default-rate spec)
        name-rates (zipmap (map #(str base-name %) rate-vec)
                           rate-vec)]
    (assoc spec
      :fn-names (assoc name-rates base-name base-rate))))

(defn- args-with-specs
  "Creates a list of (arg-value, arg-spec-item) pairs."
  [args spec property]
  {:pre [(keyword? property)]}
  (partition 2 (interleave args (map property (:args spec)))))

(defn- map-ugen-args
  "Perform argument mappings for ugen args that take a keyword but need to be
  looked up in a map supplied in the spec. (e.g. envelope actions)"
  [spec ugen]
  (let [args (:args ugen)
        args-specs (args-with-specs args spec :map)
        mapped-args (map (fn [[arg map-val]] (if (and (map? map-val)
                                                     (keyword? arg))
                                              (arg map-val)
                                              arg))
                         args-specs)]
    (assoc ugen :args mapped-args)))

(defn- append-seq-args
  "Handles argument modes :append-sequence and :append-sequence-set-num-outs,
  and :append-string  where some ugens take a seq or string for one argument
  which needs to be appended to the end of the argument list when sent to SC.
  (and in the case of strings need to be converted to a list of char ints)"
  [spec ugen]
  (let [args-specs     (args-with-specs (:args ugen) spec :mode)
        pred           #(or (= :append-sequence (second %))
                            (= :append-sequence-set-num-outs (second %))
                            (= :append-string (second %)))
        normal-args    (map first (remove pred args-specs))
        to-append      (filter pred args-specs)
        intify-strings (map (fn [[arg spec]]
                              (if (= :append-string spec)
                                (if (or (string? arg)
                                        (keyword? arg))
                                  [(cons (count (name arg)) (map int (name arg))) spec]
                                  (throw (IllegalArgumentException.
                                          (str "The following param: " arg " passed to ugen " (:name ugen) " should either be a string or a keyword" ))))
                                [arg spec]))
                            to-append)
        to-append-args (map first intify-strings)
        args           (flatten (concat normal-args to-append-args))
        ugen           (assoc ugen :args args)]
    (if-let [n-outs-arg (first (filter #(= :append-sequence-set-num-outs (second %))
                                       to-append))]
      (assoc ugen :n-outputs (count (flatten [(first n-outs-arg)])))
      ugen)))

(defn add-default-args [spec ugen]
  (let [args (:args ugen)
        arg-names (map #(keyword (:name %)) (:args spec))
        default-map (zipmap arg-names
                            (map :default (:args spec)))]
    (assoc ugen :args (arg-lister args arg-names default-map))))

(defn- with-num-outs-mode [spec ugen]
  (let [args-specs (args-with-specs (:args ugen) spec :mode)
        [args n-outs] (reduce (fn [[args n-outs] [arg mode]]
                                (if (= :num-outs mode)
                                  [args arg]
                                  [(conj args arg) n-outs]))
                              [[] (:n-outputs ugen)]
                              args-specs)]
    (assoc ugen
      :n-outputs n-outs
      :args args)))

(defn- with-floated-args [spec ugen]
  (assoc ugen :args (floatify (:args ugen))))

(def UGEN-RATE-SPEED {:ir 0
                      :dr 1
                      :kr 2
                      :ar 3})

(declare ugen?)

(defn- op-rate
  "Lookup the rate of an input ugen, otherwise use IR because the operand
  must be a constant float."
  [arg]
  (if (ugen? arg)
    (:rate arg)
    (get RATES :ir)))

(defn- ugen-arg-rates [ugen]
  (map REVERSE-RATES (map :rate (filter ugen? (:args ugen)))))

(defn real-ugen-name
  [ugen]
  (overtone-ugen-name
    (case (:name ugen)
      "UnaryOpUGen"
      (get REVERSE-UNARY-OPS (:special ugen))

      "BinaryOpUGen"
      (get REVERSE-BINARY-OPS (:special ugen))

      (:name ugen))))

(defn- check-arg-rates [spec ugen]
  (let [cur-rate (REVERSE-RATES (:rate ugen))
        ugen-args (filter ugen? (:args ugen))]
    (when-let [bad-input (some
                        (fn [ug]
                          (if (< (UGEN-RATE-SPEED cur-rate)
                                 (UGEN-RATE-SPEED (get REVERSE-RATES (:rate ug))))
                            ug false))
                        ugen-args)]
      ;;special cases
      (when-not (or
                 ;; Special case the a2k ugen
                 (and (= "A2K" (:name ugen))
                      (= :ar (:rate-name bad-input)))
                 ;; Special case the FFT ugen which may have ar ugens plugged into it
                 (and (= "FFT" (:name ugen))
                      (= :ar (:rate-name bad-input)))
                 ;; Special case demand rate ugens which may have kr ugens plugged into them
                 (and (= :dr cur-rate)
                      (= :kr (:rate-name bad-input))))

        (let [ugen-name (real-ugen-name ugen)
              in-name (real-ugen-name bad-input)
              cur-rate-name (get HUMAN-RATES cur-rate)
              in-rate-name (get HUMAN-RATES (:rate-name bad-input))]
          (throw (Exception.
                  (format "Invalid ugen rate.  The %s ugen is %s rate, but it has a %s input ugen running at the faster %s rate.  Besides the a2k ugen and demand rate ugens (which are allowed kr inputs), all ugens must be the same speed or faster than their inputs."
                          ugen-name cur-rate-name
                          in-name in-rate-name))))))
    ;;simply return the ugen if there's no problem with rates
    ugen))

(defn- auto-rate-setter [spec ugen]
  (if (= :auto (:rate ugen))
    (let [arg-rates (ugen-arg-rates ugen)
          fastest-rate (first (reverse (sort-by UGEN-RATE-SPEED arg-rates)))
          new-rate (get RATES (or fastest-rate :ir))]
      (assoc ugen :rate new-rate :rate-name (REVERSE-RATES new-rate)))
    ugen))

(defn- buffer->id
  "Returns a function that converts any buffer arguments to their :id property
  value."
  [ugen]
  (update-in ugen [:args]
             (fn [args]
               (map #(if (buffer? %) (:id %) %) args))))

(defn- bus->id
  "Returns a function that converts any bus arguments to their :id property
  value."
  [ugen]
  (update-in ugen [:args]
             (fn [args]
               (map #(if (bus? %) (:id %) %) args))))

(defn- with-ugen-metadata-init
  "Calls init fn fun. If init fn returns a map, merges it with the ugen
  otherwise if the result is a new arg list and simply assocs it to the ugen
  under the key :args, else throws an exception."
  [spec fun ugen]
  (let [rate (:rate ugen)
        args (:args ugen)
        new-args (fun rate args spec)]
    (cond
     (associative? new-args) (merge ugen new-args)
     (sequential? new-args) (assoc ugen :args new-args)
     :else (throw (Exception. (str "Unexpected return type from a ugen metadata init fn. Expected either a map or a list, got " new-args))))))

(defn- placebo-ugen-init-fn
  "The default ugen init fn (used if an :init key is not present in the ugen
  metadata). Simply returns the args unchanged."
  [rate args spec] args)

(defn- with-init-fn
  "Creates the final argument initialization function which is applied to
  arguments at runtime to do things like re-ordering and automatic filling in
  of arguments. Typically appending input arrays as the last argument and
  filling in the number of in or out channels for those ugens that need it.

  If an init function is already present it will get called after doing the
  mapping and mode transformations."
  [spec]
  (let [defaulter    (partial add-default-args spec)
        mapper       (partial map-ugen-args spec)
        init-fn      (if (contains? spec :init)
                       (:init spec)
                       placebo-ugen-init-fn)
        initer       (partial with-ugen-metadata-init spec init-fn)
        n-outputer   (partial with-num-outs-mode spec)
        floater      (partial with-floated-args spec)
        appender     (partial append-seq-args spec)
        auto-rater   (partial auto-rate-setter spec)
        rate-checker (partial check-arg-rates spec)]

    (assoc spec :init

           (fn [ugen]
             (-> ugen
                 defaulter
                 mapper
                 initer
                 n-outputer
                 floater
                 buffer->id
                 bus->id
                 appender
                 auto-rater
                 rate-checker)))))

(defn- decorate-ugen-spec
  "Interpret a ugen-spec and add in additional, computed meta-data."
  [spec]
  (-> spec
      (with-rates)
      (with-categories)
      (with-expands)
      (with-init-fn)
      (with-default-rate)
      (with-fn-names)
      (doc/with-arg-defaults)
      (doc/with-full-doc)))

(defn- specs-from-namespaces
  "Gathers all ugen spec metadata (stored in the vars spec and specs-collide)
  from the specified namespaces into a single vector of maps.

  Takes a seq of namespace endings (see UGEN-NAMESPACES) and returns a vector
  of maps containing ugen metadata."
  [namespaces]
  (reduce (fn [mem ns]
            (let [full-ns (symbol (str "overtone.sc.ugen." ns))
                  _ (require [full-ns :only '[specs specs-collide]])
                  specs (var-get (ns-resolve full-ns 'specs))]

              ;; TODO: Currently colliders must be loaded before specs in order
              ;; for this to run properly, because some ugens in specs derive
              ;; from the 'index' ugen in colliders.  Maybe the derivation
              ;; process should get smarter...
              (if-let [colliders (ns-resolve full-ns 'specs-collide)]
                (concat mem (var-get colliders) specs)
                (concat mem specs))))
          []
          namespaces))

(defn- load-ugen-specs [namespaces]
  "Perform the derivations and setup defaults for rates, names
  argument initialization functions, and channel expansion flags."
  (let [specs   (specs-from-namespaces namespaces)
        derived (derive-ugen-specs specs)]
    (map decorate-ugen-spec derived)))

(def UGEN-NAMESPACES
  '[basicops buf-io compander delay envgen fft2 fft-unpacking grain
    io machine-listening misc osc beq-suite chaos control demand
    ff-osc fft info noise pan trig line input filter random mda stk])




(def UGEN-SPECS (let [specs (load-ugen-specs UGEN-NAMESPACES)]
                  (zipmap
                   (map #(normalize-ugen-name (:name %)) specs)
                    specs)))

(defn get-ugen [word]
  (get UGEN-SPECS (normalize-ugen-name word)))

(defn find-ugen [regexp]
  (map #(second %)
       (filter (fn [[k v]] (re-find (re-pattern (normalize-ugen-name regexp))
                                   (str k)))
               (vals UGEN-SPECS))))

(defn inf!
  "users use this to tag infinite sequences for use as
   expanded arguments. needed or else multichannel
   expansion will blow up"
  [sq]
  (with-meta sq
    (merge (meta sq) {:infinite-sequence true})))

(defn- inf? [obj]
  (:infinite-sequence (meta obj)))

;; Does it really make sense to cycle over the values of a collection when
;; doing expansion?  I don't think maps should be allowed as arguments, unless
;; this is a strategy at having named arguments, but then the ordering wouldn't
;; make sense.
;;(defn- cycle-vals [coll]
;;  (cycle (if (map? coll) (vals coll) coll)))

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

(defrecord UGen [id name rate rate-name special args n-outputs])
(derive UGen ::ugen)

(defrecord ControlProxy [name value rate rate-name])
(derive ControlProxy ::ugen)

(defn control-proxy
  "Create a new control proxy with the specified name, value and rate. Rate
  defaults to :kr. Specifically handles :tr which is really a TrigControl
  ugen at :kr."
  ([name value] (control-proxy name value :kr))
  ([name value rate]
     (ControlProxy. name value (if (= :tr)
                                 (:kr RATES)
                                 (rate RATES)) rate)))

(defrecord UGenOutputProxy [ugen rate rate-name index])
(derive UGenOutputProxy ::ugen)

(defn output-proxy [ugen index]
  (UGenOutputProxy. ugen (:rate ugen) (REVERSE-RATES (:rate ugen)) index))

(def *ugens* nil)
(def *constants* nil)

(defn ugen [spec rate special args]
  ;;(check-ugen-args spec rate special args)
  (let [rate (or (get RATES rate) rate)
        ug (UGen.
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

(defn ugen? [obj] (isa? (type obj) ::ugen))
(defn control-proxy? [obj] (= ControlProxy (type obj)))
(defn output-proxy? [obj] (= UGenOutputProxy (type obj)))

(defn- ugen-base-fn [spec rate special]
  (fn [& args]
    (ugen spec rate special args)))

(defn- make-ugen-fn
  "Returns a function representing the given ugen that will fill in default
  arguments, rates, etc."
  [spec rate special]
  (let [expand-flags (map #(:expands? %) (:args spec))
        ugen-fn (make-expanding (ugen-base-fn spec rate special) expand-flags)]
    (callable-map {:name (overtone-ugen-name (:name spec))
                   :doc (:doc spec)
                   :full-doc (:full-doc spec)
                   :categories (:categories spec)
                   :rate rate
                   :src "Implemented in C code"
                   :type :ugen
                   :params (:args spec)}
                  ugen-fn)))

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
    {:type :ugen}))

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
                           (if (some #(or (ugen? %) (not (number? %))) args)
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
                                         (make-ugen-fn spec rate special)])
                      (:fn-names spec))]
    (doseq [[ugen-name ugen-fn] ugen-fns]
      (intern to-ns ugen-name ugen-fn))))

(defn- def-unary-op
  [to-ns op-name special]
  (let [spec (get UGEN-SPECS "unaryopugen")
        ugen-name (symbol (overtone-ugen-name op-name))
        ugen-name (with-meta ugen-name {:doc (:full-doc spec)})
        ugen-fn (fn [arg]
                  (ugen spec (op-rate arg) special (list arg)))
        ugen-fn (make-expanding ugen-fn [true])]
    (if (ns-resolve to-ns ugen-name)
      (overload-ugen-op to-ns ugen-name ugen-fn)
      (intern to-ns ugen-name ugen-fn))))

(defn- def-binary-op
  [to-ns op-name special]
  (let [spec (get UGEN-SPECS "binaryopugen")
        ugen-name (symbol (overtone-ugen-name op-name))
        ugen-name (with-meta ugen-name {:doc (:full-doc spec)})
        ugen-fn (fn [a b]
                  (ugen spec (max (op-rate a) (op-rate b)) special (list a b)))
        ugen-fn (make-expanding ugen-fn [true true])]
    (if (ns-resolve to-ns ugen-name)
      (overload-ugen-op to-ns ugen-name ugen-fn)
      (intern to-ns ugen-name ugen-fn))))

;; We define this uniquely because it has to be smart about its rate.
;; TODO: I think this should probably be handled by one of the ugen modes
;; that is currently not yet implemented...
(def mul-add
  (make-expanding
   (fn [in mul add]
     (ugen {:name "MulAdd",
            :args [{:name "in"}
                   {:name "mul", :default 1.0}
                   {:name "add", :default 0.0}]
            :doc "Multiply and add, equivalent to (+ add (* mul in))"}
           (op-rate in) 0 (list in mul add)))
   [true true true]))

(load "ugen/generic_ops")

(defn intern-ugens
  "Iterate over all UGen meta-data, generate the corresponding functions and
  intern them in the current or otherwise specified namespace."
  [& [to-ns]]
  (let [to-ns (or to-ns *ns*)]
    (doseq [ugen (filter #(not (or (= "UnaryOpUGen" (:name %))
                                   (= "BinaryOpUGen" (:name %))))
                         (vals UGEN-SPECS))]
      (def-ugen to-ns ugen 0))
    (doseq [[op-name special] UNARY-OPS]
      (def-unary-op to-ns op-name special))
    (doseq [[op-name special] BINARY-OPS]
      (def-binary-op to-ns op-name special))
    ;;(intern to-ns 'mul-add (make-expanding mul-add [true true true]))
    ;;(refer 'overtone.sc.ugen.extra)
    ))

(defn intern-ugens-collide
  "Intern the ugens that collide with built-in clojure functions."
  [& [to-ns]]
  (let [to-ns (or to-ns *ns*)]
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

(load "ugen/extra")

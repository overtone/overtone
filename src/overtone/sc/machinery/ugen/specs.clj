(ns
    ^{:doc "Read and decorate ugen metadata to create final UGEN-SPECS"
      :author "Jeff Rose"}
  overtone.sc.machinery.ugen.specs
  (:use [clojure.pprint]
        [overtone.helpers lib]
        [overtone.sc.machinery.ugen defaults common special-ops categories sc-ugen])
  (:require [overtone.sc.machinery.ugen.doc :as doc]))

(def UGEN-NAMESPACES
  '[basicops buf-io compander delay envgen fft2 fft-unpacking grain
    io machine-listening misc osc beq-suite chaos control demand
    ff-osc fft info noise pan trig line input filter random

    extras.mda
    extras.stk
    extras.glitch
    extras.bhob
    extras.blackrain
    extras.distortion
    extras.sl
    extras.ay
    extras.bbcut2u
    extras.bat
    extras.vosim
    extras.berlach
    extras.membrane
    ])

(defn- specs-from-namespaces
  "Gathers all ugen spec metadata (stored in the vars spec and specs-collide)
  from the specified namespaces into a single vector of maps.

  Takes a seq of namespace endings (see UGEN-NAMESPACES) and returns a vector
  of maps containing ugen metadata."
  [namespaces]
  (reduce (fn [mem ns]
            (let [full-ns (symbol (str "overtone.sc.machinery.ugen.metadata." ns))
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

(defn- args-with-specs
  "Creates a list of (arg-value, arg-spec-item) pairs."
  [args spec property]
  {:pre [(keyword? property)]}
  (partition 2 (interleave args (map property (:args spec)))))

(defn- ugen-arg-rates [ugen]
  (map REVERSE-RATES (map :rate (filter sc-ugen? (:args ugen)))))

(defn- auto-rate-setter [spec ugen]
  (if (= :auto (:rate ugen))
    (let [arg-rates (ugen-arg-rates ugen)
          fastest-rate (first (reverse (sort-by UGEN-RATE-SPEED arg-rates)))
          new-rate (get RATES (or fastest-rate :ir))]
      (assoc ugen :rate new-rate :rate-name (REVERSE-RATES new-rate)))
    ugen))

(defn- with-ugen-metadata-init
  "Calls init fn fun. Ifs init fn returns a map, merges it with the ugen
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

(defn- placebo-ugen-checker-fn
  "The default ugen checker n (used if a :check key is not present in the ugen
  metadata). Simply returns nil."
  [rate num-outs args spec] nil)

(defn- with-ugen-checker-fn
  "Calls the checker fn. If checker fn returns a string, throws an exception
  using the string as a message. Otherwise returns ugen unchanged. If the
  checker fn is a list, it will assume it's a list of fns and will call all of
  them. If any of the results are strings it will concatanate them to produce
  a list of errors separated with AND."
  [spec fun ugen]
  (let [rate (:rate ugen)
        args (:args ugen)
        num-outs (:n-outputs ugen)

        result (if (sequential? fun)
                 (let [results (map #(% rate num-outs args spec) fun)]
                   (if (some string? results)
                     (reduce (fn [s el]
                               (if (string? el)
                                 (if (empty? s)
                                   el
                                   (str s "\nAND\n" el))
                                 s))
                             ""
                             results)
                     nil))
                 (fun rate num-outs args spec))]

    (if (string? result)
      (throw (Exception. (str "Error in checker for ugen " (overtone-ugen-name (:name spec)) ":\n" result "\nUgen:\n" (with-out-str (pprint ugen)))))
      ugen)))

(defn- check-arg-rates [spec ugen]
  (let [cur-rate (REVERSE-RATES (:rate ugen))
        ugen-args (filter sc-ugen? (:args ugen))]
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
                      (= :kr (:rate-name bad-input)))
                 ;; Special case Amplitude ugen which may have ar ugens plugged into it
                 (and (= "Amplitude" (:name ugen))
                      (= :ar (:rate-name bad-input)))
                 ;; Special case Pitch ugen which may have ar ugens plugged into it
                 (and (= "Pitch" (:name ugen))
                      (= :ar (:rate-name bad-input))))

        (let [ugen-name     (real-ugen-name ugen)
              in-name       (real-ugen-name bad-input)
              cur-rate-name (get HUMAN-RATES cur-rate)
              in-rate-name  (get HUMAN-RATES (:rate-name bad-input))]
          (throw (Exception.
                  (format "Invalid ugen rate.  The %s ugen is %s rate, but it has a %s input ugen running at the faster %s rate.  Besides the a2k ugen and demand rate ugens (which are allowed kr inputs), all ugens must be the same speed or faster than their inputs."
                          ugen-name cur-rate-name
                          in-name in-rate-name))))))
    ;;simply return the ugen if there's no problem with rates
    ugen))

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

(defn add-default-args [spec ugen]
  (let [args (:args ugen)
        arg-names (map #(keyword (:name %)) (:args spec))
        default-map (zipmap arg-names
                            (map :default (:args spec)))]
    (assoc ugen :args (arg-lister args arg-names default-map))))

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

;;TODO check to see if this can be removed. Args can not take keywords as vals
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

(defn- with-floated-args [spec ugen]
  (assoc ugen :args (floatify (:args ugen))))

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

(defn- nil-arg-checker
  [ugen]
  (let [args (:args ugen)]
    (when (some nil? args)
      (throw (IllegalArgumentException. (str "Error - attempted to call the " (:name ugen) " ugen with one or more nil arguments. This usually happens when the ugen contains arguments without defaults which haven't been explicitly called. \nUgen:\n" (with-out-str (pprint args)))))))
  ugen)

(defn- sanity-checker-fn
  "Ensure all inputs are either a number or a gen. Return an error string if not"
  [rate num-outs inputs spec]
  (when (some #(and (not (number? %))
                    (not (sc-ugen? %)))
              inputs)
    (str "Error: after initialisation, not all inputs to this ugen were numbers or other ugens (inputs which are explicitly allowed to be other data types (i.e strings) will have been converted to numbers at this point): " (vec inputs))))

(defn associative->id
  "Returns a function that converts any non sc-ugen associative arguments that
  contain an :id key to the the value of that key or leaves the args untouched."
  [ugen]
  (update-in ugen [:args]
             (fn [args]
               (map #(if (and (not (sc-ugen? %))
                              (:id %))
                       (:id %)
                       %)
                    args))))

(defn- with-init-fn
  "Creates the final argument initialization function which is applied to
  arguments at runtime to do things like re-ordering and automatic filling in
  of arguments. Typically appending input arrays as the last argument and
  filling in the number of in or out channels for those ugens that need it.

  If an init function is already present it will get called after doing the
  mapping and mode transformations."
  [spec]
  (let [defaulter       (partial add-default-args spec)
        mapper          (partial map-ugen-args spec)
        init-fn         (if (contains? spec :init)
                          (:init spec)
                          placebo-ugen-init-fn)
        initer          (partial with-ugen-metadata-init spec init-fn)
        n-outputer      (partial with-num-outs-mode spec)
        floater         (partial with-floated-args spec)
        appender        (partial append-seq-args spec)
        auto-rater      (partial auto-rate-setter spec)
        rate-checker    (partial check-arg-rates spec)
        checker-fn      (if (contains? spec :check)
                          (:check spec)
                          placebo-ugen-checker-fn)
        bespoke-checker (partial with-ugen-checker-fn spec checker-fn)
        sanity-checker  (partial with-ugen-checker-fn spec sanity-checker-fn)]

    (assoc spec :init

           (fn [ugen]
             (-> ugen
                 defaulter
                 mapper
                 initer
                 n-outputer
                 floater
                 appender
                 auto-rater
                 nil-arg-checker
                 bespoke-checker
                 associative->id
                 rate-checker
                 sanity-checker)))))

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

(defn- load-ugen-specs [namespaces]
  "Perform the derivations and setup defaults for rates, names
  argument initialization functions, and channel expansion flags."
  (let [specs   (specs-from-namespaces namespaces)
        derived (derive-ugen-specs specs)]
    (map decorate-ugen-spec derived)))

(def UGEN-SPECS (let [specs (load-ugen-specs UGEN-NAMESPACES)]
                  (zipmap
                   (map #(normalize-ugen-name (:name %)) specs)
                   specs)))

(defn get-ugen [word]
  (get UGEN-SPECS (normalize-ugen-name word)))

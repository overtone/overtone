(ns
    ^{:doc "CGens, or Composite Generators are composite UGens and/or other CGens. These are purely an Overtone abstraction whereas UGens have matching counterparts on SuperCollider Server. CGens allow you to build and share re-usable sub-synth components which act like ugens."
      :author "Sam Aaron"}
  overtone.sc.cgen

  (:use [overtone.util lib]
        [overtone.sc.ugen core defaults doc]))

(defn parse-cgen-params
  "Parse a defcgen's param list throwing exceptions where it isn't well-formed
  Returns a list of maps containing at least the key :name"
  [params]
  (when-not (vector? params)
    (throw (IllegalArgumentException. (str "defcgen expected a vector of arguments, instead it found a " (class params) ", " params))))
  (loop [parsed []
         to-parse params]
    (if (empty? to-parse)
      parsed
      (if (not (symbol? (first to-parse)))
        (throw (IllegalArgumentException. (str "Expecting a symbol describing the cgen param name. Instead got a " (class (first to-parse)) ": " (first to-parse))))

        (if (map? (second to-parse))
          (recur (conj parsed (merge (second to-parse) {:name (keyword (name (first to-parse)))}))
                 (drop 2 to-parse))
          (recur (conj parsed {:name (keyword (name (first to-parse)))})
                 (drop 1 to-parse)))))))

(defn- parse-cgen-bodies
  "Parse each of the defcgen's body throwing exceptions when the form isn't well-formed.
  Returns the default rate and a map of rates to associated bodies"
  [bodies]
  (when (empty? bodies)
    (throw (IllegalArgumentException. (str "defcgen was expecting one or more bodies implementing the various rates this cgen will support i.e. (:ar (sin-osc 440))"))))
  (let [[rate bodies] (loop [default-rate nil
                             parsed       {}
                             to-parse     bodies]
                        (if (empty? to-parse)
                          [default-rate parsed]
                          (let [body (first to-parse)]
                            (if (= :default (first body))
                              (recur (second body) parsed (rest to-parse))
                              (if (= 2 (count body))
                                (if-not (or (= :ar (first body))
                                            (= :ir (first body))
                                            (= :kr (first body))
                                            (= :dr (first body)))
                                  (throw (IllegalArgumentException. (str "defcgen was expecting on of the following keywords as the first item in each cgen body form: :ar, :ir, :kr, :dr or :default. Found " (first body))))
                                  (recur default-rate (assoc parsed (first body) (second body)) (rest to-parse)))
                                (throw (IllegalArgumentException. (str "defcgen was expecting each of the bodies to have two elements - a rate and an implementation form"))))))))
        default-rate  (if rate
                        rate
                        (if (= 1 (count bodies))
                          (ffirst bodies)
                          (default-ugen-rate (keys bodies))))]
    (when-not (get bodies default-rate)
      (throw (IllegalArgumentException. (str "defcgen's default rate needs to have an implementation. Please supply an implementation for rate: " default-rate))))
    [default-rate bodies]))

(defn- cgen-form
  "Pull out various bits of cgen information from a defcgen form"
  [u-form]
  (let [summary                     (if (string? (first u-form))
                                      (first u-form)
                                      "Please add a summary!")
        u-form                      (if (string? (first u-form))
                                      (rest u-form)
                                      u-form)
        params                      (parse-cgen-params (first u-form))
        u-form                      (rest u-form)
        doc                         (if (string? (first u-form))
                                      (first u-form)
                                      "Please add some docs!")
        u-form                      (if (string? (first u-form))
                                      (rest u-form)
                                      u-form)
        [default-rate rated-bodies] (parse-cgen-bodies u-form)]

    [summary doc params rated-bodies default-rate]))

(defn- mk-cgen-fn
  "Make the function which gets executed when a cgen is called."
  [param-names defaults body]
  (let [args     (gensym 'args)
        bindings (reduce (fn [final param]
                           (conj final (symbol (name param)) `(get (arg-mapper ~args ~param-names ~defaults) ~param)))
                         []
                         param-names)]
    `(fn [& ~args]
       (let [~@bindings]
         (with-overloaded-ugens
           ~body)))))

(defn generate-full-cgen-doc
  "Generate a full docstring from a the specified cgen information"
  ([c-name summary doc categories rate params rates] (generate-full-cgen-doc c-name summary doc categories rate params rates nil nil))
  ([c-name summary doc categories rate params rates src-str contributor]
     (let [spec {:name         c-name
                 :summary      summary
                 :doc          doc
                 :categories   categories
                 :default-rate rate
                 :args         (map stringify-map-vals params)
                 :rates        (into #{} rates)
                 :src-str      src-str
                 :contributor  contributor}]
       (:full-doc (with-full-doc spec)))))

(defn mk-cgen
  "Generate the form represign a cgen - a callable map of associated information
  and the function that evaluates the body within the binding context of the
  params."
  ([c-name summary doc params body categories rate] (mk-cgen c-name summary doc params body categories rate #{rate}))
  ([c-name summary doc params body categories rate rates]
     (let [param-names (vec (map :name params))
           param-names (vec (map :name params))
           defaults    (reduce (fn [s el] (assoc s (:name el) (:default el)))
                               {}
                               params)
           full-doc    (generate-full-cgen-doc c-name summary doc categories rate params rates)
           cgen-fn     (mk-cgen-fn param-names defaults body)]
       `(callable-map {:params ~params
                       :summary ~summary
                       :doc ~doc
                       :full-doc ~full-doc
                       :categories ~categories
                       :rate ~rate
                       :name ~(name c-name)
                       :src (quote ~body)
                       :type ::cgen}
                      ~cgen-fn))))

(defmacro cgen
  "Create a cgen (composite generator) - a composition of ugens and/or other
  cgens used as a general purpose synth building block.
  A cgen behaves similarly to a ugen - it has a default rate, named params, full
  docstring and may be mixed with both ugens and cgens in the construction of
  synths."
  ([c-name summary doc params body rate] (cgen c-name doc params body rate [["Composite Ugen"]]))
  ([c-name summary doc params body rate categories]
     (let [c-name (symbol (name c-name))]
       (mk-cgen c-name summary doc params body categories rate))))

(defmacro defcgen
  "Define one or more related cgens (composite generators) with different rates.

  A cgen has a name, docstring and 0 or more params which themselves have names
  and optional info maps with the keys :default and :doc. Next you need to
  describe a number of rated bodies which may reference the named params.
  Finally, an optional :default may be specified which will define the default
  rate for this cgen, or if ommitted the standard rate precedence is used.

  An example cgen definition is as follows:

  (defcgen pm-osc
    \"phase modulation sine oscillator pair.\"
    [car-freq {:default 0.0 :doc \"Carrier frequency\"}
     mod-freq {:default 0.0 :doc \"Modulation frequency\"}
     pm-index {:default 0.0 :doc \"Phase modulation index\"}
     mod-phase {:default 0.0 :doc \"Modulation phase\"}]
    \"Longer more detailed documentation...\"
    (:ar (sin-osc:ar car-freq (* pm-index (sin-osc:ar mod-freq mod-phase))))
    (:kr (sin-osc:kr car-freq (* pm-index (sin-osc:kr mod-freq mod-phase))))
    (:default :ar))"
  [c-name & c-form]
  (let [[summary doc params bodies default-rate] (cgen-form c-form)
        arglists                                 (vec (map #(symbol (name (:name %))) params))
        arglists                                 (list 'quote arglists)
        rates                                    (into #{} (keys bodies))
        categories                               [["Composite Ugen"]]
        full-doc                                 (generate-full-cgen-doc c-name summary doc categories default-rate params rates)
        metadata                                 {:doc full-doc
                                                  :arglists arglists
                                                  :type ::cgen}
        default-body                             (get bodies default-rate)
        default-cgen                             (mk-cgen c-name summary doc params default-body categories default-rate)
        default-c-name                           (with-meta c-name metadata)
        cgen-defs                                [`(def ~default-c-name ~default-cgen)]]

    (conj cgen-defs
     (for [rate rates]
       (let [body   (get bodies rate)
             cgen   (mk-cgen c-name summary doc params body categories rate)
             c-name (symbol (str (name default-c-name) rate))
             c-name (with-meta c-name metadata)]
         `(def ~c-name ~cgen))))))

(defmethod print-method ::cgen [cgen w]
  (let [info (meta cgen)]
    (.write w (format "#<cgen: %s>" (:name info)))))

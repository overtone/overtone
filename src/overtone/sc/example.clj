(ns
    ^{:doc "Examples are stored cgens which serve as explorable documentation. Users may search and query the available examples whilst also being able to easily demo them to immediately hear what they do."
      :author "Sam Aaron"}
  overtone.sc.example

  (:use [overtone.util]
        [overtone.sc ugen]
        [overtone.sc.ugen defaults doc]
        [overtone.sc.cgen]))

(def examples* (atom {}))

(defn parse-example-form
  "Parse example form - pull out elements of form and return as list. Reads body-str to convert
  from nasty string to beautiful Clojure form. (The string is used to preserve formatting)."
  [form]
  (let [[name short-doc long-doc rate-sym rate params body-str contributor-sym contributor] form
        body (read-string body-str)
        long-doc (str short-doc "\n\n" long-doc)]
    [name short-doc long-doc params body-str body rate contributor]))

(defn mk-swap-form
  "Create form required to append a new cgen to the examples* map."
  [gen-name form]
  (let [[example-key short-doc long-doc params body-str body rate contributor] (parse-example-form form)
        categories [["Example cgen"]]
        params (parse-cgen-params params)
        cgen-name (str (name gen-name) ":" (name example-key))
        cgen-fn (mk-cgen cgen-name long-doc params body categories rate)
        full-doc `(generate-full-cgen-doc ~long-doc ~categories ~rate ~params #{~rate} ~body-str ~contributor)
        ]
    `(swap! examples* assoc-in
            [~(keyword (name gen-name))
             ~(keyword (name example-key))]
            (callable-map {:params ~params
                           :doc ~short-doc
                           :full-doc ~full-doc
                           :long-doc ~long-doc
                           :categories ~categories
                           :rate ~rate
                           :name ~cgen-name
                           :src (quote ~body)
                           :src-str ~body-str
                           :contributer ~contributor
                           :type :example}
                          ~cgen-fn))))

(defmacro defexample
  "Define a set of examples for a specific ugen or cgen.

  Each example requires you to specify:
     * specify keyword name
     * abbriviated docstring
     * full description
     * rate
     * params (with optional default/doc map)
     * form as a string (to preserve formatting)
     * contributor

 The following is an example set of sin-osc with two examples:

 (defexample sin-osc
  (:basic-tone
   \"Basic sine wave tone\"
   \"Here we simply trigger off a sine-wave ugen to create us a basic tone.
     Sine waves oscillate in and out like ripples on a pond and create a
     pure sounding tone.\"
   rate :ar
   [freq {:default 440 :doc \"The frequncy of the sine wave. Increase to hear a higher pitch\"}]
   \"
   (sin-osc freq)\"
   contributor \"Your Name\")

  (:second-example
   .
   .
   .)

"
  [gen-name & example-form]
  (let [swaps (map #(mk-swap-form gen-name %) example-form)
        added-keywd (keyword (str gen-name "-example-added"))]
    `(do ~@swaps
         ~added-keywd)))

(defn- resolve-gen-name
  "If the gen is a cgen or ugen returns the :name otherwise returns name
   unchanged"
  [gen]
  (if (and (associative? gen)
                          (or (= :ugen (:type gen))
                              (= :cgen (:type gen))))
    (keyword (:name gen))
    gen))

(defn example
  "Fetch and call specific example for gen with key
  This can then be passed to demo:
  (demo (example impulse :create-trig))

  Also, params can be passed by appending them to the end of the args:
  (demo (example foo :key arg1 arg2 :key1 arg3 :key2 arg4))"
  [gen key & params]
  (let [examples @examples*
        gen-name (resolve-gen-name gen)]
    (apply (get-in examples [gen-name key]) params)))

(defn get-example
  "Fetch specific example for gen with key. This is useful for storing an
  example for later use. Returns a cgen."
  [gen key]
  (let [examples @examples*
        gen-name (resolve-gen-name gen)]
    (get-in examples [gen-name key])))

(defn examples
  "List examples for a specific gen"
  ([gen]
     (let [all-examples @examples*
           gen-name (resolve-gen-name gen)
           examples (get all-examples gen-name)]
       (dorun
        (for [key (keys examples)]
          (println (str key " (" (:rate (get examples key)) ")  - " (:doc (get examples key))))))))
  ([gen key]
     (let [examples @examples*
           gen-name (resolve-gen-name gen)
           example (get-in examples [gen-name key])]
       (println (:full-doc example)))))

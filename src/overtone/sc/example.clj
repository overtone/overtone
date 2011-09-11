(ns
    ^{:doc "Examples are stored cgens which serve as explorable documentation. Users may search and query the available examples whilst also being able to easily demo them to immediately hear what they do."
      :author "Sam Aaron"}
  overtone.sc.example
  (:use [overtone.util lib doc]
        [overtone.sc.ugen core defaults doc]
        [overtone.sc.cgen]))

(defonce examples* (atom {}))

(defn parse-example-form
  "Parse example form - pull out elements of form and return as list. Reads body-str to convert
  from nasty string to beautiful Clojure form. (The string is used to preserve formatting)."
  [form]
  (let [[name summary long-doc rate-sym rate params body-str contributor-sym contributor] form
        body (read-string body-str)]
    [name summary long-doc params body-str body rate contributor]))

(defn mk-swap-form
  "Create form required to append a new cgen to the examples* map."
  [gen-name form]
  (let [[example-key summary long-doc params body-str body rate contributor] (parse-example-form form)
        categories [["Example cgen"]]
        params (parse-cgen-params params)
        cgen-name (str (name gen-name) ":" (name example-key))
        cgen-desc (str (name gen-name) " " example-key)
        cgen-fn (mk-cgen cgen-name summary long-doc params body categories rate)
        full-doc `(str "-------------------------\nExample => " ~cgen-desc "\n"(generate-full-cgen-doc (:name ~gen-name) ~summary ~long-doc ~categories ~rate ~params #{~rate} ~body-str ~contributor))
        ]
    `(swap! examples* assoc-in
            [~(keyword (name gen-name))
             ~(keyword (name example-key))]
            (callable-map {:params ~params
                           :summary ~summary
                           :doc ~long-doc
                           :full-doc ~full-doc
                           :categories ~categories
                           :rate ~rate
                           :name ~cgen-name
                           :src (quote ~body)
                           :src-str ~body-str
                           :contributer ~contributor
                           :type :example}
                          ~cgen-fn))))

(defmacro defexamples
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

(defn example
  "Fetch and call specific example for gen with key
  This can then be passed to demo:
  (demo (example dibrown :rand-walk))

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

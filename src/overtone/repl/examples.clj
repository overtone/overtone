(ns overtone.repl.examples
  (:use [overtone.util doc lib]
        [overtone.sc.machinery defexample]))

(defn- print-gen-examples
  ([gen-examples] (print-gen-examples gen-examples "" 0))
  ([gen-examples indent-str desc-indent-len]
     (if (empty? gen-examples)
       (println "Sorry, no examples for this generator have been contributed.\n Please consider submitting one.")
       (dorun
        (for [orig-key (keys gen-examples)]
          (let [key             (str indent-str orig-key)
                key-len         (.length key)
                desc-indent-len (+ desc-indent-len (.length indent-str))
                key             (if (< key-len desc-indent-len)
                                  (gen-padding key (- desc-indent-len key-len) " ")
                                  key)
                full-key        (str key " (" (:rate (get gen-examples orig-key)) ") - ")
                full-key-len    (.length full-key)
                indented-desc   (indented-str-block (:summary (get gen-examples orig-key)) DOC-WIDTH full-key-len)]
            (println (str full-key indented-desc))))))))

(defn- longest-example-key
  [examples]
  (let [example-keys (flatten (map (fn [[k v]] (keys v)) examples))]
    (length-of-longest-string example-keys)))

(defn- longest-gen-example-key
  [gen-examples]
  (let [example-keys (keys gen-examples)]
    (if example-keys
      (length-of-longest-string example-keys)
      0)))

(defn examples
  "Print out examples for a specific gen. If passed a gen and a key will list
  the full example documentation. If passed no arguments will list out all
  available examples.
  (examples)          ;=> print out all examples
  (examples foo)      ;=> print out examples for gen foo
  (examples foo :bar) ;=> print out doc for example :bar for gen foo"
  ([]
     (let [all-examples    @examples*
           longest-key-len (inc (longest-example-key all-examples))]
       (dorun
        (for [[gen-name examples] all-examples]
          (do
            (println (name gen-name))
            (print-gen-examples examples "  " longest-key-len)
            (println ""))))))
  ([gen]
     (let [all-examples @examples*
           gen-name (resolve-gen-name gen)
           examples (get all-examples gen-name)
           longest-key-len (inc (longest-gen-example-key examples))]
       (print-gen-examples examples "" longest-key-len)))
  ([gen key]
     (let [examples @examples*
           gen-name (resolve-gen-name gen)
           example (get-in examples [gen-name key])]
       (if example
         (println (:full-doc example))
         (println "Sorry, no example could be found for the" (name gen-name) "gen with key" key)))))

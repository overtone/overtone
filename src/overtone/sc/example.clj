(ns overtone.sc.example
  (:use [overtone.sc.machinery defexample]
        [overtone.util lib]))

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

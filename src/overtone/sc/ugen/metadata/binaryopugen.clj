(ns overtone.sc.ugen.metadata.binaryopugen
  (:use [overtone.util lib]))

(def unnormalized-binaryopugen-docspecs
  {"mod" {:summary "Modulo function"
          :doc "Outputs a modulo b. The modulo is the remainder after dividing a by b.

i.e. (modulo 5 2) ;=> 1, (modulo 5 3) ;=> 2, (modulo 5 1) ;=> 0, (modulo 1 100) ;=> 1, (modulo 150 99) ;=> 51"}

   "min" {:summary "Minimum of two inputs"
          :doc "Outputs the smallest value of the two inputs a and b"}

   })

(def binaryopugen-docspecs
  (into {} (map
            (fn [[k v]] [(normalize-ugen-name k) v])
            unnormalized-binaryopugen-docspecs)))

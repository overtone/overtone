(ns overtone.sc.ugen.metadata.binaryopugen
  (:use [overtone.util lib]))

(def unnormalized-binaryopugen-docspecs

  {"+" {:summary "Addition summing"
        :doc "Merges two signals by adding them together."}

   "-" {:summary "Signal subtraction"
        :doc "Merges two signals by subtracting the second from the first"}

   "*" {:summary "Signal multiplication"
        :doc "Merges two signals by multiplying them together."}

   "/" {:summary "Signal division"
        :doc "Merges to signals by dividing the first by the second. Note, division can be tricky with signals because of division by zero."}

   "mod" {:summary "Modulo function"
          :doc "Outputs a modulo b. The modulo is the remainder after dividing a by b.

i.e. (modulo 5 2) ;=> 1, (modulo 5 3) ;=> 2, (modulo 5 1) ;=> 0, (modulo 1 100) ;=> 1, (modulo 150 99) ;=> 51"}

   "=" {:summary "Signal comparison - equality"
        :doc "Compares the two input signals a and b. If they are equal, outputs 1, otherwise outputs 0."}

   "not=" {:summary "Signal comparison - inequality"
           :doc "Compares the two input signals a and b. If they are not equal, outputs 1, otherwise outputs 0."}

   "<" {:summary "Signal comparison - less than"
        :doc "Compares the two input signals a and b. If a is less than b outputs 1, otherwise outputs 0"}

   ">" {:summary "Signal comparison - greater than"
        :doc "Compares the two input signals a and b. If a is greater than b outputs 1, otherwise outputs 0"}

   "<=" {:summary "Signal comparison - less than or equal to"
         :doc "Compares the two input signals a and b. If a is less than or equal to b outputs 1, otherwise outputs 0"}

   ">=" {:summary "Signal comparison - greater than or equal to"
        :doc "Compares the two input signals a and b. If a is greater than or equal to b outputs 1, otherwise outputs 0"}

   "min" {:summary "Minimum of two inputs"
          :doc "Outputs the smallest value of the two inputs a and b"}

   "max" {:summary "Maximum of two inputs"
          :doc "Outputs the largest value of the two inputs a and b"}

   })

(def binaryopugen-docspecs
  (into {} (map
            (fn [[k v]] [(normalize-ugen-name k) v])
            unnormalized-binaryopugen-docspecs)))

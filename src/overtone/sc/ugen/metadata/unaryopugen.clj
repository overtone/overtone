(ns overtone.sc.ugen.metadata.unaryopugen)

(def unaryopugen-docspecs
  {"neg" {:summary "Signal inversion"
          :doc "Invert input signal a by changing its sign. i.e. 1 -> -1 and -0.5 -> 0.5"}

   "abs" {:summary "Absolute value"
          :doc "Outputs the value of input a without regard to its sign. i.e 1 -> 1 and -0.5 0.5"}

   "ceil" {:summary "Next higher integer"
           :doc "Rounds input a up the next highest integer. i.e. 5.1 -> 6 and 7.0 -> 7"}

   "floor" {:summary "Previous lower integer"
            :doc "Rounds input a down to the previous lower integer. i.e. 5.9 -> 5 and 7.0 -> 7"}

   "frac" {:summary "Fractional part of signal"
           :doc "Outputs the fractional part between input a and the next higher integer. This means that negative input vals may result in a different output than positive input vals. Output is always positive. i.e. 3.5 -> 0.5, 34.2 -> 0.2, -5.2 -> 0.8"}

   "sign" {:summary "Sign of signal"
           :doc "Outputs -1 when a < 0, +1 when a > 0, 0 when a is 0."}

   "squared" {:summary "Squared value"
              :doc "Outputs the square of input a. i.e. 5 -> 25 and -3 -> 9"}

   })

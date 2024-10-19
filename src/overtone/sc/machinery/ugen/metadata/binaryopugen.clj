(ns overtone.sc.machinery.ugen.metadata.binaryopugen
  (:use [overtone.helpers lib]))

(def unnormalized-binaryopugen-docspecs
  {"+"           {:summary "Signal summing"
                  :doc "Merges two signals by adding them together."}

   "-"           {:summary "Signal subtraction"
                  :doc "Merges two signals by subtracting the second
                       from the first"}

   "*"           {:summary "Signal multiplication"
                  :doc "Merges two signals by multiplying them together."}

   "/"           {:summary "Signal division"
                  :doc "Merges to signals by dividing the first by the
                        second. Note, division can be tricky with
                        signals because of division by zero." }

   "mod"         {:summary "Modulo function"
                  :doc "Outputs a modulo b. The modulo is the remainder
                        after dividing a by b.

                        i.e. (mod 5 2) ;=> 1, (mod 5 3) ;=> 2,
                        (mod 5 1) ;=> 0, (mod 1 100) ;=> 1,
                        (mod 150 99) ;=> 51"}

   "="           {:summary "Signal comparison - equality"
                  :doc "Compares the two input signals a and b. If they
                        are equal, outputs 1, otherwise outputs 0." }

   "not="        {:summary "Signal comparison - inequality"
                  :doc "Compares the two input signals a and b. If they
                        are not equal, outputs 1, otherwise outputs 0." }

   "<"           {:summary "Signal comparison - less than"
                  :doc "Compares the two input signals a and b. If a is
                        less than b outputs 1, otherwise outputs 0"}

   ">"           {:summary "Signal comparison - greater than"
                  :doc "Compares the two input signals a and b. If a is
                        greater than b outputs 1, otherwise outputs 0"}

   "<="          {:summary "Signal comparison - less than or equal to"
                  :doc "Compares the two input signals a and b. If a is
                        less than or equal to b outputs 1, otherwise
                        outputs 0"}

   ">="          {:summary "Signal comparison - greater than or equal to"
                  :doc "Compares the two input signals a and b. If a is
                        greater than or equal to b outputs 1, otherwise
                        outputs 0"}

   "min"         {:summary "Minimum of two inputs"
                  :doc "Outputs the smallest value of the two inputs a
                        and b"}

   "max"         {:summary "Maximum of two inputs"
                  :doc "Outputs the largest value of the two inputs a
                        and b"}

   "bit-and"     {:summary "Bitwise AND"
                  :doc "Performs a bitwise AND on the two input signals a and b.
                       Corresponds to bitAnd and the & operator in sclang."}

   "bit-or"      {:summary "Bitwise OR"
                  :doc "Performs a bitwise OR on the two input signals a and b.
                       Corresponds to bitOr and the | operator in sclang."}

   "bit-xor"     {:summary "Bitwise XOR"
                  :doc "Performs a bitwise XOR on the two input signals a and b.
                       Corresponds to bitXor in sclang."}

   "round"       {:summary "Rounding - nearest multiple"
                  :doc "Rounds a to the nearest (up or down) multiple of
                        b.

                       i.e. (round 2 10) => 0, (round 5 10) => 10
                       and (round 21 10) => 20"}

   "round-up"    {:summary "Rounding - next multiple"
                  :doc "Rounds a up to the next multiple of b above a.

                       i.e. (round-up 2 10) => 10, (round-up 21 10) => 30,
                       (round-up 0 10) => 0"}

   "round-down"  {:summary "Rounding - previous multiple"
                  :doc "Rounds a down to the previous multiple of b
                        below a. SC refers to this ugen as trunc.

                       i.e. (round-down 2 10) => 0, (round-down 20 10) => 20,
                       (round-down 0 10) => 0"}

   "atan2"       {:summary "Arctangent of fraction"
                  :doc "Returns the arctangent of a/b"}

   "hypot"       {:summary "Length of hypotenuse using Pythag"
                  :doc "Returns the square root of the sum of the
                        squares of a and b. Or equivalently, the
                        distance from the origin to the point (x, y)."}

   "hypot-aprox" {:summary "Approximation of hypotenuse length"
                  :doc "Returns an approximation of the square root of
                        the sum of the squares of x and y. The formula
                        used is :

                        abs(x) + abs(y) - ((sqrt(2) - 1) * min(abs(x), abs(y)))

                        This should not be used for simulating a doppler
                        shift because it is discontinuous. Use hypot."}

   "pow"         {:summary "exponentiation"
                  :doc "Returns a to the power of b

                       i.e. (pow 2 4) => 16, (pow 5 3) => 125"}

   "ring1"       {:summary "Ring modulation plus first source"
                  :doc "Return the value of ((a*b) + a). This is more
                        efficient than using separate unit generators
                        for the multiply and add." }

   "ring2"       {:summary "Ring modulation plus both sources"
                  :doc "Return the value of ((a*b) + a + b). This is
                        more efficient than using separate unit
                        generators for the multiply and adds." }

   "ring3"       {:summary "Ring modulation variant"
                  :doc "Return the value of (a * a * b). This is more
                        efficient than using separate unit generators
                        for each multiply." }

   "ring4"       {:summary "Ring modulation variant 2"
                  :doc "Return the value of
                        ((a * a * b) - (a * b * b)). This is more
                        efficient than using separate unit generators
                        for each operation." }

   "difsqr"      {:summary "Difference of squares"
                  :doc "Return the value of (a*a) - (b*b). This is more
                        efficient than using separate unit generators
                        for each operation." }

   "sumsqr"      {:summary "Sum of squares"
                  :doc "Return the value of (a*a) + (b*b). This is more
                        efficient than using separate unit generators
                        for each operation." }

   "sqrsum"      {:summary "Square of the sum"
                  :doc "Return the value of (a + b)^2. This is more
                        efficient than using separate unit generators
                        for each operation." }

   "sqrdif"      {:summary "Square of the difference"
                  :doc "Return the value of (a - b)**2. This is more
                        efficient than using separate unit generators
                        for each operation." }

   "absdif"      {:summary "Absolute value of the difference"
                  :doc "Return the value of abs(a - b). Finding the
                        magnitude of the difference of two values is a
                        common operation." }

   "thresh"      {:summary "Signal thresholding"
                  :doc "0 when a < b, otherwise a."}

   "amclip"      {:summary "Two quadrant multiply"
                  :doc "0  when  b <= 0,  a*b  when  b > 0"}

   "scale-neg"   {:summary "Scale negative part of input wave"
                  :doc "a*b when a < 0, otherwise a."}

   "clip2"       {:args [{:name "input"    :doc "Input signal"}
                         {:name "clip-val" :doc "Max/min val of input
                                                 signal before clipping
                                                 is applied"}]
                  :summary "Bilateral clipping"
                  :doc "clips input wave to +/- clip-val"}

   "excess"      {:summary "Clipping residual"
                  :doc "Returns the difference of the original signal
                        and its clipped form: (a - clip2(a,b))." }

   "fold2"       {:summary "Bilateral folding"
                  :doc "folds input wave a to +/- b"}

   "wrap2"       {:summary "Bilateral wrapping"
                  :doc "wraps input wave to +/- b"}})

(def binaryopugen-docspecs
  (into {} (map
            (fn [[k v]] [(normalize-ugen-name k) v])
            unnormalized-binaryopugen-docspecs)))

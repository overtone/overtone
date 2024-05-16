(ns overtone.helpers.rand)

(defn rand-in-range
  "Range rand, returns a random number between min (inclusive) and
  max (exclusive). Returns integers if both min and max are integer, floating
  point otherwise."
  [min max]
  (+ min ((if (and (int? min) (int? max))
            rand-int
            rand)
          (- max min))))

(defn rand-signed
  "Signed/symmetric rand, returns a value between -n and n (-1.0 / 1.0 when called
  without argument). Integer if n is integer, floating point otherwise."
  ([]
   (rand-signed 1.0))
  ([n]
   (rand-in-range (- n) n)))

(defn rand-gaussian
  "Random normally distributed number with mean 0.0 and standard deviation 1.0.
  Implements the Marsaglia polar method. Note that results are not constrained
  to the [-1.0 1.0] range, but can be arbitrarily far great or small outliers."
  []
  (let [a (rand-signed)
        b (rand-signed)
        s (+ (* a a) (* b b))]
    (if (or (= 0 s) (< 1.0 s))
      (recur)
      (* a (Math/sqrt (/ (* -2.0 (Math/log s)) s))))))

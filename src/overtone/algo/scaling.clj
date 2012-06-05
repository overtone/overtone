(ns
    ^{:doc "Handy number scaling and rounding fns"
      :author "Sam Aaron"}
  overtone.algo.scaling)

(defn closest-to
  "Returns either low or hi depending on which is numerically closer
  to n.
  (closest-to 4.7 4 6) ;=> 4 (4.7 is closer to 4 than 6"
  [n low hi]
  (let [low-diff  (- n low)
        hi-diff   (- hi n)]
    (if (< low-diff hi-diff)
      low
      hi)))

(defn round-to
  "Rounds n to the nearest multiple of div
  (round-to 4.7 1) ;=> 5
  (round-to 4.7 2) ;=> 4 (4.7 is closer to 4 than 6)"
  [n div]
  (let [n         (float n)
        div       (float div)
        remainder (rem n div)
        low       (- n remainder)
        hi        (+ low div)]
    (closest-to n low hi)))

(defn scale-range
  "Scales a given input value within the specified input range to a
  corresponding value in the specified output range using the formula:

           (out-max - out-min) (x - in-min)
   f (x) = --------------------------------  + out-min
                    in-max - in-min


  The result will be limited to the specified out range. So if the
  result of f(x) is greater than out-max it is reduced to out-max.

  in-min must be less than in-max and out-min must be less than
  out-max"

  [x in-min in-max out-min out-max]
  (letfn [(scale [x] (+ (/ (* (- out-max out-min) (- x in-min)) (- in-max in-min)) out-min))]
    (let [result (scale x)
          result (if (< result out-min) out-min result)
          result (if (> result out-max) out-max result)]
      result)))

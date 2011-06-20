(ns
    ^{:doc "Handy probability fns"
      :author "Sam Aaron"}
  overtone.helpers.chance
  (:use overtone.helpers.scaling))

(defn choose
  "Choose a random element from a collection."
  [col]
  (nth col (rand-int (count col))))

(defn weighted-coin
  "Returns true or false. Probability of true is weighted by n which should be
   within the range 0 - 1. n will be truncated to range 0 - 1 if it isn't"
  [n]
  (let [n (float n)
        n (if (< n 0) 0 n)
        n (if (> n 1) 1 n)]
    (< (rand) n)))


;(defn shuffle
;  "Shuffle a collection, returns a seq."
;  [coll]
;  (let [l (ArrayList. coll)]
;    (Collections/shuffle l)
                                        ;    (seq l)))
(defn ranged-rand
  "Returns a random value within the specified range"
  [min max]
  (scale-range (rand) 0 1 min max))

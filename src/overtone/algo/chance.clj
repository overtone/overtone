(ns overtone.algo.chance
  "Handy probability fns"
  {:author "Sam Aaron"}
  (:use
   [overtone.algo.scaling]))

(defn choose
  "Choose a random element from col."
  [col]
  (rand-nth col))

(defn choose-n
  "Choose n random elements from col."
  [n col]
  (take n (shuffle col)))

(defn weighted-choose
  "Returns an element from list vals based on the corresponding
  probabilities list. The length of vals and probabilities should be
  similar and the sum of all the probabilities should be 1. It is also
  possible to pass a map of val -> prob pairs as a param.

  The following will return one of the following vals with the
  corresponding probabilities:
  1 -> 50%
  2 -> 30%
  3 -> 12.5%
  4 -> 7.5%
  (weighted-choose [1 2 3 4] [0.5 0.3 0.125 0.075])
  (weighted-choose {1 0.5, 2 0.3, 3 0.125, 4 0.075})"
  ([val-prob-map] (weighted-choose (keys val-prob-map) (vals val-prob-map)))
  ([vals probabilities]
     (when-not (= (count vals) (count probabilities))
       (throw (IllegalArgumentException. (str "Size of vals and probabilities don't match. Got "
                               (count vals)
                               " and "
                               (count probabilities)))))
     (when-not (== (reduce + probabilities) 1.0)
       (throw (IllegalArgumentException. (str "The sum of your probabilities is not 1.0"))))

     (let [paired (map vector probabilities vals)
           sorted (sort #(< (first %1) (first %2)) paired)
           summed (loop [todo sorted
                         done []
                         cumulative 0]
                    (if (empty? todo)
                      done
                      (let [f-prob (ffirst todo)
                            f-val  (second (first todo))
                            cumulative (+ cumulative f-prob)]
                        (recur (rest todo)
                               (conj done [cumulative f-val])
                               cumulative))))
           rand-num (rand)]
       (loop [summed summed]
         (when (empty? summed)
           (throw (Exception. (str "Error, Reached end of weighed choice options"))))
         (if (< rand-num (ffirst summed))
           (second (first summed))
           (recur (rest summed)))))))

(defn weighted-coin
  "Returns true or false. Probability of true is weighted by n which
   should be within the range 0 - 1. n will be truncated to range 0 - 1
   if it isn't"
  [n]
  (let [n (float n)
        n (if (< n 0) 0 n)
        n (if (> n 1) 1 n)]
    (< (rand) n)))

(defn ranged-rand
  "Returns a random value within the specified range. Always returns
  floats (doubles)."
  [min max]
  (scale-range (rand) 0 1 min max))

(defn rrand
  "Range rand, returns a random number between min (inclusive) and
  max (exclusive). Returns integers if both min and max are integer, floats
  otherwise."
  [min max]
  (+ min ((if (and (int? min) (int? max))
            rand-int
            rand)
          (- max min))))

(defn srand
  "Signed/symmetric rand, returns a value between -n and n. Integer if n is
  integer."
  [n]
  (rrand (- n) n))

(defn chosen-from
  "Random infinite sequence chosen from the given collection"
  ([coll]
   (let [coll (vec coll)]
     (repeatedly #(rand-nth coll))))
  ([coll limit]
   (let [coll (vec coll)]
     (repeatedly limit #(rand-nth coll)))))

(defn only
  "Take only the specified notes from the given phrase."
  ([phrase notes] (only phrase notes []))
  ([phrase notes result]
   (if notes
     (recur phrase
            (next notes)
            (conj result (get phrase (first notes))))
     result)))

(defn sputter
  "Returns a list where some elements may have been repeated.

   Repetition is based on probabilty (defaulting to 0.25), therefore,
   for each element in the original list, there's a chance that it will
   be repeated. (The repetitions themselves are also subject to further
   repetiton). The size of the resulting list can be constrained to max
   elements (defaulting to 100).

  (sputter [1 2 3 4])        ;=> [1 1 2 3 3 4]
  (sputter [1 2 3 4] 0.7 5)  ;=> [1 1 1 2 3]
  (sputter [1 2 3 4] 0.8 10) ;=> [1 2 2 2 2 2 2 2 3 3]
  (sputter [1 2 3 4] 1 10)   ;=> [1 1 1 1 1 1 1 1 1 1]
  "
  ([list]          (sputter list 0.25))
  ([list prob]     (sputter list prob 100))
  ([list prob max] (sputter list prob max []))
  ([[head & tail] prob max result]
    (if (and head (< (count result) max))
      (if (< (rand) prob)
        (recur (cons head tail) prob max (conj result head))
        (recur tail prob max (conj result head)))
      result)))

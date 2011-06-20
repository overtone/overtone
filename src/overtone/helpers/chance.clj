(ns
    ^{:doc "Handy probability fns"
      :author "Sam Aaron"}
  overtone.helpers.chance
  (:use overtone.helpers.scaling))

(defn choose
  "Choose a random element from a collection."
  [col]
  (nth col (rand-int (count col))))

(defn weighted-choose
  "Returns an element from list vals based on the corresponding probabilities
  list. The length of vals and probabilities should be similar and the sum
  of all the probabilities should be 1. It is also possible to pass a map of
  val -> prob pairs as a param.

  The following will return one of the following vals with the corresponding
  probabilities:
  1 -> 50%
  2 -> 30%
  3 -> 12.5%
  4 -> 7.5%
  (weighted-choose [1 2 3 4] [0.5 0.3 0.125 0.075])
  (weighted-choose {1 0.5, 2 0.3, 3 0.125, 4 0.075"
  ([val-prob-map] (weighted-choose (keys val-prob-map) (vals (val-prob-map))))
  ([vals probabilities]
     (when-not (= (count vals) (count probabilities))
       (throw (Exception. (str "Size of vals and probabilities don't match. Got "
                               (count vals)
                               " and "
                               (count probabilities)))))
     (when-not (= (reduce + probabilities) 1)
       (throw (Exception. (str "The sum of your probabilities is not 1.0"))))

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

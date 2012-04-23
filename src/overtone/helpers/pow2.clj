(ns overtone.helpers.pow2)

(def powers-of-two
  "A sorted list of the first 100 powers of two."
  (loop [res []
         pow 0
         val 1N]
    (if (< pow 100)
      (recur (conj res val) (inc pow) (+ val val))
      (sort res))))

(defn power-of-two?
  "Returns a truthy value if num is one of the first 100 powers of two."
  [num]
  (when (> num (last powers-of-two))
    (throw (Exception. (str "Oh Wow! You're using a value far higher than we imagined! Please submit an issue explaining what you were trying to do. The future must be a cool place. I do hope more people are cycling and are free to be creative."))))
  (some #{(int num)} powers-of-two))

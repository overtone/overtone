(ns
    ^{:doc "Generate euclidean rhythm vectors with bjorklunds algorithm"
      :author "Mackenzie Starr and Colin Robinson"}
    overtone.algo.euclidean-rhythm)

(defn- build [level counts remainders]
  (let [pattern ((fn _build [pattern level]
            (case level
              -1 (conj pattern 0)
              -2 (conj pattern 1)
              (let [_pattern (reduce _build pattern (repeat (nth counts level 0) (- level 1)))]
                (if ((comp not zero?) (nth remainders level 0))
                  (_build _pattern (- level 2))
                  _pattern)))
                   ) [] level)
        first-hit (first (keep-indexed #(when (= %2 1) %1) pattern))]
    ;; shift pattern to get hits on first step (e.g. [1 0] vs [0 1])
    (vec (lazy-cat (drop first-hit pattern) (take first-hit pattern)))))

(defn euclidean-rhythm [pulses steps]
 "Generate euclidean rhythms using bjorklund's algorithm:
  The following will return the following sequences:

  (euclidean-rhythm 2 5)   ;; [1 0 1 0 0]

  (euclidean-rhythm 3 8)   ;; [1 0 0 1 0 0 1 0]

  algorithm description:
  https://ics-web.sns.ornl.gov/timing/Rep-Rate%20Tech%20Note.pdf

  musical applications:
  http://cgm.cs.mcgill.ca/~godfried/publications/banff.pdf
  "
  (if (> pulses steps)
    (throw (IllegalArgumentException. "the pulses cannot exceeded the steps"))
    (loop [counts  []
           remainders [pulses]
           divisor (- steps pulses)
           level 0]
      (if (not (> (remainders level) 1))
        (build level (conj counts divisor) remainders)
        (recur (conj counts (quot divisor (remainders level)))
               (conj remainders (mod divisor (remainders level)))
               (remainders level)
               (inc level))))))

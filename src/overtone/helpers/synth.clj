(ns
    ^{:doc "Utility functions for synth construction"
      :author "Sam Aaron"}
  overtone.helpers.synth)

(defn extract-target-pos-args
  [args default-target default-pos]
  (let [[args target] (if (or (= :target (first args))
                              (= :tgt    (first args)))
                        [(drop 2 args) (second args)]
                        [args default-target])
        [args pos]    (if (or (= :position (first args))
                              (= :pos      (first args)))
                        [(drop 2 args) (second args)]
                        [args default-pos])
        [args target] (if (or (= :target (first args))
                              (= :tgt    (first args)))
                        [(drop 2 args) (second args)]
                        [args target])]
    [target pos args]))

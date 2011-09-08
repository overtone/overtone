(ns
    ^{:doc "Audio position fns"
      :author "Jeff Rose"}
  overtone.algo.position)

(defn splay-pan
  "Given n channels and a center point, returns a position in a stereo field
  for each channel, evenly distributed from the center +- spread."
  [n center spread]
  (for [i (range n)]
    (+ center
       (* spread
          (- (* i
                (/ 2 (dec n)))
             1)))))

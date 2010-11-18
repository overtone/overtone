(defn mix
  "Mix down (sum) a set of input channels into a single channel."
  [& inputs]
  (reduce overtone.ugen-collide/+ inputs))

(defn square
  "a square wave generator."
  [freq]
  (pulse freq 0.5))

(defn- splay-pan
  "Given n channels and a center point, returns a position in a stereo field
  for each channel, evenly distributed from the center +- spread."
  [n center spread]
  (for [i (range n)]
    (+ center
       (* spread
          (- (* i
                (/ 2 (dec n)))
             1)))))

(defn splay
  "Spread input channels across a stereo field, with control over the center point
  and spread width of the target field, and level compensation that lowers the volume
  for each additional input channel."
  [in-array & {:as options}]
  (with-ugens
    (let [options (merge {:spread 1 :level 1 :center 0 :level-comp true} options)
          {:keys [spread level center level-comp]} options
           n (count in-array)
          level (if level-comp
                  (* level (Math/sqrt (/ 1 (dec n))))
                  level)
          positions (splay-pan n center spread)
          pans (pan2 in-array positions level)]
      (println "positions: " positions)
      (map + (parallel-seqs pans)))))


(ns
    ^{:doc "Handy trig fns"
      :author "Sam Aaron"}
  overtone.algo.trig)

(defn cosr
  "Returns a value at idx along a scaled cosine fn  with specified centre and range. The cosine cycles every period idxs. Similar to Impromptu's cosr.

  (cosr 0 10 2 8) ;=> 12
  (cosr 2 10 2 8) ;=> 10
  (cosr 4 10 2 8) ;=> 8
  (cosr 6 10 2 8) ;=> 10
  (cosr 8 10 2 8) ;=> 12"
  [idx centre range period]
  (+ centre (* range (Math/cos (* 2 Math/PI idx (/ 1 period))))))

(defn sinr
  "Returns a value at idx along a scaled sine fn with specified centre and range. The cosine cycles every period idxs. Similar to Impromptu's sinr.

  (sinr 0 10 2 8) ;=> 10
  (sinr 2 10 2 8) ;=> 12
  (sinr 4 10 2 8) ;=> 10
  (sinr 6 10 2 8) ;=> 8
  (sinr 8 10 2 8) ;=> 10"
  [idx centre range period]
  (+ centre (* range (Math/sin (* 2 Math/PI idx (/ 1 period))))))

(defn tanr
  "Returns a value at idx along a scaled tan fn with specified centre and range. The cosine cycles every period idxs. Similar to Impromptu's tanr.

  (tanr 0 10 2 8)        ;=> 10
  (tanr 1.999999 10 2 8) ;=> ~2546489
  (tanr 2 10 2 8)        ;=> ~3.2665
  (tanr 4 10 2 8)        ;=> 10
  (tanr 5.999999 10 2 8) ;=> ~2546489
  (tanr 6 10 2 8)        ;=> ~1.0886
  (tanr 8 10 2 8)        ;=> 10"
  [idx centre range period]
  (+ centre (* range (Math/tan (* 2 Math/PI idx (/ 1 period))))))

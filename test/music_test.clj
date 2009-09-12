(ns music-test
  (:use (overtone sc music)))

(def pitches [60 62 64 65 67 69 71 72])
(def dynamics [80 80 80 80 80 80 80 80])
(def rhythms [0.5 0.25 0.25 0.25 0.25 0.25 0.25 0.5])

(def dnb [["big-kick" [1 0 0 0 0 0 1 0 0 0 0 0 1 0 0 0]]
          ["noise-hat" [0 0 0 0 1 0 0 0 0 0 1 0 0 0 0 0]]])
(defn drum-and-bass []
  (clear-drums)
  (doseq [[voice pattern] dnb]
    (drum voice pattern))
  (play-drums (/ 60000 175 4) 16))

(defn techno []
  (clear-drums)
  (drum "soft-kick" [1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0])
  (drum "hat" [0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0])
  (play-drums (/ 60000 128 4) 16))

(defn hip-hop []
  (clear-drums)
  (drum "soft-kick" [1 0 0.5 0.25 0 0.6 0 0.9 0.9 0 1 0 0.5 0 0.3 0])
  (drum "hat" [0 0 0 0 1 0 0 0 0 0 0.3 0 1 0 0 0])
  (play-drums (/ 60000 85 4) 16))

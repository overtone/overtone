(ns overtone.music.pitch-test
  (:use overtone.music.pitch
        clojure.test))

(deftest invert-chord-works-properly
  (let [notes '(12 16 19 23)]
    (is (= (invert-chord notes 2) '(19 23 24 28)))
    (is (= (invert-chord notes -3) '(4 7 11 12)))))

(deftest chord-inversion-is-correct
  (is (= (chord :F3 :major 1) '(57 60 65)))
  (is (= (chord :F3 :major 2) '(60 65 69))))

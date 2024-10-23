(ns overtone.music.pitch-test
  (:use [overtone.music.pitch :as sut]
        clojure.test))

(deftest invert-chord-works-properly
  (let [notes '(12 16 19 23)]
    (is (= (invert-chord notes 2) '(19 23 24 28)))
    (is (= (invert-chord notes -3) '(4 7 11 12)))))

(deftest chord-inversion-is-correct
  (is (= (chord :F3 :major 1) '(57 60 65)))
  (is (= (chord :F3 :major 2) '(60 65 69))))

(deftest scale-test
  (is (= [60 62 64 65 67 69 71 72]
         (sut/scale :c4 :major)))
  (is (= [60 62 63 65 67 68 70 72]
         (sut/scale :c4 :minor)))
  (is (= [60 61 62 63 64 65 66 67 68 69 70 71 72]
         (sut/scale :c4 :chromatic)))
  (is (= [2 4 7 9 11 14]
         (sut/scale :d-1 :pentatonic))))

(ns overtone.music.pitch-test
  (:require [clojure.test :refer [deftest is testing]]
            [overtone.music.pitch :as sut]))

(deftest invert-chord-works-properly
  (let [notes '(12 16 19 23)]
    (is (= (sut/invert-chord notes 2) '(19 23 24 28)))
    (is (= (sut/invert-chord notes -3) '(4 7 11 12)))))

(deftest chord-inversion-is-correct
  (is (= (sut/chord :F3 :major 0) '(53 57 60)))
  (is (= (sut/chord :F3 :major 1) '(57 60 65)))
  (is (= (sut/chord :F3 :major 2) '(60 65 69))))

(deftest octave-note-test
  (testing "octave too low"
    (is (thrown? Exception (sut/octave-note -2 0))))
  (is (= 0 (sut/octave-note -1 0)))
  (is (= 12 (sut/octave-note 0 0)))
  (testing "interval too high"
    (is (thrown? Exception (sut/octave-note 0 12))))
  (is (= sut/MIDDLE-C (sut/octave-note 4 0)))
  (is (= 71 (sut/octave-note 4 11)))
  (is (= (sut/octave-note 5 0)
         (inc (sut/octave-note 4 11))))
  (is (= 127 (sut/octave-note 9 7)))
  (testing "highest octave has only 7 intervals"
    (is (thrown? Exception (sut/octave-note 9 8))))
  (testing "octave too high"
    (is (thrown? Exception (sut/octave-note 10 0)))))

(deftest note-info-test
  ;; https://en.wikipedia.org/wiki/Scientific_pitch_notation
  ;; The octave number is tied to the alphabetic character used to describe the pitch,
  ;; with the division between note letters ‘B’ and ‘C’, thus:
  (testing (str "B-1 and all of its possible variants (Bdouble flat, B♭, B, B♯, Bdouble sharp)"
                "would properly be designated as being in octave -1")
    (testing "B#-2 is not C-1"
      (is (thrown? Exception (sut/note-info :B#-2))))
    (is (= {:match "B-1", :pitch-class :B, :interval 11, :octave -1, :midi-note 11}
           (sut/note-info :B-1))))
  (testing (str "C-1 and all of its possible variants (Cdouble flat, C♭, C, C♯, Cdouble sharp)"
                "would properly be designated as being in octave -1")
    (is (= {:match "C-1", :pitch-class :C, :interval 0, :octave -1, :midi-note 0}
           (sut/note-info 0)
           (sut/note-info :C-1)))
    (testing "Cb-1 is not B-2"
      (is (= {:match "Cb-1", :pitch-class :B, :interval 11, :octave -1, :midi-note 11}
             (sut/note-info :Cb-1)))))
  (testing "notes numbers are between 0 and 127"
    (is (= {:match "G9", :pitch-class :G, :interval 7, :octave 9, :midi-note 127}
           (sut/note-info 127)
           (sut/note-info :G9)))
    (testing "B#9 is C9"
      (is (= {:match "B#9", :pitch-class :C, :interval 0, :octave 9, :midi-note 120}
             (sut/note-info :B#9))))
    (doseq [invalid [-1 128 :G#9 :Ab9 :A9 :A#9 :Bb9 :B9]]
      (testing (pr-str invalid)
        (is (thrown? Exception (sut/note-info invalid))))))
  (testing "octaves are betwen -1 and 9"
    (doseq [invalid [:C-2 :C10 :C#100]]
      (testing (pr-str invalid)
        (is (thrown? Exception (sut/note-info invalid))))))
  (is (= {:match "E#4", :pitch-class :F, :interval 5, :octave 4, :midi-note 65}
         (sut/note-info :E#4)))
  (is (= {:match "B4", :pitch-class :B, :interval 11, :octave 4, :midi-note 71}
         (sut/note-info :B4)))
  (testing "middle C's"
    (is (= {:match "B#4", :pitch-class :C, :interval 0, :octave 4, :midi-note 60}
           (sut/note-info :B#4)))
    (is (= {:match "C4", :pitch-class :C, :interval 0, :octave 4, :midi-note 60}
           (sut/note-info :C4)))))

(deftest find-scale-name-test
  (is (#{:melodic-minor :melodic-minor-asc} (sut/find-scale-name [2 1 2 2 2 2 1]))))

(deftest find-pitch-class-name-test
  (is (= :D (sut/find-pitch-class-name 62)))
  (is (= :D (sut/find-pitch-class-name 74)))
  (is (= :Eb (sut/find-pitch-class-name 75)))
  (is (= '(:D :Eb :E :F :F# :G :Ab :A :Bb :B :C :C#)
         (map sut/find-pitch-class-name (range 50 (+ 50 12)))))
  )

(deftest SCALE-test
  (doseq [[scale notes] sut/SCALE]
    (testing (pr-str scale)
      (is (= 12 (apply + notes))))))

(deftest scale-test
  (is (= [60 62 64 65 67 69 71 72] (sut/scale :c4 :major)))
  (is (= [60 61 62 63 64 65 66 67] (sut/scale :c4 :chromatic)))
  (is (= [60 61 62 63 64 65 66 67 68 69 70 71 72] (sut/scale :c4 :chromatic (range 1 13))))
  (is (= [:C4 :E4 :G4 :C5] (map sut/find-note-name (sut/scale :c4 :major [2 4 7]))))
  (is (= [:C4 :E4 :G4 :C5] (map sut/find-note-name (sut/scale :c4 :major [200]))))
  (is (= [60 62 63 65 67 68 70 72] (sut/scale :c4 :minor))))

(deftest degree->int-test
  )

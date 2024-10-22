(ns overtone.music.pitch-test
  (:require [clojure.test :refer [deftest is testing]]
            [overtone.music.pitch :as sut]))

(deftest shift-test
  (is (= [1 1 3 3] (sut/shift [0 1 2 3] [0 2] 1))))

(deftest nth-interval-test
  (is (= [0 2 4 5 7 9 11 12] (map sut/nth-interval (range 8))))
  (is (= [0 -1 -3 -5 -7 -8 -10 -12] (map sut/nth-interval (range 0 -8 -1)))))


(deftest mk-midi-string
  (is (= "F7" (sut/mk-midi-string :F 7)))
  (is (= "Fb7" (sut/mk-midi-string :Fb 7)))
  (is (= "Fb7" (sut/mk-midi-string :Fb3 7)))
  (is (= "C7" (sut/mk-midi-string 0 7)))
  (testing "invalid note"
    (is (thrown? Exception (sut/mk-midi-string -1 -1))))
  (testing "invalid octave"
    (is (thrown? Exception (sut/mk-midi-string 0 -2)))
    (is (thrown? Exception (sut/mk-midi-string 0 10)))))

(deftest invert-chord-works-properly
  (let [notes '(12 16 19 23)]
    (is (= (sut/invert-chord notes 2) '(19 23 24 28)))
    (is (= (sut/invert-chord notes -3) '(4 7 11 12)))))

(deftest chord-inversion-is-correct
  (is (= (sut/chord :F3 :major 0) '(53 57 60)))
  (is (= (sut/chord :F3 :major 1) '(57 60 65)))
  (is (= (sut/chord :F3 :major 2) '(60 65 69))))

(deftest octave-of-test
  (is (= -1 (sut/octave-of 0)))
  (is (= 9 (sut/octave-of 127)))
  (is (= 4 (sut/octave-of :C)))
  (is (= 4 (sut/octave-of :B)))
  (is (= 8 (sut/octave-of :C8))))

(deftest octave-note-test
  (testing "octave too low"
    (is (thrown? Exception (sut/octave-note -2 0))))
  (is (= 0 (sut/octave-note -1 0)))
  (is (= 0 (sut/octave-note :C-1 0)))
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
    (is (thrown? Exception (sut/octave-note 10 0))))
  (testing "overloading"
    (is (= 60
           (sut/octave-note :C)
           (sut/octave-note 4)
           (sut/octave-note :C :C)
           (sut/octave-note :C 0)
           (sut/octave-note :C4 :C)))
    (is (= 64 (sut/octave-note :C4 :E)))
    (is (= 77 (sut/octave-note :D5 :E#)))))

(deftest note-info-test
  ;; https://en.wikipedia.org/wiki/Scientific_pitch_notation
  ;; The octave number is tied to the alphabetic character used to describe the pitch,
  ;; with the division between note letters ‘B’ and ‘C’, thus:
  (testing (str "B-1 and all of its possible variants (Bdouble flat, B♭, B, B♯, Bdouble sharp)"
                "would properly be designated as being in octave -1")
    (testing "B#-2 is not C-1"
      (is (thrown? Exception (sut/note-info :B#-2))))
    (is (= {:match "B-1", :spelling "B" :pitch-class :B, :interval 11, :octave -1, :midi-note 11}
           (sut/note-info :B-1))))
  (testing (str "C-1 and all of its possible variants (Cdouble flat, C♭, C, C♯, Cdouble sharp)"
                "would properly be designated as being in octave -1")
    (is (= {:match "C-1", :spelling "C", :pitch-class :C, :interval 0, :octave -1, :midi-note 0}
           (sut/note-info 0)
           (sut/note-info :C-1)))
    (testing "Cb-1 is not B-2"
      (is (= {:match "Cb-1", :spelling "Cb", :pitch-class :B, :interval 11, :octave -1, :midi-note 11}
             (sut/note-info :Cb-1)))))
  (testing "notes numbers are between 0 and 127"
    (is (= {:match "G9", :spelling "G", :pitch-class :G, :interval 7, :octave 9, :midi-note 127}
           (sut/note-info 127)
           (sut/note-info :G9)))
    (testing "B#9 is C9"
      (is (= {:match "B#9", :spelling "B#" :pitch-class :C, :interval 0, :octave 9, :midi-note 120}
             (sut/note-info :B#9))))
    (doseq [invalid [-1 128 :G#9 :Ab9 :A9 :A#9 :Bb9 :B9]]
      (testing (pr-str invalid)
        (is (thrown? Exception (sut/note-info invalid))))))
  (testing "octaves are betwen -1 and 9"
    (doseq [invalid [:C-2 :C10 :C#100]]
      (testing (pr-str invalid)
        (is (thrown? Exception (sut/note-info invalid))))))
  (is (= {:match "E#4", :spelling "E#" :pitch-class :F, :interval 5, :octave 4, :midi-note 65}
         (sut/note-info :E#4)))
  (is (= {:match "B4", :spelling "B" :pitch-class :B, :interval 11, :octave 4, :midi-note 71}
         (sut/note-info :B4)))
  (testing "middle C's"
    (is (= {:match "B#4", :spelling "B#" :pitch-class :C, :interval 0, :octave 4, :midi-note 60}
           (sut/note-info :B#4)))
    (is (= {:match "C4", :spelling "C", :pitch-class :C, :interval 0, :octave 4, :midi-note 60}
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
  (is (= [60 62 64 65 67 69 71 72]
         (sut/scale :c :major)
         (sut/scale :c4 :major)))
  (is (= [60 62 63 65 67 68 70 72]
         (sut/scale :c :minor)
         (sut/scale :c4 :minor)))
  (testing "backwards scales"
    (is (= [72 70 68 67 65 63 62 60]
           (sut/scale :c :minor (range 7 -1 -1))
           (sut/scale :c4 :minor (range 7 -1 -1)))))
  (testing "infinite scales"
    (is (= [60 62 63 65 67 68 70 72 70 68 67 65 63 62 60 60 62 63 65 67]
           (take 20 (sut/scale :c :minor (cycle (concat (range 7) (range 7 -1 -1)))))
           (take 20 (sut/scale :c4 :minor (cycle (concat (range 7) (range 7 -1 -1))))))))
  (testing "negative degrees"
    (is (= [60 58 56 55 53 51 50 48 46 46 48 50 51 53 55 56 58 60 60 58]
           (take 20 (sut/scale :c :minor (cycle (concat (range 0 -9 -1) (range -8 1)))))
           (take 20 (sut/scale :c4 :minor (cycle (concat (range 0 -9 -1) (range -8 1))))))))
  (is (= [60 61 62 63 64 65 66 67 68 69 70 71 72] (sut/scale :c :chromatic)))
  (is (= [60 61 62 63 64 65 66 67 68 69 70 71 72] (sut/scale :c4 :chromatic)))
  (is (= [:C4 :E4 :G4 :C5]
         (map sut/find-note-name (sut/scale :c :major [0 2 4 7]))
         (map sut/find-note-name (sut/scale :c4 :major [0 2 4 7]))))
  (testing "out of bounds midi note"
    (is (thrown? Exception (doall (sut/scale :c4 :major [-200]))))
    (is (thrown? Exception (doall (sut/scale :c4 :major [200]))))
    (is (thrown? Exception (doall (sut/scale 127 :major)))))
  (testing "abbreviations"
    (is (= (sut/scale)
           (sut/scale :major)
           (sut/scale 60 :major)
           (sut/scale sut/MIDDLE-C :major)
           (sut/scale :c :major)
           (sut/scale :C :major)
           (sut/scale :C4 :major)
           (sut/scale :C :major (range 8))
           (sut/scale :C4 :major (range 8))))
    (doseq [scale-name (keys sut/SCALE)]
      (testing (pr-str scale-name)
        (is (= (sut/scale scale-name)
               (sut/scale 60 scale-name)
               (sut/scale sut/MIDDLE-C scale-name)
               (sut/scale :C scale-name)
               (sut/scale :C4 scale-name)
               (sut/scale :C4 scale-name (range (inc (count (sut/resolve-scale scale-name)))))))))
    (doseq [scale-name (keys sut/SCALE)]
      (testing (pr-str scale-name)
        (is (= (sut/scale 50 scale-name)
               (sut/scale :D3 scale-name)
               (sut/scale :D3 scale-name (range (inc (count (sut/resolve-scale scale-name)))))))))))

(deftest rand-chord-test
  (dotimes [_ 100]
    (is (every? #{:C :E :G} (map sut/canonical-pitch-class-name (sut/rand-chord))))
    (is (every? #{:D :F# :A} (map sut/canonical-pitch-class-name (sut/rand-chord :d))))
    (is (every? #{:D :F# :A} (map sut/canonical-pitch-class-name (sut/rand-chord :d :major))))
    (is (every? #{:D :F :A} (map sut/canonical-pitch-class-name (sut/rand-chord :d :minor))))
    (is (= 5 (count (sut/rand-chord :d :major 5))))
    (is (every? #(<= 0 % 24) (sut/rand-chord :c-1 :major)))
    (is (every? #(<= 0 % 24) (sut/rand-chord :c-1 :major 3))))
  ;;FIXME
  ;(sut/rand-chord 'g9)
  )

(deftest degree->int-test
  ;;TODO
  )

(deftest degree->interval-test
  (is (= 2 (sut/degree->interval :ii :major)))
  (is (= 2 (sut/degree->interval :ii#b :major)))
  (is (= 1 (sut/degree->interval :iib :major)))
  (is (= -1 (sut/degree->interval :iibbb :major)))
  (is (= 5 (sut/degree->interval :iii# :major)))
  )

(deftest resolve-chord-notes-test
  (is (= [60 64 67]
         (sut/resolve-chord-notes [:C :E :G])
         (sut/resolve-chord-notes [:C4 :E :G])
         (sut/resolve-chord-notes [:C4 :E :G])
         (sut/resolve-chord-notes [:C4 :E4 :G4])))
  (is (= [64 67 72]
         (sut/resolve-chord-notes [:E :G :C])
         (sut/resolve-chord-notes [:E4 :G :C])
         (sut/resolve-chord-notes [:E4 :G :C5])
         (sut/resolve-chord-notes [:E4 :G4 :C5])))
  (is (= [60 64 67 71 74 77] (sut/resolve-chord-notes [:C :E :G :B :D :F])))
  (is (= [60 52 55 59 60 62 65] (sut/resolve-chord-notes [:C :E3 :G :B 60 :D :F]))))

(deftest find-chord-test
  (is (= {:root :C :chord-type :major} (sut/find-chord [:C :E :G])))
  ;; TODO C major..
  (is (= {:root :E :chord-type :m+5} (sut/find-chord [:E :G :C])))
  (is (= {:root :C :chord-type :major7} (sut/find-chord [:C :E :G :B]))))

(deftest scale-field-test
  (is (= 0 (first (sut/scale-field :c))))
  (is (= 127 (peek (sut/scale-field :c))))
  (is (= 75 (count (sut/scale-field :c))))
  (is (= (sut/scale-field :c)
         (sut/scale-field :b#)
         (sut/scale-field :c4)
         (sut/scale-field :c9)
         (sut/scale-field 0)
         (sut/scale-field 12)
         (sut/scale-field 120)))
  (is (= 128 (count (sut/scale-field :C :chromatic))))
  (is (apply = (map #(sut/scale-field % :chromatic) (range 128))))
  (testing "fields contain valid MIDI notes"
    (doseq [note (range 128)
            field (sut/scale-field 120)]
      (testing (pr-str note)
        (is (<= 0 field 127))))))

(deftest canonical-pitch-class-name-test
  (is (= :C (sut/canonical-pitch-class-name 0)))
  (is (= :B (sut/canonical-pitch-class-name 11)))
  (is (= :C (sut/canonical-pitch-class-name 12)))
  (is (thrown? Exception (sut/canonical-pitch-class-name 128))))

(deftest find-note-name-test
  (is (= :C4 (sut/find-note-name :B#4)))
  (is (= :C-1
         (sut/find-note-name 0)
         (sut/find-note-name :B#-1)))
  (is (= :C#-1
         (sut/find-note-name 1)
         (sut/find-note-name :Db-1)))
  (is (= :B4
         (sut/find-note-name 71)
         (sut/find-note-name :Cb4)))
  (is (= :C4
         (sut/find-note-name 60)
         (sut/find-note-name :C)
         (sut/find-note-name :C4)
         (sut/find-note-name :B#4))))

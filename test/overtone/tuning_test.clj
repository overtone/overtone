(ns overtone.tuning-test
    (:require [overtone.util.log :as log])
    (:use overtone.music.tuning
          clojure.test))

(deftest helper-test
    (let [testlist  '(:1 :b "3" "d" ("e" "f" \g \h))
          testmap   {:1 "1", :b 2, "3" 3, :d "4" }]

        (is (= (list-flatten-first testlist) (list-flatten-first :1) :1))
        (is (= (get-or-fn testmap :b 0) 2))
        (is (= (get-or-fn testmap "a" clojure.string/capitalize) "A"))
        (is (= (map-or-fn testmap testlist identity) '("1" 2 3 "d" ("e" "f" \g \h))))))

(deftest midi-test
    (is (= (perform '(:midi 60 69 81)) '(261.6255653005986 440.0 880.0)) "Sanity check for MIDI note numbers")
    (is (= ((tunednotes :midi) :F#7) 102) "sanity check for tuned notes function.")
    (is (= ((tunednotes :midi) :F##7) 103))
    (is (= ((tunednotes :midi) :F##bb#7) 102) "Demonstration of collapsing sharps and flats.")
    (is (= ((tunednotes :midi) :db5) 73))
    (is (= ((reversenotes :midi) 73) :c#5) "Sanity check for reverse notes function.")
    (is (= ((reversenotes :midi) 102) :f#7))
    (is (= ((reversenotes :midi) ((tunednotes :midi) :F#7)) :f#7) "This combination takes note names to their canonical versions.")
    (is (= ((reversenotes :midi) ((tunednotes :midi) :F##bb#7)) :f#7))
    (is (= ((canonical-note-names :midi) :F##bb#7)) :f#7) "Which is why there is a simplified version of it.")

(deftest ed-test
    (let [perfunc (perfn '(:ed 10 10 0 100))]
        (is (< (#(- (reduce max %) (reduce min %))
                (list (/ (perfunc 101) (perfunc 100))
                      (/ (perfunc 51) (perfunc 50))
                      (/ (perfunc 28) (perfunc 27))
                      (/ (perfunc 1) (perfunc 0)))) 0.0000000001) "Frequency values should scale properly, but are floats so should not be used for equality.")
        (is (= (perfunc 0) 100.0))
        (is (= (perfunc 10) 1000.0) "Note that equal divions doesn't umply an octave.")))

(deftest edo-test
    (let [perfunc (perfn '(:edo 10 0 100))]
        (is (< (#(- (reduce max %) (reduce min %))
                (list (/ (perfunc 101) (perfunc 100))
                      (/ (perfunc 51) (perfunc 50))
                      (/ (perfunc 28) (perfunc 27))
                      (/ (perfunc 1) (perfunc 0)))) 0.0000000001))
        (is (= (perfunc 0) 100.0))
        (is (= (perfunc 10) 200.0))
        ))

(deftest edo-12-test
    (is (= (perform '((:edo 12 100 440) 73 76 79)) '(92.49860567790861 110.0 130.8127826502993))))
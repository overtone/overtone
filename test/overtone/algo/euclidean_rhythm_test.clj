(ns overtone.algo.euclidean-rhythm-test
  (:use overtone.algo.euclidean-rhythm
        clojure.test))

(deftest euclidean-rhythm-generates-correct-patterns
  (is (= (euclidean-rhythm 1 2) [1 0]))
  (is (= (euclidean-rhythm 1 3) [1 0 0]))
  (is (= (euclidean-rhythm 2 5) [1 0 1 0 0]))
  (is (= (euclidean-rhythm 3 8) [1 0 0 1 0 0 1 0])))

(deftest euclidean-rhythm-throws-if-pulses-exceeds-steps
  (is (thrown? IllegalArgumentException (euclidean-rhythm 100 1))))

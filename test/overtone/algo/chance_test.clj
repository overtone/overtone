(ns overtone.algo.chance-test
  (:use overtone.algo.chance
        clojure.test))

(deftest sputter-likelihood-is-controllable
  (is (= (sputter [1 2] 1) (take 100 (cycle [1]))))
  (is (= (sputter [1 2] 0) [1 2])))

(deftest sputter-respects-maximum-size
  (is (= (sputter [1 2] 1 0) []))
  (is (= (sputter [1 2] 1 5) [1 1 1 1 1])))

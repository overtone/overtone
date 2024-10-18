(ns overtone.sc.ugen-collide-test
  (:require [clojure.test :refer [deftest is testing]]
            [overtone.sc.ugens :as u :refer [gate:kr]]
            [overtone.sc.machinery.ugen.sc-ugen :as urep]))

;; there used to be ugens called and/or (originally sig-{and,or,xor}).
;; they were renamed to bit-{and,or} and now clojure.core/{and,or} are not shadowed,
;; which this test verifies.
(deftest ugens-colliding-with-macros-test
  (testing "and is not shadowed"
    (is (true? (u/with-overloaded-ugens (and))))
    (is (= 1 (u/with-overloaded-ugens (and 1))))
    (is (= 1 (u/with-overloaded-ugens (and 1 1))))
    (is (= 5 (u/with-overloaded-ugens (and 1 2 3 4 5)))))
  (testing "or is not shadowed"
    (is (nil? (u/with-overloaded-ugens (or))))
    (is (= 1 (u/with-overloaded-ugens (or 1))))
    (is (= 1 (u/with-overloaded-ugens (or 1 1))))
    (is (= 1 (u/with-overloaded-ugens (or 1 nil nil nil nil nil))))
    (is (= 3 (u/with-overloaded-ugens (or nil nil 3 nil nil))))))

(deftest bit-ops-test
  (testing "bit-and"
    (is (thrown? Exception (u/with-overloaded-ugens (bit-and))))
    (is (thrown? Exception (u/with-overloaded-ugens (bit-and :force-ugen))))
    (is (thrown? Exception (u/with-overloaded-ugens (bit-and 1))))
    (is (thrown? Exception (u/with-overloaded-ugens (bit-and 1 :force-ugen))))
    (is (= 1 (u/with-overloaded-ugens (bit-and 1 1))))
    (is (urep/sc-ugen? (u/with-overloaded-ugens (bit-and 1 1 :force-ugen))))
    (is (= 0 (u/with-overloaded-ugens (bit-and 1 2 3 4 5))))
    (is (urep/sc-ugen? (u/with-overloaded-ugens (bit-and 1 2 3 4 5 :force-ugen))))
    (is (urep/sc-ugen? (u/with-overloaded-ugens (bit-and 1 2 (gate:kr) 4 5))))
    (is (urep/sc-ugen? (u/with-overloaded-ugens (bit-and 1 2 (gate:kr) 4 5 :force-ugen)))))
  (testing "bit-or"
    (is (thrown? Exception (u/with-overloaded-ugens (bit-or))))
    (is (thrown? Exception (u/with-overloaded-ugens (bit-or :force-ugen))))
    (is (thrown? Exception (u/with-overloaded-ugens (bit-or 1))))
    (is (thrown? Exception (u/with-overloaded-ugens (bit-or 1 :force-ugen))))
    (is (= 1 (u/with-overloaded-ugens (bit-or 1 1))))
    (is (urep/sc-ugen? (u/with-overloaded-ugens (bit-or 1 1 :force-ugen))))
    (is (= 7 (u/with-overloaded-ugens (bit-or 1 2 3 4 5))))
    (is (urep/sc-ugen? (u/with-overloaded-ugens (bit-or 1 2 (gate:kr) 4 5))))
    (is (urep/sc-ugen? (u/with-overloaded-ugens (bit-or 1 2 (gate:kr) 4 5 :force-ugen)))))
  (testing "bit-xor"
    (is (thrown? Exception (u/with-overloaded-ugens (bit-xor))))
    (is (thrown? Exception (u/with-overloaded-ugens (bit-xor :force-ugen))))
    (is (thrown? Exception (u/with-overloaded-ugens (bit-xor 1))))
    (is (thrown? Exception (u/with-overloaded-ugens (bit-xor 1 :force-ugen))))
    (is (= 0 (u/with-overloaded-ugens (bit-xor 1 1))))
    (is (urep/sc-ugen? (u/with-overloaded-ugens (bit-xor 1 1 :force-ugen))))
    (is (= 1 (u/with-overloaded-ugens (bit-xor 1 2 3 4 5))))
    (is (urep/sc-ugen? (u/with-overloaded-ugens (bit-xor 1 2 (gate:kr) 4 5))))
    (is (urep/sc-ugen? (u/with-overloaded-ugens (bit-xor 1 2 (gate:kr) 4 5 :force-ugen))))))

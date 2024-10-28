(ns overtone.test-helper-test
  (:require [clojure.test :refer [deftest is testing]]
            [overtone.test-helper :as sut])
  (:import [java.util.concurrent TimeoutException]))

(deftest eval-in-temp-ns-test
  (testing "without errors..."
    (let [ns *ns*
          ns-count (count (all-ns))]
      (sut/eval-in-temp-ns (+ 1 2 3))
      (is (= ns-count (count (all-ns))) "removes temp-ns when done")
      (is (= ns *ns*) "restores original ns")))

  (testing "with errors..."
    (let [ns *ns*
          ns-count (count (all-ns))]
      (try (sut/eval-in-temp-ns (/ 1 0))
           (catch java.lang.ArithmeticException e))
      (is (= ns-count (count (all-ns))) "removes temp-ns when done")
      (is (= ns *ns*) "restores original ns"))))

(deftest invoke-timeout-test
  (testing "invoke-timeout..."
    (let [n (Thread/activeCount)]
      (sut/invoke-timeout #(Thread/sleep 100) 1000)
      (is (= n (Thread/activeCount)) "cleans up threads")
      (is (thrown? TimeoutException (sut/invoke-timeout #(Thread/sleep 1000) 100))
          "throws TimeoutException"))))

(deftest wait-while-test
  (testing "wait-while..."

    (let [done? (atom nil)]
      (future (Thread/sleep 1000)
              (reset! done? :done))
      (sut/wait-while #(not @done?))
      (is (= :done @done?)
          "delays the current thread"))

    (let [done? (atom nil)]
      (future (Thread/sleep 1000)
              (reset! done? :done))
      (is (thrown? TimeoutException (sut/wait-while #(not @done?) 100))
          "throws TimeoutException"))))

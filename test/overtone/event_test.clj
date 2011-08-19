(ns overtone.event-test
  (:require [overtone.core.log :as log])
  (:use overtone.event
        clojure.test))

(log/level :debug)

(deftest handler-test
  (let [counter (atom 0)]
    (on-sync-event :test-event #(swap! counter inc) :a)
    (on-event :test-event #(swap! counter inc) :b)
    (event :test-event)
    (Thread/sleep 100)
    (is (= 2 @counter))

    (remove-handler :test-event :b)
    (event :test-event)
    (Thread/sleep 100)
    (is (= 3 @counter))

    (on-event :test-event #(swap! counter inc) :x)
    (on-event :test-event #(swap! counter inc) :y)
    (on-event :test-event #(swap! counter inc) :z)
    (event :test-event)
    (Thread/sleep 100)
    (is (= 7 @counter))

    (clear-handlers :test-event)
    (event :test-event)
    (Thread/sleep 100)
    (is (= 7 @counter))))

(defn event-tests []
  (binding [*test-out* *out*]
    (run-tests 'overtone.core.event-test)))

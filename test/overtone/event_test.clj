(ns overtone.event-test
  (:require [overtone.config.log :as log])
  (:use overtone.libs.event
        clojure.test))

(log/set-level! :debug)

(deftest handler-test
  (let [counter (atom 0)]
    (on-sync-event :test-event #(swap! counter inc) :a)
    (on-event :test-event #(swap! counter inc) :b)
    (event :test-event)
    (Thread/sleep 100)
    (is (= 2 @counter))

    (remove-handler :b)
    (event :test-event)
    (Thread/sleep 100)
    (is (= 3 @counter))

    (on-event :test-event #(swap! counter inc) :x)
    (on-event :test-event #(swap! counter inc) :y)
    (on-event :test-event #(swap! counter inc) :z)
    (event :test-event)
    (Thread/sleep 100)
    (is (= 7 @counter))

    (remove-event-handlers :test-event)
    (event :test-event)
    (Thread/sleep 100)
    (is (= 7 @counter))))

(defn event-tests []
  (binding [*test-out* *out*]
    (run-tests 'overtone.core.event-test)))

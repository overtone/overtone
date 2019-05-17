(ns overtone.event-test
  (:require [overtone.config.log :as log])
  (:use overtone.libs.event
        clojure.test))

(log/set-level! :debug)

(deftest handler-test
  (let [counter (atom 0)]
    (on-sync-event :test-event (fn [_] (swap! counter inc)) :a)
    (on-event :test-event (fn [_] (swap! counter inc)) :b)
    (event :test-event)
    (Thread/sleep 100)
    (is (= 2 @counter))

    (remove-event-handler :b)
    (event :test-event)
    (Thread/sleep 100)
    (is (= 3 @counter))

    (on-event :test-event (fn [_] (swap! counter inc)) :x)
    (on-event :test-event (fn [_] (swap! counter inc)) :y)
    (on-event :test-event (fn [_] (swap! counter inc)) :z)
    (event :test-event)
    (Thread/sleep 100)
    (is (= 7 @counter))
    (remove-event-handler :test-event)
    )
  )

(defn event-tests []
  (binding [*test-out* *out*]
    (run-tests 'overtone.event-test)))

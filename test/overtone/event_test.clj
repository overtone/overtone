(ns overtone.event-test
  (:require [overtone.config.log :as log])
  (:use overtone.libs.event
        clojure.test))

(log/set-level! :debug)

(deftest handler-test
  (let [counter (atom 0)
        handler (fn [e] (swap! counter inc))]
    (on-sync-event :test-event handler :a)
    (on-event :test-event handler :b)
    (event :test-event)
    (Thread/sleep 100)
    (is (= 2 @counter))

    (remove-event-handler :b)
    (event :test-event)
    (Thread/sleep 100)
    (is (= 3 @counter))

    (on-event :test-event handler :x)
    (on-event :test-event handler :y)
    (on-event :test-event handler :z)
    (event :test-event)
    (Thread/sleep 100)
    (is (= 7 @counter))

    (remove-event-handler :a)
    (remove-event-handler :x)
    (remove-event-handler :y)
    (remove-event-handler :z)
    (event :test-event)
    (Thread/sleep 100)
    (is (= 7 @counter))))

(deftest fire-many-args-async-test
  (let [fires (atom nil)]
    (on-event :test-event #(swap! fires conj %) :test-event-key)
    (event :test-event :a "foo" :b "bar" :c "baz")

    (Thread/sleep 100)

    (is (= @fires [{:a "foo"
                    :b "bar"
                    :c "baz"}]))

    (remove-event-handler :test-event-key)))

(deftest fire-map-arg-async-test
  (let [fires (atom nil)]
    (on-event :test-event #(swap! fires conj %) :test-event-key)
    (event :test-event {:a "foo"
                        :b "bar"
                        :c "baz"})

    (Thread/sleep 100)

    (is (= @fires [{:a "foo"
                    :b "bar"
                    :c "baz"}]))

    (remove-event-handler :test-event-key)))

(deftest fire-many-args-sync-test
  (let [fires (atom nil)]
    (on-sync-event :test-event #(swap! fires conj %) :test-event-key)
    (sync-event :test-event :a "foo" :b "bar" :c "baz")

    (Thread/sleep 100)

    (is (= @fires [{:a "foo"
                    :b "bar"
                    :c "baz"}]))

    (remove-event-handler :test-event-key)))

(deftest fire-map-arg-sync-test
  (let [fires (atom nil)]
    (on-sync-event :test-event #(swap! fires conj %) :test-event-key)
    (sync-event :test-event {:a "foo"
                             :b "bar"
                             :c "baz"})

    (Thread/sleep 100)

    (is (= @fires [{:a "foo"
                    :b "bar"
                    :c "baz"}]))

    (remove-event-handler :test-event-key)))
      
(defn event-tests []
  (binding [*test-out* *out*]
    (run-tests 'overtone.event-test)))

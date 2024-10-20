(ns overtone.event-test
  (:require
   [overtone.libs.event :refer [on-sync-event on-event event remove-event-handler sync-event]]
   [clojure.test :refer [deftest is]]))

(comment
  (require '[overtone.config.log :as log])
  (log/set-level! :debug)
  )

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

    (remove-event-handler :a)
    (remove-event-handler :x)
    (remove-event-handler :y)
    (remove-event-handler :z)
    (event :test-event)
    (Thread/sleep 100)
    (is (= 7 @counter))))

#_ ;;FIXME
(deftest fire-many-args-async-test
  (let [fires (atom [])]
    (on-event :test-event #(swap! fires conj %) :test-event-key)
    (event :test-event :a "foo" :b "bar" :c "baz")

    (Thread/sleep 100)

    (is (= @fires [{:a "foo"
                    :b "bar"
                    :c "baz"}]))

    (remove-event-handler :test-event-key)))

(deftest fire-map-arg-async-test
  (let [fires (atom [])]
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
  (let [fires (atom [])]
    (on-sync-event :test-event #(swap! fires conj %) :test-event-key)
    (sync-event :test-event :a "foo" :b "bar" :c "baz")

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

    (is (= @fires [{:a "foo"
                    :b "bar"
                    :c "baz"}]))

    (remove-event-handler :test-event-key)))

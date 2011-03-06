(ns overtone.deps-test
  (:use clojure.test
        overtone.deps))

(deftest deps-basic-test
  (reset-deps)
  (let [log (atom [])]
    (on-deps :foo
            #(swap! log conj :a))
    (satisfy-deps :foo)
    (on-deps :foo
            #(swap! log conj :b))
    (on-deps [:foo :bar]
            #(swap! log conj :c))
    (on-deps #{:foo :bar :baz}
            #(swap! log conj :d))
    (satisfy-deps :bar)
    (Thread/sleep 100)
    (satisfy-deps :baz)
    (Thread/sleep 100)
    (on-deps #{:foo :baz}
            #(swap! log conj :e))
    (Thread/sleep 100)
    (is (= [:a :b :c :d :e] @log))))


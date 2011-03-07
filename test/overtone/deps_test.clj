(ns overtone.deps-test
  (:use clojure.test
        overtone.deps))

(deftest deps-basic-test
  (reset-deps)
  (let [log (atom [])]
    (with-deps :foo
            #(swap! log conj :a))
    (satisfy-deps :foo)
    (with-deps :foo
            #(swap! log conj :b))
    (with-deps [:foo :bar]
            #(swap! log conj :c))
    (with-deps #{:foo :bar :baz}
            #(swap! log conj :d))
    (satisfy-deps :bar)
    (Thread/sleep 100)
    (satisfy-deps :baz)
    (Thread/sleep 200)
    (with-deps #{:foo :baz}
            #(swap! log conj :e))
    (Thread/sleep 300)
    (is (= [:a :b :c :d :e] @log))))


(ns overtone.deps-test
  (:use clojure.test
        overtone.deps))

(deftest deps-basic-test
  (let [log (atom [])]
    (on-dep :foo
            #(swap! log conj :a))
    (on-dep [:foo :bar]
            #(swap! log conj :b))
    (on-dep #{:foo :bar :baz}
            #(swap! log conj :c))
    (satisfy-dep :foo)
    (satisfy-dep :bar)
    (satisfy-dep :baz)
    (Thread/sleep 500)
    (is (= [:a :b :c] @log))))


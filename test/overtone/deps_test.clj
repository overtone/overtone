(ns overtone.deps-test
  (:use clojure.test
        overtone.deps))

(deftest deps-basic-test
  (reset-deps)
  (let [log (atom [])]
    (on-deps :foo ::swapper
            #(swap! log conj :a))
    (satisfy-deps :foo)
    (on-deps :foo ::another-swapper
            #(swap! log conj :b))
    (on-deps [:foo :bar] ::third-swapper
            #(swap! log conj :c))
    (on-deps #{:foo :bar :baz} ::fourth-swapper
            #(swap! log conj :d))
    (satisfy-deps :bar)
    (Thread/sleep 100)
    (satisfy-deps :baz)
    (Thread/sleep 200)
    (on-deps #{:foo :baz} ::fifth-swapper
            #(swap! log conj :e))
    (Thread/sleep 300)
    (is (= [:a :b :c :d :e] @log))))


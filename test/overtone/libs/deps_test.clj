(ns overtone.libs.deps-test
  (:use clojure.test
        overtone.libs.deps))

(deftest deps-basic-test
  (with-redefs [dep-state* (agent {:satisfied #{}
                                   :todo      []
                                   :done      []
                                   :history   []})]
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
      (wait-until-deps-satisfied :bar)
      (satisfy-deps :baz)
      (wait-until-deps-satisfied :baz)
      (on-deps #{:foo :baz} ::fifth-swapper
               #(do (swap! log conj :e)
                    (satisfy-deps :done)))
      (wait-until-deps-satisfied :done)
      (is (= [:a :b :c :d :e] @log)))))

(deftest deps-start-via-empty-on-deps-test
  (dotimes [_ 10]
    (with-redefs [dep-state* (agent {:satisfied #{}
                                     :todo      []
                                     :done      []
                                     :history   []})]
      (let [log (atom [])
            fs [#(on-deps #{} ::start
                          (fn []
                            (swap! log conj :init)
                            (satisfy-deps :foo)))
                #(on-deps :foo ::a
                          (fn []
                            (swap! log conj :a)
                            (satisfy-deps :second)))
                #(on-deps :second ::b
                          (fn []
                            (swap! log conj :b)
                            (satisfy-deps :bar)))
                #(on-deps [:foo :bar] ::c
                          (fn []
                            (swap! log conj :c)
                            (satisfy-deps :baz)))
                #(on-deps #{:foo :bar :baz} ::d
                          (fn []
                            (swap! log conj :d)))
                #(on-deps #{:foo :baz} ::e
                          (fn []
                            (swap! log conj :e)
                            (satisfy-deps :done)))]
            order (shuffle (vec (range (count fs))))]
        (testing (pr-str order)
          (run! #(future ((nth fs %))) order)
          (is (true? (try (wait-until-deps-satisfied :done)
                          true
                          (catch Exception e e))))
          (is (contains? #{[:init :a :b :c :d :e]
                           [:init :a :b :c :e :d]} @log))
          (is (contains? #{[::start ::a ::b ::c ::d ::e]
                           [::start ::a ::b ::c ::e ::d]}
                         (keep #(when (#{:processed :satisfied-and-processed} (:action %))
                                  (:key %))
                               (:history @dep-state*)))))))))

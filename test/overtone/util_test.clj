(ns overtone.util-test
  (:use clojure.test)
  (:require [overtone.util.lib :as lib]))

(deftest arg-mapper-test
  (let [defaults {:a 1 :b 2 :c 3 :d 4}
        names (sort (keys defaults))]
    (is (= defaults (lib/arg-mapper []
                                    names
                                    defaults)))

    (is (= (conj defaults {:a 10 :b 20})
           (lib/arg-mapper [10 20]
                           names
                           defaults)))

    (is (= (conj defaults {:a 10 :b 20 :c 30 :d 40})
           (lib/arg-mapper [10 20 :c 30 :d 40]
                           names
                           defaults)))))

(defn util-tests []
  (binding [*test-out* *out*]
    (run-tests 'util-test)))

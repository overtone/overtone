(ns overtone.music.rhythm-test
  (:require [clojure.test :refer [deftest is]]
            [overtone.music.rhythm :as r]))

(deftest beat-ms-test
  (is (= 600.0 (r/beat-ms 100)))
  (is (= [-600.0 0.0 600.0 1200.0] (mapv #(r/beat-ms % 100) (range -1 3))))
  (is (= [0.0 500.0 1000.0 1500.0 2000.0] (mapv #(r/beat-ms % 120) (range 5)))))

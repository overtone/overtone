(ns overtone.studio.wavetable-test
  (:use [overtone.studio wavetable]
        clojure.test))

(deftest wavetable-signal-conversion-test
  (is (= (map double (range 1000.0))
         (wavetable->signal (signal->wavetable (range 1000.0))))))

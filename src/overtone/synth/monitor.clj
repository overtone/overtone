(ns overtone.synth.monitor
  "Useful monitoring synths"
  {:author "Sam Aaron"}
  (:use [overtone.core]))

(defsynth mono-audio-bus-level [in-a-bus 0 out-c-bus 0]
  (let [sig   (in:ar in-a-bus 1)
        level (amplitude sig)
        level (lag-ud level 0.1 0.3)]
    (out:kr out-c-bus [(a2k level)])))

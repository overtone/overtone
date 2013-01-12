(ns ^{:doc "A suite of synths useful for populating and manipulating
            control busses of time signals i.e. generating and counting
            beats."
      :author "Sam Aaron"}
  overtone.synth.timing
  (:use [overtone.core]))

(defsynth trigger [rate 100 out-bus 0]
  (out:kr out-bus (impulse:kr rate)))

(defsynth counter [in-bus 0 out-bus 0]
  (out:kr out-bus (pulse-count:kr (in:kr in-bus))))

(defsynth divider [div 32 in-bus 0 out-bus 0]
  (out:kr out-bus (pulse-divider (in:kr in-bus) div)))

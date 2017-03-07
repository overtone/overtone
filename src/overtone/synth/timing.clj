(ns ^{:doc "A suite of synths useful for populating and manipulating
            control buses of time signals i.e. generating and counting
            beats."
      :author "Sam Aaron"}
  overtone.synth.timing
  (:use [overtone.core]
        [overtone.helpers.lib :only [uuid]]))

(defonce count-trig-id (trig-id))

(defsynth trigger [rate 100 out-bus 0]
  (out:kr out-bus (impulse:kr rate)))

(defsynth counter [in-bus 0 out-bus 0]
  (out:kr out-bus (pulse-count:kr (in:kr in-bus))))

(defsynth divider [div 32 in-bus 0 out-bus 0]
  (out:kr out-bus (pulse-divider (in:kr in-bus) div)))

(defsynth send-beat [in-bus 0 beat-bus 0 id count-trig-id]
  (send-trig (in:kr in-bus) id (+ (in:kr beat-bus) 1)))

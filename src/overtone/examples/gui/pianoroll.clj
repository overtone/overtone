(ns overtone.examples.gui.pianoroll
  (:use overtone.live
        overtone.inst.synth
        overtone.gui.control
        overtone.gui.pianoroll))

(def m (metronome 280))

(synth-controller tb303)
(def roll (piano-roll m tb303
                      :octaves 3 :offset 36
                      :measures 1 :beats-per-measure 4
                      :steps-per-beat 4))

; Currently set notes can be retrieved like this:
; (:notes @(:state roll))

(ns overtone.examples.gui.sequencer
  (:use overtone.live
        overtone.gui.sequencer
        overtone.inst.drum))

(def m (metronome 240))

(def example-sequence
  {"kick" [true false true false true false true false]
   "closed-hat" [false true false true false true false true]
   "snare" [false false true false false false true false]})

(def sequencer (step-sequencer m 8 [kick closed-hat snare] example-sequence))

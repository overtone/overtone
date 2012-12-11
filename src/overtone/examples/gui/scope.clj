(ns overtone.examples.gui.scope
  (:use overtone.live
        overtone.inst.synth
        overtone.gui.scope))

(pscope)
(spectrogram :bus 0 :keep-on-top true)

;(demo 10 (sin-osc (* 2000 (+ 1 (sin-osc:kr 0.2)))))

;(rise-fall-pad 660)

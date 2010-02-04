(ns sc2-examples
  (:use overtone.live))

(refer-ugens)

; analog bubbles
(defsynth analog-bubbles []
  (let [f (mul-add (lf-saw:kr 0.4 0) 24 
                            (mul-add (lf-saw:kr [8 7.23] 0) 3 80))
        f1 (midicps (first f))
        f2 (midicps (second f))]
    (comb-n (* (sin-osc [f1 f2] 0) 0.04) 0.2 0.2 4)))

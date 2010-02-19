(ns examples.sc2-examples
  (:use overtone.live))

;(refer-ugens)

;(boot)

; analog bubbles
(defsynth analog-bubbles []
  (let [freqs (midicps (mul-add (lf-saw:kr 0.4 0) 
                                24 
                                (mul-add (lf-saw:kr [8 7.23] 0) 
                                3 80)))]
    (comb-n (* (sin-osc freqs 0) 0.04) 0.2 0.2 4)))

(defsynth resonant-pulses []
  (comb-l 
    (rlpf (* 0.05 (lf-pulse (mul-add (f-sin-osc:kr 0.05 0) 80 160) 0 0.4))
          (mul-add (f-sin-osc:kr [0.6 0.7] 0) 3600 4000) 
          0.2)
    0.3 [0.2 0.25] 2))

(defsynth moto-rev []
  (clip2 (rlpf (lf-pulse (mul-add (sin-osc:kr 0.2 0) 10 21) 0.1) 100 0.1) 0.4))

; TODO: Figure out what max is all about and why we don't have it
(defsynth scratchy []
  (rhpf (* (maximum (brown-noise [0.5 0.5] -0.49) 0) 20)
        5000
        1))

; TODO: Not sure why this is just spitting out noise...
(defsynth sprinkler []
  (bpz2:ar (white-noise:ar (mul-add (lf-pulse:kr (mouse-x:kr 0.2 50) 0 0.25) 0.1 0))))


(ns examples.feedback
  (:use overtone.core))

; A basic feedback loop going through a delay line
; * move the mouse left/right to adjust the delay
; * leak-dc removes any DC offset (waveform isn't centered at zero)
(defsynth feedback-loop []
  (let [input (crackle 1.5, 0.5)
        fb-in (local-in 1)
        snd (+ input (leak-dc (delay-n fb-in 0.5 (* 0.5 (mouse-x 0 1.05)))))
        fb-out (local-out snd)
        snd (limiter snd 0.8)]
    (out 0 (pan2 snd))))

;(feedback-loop)

(defsynth distorted-feedback []
  (let [noiz (mul-add (lf-noise0:kr 0.5) 2 2.05)
        input (crackle 1.5, 0.15)
        fb-in (local-in 1)
        snd (+ input (leak-dc (* 1.1 (delay-n fb-in 3.5 noiz))))
        snd (rlpf snd (mul-add (lf-noise0:kr noiz) 400 800) 0.5)
        snd (clip:ar snd 0 0.9)
        fb-out (local-out snd)]
    (out 0 (pan2 snd))))

;(distorted-feedback)

;(stop)

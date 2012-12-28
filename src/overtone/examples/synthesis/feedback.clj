(ns overtone.examples.synthesis.feedback
  (:use overtone.core))

;; The local-out and local-in ugens can be used to pipe signal data back
;; into the same synth each time processes a buffer of audio.  This is
;; how you implement feedback loops and custom delays.

;; A basic feedback loop going through a delay line
;; * move the mouse left/right to adjust the delay
;; * leak-dc removes any DC offset (waveform isn't centered at zero)
(defsynth feedback-loop []
  (let [input (crackle 1.5)
        fb-in (local-in 1)
        snd (+ input (leak-dc (delay-n fb-in 2.0 (* 0.8 (mouse-x 0.001 1.05)))))
        fb-out (local-out snd)
        snd (limiter snd 0.8)]
    (out 0 (pan2 snd))))

;;(feedback-loop)
;;(stop)

(defsynth distorted-feedback []
  (let [noiz (mul-add (lf-noise0:kr 0.5) 2 2.05)
        input (* 0.15 (crackle  1.5))
        fb-in (local-in 1)
        snd (+ input (leak-dc (* 1.1 (delay-n fb-in 3.5 noiz))))
        snd (rlpf snd (mul-add (lf-noise0:kr noiz) 400 800) 0.5)
        snd (clip:ar snd 0 0.9)
        fb-out (local-out snd)]
    (out 0 (pan2 snd))))

;;(distorted-feedback)
;;(stop)

(ns overtone.synth.sts
  (:use [overtone.core]))

;; Synths translated from the excellent book
;; "Steal This Sound"
;;   by Mitchell Sigman
;;
;; Buy a copy if you don't own one already!

(defsynth prophet
  "The Prophet Speaks (page 2)

   Dark and swirly, this synth uses Pulse Width Modulation (PWM) to
   create a timbre which continually moves around. This effect is
   created using the pulse ugen which produces a variable width square
   wave. We then control the width of the pulses using a variety of LFOs
   - sin-osc and lf-tri in this case. We use a number of these LFO
   modulated pulse ugens with varying LFO type and rate (and phase in
   some cases to provide the LFO with a different starting point. We
   then mix all these pulses together to create a thick sound and then
   feed it through a resonant low pass filter (rlpf).

   For extra bass, one of the pulses is an octave lower (half the
   frequency) and its LFO has a little bit of randomisation thrown into
   its frequency component for that extra bit of variety."

  [amp 1 freq 440 cutoff-freq 12000 rq 0.3  attack 1 decay 2 out-bus 0 ]

  (let [snd (pan2 (mix [(pulse freq (* 0.1 (/ (+ 1.2 (sin-osc:kr 1)) )))
                        (pulse freq (* 0.8 (/ (+ 1.2 (sin-osc:kr 0.3) 0.7) 2)))
                        (pulse freq (* 0.8 (/ (+ 1.2 (lf-tri:kr 0.4 )) 2)))
                        (pulse freq (* 0.8 (/ (+ 1.2 (lf-tri:kr 0.4 0.19)) 2)))
                        (* 0.5 (pulse (/ freq 2) (* 0.8 (/ (+ 1.2 (lf-tri:kr (+ 2 (lf-noise2:kr 0.2))))
                                                           2))))]))
        snd (normalizer snd)
        env (env-gen (perc attack decay) :action FREE)
        snd (rlpf (* env snd snd) cutoff-freq rq)]

    (out out-bus (* amp snd))))

;;(prophet :freq (midi->hz (note :E2)) :amp 3 :decay 5)
;;(prophet :freq (midi->hz (note :C3)) :amp 3 :decay 5)

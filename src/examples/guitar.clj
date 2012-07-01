(ns examples.guitar
  (:use overtone.live))

; Depending on your audio setup (external interfaces, etc...) you might need
; to try a couple different buses before finding what you are looking for.
; Start at index zero and go upward.

(definst guitar
  []
  (sound-in 0))

; Start routing the guitar to the mixer
(guitar)

; Checkout the built-in fx in overtone/studio/fx.clj

; add fx to an instrument chain with inst-fx
(inst-fx guitar fx-distortion2)
(inst-fx guitar fx-reverb2)

; keep an fx instance id if you want to control it later
(def lowpass (inst-fx guitar fx-rlpf))

; adjust the cutoff frequency by sending ctl messages to the fx synth
;(ctl lowpass :cutoff 10000)
;(ctl lowpass :cutoff 1000)
;(ctl lowpass :cutoff 100)

; remove all the fx
;(clear-fx)

; you can't remove or insert fx currently, so you have to clear and add them again

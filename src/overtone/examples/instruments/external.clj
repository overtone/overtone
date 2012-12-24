(ns overtone.examples.instruments.external
  (:use overtone.live))

; Depending on your audio setup (external interfaces, etc...) you might need
; to try a couple different buses before finding what you are looking for.
; Start at index zero and go upward.

(definst external
  []
  (sound-in 0))

; Start routing the external input to the mixer
(external)

; Checkout the built-in fx in overtone/studio/fx.clj

; add fx to an instrument chain with inst-fx
(inst-fx! external fx-distortion2)
(inst-fx! external fx-reverb)

; keep an fx instance id if you want to control it later
(def lowpass (inst-fx! external fx-rlpf))

; adjust the cutoff frequency by sending ctl messages to the fx synth
;(ctl lowpass :cutoff 10000)
;(ctl lowpass :cutoff 1000)
;(ctl lowpass :cutoff 100)

; remove all the fx
(clear-fx external)

; you can't remove or insert fx currently, so you have to clear and add them again
;;(stop)

(ns overtone.sc.cgens.oscillators
  (:use [overtone.sc defcgen ugens]))

(defcgen pm-osc
  "Phase modulation sine oscillator pair."
  [car-freq {:default 0.0 :doc "Carrier frequency"}
   mod-freq {:default 0.0 :doc "Modulation frequency"}
   pm-index {:default 0.0 :doc "Phase modulation index"}
   mod-phase {:default 0.0 :doc "Modulation phase"}]
  (:ar (sin-osc:ar car-freq (* pm-index (sin-osc:ar mod-freq mod-phase))))
  (:kr (sin-osc:kr car-freq (* pm-index (sin-osc:kr mod-freq mod-phase)))))

(defcgen square
  "A square wave generator"
  [freq {:default 440 :doc "Signal frequency"}]
  "A square wave only exists in two states: high and low. This wave
   produces only odd harmonics resulting in a mellow, hollow sound. This
   makes it particularly suitable for emulating wind instruments, adding
   width to strings and pads, or for the creation of deep, wide bass
   sounds."
  (:ar (pulse:ar freq 0.5)))

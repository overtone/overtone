(ns overtone.gui.meter
  (:use (overtone.core synth envelope)))

(def SAMPLE-TIME 0.2) ; How long we should analyze the signal

;(defsynth level-meter [in 0 n-channels 2 update-freq 10]
;  (let [tick (impulse :update-freq)
;        in   (in :in :n-channels)
;        amps (amplitude in 0.2 0.2)
;        peak (peak in (delay1 tick))]
;    (send-reply:ar tick "/level-meter"

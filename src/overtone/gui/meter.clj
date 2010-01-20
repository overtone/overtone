(ns overtone.gui.meter
  (:use (overtone synth envelope)))

;(def SAMPLE-TIME 0.2) ; How long we should analyze the signal
;
;(synth level-meter {:in 0 :n-channels 2 :update-freq 10}
;  (let [tick (impulse.ar :update-freq)
;        in   (in.ar :in :n-channels)
;        amps (amplitude.ar in 0.2 0.2)
;        peak (peak.ar in (delay1.ar tick))]
;    (send-reply.ar tick "/level-meter"
;
;

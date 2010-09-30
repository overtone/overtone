(ns one-forty
  (:use overtone.live))

(refer-ugens)

; Originally by Thor Magnusson
(defsynth thor []
  (let [a (lf-noise0 8)]
    (+ (+ (sin-osc (* (pulse 1) 24))
       (sin-osc (+ 90 (* a 90))))
       (moog-ff (saw (lf-noise0 4 333 666))
                (* a (* 99 (x-line 1 39 99 :free)))))))


; Originally by Julian Rohrhuber
;{ SinOsc.ar( BrownNoise.ar(30!2, 200), Ndef(\x).ar * LFNoise1.kr(1!2,1,1)) }).play;
(synth julian
  (sin-osc (brown-noise [30 30] 200) (+ (lf-noise1:kr [1 1]) 1)))

; Originally by Jose Padovani
; (synth jose [x 165] (sin-osc (* (trig (saw x 1)) x)))
(synth jose []
  (let [x 165
        p (trig (saw x 1))
        y (sin-osc (* p x))
        z (sin-osc p)]
    (free-verb
         (grain-in 2 y (/ y 2) z (* p z) -1))))



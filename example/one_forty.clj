(ns one-forty
  (:use overtone.live))

; Originally by Thor Magnusson
(demo 20
  (let [a (lf-noise0 8)]
    (+ (+ (sin-osc (* (pulse 1) 24))
       (sin-osc (+ 90 (* a 90))))
       (moog-ff (saw (lf-noise0 4 333 666))
                (* a (* 99 (x-line 1 39 99 FREE)))))))

; Originally by Julian Rohrhuber
;{ SinOsc.ar( BrownNoise.ar(30!2, 200), Ndef(\x).ar * LFNoise1.kr(1!2,1,1)) }).play;
(demo 5
  (sin-osc (+ 200 (* [30 30] (brown-noise))) (+ (lf-noise1:kr [1 1]) 1) ))

; Originally by Jose Padovani
(demo 10 (sin-osc (* (trig (saw 165 1)) 165)))

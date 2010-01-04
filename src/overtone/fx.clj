(ns overtone.fx
  (:use (overtone synth)))

;; The goal is to eventually build up a nice library of effects processors.

; An example echo effect in SClang:
;
;z = SynthDef(\src, {|mix = 0.25, room = 0.15, damp = 0.5|
;Out.ar(0,
;FreeVerb.ar(
;Decay.ar(Impulse.ar(1), 0.25, LFCub.ar(1200,0,0.1)), // mono src
;mix, // mix 0-1
;room, // room 0-1
;damp // damp 0-1 duh
;) ! 2 //fan out...
;);
;}).play
;)
;z.set(\room, 0.7)
;z.set(\mix, 0.4)
;z.set(\damp, 0.2)

(def ECHO-DELAY 0.2)
(def ECHO-DECAY 4)

;(defsynth echo [out 0 in 3]
;  (let [in (in :in)
;        echo (comb-n :in 0.5 ECHO-DELAY ECHO-DECAY)]
;    (out :out (pan2 (+ echo :in) 0))))


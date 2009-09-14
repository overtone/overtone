(ns overtone.fx
  (:use (overtone synth)))

;; FX 
(def ECHO-DELAY 0.2)
(def ECHO-DECAY 4)

(defsynth echo
  (let [in (ar "In" 0)]
    (ar "Out" 0 
        (ar "Pan2" 
            (ar "MulAdd" 
                (ar "CombN" in 0.5 ECHO-DELAY ECHO-DECAY)
                1 in)
            0))))

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
;



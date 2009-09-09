(ns synth-test
  (:require [overtone.sc :as sc])
  (:use overtone.sc.synth)
  (:import (de.sciss.jcollider UGen)))

(sc/start)

(defn quick [syn]
  (ar "Out" 0 (ar "MulAdd" syn 0.04 0))) 

(defsynth mouse-saw (quick (ar "LPF" (ar "Saw" [(kr "MouseX" 10 1200 1)
                                                (kr "MouseY" 10 1200 1)]) 120)))
;(play test-saw)
(defsynth env-test (quick (ar "MulAdd" 
                              (ar "SinOsc" 
                                  (kr "Line" 200 100 0.5) 0 )
                              (kr "Line" 0.1 0 1) 0)))
;(play env-test)
;(sc/reset)

(defsynth harmonic-swimming (quick 
  (let [freq     50
        partials 20
        z-init   0
        offset   (kr "Line" 0 -0.02 60)]
    (loop [z z-init
           i 0]
      (if (= partials i) z
        (let [f (kr "max" 0 
                    (kr "MulAdd" 
                        (kr "LFNoise1" 
                            [(+ 2 (* 8 (rand)))
                             (+ 2 (* 8 (rand)))])
                        0.02 offset))
              newz (ar "MulAdd" (ar "FSinOsc" (* freq (+ i 1)) 0) f z)]
          (recur newz (inc i))))))))

;SynthDef("big-kick", { |basefreq = 50, envratio = 3, freqdecay = 0.02, ampdecay = 0.5|
;        var   fenv = EnvGen.kr(Env([envratio, 1], [freqdecay], \exp), 1) * basefreq,
;        aenv = EnvGen.kr(Env.perc(0.005, ampdecay), 1, doneAction:2), out;
;        out = SinOsc.ar(fenv.dup, 0.5pi, aenv);
;        Out.ar([0,1] ,out);
;        }).load(s);

;(defn bk []
;  (let [basefreq 50
;        envratio 3
;        freqdecay 0.02
;        ampdecay 0.5
;        fenv (kr "EnvGen" (UGen/array (ir envratio) (ir 1))
;                          (UGen/array (ir freqdecay) ))]))

;SynthDef(\kick, {|amp= 0.5, decay= 0.1, attack= 0.001, freq= 60|
;        var env, snd;
;        env= EnvGen.kr(Env.perc(attack, decay), doneAction:2);
;        snd= SinOsc.ar(freq, 0, amp);
;        Out.ar(0, Pan2.ar(snd*env, 0));
;        }).store; 

(defsynth simple-kick 
  (let [amp    0.5
        decay  0.1
        attack 0.001
        freq   60]
    (quick (ar "Pan2" (ar "MulAdd" 
                          (ar "SinOsc" freq 0) 
                          (kr "Line" 1 0 0.2) 0)
               0))))

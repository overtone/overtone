(ns synth-test
  (:use overtone.sc)
  (:use clj-backtrace.repl)
  (:import (de.sciss.jcollider UGen)))

;(start "localhost" 57110)
(start)

;(def bass (synth "vintage-bass" {"midin" 50.0, "vel" 0.6}))
;(.set bass "gate" (float 0.0))
;(.sendMsg *s* (.newMsg bass (.asTarget *s*) (into-array ["midin"]) (float-array [40])))

(defn quick [syn]
  (ar "Out" 0 (ar "MulAdd" syn 0.6 0)))

(defsynth mouse-saw (quick (ar "LPF" (ar "Saw" [(kr "MouseX" 10 1200 1)
                                                (kr "MouseY" 10 1200 1)]) 120)))
(play mouse-saw)
(defsynth env-test (quick (ar "MulAdd" 
                              (ar "SinOsc" 
                                  (kr "Line" 200 100 0.5) 0 )
                              (kr "Line" 0.1 0 1) 0)))
(play env-test)

(defsynth env-test (quick 
  (ar "MulAdd" 
      (ar "SinOsc" 440)
      (kr "EnvGen" [ 0, 2, -99, -99, 1, 0.001, 5, -4, 0, 0.3, 5, -4 ]
          (ar "SinOsc" 2)))))

(reset)

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

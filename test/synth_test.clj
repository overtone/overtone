(ns synth-test
  (:use overtone.sc)
  (:use clj-backtrace.repl))

(comment 
(start "localhost" 57110)
(start)
)

(defn quick [syn]
  (ar "Out" 0 (ar "MulAdd" syn 0.8 0)))

(defsynth mouse-saw (quick (ar "LPF" (ar "Saw" [(kr "MouseX" 10 1200 1)
                                                (kr "MouseY" 10 1200 1)]) 120)))
(comment
(trigger mouse-saw {})
(reset)
)

(defsynth line-test (quick (ar "MulAdd" 
                              (ar "SinOsc" 
                                  (kr "Line" 200 100 0.5) 0 )
                              (kr "Line" 0.1 0 1) 0)))
(comment
(trigger line-test {})
)

;(kr "EnvGen" [0 2 -99 -99 1 0.001 5 -4 0 0.3 5 -4] 1 1 0 1 2)

(defsynth env-test 
  (ar "Out" 0 
      (ar "MulAdd" 
          (ar "MulAdd" 
              (ar "SinOsc" 440)
              (kr "EnvGen" 1 1 0 1 2 [0 2 -99 -99 1 0.001 5 -4 0 0.3 5 -4])
              (ar "SinOsc" 2)) 0.8 0)))

(comment
(trigger env-test {})
(reset)
)

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
(comment
(trigger harmonic-swimming {})
(reset)
)

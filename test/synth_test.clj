(ns synth-test
  (:use (overtone sc synth studio))
  (:use clj-backtrace.repl))

(comment 
(start "localhost" 57110)
(start)
)

(defn quick [syn]
  (ar "Out" 0 (ar "MulAdd" syn 0.8 0)))

(defsynth mouse-saw (quick (lpf.ar (saw.ar [(mousex.kr 10 1200 1)
                                                (mousey.kr 10 1200 1)]) 120)))
;(comment
(trigger mouse-saw {})
(reset)

(defsynth line-test (quick (mul-add
                              (sin-osc.ar 
                                  (line.kr 100 600 0.5) 0 )
                              (line.kr 0.1 0 1) 0)))

(comment
(trigger line-test {})
)

;(envgen.kr [0 2 -99 -99 1 0.001 5 -4 0 0.3 5 -4] 1 1 0 1 2)

(defsynth env-test 
  (out.ar 0 
      (mul-add.ar 
          (mul-add.ar 
              (sin-osc.ar 440)
              (env-gen.kr 1 1 0 1 2 [0 2 -99 -99 1 0.001 5 -4 0 0.3 5 -4])
              (sin-osc.ar 2)) 0.8 0)))

(comment
(trigger env-test {})
(reset)
)

(defsynth synthdef-test 
  (out.ar 0 
      (mul-add.ar
          (mul-add.ar 
              (sin-osc.ar 440)
              (env-gen.kr 1 1 0 1 2 [0 2 -99 -99 1 0.001 5 -4 0 0.3 5 -4])
              (sin-osc.ar 2)) 0.8 0)))

(trigger synthdef-test {})

(defsynth harmonic-swimming (quick 
  (let [freq     50
        partials 20
        z-init   0
        offset   (line.kr 0 -0.02 60)]
    (loop [z z-init
           i 0]
      (if (= partials i) z
        (let [f (max.kr 0 
                    (mul-add.kr 
                        (lf-noise-1.kr 
                            [(+ 2 (* 8 (rand)))
                             (+ 2 (* 8 (rand)))])
                        0.02 offset))
              newz (mul-add.ar (f-sin-osc.ar (* freq (+ i 1)) 0) f z)]
          (recur newz (inc i))))))))
(comment
(trigger harmonic-swimming {})
(reset)
)

(defsynth soft-kick 
  (out.ar 0 (pan2.ar
              (mul-add.ar
                (sin-osc.ar 60 (* Math/PI 2))
                (env-gen.ar 1 1 0 1 2 (perc 0.001 0.1))
                0) 
              0)))

(comment
(trigger soft-kick {})
(reset)
)

(ns synth-test
  (:use (overtone sc synth studio pitch rhythm))
  (:use clj-backtrace.repl))

(comment 
(start "localhost" 57110)
(start)
)

(defn quick [signal]
  (syn
    (out.ar 0 (mul-add.ar signal 0.8 0))))

(defsynth mouse-saw (quick (lpf.ar (saw.ar [(mousex.kr 10 1200 1)
                                                (mousey.kr 10 1200 1)]) 120)))
(comment
(trigger mouse-saw {})
(reset)
)

(defsynth line-test (quick (mul-add.ar
                              (sin-osc.ar 
                                  (line.kr 100 600 0.5) 0 )
                              (line.kr 0.1 0 1) 0)))

(comment
(trigger line-test {})
)

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
                (env-gen.ar (perc 0.001 0.1))
                0) 
              0)))

(comment
(trigger soft-kick {})
(reset)
)

(defn build-mix 
  [cur inputs]
  (if (empty? inputs) 
    cur
    (recur (syn (mul-add.ar cur 0.8 (first inputs)))
           (rest inputs))))

(defn mix 
  "Mix any number of input channels down to one."
  [in & chans] (build-mix in chans))

(defn wand [n]
  (mix 
    (syn (sin-osc.ar (mhz (- n octave))))
    (ar "LPF" (ar "Saw" [(mhz n) (mhz (+ n fifth))]) 
        (kr "MulAdd" 
            (kr "SinOsc" 4)
            30
            300))))

(defsynth triangle-test 
  (out.ar 0 
          (mul-add.ar 
            (wand 60)
            (env-gen.kr 1 1 0 1 2 (triangle 3 0.8))
            0)))

(comment
(trigger triangle-test {})
(reset)
)

(defsynth sine-test 
  (out.ar 0 
          (mul-add.ar 
          (wand 67)
;            (saw.ar [440 440])
            (env-gen.kr 1 1 0 1 2 (sine 3 0.8))
            0)))
(comment
(trigger sine-test {})
(reset)
)

(defsynth perc-test 
  (out.ar 0 
          (mul-add.ar 
            (wand 42)
;            (saw.ar [440 440])
            (env-gen.kr 1 1 0 1 2 (perc))
            0)))
(comment
(trigger perc-test {})
(reset)
)
(def m (metronome 105))
(defn bouncer []
  (trigger perc-test {})
  (callback (+ (now) (m)) #'bouncer))

(comment 
(bouncer)
  )

(defn beater []
  (trigger soft-kick {})
  (callback (+ (now) (m)) #'beater))

(comment 
(beater)
(reset)
  )


(defsynth linen-test 
  (out.ar 0 
          (mul-add.ar 
            (saw.ar [440 440])
            (env-gen.kr 1 1 0 1 2 (linen))
            0)))
(comment
(def l (trigger linen-test {}))
(reset)
)

;; TODO: Debug this one...  It should drop off from level to 0.
(defsynth cutoff-test 
  (out.ar 0 
          (mul-add.ar 
            (saw.ar [440 440])
            (env-gen.kr 1 1 0 1 2 (cutoff))
            0)))
(comment
(def l (trigger cutoff-test {}))
(.release l)
(reset)
)

(defsynth dadsr-test 
  (out.ar 0 (pan2.ar 
              (mul-add.ar 
                (saw.ar [440 440])
                (env-gen.kr (sin-osc.ar 1) (dadsr))
                0)
              0)))
(comment
(trigger dadsr-test {})
(reset)
)

(defsynth adsr-test 
  (out.ar 0 (pan2.ar 
              (mul-add.ar 
                (saw.ar [440 440])
                (env-gen.kr (sin-osc.ar 1) (adsr))
                0)
              0)))
(comment
(trigger adsr-test {})
(reset)
)

(defsynth asr-test 
  (out.ar 0 (pan2.ar 
              (mul-add.ar 
                (saw.ar [440 440])
                (env-gen.kr (sin-osc.ar 1) (asr))
                0)
              0)))
(comment
(trigger asr-test {})
(reset)
)

;; Audio Input from Jack inputs starts at 8 & 9
(defsynth audio-in-test
  (out.ar 0 [(in.ar 8)
             (delayc.ar (in.ar 9) ;; Delaying right channel half a second
                        3 0.5)]))

 (comment
(trigger audio-in-test {})
(reset)
   )

;; Playing audio samples (wav files) from disk
(def buf (sample "/home/rosejn/projects/overtone/samples/kit/boom.wav"))
(defsynth audio-sample-test
  (out.ar 0
          (play-buf.ar (.getBufNum buf)
                       1.0
                       (sin-osc.ar 2)
                       0.0
                       1.0)))

 (comment
(trigger audio-sample-test {})
(reset)
   )

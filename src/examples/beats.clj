(ns examples.beats
  (:use overtone.live))

(defn ugen-cents
  "Returns a frequency computed by adding n-cents to freq.  A cent is a
  logarithmic measurement of pitch, where 1-octave equals 1200 cents."
  [freq n-cents]
  (with-overloaded-ugens
    (* freq (pow 2 (/ n-cents 1200)))))

(definst kick
  [freq {:default 56.51 :min 20 :max 400 :step 1}
   attack {:default 0.0001 :min 0.00001 :max 2 :step 0.0001}
   decay  {:default 0.144 :min 0.00001 :max 2 :step 0.0001}
   fattack {:default 0.00001 :min 0.00001 :max 2 :step 0.0001}
   fdecay {:default 0.207 :min 0.00001 :max 2 :step 0.0001}
   noise-attack {:default 0.018 :min 0.00001 :max 2 :step 0.0001}
   noise-decay {:default 0.557 :min 0.00001 :max 2 :step 0.0001}
   amp {:default 0.8 :min 0.01 :max 1 :step 0.01}]
  (let [freq-env (env-gen:kr (perc fattack fdecay))
        wave (lf-tri (- freq (* (- 1 freq-env) (ugen-cents freq (* 38 100)))))
        env  (env-gen:kr (perc attack decay) :action FREE)
        noiz (lpf (* (white-noise) (x-line 1 0.1 noise-decay)) 354)
        snd (+ (* 0.3 noiz) (* env wave))
        dist (clip2 (* 2 (tanh (* 2 (distort (* 2 snd))))) 0.2)
        snd (b-peak-eq snd 55.38 1 36.4)]
    (* amp (+ dist snd))))
;(kick :noise-decay 4)

(definst hat
  [freq   {:default 44.77 :min 20 :max 400 :step 1}
   attack {:default 0.036 :min 0.00001 :max 2 :step 0.0001}
   decay  {:default 0.484 :min 0.00001 :max 2 :step 0.0001}
   rq     {:default 0.08 :min 0.01 :max 1 :step 0.01}
   amp    {:default 0.8 :min 0.01 :max 1 :step 0.01}]
  (let [noiz (white-noise)
        filt (rhpf noiz 4064.78 rq)
        env  (x-line 1 0.001 decay :action FREE)]
    (* amp env filt)))
;(hat)

(definst haziti-clap
  [freq   {:default 44.77 :min 20 :max 400 :step 1}
   attack {:default 0.036 :min 0.00001 :max 2 :step 0.0001}
   decay  {:default 1.884 :min 0.00001 :max 2 :step 0.0001}
   rq     {:default 0.08 :min 0.01 :max 1 :step 0.01}
   amp    {:default 0.8 :min 0.01 :max 1 :step 0.01}]
  (let [noiz (white-noise)
        bfreq (* 400 (abs (lf-noise0 80)))
        filt (* 4 (bpf (rhpf noiz 4064.78 rq) bfreq (* 1 rq)))
        env  (x-line 1 0.001 decay :action FREE)
        wave (lf-tri (* (abs (lf-noise0:kr 699)) 4400))
        wenv (env-gen (perc 0.00001 0.008))
        skip (* wave wenv)]
    (* amp (+ (* env filt) skip))))

;(haziti-clap)

(definst dance-kick
  [freq {:default 50.24 :min 20 :max 400 :step 1}
   attack {:default 0.0001 :min 0.00001 :max 2 :step 0.0001}
   decay  {:default 0.484 :min 0.00001 :max 2 :step 0.0001}
   fattack {:default 0.0001 :min 0.00001 :max 2 :step 0.0001}
   fdecay {:default 0.012 :min 0.00001 :max 2 :step 0.0001}
   amp {:default 0.8 :min 0.01 :max 1 :step 0.01}]
  (let [freq-env (env-gen:kr (perc fattack fdecay))
        wave (sin-osc (+ freq (* 8 freq freq-env)))
        env  (env-gen:kr (perc attack decay) :action FREE)
        src (* env wave)
        dist (clip2 (* 2 (tanh (* 3 (distort (* 1.5 src))))) 0.8)
        eq (b-peak-eq dist 37.67 1 10.4)]
    (* amp eq)))
;(dance-kick)

(definst quick-kick
  [freq {:default 20.0 :min 20 :max 400 :step 1}
   attack {:default 0.0001 :min 0.00001 :max 2 :step 0.0001}
   decay  {:default 0.374 :min 0.00001 :max 2 :step 0.0001}
   fattack {:default 0.001 :min 0.00001 :max 2 :step 0.0001}
   fdecay {:default 0.282 :min 0.00001 :max 2 :step 0.0001}
   amp {:default 0.8 :min 0.01 :max 1 :step 0.01}]
  (let [freq-env (env-gen:kr (perc fattack fdecay))
        wave (sin-osc (+ (* 0.5 freq) (* 14 freq freq-env)))
        env  (x-line:kr 1 0 decay)
        src (* env wave)
        dist (clip2 (* 2 (tanh (* 3 (distort (* 1.5 src))))) 0.8)
        eq (b-peak-eq dist 57.41 1 44)]
    (* amp eq)))
;(quick-kick)

(def m (metronome 128))

(defn player
  [beat]
  (let [next-beat (inc beat)]
    (at (m beat)
        (quick-kick :amp 0.5)
        (if (zero? (mod beat 2))
          (hat :amp 0.1)))
    (at (m (+ 0.5 beat))
        (haziti-clap :decay 0.05 :amp 0.3))

    (when (zero? (mod beat 3))
      (at (m (+ 0.75 beat))
          (hat :decay 0.03 :amp 0.2)))

    (when (zero? (mod beat 8))
      (at (m (+ 1.25 beat))
          (hat :decay 0.03)))

    (apply-at (m next-beat) #'player [next-beat])))

;(player (m))

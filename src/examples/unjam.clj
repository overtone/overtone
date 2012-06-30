(ns examples.unjam
  (:use overtone.live
        [overtone.gui control sequencer mixer info pianoroll]
        [examples.bells :only (ding-dong)]))

(comment
* kick drum (bassy and round)
* percussive hisses (hi-hat)

* warm, round, bass, short attack

* buzzy, melodic voice

* warm pads
* ghostly thin, metallic, pads
  )

(definst kick
  [freq {:default 20.0 :min 20 :max 400 :step 1}
   attack {:default 0.0001 :min 0.00001 :max 2 :step 0.0001}
   decay  {:default 0.374 :min 0.00001 :max 2 :step 0.0001}
   fattack {:default 0.001 :min 0.00001 :max 2 :step 0.0001}
   fdecay {:default 0.282 :min 0.00001 :max 2 :step 0.0001}
   amp {:default 0.8 :min 0.01 :max 1 :step 0.01}]
  (let [freq-env (env-gen:kr (perc fattack fdecay))
        wave (sin-osc (+ (* 0.5 freq) (* 14 freq freq-env)))
        env  (line:kr 1 0 decay FREE)
        src (* env wave)
        dist (clip2 (* 2 (tanh (* 3 (distort (* 1.5 src))))) 0.8)
        eq (b-peak-eq dist 57.41 1 44)]
    (* amp eq)))

(definst quick-kick
  [freq    {:default 40.0 :min 20 :max 400 :step 1}
   attack  {:default 0.0001 :min 0.00001 :max 2 :step 0.0001}
   decay   {:default 0.374 :min 0.00001 :max 2 :step 0.0001}
   fattack {:default 0.001 :min 0.00001 :max 2 :step 0.0001}
   fdecay  {:default 0.282 :min 0.00001 :max 2 :step 0.0001}
   amp     {:default 0.8 :min 0.01 :max 1 :step 0.01}]
  (let [freq-env (env-gen:kr (perc fattack fdecay))
        wave (sin-osc (+ (* 0.5 freq) (* 8 freq freq-env)))
        env  (x-line:kr 1 0 decay FREE)
        src (* env wave)
        dist (clip2 (* 2 (tanh (* 3 (distort (* 1.5 src))))) 0.8)
        eq (b-peak-eq dist 57.41 1 44)]
    (* amp eq)))

(definst o-hat
  [freq   {:default 44.77 :min 20 :max 400 :step 1}
   attack {:default 0.036 :min 0.00001 :max 2 :step 0.0001}
   decay  {:default 0.584 :min 0.00001 :max 2 :step 0.0001}
   rq     {:default 0.08 :min 0.01 :max 1 :step 0.01}
   amp    {:default 0.8 :min 0.01 :max 1 :step 0.01}]
  (let [noiz (white-noise)
        filt (rhpf noiz 4064.78 rq)
        env  (x-line 1 0.001 decay :action FREE)]
    (* amp env filt)))

(definst c-hat
  [freq   {:default 44.77 :min 20 :max 400 :step 1}
   attack {:default 0.006 :min 0.00001 :max 2 :step 0.0001}
   decay  {:default 0.284 :min 0.00001 :max 2 :step 0.0001}
   rq     {:default 0.08 :min 0.01 :max 1 :step 0.01}
   amp    {:default 0.8 :min 0.01 :max 1 :step 0.01}]
  (let [noiz (white-noise)
        filt (rhpf noiz 8064.78 rq)
        env  (x-line 1 0.001 decay :action FREE)]
    (* amp env filt)))

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
;(dance-kick :freq 20 :decay 0.8 :fdecay 0.02)

(definst pot
  [note    {:default 50 :min 10 :max 120 :step 1}
   attack  {:default 0.0001 :min 0.00001 :max 2 :step 0.0001}
   decay   {:default 2.884 :min 0.00001 :max 4 :step 0.0001}
   fattack {:default 0.01 :min 0.00001 :max 2 :step 0.0001}
   fdecay  {:default 0.2 :min 0.00001 :max 4 :step 0.0001}
   amp     {:default 0.8 :min 0.01 :max 1 :step 0.01}]
  (let [freq (midicps note)
        freq-env (env-gen:kr (perc fattack fdecay))
        wave (sin-osc (+ (* 0.5 freq) (* 14 freq freq-env)))
        env  (x-line:kr 1 0 decay FREE)
        src (* env wave)
        dist (clip2 (* 2 (tanh (* 3 (distort (* 1.5 src))))) 0.8)
        eq (b-peak-eq dist freq 1 44)
        echo (comb-n eq 0.5 0.3 4)
        verb (free-verb echo 0.8 0.99 0.5)]
    (* amp verb)))
;(pot)
;(bass)

(definst bass
  [note 40]
  (let [freq (midicps note)
        freq (+ (* (* freq 0.4) (saw 40)) (* 0.5 freq))
        snd  (rlpf (saw freq) (* 8 freq) 0.2)
        snd  (b-peak-eq snd freq 1 40)]
    snd))

(defn offset-cents
  [base-freq cents]
  (with-overloaded-ugens
    (* base-freq (pow 2 (/ cents 1200.0)))))

(definst warm-pad
  [note    {:default 50 :min 10 :max 120 :step 1}
   detune1 {:default -9 :min -100 :max 100 :step 1}
   detune2 {:default 13 :min -100 :max 100 :step 1}
   attack  {:default 0.1 :min 0.00001 :max 30 :step 0.0001}
   decay   {:default 0.8 :min 0.00001 :max 30 :step 0.0001}
   center  {:default 400 :min 10 :max 10000 :step 1}
   sway    {:default 0.2 :min 0.1 :max 20 :step 0.1}]
  (let [freq    (midicps note)
        d1      (offset-cents freq detune1)
        d2      (offset-cents freq detune2)
        s1      (apply + (saw [freq d1 d2]))
        s2      (* 0.1 (sin-osc (* 0.5 freq)))
        env     (env-gen (perc attack decay) :action FREE)
        snd     (* 0.25 env (+ s1 s2))
        cutoff  (+ (* 3 freq) (* 2 freq (sin-osc:kr 0.3)))
        snd     (rlpf snd cutoff 0.4)
        snd     (b-peak-eq snd (+ (* 200 (sin-osc:kr sway)) center) 1 20)
        snd     (* 2 (free-verb snd 0.5 0.5 0.2))]
    snd))
;(synth-controller warm-pad)
;(warm-pad)

(definst glass-pad
  [note    {:default 50 :min 10 :max 120 :step 1}
   attack  {:default 0.0001 :min 0.00001 :max 2 :step 0.0001}
   decay   {:default 2.884 :min 0.00001 :max 3 :step 0.0001}
   center  {:default 400 :min 10 :max 10000 :step 1}
   delay   {:default 0.3 :min 0.00001 :max 1 :step 0.001}]
  (let [freq (midicps note)
        s1 (apply + (lf-tri [freq (* 0.998 freq) (* 1.005 freq)]))
        env (env-gen (perc attack decay) :action FREE)
        snd (* 0.5 env s1)
        snd (b-peak-eq snd (* (x-line:kr 1 0.5 delay) (* 0.98 center)) 1 20)
        snd (rlpf snd (* 1 center) 0.1)
        snd (+ snd (comb-n snd 0.5 delay 0.8))]
        ;snd (* 3 (free-verb snd 0.3 0.8 0.8))]
    (* 0.3 snd)))

;(synth-controller glass-pad)

(definst fm [note 50 divisor {:default 2.0 :min 0.25 :max 40 :step 0.5}
             depth  {:default 1.0 :min 0.0001 :max 200 :step 0.5}
             attack {:default 0.01 :min 0.0001 :max 2 :step 0.001}
             decay  {:default 0.6 :min 0.0001 :max 2 :step 0.001}]
  (let [carrier (midicps note)
        modulator (/ carrier divisor)
        mod-env (env-gen (lin-env 1 0 1))
        amp-env (env-gen (perc attack decay) :action FREE)]
    (* 0.2 amp-env
       (sin-osc (+ carrier
                   (* mod-env  (* carrier depth) (sin-osc modulator)))))))


(defn player [{:keys [cmd note vel] :as event} _]
  (when (= 144 cmd)
    (glass-pad note)))

;(def kb (midi-in "LPK"))
;(midi-handle-events kb #'player)


(def m (metronome 80))
(def step-seq (step-sequencer m 16 [kick quick-kick c-hat o-hat glass-pad warm-pad]))
;(def step (stepinator))

(defn looper
  [beat]
  (let [next-beat (inc beat)]
    (at (m beat)
        (quick-kick :amp 0.95))
    (at (m (+ 0.5 beat))
        (c-hat :decay (choose [0.01 0.1 0.05 0.3]) :amp 0.5))
    (when (= 0 (mod beat 7))
      (at (m (+ 0.5 beat))
          (quick-kick :amp 0.95)))

    (comment when (and (= 0 (mod beat 2))
               (> (rand) 0.05))
      (let [delay (choose [0.25 0.125 0.5 0.25])]
        (at (m (+ delay beat))
            (warm-pad (note (choose [:e3 :c#3]))))
        (when (> (rand) 0.03)
          (at (m (+ (choose [0.26 0.51]) delay beat))
              (warm-pad (note (choose [:c#3 :ab3 :e3])))))))
    (apply-at (m next-beat) #'looper [next-beat])))


;(mixing-console)
;(server-info-window)
;(looper (m))
;(bpm m 85)


(defsynth stab []
  (let [waves (+ (saw 440) (saw 443))
        filt  (rlpf waves (* 6 440) 0.1)
        env   (env-gen (perc 0.01 0.5) :action FREE)
        snd   (* filt env)]
    (out 0 snd)))


(defn definition
  [t dt]
  (at t (glass-pad 50))
  (at (+ t dt) (glass-pad 62))
  (at (+ t dt dt) (glass-pad 54))
  (at (+ t dt dt dt) (glass-pad 57)))


(defn stibidy-stab-stab []
  (at (+ 10 (now)) (stab))
  (at (+ 210 (now)) (stab))
  (at (+ 310 (now)) (stab))
  (at (+ 460 (now)) (stab)))


;;;; Presentation

(definition (+ (now) 10) 500)

(demo 2.5
 (let [bpm 220
       notes [40 41 28 78 28 27 25 35 78]
       trig (impulse:kr (/ bpm 120))
       freq (midicps (lag (demand trig 0 (dseq notes INF)) 0.25))
       swr (demand trig 0 (dseq [1 6 6 2 1 2 4 8 3 3] INF))
       sweep (lin-exp (lf-tri swr) -1 1 40 3000)
       wob (apply + (saw (* freq [0.99 1.01])))
       wob (lpf wob sweep)
       wob (* 0.8 (normalizer wob))
       wob (+ wob (bpf wob 1500 2))
       wob (+ wob (* 0.2 (g-verb wob 9 0.7 0.7)))

       kickenv (decay (t2a (demand (impulse:kr (/ bpm 30)) 0 (dseq [1 0 0 0 0 0 1 0 1 0 0 1 0 0 0 0] INF))) 0.7)
       kick (* (* kickenv 7) (sin-osc (+ 40 (* kickenv kickenv kickenv 200))))
       kick (clip2 kick 1)

       snare (* 3 (pink-noise [1 1]) (apply + (* (decay (impulse (/ bpm 240) 0.5) [0.4 2]) [1 0.05])))
       snare (+ snare (bpf (* 4 snare) 2000))
       snare (clip2 snare 1)]

   (clip2 (+ wob kick snare) 1)))

(ding-dong) ; collaboration
(stop)

;(stibidy-stab-stab) ; ugen tree

;(pot) ; platform


















(def m (metronome 128))

(defn foo [m beat]
  (at (m beat) (kick)
      (when (> (rand) 0.6)
        (warm-pad (choose [60 64 67])))
      (when (zero? (mod beat 4))
        (o-hat)))
  (at (m (+ beat 0.5)) (c-hat))
  (apply-at (m (inc beat)) #'foo [m (inc beat)]))

;(foo m (m))

;(bpm m 100)

;(stop)




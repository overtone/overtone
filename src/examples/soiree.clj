(ns examples.soiree
  (:use overtone.live))

(demo (sin-osc 440))


















(def m (metronome 128))

(definst beep
  [note 60]
  (let [freq (midicps note)
        ;snd  (sin-osc freq)
        snd  (rlpf (saw [freq (* 1.01 freq)]) (* 2 freq) 0.03)
        env  (env-gen (perc 0.001 2.5) :action FREE)]
    (* env snd)))
(beep)

(definst dance-kick
  [freq {:default 110.24 :min 20 :max 400 :step 1}
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

(dance-kick)

(defn player
  [beat]
  (at (m beat)
      (dance-kick))
  (at (m (+ 0.5 beat))
      (hat)
      (beep (choose [40 44 47])))
  (apply-at (m (inc beat)) #'player [(inc beat)]))

(player (m))



(definst round-kick [amp 0.5 decay 0.6 freq 65]
  (* (env-gen (perc 0.01 decay) 1 1 0 1 FREE)
     (sin-osc freq (* java.lang.Math/PI 0.5)) amp))


(round-kick)







(defsynth whoah []
  (let [sound (resonz (saw (map #(+ % (* (sin-osc 100) 1000)) [440 443 437])) (x-line 10000 10 10) (line 1 0.05 10))]
  (out 0 (* (lf-saw:kr (line:kr 13 17 3)) (line:kr 1 0 10) sound))))

(whoah)

(defsynth feedback-loop []
  (let [input (crackle 1.5)
        fb-in (local-in 1)
        snd (+ input (leak-dc (delay-n fb-in 2.0 (* 0.8 (mouse-x 0.001 1.05)))))
        fb-out (local-out snd)
        snd (limiter snd 0.8)]
    (out 0 (pan2 snd))))

(feedback-loop)
(stop)


(definst hat
  [freq   {:default 44.77 :min 20 :max 400 :step 1}
   attack {:default 0.036 :min 0.00001 :max 2 :step 0.0001}
   decay  {:default 1.284 :min 0.00001 :max 2 :step 0.0001}
   rq     {:default 0.08 :min 0.01 :max 1 :step 0.01}
   amp    {:default 0.8 :min 0.01 :max 1 :step 0.01}]
  (let [noiz (white-noise)
        filt (rhpf noiz 4064.78 rq)
        env  (x-line 1 0.001 decay :action FREE)]
    (* amp env filt)))
(hat)

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
(dance-kick)

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
(quick-kick)

; # MIDI
;
; (defn player
;   [event _]
;   (println event))
;
; ;(def mpk (midi-in "MPK"))
;
; ;(midi-handle-events mpk #'player)

(definst ks1-demo
  [note 60 amp 0.8 gate 1]
  (let [freq (midicps note)
        noize (* 0.8 (white-noise))
        dly (/ 1.0 freq)
        plk   (pluck noize gate (/ 1.0 freq) dly
                     (mouse-x 0.1 50)
                     (mouse-y 0.0001 0.9999))
        dist (distort plk)
        filt (rlpf dist (* 12 freq) 0.6)
        reverb (free-verb filt 0.4 0.8 0.2)]
    (* amp (env-gen (perc 0.0001 2) :action FREE) reverb)))

(ks1-demo)

(definst ks-stringer
  [freq 440 rate 6]
  (let [noize (* 0.8 (white-noise))
        trig  (dust rate)
        coef  (mouse-x -0.999 0.999)
        delay (/ 1.0 (* (mouse-y 0.001 0.999) freq))
        plk   (pluck noize trig (/ 1.0 freq) delay 10 coef)
        filt (rlpf plk (* 12 freq) 0.6)]
    (* 0.8 filt)))

(ks-stringer)
(stop)

(definst dubstep [bpm 120 wobble 1 note 50 snare-vol 1 kick-vol 1 v 1]
 (let [trig (impulse:kr (/ bpm 120))
       freq (midicps note)
       swr (demand trig 0 (dseq [wobble] INF))
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

   (* v (clip2 (+ wob (* kick-vol kick) (* snare-vol snare)) 1))))
(dubstep)

(ctl dubstep :wobble 8)
(ctl dubstep :note 40)
(ctl dubstep :bpm 250)
(stop)

(demo 60
 (let [bpm 220
       notes [40 41 28 28 28 27 25 35 78]
       trig (impulse:kr (/ bpm 120))
       freq (midicps (lag (demand trig 0 (dxrand notes INF)) 0.25))
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



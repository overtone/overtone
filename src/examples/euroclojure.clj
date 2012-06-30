(ns examples.euroclojure
  (:use overtone.live
        overtone.gui
        clojure.repl))

(definst foo
  [note 60]
  (let [freq (midicps note)
        snd (apply + (saw [freq (* freq 1.01)]))
        snd (rlpf snd (* 4 freq) 0.4)
        env (env-gen (perc 0.001 0.3) :action FREE)]
    (* snd env)))
(stop)

(defn foo-player
  [t dur]
  (let [next-t (+ t dur)]
    (at t
        (foo (choose (scale :a4 :minor))))
    (apply-at next-t #'foo-player [next-t dur])))
(foo-player (now) 500)





;;;;;;;;;;;;;;;;;;;;;;
; Building Instruments
;;;;;;;;;;;;;;;;;;;;;;


(definst hello
  [note 60]
  (let [freq (midicps note)
        snd  (apply + (saw [freq (* 0.99 freq)]))
        snd  (rlpf snd (* 2 freq) 0.2)
        env  (env-gen (perc 0.1 0.2) :action FREE)]
    (* env snd)))

(hello)

(def m (metronome 128))

(defn hello-player
  [t dur]
  (let [next-t (+ dur t 200)
        note (choose (scale :Bb2 :minor))]
    (at t
        (kick)
        (hello note))
    (when (> (rand) 0.7)
      (at (+ t (* 0.5 dur))
          (hello (+ note 4))))
    (at (+ t (* 0.25 dur))
        (hat))
  (apply-at next-t #'hello-player [next-t dur])))

;(hello-player (now) 400)

;;;;;;;;;;;;;;;;;;;;;;;;;;
; Percussive Instruments
;;;;;;;;;;;;;;;;;;;;;;;;;;

(definst hat
  [attack {:default 0.0001 :min 0.00001 :max 2    :step 0.0001}
   decay  {:default 0.284  :min 0.00001 :max 2    :step 0.0001}
   cutoff {:default 4000   :min 20      :max 8000 :step 1}
   rq     {:default 0.8    :min 0.01    :max 1    :step 0.01}
   amp    {:default 0.5    :min 0.01    :max 1    :step 0.01}]
  (let [noiz (white-noise)
        filt (rhpf noiz cutoff rq)
        env  (env-gen (perc attack decay 1 -3) :action FREE)]
    (* amp env filt)))
(hat :decay 0.8)


















(definst quick-kick
  [freq    {:default 20.0 :min 20 :max 400 :step 1}
   attack  {:default 0.0001 :min 0.00001 :max 2 :step 0.0001}
   decay   {:default 0.374 :min 0.00001 :max 2 :step 0.0001}
   fattack {:default 0.001 :min 0.00001 :max 2 :step 0.0001}
   fdecay  {:default 0.282 :min 0.00001 :max 2 :step 0.0001}
   amp {:default 0.8 :min 0.01 :max 1 :step 0.01}]
  (let [freq-env (env-gen:kr (perc fattack fdecay))
        wave (sin-osc (+ (* 0.5 freq) (* 14 freq freq-env)))
        env  (x-line:kr 1 0 decay)
        src (* env wave)
        dist (clip2 (* 2 (tanh (* 3 (distort (* 1.5 src))))) 0.8)
        eq (b-peak-eq dist 57.41 1 44)]
    (* amp eq)))
(quick-kick)








(definst clap
  [low {:default 7500 :min 100 :max 10000 :step 1}
   hi  {:default 1500 :min 100 :max 10000 :step 1}
   amp {:default 0.3 :min 0.001 :max 1 :step 0.01}
   decay {:default 0.6 :min 0.1 :max 0.8 :step 0.001}]
  (let [noise      (bpf (lpf (white-noise) low) hi)
        clap-env   (line 1 0 decay :action FREE)
        noise-envs (map #(envelope [0 0 1 0] [(* % 0.01) 0 0.04]) (range 8))
        claps      (apply + (* noise (map env-gen noise-envs)))]
    (* claps clap-env)))

(overtone.gui.mixer/mixer hat quick-kick clap)
;(overtone.gui.sequencer/step-sequencer m 8 [hat quick-kick clap])

;(synth-controller hat)
;(synth-controller quick-kick)
;(synth-controller clap)

(definst feedback-loop
  [amp 0.3]
  (let [input  (crackle 1.5)
        fb-in  (local-in 1)
        snd    (+ input (leak-dc (delay-n fb-in 2.0 (* 0.8 (mouse-x 0.001 1.05)))))
        fb-out (local-out snd)
        snd    (limiter snd 0.8)
        snd    (rlpf snd (mouse-y 10 20000) 0.3)]
    (* amp snd)))

(feedback-loop)
(stop)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Midi controls
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn offset-cents
  [base-freq cents]
  (with-overloaded-ugens
    (* base-freq (pow 2 (/ cents 1200.0)))))

; bank-A: {:chan 0, :cmd 176, :note [12-15, 22-29], :vel 0-127, :data1 22, :data2 0}
;
;       22         23         24         25
;     \ | /      \ | /      \ | /      \ | /
;     - O -      - O -      - O -      - O -
;     / | \      / | \      / | \      / | \
;
;    attack      decay     sustain    release
;
;
;       26         27         28         29
;     \ | /      \ | /      \ | /      \ | /
;     - O -      - O -      - O -      - O -
;     / | \      / | \      / | \      / | \
;
;  cutoff-mul  cut-amt    resonance  filt-decay
;
;
;       12         13         14         15
;     \ | /      \ | /      \ | /      \ | /
;     - O -      - O -      - O -      - O -
;     / | \      / | \      / | \      / | \
;
;     sub-amt     lfo      lfo-amt    spread
;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ding-mapping
  {22 [:a #(* 0.3 (/ % 127.0))]
   23 [:d #(* 0.6 (/ % 127.0))]
   24 [:s #(/ % 127.0)]
   25 [:r #(/ % 127.0)]

   26 [:cutoff-mul #(* 10 (/ % 127.0))]
   27 [:cut-amount #(/ % 127.0)]
   28 [:resonance  #(+ 0.0001 (- 1 (/ % 127.0)))]
   29 [:filt-decay #(* 0.3 (/ % 127.0))]

   12 [:sub-amt    #(+ 0.001 (/ % 127.0))]
   13 [:lfo        #(* 30 (/ % 127.0))]
   14 [:lfo-amt    #(* 60 (/ % 127.0))]
   15 [:spread     #(* 1 %)]
   })

(def ding-state (atom {}))

(defn state-player
  [inst state-atom note velocity]
  (apply inst note velocity (flatten (seq @state-atom))))

(definst ding
  [note 60 velocity 100 gate 1
   a 0.001 d 0.1 s 0.5 r 0.3
   spread 3 sub-amt 0.3 lfo 0.0001 lfo-amt 0.0001
   filt-decay 0.3 cutoff-mul 3 cut-amount 0.5 resonance 0.3
   delay-t 0.25 decay-t 0.5]
  (let [freq (midicps note)
        freq (+ freq (* (lf-tri:kr lfo) lfo-amt))
        amp  (/ velocity 127.0)
        snd  (apply + (saw [freq (offset-cents freq spread)]))
        sub  (* sub-amt (sin-osc (* 0.5 freq)))
        cut-freq (* cutoff-mul freq)
        cut  (+ (* (- 1 cut-amount) cut-freq)
                (* cut-amount cut-freq (cutoff filt-decay)))
        filt (rlpf snd cut resonance)
        env  (env-gen (adsr a d s r) gate :action FREE)
        snd  (* amp env (+ filt sub))]
    snd))

(do
  (remove-event-handlers [:midi :control-change])
  (midi-inst-controller ding-state (partial ctl ding) ding-mapping)
  (def dinger (midi-poly-player (partial state-player ding ding-state))))


; Create a buffer to use for our wavetable
(def b (buffer 1024))
(waveform-editor b true)

(definst table-player
  [buf 0 freq 440]
  (let [snd (osc buf freq)]
    snd))

(table-player b)
(stop)

(definst padz
  [buf 0 freq 440 t 10 amt 0.5 amp 0.9]
  (let [f-env      (env-gen (perc t t) 1 1 0 1 FREE)
        src        (osc buf [freq (* freq 1.01)])
        signal     (rlpf (* 0.3 src)
                         (+ (* 0.6 freq) (* f-env 2 freq)) 0.2)
        k          (/ (* 2 amt) (- 1 amt))
        distort    (/ (* (+ 1 k) signal) (+ 1 (* k (abs signal))))
        gate       (pulse (* 2 (+ 1 (sin-osc:kr 0.05))))
        compressor (compander distort gate 0.01 1 0.5 0.01 0.01)
        dampener   (+ 1 (* 0.5 (lf-tri:kr 0.5)))
        reverb     (free-verb compressor 0.5 0.5 dampener)
        echo       (comb-n reverb 0.4 0.3 0.5)]
    (* amp echo)))

(padz b)

;  * parameters used in synthdefs
(definst step-pad
  [buf 0 note 60 amp 0.7 attack 2.009 release 1.6]
  (let [freq  (midicps note)
        env   (env-gen (perc attack release) :action FREE)
        f-env (+ freq (* 3 freq (env-gen (perc 0.012 (- release 0.1)))))
        bfreq (/ freq 2)
        sig   (apply +
                     (concat (* 0.7 (osc b [bfreq (* 0.99 bfreq)]))
                             (lpf (osc b [freq (* freq 1.01)]) f-env)))
        audio (* amp env sig)]
    audio))
(step-pad b)
;(stop)


(defn pad-player
  [t dur]
  (let [next-t (+ dur t 200)
        note (choose (scale :Bb2 :minor))]
    (at t
        (step-pad b note))
  (apply-at next-t #'pad-player [next-t dur])))
(pad-player (now) 400)
(stop)





; Bring up a monophonic step sequencer
(def pstep (stepinator))

; You can access the sequence once when creating a synth
(demo 10
  (let [note (duty (dseq [0.2 0.1] INF)
                   0
                   (dseq (map #(+ 60 %) (:steps @(:state pstep))) INF))
        src (sin-osc (midicps note))]
    (* [0.2 0.2] src)))
(stop)

; or access the sequence steps in a player function
(defn step-player [b]
  (at (m b)
      (step-pad b (+ 60 (nth (:steps @(:state pstep)) (mod b 16)))))
  (apply-at (m (inc b)) #'step-player [(inc b)]))


(step-player (m))

(stop)

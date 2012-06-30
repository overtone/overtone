(ns examples.punk
  (:use [overtone live gui]
        [clojure.core.match :only [match]]))

(def m (metronome 85))

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

(synth-controller pot)
(piano-roll m pot)

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
   sway    {:default 0.2 :min 0.1 :max 20 :step 0.1}
   fdecay  {:default 0.5 :min 0.01 :max 2 :step 0.01}
   cutoff  {:default 20000 :min 10 :max 20000 :step 1}
   reverb  {:default 20000 :min 10 :max 20000 :step 1}
   gate    {:default 1 :min 0 :max 1 :step 1}]
  (let [freq    (midicps note)
        d1      (offset-cents freq detune1)
        d2      (offset-cents freq detune2)
        s1      (apply + (saw [freq d1 d2]))
        s2      (* 0.1 (sin-osc (* 0.5 freq)))
        env     (env-gen (perc attack decay) gate :action FREE)
        snd     (* 0.25 env (+ s1 s2))
        cutoff  (* cutoff (x-line 1 0 fdecay))
        snd     (rlpf snd 20000 0.4)
        snd     (b-peak-eq snd (+ (* 200 (sin-osc:kr sway)) center) 1 20)
        snd     (* 2 (free-verb snd 0.3 0.5 0.3))]
    snd))


(warm-pad)
(synth-controller warm-pad)
(piano-roll m warm-pad)

(def mpk (midi-in "MPK"))

;(defn gated
;  [g-node]
;  (add-watch (:status g-node) ::gate-off
;             (fn [_ _ _ state]
;               (if (= state :destroyed)))


(def device-map {})

(defn handler
  [{:keys [cmd note vel] :as event} ts]
  (cond
    (= :note-on cmd) (warm-pad :note note)
    (= :note-off cmd) ())

  {:chan 0, :cmd :note-on, :orig-cmd :note-on, :note 48, :vel 55, :data1 48, :data2 55, :status :note-on}
  (println event))


(midi-handle-events mpk #'handler)

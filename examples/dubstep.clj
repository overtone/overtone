(ns examples.dubstep
  (:use [overtone.core]))

;; Dan Stowells' Dubstep Synth:
;; SClang version:
;;
;;s.waitForBoot{Ndef(\a).play;Ndef(\a,
;;{
;;var trig, freq, notes, wob, sweep, kickenv, kick, snare, swr, syn, bpm, x;
;;x = MouseX.kr(1, 4);
;;
;;
;;// START HERE:
;;
;;bpm = 120;
;;
;;notes = [40, 41, 28, 28, 28, 28, 27, 25, 35, 78];
;;
;;trig = Impulse.kr(bpm/120);
;;freq = Demand.kr(trig, 0, Dxrand(notes, inf)).lag(0.25).midicps;
;;swr = Demand.kr(trig, 0, Dseq([1, 6, 6, 2, 1, 2, 4, 8, 3, 3], inf));
;;sweep = LFTri.ar(swr).exprange(40, 3000);
;;
;;
;;// Here we make the wobble bass:
;;wob = Saw.ar(freq * [0.99, 1.01]).sum;
;;wob = LPF.ar(wob, sweep);
;;wob = Normalizer.ar(wob) * 0.8;
;;wob = wob + BPF.ar(wob, 1500, 2);
;;wob = wob + GVerb.ar(wob, 9, 0.7, 0.7, mul: 0.2);
;;
;;
;;// Here we add some drums:
;;kickenv = Decay.ar(T2A.ar(Demand.kr(Impulse.kr(bpm / 30),0,Dseq([1,0,0,0,0,0,1,0, 1,0,0,1,0,0,0,0],inf))),0.7);
;;kick = SinOsc.ar(40+(kickenv*kickenv*kickenv*200),0,7*kickenv).clip2;
;;snare = 3*PinkNoise.ar(1!2)*Decay.ar(Impulse.ar(bpm / 240, 0.5),[0.4,2],[1,0.05]).sum;
;;snare = (snare + BPF.ar(4*snare,2000)).clip2;
;;
;;// This line actually outputs the sound:
;;(wob + kick + snare).clip2;
;;
;;})}
;;
;; Directly translated to Overtone:

(demo 60
 (let [bpm 120
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

(defsynth dubstep [bpm 120 wobble 1 note 50 snare-vol 1 kick-vol 1 v 1]
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

   (out 0    (* v (clip2 (+ wob (* kick-vol kick) (* snare-vol snare)) 1)))))



(comment
  ;;Control the dubstep synth with the following:
  (dubstep)
  ;=> 25 ; synth ID to use below
  (ctl 25 :wobble 8)
  (ctl 25 :note 40)
  (ctl 25 :bpm 250)
  (stop)
  )


(comment
  ;;For connecting with a monome to control the wobble and note
  (require '(polynome [core :as poly]))
  (def m (poly/init "/dev/tty.usbserial-m64-0790"))
  (def notes (reverse [25 27 28 35 40 41 50 78]))
  (poly/on-press m (fn [x y s]
                   (do
                     (let [wobble (inc y)
                           note (nth notes x)]
                       (println "wobble:" wobble)
                       (println "note:" note)
                       (poly/clear m)
                       (poly/led-on m x y)
                       (ctl dubstep :wobble wobble)
                       (ctl dubstep :note note)))))
  (poly/disconnect m))

(comment
  ;;For connecting with a monome to drive two separate dubstep bass synths
  (do
    (require '(polynome [core :as poly]))
    (def m (poly/init "/dev/tty.usbserial-m64-0790"))
    (def curr-vals (atom {:b1 [0 0]
                          :b2 [5 0]}))
    (def curr-vol-b1 (atom 1))
    (def curr-vol-b2 (atom 1))

    (at (+ 1000 (now))
        (def b1 (dubstep))
        (def b2 (dubstep)))

    (defn swap-vol
      [v]
      (mod (inc v) 2))

    (defn fetch-note
      [base idx]
      (+ base (nth-interval :minor-pentatonic idx)))

    (defn relight
      []
      (poly/clear m)
      (apply poly/led-on m (:b1 @curr-vals))
      (apply poly/led-on m (:b2 @curr-vals)))

    (defn low-bass
      [x y]
      (println "low" [x y])
      (if (= [x y]
             (:b1 @curr-vals))
        (ctl b1 :v (swap! curr-vol-b1 swap-vol))
        (do
          (ctl b1 :wobble (inc x) :note (fetch-note 20 y))
          (swap! curr-vals assoc :b1 [x y])))
      (relight))

    (defn hi-bass
      [x y]
      (println "hi" [x y])
      (if (= [x y]
             (:b2 @curr-vals))
        (ctl b2 :v (swap! curr-vol-b2 swap-vol))
        (do
          (ctl b2 :wobble (- x 3) :note (fetch-note 40 y))
          (swap! curr-vals assoc :b2 [x y])))
      (relight))

    (poly/on-press m (fn [x y s]
                       (if (< x 4)
                         (apply #'low-bass [x y])
                         (apply #'hi-bass [x y]))))

    (poly/on-press m (fn [x y s]
                       (poly/toggle-led m x y))))
)

;;(stop)

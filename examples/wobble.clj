(ns examples.wobble
  (:use [overtone.live]))

(defcgen wobble
  "wobble an input source to a specified wobble val (which is designed
  to be remotely modulated"
  [src {:doc "input source"}
   wobble-amount {:doc "Amount of wobble to apply (-1 to 1)" :default 0}
   min {:doc "minimum freq to always let through" :default 40}
   max {:doc "maximum freq to let through the wobble" :default 3000}]
  (:ar
   (let [scaled-wob (lin-exp (lf-tri wobble-amount) -1 1 min max)
         wob        (lpf src scaled-wob)
         wob        (* 0.8 (normalizer wob))
         wob        (+ wob (bpf wob 1500 2))]
     (+ wob (* 0.2 (g-verb wob 9 0.7 0.7))))))

(defcgen auto-wobble
  "wobble an input src with a specified number of wobbles per second"
  [src {:doc "input source"}
   wobble-factor {:doc "num wobbles per second"}]
  (:ar
   (let [sweep (lin-exp (lf-tri wobble-factor) -1 1 40 3000)
         wob   (lpf src sweep)
         wob   (* 0.8 (normalizer wob))
         wob   (+ wob (bpf wob 1500 2))]
     (+ wob (* 0.2 (g-verb wob 9 0.7 0.7))))))

(demo 1 [(sin-osc 50) (sin-osc 50)])

(demo 3 (saw (* 50 [0.99 1.01])))

(demo 3
      (auto-wobble
       (apply + (saw (* 50 [1.01 0.99]))) 5))
(stop)

(ns examples.monomestep
  (:use [overtone.live]
        [polynome.core]))

(definst dubstep [note 40 wob 3 hi-man 0 sweep-man 0 deci-man 0 tan-man 0 shape 0 max-freq 3000]
  (let [shape (select shape [(lf-saw wob) (lf-tri wob)])
        sweep (lin-exp shape -1 1 40 max-freq)
        snd   (mix (saw (* note [0.99 1.01])))
        snd   (lpf snd sweep)
        snd   (normalizer snd)
        snd   (+ snd (bpf snd 1500 2))
        ;; ;;special flavours
        ;; ;;hi manster
        ;; snd   (select (> hi-man 0.05) [snd (* 4 (hpf snd 1000))])

        ;; ;;sweep manster
        ;; snd   (select (> sweep-man 0.05) [snd (* 4 (hpf snd sweep))])

        ;; ;;decimate
        ;; snd   (select (> deci-man 0.05) [snd (round snd 0.1)])

        ;; ;;crunch
        ;; snd   (select (> tan-man 0.05) [snd (tanh (* snd 5))])
        snd   (+ snd (* 0.3 (g-verb snd 10 0.7 0.7)))]

    snd))


(dubstep)
(stop)

(ctl dubstep :wob 2)
(ctl dubstep :shape 1)




(defcgen wobble
  "wobble an input src"
  [src {:doc "input source"}
   wobble-factor {:doc "num wobbles per second"}]
  (:ar
   (let [sweep (lin-exp (lf-tri wobble-factor) -1 1 40 3000)
         wob   (lpf src sweep)
         wob   (* 0.8 (normalizer wob))
         wob   (+ wob (bpf wob 1500 2))]
     (+ wob (* 0.2 (g-verb wob 9 0.7 0.7))))))

(demo 5 (wobble (lf-saw (* 80 [0.99 1.01])) 2))

(demo 0.1 (dubstep))
(demo (sin-osc))
(boot-server)

(stop)

(do (dubstep) (Thread/sleep 200) (kill dubstep))

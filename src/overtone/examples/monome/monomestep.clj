(ns ^:hw overtone.examples.monome.monomestep
  (:use [overtone.live]
        [clojure.core.match :only [match]]
        [polynome.core :as poly]))

(defonce dub-vol (atom 1))

(definst dubstep [note 40 wob 2 hi-man 0 lo-man 0 sweep-man 0 deci-man 0 tan-man 0 shape 0 sweep-max-freq 3000 hi-man-max 1000 lo-man-max 500 beat-vol 0 amp 1]
  (let [bpm 300
        shape (select shape [(lf-tri wob) (lf-saw wob)])
        sweep (lin-exp shape -1 1 40 sweep-max-freq)
        snd   (mix (saw (* (midicps note) [0.99 1.01])))
        snd   (lpf snd sweep)
        snd   (normalizer snd)

        snd   (+ snd (bpf snd 1500 2))
        ;;special flavours
        ;;hi manster
        snd   (select (> hi-man 0.05) [snd (* 4 (hpf snd hi-man-max))])

        ;;sweep manster
        snd   (select (> sweep-man 0.05) [snd (* 4 (hpf snd sweep))])

        ;;lo manster
        snd   (select (> lo-man 0.05) [snd (lpf snd lo-man-max)])

        ;;decimate
        snd   (select (> deci-man 0.05) [snd (round snd 0.1)])

        ;;crunch
        snd   (select (> tan-man 0.05) [snd (tanh (* snd 5))])

        snd   (* 0.5 (+ (* 0.8 snd) (* 0.3 (g-verb snd 100 0.7 0.7))))

               kickenv (decay (t2a (demand (impulse:kr (/ bpm 30)) 0 (dseq [1 0 0 0 0 0 1 0 1 0 0 1 0 0 0 0] INF))) 0.7)
       kick (* (* kickenv 7) (sin-osc (+ 40 (* kickenv kickenv kickenv 200))))
       kick (clip2 kick 1)

       snare (* 3 (pink-noise) (apply + (* (decay (impulse (/ bpm 240) 0.5) [0.4 2]) [1 0.05])))
       snare (+ snare (bpf (* 4 snare) 2000))
        snare (clip2 snare 1)
       beat (* beat-vol (+ kick snare))
        ]
    (* amp (+ (pan2 snd shape) beat))))

(def m (poly/init "/dev/tty.usbserial-m64-0790"))
;;(def m beatbox.core/m)
;;(poly/disconnect m)
(poly/remove-all-callbacks m)

(def id->dub-ctl {0 :hi-man
                  1 :lo-man
                  2 :deci-man
                  3 :tan-man
                  4 :beat-vol})

(defn toggle-vol
  []
  (ctl dubstep :amp (swap! dub-vol #(mod (inc %) 2))))

(defn toggle-fx
  [x y]
  (when-let [ctl-name (get id->dub-ctl y)]
    (poly/toggle-led m x y)
    (let [val (poly/led-activation m x y)]
      (ctl dubstep ctl-name val))))

(defn modulate-pitch-wob
  [x y]
  (let [wob x
        note (nth (scale :g1 :minor-pentatonic) y)]
    (ctl dubstep :note note)
    (ctl dubstep  :wob wob)))

(poly/on-press m ::foo (fn [x y s]
                         (match [x y]
                           [0 7] (toggle-vol)
                           [0 _] (toggle-fx x y)
                           [_ _] (modulate-pitch-wob x y))))

(dubstep)
;;(stop)
(comment

  (ctl dubstep :lo-man-max 1000)
  (ctl dubstep :hi-man-max 400)
  (ctl dubstep :sweep-max-freq 3000)
  (ctl dubstep :note 30)
  (stop)
  )
;;(poly/disconnect m)

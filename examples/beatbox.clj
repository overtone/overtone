(ns beatbox.core
  (:use [overtone.live])
  (:require [polynome.core :as poly]))

;;design a sc synth to play the samples
(definst loop-synth [buf 0 vol 1 rate 1 amp 1 wet-dry 0.2 room-size 0  dampening 1]
  (let [src (play-buf 1 buf rate 1.0 0.0 1.0 1)]
    (* src vol amp)))

;;change m to point to your monome (use dummy if you don't have one...)
(defonce m (poly/init "/dev/tty.usbserial-m64-0790"))
;;(def m (poly/init "/dev/tty.usbserial-m128-115"))
;;(def m (poly/init "dummy"))

(def samples (load-samples "assets/*.{aif,AIF,wav,WAV}"))

(defn start-samples
  "Starts all samples playing at init-vol. Returns a seq containing info
  regarding all running samples. Samples start playing 1s after the this
  fn is called to ensure that they're all started in sync"
  (let [init-vol 0]
    (at (+ 1000 (now))
        (doall
         (reduce (fn [res samp]
                   (let [id (loop-synth samp init-vol)]
                     (assoc res id {:vol init-vol
                                    :id
                                    :samp samp})))
                 []
                 samples)))))

(def playing-samples* (agent (start-samples)))

(defn reset-samples!
  (send playing-samples*
        (fn [playing-samples])
        (doall (map #(kill (:id %)) playing-samples))
        (start-samples)))

(defn toggle
  "Invert the vol from 1 to 0 or 0 to 1"
  [vol]
  (mod (inc vol) 2))

(defn toggle-sample
  [n]
  (send playing-samples* (fn [playing-samples]
                           (let [samp (nth playing-samples n)
                                 id (:id samp)
                                 vol (:vol samp)
                                 new-vol (toggle vol)]
                             (ctl id :vol new-vol))
)))

(defn change-vol
  "Update vol with update-fn and change led state and loop vol accordingly"
  [vol update-fn looper x y]
  (let [new-vol (update-fn vol)
        synth   (looper :synth)
        path    (looper :path)]
    (ctl synth :vol new-vol)
    (poly/led m x y new-vol)
    (if (= 1 new-vol)
      (println "Playing " path)
      (println "Stopping" path))
    new-vol))

(defn trigger
  "Invert the volume for the loop corresponding to the given x y coords. Also
   update the associated agent's state and monome LED state."
  [x y]
  (let [looper (get loopers [x y])
        state (looper :state)]
    (send state change-vol toggle looper x y)))

(defn mute
  "Silences all loopers."
  []
  (doseq [[coords looper] loopers] (apply send (looper :state) change-vol silence looper coords )))

(poly/on-press m (fn [x y s] (trigger x y)))

(comment
  ;;play about with synth params:
  (def rate (atom 1))
  (ctl loop-synth :rate 1)
  (ctl loop-synth :amp 0.1)
  (ctl loop-synth :room-size 1)
  (ctl loop-synth :dampening 1)
  (ctl loop-synth :wet-dry 0.1)

  (defn tempo-slide [to]
    (let [from @rate
          step (if (< from to) 0.01 -0.01)
          vals (range from to step)]
      (doall (map #(do (ctl loop-synth :rate (reset! rate %)) (Thread/sleep 35)) vals))))

  (ctl loop-synth :rate 1)

  (tempo-slide 1)

  (comment
    (def s (space/space-navigator))

    (space/on-diff-vals s
                        (fn [vals]
                          (ctl looper :amp (:rz vals)))
                        {:min-rz 0
                         :max-rz 1}
                        space/SAMPLE-RANGES)

    (space/on-diff-vals s
                        (fn [vals]
                          (ctl looper :wet-dry (:x vals)))
                        {:min-x 0
                         :max-x 1}
                        space/SAMPLE-RANGES)

    (space/on-diff-vals s
                        (fn [vals]
                          (ctl looper :room-size (:z vals)))
                        {:min-z 0
                         :max-z 1}
                        space/SAMPLE-RANGES)

    (space/on-diff-vals s
                        (fn [vals]
                          (ctl looper :dampening (:y vals)))
                        {:min-y 0
                         :max-y 1}
                        space/SAMPLE-RANGES)))

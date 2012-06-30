(ns examples.intro-sample-looper
  (:use [overtone.live])
  (:require [polynome.core :as poly]))

;;(def loop-group (group))

;;design a sc synth to play the samples
(defsynth loop-synth [buf 0 vol 1 rate 1 loudness-buf 0]
  (let [src (play-buf 1 buf rate 1.0 0.0 1.0 1)
        snd (* src vol)]
;;    (tap "loop-vol" 10 (loudness (fft loudness-buf snd)))
    (out 0 (pan2 snd))))

;;change m to point to your monome (use dummy if you don't have one...)
(def m (poly/init "/dev/tty.usbserial-m64-0790"))

;;(def m (poly/init "/dev/tty.usbserial-m128-115"))
;;(def m (poly/init "dummy"))

(defonce samples (load-samples "/Users/sam/Development/improcess/apps/scratch/assets/*.{aif,AIF,wav,WAV}"))

;;(def samples (load-samples "~/Desktop/devil/shots/*.wav"))

(def bufs (doall (for [ _ (range 64)] (buffer 1024))))

(defn start-samples
  "Starts all samples playing at init-vol. Returns a seq containing info
  regarding all running samples. Samples start playing 1s after the this
  fn is called to ensure that they're all started in sync"
  []
  (at (+ 1000 (now))
      (doall
       (reduce (fn [res [idx samp]]
                 (let [loudness-buf (nth bufs idx)
                       synthi (loop-synth  samp 0 1 loudness-buf)]
                   (conj res  {:vol 0
                               :synthi synthi
                               :idx idx
                               :samp samp})))
               []
               (into [] (zipmap (range) samples))))))

(def playing-samples* (agent (start-samples)))
;;(ctl (:synthi (nth @playing-samples* 20)) :vol 0.5)
;;(ctl (:synthi (nth @playing-samples* 1)) :vol 0)
(def sample-vols (doall (map #(get (:taps (:synthi %))"loop-vol") @playing-samples* )))

(defn reset-samples!
  []
  (send playing-samples*
        (fn [playing-samples]
          (doall (map #(kill (:id %)) playing-samples))
          (poly/clear m)
          (start-samples))))

(defn toggle
  "Invert the vol from 1 to 0 or 0 to 1"
  [vol]
  (mod (inc vol) 2))

(defn toggle-sample
  [n]
  (if (< n (count @playing-samples*))
    (send playing-samples* (fn [playing-samples]
                           (let [samp     (nth playing-samples n)
                                 id       (:synthi samp)
                                 new-vol  (toggle (:vol samp))
                                 new-samp (assoc samp :vol new-vol)]
                             (ctl id :vol new-vol)
                             (assoc playing-samples n new-samp))))
      false))

(defn trigger
  "Invert the volume for the loop corresponding to the given x y coords. Also
   update the associated agent's state and monome LED state."
  [x y]
  (when (toggle-sample (poly/button-id m x y))
    (poly/toggle-led m x y)))

(def fx-id (atom 0))

(poly/on-press m ::triggerer (fn [x y s] (if (and  (= 7 y)
                                                  (not (= 7 x)))
                                          (reset! fx-id (mod x 7))
                                          (trigger x y))))

(def rate* (atom 1))


(def b (buffer 1024))

(defsynth vol-relay
  []
  (tap "trig-vol" 30 (* 0.5 (loudness (fft b (in 0))))))

(def relay (vol-relay :pos :after  :tgt 5 ))


(defn tempo-slide [to]
  (let [from @rate*
        step (if (< from to) 0.01 -0.01)
        vals (range from to step)]
    (dorun (map #(do (ctl 5  :rate (reset! rate* %)) (Thread/sleep 35)) vals))))

;;(tempo-slide  1)
;;(volume 1.5)
;;(reset-samples!)
;;(trigger 1 1)
;;(poly/remove-all-callbacks m)
;;(poly/disconnect m)

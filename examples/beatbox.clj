(ns beatbox.core
  (:use [overtone.live])
  (:require [polynome.core :as poly]))

;;design a sc synth to play the samples
(definst loop-synth [buf 0 vol 1 rate 1]
  (let [src (play-buf 1 buf rate 1.0 0.0 1.0 1)]
    (* src vol)))

;;change m to point to your monome (use dummy if you don't have one...)
(defonce m (poly/init "/dev/tty.usbserial-m64-0790"))
;;(def m (poly/init "/dev/tty.usbserial-m128-115"))
;;(def m (poly/init "dummy"))

(defonce samples (load-samples "assets/*.{aif,AIF,wav,WAV}"))

(defn start-samples
  "Starts all samples playing at init-vol. Returns a seq containing info
  regarding all running samples. Samples start playing 1s after the this
  fn is called to ensure that they're all started in sync"
  []
  (at (+ 1000 (now))
      (doall
       (reduce (fn [res samp]
                 (let [id (loop-synth samp 0)]
                   (conj res  {:vol 0
                               :id id
                               :samp samp})))
               []
               samples))))

(def playing-samples* (agent (start-samples)))

(defn reset-samples!
  []
  (send playing-samples*
        (fn [playing-samples]
          (doall (map #(kill (:id %)) playing-samples))
          (poly/clear m)
          (start-samples))))

(reset-samples!)

(defn toggle
  "Invert the vol from 1 to 0 or 0 to 1"
  [vol]
  (mod (inc vol) 2))

(defn toggle-sample
  [n]
  (send playing-samples* (fn [playing-samples]
                           (let [samp     (nth playing-samples n)
                                 id       (:id samp)
                                 new-vol  (toggle (:vol samp))
                                 new-samp (assoc samp :vol new-vol)]
                             (ctl id :vol new-vol)
                             (assoc playing-samples n new-samp)))))

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
  (poly/toggle-led m x y)
  (toggle-sample (poly/button-id m x y)))


(poly/on-press m (fn [x y s] (trigger x y)))

(def rate* (atom 1))

(defn tempo-slide [to]
  (let [from @rate*
        step (if (< from to) 0.01 -0.01)
        vals (range from to step)]
    (dorun (map #(do (ctl loop-synth :rate (reset! rate* %)) (Thread/sleep 35)) vals))))


;;(tempo-slide 1.5)

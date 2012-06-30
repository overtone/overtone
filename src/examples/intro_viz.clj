(ns examples.intro-viz
  (:use quil.core
        [overtone.algo.scaling :only [scale-range]])
  (:require [polynome.core :as poly]
            [monome-serial.util :as mon]
            [examples.intro-sample-looper]))

;;(defonce bm (poly/init "/dev/tty.usbserial-m128-115"))
;;(poly/all bm)

(defonce vol-tap (get (:taps examples.intro-sample-looper/relay) "trig-vol"))
(defonce growing?* (atom false))

(comment (add-watch vol-tap ::shine-monome (fn [k r o n]
                                             (let [b (int (scale-range n 0 30 0 15))]
                                               (println "monome shiner: " b)
                                               (mon/brightness bm b)))))

(defn setup []
  (no-stroke)
  (frame-rate 60)
  (smooth)
  (background 70)
  (rect-mode :center)
  (set-state! :img (load-image "/Users/sam/Desktop/clojure-logo.png")))

(defn jumpy-circle
  [vol ]
  (fill (* 10 vol) (mod (frame-count) 255) (mod (frame-count) 255) (* 10 vol))
  (let [size (* 60 vol)]
    (let [i (state :img)
          s (/ vol 14)]
      (scale s)
      (image-mode :center)
      (when (> vol 10)
        (image i (/ (width) 2) (/ (height) 2))
        (ellipse (/ (width) 2) (/ (height) 2) size size)))))

(defn rotated-circle
  [vol]
  (translate  (/ (width) 2) (/ (height) 2))
  (when (> vol 1)
    (rotate (radians (+ (+  (frame-count) (/ vol 0.1)) ))))
  (image (state :img) 0 0))

(defn growing-circle
  [vol ]
  (translate  (/ (width) 2) (/ (height) 2))
  (scale (/ vol 10))
  (image (state :img) 0 0))

(defn spinning-circle
  [vol amt]
  (translate  (/ (width) 2) (/ (height) 2))
  (future (examples.intro-sample-looper/tempo-slide amt))
  (rotate (radians (* (frame-count) amt 10 (/ @examples.intro-sample-looper/rate* 0.2))))
  (when @growing?*
    (scale (/ vol 10)))
  (image (state :img) 0 0))

(defn toggle-growth
  [x y s]
  (if (and  (= 7 y)
            (= 7 x))
    (swap! growing?* not)))

(poly/on-press examples.intro-sample-looper/m ::growth-toggle toggle-growth)

(defn draw []
  (background 20)
  (tint 255 100)
  (let [vol @vol-tap]
    (case @examples.intro-sample-looper/fx-id
      0 (jumpy-circle vol)
      1 (rotated-circle vol)
      2 (growing-circle  vol)
      3 (spinning-circle vol 0.4)
      4 (spinning-circle vol 0.6)
      5 (spinning-circle vol 0.8)
      6 (spinning-circle vol 1))))

(defsketch wobble-ellipse
  :size [1600 1000]
  :title "Clojure/West"
  :renderer :opengl
  :target :perm-frame
  :draw draw
  :setup setup)

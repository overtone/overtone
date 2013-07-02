(ns
  ^{:doc "Building a simple Quil/Processing based GUI for controlling
          a running Overtone synth. The GUI supports buttons and
          sliders, but can be extended with other control types."
    :author "Karsten Schmidt"}
  overtone.examples.workshops.resonate2013.ex06_quilstep
  (:require [quil.core :as q :refer [defsketch]])
  (:use [overtone.live]))

;; This atom will hold the ID of the currently playing dubstep synth instance
(def synth-ref (atom 0))

;; The actual dubstep synth (taken from
;; overtone.examples.instruments.dubstep namespace)
;; Also see ex05_synthesis for detailed comments
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
       snare (* 3 (pink-noise) (apply + (* (decay (impulse (/ bpm 240) 0.5) [0.4 2]) [1 0.05])))
       snare (+ snare (bpf (* 4 snare) 2000))
       snare (clip2 snare 1)]
   (out 0 (* v (clip2 (+ wob (* kick-vol kick) (* snare-vol snare)) 1)))))

(defn map-interval
  "Maps `x` from interval `in1`..`in2` into interval `out1`..`out2`."
  [x in1 in2 out1 out2]
  (+ out1 (* (/ (- x in1) (- in2 in1)) (- out2 out1))))

(defn in-rect?
  "Returns true if point [x y] is in rectangle."
  [x y [rx ry rw rh]]
  (and (>= x rx) (< x (+ rx rw)) (>= y ry) (< y (+ ry rh))))

(defn make-slider
  "Returns a map definition of a slider GUI widget.
  `handler` is a function accepting the new slider value."
  [handler v minv maxv x y w h label]
  {:handler handler
   :val v :min minv :max maxv
   :bounds [x y w h]
   :label label})

(defn make-button
  "Returns a map definition of a button GUI widget.
  `handler` is a no-arg function called when the button was pressed."
  [handler x y w h bg label]
  {:handler handler :bounds [x y w h] :bg bg :label label})

(defn make-key
  "Take a note id (0..11) and key color (0 = black, 1 = white),
  calls `make-button` with predefined handler to switch pitch of wobble
  bass in currently playing syth instance."
  [[n col]]
  (let [col (* 255 col)]
    (make-button #(ctl @synth-ref :note (+ (note :c2) n)) (+ 140 (* n 25))
                 20 25 50 [col col col] nil)))

(defn button-handler
  "Helper fn/wrapper for button handlers. Accepts a button and mouse pos
  calls button's handler fn if mouse was inside button's screen rect."
  [{:keys [bounds handler]} click-x click-y]
  (when (in-rect? click-x click-y bounds) (handler)))

;; Clojure compiler only does a single-pass, so we need to forward
;; declare the presence of the `sliders` symbol defined below
(declare sliders)

(defn slider-handler
  "Helper fn/wrapper for slider handlers. Accepts a slifer and mouse pos,
  updates slider value and calls slider's handler fn if mouse was inside
  slider's screen rect."
  [id {:keys [val min max bounds handler]} click-x click-y]
  (when (in-rect? click-x click-y bounds)
    (let [[x _ w] bounds
          new-val (map-interval click-x x (+ x w) min max)]
      (swap! sliders assoc-in [id :val] new-val)
      (handler new-val))))

(def buttons
  "Defines a list of buttons: play & stop and 12 piano keys for changing pitch."
  (concat
    [(make-button #(reset! synth-ref (:id (dubstep))) 20 20 50 50 [0 0 0] "play")
     (make-button stop 80 20 50 50 [0 0 0] "stop")]
    (map make-key (zipmap (range) [1 0 1 0 1 1 0 1 0 1 0 1]))))

(def sliders
  "Defines a map of sliders and their handlers to control the synth, all wrapped
  in an atom to allow for interactive updates of slider values."
  (atom {:tempo (make-slider (fn [x] (ctl @synth-ref :bpm x)) 120 80 180 20 80 200 20 "tempo")
         :wobble (make-slider (fn [x] (ctl @synth-ref :wobble (int x))) 1 1 16 20 110 200 20 "wobble")
         :amp (make-slider (fn [x] (ctl @synth-ref :v x)) 1.0 0.0 1.0 20 140 200 20 "volume")}))

(defn draw-slider
  "Takes a single slider map and draws it with optional label."
  [{:keys [val min max label] [x y w h] :bounds}]
  (let [x2 (+ x w)
        ymid (+ y (/ h 2))]
    (q/stroke 0)
    (q/line x ymid x2 ymid)
    (q/fill 255 255 0)
    (q/rect (- (map-interval val min max x x2) 5) y 10 20)
    (when label
      (q/fill 0)
      (q/text label (+ x w 20) (+ ymid 4)))))

(defn draw-button
  "Takes a single button map and draws it with optional label.
  (I.e. Piano keys don't have labels."
  [{label :label [r g b] :bg [x y w h] :bounds}]
  (q/fill r g b)
  (q/rect x y w h)
  (when label
    (q/fill 255 255 0)
    (q/text label (+ x 10) (+ y h -10))))

(defn draw []
  "Main draw function of the Quil sketch."
  (q/background 192)
  (doseq [b buttons] (draw-button b))
  (doseq [s (vals @sliders)] (draw-slider s)))

(defn mouse-pressed []
  "Mouse event handler. Checks & updates all GUI elements."
  (let [x (q/mouse-x) y (q/mouse-y)]
    (doseq [b buttons] (button-handler b x y))
    (doseq [[id s] @sliders] (slider-handler id s x y))))

;; Define & launch the Quil sketch...
(defsketch Resonate
  :size [460 180]
  :title "Resonate 2013 Quilstep"
  :draw draw
  :mouse-pressed mouse-pressed)

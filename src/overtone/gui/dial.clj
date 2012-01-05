(ns overtone.gui.dial
  (:use [seesaw core graphics make-widget])
  (:require [seesaw.bind :as bind])
  (:import [javax.swing DefaultBoundedRangeModel]))

(def ^{:private true} DIAL_RADIUS 30)
(def ^{:private true} DIAL_OUTLINE_RADIUS 42)
(def ^{:private true} DIAL_PADDING 2.5)
(def ^{:private true} DELTA_DIVISOR -200)

(defn- center-arc
  [x y w h start end]
  (arc (- x w) (- y h) (* w 2) (* h 2) start end))

(defn- paint-dial-group
  "Paint the dial widget group"
  [dial-value size c g]
  (let [w   (width c)
        h   (height c)
        s   size
        line-style (style :foreground "#000000" :stroke 2.0 :cap :round)
        indicator-style (style :foreground "#000000" :background "#000000" :stroke 1.0 :cap :round)
        outline-radius (- s DIAL_PADDING)
        dial-radius (- s (* DIAL_PADDING 6))
        cx (/ w 2)
        cy (/ h 2)
        dx (- cx (/ dial-radius 2))
        dy (- cy (/ dial-radius 2))
        ox (- dx (* DIAL_PADDING 2))
        oy (- dy (* DIAL_PADDING 2))
        theta (- (* @dial-value 270) 135)]
    (comment
      ; shows the inner circle for debugging and other possible styles
     (draw g
       (circle (+ ox 4) (+ oy (- outline-radius 6)) 2) indicator-style)
     (draw g
       (circle (+ ox (- outline-radius 6)) (+ oy (- outline-radius 6)) 2) indicator-style)
     (draw g
       (center-arc cx cy (/ outline-radius 2) (/ outline-radius 2) -44 269) (style :foreground "#000000" :stroke 2.0 :cap :round))
      )

    (translate g cx cy)
    (rotate g theta)
    (translate g (- 0 cx) (- 0 cy))

    (draw g
          (circle cx cy (/ dial-radius 2)) line-style)
    (draw g
          (line cx (+ dy 6) cx (- dy 1)) line-style)))

(defn- dial-widget
  [dial-value size]
  (let [last-y (atom nil)
        dial (canvas :id :dial-base
                     :size [size :by size]
                     :paint (partial paint-dial-group dial-value size))
        panel (border-panel
                :center dial)]
    (listen dial
            :mouse-pressed
            (fn [e] (reset! last-y (.getY e)))
            :mouse-dragged
            (fn [e]
              (let [cy (.getY e)
                    delta (double (/ (- cy @last-y) DELTA_DIVISOR))]
                (swap! dial-value
                       (fn [v]
                         (max 0.0 (min 1.0 (+ v delta)))))
                (reset! last-y cy)
                (.repaint (.getSource e)))))
    panel))


(deftype Dial [panel value model]
  bind/ToBindable
  (to-bindable* [this] model)
  MakeWidget
  (make-widget* [this] panel))


(defn dial
  [& {:keys [minimum maximum value extent size]
      :or {minimum 0.0 maximum 100.0 value 50.0 extent 0.0 size 35.0}}]
  (let [model (DefaultBoundedRangeModel. value extent minimum maximum)
        dial-value (atom value)
        panel (dial-widget dial-value size)]
    (bind/bind dial-value
               (bind/b-do [v] (.setValue model (+ (* (- maximum minimum) v) minimum))))
    (Dial. panel dial-value model)))

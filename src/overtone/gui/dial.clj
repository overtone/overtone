(ns overtone.gui.dial
  (:use [seesaw core graphics]
        [seesaw.behave :only [when-mouse-dragged]]
        [seesaw.meta :only [put-meta! get-meta]])
  (:require [seesaw.bind :as bind]))

(def ^{:private true} DIAL_RADIUS 30)
(def ^{:private true} DIAL_OUTLINE_RADIUS 42)
(def ^{:private true} DIAL_PADDING 2.5)
(def ^{:private true} DELTA_DIVISOR -200)

(def ^{:private true} LINE_STYLE (style :foreground "#000000" :stroke 2.0 :cap :round))
(def ^{:private true} INDICATOR_STYLE (style :foreground "#000000" :background "#000000" :stroke 1.0 :cap :round))

(defn- center-arc
  [x y w h start end]
  (arc (- x w) (- y h) (* w 2) (* h 2) start end))

(defn- paint-dial-group
  "Paint the dial widget group"
  [dial-value c g]
  (let [w   (width c)
        h   (height c)
        s   (min w h) 
        outline-radius (- s DIAL_PADDING)
        dial-radius (- s (* DIAL_PADDING 6))
        cx (/ w 2)
        cy (/ h 2)
        dx (- cx (/ dial-radius 2))
        dy (- cy (/ dial-radius 2))
        ox (- dx (* DIAL_PADDING 2))
        oy (- dy (* DIAL_PADDING 2))
        theta (- (* @dial-value 270) 135)]
    
      ; shows the inner circle for debugging and other possible styles
     (comment
       (draw g
       (circle (+ ox 4) (+ oy (- outline-radius 6)) 2) INDICATOR_STYLE
       (circle (+ ox (- outline-radius 6)) (+ oy (- outline-radius 6)) 2) INDICATOR_STYLE
       (center-arc cx cy (/ outline-radius 2) (/ outline-radius 2) -44 269)           (style :foreground "#000000" :stroke 2.0 :cap :round)
)) 
    (translate g cx cy)
    (rotate g theta)
    (translate g (- 0 cx) (- 0 cy))
    (draw g
          (circle cx cy (/ dial-radius 2)) LINE_STYLE
          (line cx (+ dy 6) cx (- dy 1))   LINE_STYLE)))

(defn- dial-widget
  [dial-value size]
  (let [dial (canvas :preferred-size [size :by size]
                     :paint (partial paint-dial-group dial-value))]
    (when-mouse-dragged dial
      :drag (fn [e [dx dy]]
              (let [cy (.getY e)
                    delta (double (/ dy DELTA_DIVISOR))]
                (swap! dial-value
                       (fn [v]
                         (max 0.0 (min 1.0 (+ v delta))))))))
    dial))

(defn dial-value 
  "Returns the atom backing dial d"
  [d]
  (get-meta d ::dial-value))

(defn dial
  [& {:keys [id class size value] 
      :or   {size 35.0 value 0.5}}]
  (let [dial-value (atom value)
        widget     (dial-widget dial-value size)]
    (when id    (config! widget :id id))
    (when class (config! widget :class class))
    (put-meta! widget ::dial-value dial-value)
    (bind/bind dial-value (bind/b-do [_] (invoke-soon (repaint! widget))))
    widget))


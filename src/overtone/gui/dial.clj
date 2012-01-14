(ns overtone.gui.dial
  (:use [seesaw core graphics]
        [seesaw.behave :only [when-mouse-dragged]]
        [seesaw.options :only [apply-options option-map default-option]]
        [seesaw.keymap :only [map-key]]
        [seesaw.widget-options :only [WidgetOptionProvider]]
        [seesaw.value :only [Value]]
        [seesaw.selection :only [Selection]])
  (:require [seesaw.bind :as bind]))

(def ^{:private true} DIAL_PADDING 2.5)

(def ^{:private true} LINE_STYLE (style :foreground "#000000" :stroke 2.0 :cap :round))
(def ^{:private true} FOCUS_STYLE (style :foreground "#444444" 
                                         :stroke (stroke :dashes [4.0])
                                         :cap :round))
(def ^{:private true} INDICATOR_STYLE (style :foreground "#000000" :background "#000000" :stroke 1.0 :cap :round))

(defn- center-arc
  [x y w h start end]
  (arc (- x w) (- y h) (* w 2) (* h 2) start end))

(defn- paint-dial-group
  "Paint the dial widget group"
  [^javax.swing.DefaultBoundedRangeModel state 
   ^javax.swing.JPanel c g]
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
        theta (- (* (/ (- (.getValue state) 
                          (.getMinimum state)) 
                       (- (.getMaximum state)
                          (.getMinimum state)))
                    270)
                 135)]
    
      ; shows the inner circle for debugging and other possible styles
     (comment
       (draw g
       (circle (+ ox 4) (+ oy (- outline-radius 6)) 2) INDICATOR_STYLE
       (circle (+ ox (- outline-radius 6)) (+ oy (- outline-radius 6)) 2) INDICATOR_STYLE
       (center-arc cx cy (/ outline-radius 2) (/ outline-radius 2) -44 269)           (style :foreground "#000000" :stroke 2.0 :cap :round)
)) 
    (if (.isFocusOwner c)
      (draw g
            (circle cx cy (+ (/ dial-radius 2) 4)) FOCUS_STYLE))
    (translate g cx cy)
    (rotate g theta)
    (translate g (- 0 cx) (- 0 cy))
    (draw g
          (circle cx cy (/ dial-radius 2)) LINE_STYLE
          (line cx (+ dy 6) cx (- dy 1))   LINE_STYLE)))

(defn dial-proxy [state]
  (proxy [javax.swing.JPanel clojure.lang.IDeref] []
    (deref [] state)
    (paintComponent [g]
      (proxy-super paintComponent g)
      (paint-dial-group state this (anti-alias g)))))

(def ^{:private true} DialClass (class (dial-proxy {})))

(defn dial
  [& opts]
  (let [state  (javax.swing.DefaultBoundedRangeModel.)
        widget (dial-proxy state)
        adjust (fn [dv e]
                 (.setValue state (+ (.getValue state) dv)))
        percent-adjust (fn [p e]
                         (adjust (* p (- (.getMaximum state) 
                                         (.getMinimum state))) e))]
    (when-mouse-dragged widget 
      :start (fn [e] (.requestFocusInWindow widget))
      :drag (fn [e [dx dy]]
              ; map height of widget to full range of dial, i.e.
              ; dragging from top to bottom will move dial from
              ; min to max.
              (let [range (- (.getMaximum state) (.getMinimum state))
                    delta (* -1 dy (/ range (height widget)))]
                (.setValue state (+ (.getValue state) delta)))))
    (listen widget :focus repaint!)

    ; Repaint when the model changes
    (bind/bind state
               (bind/b-do [_] (invoke-soon (repaint! widget))))
    ; Key bindings
    (doseq [[k f] [["DOWN" (partial adjust -1)]
                   ["UP"   (partial adjust 1)]
                   ["shift DOWN" (partial percent-adjust -0.1)]
                   ["shift UP" (partial percent-adjust 0.1)]]]
      (map-key widget k f))

    (apply-options 
      widget 
      (concat
        [:focusable? true]
        opts))))

(def dial-options
  (merge
    default-options
    (option-map
      (default-option :value
        (fn [this v] (.setValue @this v))
        (fn [this]   (.getValue @this)))
      (default-option :min
        (fn [this v] (.setMinimum @this v))
        (fn [this]   (.getMinimum @this)))
      (default-option :max
        (fn [this v] (.setMaximum @this v))
        (fn [this]   (.getMaximum @this))))))

(extend-type (do DialClass)
  WidgetOptionProvider
    (get-widget-option-map* [this] [dial-options])
    (get-layout-option-map* [this] nil)
  
  Value
    (container?* [this] false)
    (value* [this] (config this :value))
    (value!* [this v] (config! this :value v))
  Selection
    (get-selection [this] [(value this)])
    (set-selection [this [v]] (value! this v))
  
  bind/ToBindable
    (to-bindable* [this] @this))

(comment
  (use 'overtone.gui.dial 'seesaw.core 'seesaw.dev)
  (def d (dial :id :my-dial))
  (-> (frame :content d) pack! show!))

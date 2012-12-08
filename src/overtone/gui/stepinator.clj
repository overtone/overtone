(ns overtone.gui.stepinator
  (:use [seesaw core graphics make-widget color]
        [overtone.gui color dial adjustment-popup])
  (:require [seesaw.bind :as bind]
            [seesaw.font :as font]
            [overtone.config.log :as log])
  (:import [java.awt Color Paint Stroke BasicStroke GradientPaint
            LinearGradientPaint RadialGradientPaint]
           [java.awt.geom Point2D$Float Point2D$Double CubicCurve2D$Double QuadCurve2D GeneralPath]
           [java.lang.Math]))

(def ^{:private true} step-style
  (style
;    :background (clarify-color (theme-color :fill-1) -1)
    :background (theme-color :fill-1)
    :foreground (theme-color :stroke-1)
    :stroke 1.0))

(def ^{:private true} step-border-style
  (style
    :foreground (theme-color :stroke-2)
    :background (theme-color :fill-2)
    :stroke 2.0))

(def ^{:private true} background-stroke
  (style
    :foreground (theme-color :background-stroke)
    :stroke 1.0))

(def ^{:private true} background-fill
  (style
    :foreground (theme-color :fill-1)
    :background (theme-color :background-fill)
    :stroke 1.0))

(defn- step-state
  "create the intitial state with the number of possible steps, width and height of the widget"
  [num-steps num-slices width height values]
  {:steps      (if (empty? values)
                 (vec (repeat num-steps 0))
                 (vec values))
   :num-steps  num-steps
   :num-slices num-slices
   :width      width
   :height     height})

(defn- paint-stepinator
  [state c g]
  (let [w             (width c)
        h             (height c)
        steps         (:steps state)
        x-res         (:num-steps state)
        step-width    (/ w (:num-steps state))
        y             (/ h 2)
        tick-height   (/ h (:num-slices state))
        num-steps     (:num-steps state)
        line-padding  0]

    (draw g (rect 0 0 w h) background-fill)

    (dotimes [i (:num-slices state)]
      (draw g (line 0 (* i tick-height) w (* i tick-height)) background-stroke))

    (dotimes [i num-steps]
      (let [step-val  (- (nth steps i))
            x         (* i step-width)]
        (draw g (line (* i step-width) 0 (* i step-width) h)
              background-stroke)

        (draw g
          (rect x y step-width (* step-val tick-height))
            step-style)

        (draw g
              (line (+ x line-padding)
                    (+ y (* step-val tick-height))
                    (- (+ x step-width) line-padding)
                    (+ y (* step-val tick-height)))
              step-border-style)))))

(defn- on-press-drag
  [state e]
  (let [x             (.getX e)
        y             (.getY e)
        w             (width e)
        h             (height e)
        x-cell        (quot x (quot w (:num-steps state)))
        tick-height   (quot h (:num-slices state))
        current-value (- (quot (- h y) tick-height) (quot (:num-slices state) 2))]
    (if (and (> x 0) (< x w) (> y 0) (< y h))
      (assoc (assoc-in state [:steps x-cell] current-value)
             :current-value current-value)
      state)))

(defn- stepinator-panel
  [state-atom]
  (let [c       (canvas :id :stepinator-canvas
                        :background (color :white)
                        :paint #(paint-stepinator @state-atom %1 %2))]
      (listen c
        :mouse-pressed #(swap! state-atom on-press-drag %)
        :mouse-dragged #(swap! state-atom on-press-drag %))
    c))

(defn stepinator
  "Creates a simple UI for building a sequence of step values.

  A grid is displayed which is :steps columns wide and :slices rows tall.
  Click and drag on the ui to change the value at each step. The range of
  values is [-slices/2, slices/2] with 0 at the center, vertically of the
  window.

  Returns a map with keys:

    :frame The ui frame
    :state The state *atom* which is a map with a :steps entry which is a
      vector of step values

  The optional :stepper parameter is a function which takes a vector of
  step values. If present, a 'Stepinate' button will be creates on the ui
  which, when clicked, will invoke the function with the curent state of
  stepinator.
  "
  [& {:keys [steps slices width height stepper values]
      :or {steps 16 slices 20 width 300 height 150 values []}}]
  (invoke-now
    (log/with-error-log "stepinator"
      (let [state-atom    (atom (step-state steps slices width height values))
            stepinator    (stepinator-panel state-atom)
            f             (frame :title    "Stepinator"
                                 :content  (border-panel
                                             :id :content
                                             :size [width :by height]
                                             :center stepinator))
            state-bindable (bind/bind state-atom
                                      (bind/transform #(:current-value %)))
            ]
        (if stepper
          (config! (select f [:#content])
                   :south (action :name "Stepinate"
                                  :handler (fn [_] (stepper (:steps @state-atom))))))
        (bind/bind state-atom (bind/b-do [_] (repaint! stepinator)))
        (adjustment-popup :widget stepinator :label "Value:" :bindable state-bindable)
        (with-meta {:frame (-> f pack! show!)
                    :state state-atom }
                   {:type :stepinator})))))


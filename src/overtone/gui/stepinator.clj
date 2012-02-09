(ns overtone.gui.stepinator
  (:use [seesaw core graphics make-widget color]
        [overtone.gui color dial adjustment-popup]
        [overtone.util log])
  (:require [seesaw.bind :as bind]
            [seesaw.font :as font])
  (:import [java.awt Color Paint Stroke BasicStroke GradientPaint
            LinearGradientPaint RadialGradientPaint]
           [java.awt.geom Point2D$Float Point2D$Double CubicCurve2D$Double QuadCurve2D GeneralPath]
           [java.lang.Math]))

(def ^{:private true} step-style
  (style
    :background (clarify-color (theme-color :fill-1) -1)
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
    :foreground (theme-color :background-fill)
    :background (theme-color :background-fill)
    :stroke 1.0))

(def ^{:private true} NUM_SLICES 20)

(defn- create-init-state
  "create the intitial state with the number of possible steps, width and height of the widget"
  [num-steps width height]
  {:steps      (vec (repeat num-steps 0))
   :num-steps  num-steps
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
        tick-height   (/ h NUM_SLICES)
        num-steps    (:num-steps state)
        line-padding  0]

    (draw g (rect 0 0 w h) background-fill)

    (dotimes [i 20]
      (draw g (line 0 (* i tick-height) w (* i tick-height)) background-stroke))

    (dotimes [i num-steps]
      (let [step-val  (nth steps i)
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
  (let [
        x-cell        (int (/ (.getX e) (/ (.getWidth (.getComponent e)) (:num-steps state))))
        y-cell        (int (/ (.getY e) (/ (.getHeight (.getComponent e)) 2)))
        tick-height  (/ (.getHeight (.getComponent e)) NUM_SLICES)
        current-value (- (int (/ (.getY e) tick-height)) 10)]
    (assoc (assoc-in state [:steps x-cell] current-value)
           :current-value current-value)))

(defn- stepinator-panel
  [state-atom]
  (let [c       (canvas :id :stepinator-canvas
                        :background (color :white)
                        :paint #(paint-stepinator @state-atom %1 %2))]
      (listen c
        :mouse-pressed #(with-error-log "stepinator on-pressed"
                          (swap! state-atom on-press-drag %))
        :mouse-dragged #(with-error-log "stepinator on-dragged"
                          (swap! state-atom on-press-drag %)))
    c))

(defn stepinator
  [& {:keys [steps width height]
      :or {steps 16 width 300 height 150}}]
  (invoke-now
    (with-error-log "stepinator"
                    (let [state-atom    (atom (create-init-state steps width height))
                          stepinator    (stepinator-panel state-atom)
                          f             (frame :title    "Stepinator"
                                               :content  (border-panel
                                                           :size [width :by height]
                                                           :center stepinator))
                          state-bindable (bind/bind state-atom
                                                    (bind/transform #(:current-value %)))
                          ]
                      (bind/bind state-atom (bind/b-do [_] (repaint! stepinator)))
                      (adjustment-popup :widget f :label "Value:" :bindable state-bindable)
                      (with-meta {:frame (-> f pack! show!)
                                  :state state-atom }
                                 {:type :stepinator})))))

(comment
(use 'overtone.live)
(use 'overtone.gui.stepinator)
(def pstep (stepinator))
(demo 2
  (let [note (duty (dseq [0.2 0.1] INF)
                   0
                   (dseq (map #(+ 60 %) (:steps @(:state pstep)))))
        src (saw (midicps note))]
    (* [0.2 0.2] src)))
)

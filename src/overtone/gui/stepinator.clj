(ns overtone.gui.stepinator
  (:use [seesaw core graphics make-widget color]
        [overtone.gui.dial]
        [overtone.gui.toolbelt])
  (:require [seesaw.bind :as bind]
            [seesaw.font :as font])
  (:import [java.awt Color Paint Stroke BasicStroke GradientPaint
            LinearGradientPaint RadialGradientPaint]
           [java.awt.geom Point2D$Float Point2D$Double CubicCurve2D$Double QuadCurve2D GeneralPath]
           [java.lang.Math]))




(def ^{:private true} step-style
  (style
    :background (color 220 220 10 175)
    :stroke 1.0))

(def ^{:private true} step-border-style
  (style
    :foreground (color :red)
    :background (color :blue)
    :stroke 2.0))

(def ^{:private true} background-line-style
  (style
    :foreground (color 0 0 0 25)
    :stroke 1.0))

(defn- neg-rect
[x y w h]
  (rect x (- y h) w h))

(def ^{:private true} NUM_SLICES 20)



(defn- create-init-state  
  "create the intitial state with the number of possible steps, width and height of the widget"
  [step-count width height]
  (let [x-step-size   (/ width step-count)
        state-map     {
                        :steps          (vec (repeat step-count 0))

                        :step-count     step-count
                        :num-rows       2
                        :steps-to-use   16
                        :width          width
                        :height         height

                        :step-size      x-step-size
                      }]
    state-map))

(defn- paint-stepinator
  [state c g]
  (let [w             (width c)
        h             (height c)
        steps         (:steps state)
        x-res         (:step-count state)
        y-res         (:steps-to-use state)
        step-size     (/ w (:step-count state))
        y             (/ h 2)
        slice-height  (/ h NUM_SLICES)
        step-count    (:step-count state)
        line-padding  1.5]
    

    (dotimes [i 20] 
      (draw g (line 0 (* i slice-height) w (* i slice-height)) background-line-style)
      )

    (dotimes [i step-count]
      (let [step-val  (nth steps i)
            x         (* i step-size)]
        (draw g
          (rect x y step-size (* step-val slice-height))
            step-style)

        (draw g (line (+ x line-padding) (+ y (* step-val slice-height)) (- (+ x step-size) line-padding) (+ y (* step-val slice-height)))  step-border-style)

          ))))



(defn- on-pressed
  [state e]
  (let [
        x-cell        (int (/ (.getX e) (/ (.getWidth (.getComponent e)) (:step-count state))))
        y-cell        (int (/ (.getY e) (/ (.getHeight (.getComponent e)) 2)))
        slice-height  (/ (.getHeight (.getComponent e)) NUM_SLICES)
        y-slice       (- (int (/ (.getY e) slice-height)) 10)]
    (assoc-in state [:steps x-cell] y-slice)))

(defn- on-dragged
  [state e]
  (let [
        x-cell        (int (/ (.getX e) (/ (.getWidth (.getComponent e)) (:step-count state))))
        y-cell        (int (/ (.getY e) (/ (.getHeight (.getComponent e)) 2)))
        slice-height  (/ (.getHeight (.getComponent e)) NUM_SLICES)
        y-slice       (- (int (/ (.getY e) slice-height)) 10)]
    (assoc-in state [:steps x-cell] y-slice)))


(defn- build-stepinator
  [state-atom]
  (let [c       (canvas :id :stepinator-canvas
                        :background (color :white)
                        :paint #(paint-stepinator @state-atom %1 %2))]
      (listen c
        :mouse-pressed #(swap! state-atom on-pressed %)
        :mouse-dragged #(swap! state-atom on-dragged %)
        )
    c))

(defn arp
  []
  (invoke-now
    (let [state-atom    (atom (create-init-state 16 300 150))
          stepinator    (border-panel :center (build-stepinator state-atom))
          f             (frame :title    "The Stepinator"
                               :content  (border-panel
                                         :size [300 :by 150]
                                         :center  stepinator)
                               :on-close :dispose)]
      (bind/bind state-atom (bind/b-do [v] (repaint! stepinator)))

      (with-meta {:frame (-> f pack! show!)
                  :state state-atom }
                 {:type :sequencer}))))

(comment 
  (use 'overtone.gui.parametriceq)
  (parametric-eq)
)




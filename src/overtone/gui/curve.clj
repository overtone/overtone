(ns overtone.gui.curve
  (:import 
    (java.awt Graphics Dimension Color BasicStroke)
    (java.awt.geom Rectangle2D$Float Path2D$Float Arc2D$Float Arc2D)
    (com.sun.scenario.scenegraph JSGPanel SGText SGShape SGGroup SGTransform SGComponent
                                 SGAbstractShape$Mode)
    (com.sun.scenario.scenegraph.fx FXShape)
    (com.sun.scenario.scenegraph.event SGMouseAdapter))
  (:use 
     [overtone.core event envelope]
    clojure.stacktrace)
  (:require [overtone.core.log :as log]))

(defonce curve* (ref {:curve (adsr)
                      :color (Color. 0 130 226)
                      :background (Color. 50 50 50)
                      :width 600
                      :height 400
                      :padding-x 20
                      :padding-y 20
                      :seconds 3.0
                      :p-radius 10.0}))

; Use Scenegraph constructs to create a widget that can display and modify envelope 
; curve arrays.

; Envelope arrays are structured like this:
  ; * initial level
  ; * n-segments
  ; * release node (int or -99, tells envelope where to optionally stop until released)
  ; * loop node (int or -99, tells envelope which node to loop back to until released)
  ; [
  ;   - segment 1 endpoint level
  ;   - segment 1 duration
  ;   - segment shape
  ;   - segment curve
  ; ] * n-segments


; Converting from envelope space to pixel coords
; 
; env (0.0, 0.0)
; pix (0.0, height)
;
; env (0.5 0.5)
; pix (0.5 * pixels-per-second, (1 - 0.5) * height)
;
; env (1.0 1.0)
; pix (1.0 * pixels-per-second, (1 - 1.0) * height)

(defn y-pixel [y]
  (+ (:padding-y @curve*) 
     (* (- 1.0 y)
        (- (:height @curve*) (* 2 (:padding-y @curve*))))))

(defn x-pixel [x]
  (let [real-width (- (:width @curve*) (* 2 (:padding-x @curve*)))]
    (+ (:padding-x @curve*)
       (* x (/ real-width (:seconds @curve*))))))

(defn points []
  (let [curve (:curve @curve*)
        height (:height @curve*)
        width (:width @curve*)
        start (first curve)
        n-segments (second curve)
        releaser (nth curve 2)
        looper (nth curve 3)
        segments (partition 4 (drop 4 curve))
        start-point [(x-pixel 0.0) (y-pixel 0.0)]
        [points t] (reduce (fn [[points t] [end dur shape curve]] 
                             (let [t (+ t dur)
                                   x (x-pixel t)
                                   y (y-pixel end)]
                               [(conj points [x y]) t]))
                           [[start-point] 0.0]
                           segments)]
    points))

(defn curve-editor
  "Display an envelope curve in the wave window."
  []
  (let [curve-group (SGGroup.)
        background (FXShape.)
        points (points)
        line-path (Path2D$Float.)
        line (FXShape.)
        radius (:p-radius @curve*)
        r2 (/ radius 2.0)]

    ; background box
    (doto background
      (.setFillPaint (Color. 50 50 50))
      (.setShape (Rectangle2D$Float. 0.0 0.0 (:width @curve*) (:height @curve*))))
    (.add curve-group background)

    ; envelope line
    (.moveTo line-path (x-pixel 0.0) (y-pixel 0.0))
    (doseq [[x y] points]
      (.lineTo line-path x y))

    (.setFillPaint line (Color. 0 130 226))
    (.setShape line line-path)
    (.add curve-group line)

    ; control points
    (doseq [[x y] points]
      (println "---> x: " x " y: " y)
      (let [s (FXShape.)
            p (Arc2D$Float. (- x r2) (- y r2) radius radius 0 360 Arc2D/CHORD)]
        (.setShape s p)
        (.setFillPaint s (Color. 50 200 50))
        (.add curve-group s)))

    curve-group))

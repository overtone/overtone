(ns overtone.gui.curve
  (:import 
    (java.awt Graphics Dimension Color BasicStroke Font)
    (java.awt.font TextAttribute)
    (java.awt.geom Rectangle2D$Float Path2D$Float Arc2D$Float Arc2D)
    (com.sun.scenario.scenegraph JSGPanel SGText SGShape SGGroup SGTransform SGComponent
                                 SGAbstractShape$Mode)
    (com.sun.scenario.scenegraph.fx FXShape FXText)
    (com.sun.scenario.scenegraph.event SGMouseAdapter))
  (:use 
    (overtone.core event envelope)
    clj-scenegraph.core 
    clojure.stacktrace
    clojure.contrib.seq-utils)
  (:require [overtone.core.log :as log]))

(defonce curve* (ref {:curve (adsr)
                      :line nil
                      :color (Color. 0 130 226)
                      :fill-alpha 120
                      :background (Color. 50 50 50)
                      :width 600
                      :height 400
                      :padding-x 20
                      :padding-y 20
                      :seconds 3.0
                      :p-radius 10.0
                      :label-font (Font. "helvetica" Font/BOLD 18)
                      }))

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
;   envelope  :  pixel
; (0.0, 0.0)  :  (0.0, height)
; (0.5 0.5)   :  (0.5 * pixels-per-second, (1 - 0.5) * height)
; (1.0 1.0)   :  (1.0 * pixels-per-second, (1 - 1.0) * height)

(defn curve-to-canvas [x y & [pad-x pad-y]]
  (let [pad-x (or pad-x (:padding-x @curve*))
        pad-y (or pad-y (:padding-y @curve*))]
    [(+ pad-x (* x (/ (- (:width @curve*) 
                         (* 2 pad-x)) 
                      (:seconds @curve*))))
     (+ pad-y 
        (* (- 1.0 y)
           (- (:height @curve*) (* 2 pad-y))))]))

(defn canvas-to-curve [x y & [pad-x pad-y]]
  (let [pad-x (or pad-x (:padding-x @curve*))
        pad-y (or pad-y (:padding-y @curve*))]
  [(/ (- x pad-x) (/ (- (:width @curve*) 
                         (* 2 pad-x)) 
                      (:seconds @curve*)))
   (- 1.0 (/ (- y pad-y) (- (:height @curve*) (* 2 pad-y))))]))

(defn points []
  (let [curve (:curve @curve*)
        height (:height @curve*)
        width (:width @curve*)
        start (first curve)
        n-segments (second curve)
        releaser (nth curve 2)
        looper (nth curve 3)
        segments (partition 4 (drop 4 curve))
        start-point (curve-to-canvas 0.0 0.0)
        [points t] (reduce (fn [[points t] [end dur shape curve]] 
                             (let [t (+ t dur)]
                               [(conj points (curve-to-canvas t end)) t]))
                           [[start-point] 0.0]
                           segments)]
    points))

(defn curve-color 
  "Set the primary envelope color.

  (curve-color :red)
  (curve-color 0 30 120)
  (curve-color 0 30 120 100)
  (curve-color (Color. 0 30 120))
  "
  [& args]
  (let [arg (first args)
        args (if (= Color (type arg))
               [(.getRed arg) 
                (.getGreen arg) 
                (.getBlue arg) 
                (:fill-alpha @curve*)]
               args)
        line (:line @curve*)]
    (apply set-draw-paint! line args)
    (apply set-fill-paint! line args)))

(def current-pos* (ref nil))

(defn- update-point
  "Update the control point at index with new coords."
  [points index x y]
  (concat (take index points) [[x y]] (drop (inc index) points)))

(defn- points-to-path [path points]
  (.reset path)
  (let [[start-x start-y] (curve-to-canvas 0.0 0.0)]
    (.moveTo path start-x start-y)
    (doseq [[x y] points]
      (.lineTo path x y))
    path))

(defn curve-editor
  "Display an envelope curve in the wave window."
  []
  (let [curve-group (sg-group)
        background (sg-shape)
        points (points)
        line-path (Path2D$Float.)
        line (sg-shape)
        radius (:p-radius @curve*)
        r2 (/ radius 2.0)
        grid-path (Path2D$Float.)
        grid (sg-shape)]

    ; background box
    (doto background
      (set-mode! :stroke-fill)
      (set-draw-paint! 100 100 100)
      (set-fill-paint! 40 40 40)
      (set-shape! (Rectangle2D$Float. 0.0 0.0 (:width @curve*) (:height @curve*))))
    (.add curve-group background)

    ; grid
    (set-mode! grid :stroke)
    (set-draw-paint! grid 80 80 80)

    (let [[start-x start-y] (curve-to-canvas 0.0 0.0 0 0)]
      (.moveTo grid-path start-x start-y))

    (doseq [x (range 0.0 (:seconds @curve*) 0.1)] ; vertical lines
      (let [[tgt-x tgt-y] (curve-to-canvas x 1.0 0 0)
            [nxt-x nxt-y] (curve-to-canvas (+ 0.1 x) 0.0 0 0)]
        (.lineTo grid-path tgt-x tgt-y)
        (.moveTo grid-path nxt-x nxt-y)))
    (doseq [y (range 0.0 1.0 0.25)]               ; horizontal lines
      (let [[tgt-x tgt-y] (curve-to-canvas (:seconds @curve*) y 0 0)
            [nxt-x nxt-y] (curve-to-canvas 0.0 (+ y 0.25) 0 0)]
        (.lineTo grid-path tgt-x tgt-y)
        (.moveTo grid-path nxt-x nxt-y)))
    (.setShape grid grid-path)
    (set-antialias! grid :on)
    (.add curve-group grid)

    ; envelope curve
    (dosync (alter curve* assoc :line line))

    (points-to-path line-path points)
    (set-mode! line :stroke-fill)
    (set-draw-paint! line 0 130 226)
    (set-fill-paint! line 0 130 226 120)
    (.setShape line line-path)
    (set-antialias! line :on)
    (.add curve-group line)

    ; control points
    (dosync (alter curve* assoc :points points))

    (doseq [[idx [x y]] (indexed points)]
      (let [group (sg-group)
            shape (sg-shape)
            label (FXText.)
            trans (translate x y group)
            circle (Arc2D$Float. (- 0 r2) (- 0 r2) radius radius 0 360 Arc2D/CHORD)
            [cx cy] (canvas-to-curve x y)]
        (.add curve-group trans)
        (doto group
          (.add shape)
          (.add label))

        (doto shape ; circle
          (set-mode! :stroke-fill)
          (set-shape! circle)
          (set-draw-paint! :white)
          (set-fill-paint! 255 255 255 50)
          (set-antialias! :on))

        (doto label ; coordinate label
          (.setText (format "(%2.3f, %2.3f)" cx cy))
          (.setFont (:label-font @curve*))
          (.setFillPaint (Color. 255 255 255))
          (.setTranslateX radius)
          (.setTranslateY (* 2 radius))
          (.setVisible false))

        (on-mouse-pressed shape #(do
                                   (dosync (ref-set current-pos* (.getPoint %)))
                                   (.setVisible label true)))

        (on-mouse-dragged shape #(let [dx (- (.getX %) (.getX @current-pos*))
                                       dy (- (.getY %) (.getY @current-pos*))]
                                   (.translateBy trans (double dx) (double dy))
                                   (let [new-x (.getTranslateX trans)
                                         new-y (.getTranslateY trans)
                                         [new-cx new-cy] (canvas-to-curve new-x new-y)
                                         new-points (update-point (:points @curve*) idx new-x new-y)]
                                     (.setText label (format "(%2.3f, %2.3f)" new-cx new-cy))
                                     (.setShape line (points-to-path (Path2D$Float.) new-points))
                                     (dosync 
                                       (ref-set current-pos* (.getPoint %))
                                       (alter curve* assoc :points new-points)))))
        

        (on-mouse-released shape #(do
                                    (dosync (ref-set current-pos* nil))
                                    (.setVisible label false)))))
    curve-group))

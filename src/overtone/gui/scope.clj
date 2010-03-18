(ns overtone.gui.scope
  (:import 
    (java.awt Color BasicStroke)
    (java.awt.geom Rectangle2D$Float)
    (com.sun.scenario.scenegraph SGText SGShape SGGroup SGTransform 
                                 SGAbstractShape$Mode)
     (com.sun.scenario.scenegraph.event SGMouseAdapter)
     (com.sun.scenario.scenegraph.fx FXShape))
  (:use 
     [overtone.core sc synth util ugen time-utils])
  (:require [overtone.core.log :as log]))

(def SCOPE-BUF-SIZE 10000)

(def scope-buf* (ref false))
(def scope-bus* (ref 0))

;(defn scope-bus [bus]
;  (if (nil? @scope-buf*)
;    (dosync (ref-set scope-buf* (buffer SCOPE-BUF-SIZE))))
;  (overtone-scope bus (:id @scope-buf*)))

; TODO: remove all the scope ugens
;  * need to save synth objects or names stored in a lookup or something...
;  * stop animation
;  * close window
(defn kill-scope []
  )

(def fps* (ref 30))
(def wave-stroke-color* (ref (Color. 0 130 226)))
(def scope-bg-color* (ref (Color. 50 50 50)))
(def scope-width* (ref 600))
(def scope-height* (ref 400))
(def PADDING 10)

(def wave-shape (FXShape.))
(def wave-scale (SGTransform/createScale 0.01 180 wave-shape))
(def wave-shift (SGTransform/createTranslation 0 (/ @scope-height* 2) wave-scale))
(def wave-path (java.awt.geom.Path2D$Float.))

(defn update-wave []
  (when @scope-buf*
    (let [frames (buffer-data @scope-buf*)
          n-frames (count frames)]
      (.reset wave-path)
      (.moveTo wave-path (float 0) (aget frames 0))
      (doseq [i (range 1 n-frames)]
        (.lineTo wave-path (float i) (aget frames i))))))

(def SCOPE-WIDTH 22500)
(def SCOPE-BUS 10)
(declare test-buf)

(defn setup-scope []
  (def test-buf (buffer SCOPE-WIDTH))
  (defsynth simple [freq 200] (overtone.ugens/out SCOPE-BUS (overtone.ugens/sin-osc freq)))
  (defsynth scope-record [in-bus 22500
                          out-buf 0] 
    (overtone.ugens/record-buf in-bus out-buf))
  (def test-synth (simple 220))
  (scope-record SCOPE-BUS (:id test-buf))
  )

(defn scope-buf [buf]
  (dosync (ref-set scope-buf* buf))
  (.setScaleX wave-scale (float (/ @scope-width* (count (buffer-data buf)))))
  (.setScaleY wave-scale (float (/ @scope-height* 4)))
  (.setTranslateX wave-shift PADDING)
  (.setTranslateY wave-shift (+ (/ @scope-height* 2) PADDING))
  (update-wave))

(defn scope []
  (let [scope-group (SGGroup.)
        background (SGShape.)]
    (doto background
      (.setShape (Rectangle2D$Float. 0 0 
                                     (+ @scope-width* (* 2 PADDING)) 
                                     (+ @scope-height* (* 2 PADDING))))
      (.setMode SGAbstractShape$Mode/STROKE_FILL)
      (.setFillPaint @scope-bg-color*))

    (doto wave-shape
      (.setShape wave-path)
      (.setMode SGAbstractShape$Mode/STROKE)
;      (.setAntialiasingHint RenderingHints/VALUE_ANTIALIAS_ON)
      (.setDrawPaint @wave-stroke-color*)
      (.setDrawStroke (BasicStroke. 1.15)))

    (doto scope-group
      (.add background)
      (.add wave-shift))

    (setup-scope)
    (scope-buf test-buf)
    
;    (periodic #(update-wave) (/ 1000 @fps))
    (SGTransform/createTranslation 300 300 scope-group)))
 
;(def audio (load-sample "samples/strings/STRNGD5.WAV"))
;(def abuf (:buf audio))

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
(comment defn show-curve 
  "Display a SuperCollider envelope curve in a graphical window."
  [c]
  (let [[start-y n-segs rel-node loop-node & segments] c
        ds (DefaultXYDataset.)
        ary (make-array Double/TYPE 2 (inc n-segs))]
    (aset-double ary 0 0 0.0)
    (aset-double ary 1 0 (double start-y))
    (println "0.0" start-y)
    (loop [segs segments
           cur-x 0.0
           idx 1]
      (when segs
        (let [[y dur shape curve & segs] segs
              x (double (+ cur-x (* 1000 dur)))
              y (double y)]
          (aset-double ary 0 idx x)
          (aset-double ary 1 idx y)
          (println x y)
          (recur segs x (inc idx)))))
    (.addSeries ds "envelope" ary)
    (scope ds)))


; This is the line that does it!
;(update-dataset ds (.getFloatArray (.data (buffer-copy 0)) 0 (:n-frames (sample-info boom)))))

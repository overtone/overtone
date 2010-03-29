(ns overtone.gui.scope
  (:import 
    (java.awt Color BasicStroke)
    (java.awt.geom Rectangle2D$Float)
     (javax.swing JFrame JPanel) 
    (com.sun.scenario.scenegraph JSGPanel SGText SGShape SGGroup SGTransform 
                                 SGAbstractShape$Mode)
     (com.sun.scenario.scenegraph.event SGMouseAdapter)
     (com.sun.scenario.scenegraph.fx FXShape))
  (:use 
     [overtone.core sc synth util ugen time-utils]
    clojure.stacktrace)
  (:require [overtone.core.log :as log]))

(def SCOPE-BUF-SIZE 10000)
(def SCOPE-BUS 10)

(defonce scope-buf* (ref false))
(defonce scope-bus* (ref 0))

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

(defonce fps* (ref 30))
(defonce wave-stroke-color* (ref (Color. 0 130 226)))
(defonce scope-bg-color* (ref (Color. 50 50 50)))
(defonce scope-width* (ref 600))
(defonce scope-height* (ref 400))
(def PADDING 10)

(defonce wave-shape (FXShape.))
(defonce wave-scale (SGTransform/createScale 0.01 180 wave-shape))
(defonce wave-shift (SGTransform/createTranslation 0 (/ @scope-height* 2) wave-scale))
(defonce wave-path (java.awt.geom.Path2D$Float.))

(defn update-wave []
  (when @scope-buf*
    (println "Updating wave: " @scope-buf* (buffer-info @scope-buf*))
    (let [frames (buffer-data @scope-buf*)
          n-frames (count frames)]
      (.reset wave-path)
      (.moveTo wave-path (float 0) (aget frames 0))
 ;     (doseq [i (range 0 n-frames)]
 ;       (.lineTo wave-path (float i) (aget frames i))))))
      (doseq [i (range 0 n-frames (int (/ n-frames @scope-width*)))]
        (.lineTo wave-path (float i) (aget frames i))))))

(declare test-buf)

(defn setup-scope []
  (defsynth simple [freq 200] (overtone.ugens/out SCOPE-BUS (overtone.ugens/sin-osc freq)))
  (defsynth scope-record [in-bus SCOPE-BUS
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

(defn scope [& [buf]]
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

    (if buf
      (scope-buf buf))
    
;    (periodic #(update-wave) (/ 1000 @fps))
    (SGTransform/createTranslation 300 300 scope-group)))
 
(def frame (JFrame. "scope"))
(def panel (JSGPanel.))
(.add (.getContentPane frame) panel)
(.setScene panel (scope))

(defn test-scope []
  (if (not (connected?))
    (do 
      (boot)
      (Thread/sleep 500)
      (let [sample (load-sample "/home/rosejn/studio/samples/kit/boom.wav")]
        (Thread/sleep 500)
        (scope-buf sample))))
  (.show frame))

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
(defn show-curve 
  "Display an envelope curve in the wave window."
  [c])

(ns overtone.gui.scope
  (:import 
    (java.awt Dimension Color BasicStroke)
    (java.awt.geom Rectangle2D$Float Path2D$Float)
     (javax.swing JFrame JPanel) 
    (com.sun.scenario.scenegraph JSGPanel SGText SGShape SGGroup SGTransform 
                                 SGAbstractShape$Mode)
     (com.sun.scenario.scenegraph.event SGMouseAdapter)
     (com.sun.scenario.scenegraph.fx FXShape))
  (:use 
     [overtone.core event sc synth util time-utils]
    clojure.stacktrace)
  (:require [overtone.core.log :as log]))

(def SCOPE-BUF-SIZE 10000)
(def SCOPE-BUS 10)

(defonce scope* (ref {:buf false
                      :buf-size 0
                      :bus 0
                      :fps 15
                      :status :off
                      :runner nil}))

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

(def X-PADDING 5)
(def Y-PADDING 10)

(defonce wave-stroke-color* (ref (Color. 0 130 226)))
(defonce scope-bg-color* (ref (Color. 50 50 50)))
(defonce scope-width* (ref 600))
(defonce scope-height* (ref 400))
(defonce wave-shape (FXShape.))
(defonce wave-path (Path2D$Float.))

(defn update-wave []
  (when (:buf @scope*)
    (let [buf (:buf @scope*)
          frames (buffer-data buf)
          n-frames (:buf-size @scope*)
          y-scale (/ (- @scope-height* (* 2 Y-PADDING)) 2)]
      (.reset #^Path2D$Float wave-path)
      (.moveTo #^Path2D$Float wave-path (float 0) (aget #^floats frames 0))
      (doseq [i (range 1 n-frames (int (/ n-frames @scope-width*)))]
        (.lineTo #^Path2D$Float wave-path (float i) (* y-scale (aget #^floats frames i)))))
    (.setShape #^FXShape wave-shape #^Path2D$Float wave-path)))

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
  (dosync (alter scope* assoc 
                 :buf buf
                 :buf-size (count (buffer-data buf))))
  (.setScaleX wave-shape (/ @scope-width* (float (:buf-size @scope*))))
  (.setTranslateX wave-shape X-PADDING)
  (.setTranslateY wave-shape (+ (/ @scope-height* 2) Y-PADDING))
  (update-wave))

(defn scope [& [buf]]
  (let [scope-group (SGGroup.)
        background (SGShape.)]
    (doto background
      (.setShape (Rectangle2D$Float. 0 0 
                                     (+ @scope-width* (* 2 X-PADDING)) 
                                     (+ @scope-height* (* 2 Y-PADDING))))
      (.setMode SGAbstractShape$Mode/STROKE)
      (.setDrawPaint @scope-bg-color*))

    (doto wave-shape
      (.setShape wave-path)
      (.setMode SGAbstractShape$Mode/STROKE)
;      (.setAntialiasingHint RenderingHints/VALUE_ANTIALIAS_ON)
      (.setDrawPaint @wave-stroke-color*)
      (.setDrawStroke (BasicStroke. 1.15)))

    (doto scope-group
      (.add background)
      (.add wave-shape))

    (if buf
      (scope-buf buf))
    
    ;(SGTransform/createTranslation 30 30 scope-group)
    scope-group))

(defn scope-on []
  (dosync (alter scope* assoc 
                 :status :on 
                 :runner (periodic #(update-wave) (/ 1000 (:fps @scope*))))))

(defn scope-off []
  (.cancel (:runner @scope*) true)
  (dosync (alter scope* assoc
                 :status :off
                 :runner nil)))
 
(require 'examples.basic)

(defonce frame (JFrame. "scope"))
(defonce panel (JSGPanel.))
(defonce _test-scope (do 
             (.setPreferredSize panel (Dimension. 600 400))
             (.add (.getContentPane frame) panel)
             (.setScene panel (scope))
             (.pack frame)
             (.show frame)))

(defn- go-go-scope []
  (let [b (buffer 2048)]
    (Thread/sleep 100)
    (scope-buf b)
    (scope-on)
    (examples.basic/sizzle :bus 20)
    (examples.basic/bus->buf 20 (:id b))
    (examples.basic/bus->bus 20 0)))

(defn test-scope []
  (if (not (connected?))
    (do 
      (boot)
      (on :examples-ready go-go-scope))
    (go-go-scope))
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

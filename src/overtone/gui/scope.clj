(ns overtone.gui.scope
  (:import 
    (java.awt Graphics Dimension Color BasicStroke)
    (java.awt.geom Rectangle2D$Float Path2D$Float)
     (javax.swing JFrame JPanel) 
    (com.sun.scenario.scenegraph JSGPanel SGText SGShape SGGroup SGTransform SGComponent
                                 SGAbstractShape$Mode)
     (com.sun.scenario.scenegraph.event SGMouseAdapter))
  (:use 
     [overtone.core event sc synth ugen util time-utils]
    clojure.set
    clojure.stacktrace)
  (:require [overtone.core.log :as log]))

(refer-ugens)

(defonce scope* (ref {:buf false
                      :buf-size 0
                      :bus 0
                      :fps 15
                      :status :off
                      :runner nil
                      :panel nil
                      :color (Color. 0 130 226)
                      :background (Color. 50 50 50)
                      :width 600
                      :height 400}))

; Some utility synths for signal routing and scoping
(defn define-synths []
  (defsynth bus->buf [bus 20 buf 0]
    (record-buf (in bus) buf))

  (defsynth bus->bus [in-bus 20 out-bus 0]
    (out out-bus (in in-bus)))

  (defsynth scoper-outer [buf 0]
    (scope-out (sin-osc 200) buf))

  (defsynth freq-scope-zero [in-bus 0 fft-buf 0 scope-buf 1 
                             rate 4 phase 1 db-factor 0.02]
    (let [n-samples (* 0.5 (- (buf-samples fft-buf) 2))
          signal (in in-bus)
          chain (pv-mag-smear (fft fft-buf signal 0.5 :hann) 1)
          phas  (lf-saw (/ rate (buf-dur fft-buf)) phase n-samples (+ 2 n-samples))
          phas (round phas 2)]
      (scope-out (buf-rd 1 fft-buf phas 1 1) scope-buf))))

(if (connected?)
  (define-synths)
  (on :connected define-synths))

(defonce x-array (int-array (:width @scope*)))
(defonce _x-init (dotimes [i (:width @scope*)] 
                   (aset x-array i i)))

(defonce y-array (int-array (:width @scope*)))
(defonce _y-init (dotimes [i (:width @scope*)] 
                   (aset y-array i (/ (:height @scope*) 2))))

(def X-PADDING 5)
(def Y-PADDING 10)

(defn- update-scope []
  (let [frames (buffer-data (:buf @scope*))
        step (int (/ (:buf-size @scope*) (:width @scope*)))
        y-scale (/ (- (:height @scope*) (* 2 Y-PADDING)) 2)
        y-shift (+ (/ (:height @scope*) 2) Y-PADDING)]
    (doseq [x x-array]
      (aset #^ints y-array x 
            (int (+ y-shift (* y-scale (aget #^floats frames (* x step))))))))
  (.repaint (:panel @scope*)))

(defn- paint-scope [g]
  (.setColor #^Graphics g #^Color (:background @scope*))
  (.fillRect #^Graphics g 0 0 (:width @scope*) (:height @scope*))
  (.setColor #^Graphics g #^Color (Color. 100 100 100))
  (.drawRect #^Graphics g 0 0 (:width @scope*) (:height @scope*))

  (.setColor #^Graphics g #^Color (:color @scope*))
  (.drawPolyline #^Graphics g #^ints x-array #^ints y-array (int (:width @scope*))))

(defn- clean-scope []
  (dosync
    (if (:tmp-buf @scope*)
      (buffer-free (:buf @scope*)))
    (if-let [s (:bus-synth @scope*)]
      (kill s))
    (alter scope* assoc :buf nil :buf-size 0 :tmp-buf false 
           :bus nil :bus-synth nil)))

(defn scope-buf 
  "Set a buffer to view in the scope."
  [buf]
  (clean-scope)
  (dosync (alter scope* assoc 
                 :buf buf
                 :buf-size (count (buffer-data buf))))
  (update-scope))

(defn- wait-for-buffer [b]
  (loop [i 0]
    (cond 
      (= 20 i) nil
      (not (buffer-ready? b)) (do 
                                (java.lang.Thread/sleep 50)
                                (recur (inc i))))))

(defn scope-bus
  "Set a bus to view in the scope."
  [bus]
  (clean-scope)
  (let [buf (buffer 2048)
        _ (wait-for-buffer buf)
        bus-synth (bus->buf bus (:id buf))]; :target 0 :position :tail)]
    (dosync (alter scope* assoc 
                   :buf buf
                   :buf-size 2048
                   :bus bus
                   :tmp-buf true
                   :bus-synth bus-synth))
    (call-at (+ (now) 1000) update-scope)
    (update-scope)))

(defn freq-scope-buf [buf]
  )

(defn scope-panel []
  (let [p (proxy [JPanel] []
            (paint [g] (paint-scope g)))]
    (dosync (alter scope* assoc :panel p))
    (.setMinimumSize p (Dimension. 600 400))
    p))

(dotimes [i (:width @scope*)] (aset x-array i i))

(defn scope-frame []
  (let [f (JFrame. "scope")]
    (doto f
      (.setMinimumSize (Dimension. 600 400))
      (.add (scope-panel))
      (.pack)
      (.show))))

(defn scope-on []
  (dosync (alter scope* assoc 
                 :status :on 
                 :runner (periodic update-scope (/ 1000 (:fps @scope*))))))

(defn scope-off []
  (.cancel (:runner @scope*) true)
  (dosync (alter scope* assoc
                 :status :off
                 :runner nil)))
 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Testing
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
(require 'examples.basic)

(defonce test-frame (JFrame. "scope"))
(defonce test-panel (JSGPanel.))
(defonce _test-scope (do 
             (.setPreferredSize test-panel (Dimension. 600 400))
             (.add (.getContentPane test-frame) test-panel)
             (.setScene test-panel (scope))
             (.pack test-frame)
             (.show test-frame)))

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
  (.show test-frame)))

(ns overtone.gui.scope
  (:import 
    (java.awt Graphics Dimension Color BasicStroke)
    (java.awt.geom Rectangle2D$Float Path2D$Float)
     (javax.swing JFrame JPanel) 
    (com.sun.scenario.scenegraph JSGPanel SGText SGShape SGGroup SGTransform SGComponent
                                 SGAbstractShape$Mode)
     (com.sun.scenario.scenegraph.event SGMouseAdapter))
  (:use 
     [overtone.core event sc synth util time-utils]
    clojure.stacktrace)
  (:require [overtone.core.log :as log]))

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

(defonce x-array (int-array (:width @scope*)))
(defonce _x-init (dotimes [i (:width @scope*)] 
                   (aset x-array i i)))

(defonce y-array (int-array (:width @scope*)))
(defonce _y-init (dotimes [i (:width @scope*)] 
                   (aset y-array i (/ (:height @scope*) 2))))

(def X-PADDING 5)
(def Y-PADDING 10)

(defn update-scope []
  (let [frames (buffer-data (:buf @scope*))
        step (int (/ (:buf-size @scope*) (:width @scope*)))
        y-scale (/ (- (:height @scope*) (* 2 Y-PADDING)) 2)
        y-shift (+ (/ (:height @scope*) 2) Y-PADDING)]
    (doseq [x x-array]
      (aset #^ints y-array x 
            (int (+ y-shift (* y-scale (aget #^floats frames (* x step))))))))
  (.repaint (:panel @scope*)))

(defn scope-buf [buf]
  (dosync (alter scope* assoc 
                 :buf buf
                 :buf-size (count (buffer-data buf))))
  (update-scope))

(defn paint-scope [g]
  (.setColor #^Graphics g #^Color (:background @scope*))
  (.fillRect #^Graphics g 0 0 (:width @scope*) (:height @scope*))
  (.setColor #^Graphics g #^Color (Color. 100 100 100))
  (.drawRect #^Graphics g 0 0 (:width @scope*) (:height @scope*))

  (.setColor #^Graphics g #^Color (:color @scope*))
  (.drawPolyline #^Graphics g #^ints x-array #^ints y-array (int (:width @scope*))))

(defn scope-panel []
  (proxy [JPanel] []
    (paint [g] (paint-scope g))))

(dotimes [i (:width @scope*)] (aset x-array i i))

(defn scope []
  (let [scope-node (SGComponent.)
        panel (scope-panel)]

    (dosync (alter scope* assoc :panel panel))

    (doto panel
      (.setDoubleBuffered true)
      (.setFocusTraversalKeysEnabled false))

    (doto scope-node
      (.setSize 600 400)
      (.setComponent panel))
    scope-node))

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

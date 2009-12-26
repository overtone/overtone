(ns overtone.gui.scope
  (:import 
     (java.awt Toolkit EventQueue Color Font FontMetrics Dimension BorderLayout)
     (javax.swing JFrame JPanel JLabel JTree JEditorPane JScrollPane JTextPane 
                  JSplitPane JMenuBar JMenu JMenuItem SwingUtilities)
     (org.jfree.chart ChartFactory ChartPanel)
     (org.jfree.chart.plot PlotOrientation)
     (org.jfree.data.xy DefaultXYDataset))
  (:use 
     (overtone sc synth util))
  (:require [overtone.log :as log]))

(def SCOPE-BUF-SIZE 10000)

(def scope-buf* (ref nil))
(def scope-bus* (ref 0))

(def overtone-scope (synth overtone-scope {:in 0 :buf 1}
  (record-buf.ar (in.ar :in) :buf)))

(def scope-test (synth scope-test {:out 10 :freq 220} (out.ar :out (sin-osc.ar :freq))))

(defn load-scope [bus]
  (load-synth overtone-scope)
  (load-synth scope-test)

  (if (nil? @scope-buf*)
    (dosync (ref-set scope-buf* (buffer SCOPE-BUF-SIZE))))
  (hit overtone-scope :in bus :buf (:id @scope-buf*)))

; TODO: remove all the scope ugens
;  * need to save synth objects or names stored in a lookup or something...
(defn kill-scope []
  )

(defn scope-dataset []
  (let [ds (DefaultXYDataset.)
        ary (make-array Double/TYPE 2 10000)]
    (loop [idx (range 10000)
           y (cycle (range 0 (* Math/PI 2) (/ (* Math/PI 2) 147)))]
      (when idx
        (let [i (first idx)]
          (aset-double ary 0 i (double i))
          (aset-double ary 1 i (Math/sin (first y)))
          (recur (next idx) (next y)))))
    (println "first: " (aget ary 0 0) (aget ary 1 0))
    (println "last: " (aget ary 0 1499) (aget ary 1 1499))
    (.addSeries ds "fake" ary)
    ds))

(defn get-samples []
  (buffer-read @scope-buf* 0 SCOPE-BUF-SIZE))

(defn scope [data]
  (let [frame (JFrame. "Scope")
        content (.getContentPane frame)
        chart (ChartFactory/createXYLineChart "" "" ""
                                              data PlotOrientation/VERTICAL
                                              false false false)
        chart-panel (ChartPanel. chart)]
    (doto chart
      (.setBorderVisible false))

    (doto content
      (.setLayout (BorderLayout.))
      (.add chart-panel BorderLayout/CENTER))

    (doto frame
      (.pack)
      (.setVisible true))))

(defn test-scope []
  (try 
    ;(boot)
    ;(load-scope)
    (scope (scope-dataset))))
    ;(finally (quit))))

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

(ns overtone.gui.scope
  (:import 
     (java.awt Toolkit EventQueue Color Font FontMetrics Dimension BorderLayout)
     (javax.swing JFrame JPanel JLabel JTree JEditorPane JScrollPane JTextPane 
                  JSplitPane JMenuBar JMenu JMenuItem SwingUtilities)
     (org.jfree.chart ChartFactory ChartPanel)
     (org.jfree.chart.plot PlotOrientation)
     (org.jfree.data.xy DefaultXYDataset))
  (:use clj-backtrace.repl
     (overtone sc synthdef utils)))

(def SCOPE-BUF-SIZE 4096)

(def scope-buf* (ref nil))

(defsynth overtone-scope {:in 0 :buf 1}
  (record-buf.ar (in.ar :in) :buf))

(defn load-scope [bus]
  (load-synth overtone-scope)

  (if (nil? @scope-buf*)
    (dosync (ref-set scope-buf* (buffer SCOPE-BUF-SIZE))))

  (hit overtone-scope :in bus :buf (:id @scope-buf)))

(defn make-dataset []
  (let [ds (DefaultXYDataset.)
        ary (make-array Double/TYPE 2 10000)]
    (loop [idx (range 1500)
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

(defn scope []
  (let [frame (JFrame. "Scope")
        content (.getContentPane frame)
        data (make-dataset)
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
    (boot)
    (load-scope)
    (scope)
    (finally (quit))))


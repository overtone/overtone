(ns overtone.app.main
  (:gen-class)
  (:import 
    (java.awt Toolkit EventQueue Dimension Point Dimension Color Font 
              RenderingHints Point BasicStroke BorderLayout)
    (java.awt.geom Ellipse2D$Float RoundRectangle2D$Float)
    (javax.swing JFrame JPanel JSplitPane JLabel JButton SwingUtilities BorderFactory
                 JSpinner SpinnerNumberModel) 
    (com.sun.scenario.scenegraph JSGPanel SGText SGShape SGGroup 
                                 SGAbstractShape$Mode SGComponent SGTransform)
    (com.sun.scenario.scenegraph.event SGMouseAdapter)
    (com.sun.scenario.scenegraph.fx FXShape))
  (:use (overtone.app editor tools)
        (overtone.core sc ugen synth envelope event time-utils)
        (overtone.gui scope curve)
        clj-scenegraph.core 
        clojure.stacktrace)
  (:require [overtone.core.log :as log]))

(alias 'ug 'overtone.ugens)

(defmacro in-swing [& body]
  `(SwingUtilities/invokeLater (fn [] ~@body)))

(def app* (ref {:name "Overtone"
                :padding 5.0
                :background (Color. 50 50 50)
                :header-fg (Color. 255 255 255)
                :header-font (Font. "helvetica" Font/BOLD 16)
                :header-height 20
                :status-update-period 1000
                :edit-panel-dim (Dimension. 550 900)
                :scene-panel-dim (Dimension. 615 900)
                :tools-panel-dim (Dimension. 300 900)
                }))

(defn header []
  (let [panel (JPanel.)
        bpm-lbl (JLabel. "BPM: ")
        bpm-model (SpinnerNumberModel. 120 1 400 1)
        bpm-spin (JSpinner. bpm-model)
        beat-panel (JPanel.)
        border (BorderFactory/createEmptyBorder 2 5 2 5)
        ugen-lbl (JLabel. "ugens: 0")
        synth-lbl (JLabel. "synths: 0")
        group-lbl (JLabel. "groups: 0")
        cpu-lbl (JLabel. "avg-cpu: 0.00")
        lbl-panel (JPanel.)
        updater (fn [] (let [sts (status)]
                           (in-swing
                             (.setText ugen-lbl  (format "ugens: %4d" (:n-ugens sts)))
                             (.setText synth-lbl (format "synths: %4d" (:n-synths sts)))
                             (.setText group-lbl (format "groups: %4d" (:n-groups sts)))
                             (.setText cpu-lbl   (format "avg-cpu: %4.2f" (:avg-cpu sts))))))
        help-btn (JButton. "Help")
        quit-btn (JButton. "Quit")
        btn-panel (JPanel.)]

    (.setForeground bpm-lbl (:header-fg @app*))

    (doto beat-panel
      (.setBackground (:background @app*))
      (.add bpm-lbl)
      (.add bpm-spin))

    (doto lbl-panel
      (.setBackground (:background @app*))
      (.add ugen-lbl )
      (.add synth-lbl)
      (.add group-lbl)
      (.add cpu-lbl))

    (doseq [lbl [ugen-lbl synth-lbl group-lbl cpu-lbl]]
      (.setBorder lbl border)
      (.setForeground lbl (:header-fg @app*)))

    (on :connected #(periodic updater (:status-update-period @app*)))

    (doto btn-panel
      (.setBackground (:background @app*))
      (.add help-btn)
      (.add quit-btn))

    (doto panel
      (.setLayout (BorderLayout.))
      (.setBackground (:background @app*))
      (.add beat-panel BorderLayout/WEST)
      (.add lbl-panel BorderLayout/CENTER)
      (.add btn-panel BorderLayout/EAST))

    (dosync (alter app* assoc :header panel))
    panel))

(defn overtone-scene [args]
  (let [root (sg-group)]
    (doto root
      (add! (translate (:padding @app*) 0.0 (scope)))
      (add! (translate (:padding @app*) (+ 400.0 (:padding @app*)) (curve-editor))))
    (dosync (alter app* assoc :scene-group root))
    root))

(defn screen-dim []
  (.getScreenSize (Toolkit/getDefaultToolkit)))

(defn screen-size []
  (let [dim (screen-dim)]
    [(.width dim) (.height dim)]))

;TODO: It undecorates, but it doesn't seem to change the size of the frame...
(defn fullscreen-frame [f]
    (.setExtendedState f JFrame/MAXIMIZED_BOTH)
    (.setUndecorated f true))

(defn -main [& args]
  (let [app-frame (JFrame.  "Project Overtone")
        app-panel (.getContentPane app-frame)
        ;browse-panel (browser)
        header-panel (header)
        edit-panel (editor-panel)
        scene-panel (JSGPanel.)
        tools-panel (tool-panel @app*)]
        ;left-split (JSplitPane. JSplitPane/HORIZONTAL_SPLIT browse-panel edit-panel)]

    (when (not (connected?))
      (boot)
      (Thread/sleep 1000))

    (doto edit-panel
      (.setPreferredSize (:edit-panel-dim @curve*)))

    (doto scene-panel
      (.setBackground Color/BLACK)
      (.setScene (overtone-scene args))
      (.setPreferredSize (:scene-panel-dim @curve*)))

    (doto tools-panel
      (.setPreferredSize (:tools-panel-dim @curve*)))

    (doto app-panel
      (.setLayout (BorderLayout.))
      (.add header-panel BorderLayout/NORTH)
      (.add edit-panel BorderLayout/WEST)
      (.add scene-panel BorderLayout/CENTER)
      (.add tools-panel BorderLayout/EAST))

    ;(.setDividerLocation left-split 0.4)

    (doto app-frame
      (.pack)
      (.setVisible true))))

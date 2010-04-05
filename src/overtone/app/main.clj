(ns overtone.app.main
  (:gen-class)
  (:import 
    (java.awt Toolkit EventQueue Dimension Point Dimension Color Font 
              RenderingHints Point BasicStroke BorderLayout)
    (java.awt.geom Ellipse2D$Float RoundRectangle2D$Float)
    (javax.swing JFrame JPanel JSplitPane JLabel JButton BorderFactory
                 JSpinner SpinnerNumberModel) 
    (com.sun.scenario.scenegraph JSGPanel SGText SGShape SGGroup 
                                 SGAbstractShape$Mode SGComponent SGTransform)
    (com.sun.scenario.scenegraph.event SGMouseAdapter)
    (com.sun.scenario.scenegraph.fx FXShape))
  (:use (overtone.app editor tools)
        (overtone.core sc ugen synth envelope event time-utils)
        (overtone.gui scope curve utils)
        clj-scenegraph.core 
        clojure.stacktrace)
  (:require [overtone.core.log :as log]))

(alias 'ug 'overtone.ugens)

(def app* (ref {:name "Overtone"
                :padding 5.0
                :background (Color. 50 50 50)
                :foreground (Color. 255 255 255)
                :header-fg (Color. 255 255 255)
                :header-font (Font. "helvetica" Font/BOLD 16)
                :header-height 20
                :status-update-period 1000
                :edit-font (Font. "helvetica" Font/PLAIN 12)
                :edit-panel-dim (Dimension. 550 900)
                :scene-panel-dim (Dimension. 615 900)
                :tools-panel-dim (Dimension. 300 900)
                }))

(defn metro-panel []
  (let [bpm-lbl (JLabel. "BPM: ")
        bpm-model (SpinnerNumberModel. 120 1 400 1)
        bpm-spin (JSpinner. bpm-model)
        beat-panel (JPanel.)]
    (.setForeground bpm-lbl (:header-fg @app*))
    (doto beat-panel
      (.setBackground (:background @app*))
      (.add bpm-lbl)
      (.add bpm-spin))))

(defn status-panel []
  (let [ugen-lbl (JLabel. "ugens: 0")
        synth-lbl (JLabel. "synths: 0")
        group-lbl (JLabel. "groups: 0")
        cpu-lbl (JLabel. "avg-cpu: 0.00")
        border (BorderFactory/createEmptyBorder 2 5 2 5)
        lbl-panel (JPanel.)
        updater (fn [] (let [sts (status)]
                           (in-swing
                             (.setText ugen-lbl  (format "ugens: %4d" (:n-ugens sts)))
                             (.setText synth-lbl (format "synths: %4d" (:n-synths sts)))
                             (.setText group-lbl (format "groups: %4d" (:n-groups sts)))
                             (.setText cpu-lbl   (format "avg-cpu: %4.2f" (:avg-cpu sts))))))]
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
    lbl-panel))

(defn header []
  (let [panel (JPanel.)
        metro (metro-panel)
        status (status-panel)
        help-btn (JButton. "Help")
        quit-btn (JButton. "Quit")
        btn-panel (JPanel.)]

    (doto btn-panel
      (.setBackground (:background @app*))
      (.add help-btn)
      (.add quit-btn))

    (doto panel
      (.setLayout (BorderLayout.))
      (.setBackground (:background @app*))
      (.add metro BorderLayout/WEST)
      (.add status BorderLayout/CENTER)
      (.add btn-panel BorderLayout/EAST))

    (dosync (alter app* assoc :header panel))
    panel))

(defn overtone-scene []
  (let [root (sg-group)]
    (doto root
      (add! (translate (:padding @app*) 0.0 (scope)))
      (add! (translate (:padding @app*) (+ 400.0 (:padding @app*)) (curve-editor))))
    (dosync (alter app* assoc :scene-group root))
    root))

(defn overtone-frame []
  (let [app-frame (JFrame.  "Project Overtone")
        app-panel (.getContentPane app-frame)
        ;browse-panel (browser)
        header-panel (header)
        edit-panel (editor-panel @app*)
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
      (.setScene (overtone-scene))
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

(defn -main [& args]
  (in-swing (overtone-frame)))

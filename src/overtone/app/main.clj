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
    (com.sun.scenario.scenegraph.fx FXShape)
     (scenariogui.synth ISynth ISynthParameter SynthControl))
  (:use (overtone.app editor browser)
        (overtone.core sc ugen synth envelope event time-utils)
        (overtone.gui scope curve)
        clojure.stacktrace)
  (:require [overtone.core.log :as log]))

(alias 'ug 'overtone.ugens)

(defmacro in-swing [& body]
  `(SwingUtilities/invokeLater (fn [] ~@body)))

(def app* (ref {:name "Overtone"
                :header-bg (Color. 50 50 50)
                :header-fg (Color. 255 255 255)
                :header-font (Font. "helvetica" Font/BOLD 16)
                :header-height 20
                :status-update-period 1000
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
        cpu-lbl (JLabel. "avg-cpu: 0")
        lbl-panel (JPanel.)
        updater (fn [] (let [sts (status)]
                           (in-swing
                             (.setText ugen-lbl  (format "ugens: %4d" (:n-ugens sts)))
                             (.setText synth-lbl (format "synths: %4d" (:n-synths sts)))
                             (.setText group-lbl (format "groups: %4d" (:n-groups sts)))
                             (.setText cpu-lbl   (format "avg-cpu: $4.2f" (:avg-cpu sts))))))
        help-btn (JButton. "Help")
        quit-btn (JButton. "Quit")
        btn-panel (JPanel.)]

    (.setForeground bpm-lbl (:header-fg @app*))

    (doto beat-panel
      (.setBackground (:header-bg @app*))
      (.add bpm-lbl)
      (.add bpm-spin))

    (doto lbl-panel
      (.setBackground (:header-bg @app*))
      (.add ugen-lbl )
      (.add synth-lbl)
      (.add group-lbl)
      (.add cpu-lbl))

    (doseq [lbl [ugen-lbl synth-lbl group-lbl cpu-lbl]]
      (.setBorder lbl border)
      (.setForeground lbl (:header-fg @app*)))

    (on :connected #(periodic updater (:status-update-period @app*)))

    (doto btn-panel
      (.setBackground (:header-bg @app*))
      (.add help-btn)
      (.add quit-btn))

    (doto panel
      (.setLayout (BorderLayout.))
      (.setBackground (:header-bg @app*))
      (.add beat-panel BorderLayout/WEST)
      (.add lbl-panel BorderLayout/CENTER)
      (.add btn-panel BorderLayout/EAST))

    (dosync (alter app* assoc :header panel))
    
    panel))

(defn make-synth-param [[name start end step]]
  (proxy [ISynthParameter] []
    (getName [] name)
    (getStart [] start)
    (getEnd [] end)
    (getStep [] step)))

(on :connected #(defsynth foo-bass [freq 400 dur 0.2] 
                  (ug/* (ug/env-gen (perc 0.1 dur) 1 1 0 1 :free) (ug/lpf (ug/saw freq) freq))))

(defn make-synth []
  (let [s-fn foo-bass]
    (proxy [ISynth] []
      (getParams [] (into-array (map make-synth-param [["freq" 0.0 1200.0 1.0] ["dur" 0.01 10.0 0.1]])))
      (getName [] (str (:name (meta s-fn))))
      (play [vals] (apply s-fn (seq vals)))
      (kill [] ())
      (control [param value] (s-fn :ctl (keyword param) value)))))

(def scene-root* (ref nil))

(defn synth-control []
  (.add @scene-root* (SynthControl. (make-synth))))

(defn overtone-scene [args]
  (let [root (SGGroup.)]
    (doto root
      (.add (scope))
      (.add (SGTransform/createTranslation 0.0 450.0 (curve-editor))))
      ;(.add (header))
      (dosync (ref-set scene-root* root))
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
        app-pane (.getContentPane app-frame)
        browse-panel (browser)
        header-panel (header)
        scene-panel (JSGPanel.)
        edit-panel (editor-panel)
        left-split (JSplitPane. JSplitPane/HORIZONTAL_SPLIT browse-panel edit-panel)]

    (when (not (connected?))
      (boot)
      (Thread/sleep 1000))

    (doto scene-panel
      (.setBackground Color/BLACK)
      (.setScene (overtone-scene args))
      (.setPreferredSize (Dimension. 600 900)))

    (doto app-pane
      (.setLayout (BorderLayout.))
      (.add header-panel BorderLayout/NORTH)
      (.add left-split BorderLayout/CENTER)
      (.add scene-panel BorderLayout/EAST))

    (.setDividerLocation left-split 0.4)

    (doto app-frame
      (.pack)
      (.setVisible true))))

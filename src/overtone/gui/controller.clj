(ns overtone.gui.controller
  (:import
     (java.awt Toolkit EventQueue Color Dimension BorderLayout)
     (javax.swing JFrame JPanel JLabel JSlider JScrollPane SwingUtilities BoxLayout)
     (javax.swing.event ChangeListener))
  (:use
     (overtone.core sc synth synthdef util time-utils)
     clojure.contrib.swing-utils))

(defn determine-range [val]
  (cond
    (< val 1.0) [0.0 1.0]
    (< val 10.0) [0.0 10.0]
    (< val 100.0) [0.0 100.0]
    (< val 1000.0) [0.0 1000.0]
    (< val 10000.0) [0.0 10000.0]))

(def SLIDER-MAX 100000)

(defn add-change-listener [component f & args]
  (let [listener (proxy [ChangeListener] []
                   (stateChanged [event] (apply f event args)))]
    (.addChangeListener component listener)
    listener))

(defn slider-panels [synth-id infos]
  (for [[ctl-name ctl-default] infos]
    (let [[low hi] (determine-range ctl-default)
          factor (/ SLIDER-MAX hi)
          scaled-default (* ctl-default factor)
          slider (JSlider. 0 SLIDER-MAX scaled-default)
          label (JLabel. (str scaled-default))
          pane (JPanel. (BorderLayout.))]
      (add-change-listener slider
                           (fn [event]
                             (let [new-val (float (int (/ (.getValue slider) factor)))]
                               (.setText label (str new-val))
                               (ctl (now) synth-id ctl-name new-val))))
      (doto pane
        (.add slider BorderLayout/CENTER)
        (.add label BorderLayout/SOUTH)))))

(defn controller [synth-id sdef]
  (let [frame (JFrame. (str (:name sdef) " controller"))
        content (.getContentPane frame)
        ctl-panel (JPanel.)
        layout (BoxLayout. ctl-panel BoxLayout/X_AXIS)
        ctl-info (synth-controls sdef)
        slider-panels (slider-panels synth-id ctl-info)]

    (doto ctl-panel
      (.setLayout layout))

    (doseq [slider slider-panels]
      (.add ctl-panel slider))

    (doto content
      (.setLayout (BorderLayout.))
      (.add ctl-panel BorderLayout/CENTER))

    (doto frame
      (.pack)
      (.setVisible true))))


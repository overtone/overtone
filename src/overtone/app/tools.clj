(ns overtone.app.tools
  (:import 
    (java.awt Toolkit EventQueue Dimension Point Dimension Color Font 
              RenderingHints Point BasicStroke BorderLayout)
    (java.awt.geom Ellipse2D$Float RoundRectangle2D$Float)
    (javax.swing JPanel JLabel JButton SwingUtilities BorderFactory
                 JSpinner SpinnerNumberModel JColorChooser BorderFactory
                 BoxLayout)
    (javax.swing.event ChangeListener))
  (:use (overtone.core event))) 

(def tools* (ref {:current-color (Color. 0 130 226)}))

(def color-handler* (ref nil))

(defn stop-color []
  (remove-handler :color-changed @color-handler*)
  (dosync (ref-set color-handler* nil)))

(defn live-color [handler]
  (stop-color)
  (dosync (ref-set color-handler* handler))
  (on :color-changed #(handler (:color %))))

(def chooser* (ref nil))

(defn color-selector [app]
  (let [color-chooser (JColorChooser. (:current-color @tools*))
        border (BorderFactory/createTitledBorder "Color")
        choosers (.getChooserPanels color-chooser)]
    (println "chooser: " (count choosers))
    (dosync (ref-set chooser* color-chooser))

    (.setTitleColor border (:foreground app))

    (doto color-chooser
      (.setBorder border) 
      (.setBackground (:background app))
      (.setForeground (:foreground app))
      (.setChooserPanels (into-array [(first choosers)]))
      (.setPreviewPanel (JPanel.)))

    (doseq [cp (.getChooserPanels color-chooser)]
      (doto cp
        (.setBackground (:background app))
        (.setForeground (:foreground app)))
      (doseq [comp (seq (.getComponents cp))] 
        (.setBackground comp (Color. 50 50 50))))

    (-> color-chooser
      (.getSelectionModel)
      (.addChangeListener 
        (proxy [ChangeListener] []
          (stateChanged [_] (event :color-changed 
                                   :color (.getColor color-chooser))))))
    color-chooser))

(defn log-viewer [app]
  (let [panel (JPanel.)]

    panel))

(defn tool-panel [app]
  (let [panel (JPanel.)
        layout (BoxLayout. panel BoxLayout/Y_AXIS)
        color-chooser (color-selector app)
        log-view (log-viewer app)
        filler (JPanel.)]

    (doto panel
      (.setBackground (:background app))
      (.setForeground (:foreground app))
      
      (.add color-chooser)
      (.add log-view)
      (.add filler))

    panel))

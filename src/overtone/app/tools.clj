(ns overtone.app.tools
  (:import 
    (java.awt Toolkit EventQueue Dimension Point Dimension Color Font 
              RenderingHints Point BasicStroke BorderLayout)
    (java.awt.geom Ellipse2D$Float RoundRectangle2D$Float)
    (javax.swing JPanel JLabel JButton SwingUtilities BorderFactory
                 JSpinner SpinnerNumberModel JColorChooser)
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

(defn tool-panel [app]
  (let [panel (JPanel.)
        color-chooser (JColorChooser. (:current-color @tools*))]

    (doto color-chooser
      (.setBackground (:background app))
      (.setChooserPanels (into-array [(second (.getChooserPanels color-chooser))]))
      (.setPreviewPanel (JPanel.)))

    (-> color-chooser
      (.getSelectionModel)
      (.addChangeListener 
        (proxy [ChangeListener] []
          (stateChanged [_] (event :color-changed 
                                   :color (.getColor color-chooser))))))

    (.add panel color-chooser BorderLayout/CENTER)
    panel))

(ns overtone.app.tools
  (:import 
    (java.awt Toolkit EventQueue Dimension Point Dimension Color Font 
              RenderingHints Point BasicStroke BorderLayout)
    (java.awt.geom Ellipse2D$Float RoundRectangle2D$Float)
    (javax.swing JPanel JLabel JButton SwingUtilities 
                 JSpinner SpinnerNumberModel JColorChooser 
                 BoxLayout JTextArea JScrollPane)
    (javax.swing.event ChangeListener)
    (java.util.logging StreamHandler SimpleFormatter))
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

(defn color-panel [app]
  (let [color-chooser (JColorChooser. (:current-color @tools*))
        choosers (.getChooserPanels color-chooser)]
    (println "chooser: " (count choosers))
    (dosync (ref-set chooser* color-chooser))

    (doto color-chooser
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

(defn- log-view-handler [text-area]
  (let [formatter (SimpleFormatter.)]
    (proxy [StreamHandler] []
      (publish [msg] (.append text-area (.format formatter msg))))))

(defn log-view-panel [app]
  (let [text-area (JTextArea. "Overtone Log:\n" 10 40)
        scroller (JScrollPane. text-area)]
    scroller))

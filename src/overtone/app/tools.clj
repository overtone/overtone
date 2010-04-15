(ns overtone.app.tools
  (:import 
    (java.awt Toolkit EventQueue Dimension Point Dimension Color Font 
              RenderingHints Point BasicStroke BorderLayout)
    (java.awt.geom Ellipse2D$Float RoundRectangle2D$Float)
    (javax.swing JPanel JLabel JButton SwingUtilities 
                 JSpinner SpinnerNumberModel JColorChooser 
                 BoxLayout JTextArea JScrollPane JTable)
    (javax.swing.event ChangeListener)
    (javax.swing.table AbstractTableModel)
    (java.util.logging StreamHandler SimpleFormatter))
  (:use (overtone.core event)
        (overtone.app editor)))

(def tools* (ref {:current-color (Color. 0 130 226)}))

(def color-handler* (ref nil))

(defn stop-color []
  (remove-handler :color-changed @color-handler*)
  (dosync (ref-set color-handler* nil)))

(defn live-color [handler]
  (stop-color)
  (dosync (ref-set color-handler* handler))
  (on :color-changed #(handler (:color %))))

(defn color-panel [app]
  (let [color-chooser (JColorChooser. (:current-color @tools*))
        choosers (.getChooserPanels color-chooser)]
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

(defn keymap-table-model []
  (let [keymap #(get @editor* (:current-keymap @editor*))]
    (proxy [AbstractTableModel] []
      (getColumnName [c] (get ["Actions", "Key Strokes"] c))
      (getRowCount [] (count (keymap)))
      (getColumnCount [] 2)
      (getValueAt [row col] (get (first 
                                   (drop row (seq (keymap)))) 
                                 col))
      (getColumnClass [c] (class (get (first (seq (keymap))) c)))
      (isCellEditable [row col] (= 1 col))
      (setValueAt [val row col]
                  (println "setting binding: " row ":" col "-> " val)))))

(defn keymap-panel [app]
  (let [km (get @editor* (:current-keymap @editor*))
        table (JTable. (keymap-table-model))
        scroller (JScrollPane. table)]
    scroller))

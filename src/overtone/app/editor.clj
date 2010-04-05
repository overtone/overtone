(ns overtone.app.editor
  (:import 
     (java.awt EventQueue Color Font FontMetrics Dimension BorderLayout 
               GridBagLayout GridBagConstraints Insets)
     (javax.swing JFrame JPanel JLabel JTree JEditorPane JScrollPane JTextPane 
                  JSplitPane JButton JFileChooser) 
     (com.sun.scenario.scenegraph JSGPanel SGText SGShape SGGroup 
                                  SGAbstractShape$Mode SGComponent SGTransform)
     (jsyntaxpane DefaultSyntaxKit))
  (:use (overtone.gui utils)
        [clojure.contrib.fcase :only (case)]
        (clojure.contrib swing-utils)))

(def TAB-STOP 4)
(def CARET-COLOR Color/BLACK)

(def editor* (ref {}))

(defn- status-panel [editor]
  (let [status-pane (JPanel.)
        general-status (JLabel. "general status")
        stroke-status (JLabel. "stroke status")
        mode-status (JLabel. "mode-status")]
;        status-display (-> (ViManager/getViTextView editor) (.getStatusDisplay))]
    
    ; Hookup the status bar labels to the Vi machinery
 ;   (set! (.generalStatus status-display) general-status)
 ;   (set! (.strokeStatus status-display) stroke-status)
 ;   (set! (.modeStatus status-display) mode-status)

    (doto status-pane
      (.setLayout (GridBagLayout.))
      (.add general-status (GridBagConstraints. 0 0 1 1 1.0 0.0 GridBagConstraints/WEST 
                                               GridBagConstraints/HORIZONTAL, (Insets. 0 0 0 0) 111 0))
      (.add stroke-status (GridBagConstraints. 1 0 1 1 0.0 0.0
                                               GridBagConstraints/WEST
                                               GridBagConstraints/VERTICAL
                                               (Insets. 0 2 0 0) 0 0))
      (.add mode-status (GridBagConstraints. 2 0 1 1 0.0 0.0
                                             GridBagConstraints/WEST 
                                             GridBagConstraints/VERTICAL 
                                             (Insets. 0 2 0 0) 0 0)))))

(defn file-open-dialog [parent & [path]]
  (let [chooser (if path
                  (JFileChooser. path)
                  (JFileChooser.))
        ret (.showOpenDialog chooser parent)]
    (case ret
      JFileChooser/APPROVE_OPTION (-> chooser (.getSelectedFile) (.getName))
      JFileChooser/CANCEL_OPTION nil
      JFileChooser/ERROR_OPTION  nil)))

(defn file-save-dialog [parent & [path]]
  (let [chooser (if path
                  (JFileChooser. path)
                  (JFileChooser.))
        ret (.showSaveDialog chooser parent)]
    (case ret
      JFileChooser/APPROVE_OPTION (-> chooser (.getSelectedFile) (.getName))
      JFileChooser/CANCEL_OPTION nil
      JFileChooser/ERROR_OPTION  nil)))

(defn button-bar [editor]
  (let [panel (JPanel.)
        open (JButton. (icon "org/freedesktop/tango/16x16/actions/document-open.png"))
        save (JButton. (icon "org/freedesktop/tango/16x16/actions/document-save.png"))]

    (add-action-listener open (fn [_]
                                (.setText editor 
                                          (slurp (file-open-dialog editor)))))
    (add-action-listener save (fn [_]
                                (file-save-dialog editor (.getText editor))))

    (doto panel
      (.add open)
      (.add save))

    panel))

(defn editor-panel [app]
  (let [editor-pane (JPanel.)
        editor (JEditorPane.)
        button-pane (button-bar editor)
        scroller (JScrollPane. editor)
        font (.getFont editor)
        fm (.getFontMetrics editor font)
        width (* 81 (.charWidth fm \space))
        height (* 10 (.getHeight fm))]
    (DefaultSyntaxKit/initKit)

    (doto button-pane
      (.setBackground (:background app)))

    (doto editor
      (.setFont (:edit-font app))
      (.setContentType "text/clojure")
      (.setText (slurp "src/examples/basic.clj"))
      (.setCaretColor CARET-COLOR)
      (.requestFocusInWindow))

    (doto editor-pane
      (.setLayout (BorderLayout.))
      (.add button-pane BorderLayout/NORTH)
      (.add scroller BorderLayout/CENTER)
      (.add (status-panel editor) BorderLayout/SOUTH))))

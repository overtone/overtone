(ns overtone.app.editor
  (:import 
    (java.awt EventQueue Color Font FontMetrics Dimension BorderLayout 
              GridBagLayout GridBagConstraints Insets FlowLayout)
    (java.awt.event InputEvent)
    (javax.swing JFrame JPanel JLabel JTree JEditorPane JScrollPane JTextPane 
                 JSplitPane JButton JFileChooser KeyStroke) 
    (javax.swing.text TextAction)
    (com.sun.scenario.scenegraph JSGPanel SGText SGShape SGGroup 
                                 SGAbstractShape$Mode SGComponent SGTransform)
    (jsyntaxpane DefaultSyntaxKit))
  (:use (overtone.core event)
        (overtone.gui swing)
        [clojure.contrib.fcase :only (case)]
        (clojure.contrib swing-utils duck-streams)))

(def TAB-STOP 4)
(def CARET-COLOR Color/BLACK)

(defonce editor* (ref {}))

(defn- status-panel [editor]
  (let [status-pane (JPanel.)
        general-status (JLabel. "general status")
        stroke-status (JLabel. "stroke status")
        mode-status (JLabel. "mode-status")]
    
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
      JFileChooser/APPROVE_OPTION (-> chooser (.getSelectedFile) (.getAbsolutePath))
      JFileChooser/CANCEL_OPTION nil
      JFileChooser/ERROR_OPTION  nil)))

(defn file-save-dialog [parent & [path]]
  (let [chooser (if path
                  (JFileChooser. path)
                  (JFileChooser.))
        ret (.showSaveDialog chooser parent)]
    (case ret
      JFileChooser/APPROVE_OPTION (-> chooser (.getSelectedFile) (.getAbsolutePath))
      JFileChooser/CANCEL_OPTION nil
      JFileChooser/ERROR_OPTION  nil)))

(defn button-bar [editor]
  (let [panel (JPanel. (FlowLayout. FlowLayout/LEFT))
        open (JButton. (icon "org/freedesktop/tango/16x16/actions/document-open.png"))
        save (JButton. (icon "org/freedesktop/tango/16x16/actions/document-save.png"))]

    (.setToolTipText open "Open a file")
    (.setToolTipText save "Save the current file")

    (add-action-listener open (fn [_]
                                (if-let [path (file-open-dialog editor)]
                                  (.setText editor (slurp path)))))
    (add-action-listener save (fn [_]
                                (if-let [path (file-save-dialog editor)]
                                  (spit path (.getText editor)))))

    (doto panel
      (.add open)
      (.add save))

    panel))

(defn eval-action []
  (proxy [TextAction] ["EVAL"]
    (actionPerformed [e] (event :overtone.gui.repl/write 
                                :text (.getSelectedText (:editor @editor*))))))

(defn editor-panel [app]
  (let [editor-pane (JPanel.)
        editor (JEditorPane.)
        button-pane (button-bar editor)
        scroller (JScrollPane. editor)
        font (.getFont editor)
        fm (.getFontMetrics editor font)
        width (* 81 (.charWidth fm \space))
        height (* 10 (.getHeight fm))
        eval-ks (KeyStroke/getKeyStroke \e InputEvent/CTRL_DOWN_MASK)]
    (DefaultSyntaxKit/initKit)

    (dosync (alter editor* assoc :editor editor))

    (doto button-pane
      (.setBackground (:background app)))

    (doto editor
      (.setFont (:edit-font app))
      (.setContentType "text/clojure")
      (.setText (slurp "src/examples/basic.clj"))
      (.setCaretColor CARET-COLOR)
      (.requestFocusInWindow))

    ; Add an eval key stroke action for CTRL-e
    (doto (.getKeymap editor)
      (.removeKeyStrokeBinding eval-ks)
      (.addActionForKeyStroke eval-ks (eval-action)))

    (doto editor-pane
      (.setLayout (BorderLayout.))
      (.add button-pane BorderLayout/NORTH)
      (.add scroller BorderLayout/CENTER)
      (.add (status-panel editor) BorderLayout/SOUTH))))

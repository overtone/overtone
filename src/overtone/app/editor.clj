(ns overtone.app.editor
  (:import
    (java.awt EventQueue Color Font FontMetrics Dimension BorderLayout
              GridBagLayout GridBagConstraints Insets FlowLayout)
    (java.awt.event InputEvent)
    (javax.swing JFrame JPanel JLabel JTree JEditorPane JScrollPane JTextPane
                 JSplitPane JButton JFileChooser KeyStroke)
    (javax.swing.text TextAction JTextComponent)
    (com.sun.scenario.scenegraph JSGPanel SGText SGShape SGGroup
                                 SGAbstractShape$Mode SGComponent SGTransform)
    (jsyntaxpane DefaultSyntaxKit))
  (:use (overtone.core event util)
        (overtone.gui swing)
        (overtone.app.editor keymap)
        [clojure.contrib.fcase :only (case)]
        (clojure.contrib swing-utils duck-streams)))

(def TAB-STOP 4)
(def CARET-COLOR Color/BLACK)

(defonce editor* (ref {}))

(load "editor/actions")

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
        save (JButton. (icon "org/freedesktop/tango/16x16/actions/document-save.png"))
        save-as (JButton. (icon "org/freedesktop/tango/16x16/actions/document-save-as.png"))]

    (.setToolTipText open "Open")
    (.setToolTipText save "Save")
    (.setToolTipText save-as "Save as")

    (add-action-listener open (fn [_]
                                (if-let [path (file-open-dialog 
                                                editor
                                                (:current-dir @editor*))]
                                  (file-open path))))
    (add-action-listener save (fn [_] (file-save)))
    (add-action-listener save-as (fn [_]
                                (if-let [path (file-save-dialog editor)]
                                  (file-save-as path))))
    (doto panel
      (.add open)
      (.add save)
      (.add save-as))
    panel))

(defn editor-panel [app]
    (DefaultSyntaxKit/initKit)
  (let [editor-pane (JPanel.)
        editor (JEditorPane.)
        button-pane (button-bar editor)
        scroller (JScrollPane. editor)
        font (.getFont editor)
        fm (.getFontMetrics editor font)
        width (* 81 (.charWidth fm \space))
        height (* 10 (.getHeight fm))
        insert-mode-map (keymap-for editor)
        ]

    (dosync (alter editor* assoc :editor editor
                   :keymaps {:insert insert-mode-map}
                   :current-keymap :insert))

    (doto button-pane
      (.setBackground (:background app)))

    (doto insert-mode-map
      (assoc (key-stroke "control E")
             (text-action #(event :overtone.gui.repl/repl-write
                                  :text (.trim (.getSelectedText (:editor @editor*)))))))

    (doto editor
      (.setKeymap (:keymap insert-mode-map))
      (.setFont (:edit-font app))
      (.setContentType "text/clojure")
      (.setCaretColor CARET-COLOR)
      (.setBackground (Color. (float 1.0) (float 1.0) (float 1.0)))
      (.requestFocusInWindow))

;    (file-open "src/examples/basic.clj")

    (doto editor-pane
      (.setLayout (BorderLayout.))
      (.add button-pane BorderLayout/NORTH)
      (.add scroller BorderLayout/CENTER)
      (.add (status-panel editor) BorderLayout/SOUTH))))

(defn editor-keymap [k]
  (.setKeymap (:editor @editor*) (:keymap k)))

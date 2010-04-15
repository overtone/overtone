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
        (overtone.app.editor keymap actions)
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
      (.setText (slurp "src/examples/basic.clj"))
      (.setCaretColor CARET-COLOR)
      (.setBackground (Color. (float 1.0) (float 1.0) (float 1.0)))
      (.requestFocusInWindow))

    (doto editor-pane
      (.setLayout (BorderLayout.))
      (.add button-pane BorderLayout/NORTH)
      (.add scroller BorderLayout/CENTER)
      (.add (status-panel editor) BorderLayout/SOUTH))))

(defn editor-keymap [k]
  (.setKeymap (:editor @editor*) (:keymap k)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Text Selection
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn selected-text
  "Returns the currently selected text."
  []
  (.getSelectedText (:editor @editor*)))

(defn select-all 
  "Selects all of the text."
  []
  (.selectAll (:editor @editor*)))

(defn select-start
  "Get the starting point of the current selection."
  []
  (.getSelectionStart (:editor @editor*)))

(defn set-select-start
  "Set the starting point of the current selection."
  [start]
  (.setSelectionStart (:editor @editor*) start))

(defn select-end
  "Get the end point of the current selection."
  []
  (.getSelectionEnd (:editor @editor*)))

(defn set-select-end
  "Set the end point of the current selection."
  [end]
  (.setSelectionEnd (:editor @editor*) end))

(defn select-range 
  "Set the current selection range."
  ([start end]
   (.select (:editor @editor*) start end)))

(defn selected-text-color
  "Get the selection color, a java.awt.Color."
  []
  (.getSelectedTextColor (:editor @editor*)))

(defn set-selected-text-color
  "Set the selection color with a java.awt.Color."
  [c]
  (.setSelectedTextColor (:editor @editor*) c))

(defn highlight-color
  "Get the selection color, a java.awt.Color."
  []
  (.getSelectionColor (:editor @editor*)))

(defn set-highlight-color
  "Set the selection color with a java.awt.Color."
  [c]
  (.setSelectionTextColor (:editor @editor*) c))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Selection operations
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn text-cut
  "cut"
  []
  (.cut (:editor @editor*)))

(defn text-copy
  "copy"
  []
  (.copy (:editor @editor*)))

(defn text-paste
  "paste"
  []
  (.paste (:editor @editor*)))

(defn text-replace
  "Replace the current selection text."
  [s]
  (.replaceSelection (:editor @editor*) s))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The caret
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn caret-color 
  "Get the caret's java.awt.Color."
  []
  (.getCaretColor (:editor @editor*)))

(defn set-caret-color 
  "Set the caret's java.awt.Color."
  [c]
  (.setCaretColor (:editor @editor*) c))

(defn caret-position
  "Get the caret's position."
  []
  (.getCaretPosition (:editor @editor*)))

(defn set-caret-position
  "Set the caret's position."
  [index]
  (.setCaretPosition (:editor @editor*) index))

(defn move-caret
  "Shift the caret's position."
  [amount]
  (.moveCaretPosition (:editor @editor*) amount))

(defn current-line
  "The line of text containing the caret."
  []
  (.getLineAt (.getDocument (:editor @editor*)) (caret-position)))

(defn remove-current-line
  "Delete the current line of text."
  []
  (.removeLineAt (.getDocument (:editor @editor*)) (caret-position)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Search
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


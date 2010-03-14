(ns overtone.app.editor
  (:import 
     (java.awt Toolkit EventQueue Color Font FontMetrics Dimension BorderLayout 
               GridBagLayout GridBagConstraints Insets)
     (javax.swing JFrame JPanel JLabel JTree JEditorPane JScrollPane JTextPane 
                  JSplitPane JMenuBar JMenu JMenuItem SwingUtilities) 
     (com.sun.scenario.scenegraph JSGPanel SGText SGShape SGGroup 
                                  SGAbstractShape$Mode SGComponent SGTransform)
     (jsyntaxpane DefaultSyntaxKit)))

(def DEFAULT-FONT "Sans")
(def DEFAULT-FONT-SIZE 12)

(def TAB-STOP 4)
(def CARET-COLOR Color/BLACK)

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

(defn editor-panel []
  (let [editor-pane (JPanel.)
        editor (JTextPane.)
        scroller (JScrollPane. editor)
        font (Font. DEFAULT-FONT 
                    Font/PLAIN
                    DEFAULT-FONT-SIZE)
        _ (.setFont editor font)
        font (.getFont editor)
        fm (.getFontMetrics editor font)
        width (* 81 (.charWidth fm \space))
        height (* 10 (.getHeight fm))]
;    (DefaultSyntaxKit/initKit)

    (doto editor
      (.setContentType "text/clojure")
      (.setText "(defn foo [a b] (dosync (ref-set asdf* (+ a b))))")
      (.setCaretColor CARET-COLOR)
      (.requestFocusInWindow))

    (doto editor-pane
      (.setLayout (BorderLayout.))
      (.add scroller BorderLayout/CENTER)
      (.add (status-panel editor) BorderLayout/SOUTH))))

(defn editor []
  (let [edit-node (SGComponent.)]
    (doto edit-node
      (.setSize 500 500)
      (.setComponent (editor-panel)))
    (SGTransform/createTranslation 100 100 edit-node)))

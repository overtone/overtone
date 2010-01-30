(ns overtone.app.editor
  (:import 
     (java.awt Toolkit EventQueue Color Font FontMetrics Dimension BorderLayout)
     (javax.swing JFrame JPanel JLabel JTree JEditorPane JScrollPane JTextPane 
                  JSplitPane JMenuBar JMenu JMenuItem SwingUtilities) 
     (com.raelity.jvi ViManager ColonCommands)
     (com.raelity.jvi.swing DefaultViFactory StatusDisplay TextView)
     (jsyntaxpane DefaultSyntaxKit)))

(def DEFAULT-FONT "Sans")
(def DEFAULT-FONT-SIZE 12)

(def TAB-STOP 4)
(def CARET-COLOR Color/BLACK)

(defonce factory (ViManager/setViFactory (DefaultViFactory.)))

(defn editor []
  (let [editor-pane (JPanel.)
        editor (JEditorPane.)
        scroller (JScrollPane. editor)
        status-pane (JPanel.)
        general-status (JLabel. "general status")
        stroke-status (JLabel. "stroke status")
        mode-status (JLabel. "mode-status")

        status-display (-> (ViManager/getViTextView editor) (.getStatusDisplay))
        font (Font. DEFAULT-FONT 
                    Font/PLAIN
                    DEFAULT-FONT-SIZE)
        _ (.setFont editor font)
        font (.getFont editor)
        fm (.getFontMetrics editor font)
        width (* 81 (.charWidth fm \space))
        height (* 30 (.getHeight fm))]
    
    ; Hookup the status bar labels to the Vi machinery
    (set! (.generalStatus status-display) general-status)
    (set! (.strokeStatus status-display) stroke-status)
    (set! (.modeStatus status-display) mode-status)

    (DefaultSyntaxKit/initKit)

    (ViManager/activateAppEditor editor nil "testing frame...")
    ;(ViManager/requestSwitch editor)
    (ViManager/installKeymap editor)

    (doto editor
      (.setContentType "text/clojure")
      (.setText "(defn foo [a b] (dosync (ref-set asdf* (+ a b))))")
      (.setCaretColor CARET-COLOR)
      (.requestFocusInWindow))

    (doto editor-pane
      (.setLayout (BorderLayout.))
      (.add scroller BorderLayout/CENTER)
      (.add status-pane BorderLayout/SOUTH))))

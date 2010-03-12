(ns overtone.app.repl
  (:gen-class)

  (:import
   (javax.swing JFrame JEditorPane JScrollPane JPanel JTextField SwingUtilities JButton)
   (java.awt Toolkit EventQueue Dimension Point)
   (java.awt Dimension Color Font RenderingHints Point BasicStroke)
     )

  (:import                  
   (java.io StringReader PushbackReader OutputStreamWriter PrintWriter)
   (java.util.concurrent LinkedBlockingQueue)
   (jsyntaxpane DefaultSyntaxKit)
   (javax.swing.text.BadLocationException)
   (javax.swing.text.DefaultStyledDocument)
   (java.awt Dimension BorderLayout))

  (:import
   java.awt.event.ActionEvent
   java.awt.event.KeyEvent
   javax.swing.AbstractAction
   javax.swing.JFrame
   javax.swing.JTextField
   javax.swing.KeyStroke
   javax.swing.text.AttributeSet
   javax.swing.text.BadLocationException
   javax.swing.text.DefaultStyledDocument)
  
  (:use [hiredman.hydra :only [hydra]])
  (:require [clj_repl.core :as r]))


;; Swing macros 

(defmacro EDT
  "runs body on the Event-Dispatch-Thread (Swing)"
  [& body]
  `(SwingUtilities/invokeLater (fn [] ~@body)))

(def #^{:doc "balanced pairs"}
     pairs '((\( \))
             (\[ \])
             (\" \")
             (\{ \})))

(defn balanced?
  "are all the pairs balanced in this string?"
  [string]
  ((comp not some)
   false?
   (map
    (fn [pair] (-> pair set (filter string) count (mod 2) zero?))
    pairs)))

(def prompt "hello ")1
(defn get-prompt-length [] (.length prompt))

(defn make-prompt-doc []
  (proxy [DefaultStyledDocument] []      
    (insertString [offs str a] (if (> offs (- (get-prompt-length) 1))
                                 (.insertString (.getSuperClass this) offs str a)))
    (remove [offs len] (if (> offs (- (get-prompt-length) 1))
                         (let [buffer (- offs (get-prompt-length))]
                           (.remove (.getSuperClass this) offs (if (< buffer 0) buffer len)))))))


(def prompt-doc (make-prompt-doc))

(defn  doc-gettext [] (.getText (.getSuperClass prompt-doc) (- (.getLength prompt-doc) (get-prompt-length))))

(defn doc-set-prompt [new-prompt]
  (try
   (let [length (.getLength prompt-doc)]
     (if (> 0 length) (.remove (.getSuperClass prompt-doc) 0 length)))))

(defn make-prompt-replace-action []
  (proxy [AbstractAction] []
    (actionPerformed [e]
                     (try
                      (def prompt (str (.getText prompt-doc) " >") )))))

(defn repl-window []
  (let [frame      (JFrame.)
        content    (.getContentPane frame)
        
        history     (JTextField. prompt-doc "" 20)
        history-scroll (JScrollPane. history (JScrollPane/VERTICAL_SCROLLBAR_NEVER) (JScrollPane/HORIZONTAL_SCROLLBAR_NEVER))
        
        prompt     (JTextField.)

        input (JEditorPane.)
        input-scroll (JScrollPane. input (JScrollPane/VERTICAL_SCROLLBAR_NEVER) (JScrollPane/HORIZONTAL_SCROLLBAR_NEVER))

        panel      (JPanel.)
        queue      (hydra)

        print      (fn [m] (EDT                                              
                            (let [ doc (.getDocument history)
                                  length (.getLength doc)]
                              (.insertString doc length m nil))))

        send       (fn [m] (do (r/send-to-repl queue m)
                               (print m)))
        
        cls-input  (fn [] (EDT (.setText input "")))
        
        ]
    
    (DefaultSyntaxKit/initKit) 

    (doc-set-prompt prompt)
    
    (doto queue
      (r/start-repl-thread (fn [q itm] (print itm))                          
                           (fn [q ns] (EDT
                                       (.setText prompt (str ns))))))

    (doto prompt
      (.setEditable true)
      (.setPreferredSize (Dimension. 150 20)))

    (doto input  ;       (.setContentType "text/clojure")
      (.setPreferredSize (Dimension. 400 150))
      (.setFont (Font. "SansSerif" (Font/PLAIN) 20))
      (.setBackground (Color. 200 200 200 )))

;    (doto (.getKeymap input)
;      (.addActionForKeyStroke
;       (javax.swing.KeyStroke/getKeyStroke (java.awt.event.KeyEvent/VK_ENTER) 0)
;       (proxy [javax.swing.AbstractAction] [] (actionPerformed [evt]  (let [text (.getText input)]
;                                                                        (if (and (not (empty? text))
;                                                                                 (balanced? text))
;                                                                          (do (send text)
;                                                                              (cls-input)
;                                                                              (.requestFocus input))))))))
    
    (doto panel
      (.setLayout (BorderLayout.))
      (.add prompt (BorderLayout/NORTH))       
      (.add input-scroll (BorderLayout/CENTER))
      (.doLayout))
    
    (doto history                     ;       (.setContentType "text/clojure")                                        
      (.setEditable true)
      )

    (.put (.getActionMap history) "NewPrompt" (make-prompt-replace-action))
    (.put (.getInputMap history) (KeyStroke/getKeyStroke (KeyEvent/VK_ENTER) 0) "NewPrompt")    

    (doto content
      (.setLayout  (BorderLayout.))
      (.add history-scroll (BorderLayout/CENTER))
      (.add panel  (BorderLayout/SOUTH)))

    (doto frame ;(.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
      (.pack)        
      (.setVisible true))

    (do
      (send "(in-ns 'user)"))
    
    (doto input
      (.requestFocus))))

(def window (let [w (repl-window)] (EDT w) w))

(defn -main []
  (EDT
   (repl-window)))

(comment
  (-main))

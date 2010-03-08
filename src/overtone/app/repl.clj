(ns overtone.app.repl
  (:gen-class)
  (:import 
     (java.awt Toolkit EventQueue Dimension Point)
     (java.awt Dimension Color Font RenderingHints Point BasicStroke)
     (java.awt.geom Ellipse2D$Float RoundRectangle2D$Float)
     (javax.swing JFrame JPanel) 
     (com.sun.scenario.scenegraph JSGPanel SGText SGShape SGGroup SGAbstractShape$Mode SGComponent
                                  SGTransform)
     (com.sun.scenario.scenegraph.event SGMouseAdapter)
     (com.sun.scenario.scenegraph.fx FXShape))

  (:import                  
   (javax.swing JFrame JEditorPane JScrollPane JPanel JTextField SwingUtilities JButton)
   (java.io StringReader PushbackReader OutputStreamWriter PrintWriter)
   (java.util.concurrent LinkedBlockingQueue)
   (jsyntaxpane DefaultSyntaxKit)
   (java.awt Dimension BorderLayout))
  
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

(defn -main []
  (EDT
   (let [frame      (JFrame.)
         content    (.getContentPane frame)

         history     (JEditorPane.)
         history-scroll (JScrollPane. history (JScrollPane/VERTICAL_SCROLLBAR_NEVER) (JScrollPane/HORIZONTAL_SCROLLBAR_NEVER))
         
         prompt     (JTextField.)

         input (JEditorPane.)
         input-scroll (JScrollPane. input (JScrollPane/VERTICAL_SCROLLBAR_NEVER) (JScrollPane/HORIZONTAL_SCROLLBAR_NEVER))

         panel     (JPanel.)
         queue      (hydra)

         history-vec (ref [""])
         history-vec-current (ref 0)
         
         print      (fn [m] (EDT                                              
                             (let [ doc (.getDocument history)
                                   length (.getLength doc)]
                               (.insertString doc length m nil))))

         send       (fn [m] (do (r/send-to-repl queue m)
                                (print m)
                                (dosync (ref-set history-vec (concat @history-vec [m])))))
         
         cls-input  (fn [] (EDT (.setText input "")))

         get-history (fn [] (do (cls-input)                              
                                (EDT (.setText input (nth @history-vec @history-vec-current "")))))
         
         get-history-next (fn [] (do (if (<= @history-vec-current (- (count @history-vec) 2))                                       
                                       (dosync (ref-set history-vec-current (+ @history-vec-current 1))))
                                     (get-history)))

         get-history-prev (fn [] (do (if (>= @history-vec-current 1)                                    
                                       (dosync (ref-set history-vec-current (- @history-vec-current 1))))
                                     (get-history)))
         ]
     
     (DefaultSyntaxKit/initKit) 

     (doto queue
       (r/start-repl-thread (fn [q itm] (print itm))                          
                            (fn [q ns] (EDT
                                        (.setText prompt (str ns))))))

     (doto prompt
       (.setEditable false)
       (.setPreferredSize (Dimension. 150 20)))

     (doto input
       (.setPreferredSize (Dimension. 400 150))
       (.setFont (Font. "SansSerif" (Font/PLAIN) 20))
       (.setBackground (Color. 200 200 200 ))
;       (.setContentType "text/clojure")
       )

     (doto (.getKeymap input)
       (.addActionForKeyStroke
        (javax.swing.KeyStroke/getKeyStroke (java.awt.event.KeyEvent/VK_ENTER) 0)
        (proxy [javax.swing.AbstractAction] [] (actionPerformed [evt]  (let [text (.getText input)]
                                                                         (if (and (not (empty? text))
                                                                                  (balanced? text))
                                                                           (do (send text)
                                                                               (cls-input)
                                                                               (.requestFocus input)))))))
       (.addActionForKeyStroke
        (javax.swing.KeyStroke/getKeyStroke (java.awt.event.KeyEvent/VK_DOWN) (java.awt.event.KeyEvent/CTRL_MASK))
        (proxy [javax.swing.AbstractAction] [] (actionPerformed [evt]  (get-history-prev))))
       (.addActionForKeyStroke
        (javax.swing.KeyStroke/getKeyStroke (java.awt.event.KeyEvent/VK_UP) (java.awt.event.KeyEvent/CTRL_MASK))
        (proxy [javax.swing.AbstractAction] [] (actionPerformed [evt]  (get-history-next)))))
     
     (doto panel
       (.setLayout (BorderLayout.))
       (.add prompt (BorderLayout/NORTH))       
       (.add input-scroll (BorderLayout/CENTER))
       (.doLayout))
     
     (doto history
;       (.setContentType "text/clojure")
       (.setEditable false))
        
     (doto content
       (.setLayout  (BorderLayout.))
       (.add history-scroll (BorderLayout/CENTER))
       (.add panel  (BorderLayout/SOUTH))
       )

     (doto frame
       (.pack)        
       (.setVisible true)
       (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
       )

     (do
       (send "(in-ns 'user)"))
     
 ;      (send "(use 'overtone.live)")
;       (send "(refer-ugens)")
;       )
          
     (doto input
       (.requestFocus)))))

(comment
  (-main))

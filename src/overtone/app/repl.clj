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

(defmacro action-performed [component event & body]
  `(. ~component addActionListener
      (proxy [java.awt.event.ActionListener] []
        (actionPerformed [~event] ~@body))))

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
         history-scroll     (JScrollPane. history)
         
         panel      (JPanel.)
         prompt     (JTextField.)

         input (JEditorPane.)
         input-scroll (JScrollPane. input)
         
         toolbar    (JPanel.)
         tb-init    (JButton. "init")
         
         queue      (hydra)
         send       (fn [m] (r/send-to-repl queue m))
         print      (fn [m] (EDT                                              
                             (let [ doc (.getDocument history)
                                   length (.getLength doc)]
                               (.insertString doc length m nil))))
         cls-input  (fn [] (EDT
                            (.setText input "")))]
     
     (DefaultSyntaxKit/initKit) 

     (doto tb-init
       (action-performed event (do
                                 (send "(in-ns 'user)")
                                 (send "(use 'overtone.live)")
                                 (send "(refer-ugens)")
                                 ))
       )
    
     (doto toolbar
       (.add tb-init)
       )
     
     (doto queue
       (r/start-repl-thread (fn [q itm] (print itm))
                          
                            (fn [q ns] (EDT
                                        (.setText prompt (str ns))))))
     
     (doto prompt
       (.setEditable false)
       (.setPreferredSize (Dimension. 150 20)))

     (doto input
       (.setPreferredSize (Dimension. 300 80))
;       (.setContentType "text/clojure")
       (.addKeyListener (proxy [java.awt.event.KeyListener] []
                            (keyTyped [e] )
                            (keyPressed [e] )
                            (keyReleased [e]
                                         (let [key-text (str (java.awt.event.KeyEvent/getKeyText (.getKeyCode e)))
                                               text (.getText input)]
                                           (if (and (= "Enter" key-text)
                                                    (not (empty? text ))
                                                    (balanced? text))
                                             (do (send text)
                                                 (cls-input))))
                                           ))))
     
     (doto panel
       (.add prompt)
       (.add input-scroll)
       (.doLayout))
     
     (doto history
 ;      (.setContentType "text/clojure")
       (.setEditable false))
        
     (doto content
       (.setLayout  (BorderLayout.))
       (.add history-scroll (BorderLayout/CENTER))
       (.add panel  (BorderLayout/SOUTH))
       (.add toolbar (BorderLayout/NORTH)))

     (doto frame
       (.pack)        
       (.setVisible true)

       ;(.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
       
       )

     (doto input
       (.requestFocus)))))

(comment)
(-main)
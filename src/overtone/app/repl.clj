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

(defn -main []
  (EDT
   (let [frame      (JFrame.)
         content    (.getContentPane frame)
         editor     (JEditorPane.)
         scroll     (JScrollPane. editor)
         panel      (JPanel.)
         prompt     (JTextField.)
         text-field (JTextField.)

         toolbar    (JPanel.)
         tb-init    (JButton. "1. init")
         tb-boot    (JButton. "2. boot")
         tb-quit    (JButton. "5. quit")
         tb-mn      (JButton. "4. make noise")
         tb-slog    (JButton. "3. server log")
         queue      (hydra)]
     
     (DefaultSyntaxKit/initKit) 

     (doto tb-init
       (action-performed event (do
                                 (r/send-to-repl queue "(use 'overtone.live)(refer-ugens)")
                                 (r/send-to-repl queue "(refer-ugens)")
                                 (EDT (let [ doc (.getDocument editor)
                                            length (.getLength doc)]
                                        (.insertString doc length ";; Make sure jackd is started" nil)))
                                 )))

     (doto tb-boot
       (action-performed event (r/send-to-repl queue "(boot)")))

     (doto tb-quit
       (action-performed event (r/send-to-repl queue "(quit)")))

     (doto tb-mn
       (action-performed event (do

                                 (r/send-to-repl queue "(defsynth foo (sin-osc 440))")
                                 (r/send-to-repl queue "(foo)")
                                 
                                 )))     
     
     (doto tb-slog
       (action-performed event (do
                                 (r/send-to-repl queue "(print-server-log)")
                                 (EDT (let [ doc (.getDocument editor)
                                            length (.getLength doc)]
                                        (.insertString doc length ";; No errors ? Now connect your jack ports and start transport and get ready to make some noise !" nil)))
                                 )))
     
     (doto toolbar
       (.add tb-init)
       (.add tb-boot)
       (.add tb-slog)
       (.add tb-mn)       
       (.add tb-quit))
     
     (doto queue
       (r/start-repl-thread (fn [q itm] (EDT                                              
                                         (let [ doc (.getDocument editor)
                                               length (.getLength doc)]
                                           (.insertString doc length itm nil))))
                          
                            (fn [q ns] (EDT
                                        (.setText prompt (str ns))))))
     
     (doto prompt
       (.setEditable false)
       (.setPreferredSize (Dimension. 150 20)))

     (doto text-field
       (action-performed event
                         (EDT
                          (r/send-to-repl queue (.getText text-field))
                          (.setText text-field "")))
       (.setPreferredSize (Dimension. 300 20)))
     
     (doto panel
       (.setPreferredSize (Dimension. 300 30))
       (.add prompt)
       (.add text-field)
       (.doLayout))
     
     (doto editor
       (.setContentType "text/clojure")
       (.setEditable false))
        
     (doto content
       (.setLayout  (BorderLayout.))
       (.add scroll (BorderLayout/CENTER))
       (.add panel  (BorderLayout/SOUTH))
       (.add toolbar (BorderLayout/NORTH)))

     (doto frame
       (.pack)        
       (.setVisible true)

       ;(.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
       
       )

     (doto text-field
       (.requestFocus)))))

(comment
  (-main)
  )
  

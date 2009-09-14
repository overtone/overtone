(ns overtone.gui
  (:import 
     (javax.swing JFrame JPanel JButton JOptionPane)
     (java.awt.event ActionListener)
     (java.awt BorderLayout)
     (de.sciss.jcollider.gui ServerPanel LogTextArea SynthDefDiagram 
                             SynthDefDiagram))
  (:use (overtone sc)))

(def *window (ref nil))

(def LOG-ROWS 40)
(def LOG-COLS 80)
(def LOG-FILE? false)
(def LOG-FILE nil)

(defn make-gui []
  (let [frame (JFrame. "Overtone")
        button (JButton. "Scope...")
        server-panel (ServerPanel. @*s (bit-or ServerPanel/BOOTQUIT 
                                         (bit-or ServerPanel/CONSOLE 
                                           (bit-or ServerPanel/COUNTS 
                                             ServerPanel/DUMP))))
        log-panel (LogTextArea. LOG-ROWS LOG-COLS LOG-FILE? LOG-FILE)
        container (.getContentPane frame)
        main      (JPanel. (BorderLayout.))]

(comment
 (.addActionListener button
   (proxy [ActionListener] []
     (actionPerformed [evt]
       (JOptionPane/showMessageDialog  nil,
          (str "<html>Hello from <b>Clojure</b>. Button "
               (.getActionCommand evt) " clicked.")))))
)
    (.add container main)
    (doto main 
      (.add server-panel BorderLayout/NORTH)
      (.add log-panel BorderLayout/SOUTH))
;      (.add button))

    (doto frame
      (.setSize 800 400)
      (.setLocation 100 100)  ; // default is 0,0 (top left corner)
      (.setDefaultCloseOperation JFrame/DISPOSE_ON_CLOSE)
      (.pack)
      (.setVisible true))
    frame))

(defn overtone []
  (javax.swing.SwingUtilities/invokeLater
   #(dosync (ref-set *window (make-gui)))))

(defn hide []
  (.hide @*window))

(defn synth-diagram [sdef]
  (SynthDefDiagram. sdef))

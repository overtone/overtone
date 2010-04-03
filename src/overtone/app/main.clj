(ns overtone.app.main
  (:gen-class)
  (:use clj-scenegraph.core)
  
  (:use (overtone.app editor)
        (overtone.core sc event)
        (overtone.gui repl)))

(defn -main [& args]
  (let [panel (sg-panel 640 480)
        group (sg-group)
        repl (repl)]

    (if (not (connected?))
      (boot :internal))
    
    (doto group
      (add! (translate 30 30 repl)))
    
    (set-scene! panel (translate 10 10 group))
    
    (sg-window panel)))

(-main)
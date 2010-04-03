(ns overtone.gui.editor
  (:gen-class)
   
  (:use overtone.core.event)

  (:use clj-scenegraph.core))

(import '(com.sun.scenario.scenegraph SGComponent))

(defn sg-component
  ([] (SGComponent.))
  ([comp]
     (doto (sg-component)
       (.setComponent comp)))
  ([comp w h]
     (doto (sg-component comp)
       (.setSize w h))))

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
 
(defn editor []
  (let [group (sg-group)
        editor (javax.swing.JEditorPane.)]

    
    (doto group
      (add! (translate 0 30 (sg-component editor 400 600))))
    
    group))

(def panel (sg-panel 640 480))
(sg-window panel)
(set-scene! panel (translate 30 30 (editor)))
(ns overtone.gui.swing
  (:import (clojure.lang RT)
           (java.awt Toolkit GraphicsEnvironment)
           (java.awt.event ActionListener)
           (javax.swing JFrame JPanel JSplitPane JLabel 
                        JButton SwingUtilities BorderFactory
                        ImageIcon))
  (:use clojure.stacktrace
        (overtone.core util)))

(defmacro in-swing [& body]
  `(SwingUtilities/invokeLater (fn [] ~@body)))

(defn resource-url [path]
  (.getResource (RT/baseLoader) path))

(defn icon [path]
  (ImageIcon. (resource-url path)))

(defn screen-dim []
  (.getScreenSize (Toolkit/getDefaultToolkit)))

(defn screen-size []
  (let [dim (screen-dim)]
    [(.width dim) (.height dim)]))

;TODO: Fix me
; It undecorates, but it doesn't seem to change the size of the frame...
(defn fullscreen-frame [f]
    (.setExtendedState f JFrame/MAXIMIZED_BOTH)
    (.setUndecorated f true))

; API not supported yet
;(Desktop/browse my-uri)

(defn- run-handler [handler event]
  (try
    (condp = (arg-count handler)
      0 (handler)
      1 (handler event))
    (catch Exception e
      (println "event handler exception: " event "\n" 
                 (with-out-str (print-cause-trace e))))))
(defn on-action
  "Adds a handler to a component."
  [component handler]
  (let [listener (proxy [ActionListener] []
                   (actionPerformed [event] (run-handler handler event)))]
    (.addActionListener component listener)
    listener))

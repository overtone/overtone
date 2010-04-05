(ns overtone.gui.utils
  (:import (clojure.lang RT)
           (java.awt Toolkit)
           (javax.swing JFrame JPanel JSplitPane JLabel 
                        JButton SwingUtilities BorderFactory
                        ImageIcon)))

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


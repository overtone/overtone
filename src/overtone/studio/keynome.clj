(ns overtone.studio.keynome
  (:import [java.awt GridLayout Dimension]
           [java.awt.event ActionListener KeyListener KeyEvent]
           [javax.swing JFrame JButton JPanel]))


(defn keynome [ &{ :keys [map title]
                  :or {map {} title "ohai!"} }]
  (let [map (atom map) painter (atom (fn [g] nil))
        handle-keypress (fn [evt map]
                          (let [key (str (.getKeyChar evt))
                                act (@map (keyword key))]
                            (do (if act (act) nil))))
        key-listener (proxy [KeyListener] []
                       (keyPressed  [evt] (handle-keypress evt map))
                       (keyReleased [evt] nil)
                       (keyTyped    [evt] nil))
        grid-layout (GridLayout. 4 11)
        panel (JPanel.)
        panel (proxy [JPanel] [] (paint [g] (painter g)))
        frame (JFrame. title)]
    (do (doto panel
          (.setLayout grid-layout)
          (.addKeyListener key-listener)
          (.setFocusable true)
          (.setPreferredSize (Dimension. 150 50)))
        (.add (.getContentPane frame) panel)
        (doto frame (.pack) (.setVisible true)))
    (fn [& args]
      (cond (empty? args) (do (.setVisible frame true))
            (> (count args) 2)
            (let [k (first args)]
              (cond (= k :painter) (reset! painter (second args))
                    (= k :map) (reset! map (conj @map (apply hash-map (rest args))))
                    ))))))

(defn switch []
  (let [on-or-off? (atom false)]
    (fn ([] @on-or-off?)
      ([k] (reset! on-or-off?
                   (cond (= k :swap) (if @on-or-off? false true)
                         (= k :on) true
                         (= k :off) false
                         (= k :rand) (if (< (rand) 0.5) true false)
                         ))))))

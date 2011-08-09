(ns overtone.studio.keynome
  (:import [java.awt GridLayout Dimension]
           [java.awt.event ActionListener KeyListener KeyEvent]
           [javax.swing JFrame JButton JPanel]))

;; to be honest, this is Overtone neutral and could be used
;; in any clojure/swing app.

(defn- handle-keypress [evt map]
  (let [key (str (.getKeyChar evt))
        act (@map (keyword key))]
    (do (if act (act) nil))))

(defn- key-listener-proxy [map]
  (proxy [KeyListener] []
    (keyPressed  [evt] (handle-keypress evt map))
    (keyReleased [evt] nil)
    (keyTyped    [evt] nil) ))

(defn keynome [ &{ :keys [map title] :or {map {} title "ohai!"} }]
  (let [map (ref map)
        key-listener (key-listener-proxy map)
        grid-layout (GridLayout. 4 11)
        panel (JPanel.)
        frame (JFrame. title)]
    (do (doto panel
          (.setLayout grid-layout)
          (.addKeyListener key-listener)
          (.setFocusable true)
          (.setPreferredSize (Dimension. 150 50)))
        (.add (.getContentPane frame) panel)
        (doto frame (.pack) (.setVisible true))
        {:frame frame
         :map map})))

(defn keynome2 [ &{ :keys [map title] :or {map {} title "ohai!"} }]
  (let [map (atom map) painter (atom (fn [g] nil))
        key-listener (key-listener-proxy map)
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
        (doto frame (.pack) (.setVisible true))
        ;;{:frame frame :map map}
        )
    (fn [& args]
      (cond (empty? args) (do (.setVisible frame true))
            (> (count args) 2)
            (let [k (first args)]
              (cond (= k :painter) (reset! painter (second args))
                    (= k :map) (reset! map (conj map (rest args)) )))))))

(defmacro defkeynome [name & args]
  `(def ~name (keynome [~@args])))

(defn set-actions [ & args]
  (let [am ((first args) :map)
        kwd-acts (partition 2 (rest args))]
    (doall (for [[kwd act] kwd-acts]
             (dosync (ref-set am (assoc @am kwd act))))) nil))

(defn switch []
  (let [on-or-off? (atom false)]
    (fn ([] @on-or-off?)
      ([k] (reset! on-or-off?
                   (cond (= k :swap) (if @on-or-off? false true)
                         (= k :on) true
                         (= k :off) false) )))))

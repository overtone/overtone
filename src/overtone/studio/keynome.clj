(ns overtone.keynome
  (:use overtone.live)
  (:import [java.awt GridLayout Dimension]
           [java.awt.event ActionListener KeyListener KeyEvent]
           [javax.swing JFrame JButton JPanel]))

(def keyboard
  (let [cm (keyword ","), sl (keyword "/"), sc (keyword ";"),
        mi (keyword "-"), lb (keyword "["), rb (keyword "]"),
        qt (keyword "'")]
    [:1 :2 :3 :4 :5 :6 :7 :8 :9 :0 mi
     :q :w :e :r :t :y :u :i :o :p lb
     :a :s :d :f :g :h :j :k :l sc qt
     :z :x :c :v :b :n :m cm :. sl rb]))

(defn new-keynome []
  (let [grid-layout (GridLayout. 4 11)
        action-map (ref {})
        buttons (for [kwd keyboard] (JButton. (str kwd)))
        panel (JPanel.)
        fire (fn [e am]
               (let [key (str (.getKeyChar e))
                     f (@am (keyword key))]
                 (do (if f (f) (println "key" key "pressed, no action")))))
        key-listener (proxy [KeyListener] []
                       (keyPressed [e] (fire e action-map))
                       (keyReleased [e] nil) (keyTyped [e] nil))
        frame (JFrame. "kbdnome")]
    (do (doall (for [b buttons] (do (.add panel b))))
        (doto panel
          (.setLayout grid-layout)
          (.addKeyListener key-listener)
          (.setFocusable true)
          (.setFocusTraversalKeysEnabled false))
        (.add (.getContentPane frame) panel)
        (doto frame (.pack) (.setVisible true))
        {:frame frame :action-map action-map
         :buttons (apply hash-map (interleave keyboard buttons))})))

(defn set-actions [ & stuff]
  (let [am ((first stuff) :action-map)
        kwd-action-pairs (partition 2 (rest stuff))]
    (doall (for [[kwd action] kwd-action-pairs]
             (dosync (ref-set am (assoc @am kwd action))))) nil))

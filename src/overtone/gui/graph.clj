(ns overtone.gui.graph
  (:import
     (java.awt Dimension Color Font RenderingHints Point BasicStroke)
     (java.awt.geom Ellipse2D$Float RoundRectangle2D$Float)
     (javax.swing JFrame JPanel)
     (com.sun.scenario.scenegraph JSGPanel SGText SGShape SGGroup SGAbstractShape$Mode)
     (com.sun.scenario.scenegraph.event SGMouseAdapter)
     (com.sun.scenario.scenegraph.fx FXShape)
     (com.sun.scenario.animation Clip Interpolators)
     (com.sun.scenario.effect DropShadow))
  (:use (overtone.core synth)))

(def NODE-HEIGHT 20)
(def NODE-ARC 4)
(def NODE-PADDING 5)
(def NODE-FONT-SIZE 14)
(def NODE-BACKGROUND (Color. 50 50 50))
(def NODE-STROKE (Color. 100 100 255))

(defn ugen-label [sdef ugen]
  (let [u-name (:name ugen)
        args (repeat (count (:inputs ugen)) "arg ")
        text (apply str u-name " " args)
        lbl (SGText.)]
    (doto lbl
      (.setText text)
      (.setFont (Font. "SansSerif" Font/BOLD NODE-FONT-SIZE))
      (.setAntialiasingHint RenderingHints/VALUE_TEXT_ANTIALIAS_ON)
      (.setFillPaint Color/WHITE))))

(defn ugen-mouse-listener [glow anim]
  (proxy [SGMouseAdapter] []
    (mouseEntered [evt node] (.start anim))
    (mouseExited  [evt node]
                 (.stop anim)
                 (.setRadius glow 0))))

(defn ugen-node-view
  "Create a node object representing the given ugen in sdef."
  [sdef ugen x y]
  (let [box (FXShape.)
        text (ugen-label sdef ugen)
        bounds (.getBounds text)
        group (SGGroup.)
        glow (DropShadow.)
        clip (Clip/create (long 2000) Clip/INDEFINITE glow "radius" (to-array [(float 1.0) (float 15.0)]))
        listener (ugen-mouse-listener glow clip)]
    (.setLocation text (Point. (+ x NODE-PADDING) (+ y NODE-PADDING (.height bounds))))

    (doto glow
      (.setRadius 1.0)
      (.setColor (Color. 88 248 246)))

    (doto box
      (.setShape (RoundRectangle2D$Float. x y
                                          (+ (* 2 NODE-PADDING) (.width bounds))
                                          (+ (* 2 NODE-PADDING) (.height bounds))
                                          NODE-ARC NODE-ARC))
      (.setMode SGAbstractShape$Mode/STROKE_FILL)
      (.setAntialiasingHint RenderingHints/VALUE_ANTIALIAS_ON)
      (.setFillPaint NODE-BACKGROUND)
      (.setDrawPaint NODE-STROKE)
      (.setDrawStroke (BasicStroke. 1.15)))

    (.setEffect box glow)

    (doto clip
      (.setInterpolator (Interpolators/getLinearInstance)))

    (doto group
      (.add box)
      (.add text)
      (.addMouseListener listener))

    group))

(defn position-ugen-nodes [nodes])

(def VIEW-PADDING 10)
(def NODE-MARGIN 20)
(def ROW-HEIGHT 30)

(def sdef-group (ref nil))

(defn ugen-node [ugen sdef]
  (let [consts (:constants sdef)
        ugens (:ugens sdef)
        args (map (fn [input]
                    (cond
                      (= -1 (:src input)) (nth consts (:index input))
                      :default (nth ugens (:index input))))
                  (:inputs ugen))]))

(defn sdef-graph
  "Convert an sdef into some more easily dealt with tree structure so we
  can"
  [sdef]
  (let [ugens (reverse (:ugens sdef))]))

(defn sdef-view [sdef]
  (let [group (SGGroup.)]
    (.add group (ugen-node-view sdef (first (:ugens sdef)) 100 100))
;        ugen-nodes (loop [x 0
;                          y 0
;                          nodes {}
;                          ugens (reverse (:ugens sdef))]
;                     (if ugens
;                       (let [ugen (first ugens)
;                             id (:id ugen)
;                             [x y] (if (contains? nodes id)
;                                     (+ y ROW-HEIGHT)
;                                     y)
;                             node (ugen-node-view sdef ugen x y)
;                             width (-> node (.getBounds) (.width))]
;                         (recur (+ x width NODE-MARGIN)
;                                y
;                                (assoc nodes id node)
;                                (next ugens)))
;                       nodes))]
;    ;(position-nodes nodes)
;    (doseq [[id n] ugen-nodes]
;      (.add group n))
;    ;(dosync (ref-set sdef-group group))
    group))

(defn graph-window [sdef]
  (let [g-frame (JFrame.  "Project Overtone: Graph View")
        g-panel (JSGPanel.)]
    (.add (.getContentPane g-frame) g-panel)

    (doto g-panel
      (.setBackground Color/BLACK)
      (.setScene (sdef-view sdef))
      (.setPreferredSize (Dimension. 1000 1000)))

    (doto g-frame
      (.add g-panel)
      (.pack)
      (.setVisible true))))

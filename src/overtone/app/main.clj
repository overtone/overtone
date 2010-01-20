(ns overtone.app.main
  (:import 
     (java.awt Dimension Color Font RenderingHints Point BasicStroke)
     (java.awt.geom Ellipse2D$Float RoundRectangle2D$Float)
     (javax.swing JFrame JPanel) 
     (com.sun.scenario.scenegraph JSGPanel SGText SGShape SGGroup SGAbstractShape$Mode)
     (com.sun.scenario.scenegraph.event SGMouseAdapter)
     (com.sun.scenario.scenegraph.fx FXShape))
     ;(com.sun.scenario.animation Clip Interpolators)
     ;(com.sun.scenario.effect DropShadow))
  (:gen-class))

(def WINDOW-FILL   (Color. 50 50 50))
(def WINDOW-STROKE (Color. 50 50 50))
(def LOGO-SIZE 14)

(defn header-panel []
  (let [browse-root (SGGroup.)
        base-box (FXShape.)
        ;glow (DropShadow.)
        logo (SGText.)]

    (comment doto glow 
      (.setRadius 1.0)
      (.setColor (Color. 88 248 246)))

    (doto base-box
      (.setShape (RoundRectangle2D$Float. 20 20 200 200 4 4))
      (.setMode SGAbstractShape$Mode/STROKE_FILL)
      (.setAntialiasingHint RenderingHints/VALUE_ANTIALIAS_ON)
      (.setFillPaint WINDOW-FILL)
      (.setDrawPaint WINDOW-STROKE)
      (.setDrawStroke (BasicStroke. 1.15)))

    ;(.setEffect base-box glow)

    (doto logo
      (.setText "Overtone")
      (.setFont (Font. "SansSerif" Font/BOLD LOGO-SIZE))
      (.setAntialiasingHint RenderingHints/VALUE_TEXT_ANTIALIAS_ON)
      (.setFillPaint Color/WHITE))

    (doto browse-root
      (.add logo)
      (.add base-box))))

(defn overtone-scene [args]
  (let [root (SGGroup.)
        header (header-panel)]
    (doto root
      (.add header))))

(defn -main [& args]
  (let [app-frame (JFrame.  "Project Overtone")
        main-panel (JSGPanel.)]
    (.add (.getContentPane app-frame) main-panel)

    (doto main-panel
      (.setBackground Color/BLACK)
      (.setScene (overtone-scene args))
      (.setPreferredSize (Dimension. 1000 1000)))

    (doto app-frame
      (.add main-panel)
      (.pack)
      (.setVisible true))))

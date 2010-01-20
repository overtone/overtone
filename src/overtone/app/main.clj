(ns overtone.app.main
  (:gen-class)
  (:import 
     (java.awt Toolkit EventQueue Dimension Point)
     (java.awt Dimension Color Font RenderingHints Point BasicStroke)
     (java.awt.geom Ellipse2D$Float RoundRectangle2D$Float)
     (javax.swing JFrame JPanel) 
     (com.sun.scenario.scenegraph JSGPanel SGText SGShape SGGroup SGAbstractShape$Mode SGComponent
                                  SGTransform)
       (com.sun.scenario.scenegraph.event SGMouseAdapter)
     (com.sun.scenario.scenegraph.fx FXShape))
     ;(com.sun.scenario.animation Clip Interpolators)
     ;(com.sun.scenario.effect DropShadow))
  (:use (overtone.app editor)))

(def HEADER-HEIGHT 20)
(def WINDOW-FILL   (Color. 50 50 50))
(def WINDOW-STROKE (Color. 50 50 50))
(def LOGO-SIZE 20)

(defn header-panel []
  (let [browse-root (SGGroup.)
        base-box (FXShape.)
        ;glow (DropShadow.)
        logo (SGText.)
        width 1000]

    (comment doto glow 
      (.setRadius 1.0)
      (.setColor (Color. 88 248 246)))

    (doto base-box
      (.setShape (RoundRectangle2D$Float. 0 0 width HEADER-HEIGHT 4 4))
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
      (.setFillPaint Color/WHITE)
      (.setLocation (Point. 10 18)))

    (doto browse-root
      (.add base-box)
      (.add logo))))

(defn overtone-scene [args]
  (let [root (SGGroup.)
        header (header-panel)
        edit (editor)
        edit-node (SGComponent.)
        edit-translate (SGTransform/createTranslation 100 100 edit-node)]
    (doto edit-node
      (.setSize 500 800)
      (.setComponent edit))

    (doto root
      (.add edit-translate)
      (.add header))))

(defn screen-dim []
  (.getScreenSize (Toolkit/getDefaultToolkit)))

(defn screen-size []
  (let [dim (screen-dim)]
    [(.width dim) (.height dim)]))

;TODO: It undecorates, but it doesn't seem to change the size of the frame...
(defn fullscreen-frame [f]
    (.setExtendedState f JFrame/MAXIMIZED_BOTH)
    (.setUndecorated f true))

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

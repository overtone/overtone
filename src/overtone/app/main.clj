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
  (:use (overtone.app editor)
        (overtone.core sc)
        (overtone.gui scope)))

(def APP-NAME "Overtone")

(def HEADER-HEIGHT 20)
(def WINDOW-FILL   (Color. 50 50 50))
(def WINDOW-STROKE (Color. 50 50 50))
(def LOGO-SIZE 20)
(def HEADER-FONT (Font. "SansSerif" Font/BOLD 16))

(defn header-status []
  (let [status-txt (SGText.)]
    (add-watch status* :header 
               (fn [skey sref old-status new-status]
                 (.setText status-txt (name new-status))))
    (doto status-txt
      (.setText (str "DSP: " (name @status*)))
      (.setFont HEADER-FONT)
      (.setAntialiasingHint RenderingHints/VALUE_TEXT_ANTIALIAS_ON)
      (.setFillPaint Color/WHITE)
      (.setLocation (Point. 550 17)))))

(defn booter []
  (let [boot-txt (SGText.)]
    (doto boot-txt
      (.setText "boot")
      (.setFont HEADER-FONT)
      (.setAntialiasingHint RenderingHints/VALUE_TEXT_ANTIALIAS_ON)
      (.setFillPaint Color/WHITE)
      (.setLocation (Point. 500 17))
      (.addMouseListener
        (proxy [SGMouseAdapter] []
          (mouseClicked [event node] 
            (if (connected?)
              (do (quit)
                (.setText boot-txt "boot"))
              (do (boot)
                (.setText boot-txt "quit")))))))))

(defn logo []
  (doto (SGText.)
      (.setText APP-NAME)
      (.setFont (Font. "SansSerif" Font/BOLD LOGO-SIZE))
      (.setAntialiasingHint RenderingHints/VALUE_TEXT_ANTIALIAS_ON)
      (.setFillPaint Color/WHITE)
      (.setLocation (Point. 10 18))))

(defn header []
  (let [browse-root (SGGroup.)
        base-box (FXShape.)
        width 1000]

    (doto base-box
      (.setShape (RoundRectangle2D$Float. 0 0 width HEADER-HEIGHT 4 4))
      (.setMode SGAbstractShape$Mode/STROKE_FILL)
      (.setAntialiasingHint RenderingHints/VALUE_ANTIALIAS_ON)
      (.setFillPaint WINDOW-FILL)
      (.setDrawPaint WINDOW-STROKE)
      (.setDrawStroke (BasicStroke. 1.15)))

    (doto browse-root
      (.add base-box)
      (.add (logo))
      (.add (booter))
      (.add (header-status)))))

(defn overtone-scene [args]
  (let [root (SGGroup.)]
    (doto root
      (.add (header))
;      (.add (editor))
      (.add (scope)))))

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

    (boot :internal)

    (doto main-panel
      (.setBackground Color/BLACK)
      (.setScene (overtone-scene args))
      (.setPreferredSize (Dimension. 1000 1000)))

    (doto app-frame
      (.add main-panel)
      (.pack)
      (.setVisible true))))

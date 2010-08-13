(ns overtone.gui.sg
  ^{:doc "Scenegraph syntactic sugar"
     :author "Fabian Aussems"}
  (:gen-class)
  (:require (clojure.contrib [str-utils2 :as s2]))
  (:use (overtone.core util event))
  (:import
   (com.sun.scenario.scenegraph
     JSGPanel ProportionalPaint SGAbstractGeometry SGAbstractShape
     SGAbstractShape$Mode SGAlignment SGArc SGCircle SGClip SGComponent
     SGComposite SGCubicCurve SGEffect SGEllipse SGEmbeddedToolkit
     SGFilter SGGroup SGImage SGImageOp SGLeaf SGLine SGNode SGParent
     SGPerspective SGQuadCurve SGRectangle SGRenderCache SGShape
     SGSourceContent SGText SGTransform SGTransform$Affine
     SGTransform$Rotate SGTransform$Scale SGTransform$Shear
     SGTransform$Translate SGWrapper)
   (com.sun.scenario.scenegraph.event
     SGFocusListener SGKeyListener SGMouseAdapter SGNodeListener
     SGMouseAdapter SGNodeEvent)
   (com.sun.scenario.effect
     AbstractGaussian Blend Bloom Brightpass ColorAdjust DropShadow
     Effect GaussianBlur Identity Merge Offset PhongLighting SepiaTone
     Shadow Glow Source SourceContent Blend$Mode Effect$AccelType)
   (com.sun.scenario.effect.light
     DistantLight Light PointLight SpotLight Light$Type )
   (java.awt BasicStroke BorderLayout Color Point Dimension
             Font Insets RenderingHints Shape)
   (java.awt.event MouseEvent MouseListener MouseAdapter)
   (java.awt.geom Arc2D Ellipse2D Point2D$Float Rectangle2D
                  RoundRectangle2D)
   (java.awt.image.BufferedImage)
   (javax.swing JComponent JLabel JPanel JFrame JSlider JTabbedPane)
   (javax.swing.border.EmptyBorder)
   (javax.swing.event ChangeEvent ChangeListener)))

(defn sg-panel
  ([] (doto (JSGPanel.)
        (.setBackground java.awt.Color/GRAY)))
  ([w h] (doto (sg-panel)
           (.setPreferredSize (java.awt.Dimension. w h)))))

(defn set-scene! [panel scene] (.setScene panel scene))

(defn on-focus-gained
  "on focus gained event"
  [component handler]
  (. component addFocusListener
     (proxy [SGFocusListener] []
       (focusGained [event node] (run-handler handler event node))
       (focusLost [event node]))))

(defn on-focus-lost
  "on focus lost event"
  [component handler]
  (. component addFocusListener
      (proxy [SGFocusListener] []
        (focusGained [event node] )
        (focusLost [event node] (run-handler handler event node)))))

(defn on-key-pressed [component handler]
  (. component addKeyListener
      (proxy [SGKeyListener] []
        (keyPressed [event node] (run-handler handler event node))
        (keyReleased [event node])
        (keyTyped [event node] ))))

(defn on-key-released [component handler]
  (. component addKeyListener
      (proxy [SGKeyListener] []
        (keyPressed [event node])
        (keyReleased [event node] (run-handler handler event node))
        (keyTyped [event node] ))))

(defn on-key-typed [component handler]
  (. component addKeyListener
      (proxy [SGKeyListener] []
        (keyPressed [event node] )
        (keyReleased [event node] )
        (keyTyped [event node] (run-handler handler event node)))))

(defn on-mouse-clicked [component handler]
  (. component addMouseListener
      (proxy [SGMouseAdapter] []
        (mouseClicked [event node] (run-handler handler event node)))))

(defn on-mouse-dragged [component handler]
  (. component addMouseListener
      (proxy [SGMouseAdapter] []
        (mouseDragged [event node] (run-handler handler event node)))))

(defn on-mouse-entered [component handler]
  (. component addMouseListener
      (proxy [SGMouseAdapter] []
        (mouseEntered [event node] (run-handler handler event node)))))

(defn on-mouse-exited [component handler]
  (. component addMouseListener
      (proxy [SGMouseAdapter] []
        (mouseExited [event node]  (run-handler handler event node) ))))

(defn on-mouse-moved [component handler]
  (. component addMouseListener
      (proxy [SGMouseAdapter] []
        (mouseMoved [event node] (run-handler handler event node)))))

(defn on-mouse-pressed [component handler]
  (. component addMouseListener
      (proxy [SGMouseAdapter] []
        (mousePressed [event node] (run-handler handler event node)))))

(defn on-mouse-released [component handler]
  (. component addMouseListener
      (proxy [SGMouseAdapter] []
        (mouseReleased [event node] (run-handler handler event node) ))))

(defn on-mouse-wheel-moved [component handler]
  (. component addMouseListener
      (proxy [SGMouseAdapter] []
        (mouseWheelMoved [event node] (run-handler handler event node)))))

(defn on-bounds-changed [component handler]
  (. component addNodeListener
      (proxy [SGNodeListener] []
        (boundsChanged [event] (run-handler handler event)))))

;; SGGroup

(defn sg-group [] (SGGroup.))
(defn add! [group node] (.add group node))
(defn remove! [group node] (.remove group node))


;; SGShape

(defn sg-shape [] (SGShape.))
(defn set-shape! [node shape] (.setShape node shape))


;; SGTransform

(defn rotate [theta node] (SGTransform$Rotate/createRotation theta node))
(defn zoom [sx sy node] (SGTransform$Scale/createScale sx sy node ))
(defn shear [shx shy node] (SGTransform$Shear/createShear shx shy node ))
(defn translate [tx ty node] (SGTransform$Translate/createTranslation tx ty node))

;; SGAbstractShape

(def shape-mode-map {:fill        SGAbstractShape$Mode/FILL
                     :stroke      SGAbstractShape$Mode/STROKE
                     :stroke-fill SGAbstractShape$Mode/STROKE_FILL})

(defn set-mode! [node mode] (.setMode node (shape-mode-map mode)))

(def stroke-cap-map  {:butt   BasicStroke/CAP_BUTT
                      :round  BasicStroke/CAP_ROUND
                      :square BasicStroke/CAP_SQUARE})

(def stroke-join-map {:bevel  BasicStroke/JOIN_BEVEL
                      :miter  BasicStroke/JOIN_MITER
                      :round  BasicStroke/JOIN_ROUND})

(defn set-draw-stroke!
  ([node width] (.setDrawStroke node (BasicStroke. width)))
  ([node width cap join] (.setDrawStroke node (BasicStroke. width (stroke-cap-map cap) (stroke-join-map join)))))

(def color-map {:black      Color/BLACK
                :blue       Color/BLUE
                :cyan       Color/CYAN
                :dark-gray  Color/DARK_GRAY
                :gray       Color/GRAY
                :green      Color/GREEN
                :light-gray Color/LIGHT_GRAY
                :magenta    Color/MAGENTA
                :orange     Color/ORANGE
                :pink       Color/PINK
                :red        Color/RED
                :white      Color/WHITE
                :yellow     Color/YELLOW})

(defn set-draw-paint!
  ([node color] (.setDrawPaint node (color-map color)))
  ([node r g b] (.setDrawPaint node (Color. r g b)))
  ([node r g b a] (.setDrawPaint node (Color. r g b a))))

(defn set-fill-paint!
  ([node color] (.setFillPaint node (color-map color)))
  ([node r g b] (.setFillPaint node (Color. r g b)))
  ([node r g b a] (.setFillPaint node (Color. r g b a))))

;; SGAbstractGeometry

(defn set-width! [node w] (.setWidth node w))
(defn set-height! [node h] (.setHeight node h))
(defn set-arc-width! [node w] (.setArcWidth node w))
(defn set-arc-height! [node h] (.setArcWidth node h))
(defn set-center-x! [node cx] (.setCenterX node cx))
(defn set-center-y! [node cy] (.setCenterX node cy))
(defn set-radius! [node r] (.setRadius node r))
(defn set-radius-x! [node r] (.setRadiusX node r))
(defn set-radius-y! [node r] (.setRadiusY node r))
(defn set-x! [node x] (.setX node x))
(defn set-y! [node y] (.setY node y))
(defn set-x1! [node x1] (.setX1 node x1))
(defn set-y1! [node y1] (.setY1 node y1))
(defn set-x2! [node x2] (.setX2 node x2))
(defn set-y2! [node y2] (.setY2 node y2))
(defn set-ctrl-x! [node ctrlx] (.setCtrlX node ctrlx))
(defn set-ctrl-y! [node ctrly] (.setCtrlY node ctrly))
(defn set-ctrl-x1! [node ctrlx] (.setCtrlX1 node ctrlx))
(defn set-ctrl-y1! [node ctrly] (.setCtrlY1 node ctrly))
(defn set-ctrl-x2! [node ctrlx] (.setCtrlX2 node ctrlx))
(defn set-ctrl-y2! [node ctrly] (.setCtrlY2 node ctrly))

(defn sg-circle
  ([] (SGCircle.))
  ([cx cy r] (doto (sg-circle)
               (set-center-x! cx)
               (set-center-y! cy)
               (set-radius! r))))

(defn sg-cubic-curve
  ([] (SGCubicCurve.))
  ([x1 y1 x2 y2 ctrlx1 ctrly1 ctrlx2 ctrly2] (doto (sg-cubic-curve)
                                               (set-x1! x1)
                                               (set-y1! y1)
                                               (set-x2! x2)
                                               (set-y2! y2)
                                               (set-ctrl-x1! ctrlx1)
                                               (set-ctrl-y1! ctrly1)
                                               (set-ctrl-x2! ctrlx2)
                                               (set-ctrl-y2! ctrly2))))

(defn sg-ellipse
  ([] (SGEllipse.))
  ([cx cy rx ry] (doto (sg-ellipse)
                   (set-center-x! cx)
                   (set-center-y! cy)
                   (set-radius-x! rx)
                   (set-radius-y! ry))))

(defn sg-line
  ([] (SGLine.))
  ([x1 y1 x2 y2] (doto (SGLine.)
                   (set-x1! x1)
                   (set-y1! y1)
                   (set-x2! x2)
                   (set-y2! y2))))

(defn sg-quad-curve
  ([] (SGQuadCurve.))
  ([x1 y1 x2 y2 ctrlx ctrly] (doto (sg-quad-curve)
                               (set-x1! x1)
                               (set-y1! y1)
                               (set-x2! x2)
                               (set-y2! y2)
                               (set-ctrl-x! ctrlx)
                               (set-ctrl-y! ctrly))))

(defn sg-rectangle
  ([] (SGRectangle.))
  ([w h] (doto (sg-rectangle)
           (set-width! w)
           (set-height! h)))
  ([x y w h] (doto (sg-rectangle)
               (set-x! x)
               (set-y! y)
               (set-width! w)
               (set-height! h)))
  ([x y w h arcw arch] (doto (sg-rectangle)
                         (set-x! x)
                         (set-y! y)
                         (set-width! w)
                         (set-height! h)
                         (set-arc-width! arcw)
                         (set-arc-height! arch))))

(def antialias-map {:default RenderingHints/VALUE_ANTIALIAS_DEFAULT
                    :off     RenderingHints/VALUE_ANTIALIAS_OFF
                    :on      RenderingHints/VALUE_ANTIALIAS_ON})

(defn set-antialias! [node value] (.setAntialiasingHint node (antialias-map value)))

;; SGText

(defn sg-text [] (SGText.))
(defn set-location! [node x y]
  (.setLocation node (Point2D$Float. x y)))
(defn set-text! [node text] (.setText node text))

(def font-style-map {:plain  Font/PLAIN
                     :italic Font/ITALIC
                     :bold   Font/BOLD})

(defn set-font! [node name style size] (.setFont node (Font. name (font-style-map style) size)))

(def text-antialias-map {:default  RenderingHints/VALUE_TEXT_ANTIALIAS_DEFAULT
                         :gasp     RenderingHints/VALUE_TEXT_ANTIALIAS_GASP
                         :lcd-hbgr RenderingHints/VALUE_TEXT_ANTIALIAS_LCD_HBGR
                         :lcd-hrgb RenderingHints/VALUE_TEXT_ANTIALIAS_LCD_HRGB
                         :lcd-vbgr RenderingHints/VALUE_TEXT_ANTIALIAS_LCD_VBGR
                         :lcd-vrgb RenderingHints/VALUE_TEXT_ANTIALIAS_LCD_VRGB
                         :off      RenderingHints/VALUE_TEXT_ANTIALIAS_OFF
                         :on       RenderingHints/VALUE_TEXT_ANTIALIAS_ON})

(defn set-text-antialias! [node val] (.setAntialiasingHint node (text-antialias-map val)))


;; Effects

(defmacro on-property-change [component [event] & body]
  `(. ~component addPropertyChangeListener
      (proxy [java.beans.PropertyChangeListener] []
        (propertyChange [~event] ~@body))))


(def effect-map {:phong-lighting        PhongLighting
                 :glow                  Glow
                 :shadow                Shadow})

(def blend-mode-map {:add           Blend$Mode/ADD
                     :blue          Blend$Mode/BLUE
                     :color-burn    Blend$Mode/COLOR_BURN
                     :color-dodge   Blend$Mode/COLOR_DODGE
                     :darken        Blend$Mode/DARKEN
                     :difference    Blend$Mode/DIFFERENCE
                     :exclusion     Blend$Mode/EXCLUSION
                     :green         Blend$Mode/GREEN
                     :hard-light    Blend$Mode/HARD_LIGHT
                     :lighten       Blend$Mode/LIGHTEN
                     :multiply      Blend$Mode/MULTIPLY
                     :overlay       Blend$Mode/OVERLAY
                     :red           Blend$Mode/RED
                     :screen        Blend$Mode/SCREEN
                     :soft-light    Blend$Mode/SOFT_LIGHT
                     :src-atop      Blend$Mode/SRC_ATOP
                     :src-in        Blend$Mode/SRC_IN
                     :src-out       Blend$Mode/SRC_OUT
                     :src-over      Blend$Mode/SRC_OVER})

(def light-type-map {:distant Light$Type/DISTANT
                     :point   Light$Type/POINT
                     :spot    Light$Type/SPOT})

(defmacro set-prop! [component kw val]
  `( ~(symbol (str ".set" (s2/capitalize (name kw)))) ~component ~val))

; (macroexpand-1 `(set-prop! glow :level 0.1))

(defmacro create-fx [fx-name args]
  `(new ~(fx-name effect-map) ~@args ))

;(macroexpand-1 `(create-fx :shadow [:a :b :c]))

(defmacro fx
  ([fx-name args] `(create-fx ~fx-name ~args))
  ([fx-name args fx & body]
      `(let [~fx (create-fx ~fx-name ~args)]
         ~@body
         ~fx)))

(defn set-fx! [effect fx] (.setEffect effect fx))
(defn set-child! [effect node] (.setChild effect node) )

(defn sg-effect
  ([] (SGEffect.))
  ([effect] (doto (sg-effect) (set-fx! effect)))
  ([effect child] (doto (sg-effect)
                    (set-fx! effect)
                    (set-child! child))))

;(println (macroexpand-1 `(fx :shadow [] fx (println fx))))
;(effect :glow [] fx (doto fx (.setLevel 0.4)))

;; sg-component

(defn sg-component
  ([] (SGComponent.))
  ([comp]
     (doto (sg-component)
       (.setComponent comp)))
  ([comp w h]
     (doto (sg-component comp)
       (.setSize w h))))

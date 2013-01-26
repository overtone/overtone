(ns overtone.gui.surface
  (:use [seesaw.core]
        [seesaw.graphics :only [draw circle style string-shape]]
        [seesaw.color :only [color]]
        [overtone.sc.node :only [ctl]])
  (:require [seesaw.mouse :as mouse]))

;;;

(defn- scale-val [[from-min from-max] [to-min to-max] v]
  (let [from-min (double from-min)
        from-max (double from-max)
        to-min   (double to-min)
        to-max   (double to-max)
        v        (double v)]
    (+ to-min
     (* (/ (- v from-min)
           (- from-max from-min))
        (- to-max to-min)))))

(defn- to-screen [dim {:keys [get-value min max]}]
  (scale-val [min max]
             (if (vector? dim) dim [0 dim])
             (get-value)))
(defn- to-model [{:keys [min max]} dim v]
  (scale-val (if (vector? dim) dim [0 dim])
             [min max] v))

(defn- paint-point
  [point c g]
  (let [x (to-screen (width c) (:x point))
        y (to-screen (height c) (:y point))
        z (to-screen [50 255] (:z point))
        r (to-screen [15 35] (:z point))]
    (draw g
        (circle x y r)
        (style :background (color (:color point) (max 40 (int z)))))))

(defn- paint-surface
  [state c g]
  (paint-point state c g)
  (draw g
    (string-shape 5 15 (str "x: " (get-in state [:x :name])))
    (style :foreground :darkgrey)

    (string-shape 5 30 (str "y: " (get-in state [:y :name])))
    (style :foreground :darkgrey)

    (string-shape 5 45 (str "z: " (get-in state [:z :name])))
    (style :foreground :darkgrey)))

(defn- handle-move-event [state e]
  (if (or (mouse/button-down? e :left)
          (mouse/button-down? e :right))
    (let [x (min (max (.getX e) 0) (width e))
          y (min (max (.getY e) 0) (height e))]
      ((get-in state [:x :set-value]) (to-model (:x state) (width e) x))
      ((get-in state [:y :set-value]) (to-model (:y state) (height e) y))
      (repaint! e))))

(defn- handle-press-event [state timer e]
  (handle-move-event state e)
  (if (= :right (mouse/button e))
    (reset! (:dir state) +)
    (repaint! e)))

(defn- handle-release-event [state timer e]
  (if (= :right (mouse/button e))
    (reset! (:dir state) -)
    (repaint! e)))

(defn- handle-timer [state c]
  (if-let [dir @(:dir state)]
    (let [{min-val :min max-val :max
           set-value :set-value get-value :get-value} (:z state)
          v (get-value)]
      (set-value (max min-val
                      (min max-val
                           (dir v
                                (/ (- max-val min-val) 20.0)))))
      (repaint! c)))
  c)

(defn- nil-param
  []
  (let [v (atom 0.0)]
    {:name ""
    :get-value (fn [] @v)
    :set-value (fn [new-val] (reset! v new-val))
    :min 0.0
    :max 1.0}))

(defn surface-panel [x y z]
  (let [state {:color :blue
               :x (or x (nil-param))
               :y (or y (nil-param))
               :z (or z (nil-param))
               :dir (atom nil)}
        c (canvas :preferred-size [400 :by 300]
                  :cursor :crosshair
                  :paint (fn [c g] (paint-surface state c g)))
        t (timer (partial handle-timer state)
                 :initial-value c
                 :start? true :delay 100 :initial-delay 1)]
    (listen c
      #{:mouse-moved :mouse-dragged}  (partial handle-move-event state)
      :mouse-pressed  (partial handle-press-event state t)
      :mouse-released (partial handle-release-event state t))
    [c t]))


(defn surface
  "Create a 'surface' for controlling params. Takes three
  params for x, y, and z.

  Mouse controls are as follows:

    x - left-drag left to right
    y - left-drag top to bottom
    z - hold down right mouse button

  See:
    (synth-param)
  "
  [x y z]
  (invoke-now
    (let [[panel timer] (surface-panel x y z)
          f (frame :title "Surface"
                   :content panel
                   :on-close :dispose)]
      (listen f :window-closed (fn [_] (.stop timer)))
      (-> f pack! show!))))

(defn surface-grid
  "Create a grid of surfaces. params is a sequence of [x y z] triples passed
  to (surface). Returns the frame that displays the grid. If you get an
  empty surface, it probably means you've got an inst without sufficient
  metadata.

  See:
    (surface)
  "
  [& params]
  (let [p-and-t (for [p params] (apply surface-panel p))
        panels (map first p-and-t)
        timers (map second p-and-t)
        f (frame :title "Surfaces"
                 :content (grid-panel
                            :columns (int (Math/ceil (Math/sqrt (count panels))))
                            :border 5 :hgap 5 :vgap 5
                            :background :black
                            :items panels))]
    (listen f :window-closed (fn [_]
                               (doseq [t timers] (.stop t))))
    (-> f pack! show!)))

(defn synth-param
  "Create a surface param for a synth or instrument.

  See:
    (surface)
  "
  ([synth id name]
   (let [param (some #(if (= name (:name %)) %) (:params synth))
         value (atom (:min param))]
     {  :name (str (:name synth) "/" id "/" name)
        :get-value (fn []  @value)
        :set-value #(ctl id name (reset! value %))
        :min (:min param)
        :max (:max param)}))
  ([synth name]
   (let [param (some #(if (= name (:name %)) %) (:params synth))]
     { :name (str (:name synth) "/" name)
      :get-value (fn []  @(:value param))
      :set-value #(reset! (:value param) %)
      :min (:min param)
      :max (:max param)})))

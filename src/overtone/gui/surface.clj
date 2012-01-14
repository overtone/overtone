(ns overtone.gui.surface
  (:use [seesaw.core]
        [seesaw.graphics :only [draw circle style string-shape]]
        [seesaw.color :only [color]]))

; TODO move to Seesaw.
(def ^ {:private true} input-modifier-table
  {:left java.awt.event.InputEvent/BUTTON1_DOWN_MASK
    :center java.awt.event.InputEvent/BUTTON2_DOWN_MASK
   :right java.awt.event.InputEvent/BUTTON3_DOWN_MASK})

(def ^ {:private true} mouse-button-table
  {java.awt.event.MouseEvent/BUTTON1 :left
   java.awt.event.MouseEvent/BUTTON2 :center
   java.awt.event.MouseEvent/BUTTON3 :right
   java.awt.event.MouseEvent/NOBUTTON :none })

(defn- mouse-button-down?
  [^java.awt.event.InputEvent e btn]
  (let [mask (input-modifier-table btn 0)]
    (not= 0 (bit-and mask (.getModifiersEx e)))))

(defn- mouse-button [^java.awt.event.MouseEvent e]
  (mouse-button-table (.getButton e)))

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
  (if (or (mouse-button-down? e :left)
          (mouse-button-down? e :right))
    (let [x (min (max (.getX e) 0) (width e))
          y (min (max (.getY e) 0) (height e))]
      ((get-in state [:x :set-value]) (to-model (:x state) (width e) x))
      ((get-in state [:y :set-value]) (to-model (:y state) (height e) y))
      (repaint! e))))

(defn- handle-press-event [state timer e]
  (handle-move-event state e)
  (if (= :right (mouse-button e))
    (reset! (:dir state) +)
    (repaint! e)))

(defn- handle-release-event [state timer e]
  (if (= :right (mouse-button e))
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

(defn surface-panel [x y z]
  (let [state {:color :blue
               :x x :y y :z z :dir (atom nil)}
        c (canvas :preferred-size [480 :by 480]
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
  [x y z]
  (invoke-now
    (let [[panel timer] (surface-panel x y z)
          f (frame :title "Surface"
                   :content panel
                   :on-close :dispose)]
      (listen f :window-closed (fn [_] (.stop timer)))
      (-> f pack! show!))))

(defn- synth-param [synth name]
  (let [param (some #(if (= name (:name %)) %) (:params synth))]
    { :name (str (:name synth) "/" name)
      :get-value (fn []  @(:value param))
      :set-value #(reset! (:value param) %)
      :min (:min param)
      :max (:max param)}))

(def ^ {:private true} nil-param {:name ""
                                  :get-value (fn [] 0)
                                  :set-value (fn [_])
                                  :min 0.0
                                  :max 1.0})

(defn synth-surface
  "Create a 'surface' for controlling synth params. Takes three
  synth/param-name pairs for x, y, and z.

  Mouse controls are as follows:

    x - left-drag left to right
    y - left-drag top to bottom
    z - hold down right mouse button

  "
  [[x-synth x-name] [y-synth y-name] [z-synth z-name]]
  (surface
    (if x-synth (synth-param x-synth x-name) nil-param)
    (if y-synth (synth-param y-synth y-name) nil-param)
    (if z-synth (synth-param z-synth z-name) nil-param)))

(comment
  (do
    (use 'overtone.live)
    (use 'overtone.gui.sequencer)
    (use 'overtone.gui.surface)
    (use 'overtone.inst.drum)
    (def m (metronome 128))
    (step-sequencer m 11 [kick closed-hat snare])
    (synth-surface [snare "freq"] [kick "freq"] [snare "sustain"])))


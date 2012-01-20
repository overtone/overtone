(ns overtone.gui.sequencer
  (:use [overtone.sc server]
        [overtone.music time]
        [overtone.gui.control :only [synth-controller]]
        [seesaw core]
        [seesaw.color :only [color]]
        [seesaw.graphics :only [style update-style draw rounded-rect line]]
        [seesaw.mig :only [mig-panel]])
  (:require [seesaw.bind :as bind]))

(defn- make-initial-state [metro steps instruments init-vals]
  {:playing?  false
   :metronome metro
   :steps     steps
   :step      0
   :rows      (vec
                (for [i instruments]
                  (let [i-vals (or (get init-vals (:name i))
                                   (vec (repeat steps false)))]
                    {:inst  i
                     :param (-> i :params first :name)
                     :value i-vals})))
   })

(def ^{:private true} NEW_ENTRY {:on true})

(defn- toggle-playing [state]
  (update-in state [:playing?] not))

(defn- get-entry [state row col]
  (get-in state [:rows row :value col]))

(defn- set-entry [state row col v]
  (if (< row (count (:rows state)))
    (assoc-in state [:rows row :value col] v)
    state))

(defn- update-entry [state row col v]
  (if (< row (count (:rows state)))
    (update-in state [:rows row :value col] merge NEW_ENTRY v)
    state))

(defn- mute-entry [state row col]
  (update-in state [:rows row :value col]
             (fn [v]
               (if (associative? v)
                 (assoc-in v [:on] false)
                 v))))

(defn- toggle-entry [state row col]
  (update-in state [:rows row :value col]
             (fn [v]
               (if (associative? v)
                 (update-in v [:on] not)
                 NEW_ENTRY))))

(defn- delete-entry [state row col]
  (set-entry state row col false))

(defn- get-row-param
  [state row]
  (get-in state [:rows row :param]))

(defn- get-param-info
  [state row]
  (let [p-name (get-row-param state row)]
    (first (filter #(= (:name %) p-name)
                 (get-in state [:rows row :inst :params])))))

(defn- clear-row [state row]
  (assoc-in state [:rows row :value] (vec (repeat (:steps state) false))))

(defn- play-step
  [row index]
  (let [{:keys [inst value]} row
        step-val (nth value index)]

    (when (:on step-val)
      (apply inst (-> step-val
                      (dissoc :on)
                      seq
                      flatten)))))

(defn- step-player
  [state-atom beat]
  (let [state @state-atom]
    (when (:playing? state)
      (let [metro (:metronome state)
            steps (:steps state)
            index (mod beat steps)
            next-beat (inc beat)]

        (swap! state-atom assoc-in [:step] index)

        (doseq [row (:rows state)]
          (at (metro beat) (play-step row index)))

        (apply-at (metro next-beat) #'step-player
                  [state-atom next-beat])))))

(def ^{:private true} grid-line-style
  (style :foreground "#55F" :stroke 1.0 :cap :round))

(def ^{:private true} enabled-entry-style
  (style
    :stroke 1.0
    :background (color 0 255 0 200)
    :foreground (color 0 150 0)))

(def ^{:private true} muted-entry-style
  (style
    :stroke 1.0
    :background (color 150 255 150 200)
    :foreground (color 0 150 0)))

(def ^{:private true} current-step-style
  (style
    :stroke 1.0
    :background (color 128 128 224 200)
    :foreground (color 0 150 0)))

(defn- scaled-entry-style
  [n]
  (let [g (int (+ (* n 200) 55))
        bg (color 0 g 0 200)]
    (update-style enabled-entry-style :background bg)))

(defn- get-param-factor
  [p-info val]
  (let [{:keys [max min name default]} p-info
        p-val (or ((keyword name) val)
                  default)]
    (double (/ (- p-val min) (- max min)))))

(defn- paint-grid [state ^javax.swing.JComponent c g]
  (let [w    (width c)
        h    (height c)
        rows (count (:rows state))
        cols (:steps state)
        step (:step state)
        dy   (/ h rows)
        dx   (/ w cols)]
    (if (:playing? state)
      (let [x (* step dx)]
        (draw g (rounded-rect x 0 dx h) current-step-style)))
    (dotimes [r rows]
      (let [y (* r dy)
            p (get-param-info state r)]
        (draw g (line 0 y w y) grid-line-style)
        (dotimes [c cols]
          (let [x (* c dx)]
            (draw g (line x 0 x h) grid-line-style)
            (when-let [val (get-entry state r c)]
              (let [paint-cell
                    #(draw g
                           (rounded-rect (+ x 2) (+ y 2) (- dx 3) (- dy 3) 3 3)
                           %)]
                (if (:on val)
                  (let [p-fact (get-param-factor p val)]
                    (paint-cell (scaled-entry-style p-fact)))
                  (paint-cell muted-entry-style))))))))))

(defn- scaled-param-map
  [state row val]
  (let [{:keys [max min name]} (get-param-info state row)
        p-name (keyword name)
        p-val (-> val
                (* (- max min))
                (+ min))]
    {p-name p-val}))

(defn- parse-grid-click
  [state e]
  (let [grid   (to-widget e)
        x      (.getX e)
        y      (.getY e)
        n-rows (count (:rows state))
        n-cols (:steps state)
        r-size (/ (height grid) n-rows)
        c-size (/ (width  grid) n-cols)
        r      (int (/ y r-size))
        c      (int (/ x c-size))
        r-top  (* r-size r)
        r-btm  (+ r-top r-size)
        y-val  (-> y (max r-top) (min r-btm) (- r-top))
        y-val  (- 1 (double (/ y-val r-size)))
        param  (scaled-param-map state r y-val)]
    {:row r :col c :r-size r-size :c-size c-size :y-val y-val :param param}))

(defn- on-grid-clicked [state e]
  (let [{:keys [row col param]} (parse-grid-click state e)
        new-state (cond (.isControlDown e) (delete-entry state row col)
                        (.isAltDown e)     (update-entry state row col param)
                        :else (toggle-entry state row col))]

    ;;when they enable a cell, play the sample.
    (when (not (:playing? new-state))
      (let [row (get-in new-state [:rows row])]
        (play-step row col)))

    new-state))

(defn- on-grid-drag [state e]
  (let [{:keys [row col r-size c-size y-val param]} (parse-grid-click state e)]
    (cond (.isControlDown e) (delete-entry state row col)
          (.isShiftDown e)   (mute-entry state row col)
          (.isAltDown e)     (update-entry state row col param)
          :else              (update-entry state row col {}))))

(defn- inst->index
  [rows inst]
  (first (keep-indexed
          (fn [index item]
            (when (= inst (:inst item))
              index))
          rows)))

(defn- on-param-selection [state inst e]
  (let [r (inst->index (:rows state) inst)]
    (assoc-in state [:rows r :param] (selection e))))

(defn- step-grid [state-atom]
  (let [state @state-atom

        c (canvas :background :darkgrey
                  :paint #(paint-grid @state-atom %1 %2)
                  :preferred-size [(* 25 (:steps state))
                                   :by
                                   (* 25 (count (:rows state)))])]

    (listen c :mouse-clicked #(swap! state-atom on-grid-clicked %)
              :mouse-dragged #(swap! state-atom on-grid-drag %))
    c))

(defn- inst-button
  [inst]
  (button :text (:name inst)
          :listen [:action (fn [e] (synth-controller inst))]))

(defn- inst-param
  [inst]
  (combobox :model (map :name (:params inst))
            :class :param))

(defn- inst-mute
  [inst]
  (toggle :text "Mute" :class :mute))

(defn- inst-solo
  [inst]
  (toggle :text "Solo" :class :solo))

(defn- inst-panel
  [state-atom inst]
  (let [panel (mig-panel :constraints ["wrap 2"
                                       "grow"]
                         :items [[(inst-button inst) "span, growx"]
                                 [(inst-param  inst) "span, growx"]
                                 [(inst-mute   inst) "growx"]
                                 [(inst-solo   inst) "growx"]])]
    (listen (select panel [:.param])
             :selection #(swap! state-atom on-param-selection inst %))
    panel))

(defn step-sequencer
  [metro steps instruments & [init-vals]]
  (invoke-now
    (let [state-atom   (atom (make-initial-state metro steps instruments init-vals))
          play-btn     (button :text "play")
          bpm-spinner  (spinner :model (spinner-model (metro :bpm) :from 1 :to 10000 :by 1)
                                :maximum-size [60 :by 100])
          controls-btn (button :text "controls")
          control-pane (toolbar :floatable? false
                                :items [play-btn
                                        :separator
                                        bpm-spinner
                                        [:fill-h 5]
                                        "bpm"
                                        :separator
                                        controls-btn])
          grid         (step-grid state-atom)
          inst-panels  (map (partial inst-panel state-atom)
                            instruments)
          f (frame :title    "Sequencer"
                   :content  (border-panel
                               :border 5 :hgap 5 :vgap 5
                               :north control-pane
                               :west (grid-panel :columns 1
                                                 :items inst-panels)
                               :center grid)
                   :on-close :dispose)]

      (bind/bind bpm-spinner (bind/b-do [v] (metro :bpm v)))
      (bind/bind state-atom (bind/b-do [v] (repaint! grid)))

      (listen play-btn :action
              (fn [e]
                (let [playing? (:playing? (swap! state-atom toggle-playing))]
                  (config! play-btn :text (if playing? "stop" "play"))
                  (if playing?
                    (step-player state-atom (metro))))))

      (listen controls-btn :action
              (fn [e]
                (apply synth-controller instruments)))

      (with-meta {:frame (-> f pack! show!)
                  :state state-atom }
                 {:type :sequencer}))))

(defn step-sequencer-map
  "Returns a map that can be passed to initialize a new step-sequencer."
  [s]
  (let [rows (:rows @(:state s))
        rows (map (fn [row] [(:name (:inst row)) (:value row)]) rows)]
    (into {} rows)))

(comment
  (do (use 'overtone.live)
      (use 'overtone.gui.sequencer)
      (use 'overtone.gui.control)
      (use 'overtone.inst.drum)
      (def m (metronome 128))
      (step-sequencer m 8 [kick closed-hat snare]))
  )

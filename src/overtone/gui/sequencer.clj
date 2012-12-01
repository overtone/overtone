(ns overtone.gui.sequencer
  (:use [overtone.sc server]
        [overtone.music time]
        [overtone.gui spinner-label]
        [overtone.gui.control :only [synth-controller]]
        [seesaw core]
        [seesaw.color :only [color]]
        [seesaw.graphics :only [style update-style draw rect rounded-rect line]]
        [seesaw.mig :only [mig-panel]]
        [seesaw.border :only [line-border]])
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
                     :value i-vals})))
   })

(defn- toggle-playing [state]
  (update-in state [:playing?] not))

(defn- get-entry [state row col]
  (get-in state [:rows row :value col]))

(defn- set-entry [state row col v]
  (if (< row (count (:rows state)))
    (assoc-in state [:rows row :value col] v)
    state))

(defn- toggle-entry [state row col]
  (update-in state [:rows row :value col] not))

(defn- add-column-to-row [row]
  (update-in row [:value] #(conj % false)))

(defn- add-column [state]
  (-> state
      (update-in [:rows] #(vec (map add-column-to-row %)))
      (update-in [:steps] inc)))

(defn- remove-column-from-row [row]
  (update-in row [:value] pop))

(defn- remove-column [state]
  (if (> (:steps state) 2)
    (-> state
      (update-in [:rows] #(vec (map remove-column-from-row %)))
      (update-in [:steps] dec))
    state))

(defn- clear-row [state row]
  (assoc-in state [:rows row :value] (vec (repeat (:steps state) false))))

(defn- step-player
  [state-atom beat]
  (let [state @state-atom]
    (when (:playing? state)
      (let [metro (:metronome state)
            steps (:steps state)
            index (mod beat steps)
            next-beat (inc beat)]

        (swap! state-atom assoc-in [:step] index)

        (doseq [{:keys [inst value]} (:rows state)]
          (when (value index)
            (at (metro beat) (inst))))

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
      (let [y (* r dy)]
        (draw g (line 0 y w y) grid-line-style)
        (dotimes [c cols]
          (let [x (* c dx)]
            (draw g (line x 0 x h) grid-line-style)
            (when (get-entry state r c)
              (draw g
                    (rounded-rect (+ x 2) (+ y 2) (- dx 4) (- dy 4) 8 8)
                    enabled-entry-style))))))
    (draw g (rect 0 0 (dec w) (dec h)) grid-line-style)))

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
        c      (int (/ x c-size))]
    { :row r :col c }))

(defn- on-grid-clicked [state e]
  (let [{:keys [row col]} (parse-grid-click state e)
        new-state (toggle-entry state row col)]

    ;;when they enable a cell, play the sample.
    (when-not (:playing? new-state)
      ((get-in new-state [:rows row :inst])))

    new-state))

(defn- on-grid-drag [state e]
  (let [{:keys [row col]} (parse-grid-click state e)]
    (set-entry state row col (not (.isShiftDown e)))))

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
          :listen [:action (fn [_] (synth-controller inst))]))

(defn- inst-panel
  [state-atom inst]
  (inst-button inst))

(defn step-sequencer
  [metro steps instruments & [init-vals]]
  (invoke-now
    (let [state-atom   (atom (make-initial-state metro steps instruments init-vals))
          play-btn     (button :text "Play")
          bpm-spinner  (spinner-label :class :sequencer-bpm-spinner
                                      :halign :center
                                      :border (line-border :thickness 1 :color :darkgrey)
                                      :maximum-size [60 :by 100]
                                      :model (spinner-model (metro :bpm) :from 1 :to 10000 :by 1))
          controls-btn (button :text "Controls" :tip "Show controls for all insts")
          plus-btn     (button :text "+" :tip "Add a column")
          minus-btn    (button :text "-" :tip "Remove a column")
          control-pane (toolbar :floatable? false
                                :items [play-btn    [:fill-h 5]
                                        bpm-spinner [:fill-h 5] "bpm"])
          grid         (step-grid state-atom)
          inst-panels  (map (partial inst-panel state-atom)
                            instruments)
          f (frame :title    "Sequencer"
                   :content  (border-panel
                               :border 5 :hgap 5 :vgap 5
                               :north control-pane
                               :west (grid-panel :columns 1
                                                 :items inst-panels)
                               :center grid
                               :south (toolbar :floatable? false
                                        :items [controls-btn :fill-h plus-btn minus-btn]))
                   :on-close :dispose)]

      (bind/bind bpm-spinner (bind/b-do [v] (metro :bpm v)))
      (bind/bind state-atom (bind/b-do [v] (repaint! grid)))

      (listen play-btn :action
              (fn [e]
                (let [playing? (:playing? (swap! state-atom toggle-playing))]
                  (config! play-btn :text (if playing? "stop" "play"))
                  (if playing?
                    (step-player state-atom (metro))))))
      (listen plus-btn :action (fn [_] (swap! state-atom add-column)))
      (listen minus-btn :action (fn [_] (swap! state-atom remove-column)))
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

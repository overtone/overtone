(ns overtone.gui.sequencer
  (:use [overtone.sc server]
        [overtone.music time]
        [overtone.gui.control :only [synth-controller]]
        [seesaw core]
        [seesaw.color :only [color]]
        [seesaw.graphics :only [style draw rounded-rect line]]
        [seesaw.swingx :only [hyperlink]])
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
                    {:inst i
                     :value i-vals})))
   })

(defn- toggle-playing [state]
  (update-in state [:playing?] not))

(defn- toggle-entry [state row col]
  (update-in state [:rows row :value col] not))

(defn- set-entry [state row col v]
  (assoc-in state [:rows row :value col] v))

(defn- get-entry [state row col]
  (get-in state [:rows row :value col]))

(defn- clear-row [state row]
  (assoc-in state [:rows row :value] (vec (repeat (:steps state) false))))

(defn- step-player
  [state-atom beat]
  (let [state @state-atom]
    (when (:playing? state)
      (let [metro (:metronome state)
            steps (:steps state)
            index (mod beat steps)
            next-beat (inc beat) ]
        (swap! state-atom assoc-in [:step] index)
        (doseq [{:keys [inst value]} (:rows state)]
          (when (nth value index)
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
                    (rounded-rect (+ x 2) (+ y 2) (- dx 3) (- dy 3) 3 3)
                    enabled-entry-style))))))))

(defn- on-grid-clicked [state e]
  (let [grid (to-widget e)
        x    (.getX e)
        y    (.getY e)
        rows (count (:rows state))
        cols (:steps state)
        c    (int (/ x (/ (width grid) cols)))
        r    (int (/ y (/ (height grid) rows)))]
    (toggle-entry state r c)))

(defn- on-grid-drag [state e]
  (let [grid (to-widget e)
        x    (.getX e)
        y    (.getY e)
        rows (count (:rows state))
        cols (:steps state)
        c    (int (/ x (/ (width grid) cols)))
        r    (int (/ y (/ (height grid) rows)))]
    (set-entry state r c (not (.isShiftDown e)))))

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
  (hyperlink :text (:name inst)
             :listen [:action (fn [e] (synth-controller inst))]))

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
          inst-btns    (map inst-button instruments)
          f (frame :title    "Sequencer"
                   :content  (border-panel
                               :border 5 :hgap 5 :vgap 5
                               :north control-pane
                               :west (grid-panel :columns 1
                                                 :items inst-btns)
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
  (use 'overtone.live)
  (use 'overtone.gui.sequencer)
  (use 'overtone.gui.control)
  (use 'overtone.inst.drum)
  (def m (metronome 128))
  (step-sequencer m 8 kick closed-hat snare)
)

(ns overtone.gui.sequencer
  (:use [overtone.sc server]
        [overtone.music time]
        [seesaw core mig border])
  (:require [seesaw.bind :as bind]))

(defn- make-initial-state [metro steps instruments]
  {:playing?  false
   :metronome metro
   :step      0
   :rows      (into
                {}
                (for [i instruments]
                  [(:name i) (vec (repeat steps false))]))})

(defn- toggle-playing [state]
  (update-in state [:playing?] not))

(defn- set-step-state [state step-vec inst]
  (assoc-in state [:rows (:name inst)] step-vec))

(defn- step-player
  [state-atom beat steps instruments]
  (let [state @state-atom]
    (when (:playing? state)
      (let [metro (:metronome state)
            index (mod beat steps)
            next-beat (inc beat)]
        (swap! state-atom assoc-in [:step] index)
        (doseq [inst instruments]
          (when (nth (get-in state [:rows (:name inst)]) index)
            (at (metro beat) (inst))))
        (apply-at (metro next-beat) #'step-player
                  [state-atom next-beat steps instruments])))))

(defn- step-row
  [ins steps state-atom]
  (let [lbl [(label (:name ins)) "gap 5px, gapright 10px"]
        btns (repeatedly steps #(toggle :selected? false))
        btns-constrained (map (fn [b] [b "width 25:25:25"]) btns)]
    ; Route changes in buttons to changes in state vector for row
    (bind/bind
      (apply bind/funnel btns)
      (bind/b-swap! state-atom set-step-state ins))

    (apply vector lbl btns-constrained) ))

(defn step-sequencer
  [metro steps & instruments]
  (invoke-now
    (let [state-atom   (atom (make-initial-state metro steps instruments))
          play-btn     (button :text "play")
          bpm-spinner  (spinner :model (spinner-model (metro :bpm) :from 20 :to 300 :by 1)
                                :maximum-size [60 :by 100])
          step-lbls    (vec (for [i (range steps)] (label :size [25 :by 25]
                                                          :background "#22F"
                                                          :opaque? false
                                                          :border (line-border :color "#22A"))))
          control-pane (toolbar :floatable? false
                                :items [play-btn
                                        :separator
                                        bpm-spinner
                                        [:fill-h 5]
                                        "bpm"])
          seq-pane     (mig-panel :constraints [(str "wrap " (inc steps)) "" ""]
                                  :items (concat
                                           [["step" "gap 5px, gapright 10px"]]
                                           (for [lbl step-lbls] [lbl "width 25:25:25"])
                                           (mapcat #(step-row % steps state-atom) instruments)))
          f (frame :title    "Sequencer"
                   :content  (border-panel :north control-pane :center seq-pane)
                   :on-close :dispose)]
      (bind/bind bpm-spinner (bind/b-do [v] (metro :bpm v)))
      (bind/bind
        state-atom
        (bind/b-do [{:keys [step]}]
                   (config! step-lbls :opaque? false)
                   (config! (step-lbls step) :opaque? true)
                   (repaint! step-lbls)))
      (listen play-btn :action
              (fn [e]
                (let [playing? (:playing? (swap! state-atom toggle-playing))]
                  (config! play-btn :text (if playing? "stop" "play"))
                  (if playing?
                    (step-player state-atom (metro) steps instruments)))))

      (with-meta {:frame (-> f pack! show!)
                  :state state-atom }
                 {:type :sequencer}))))

(comment
  (use 'overtone.live)
  (use 'overtone.gui.sequencer)
  (use 'overtone.gui.control)
  (use 'overtone.inst.drum)
  (def m (metronome 128))
  (step-sequencer m 8 kick closed-hat snare)
)

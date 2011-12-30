(ns overtone.gui.sequencer
  (:use [overtone.sc server]
        [overtone.music time]
        [seesaw core mig])
  (:require [seesaw.bind :as bind]))

(defn- step-player
  [playing? metro beat steps instruments step-states]
  (when @playing?
    (let [index (mod beat steps)
          next-beat (inc beat)]
      (doseq [inst instruments]
        (when @(nth (get step-states (:name inst)) index)
          (at (metro beat) (inst))))
    (apply-at (metro next-beat) #'step-player
              [playing? metro next-beat steps instruments step-states]))))

(defn- step-row
  [ins steps states]
  (let [lbl [(label (:name ins)) "gap 5px, gapright 10px"]
        btns (repeatedly steps #(toggle :selected? false))
        btns-constrained (map (fn [b] [b "width 25:25:25"]) btns)
        btns-states (partition 2 (interleave btns states))]
    (doseq [[btn state] btns-states]
      (bind/bind btn state))
    (apply vector lbl btns-constrained)))

(defn step-sequencer
  [metro steps & instruments]
  (invoke-now
    (let [play-btn (button :text "play")
          control-pane (horizontal-panel :items [play-btn])
          step-states (into {}
                            (for [i instruments]
                              [(:name i) (vec (repeatedly steps #(atom false)))]))
          step-rows (mapcat #(step-row % steps (get step-states (:name %))) instruments)
          seq-pane (mig-panel :constraints [(str "wrap " (inc steps)) "" ""]
                          :items step-rows)
          f (frame :title "Sequencer"
                   :content (vertical-panel :items [control-pane seq-pane])
                   :on-close :dispose)
          playing? (atom false)]
      (listen play-btn :action
              (fn [e]
                (if @playing?
                  (do
                    (reset! playing? false)
                    (invoke-later (config! play-btn :text "play")))
                  (do
                    (reset! playing? true)
                    (step-player playing? metro (metro) steps instruments step-states)
                    (invoke-later (config! play-btn :text "stop"))))))
      (with-meta {:frame (-> f pack! show!)
                  :state step-states
                  :playing? playing?}
                 {:type :sequencer}))))

(comment
  (use 'overtone.live)
  (use 'overtone.gui.sequencer)
  (use 'overtone.gui.control)
  (use 'overtone.inst.synth)
  (def m (metronome 128))
  (step-sequencer m 8 ks1 ping)
)

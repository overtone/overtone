(ns overtone.gui.mixer
  (:use [seesaw core border]
        [overtone.studio mixer inst]
        overtone.gui.dial
        overtone.gui.adjustment-popup
        [overtone.libs event])
  (:require [seesaw.bind :as bind]))

(def ^{:private true} CHAN-WIDTH  100)
(def ^{:private true} CHAN-HEIGHT 300)

(defn- inst-name
  [ins]
  (.replace (:name ins) "-" " "))

(defn- mixing-channel
  [ins]
  (let [volume-slider (slider :value (* @(:volume ins) 100.0) :min 0 :max 120
                    :orientation :vertical)
        vsp (border-panel :center volume-slider)
        pan-dial (dial :size [45 :by 45] :min -100 :max 100 :value (* @(:pan ins)))
        mute-state (atom false)
        mute-toggle #(if @mute-state
                       (do
                         (inst-volume ins @mute-state)
                         (reset! mute-state false))
                       (do
                         (reset! mute-state @(:volume ins))
                         (inst-volume ins 0)))
        mute-btn (border-panel :center
                               (button :text "M"
                                       :listen [:action mute-toggle]))
        solo-btn (border-panel :center (button :text "S"))]
    (adjustment-popup :widget volume-slider :label "Volume:")
    (adjustment-popup :widget pan-dial :label "Pan:")
    (bind/bind volume-slider
               (bind/transform (fn [v] (/ v 100.0)))
               (bind/b-do [v] (inst-volume ins v)))
    (bind/bind pan-dial
               (bind/transform (fn [p] (/ p 100.0)))
               (bind/b-do [p] (inst-pan ins p)))
    (vertical-panel :size [CHAN-WIDTH :by CHAN-HEIGHT] :border (to-border (inst-name ins))
                    :items [vsp pan-dial])))

; TODO: complete mute and solo, then add these buttons back to the GUI
; :items [vsp pan-dial mute-btn solo-btn])))

(defn mixer
  [& insts]
  (invoke-now
    (let [f (-> (frame :title "Mixer"
                       :on-close :dispose
                       :content (horizontal-panel :id :mix-panel
                                                  :items (map mixing-channel insts)))
              pack!
              show!)]
      (on-event :new-inst
                (fn [event]
                  (let [ins (:inst event)
                        dim (config f :size)
                        width (.getWidth dim)]
                    (invoke-later
                      (add! f (mixing-channel ins))
                      (pack! f))))
                :inst-added)
      f)))


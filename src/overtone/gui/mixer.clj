(ns overtone.gui.mixer
  (:use [seesaw core border]
        overtone.studio.mixer
        overtone.gui.dial
        [overtone.libs event])
  (:require [seesaw.bind :as bind]))

(def ^{:private true} CHAN-WIDTH  100)
(def ^{:private true} CHAN-HEIGHT 300)

(defn- inst-name
  [ins]
  (.replace (:name ins) "-" " "))

(defn- mixing-channel
  [ins]
  (let [v-slider (slider :value (* @(:volume ins) 100.0) :min 0 :max 100
                    :orientation :vertical)
        vsp (border-panel :center v-slider)
        p-slider (dial :size [45 :by 45] :min -100 :max 100 :value (* @(:pan ins)))
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
    (bind/bind v-slider
               (bind/transform (fn [v] (/ v 100.0)))
               (bind/b-do [v] (inst-volume ins v)))
    (bind/bind p-slider
               (bind/transform (fn [p] (/ p 100.0)))
               (bind/b-do [p] (inst-pan ins p)))
    (vertical-panel :size [CHAN-WIDTH :by CHAN-HEIGHT] :border (to-border (inst-name ins))
                    :items [vsp p-slider mute-btn solo-btn])))

(defn mixing-console
  ([] (mixing-console (vals @instruments*)))
  ([insts]
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
                 f))))


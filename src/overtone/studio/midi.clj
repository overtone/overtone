(ns overtone.studio.midi
  (:use [midi]
        [overtone.studio rig]))

(defn midi-inst-player [inst event ts]
  (let [notes* (atom {})]
    (condp = (:cmd event)
      :note-on (inst :note (:note event)
                     :velocity (:vel event))
      :note-off (inst :ctl :gate 0))))

(defn midi->inst [device inst]
  (midi-handler device (partial midi-inst-player inst)))

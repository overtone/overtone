(ns
  ^{:doc "Mini example of using MIDI events to construct
          a baby drum machine using samples from freesound.org"
    :author "Karsten Schmidt"}
  overtone.examples.workshops.resonate2013.ex04_midi
  (:use [overtone.live]))

;; Here we define a map of MIDI notes to samples
;; You can use any freesound.org sample you wish, just change
;; the sample IDs found on the website
;; Samples will be downloaded automatically and cached in
;; the .overtone directory in your user/home folder
(def drum-kit
  ;; c4 = kick, d4 = snare, d#4 = clap, e4 = closed hh, f4 = open hh, g4 = cowbell
  (->> {:c4 2086 :d4 26903 :d#4 147597 :e4 802 :f4 26657 :g4 9780}
       (map (fn [[n sample-id]] [(note n) (freesound sample-id)]))
    (into {})))

;; First let's see which MIDI devices are connected...
(midi-connected-devices)

;; MIDI is event based...
;; For drums we only are interested in :note-on events
;; emitted when a key on any connected device is pressed
(on-event
 [:midi :note-on]
  ;; look up MIDI note in drumkit and only play if there's sample for it
  (fn [e] (when-let [drum (drum-kit (:note e))] (drum)))
  ::drumkit)

;; execute the below to remove the event handler later on...
(comment
  (remove-event-handler ::drumkit))

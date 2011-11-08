(ns overtone.inst.sampled-piano
  (:use [overtone.core]))

(defonce piano-samples (load-samples "~/Desktop/Desktop/samples/MIS_Stereo_Piano/Piano/*LOUD*"))

(defn- matching-notes
  "Find the matching sample in piano-samples which matches the midi note.
  Assumes the name of the sample contains a string repressntation of the midi
  note i.e. C4."
  [note]
  (filter #(if-let [n (match-note (:name %))]
             (= note (:midi-note n)))
          piano-samples))

(defn sampled-piano
  "Play the specified midi note of the sampled piano at vol (defaults to 1)."
  ([note] (sampled-piano note 1))
  ([note vol]
     (if-let [sample (first (matching-notes note))]
       (stereo-player sample :vol vol))))

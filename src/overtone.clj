(ns overtone
  (:use overtone.utils))

(def *id-counter (ref 0))
(def *on-start-callbacks (ref {}))
(def *on-reset-callbacks (ref {}))

(defn next-id []
  (dosync (alter *id-counter inc)))

(defn on-start [callback]
  (let [id (next-id)]
    (dosync (ref-set *on-start-callbacks 
                     (assoc @*on-start-callbacks id callback)))
    id))

(defn remove-on-start-cb [id]
  (dosync (alter *on-start-callbacks dissoc id)))

(immigrate
  'overtone.rhythm
  'overtone.voice
  'overtone.pitch
  'overtone.tuning
  'overtone.midi
  'overtone.sc
  'overtone.synth
  'overtone.studio)

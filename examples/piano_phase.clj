(ns examples.piano-phase
  (:use overtone.live
        overtone.inst.sampled-piano))

(def piece [:E4 :F#4 :B4 :C#5 :D5 :F#4 :E4 :C#5 :B4 :F#4 :D5 :C#5])

(defn player
  [metro beat notes]
  (let [n     (first notes)
        notes (next notes)
        b     (inc beat)]
    (when n
      (at (metro beat)
          (sampled-piano (note n)))
      (apply-at (metro b) #'player [metro b (next notes)]))))

(def m (metronome 140))
(player m (m) piece)

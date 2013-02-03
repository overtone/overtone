(ns overtone.examples.getting-started.rhythm
  (:use [overtone.live]
        [overtone.inst.drum :only [quick-kick haziti-clap soft-hat open-hat]]))

(def m (metronome 128))

(defn player
  [beat]
  (let [next-beat (inc beat)]
    (at (m beat)
        (quick-kick :amp 0.5)
        (if (zero? (mod beat 2))
          (open-hat :amp 0.1)))
    (at (m (+ 0.5 beat))
        (haziti-clap :decay 0.05 :amp 0.3))

    (when (zero? (mod beat 3))
      (at (m (+ 0.75 beat))
          (soft-hat :decay 0.03 :amp 0.2)))

    (when (zero? (mod beat 8))
      (at (m (+ 1.25 beat))
          (soft-hat :decay 0.03)))

    (apply-by (m next-beat) #'player [next-beat])))

;;(player (m))
;;(stop)

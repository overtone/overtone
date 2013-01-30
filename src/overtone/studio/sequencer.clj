(ns overtone.studio.sequencer
  (:use [overtone.helpers lib]
        [overtone.music time]
        [overtone.sc server node]
        [overtone.studio rig]))

(defn hit-fn
  "A function that takes a ref (something dereferencable) and returns
  a function that will act as a track player.

  The track player fn takes a metronome and an instrument, and it will
  loop forever generating notes by reading from the ref and playing the
  instrument, passing the val in ref as the first argument."
  [hit-ref]
  ; Play a full bar at a time
  (fn hit-fn [m beat ins]
    (let [next-bar @hit-ref
          hit-cnt (count next-bar)
          next-beat (+ beat hit-cnt)
          next-tick (m next-beat)]
      (doall
        (map-indexed
          #(when (and (playing?) %2)
             (at (m (+ beat %1))
               (ins %2)))
          next-bar))
      (apply-by next-tick hit-fn [m next-beat ins]))))

(defn mono-play-fn
  ""
  [note-ref]
  (fn play-fn [m beat ins]
    (let [next-bar @note-ref
          hit-cnt (count next-bar)
          next-beat (+ beat hit-cnt)
          next-tick (m next-beat)]
      (doall
        (map-indexed
          #(when (and (playing?) %2)
             (let [b (+ beat %1)
                   id (at (m b)
                          (ins %2))]
               (when (some #{"gate"} (:args ins))
                 (at (m (inc b))
                     (ctl id :gate 0)))))
          next-bar))
      (apply-by next-tick play-fn [m next-beat ins]))))

(ns example.one-bar-sequencer
  (:use [overtone.live]
        [overtone.inst drum]))

(def metro (metronome 128))

; Our bar is a map of beat to instruments to play

(def bar {0   [kick]
          0.5 [c-hat]
          1   [kick snare]
          1.5 [c-hat]
          2   [kick]
          2.5 [c-hat]
          3   [kick snare]
          3.5 [c-hat]})

; For every tick of the metronome, we loop through all our beats
; and find the apropriate one my taking the metronome tick mod 4.
; Then we play all the instruments for that beat.

(defn player
  [tick]
  (dorun
    (for [k (keys bar)]
      (let [beat (Math/floor k)
            offset (- k beat)]
           (if (= 0 (mod (- tick beat) 4))
               (let [instruments (bar k)]
                    (dorun
                      (for [instrument instruments]
                        (at (metro (+ offset tick)) (instrument))))))))))

;; define a run fn which will call our player fn for each beat and will schedule
;; itself to be called just before the next beat

(defn run
  [m]
  (let [beat (m)]
    (player beat)
    (apply-at (m (inc beat))  #'run [m])))

;; make beats!
(run metro)

;; stop
(stop)

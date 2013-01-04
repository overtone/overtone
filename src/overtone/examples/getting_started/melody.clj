(ns overtone.examples.getting-started.melody
    (:use [overtone.live]))

(defn string
  [freq duration]
  (with-overloaded-ugens
    (* (line:kr 1 1 duration FREE)
       (pluck (* (white-noise) (env-gen (perc 0.001 5) :action FREE))
              1 1 (/ 1 freq) (* duration 2) 0.25))))

(definst harpsichord [freq 440]
  (let [duration 1
        snd  (string freq duration)
        t1   (* 0.2 (string (* 2/1 freq) duration))
        t2   (* 0.15 (string (* 3/2 freq) duration))
        t3   (* 0.1 (string (* 4/3 freq) duration))
        t4   (* 0.1 (string (* 5/4 freq) duration))
        snd  (+ snd (mix [t1 t2 t3 t4]))]
    snd))

(def melody
  (let [pitches
        [67 67 67 69 71             ; Row, row, row your boat,
         71 69 71 72 74             ; Gently down the stream,
         79 79 79 74 74 74 71 71 71 ; Merrily, merrily, merrily, merrily,
         67 67 67 74 72 71 69 67]   ; Life is but a dream!
        durations
        [1 1 2/3 1/3 1
         2/3 1/3 2/3 1/3 2
         1/3 1/3 1/3 1/3 1/3 1/3 1/3 1/3 1/3 1/3 1/3 1/3
         2/3 1/3 2/3 1/3 2]
        times (reductions + 0 durations)]
    (map vector times pitches)))

(defn play [metro notes]
  (let [play-note (fn [[beat midi]] (at (metro beat) (-> midi midi->hz harpsichord)))]
    (dorun (map play-note notes))))

(defn after [beats metro] (comp metro #(+ % beats)))

(defn play-round [metro notes]
  (play metro notes)
  (play (after 4 metro) notes)
  (play (after 8 metro) notes)
  (play (after 16 metro) notes))

;(play (metronome 120) melody)
;(play-round (metronome 120) melody)

(defn -main [& args]
  (let [metro (metronome 120)]

    ;; play the melody
    (play metro melody)
    (play-round (after 16 metro) melody)

    ;; wait for the melody to finish
    (Thread/sleep (- (metro 48) (now)))))
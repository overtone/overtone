(ns overtunes.songs.row-row-row-your-boat
  (:use
    [overtone.live]))

(definst harpsichord [freq 440]
  (let [duration 1]
    (*
      (line:kr 1 1 duration FREE)
      (pluck (* (white-noise) (env-gen (perc 0.001 5) :action FREE))
             1 1 (/ 1 freq) (* duration 2) 0.25))))

(def melody 
  (let [pitches
         [67 67 67 69 71              ; Row, row, row your boat,
          71 69 71 72 74              ; Gently down the stream,
          79 79 79 74 74 74 71 71 71  ; Merrily, merrily, merrily, merrily,
          67 67 67 74 72 71 69 67]    ; Life is but a dream! 
        durations
         [1 1 2/3 1/3 1
          2/3 1/3 2/3 1/3 2
          1/3 1/3 1/3 1/3 1/3 1/3 1/3 1/3 1/3 1/3 1/3 1/3
          2/3 1/3 2/3 1/3 2]
        times (reductions + 0 durations)]
    (map vector timings pitches)))


(defn play [metro notes] 
  (let [play-note (fn [[beat midi]] (at (metro beat) (-> midi midi->hz harpsichord)))]
    (dorun (map play-note notes)))) 

(defn play-round [metro notes]
  (let [after (fn [beats metro] (comp metro #(+ % beats)))]
    (play metro notes)
    (play (after 4 metro) notes)
    (play (after 8 metro) notes)
    (play (after 16 metro) notes)))

;(play (metronome 120) melody)
;(play-round (metronome 120) melody)
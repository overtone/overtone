(ns overtone.music
  (:import (java.util.concurrent ScheduledThreadPoolExecutor TimeUnit
                                 PriorityBlockingQueue)
     (java.util Comparator)))

(def *bpm (ref 120))           ; beats per minute
(def *signature (ref 4/4))     ; time signature

;; MIDI
(def midi-range (range 128))
(def middle-C 60)

;; Frequencies
(defn shift
  "Shift the notes in phrase by amount.  The index specified in notes starts 
  at one, not zero."
  [phrase notes amount]
  (if notes
    (let [note (dec (first notes))
          shifted (+ (get phrase note) amount)]
      (recur (assoc phrase note shifted) (next notes) amount))
    phrase))

(defn flat 
  "Flatten the specified notes in the phrase."
  [phrase notes]
  (shift phrase notes -1))

(defn sharp 
  "Sharpen the specified notes in the phrase."
  [phrase notes]
  (shift phrase notes +1))

(defn only 
  "Take only the specified notes from the given phrase."
  ([phrase notes] (only phrase notes []))
  ([phrase notes result]
   (if notes
     (recur phrase 
            (next notes) 
            (conj result (get phrase (first notes))))
     result)))

(def NOTE {:C  0
           :C# 1 :Db 1
           :D  2
           :D# 3 :Eb 3
           :E  4
           :F  5
           :F# 6 :Gb 6
           :G  7
           :G# 8 :Ab 8
           :A  9
           :A# 10 :Bb 10
           :B  11})

; Use a note (:C scale) or (:Eb scale) 
(def SCALE (let [major [0 2 2 1 2 2 2 1]
                 minor (flat major [3 6 7])]
             {:major major
              :minor minor
              :major-pentatonic (only major [1 2 3 5 6])
              :minor-pentatonic (only minor [1 3 4 5 7])}))

(def CHORD 
  (let [major [0 4 7]
        minor [0 3 7]
        major7 [0 4 7 11]
        minor7 [0 2 6 9]
        aug    [0 4 8]
        dim    [0 3 6]]
    {:major  major
     :M      major
     :minor  minor
     :m      minor
     :major7 major7
     :M7     major7
     :augmented aug
     :a         aug
     :diminished dim
     :i          dim}))

(defn parse-scale [s]
  (let [s (name s)
        note (get NOTE (keyword (str (first s) (re-find #"#" s))))]))

; Chord rules:
;
; minor =>  b3
; minor7 => b3 b7
;(defn chord [note] 
;  (cond 
;    (major? 
;  (map #(+ note %1) MAJOR-TRIAD))
;
(defn scale-raw [s]
  "Create the note field for a given scale.  Scales are specified with a keyword:
    :g => g major          :dm => d minor
    :eb7 => eb major 7     :cm7 => c minor 7
    :ba => b augmented     :f#i => f# diminished"
  (let [[note intervals] (parse-scale s)]))
;        full-intervals (apply concat (take 12 (repeat (scales intervals))))
;        [result _] (reduce 
;                     (fn [[mem last-v] v] 
;                       (let [new-v (+ last-v v)]
;                         [(conj mem new-v) new-v]))
;                     [[] base] 
;                     full-intervals)]
;    result))
;
(def scale (memoize scale-raw))

(defn octave-note 
  "Convert an octave and note to a midi note."
  [octave note]
  (+ (+ (* octave 12) note) 12))

(defn midi-hz-raw 
  "Convert a midi note number to a frequency in hz."
  [note]
  (* 440.0 (Math/pow 2.0 (/ (- note 69.0) 12.0))))

(def midi-hz (memoize midi-hz-raw))

(defn octaves [scale])

(defn choose 
  "Choose a random note from notes."
  [notes]
  (get notes (rand-int (count notes))))

(defn chosen-from [notes]
  (let [num-notes (count notes)]
    (repeatedly #(get notes (rand-int num-notes)))))

;(def notes (octaves (range 2 6) (scale :C :major)))
;(play notes rhythm groove)

;; Time

; System "time of day" clock => ms since the epoch (00:00 of Jan. 1, 1970)
; Accurate to the OS clock interrupt interval, which is typically about 10ms.
;(System/currentTimeMillis)

; Uses the highest resolution timer available, returning a free-running count
; in nanoseconds, although resolution is typically on the order of microseconds.
; Only valid when compared with other nanoTime values, so used for event timing.
;(System/nanoTime)

(def NUM-PLAYER-THREADS 10)
(def *player-pool* (ScheduledThreadPoolExecutor. NUM-PLAYER-THREADS))

(defn schedule [job ms-delay]
  (.schedule *player-pool* job (long ms-delay) TimeUnit/MILLISECONDS))

(defn periodic [job ms-period & [initial-delay]]
  (let [initial-delay (if initial-delay 
                        (long initial-delay)
                        (long 0))]
    (.scheduleAtFixedRate *player-pool* job initial-delay (long ms-period) TimeUnit/MILLISECONDS)))

(defn stop-players [& [now]]
  (if now
    (.shutdownNow *player-pool*)
    (.shutdown *player-pool*))
  (def *player-pool* (ScheduledThreadPoolExecutor. NUM-PLAYER-THREADS)))

(defn stop-player [player & [now]]
  (.cancel player (or now false)))

; Timer based "recursion in time" 
;  The idea is to create something along the lines of impromptu, except do it
; using a macro and an exception.
;  * save the arguments inside the exception object
;  * throw the exception to pop off the call-stack to some base level
;  * in the exception handler lookup which function threw the exception and store it in the exception too
;  * add an event to the task queue for the specified time in the future
;  * when event fires call the function with the arguments saved in the exception 

;(gen-and-load-class 'overtone.music.TimeException :extends Exception)
;
;(defn schedule [fun ms-delay]
;  (throw (new overtone.music.TimeException {:callback fun :ms ms-delay}))) 
;
;(defn player [func-ref]
;  (try
;    (func-ref)
;    (catch overtone.music.TimeException e 
;      (println e))))

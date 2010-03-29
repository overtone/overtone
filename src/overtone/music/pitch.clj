(ns 
  #^{:doc "Functions to help generate and manipulate frequencies and sets of related frequencies.
          This is the place for functions representing general musical knowledge, like scales, chords,
          intervals, etc."
     :author "Jeff Rose"}
  overtone.music.pitch)

;; Notes in a typical scale are related by small, prime number ratios. Of all
;; possible 7 note scales, the major scale has the highest number of consonant
;; intervals.
;;
;; * 1:1 unison
;; * 2:1 octave
;; * 3:2 perfect fifth
;; * 4:3 fourth
;; * 5:3 major sixth
;; * 5:4 major third
;; * 6:5 minor third
;; * 8:5 minor sixth 

(defmacro defratio [rname ratio]
  `(defn ~rname [freq#] (* freq# ~ratio)))

(defratio octave    2/1)
(defratio fifth     3/2)
(defratio fourth    4/3)
(defratio sixth     5/3)
(defratio third     5/4)
(defratio min-third 6/5)
(defratio min-sixth 8/5)

;; MIDI
(def midi-range (range 128))
(def middle-C 60)

;; Frequencies
(defn shift
  "Shift the 'notes' in 'phrase' by a given 'amount' of half-steps."
  [phrase notes amount]
  (if notes
    (let [note (first notes)
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

(defn invert
  "Invert a sequence of notes using either the first note as the stationary
  pivot point or the optional second argument."
  [notes & [pivot]]
  (let [pivot (or pivot (first notes))]
    (for [n notes] (- pivot (- n pivot)))))

(defn only 
  "Take only the specified notes from the given phrase."
  ([phrase notes] (only phrase notes []))
  ([phrase notes result]
   (if notes
     (recur phrase 
            (next notes) 
            (conj result (get phrase (first notes))))
     result)))

(def NOTE {:C  0  :c  0
           :C# 1  :c# 1  :Db 1  :db 1
           :D  2  :d  2
           :D# 3  :d# 3  :Eb 3  :eb 3
           :E  4  :e  4
           :F  5  :f  5
           :F# 6  :f# 6  :Gb 6  :gb 6
           :G  7  :g  7
           :G# 8  :g# 8  :Ab 8  :ab 8
           :A  9  :a  9
           :A# 10 :a# 10 :Bb 10 :bb 10
           :B  11 :b  11})

;; * Each note in a scale acts as either a generator or a collector of other notes, 
;; depending on their relations in time within a sequence.
;;  - How can this concept be developed into parameterized sequences with knobs for
;;  adjusting things like tension, dissonance, swing, genre (latin, asian, arabic...)

; Use a note (:C scale) or (:Eb scale) 
(def SCALE (let [major [0 2 2 1 2 2 2 1]
                 minor (flat major [3 6 7])]
             {:major major
              :minor minor
              :major-pentatonic (only major [1 2 3 5 6])
              :minor-pentatonic (only minor [1 3 4 5 7])}))

; Various scale intervals in terms of steps on a piano, or midi note numbers
(def SCALES
  {:pentatonic        [2 2 3 2]
   :wholetone         [2 2 2 2 2]
   :chromatic         [1 1 1 1 1 1 1 1 1 1 1]
   :octatonic         [2 1 2 1 2 1 2]                      
   :messiaen1         [2 2 2 2 2]                                            
   :messiaen2         [2 1 2 1 2 1 2]                                            
   :messiaen3         [2 1 1 2 1 1 2 1]
   :messiaen4         [1 1 3 1 1 1 3]
   :messiaen5         [1 4 1 1 4]
   :messiaen6         [2 2 1 1 2 2 1]
   :messiaen7         [1 1 1 2 1 1 1 1 2]
   :ionian            [2 2 1 2 2 2]
   :dorian            [2 1 2 2 2 1]
   :phrygian          [1 2 2 2 1 2]
   :lydian            [2 2 2 1 2 2]
   :lydian-mixolydian [2 1 2 1 2 1 2]
   :mixolydian        [2 2 1 2 2 1]
   :aeolian           [2 1 2 2 1 2]
   :locrian           [1 2 2 1 2 2]})
;; * Diatonic function
;;   - in terms of centeredness around the root, this is the order
;; of chord degrees: I, V, IV, vi, iii, ii, vii (The first 3 chords 
;; being major, second 3 minor, and last diminished)
    
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
     :minor7 minor7
     :m7     minor7
     :augmented aug
     :a         aug
     :diminished dim
     :i          dim}))

; TODO: finish this...
(defn chord [base chord]
  (map #(+ %1 base) (:major CHORD)))

(defn parse-scale [s]
  (let [s (name s)
        base (get NOTE (keyword (str (first s) (re-find #"#" s))))
        scale (cond
                (.endsWith s "m7") :minor7
                (.endsWith s "m")  :minor
                (.endsWith s "a")  :augmented
                (.endsWith s "i")  :diminished
                true               :major)
        intervals (scale SCALE)]
    [base intervals]))

(defn scale-raw [s]
  "Create the note field for a given scale.  Scales are specified with a keyword:
  :g => g major          :dm => d minor
  :eb7 => eb major 7     :cm7 => c minor 7
  :ba => b augmented     :f#i => f# diminished"
  (let [[note intervals] (parse-scale s)]
    (reduce (fn [[mem last-note] interval]
              (let [new-note (+ last-note interval)]
                [(conj mem new-note) new-note]))
            (take (* 8 12) (cycle intervals)))))
    
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

(defn from-scale [lower upper ] nil)

(defn octave-note 
  "Convert an octave and note to a midi note."
  [octave note]
  (+ (+ (* octave 12) note) 12))

(defn midi-hz-raw 
  "Convert a midi note number to a frequency in hz."
  [note]
  (* 440.0 (Math/pow 2.0 (/ (- note 69.0) 12.0))))

(def midi-hz (memoize midi-hz-raw))
(def mhz midi-hz)

(defn octaves [scale])

(defn chosen-from [notes]
  (let [num-notes (count notes)]
    (repeatedly #(get notes (rand-int num-notes)))))

;; TODO:
;; * weighted random 
;; * arpeggiator(s)
;; * shufflers (randomize a sequence, or notes within a scale, etc.)
;; * 
;;* Sequence generators 
;; - probabilistic arpeggiator
;; - take a rhythym seq, note seq, and groove seq
;; - latin sounds
;; - house sounds
;; - minimal techno sounds
;; - drum and bass sounds
;;
;;* create a library of sequence modifiers and harmonizers

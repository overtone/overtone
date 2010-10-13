(ns
  ^{:doc "Functions to help generate and manipulate frequencies and sets of related frequencies.
          This is the place for functions representing general musical knowledge, like scales, chords,
          intervals, etc."
     :author "Jeff Rose"}
  overtone.music.pitch
  (:require [clojure.contrib.math :as math]))

;; Notes in a typical scale are related by small, prime number ratios. Of all
;; possible 7 note scales, the major scale has the highest number of consonant
;; intervals.

(defmacro defratio [rname ratio]
  `(defn ~rname [freq#] (* freq# ~ratio)))

; Perfect consonance
(defratio unison    1/1)
(defratio octave    2/1)
(defratio fifth     3/2)

; Imperfect consonance
(defratio sixth     5/3)
(defratio third     5/4)

; Dissonance
(defratio fourth    4/3)
(defratio min-third 6/5)
(defratio min-sixth 8/5)

(defn cents
  "Returns a frequency computed by adding n-cents to freq.  A cent is a
  logarithmic measurement of pitch, where 1-octave equals 1200 cents."
  [freq n-cents]
  (* freq (java.lang.Math/pow 2 (/ n-cents 1200))))

;; MIDI
(def midi-range (range 128))
(def middle-C 60)

;; Manipulating pitch using midi note numbers

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

; Making MIDI note numbers nicer to look at...
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
;;  - Can we develop a symbol language or visual representation so that someone could compose
;;  a piece by using mood tokens rather than specifying scales and notes directly?  Basically,
;;  generator functions would have to choose the scales, chords, notes and rhythm based on
;;  a mix of looking up aspects of the mood, and informed randomness.

; Use a note (:C scale) or (:Eb scale)
(def SCALE (let [major [2 2 1 2 2 2 1]
                 minor [2 1 2 2 1 2 2]]
             {:major major
              :minor minor
              :major-pentatonic (only major [1 2 3 5 6])
              :minor-pentatonic (only minor [1 3 4 5 7])}))

(def DIOTONIC-MODES
  ;; offset-order: ionian, dorian, phrygian, lydian, mixolydian, aeolian, lochrian
  (let [ionian-sequence [2 2 1 2 2 2 1]
        ionian-offset     0
        dorian-offset     1
        phrygian-offset   2
        lydian-offset     3
        mixolydian-offset 4
        aeolian-offset    5
        lochrian-offset   6
        rotate-ionian (fn [offset] (drop offset (take (+ 7 offset) (cycle ionian-sequence))))]
    {:ionian     (rotate-ionian ionian-offset)
     :major      (rotate-ionian ionian-offset)
     :dorian     (rotate-ionian dorian-offset)
     :phrygian   (rotate-ionian phrygian-offset)
     :lydian     (rotate-ionian lydian-offset)
     :mixolydian (rotate-ionian mixolydian-offset)
     :aeolian    (rotate-ionian aeolian-offset)
     :minor      (rotate-ionian aeolian-offset)
     :lochrian   (rotate-ionian lochrian-offset)}))

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

(defn octave-note
  "Convert an octave and note to a midi note."
  [octave note]
  (+ (+ (* octave 12) note) 12))

; midicps
(defn midi->hz
  "Convert a midi note number to a frequency in hz."
  [note]
  (* 440.0 (java.lang.Math/pow 2.0 (/ (- note 69.0) 12.0))))

; cpsmidi
(defn hz->midi
  "Convert from a frequency to the nearest midi note number."
  [freq]
  (java.lang.Math/round (+ 69
                 (* 12
                    (/ (java.lang.Math/log (* freq 0.0022727272727))
                       (java.lang.Math/log 2))))))

; ampdb
(defn amp->db
  "Convert linear amplitude to decibels."
  [amp]
  (* 20 (java.lang.Math/log10 amp)))

; dbamp
(defn db->amp
  "Convert decibels to linear amplitude."
  [db]
  (java.lang.Math/exp (* (/ db 20) (java.lang.Math/log 10))))

; TODO: finish this...
(defn chord
  "Returns notes for the specified chord.
  (chord :c :major)  ; c major
  (chord :a :minor)  ; a minor
  (chord :Bb :dim)   ; b flat diminished
  (chord :g :minor7) ; g minor seventh
  (chord :E :m7)     ; E minor seventh
  (chord :D :M7)     ; D major seventh
  (chord :F :aug)    ; D major seventh
  "
  [base cname & [octave]]
  (let [octave (or octave 4)
        base (octave-note octave (NOTE base))]
    (map #(+ % base) (CHORD cname))))

(defn scale [skey & [sname]]
  "Create the note field for a given scale.  Scales are specified with a keyword:
  :g :major,
  :d :minor"
  (let [base (NOTE skey)
        sname (or sname :major)
        intervals (SCALE sname)]
    (reverse (next
      (reduce (fn [mem interval]
              (let [new-note (+ (first mem) interval)]
                (conj mem new-note)))
            (list base)
            (take (* 8 12) (cycle intervals)))))))

(defn from-scale [lower upper ] nil)

(defn octaves [scale])

(defn chosen-from [notes]
  (let [num-notes (count notes)]
    (repeatedly #(get notes (rand-int num-notes)))))

(defn nth-octave
  "Returns the freq n octaves from the supplied reference freq
   i.e. (nth-ocatve 440 1) will return 880 which is the freq of the next octave from 440."
  [freq n]
  (* freq (math/expt 2 n)))

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

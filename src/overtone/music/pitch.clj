(ns
  ^{:doc "Functions to help generate and manipulate frequencies and sets of related frequencies.
          This is the place for functions representing general musical knowledge, like scales, chords,
          intervals, etc."
     :author "Jeff Rose, Sam Aaron & Marius Kempe"}
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
(def MIDI-RANGE (range 128))
(def MIDDLE-C 60)

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

(defn octave-note
  "Convert an octave and note to a midi note."
  [octave note]
  (+ (+ (* octave 12) note) 12))

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

;; Use a note (:C scale) or (:Eb scale)

;;  You may be interested to know that each of the seven degrees of the diatonic scale has its own name:
;;
;; 1 (do)  tonic
;; 2 (re)  supertonic
;; 3 (mi)  mediant
;; 4 (fa)  subdominant
;; 5 (sol) dominant
;; 6 (la)  submediant/superdominant
;; 7 (ti)  subtonic"


;; Various scale intervals in terms of steps on a piano, or midi note numbers
;; All sequences should add up to 12 - the number of semitones in an octave

(def SCALE   (let [ionian-sequence [2 2 1 2 2 2 1]
                   ionian-len (count ionian-sequence)
                   rotate-ionian (fn [offset] (drop offset (take (+ ionian-len offset) (cycle ionian-sequence))))]

               {:diatonic          ionian-sequence
                :ionian            (rotate-ionian 0)
                :dorian            (rotate-ionian 1)
                :phrygian          (rotate-ionian 2)
                :lydian            (rotate-ionian 3)
                :mixolydian        (rotate-ionian 4)
                :aeolian           (rotate-ionian 5)
                :lochrian          (rotate-ionian 6)

                ;;comment these out for the time being as
                ;;they can be confused with major and minor
                ;;chords:
                ;;:minor             (rotate-ionian 5)
                ;;:major             (rotate-ionian 0)

                :pentatonic        [2 3 2 2 3]
                :major-pentatonic  [2 2 3 2 3]
                :minor-pentatonic  [3 2 2 3 2]
                :whole-tone        [2 2 2 2 2 2]
                :chromatic         [1 1 1 1 1 1 1 1 1 1 1 1]
                :harmonic-minor    [2 1 2 2 1 3 1]
                :melodic-minor-asc [2 1 2 2 2 2 1]
                :hungarian-minor   [2 1 3 1 1 3 1]
                :octatonic         [2 1 2 1 2 1 2 1]
                :messiaen1         [2 2 2 2 2 2]
                :messiaen2         [1 2 1 2 1 2 1 2]
                :messiaen3         [2 1 1 2 1 1 2 1 1]
                :messiaen4         [1 1 3 1 1 1 3 1]
                :messiaen5         [1 4 1 1 4 1]
                :messiaen6         [2 2 1 1 2 2 1 1]
                :messiaen7         [1 1 1 2 1 1 1 1 2 1]}))

(defn scale-field [skey & [sname]]
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

(defn nth-interval
  "Return the count of semitones for the nth degree from the start of the diatonic scale in the specifiec mode (or ionian/major by default).
     i.e. the ionian/major scale has an interval sequence of 2 2 1 2 2 2 1
          therefore the 4th degree is (+ 2 2 1 2) semitones from the start of the scale."
  ([n] (nth-interval :ionian n))
  ([scale n]
     (reduce + (take n (cycle (scale SCALE))))))

(def DEGREE {:i   1
             :ii  2
             :iii 3
             :iv  4
             :v   5
             :vi  6
             :vii 7})

(defn degree->interval
  "Converts the degree of a scale given as a roman numeral keyword or integer and converts it to the number
   of intervals (semitones) from the tonic of the specified scale."
  [scale degree]
  (if-let [deg (DEGREE degree)]
    (nth-interval scale (dec deg))
    (nth-interval scale (dec degree))))

(def CHORD
  (let [major  [0 4 7]
        minor  [0 3 7]
        major7 [0 4 7 11]
        dom7   [0 4 7 10]
        minor7 [0 3 7 10]
        aug    [0 4 8]
        dim    [0 3 6]
        dim7   [0 3 6 9]]
    {:major      major
     :M          major
     :minor      minor
     :m          minor
     :major7     major7
     :dom7       dom7
     :7          dom7
     :M7         major7
     :minor7     minor7
     :m7         minor7
     :augmented  aug
     :a          aug
     :diminished dim
     :dim        dim
     :i          dim
     :diminished7 dim7
     :dim7       dim7
     :i7         dim7}))

(defn chord
  "Returns a set of notes for the specified chord at the specified octave (defaulting to 4).
  (chord :c :major)  ; c major           -> #{60 64 67}
  (chord :a :minor 3); a minor           -> #{57 60 64}
  (chord :Bb :dim)   ; b flat diminished -> #{70 73 76}
  "
  ([root chord-name] (chord root chord-name 4))
  ([root chord-name octave]
     (let [root (if (keyword? root) (NOTE root) root)
           chord-name (if (keyword? chord-name) (CHORD chord-name) chord-name)
           base (octave-note octave root)]
       (set (map #(+ % base) chord-name)))))

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

(defn chosen-from [notes]
  (let [num-notes (count notes)]
    (repeatedly #(get notes (rand-int num-notes)))))

(defn nth-octave
  "Returns the freq n octaves from the supplied reference freq
   i.e. (nth-ocatve 440 1) will return 880 which is the freq of the next octave from 440."
  [freq n]
  (* freq (math/expt 2 n)))

(defn nth-equal-tempered-freq
  "Returns the frequency of a given scale interval using an equal-tempered tuning i.e. dividing all 12 semi-tones equally across an octave. This is currently the standard tuning."
  [base-freq interval]
  (* base-freq (math/expt 2 (/ interval 12))))

(defn interval-freq
  "Returns the frequency of the given interval using the specified mode and tuning (defaulting to ionian and equal-tempered respectively)."
  ([base-freq n] (interval-freq base-freq n :ionian :equal-tempered))
  ([base-freq n mode tuning]
     (case tuning
           :equal-tempered (nth-equal-tempered-freq base-freq (nth-interval n mode)))))

(defn- weighted-bins
  "Creates N bins where N is the smallest power of two greater than the number
  of elements to be chosen from.  Elements are placed into bins according to
  how much weight they are assigned, and each bin contains either one key if it
  fills the bin or a pair of keys split according to how much of each key sits
  in that bin.

  Look here for a vague description of this algorithm:
  http://stackoverflow.com/questions/352670/weighted-random-selection-with-and-without-replacement
  "
  [pairs]
  (let [pairs      (apply hash-map pairs)
        sum        (float (apply + (vals pairs)))
        normalized (map #(/ % sum) (vals pairs))
        elems      (zipmap (keys pairs) normalized)
        n-bins     (first (filter #(> % (count pairs)) (iterate #(* 2 %) 2)))
        bin-size   (/ 1.0 n-bins)]
    (loop [[cur-k cur-v]  (first elems)
           elems (next elems)
           space bin-size
           bins []
           cur-bin []
           i 0]
      (if (and cur-k
               (< i 10))
        (let [amount  (min space cur-v)
              new-pair [cur-k amount]
              space (- space amount)
              new-bin (conj cur-bin new-pair)
              new-bins (if (zero? space)
                         (conj bins new-bin)
                         bins)
              new-bin (if (zero? space) [] new-bin)
              new-space (if (zero? space) bin-size space)
              new-v (- cur-v amount)
              new-elem (if (zero? new-v) (first elems) [cur-k new-v])
              new-elems (if (zero? new-v) (next elems) elems)]
          ;(println cur-k cur-v new-space amount)
          (recur new-elem new-elems new-space new-bins new-bin (inc i)))
        bins))))

(defn log2 [x]
  (/ (Math/log x)
     (Math/log 2)))

(defn choose-weighted [& pairs]
  (println "here...")
  (let [bins (weighted-bins pairs)
        shift (log2 (count bins))
        ;index (bit-shift-left (rand) shift)  ; Damn!
        index (first (drop shift (iterate #(* % 2) (rand))))
        int-index (min (dec (count bins)) (max 0 (int index)))
        leftover (- index int-index)
        _ (println "n-bins: " (count bins) int-index)
        bin (nth bins int-index)]
    (if (= 2 (count bin))
      (if (<= leftover (second (first bin)))
        (ffirst bin)
        (first (second bin)))
      (ffirst bin))))

; Hmmmm...  It doesn't seeem to be producing what we would expect:
;(mapcat (fn [[k v]] [k (/ v 1000000.0)]) (frequencies (take 1000000
;      (repeatedly #(choose-weighted :a 20 :b 50 :c 30)))))

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

;;; ideas:

;;; represent all notes with midi numbers
;;; represent sequences of notes (i.e. scales) with vectors/lists
;;; represent sequences of durations with vectors/lists
;;; [1 3 5 7]
;;; represent chords with sets
;;; #{1 3 5}
;;
;;[1 3 5 #{1 4 5} 7]
;;[1 1 2     6    3]


;; chromatic notes -> 0-11
;; degrees -> i -> vii

;; chord - concrete: (60 64 67)
;; chord - concrete - chromatic notes: (4 7 12)


;; chord - abstract - chromatic notes: (0 4 7)
;; chord - abstract - chromatic notes: (0 4 7)
;; chord - abstract - degrees: (i iii v)

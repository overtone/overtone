(ns ^{:doc "Functions to help generate and manipulate frequencies and
    sets of related frequencies. This is the place for functions
    representing general musical knowledge, like scales, chords,
    intervals, etc."
      :author "Jeff Rose, Sam Aaron & Marius Kempe"}
  overtone.music.pitch
  (:use [overtone.helpers old-contrib]
        [overtone.helpers.map :only [reverse-get]]
        [overtone.algo chance])
  (:require [clojure.string :as string]))

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
  "Returns a frequency computed by adding n-cents to freq.  A cent is
  a logarithmic measurement of pitch, where 1-octave equals 1200
  cents."
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
  "Invert a sequence of notes using either the first note as the
  stationary pivot point or the optional second argument."
  [notes & [pivot]]
  (let [pivot (or pivot (first notes))]
    (for [n notes] (- pivot (- n pivot)))))

(defn octave-note
  "Convert an octave and interval to a midi note."
  [octave interval]
  (+ (* octave 12) interval 12))

(def NOTES {:C  0  :c  0  :b# 0  :B# 0
            :C# 1  :c# 1  :Db 1  :db 1  :DB 1  :dB 1
            :D  2  :d  2
            :D# 3  :d# 3  :Eb 3  :eb 3  :EB 3  :eB 3
            :E  4  :e  4
            :E# 5  :e# 5  :F  5  :f  5
            :F# 6  :f# 6  :Gb 6  :gb 6  :GB 6  :gB 6
            :G  7  :g  7
            :G# 8  :g# 8  :Ab 8  :ab 8  :AB 8  :aB 8
            :A  9  :a  9
            :A# 10 :a# 10 :Bb 10 :bb 10 :BB 10 :bB 10
            :B  11 :b  11 :Cb 11 :cb 11 :CB 11 :cB 11})

(def REVERSE-NOTES
  {0 :C
   1 :C#
   2 :D
   3 :Eb
   4 :E
   5 :F
   6 :F#
   7 :G
   8 :Ab
   9 :A
   10 :Bb
   11 :B})

(defn canonical-pitch-class-name
  "Returns the canonical version of the specified pitch class pc."
  [pc]
  (let [pc (keyword (name pc))]
      (REVERSE-NOTES (NOTES pc))))

(def MIDI-NOTE-RE-STR "([a-gA-G][#bB]?)([-0-9]+)" )
(def MIDI-NOTE-RE (re-pattern MIDI-NOTE-RE-STR))
(def ONLY-MIDI-NOTE-RE (re-pattern (str "\\A" MIDI-NOTE-RE-STR "\\Z")))

(defn- midi-string-matcher
  "Determines whether a midi keyword is valid or not. If valid,
  returns a regexp match object"
  [mk]
  (re-find ONLY-MIDI-NOTE-RE (name mk)))

(defn- validate-midi-string!
  "Throws a friendly exception if midi-keyword mk is not
  valid. Returns matches if valid."
  [mk]
  (let [matches (midi-string-matcher mk)]
    (when-not matches
      (throw (IllegalArgumentException.
              (str "Invalid midi-string. " mk
                   " does not appear to be in MIDI format i.e. C#4"))))

    (let [[match pictch-class octave] matches]
      (when (< (Integer. octave) -1)
        (throw (IllegalArgumentException.
                (str "Invalid midi-string: " mk
                     ". Octave is out of range. Lowest octave value is -1")))))
    matches))

(defn note-info
  "Takes a string representing a midi note such as C4 and returns a map
  of note info"
  [midi-string]
  (let [[match pitch-class octave] (validate-midi-string! midi-string)
        pitch-class                (canonical-pitch-class-name pitch-class)
        octave                     (Integer. octave)
        interval                   (NOTES (keyword pitch-class))]
    {:match       match
     :pitch-class pitch-class
     :octave      (Integer. octave)
     :interval    interval
     :midi-note   (octave-note octave interval)}))

(defn mk-midi-string
  "Takes a string or keyword representing a pitch and a number
  representing an integer and returns a new string which is a
  concatanation of the two. Throws an error if the resulting midi
  string is invalid.

  (midi-string :F 7)  ;=> \"F7\"
  (midi-string :Eb 3) ;=> \"Eb3\""
  [pitch-key octave]
  (let [res (str (name pitch-key) octave)]
    (validate-midi-string! res)
    res))

(defn note
  "Resolves note to MIDI number format. Resolves upper and lower-case
  keywords and strings in MIDI note format. If given an integer or
  nil, returns them unmodified. All other inputs will raise an
  exception.

  Usage examples:

  (note \"C4\")  ;=> 60
  (note \"C#4\") ;=> 61
  (note \"eb2\") ;=> 39
  (note :F#7)    ;=> 102
  (note :db5)    ;=> 73
  (note 60)      ;=> 60
  (note nil)     ;=> nil"
  [n]
  (cond
    (nil? n) nil
    (integer? n) (if (>= n 0)
                   n
                   (throw (IllegalArgumentException.
                           (str "Unable to resolve note: "
                                n
                                ". Value is out of range. Lowest value is 0"))))
    (keyword? n) (note (name n))
    (string? n) (:midi-note (note-info n))
    :else (throw (IllegalArgumentException. (str "Unable to resolve note: " n ". Wasn't a recognised format (either an integer, keyword, string or nil)")))))

(defn match-note
  "Returns the first midi-note formatted substring in s. If passed
   optional prev and pos strings will use them to generate positive
   look ahead and behind matchers. "
  ([s] (match-note s "" ""))
  ([s prev-str post-str]
     (let [look-behind (if prev-str (str "(?<=" prev-str ")") "")
           look-ahead  (if post-str (str "(?=" post-str ")") "")
           match       (re-find (re-pattern (str look-behind MIDI-NOTE-RE-STR look-ahead)) s)]
       (when match
         (let [[match pitch-class octave] match]
           (note-info match))))))



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

(def SCALE
  (let [ionian-sequence [2 2 1 2 2 2 1]
        ionian-len      (count ionian-sequence)
        rotate-ionian   (fn [offset]
                          (take ionian-len
                                (drop offset (cycle ionian-sequence))))]
  {:diatonic          ionian-sequence
   :ionian            (rotate-ionian 0)
   :major             (rotate-ionian 0)
   :dorian            (rotate-ionian 1)
   :phrygian          (rotate-ionian 2)
   :lydian            (rotate-ionian 3)
   :mixolydian        (rotate-ionian 4)
   :aeolian           (rotate-ionian 5)
   :minor             (rotate-ionian 5)
   :locrian           (rotate-ionian 6)
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

(defn resolve-scale
  "Either looks the scale up in the map of SCALEs if it's a keyword or
  simply returns it unnmodified. Allows users to specify a scale
  either as a seq such as [2 2 1 2 2 2 1] or by keyword such
  as :aeolian"
  [scale]
  (if (keyword? scale)
    (SCALE scale)
    scale))

(defn scale-field
  "Create the note field for a given scale.  Scales are specified with
  a keyword representing the key and an optional scale
  name (defaulting to :major):
  (scale-field :g)
  (scale-field :g :minor)"
  [skey & [sname]]
  (let [base (NOTES skey)
        sname (or sname :major)
        intervals (SCALE sname)]
    (reverse (next
      (reduce (fn [mem interval]
              (let [new-note (+ (first mem) interval)]
                (conj mem new-note)))
            (list base)
            (take (* 8 12) (cycle intervals)))))))

(defn nth-interval
  "Return the count of semitones for the nth degree from the start of
  the diatonic scale in the specific mode (or ionian/major by
  default).

  i.e. the ionian/major scale has an interval sequence of 2 2 1 2 2 2
       1 therefore the 4th degree is (+ 2 2 1 2) semitones from the
       start of the scale."
  ([n] (nth-interval :diatonic n))
  ([scale n]
     (reduce + (take n (cycle (scale SCALE))))))

(def DEGREE {:i     1
             :ii    2
             :iii   3
             :iv    4
             :v     5
             :vi    6
             :vii   7
             :_     nil})

(defn degree->int
  [degree]
  (if (some #{degree} (keys DEGREE))
    (degree DEGREE)
    (throw (IllegalArgumentException. (str "Unable to resolve degree: " degree ". Was expecting a roman numeral in the range :i -> :vii or the nil-note symbol :_")))))

(defn resolve-degree
  "returns a map representing the degree, and the octave semitone
  shift (i.e. sharp flat)"
  ([degree] (resolve-degree degree 0 0))
  ([degree octave-shift semitone-shift]
     (cond
      (.endsWith (name degree) "-")
      (resolve-degree (keyword (chop (name degree))) (dec octave-shift) semitone-shift)

      (.endsWith (name degree) "+")
      (resolve-degree (keyword (chop (name degree))) (inc octave-shift) semitone-shift)

      (.endsWith (name degree) "b")
      (resolve-degree (keyword (chop (name degree))) octave-shift (dec semitone-shift))

      (.endsWith (name degree) "#")
      (resolve-degree (keyword (chop (name degree))) octave-shift (inc semitone-shift))

      :default
      (let [degree (degree->int degree)]
        {:degree degree
         :octave-shift octave-shift
         :semitone-shift semitone-shift}))))

(defn degree->interval
  "Converts the degree of a scale given as a roman numeral keyword and
  converts it to the number of semitones from the tonic of
  the specified scale.

  (degree->interval :ii :major) ;=> 2

  Trailing #, b, + - represent sharps, flats, octaves up and down
  respectively.  An arbitrary number may be added in any order."
  [degree scale]
  (cond
    (nil? degree) nil
    (= :_ degree) nil

    (number? degree) (nth-interval scale (dec degree))

    (keyword? degree) (let [degree     (resolve-degree degree)
                            interval   (nth-interval scale (dec (:degree degree)))
                            oct-shift  (* 12 (:octave-shift degree))
                            semi-shift (:semitone-shift degree)]
                        (+ interval oct-shift semi-shift))))

(defn degrees->pitches
  "Convert intervals to pitches in MIDI number format.  Supports
  nested collections."
  [degrees scale root]
  (let [root (note root)]
    (when (nil? root)
      (throw (IllegalArgumentException. (str "root resolved to a nil value. degrees->pitches requires a non-nil root."))))
    (map (fn [degree]
           (cond
            (coll? degree) (degrees->pitches degree scale root)
            (nil? degree) nil
            :default (if-let [interval (degree->interval degree scale)]
                       (+ root interval))))
         degrees)))

(defn resolve-degrees
  "Either maps the degrees to integers if they're keywords using the map DEGREE
  or leaves them unmodified"
  [degrees]
  (map #(if (keyword? %) (DEGREE %) %) degrees))

(defn scale
  "Returns a list of notes for the specified scale. The root must be
   in midi note format i.e. :C4 or :Bb4


   (scale :c4 :major)  ; c major      -> (60 62 64 65 67 69 71 72)
   (scale :Bb4 :minor) ; b flat minor -> (70 72 73 75 77 78 80 82)"

  ([root scale-name] (scale root scale-name (range 1 8)))
  ([root scale-name degrees]
     (let [root (note root)
           degrees (resolve-degrees degrees)]
       (cons root (map #(+ root (nth-interval scale-name %)) degrees)))))

(def CHORD
  (let [major  #{0 4 7}
        minor  #{0 3 7}
        major7 #{0 4 7 11}
        dom7   #{0 4 7 10}
        minor7 #{0 3 7 10}
        aug    #{0 4 8}
        dim    #{0 3 6}
        dim7   #{0 3 6 9}]
    {:1         #{0}
     :5         #{0 7}
     :+5        #{0 4 8}
     :m+5       #{0 3 8}
     :sus2      #{0 2 7}
     :sus4      #{0 5 7}
     :6         #{0 4 7 9}
     :m6        #{0 3 7 9}
     :7sus2     #{0 2 7 10}
     :7sus4     #{0 5 7 10}
     :7-5       #{0 4 6 10}
     :m7-5      #{0 3 6 10}
     :7+5       #{0 4 8 10}
     :m7+5      #{0 3 8 10}
     :9         #{0 4 7 10 14}
     :m9        #{0 3 7 10 14}
     :maj9      #{0 4 7 11 14}
     :9sus4     #{0 5 7 10 14}
     :6*9       #{0 4 7 9 14}
     :m6*9      #{0 3 9 7 14}
     :7-9       #{0 4 7 10 13}
     :m7-9      #{0 3 7 10 13}
     :7-10      #{0 4 7 10 15}
     :9+5       #{0 10 13}
     :m9+5      #{0 10 14}
     :7+5-9     #{0 4 8 10 13}
     :m7+5-9    #{0 3 8 10 13}
     :11        #{0 4 7 10 14 17}
     :m11       #{0 3 7 10 14 17}
     :maj11     #{0 4 7 11 14 17}
     :11+       #{0 4 7 10 14 18}
     :m11+      #{0 3 7 10 14 18}
     :13        #{0 4 7 10 14 17 21}
     :m13       #{0 3 7 10 14 17 21}
     :major      major
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

(defn resolve-chord
  "Either looks the chord up in the map of CHORDs if it's a keyword or
  simply returns it unnmodified. Allows users to specify a chord
  either with a set such as #{0 4 7} or by keyword such as :major"
  [chord]
  (if (keyword? chord)
    (CHORD chord)
    chord))

(defn- inc-first
  "Remove the first element, increment it by n, and append to seq."
  [elems n]
  (concat (next elems) [(+ n (first elems))]))

(defn- dec-last
  "Remove the last element, decrement it by n, and prepend to seq."
  [elems n]
  (concat [(- (last elems) n)] (next elems)))

(defn invert-chord
  "Move a chord voicing up or down.

    ;first inversion
    (invert-chord [60 64 67] 1) ;=> (64 67 72)

    ; second inversion
    (invert-chord [60 64 67] 1) ;=> (67 72 76)
  "
  [notes shift]
  (cond
    (pos? shift) (recur (inc-first notes 12) (dec shift))
    (neg? shift) (recur (dec-last notes 12) (inc shift))
    (zero? shift) notes))

(defn chord
  "Returns a set of notes for the specified chord. The root must be in
  midi note format i.e. :C4.

  (chord :c4 :major)  ; c major           -> #{60 64 67}
  (chord :a4 :minor)  ; a minor           -> #{57 60 64}
  (chord :Bb4 :dim)   ; b flat diminished -> #{70 73 76}
  "
  ([root chord-name]
   (chord root chord-name 0))
  ([root chord-name inversion]
     (let [root (note root)
           chord (resolve-chord chord-name)
           notes (map #(+ % root) chord)]
       (invert-chord notes inversion))))

(defn rand-chord
  "Generates a random list of MIDI notes with cardinality num-pitches
  bound within the range of the specified root and pitch-range and
  only containing pitches within the specified chord-name. Similar to
  Impromptu's pc:make-chord"
  [root chord-name num-pitches pitch-range]
  (let [chord (chord root chord-name)
        root (note root)
        max-pitch (+ pitch-range root)
        roots (range 0 max-pitch 12)
        notes (flatten (map (fn [root] (map #(+ root %) chord)) roots))
        notes (take-while #(<= % max-pitch) notes)]
    (sort (choose-n num-pitches notes))))

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

(defn nth-octave
  "Returns the freq n octaves from the supplied reference freq

   i.e. (nth-ocatve 440 1) will return 880 which is the freq of the
   next octave from 440."
  [freq n]
  (* freq (java.lang.Math/pow 2 n)))

(defn nth-equal-tempered-freq
  "Returns the frequency of a given scale interval using an
  equal-tempered tuning i.e. dividing all 12 semi-tones equally across
  an octave. This is currently the standard tuning."
  [base-freq interval]
  (* base-freq (java.lang.Math/pow 2 (/ interval 12))))

(defn interval-freq
  "Returns the frequency of the given interval using the specified
  mode and tuning (defaulting to ionian and equal-tempered
  respectively)."
  ([base-freq n] (interval-freq base-freq n :ionian :equal-tempered))
  ([base-freq n mode tuning]
     (case tuning
           :equal-tempered (nth-equal-tempered-freq base-freq (nth-interval n mode)))))

(defn find-scale-name
  "Return the name of the first matching scale found in SCALE
  or nil if not found

  ie: (find-scale-name [2 1 2 2 2 2 1]
  :melodic-minor-asc"
  [scale]
  (reverse-get SCALE scale))

(defn find-pitch-class-name
  "Given a midi number representing a note, returns the name of the note
  independent of octave.

  (find-pitch-class-name 62) ;=> :D
  (find-pitch-class-name 74) ;=> :D
  (find-pitch-class-name 75) ;=> :Eb"
  [note]
  (REVERSE-NOTES (mod note 12)))

(defn find-note-name
  [note]
  "Given a midi number representing a note, returns a keyword
  representing the note including octave number. Reverse of the fn note.

  (find-note-name 45) ;=> A2
  (find-note-name 57) ;=> A3
  (find-note-name 58) ;=> Bb3"
  (let [octave (dec (int (/ note 12)))]
    (keyword (str (name (find-pitch-class-name note)) octave))))

(defn- fold-note
  "Folds note intervals into a 2 octave range so that chords using
  notes spread across multiple octaves can be correctly recognised."
  [note]
  (if (or (< 21 note) (contains? #{20 19 16 12} note))
    (fold-note (- note 12))
     note ))

(defn- simplify-chord
  "Expects notes to contain 0 (the root note) Reduces all notes into 2
  octaves. This will allow identification of fancy jazz chords, but
  will miss some simple chords if they are spread over more than 1
  octave."
  [notes]
  (set (map (fn [x] (fold-note x)) notes)))

(defn- compress-chord
  "Expects notes to contain 0 (the root note) Reduces all notes into 1
  octave. This will lose all the fancy jazz chords but recognise
  sparse multiple octave simple chords"
  [notes]
  (set (map (fn [x] (mod x 12)) notes)))

(defn- select-root
  "Adds a new root note below the lowest note present in notes"
  [notes root-index]
  (if (< 0 root-index)
    (let [new-root (nth (seq (sort notes)) root-index)
         lowest-note (first (sort notes))
         octaves (+ 1 (quot (- new-root lowest-note) 12))]
      (set (cons (- new-root (* octaves 12)) notes)))
    notes))

(defn- find-chord-with-low-root
  "Finds the chord represented by notes
   Assumes the root note is the lowest note in notes
   notes can be spread over multiple octaves"
  [notes]
  (if (< 0 (count notes))
    (let [root (first (sort notes))
          adjusted-notes (set (map (fn [x] (- x root)) notes ))]
      (or (reverse-get CHORD (simplify-chord adjusted-notes))
          (reverse-get CHORD (compress-chord adjusted-notes))))))

(defn find-chord
  [notes]
  (loop [note 0]
    (if (< note (count notes) )
      (let [mod-notes (select-root notes note)
            chord  (find-chord-with-low-root mod-notes)
            root (find-pitch-class-name (first (sort mod-notes)))]
       (if chord
         {:root root :chord-type chord}
         (recur (inc note))))
      nil)))

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

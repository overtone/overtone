(ns overtone.music.pitch
  "Functions to help generate and manipulate frequencies and sets of related
  frequencies. This is the place for functions representing general musical
  knowledge, like scales, chords, intervals, etc.
  
  Scientific pitch notation is used to represent notes as strings
  https://en.wikipedia.org/wiki/Scientific_pitch_notation

  We denote Middle C (60) as C4, with octaves ranging from -1 to 9.
  Only single flats and sharps are supported."
  {:author "Jeff Rose, Sam Aaron & Marius Kempe"}
  (:use [overtone.helpers old-contrib]
        [overtone.helpers.map :only [reverse-get]]
        [overtone.algo chance])
  (:require [clojure.string :as string]
            [overtone.music.pitch :as-alias p]))

(set! *warn-on-reflection* true)

;; Notes in a typical scale are related by small, prime number ratios. Of all
;; possible 7 note scales, the major scale has the highest number of consonant
;; intervals.

(declare note midi->hz note-info)

(defmacro defratio
  ([rname ratio] `(defratio ~rname ~(name rname) ~ratio))
  ([rname doc ratio]
   (assert (simple-symbol? rname))
   (assert (string? doc))
   (assert (number? ratio))
   (let [constant-name (symbol (str (name rname) "-ratio"))
         doc (or doc (name rname))]
     `(do (def ~constant-name
            ~(str "The ratio of a pair of pitches related by a " doc)
            ~ratio)
          (defn ~rname
            ~(str "Returns the frequency raised by a " doc ".\n"
                  "If not a number, input will be treated as a note.")
            {:arglists '([freq-or-note])}
            [freq#] (* (if (number? freq#)
                         freq#
                         (-> freq# note midi->hz))
                       ~ratio))))))

;; Perfect consonance
(defratio unison 1/1)
(defratio octave 2/1)
(defratio fourth "perfect fourth" 4/3)
(defratio fifth  "perfect fifth" 3/2)

;; Imperfect consonance
(defratio sixth "major sixth" 5/3)
(defratio third "major third" 5/4)
(defratio min-third "minor third" 6/5)
(defratio min-sixth "minor sixth" 8/5)

(defn cents
  "Returns a frequency computed by adding n-cents to freq.  A cent is
  a logarithmic measurement of pitch, where 1-octave equals 1200
  cents."
  [freq n-cents]
  (* freq (java.lang.Math/pow 2 (/ n-cents 1200))))

;; MIDI
(def ^:private MIDI-LOWEST-NOTE 0)
(def ^:private MIDI-HIGHEST-NOTE 127)
(def ^:private MIDI-LOWEST-OCTAVE -1)
(def ^:private MIDI-HIGHEST-OCTAVE 9)
(def MIDI-RANGE (range MIDI-LOWEST-NOTE (inc MIDI-HIGHEST-NOTE)))
(def MIDDLE-C 60)

;; Manipulating pitch using midi note numbers

(defn- validate-midi-note-number! [note]
  (when-not (and (int? note)
                 (<= MIDI-LOWEST-NOTE
                     note
                     MIDI-HIGHEST-NOTE))
    (throw (ex-info (str "Invalid note number " (pr-str note)
                         ": must be between " MIDI-LOWEST-NOTE " and " MIDI-HIGHEST-NOTE)
                    {})))
  note)

(defn shift
  "Shift the 'notes' in 'phrase' by a given 'amount' of half-steps."
  [phrase notes amount]
  (reduce
    (fn [phrase n]
      (update phrase n #(validate-midi-note-number!
                          (+ % amount))))
    phrase notes))

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

(defn- validate-octave! [octave]
  (when-not (and (int? octave)
                 (<= MIDI-LOWEST-OCTAVE
                     octave
                     MIDI-HIGHEST-OCTAVE))
    (throw (ex-info (str "Invalid MIDI octave number "
                         (pr-str octave)
                         ": must be between "
                         MIDI-LOWEST-OCTAVE " and " MIDI-HIGHEST-OCTAVE)
                    {})))
  octave)


(defn- validate-scale! [scale]
  (when-not (and (sequential? scale)
                 (every? pos-int? scale)
                 (= 12 (apply + scale)))
    (throw (ex-info (str "Invalid scale, must be positive integers that add up to 12: "
                         (pr-str scale))
                    {})))
  scale)

(defn octave-of
  "Returns the octave of the note. Defaults to 4.
  
  (sut/octave-of 0) => -1  ;; lowest note is in lowest octave
  (sut/octave-of 127) => 0  ;; highest note is in highest octave
  (sut/octave-of :C) => 4  ;; defaults to octave 4
  (sut/octave-of :B8) => 8"
  [note]
  (-> note note-info (:octave 4)))

(defn resolve-octave
  "Coerces input to an octave. If given a number,
  is treated as an octave identifier. Otherwise,
  is treated as a note and passed to [[octave-of]].
  
  (resolve-octave -1) => -1  ;; lowest octave
  (resolve-octave 9) => 9    ;; highest octave
  (resolve-octave :C) => 4   ;; defaults to octave 4
  (resolve-octave :D8) => 8"
  [octave-or-note]
  (if (int? octave-or-note)
    (validate-octave! octave-or-note)
    (octave-of octave-or-note)))

(defn pitch-class
  "Returns a number from 0-11 representing the pitch
  class of the note, representing the scale degrees
  of the C chromatic scale."
  [note]
  (if (int? note)
    (mod (validate-midi-note-number! note) 12)
    (-> note note-info :interval)))

(defn octave-note
  "Convert an octave and pitch class to a midi note. Pitch
  defaults to 0. Position can be 0-11 for all octaves except
  octave 9, which must be 0-7.
  
  (octave-note -1) => 0    ;; lowest note
  (octave-note -1 1) => 1  ;; lowest note"
  ([octave] (octave-note octave 0))
  ([octave pitch-class]
   (let [octave (resolve-octave octave)
         pitch-class (p/pitch-class pitch-class)]
     (if (= MIDI-HIGHEST-OCTAVE octave)
       (when-not (and (int? pitch-class)
                      (<= 0 pitch-class 7))
         (throw (ex-info (str "Invalid pitch-class, must be between 0-7 for highest octave: "
                              (pr-str pitch-class))
                         {})))
       (when-not (and (int? pitch-class)
                      (<= 0 pitch-class 11))
         (throw (ex-info (str "Invalid pitch-class, must be betwen 0-11: " (pr-str pitch-class))
                         {}))))
     (+ (* octave 12) pitch-class 12))))

(def NOTES {:C  0  :c  0  :b# 0  :B# 0
            :C# 1  :c# 1  :Db 1  :db 1  :DB 1  :dB 1
            :D  2  :d  2
            :D# 3  :d# 3  :Eb 3  :eb 3  :EB 3  :eB 3
            :E  4  :e  4  :Fb 4  :fb 4  :FB 4  :fB 4
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

(declare note-info)

(defn canonical-pitch-class-name
  "Returns the canonical version of pitch class of the provided note.
  Canonical names are the notes of the C major scale
  plus C#, Eb, F#, Ab, and Bb."
  [note]
  (if (int? note)
    (REVERSE-NOTES (pitch-class note))
    (or (REVERSE-NOTES (NOTES note))
        (-> note note-info :pitch-class))))

(def MIDI-NOTE-RE-STR "([a-gA-G][#bB]?)([-0-9]+)?" )
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
                   " does not appear to be in MIDI format e.g. C#4"))))

    (let [[match _pitch-class ^String octave-str] matches]
      (when-some [octave (some-> octave-str Integer.)]
        (when-not (<= MIDI-LOWEST-OCTAVE
                      octave
                      MIDI-HIGHEST-OCTAVE)
          (throw (IllegalArgumentException.
                   (str "Invalid midi-string: " (pr-str mk)
                        ". Octave is out of range: must be between "
                        MIDI-LOWEST-OCTAVE " and " MIDI-HIGHEST-OCTAVE))))))
    matches))

(declare find-note-name)

(defn note-info
  "Takes a simple ident, string, or integer representing a midi note such as C4 and
  returns a map of note info. If the octave is omitted, :octave and :midi-note are not
  populated.
 
  :match - the input as a string, or the canonical pitch class if given an integer
  :pitch-class - the canonical pitch class according to `canonical-pitch-class-name`
  :spelling - the combination of note name and accidentals most resembling original input
  :interval - the number of descending notes until a C natural is reached.
              If octave specified, also the number of notes from the beginning of the octave.
  :octave - the octave number, if octave specified
  :midi-note - the midi note number, if octave specified"
  [midi-note]
  (let [midi-note (cond-> midi-note
                    (int? midi-note) find-note-name)
        [match spelling octave] (validate-midi-string! midi-note)
        pitch-class (canonical-pitch-class-name (keyword spelling))
        octave                     (when octave (Integer. ^String octave))
        interval                   (NOTES pitch-class)
        midi-note (some-> octave (octave-note interval))]
    (cond-> {:match       match
             :spelling    spelling
             :pitch-class pitch-class
             :interval    interval}
      octave (assoc :octave octave :midi-note midi-note))))

(defn mk-midi-string
  "Takes a note and an octave and returns a string representing
  the note in that octave.

  (mk-midi-string :F 7)  ;=> \"F7\"
  (mk-midi-string :Eb 3) ;=> \"Eb3\""
  [note octave]
  (validate-octave! octave)
  (str (name (:spelling (note-info note))) octave))

(defn note
  "Resolves note to MIDI number format. Resolves upper and lower-case
  simple idents and strings in MIDI note format. If given an integer or
  nil, returns them unmodified. All other inputs will raise an
  exception. Octave defaults to 4.

  Usage examples:

  (note \"C\")   ;=> 60
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
    (int? n) (validate-midi-note-number! n)
    (simple-ident? n) (note (name n))
    (string? n) (let [info (note-info n)]
                  (or (:midi-note info)
                      (+ 60 (:interval info))))
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
         (let [[match _pitch-class _octave] match]
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
  (let [ionian-sequence     [2 2 1 2 2 2 1]
        hex-sequence        [2 2 1 2 2 3]
        pentatonic-sequence [3 2 2 3 2]
        rotate (fn [scale-sequence offset]
                 (take (count scale-sequence)
                       (drop offset (cycle scale-sequence))))]
    {:diatonic           ionian-sequence
     :ionian             (rotate ionian-sequence 0)
     :major              (rotate ionian-sequence 0)
     :dorian             (rotate ionian-sequence 1)
     :phrygian           (rotate ionian-sequence 2)
     :lydian             (rotate ionian-sequence 3)
     :mixolydian         (rotate ionian-sequence 4)
     :aeolian            (rotate ionian-sequence 5)
     :minor              (rotate ionian-sequence 5)
     :locrian            (rotate ionian-sequence 6)
     :hex-major6         (rotate hex-sequence 0)
     :hex-dorian         (rotate hex-sequence 1)
     :hex-phrygian       (rotate hex-sequence 2)
     :hex-major7         (rotate hex-sequence 3)
     :hex-sus            (rotate hex-sequence 4)
     :hex-aeolian        (rotate hex-sequence 5)
     :minor-pentatonic   (rotate pentatonic-sequence 0)
     :yu                 (rotate pentatonic-sequence 0)
     :major-pentatonic   (rotate pentatonic-sequence 1)
     :gong               (rotate pentatonic-sequence 1)
     :egyptian           (rotate pentatonic-sequence 2)
     :shang              (rotate pentatonic-sequence 2)
     :jiao               (rotate pentatonic-sequence 3)
     :pentatonic         (rotate pentatonic-sequence 4) ;; historical match
     :zhi                (rotate pentatonic-sequence 4)
     :ritusen            (rotate pentatonic-sequence 4)
     :whole-tone         [2 2 2 2 2 2]
     :whole              [2 2 2 2 2 2]
     :chromatic          [1 1 1 1 1 1 1 1 1 1 1 1]
     :harmonic-minor     [2 1 2 2 1 3 1]
     :melodic-minor-asc  [2 1 2 2 2 2 1]
     :hungarian-minor    [2 1 3 1 1 3 1]
     :octatonic          [2 1 2 1 2 1 2 1]
     :messiaen1          [2 2 2 2 2 2]
     :messiaen2          [1 2 1 2 1 2 1 2]
     :messiaen3          [2 1 1 2 1 1 2 1 1]
     :messiaen4          [1 1 3 1 1 1 3 1]
     :messiaen5          [1 4 1 1 4 1]
     :messiaen6          [2 2 1 1 2 2 1 1]
     :messiaen7          [1 1 1 2 1 1 1 1 2 1]
     :super-locrian      [1 2 1 2 2 2 2]
     :hirajoshi          [2 1 4 1 4]
     :kumoi              [2 1 4 2 3]
     :neapolitan-major   [1 2 2 2 2 2 1]
     :bartok             [2 2 1 2 1 2 2]
     :bhairav            [1 3 1 2 1 3 1]
     :locrian-major      [2 2 1 1 2 2 2]
     :ahirbhairav        [1 3 1 2 2 1 2]
     :enigmatic          [1 3 2 2 2 1 1]
     :neapolitan-minor   [1 2 2 2 1 3 1]
     :pelog              [1 2 4 1 4]
     :augmented2         [1 3 1 3 1 3]
     :scriabin           [1 3 3 2 3]
     :harmonic-major     [2 2 1 2 1 3 1]
     :melodic-minor-desc [2 1 2 2 1 2 2]
     :romanian-minor     [2 1 3 1 2 1 2]
     :hindu              [2 2 1 2 1 2 2]
     :iwato              [1 4 1 4 2]
     :melodic-minor      [2 1 2 2 2 2 1]
     :diminished2        [2 1 2 1 2 1 2 1]
     :marva              [1 3 2 1 2 2 1]
     :melodic-major      [2 2 1 2 1 2 2]
     :indian             [4 1 2 3 2]
     :spanish            [1 3 1 2 1 2 2]
     :prometheus         [2 2 2 5 1]
     :diminished         [1 2 1 2 1 2 1 2]
     :todi               [1 2 3 1 1 3 1]
     :leading-whole      [2 2 2 2 2 1 1]
     :augmented          [3 1 3 1 3 1]
     :purvi              [1 3 2 1 1 3 1]
     :chinese            [4 2 1 4 1]
     :lydian-minor       [2 2 2 1 1 2 2]}))

(defn resolve-scale
  "Either looks the scale up in the map of SCALEs if it's a keyword or
  simply returns it unmodified after verifying it is a valid scale.
  Allows users to specify a scale either as a seq such as [2 2 1 2 2 2 1]
  or by keyword such as :aeolian. Scales must contain positive integers that
  sum to 12."
  [scale]
  (if (keyword? scale)
    (or (SCALE scale)
        (throw (ex-info (str "Unknown scale: " (pr-str scale)) {})))
    (validate-scale! scale)))

(defn scale-field
  "Create the note field for a given scale.  Scales are specified with
  a keyword representing the key and an optional scale
  name (defaulting to :major):
  (scale-field :g)
  (scale-field :g :minor)"
  ([root] (scale-field root nil))
  ([root scale]
   (let [base (:interval (note-info root))
         intervals (vec (resolve-scale (or scale :major)))
         nintervals (count intervals)]
     (loop [field []
            note (- base 12) ;; start 1-12 notes below MIDI-LOWEST-NOTE
            interval-idx (num 0)]
       (let [note (+ note (nth intervals interval-idx))]
         (if (<= MIDI-LOWEST-NOTE note)
           (if (<= note MIDI-HIGHEST-NOTE)
             (recur (conj field note)
                    note
                    (mod (inc interval-idx) nintervals))
             field)
           (recur field
                  note
                  (mod (inc interval-idx) nintervals))))))))

(defn nth-interval
  "Return the count of semitones for the nth degree from the start of
  the diatonic scale in the specific mode (or ionian/major by
  default).

  i.e. the ionian/major scale has an interval sequence of 2 2 1 2 2 2 1
       therefore the 4th degree is (+ 2 2 1 2) semitones from the start of the scale."
  ([n] (nth-interval :diatonic n))
  ([scale n]
   (if (neg? n)
     (- (reduce + (eduction (take (- n)) (cycle (reverse (resolve-scale scale))))))
     (reduce + (eduction (take n) (cycle (resolve-scale scale)))))))

(def DEGREE {:i     1
             :ii    2
             :iii   3
             :iv    4
             :v     5
             :vi    6
             :vii   7
             :I     1
             :II    2
             :III   3
             :IV    4
             :V     5
             :VI    6
             :VII   7
             :_     nil})

(defn degree->int
  [degree]
  (cond
    (and (int? degree)
         (<= 1 degree 7))
    degree

    (some #{degree} (keys DEGREE))
    (degree DEGREE)

    :else
    (throw (IllegalArgumentException. (str "Unable to resolve degree: " degree ". Was expecting a roman numeral in the range :i -> :vii or the nil-note symbol :_")))))

(defn resolve-degree
  "returns a map representing the degree, and the octave semitone
  shift (i.e. sharp flat)

  Usage example:

  (resolve-degree :iv)      ;=> {:degree 4, :octave-shift 0, :semitone-shift 0}
  (resolve-degree :vii 3 5) ;=> {:degree 7, :octave-shift 3, :semitone-shift 5}
  "
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
  "Returns a list of the first 8 midi note numbers for the specified scale.
  The root must be in any valid midi note format, see [[note]]. e.g. `:C4`, `\"Bb4\"`, `60`.
  If degrees is provided as a list of integers, returns a list of midi note numbers 
  corresponding to the degrees of the scale. Negative degrees count backwards from root,
  and degrees beyond the number of notes in the scale in either direction continue with
  the same scale.

  Root note defaults to C. Octave defaults to 4.

  (scale :c4 :major)  ; c major      -> (60 62 64 65 67 69 71 72)
  (scale :Bb4 :minor) ; b flat minor -> (70 72 73 75 77 78 80 82)
  (scale :c4 :chromatic) ; chromatic scale
  -> (60 61 62 63 64 65 66 67 68 69 70 71 72)
  (scale :c4 :major [0 2 5 12]) ; c major chord
  -> (60 64 67 72)"
  ([] (scale :major))
  ([scale-name] (scale MIDDLE-C scale-name))
  ([root scale-name]
   (let [{:keys [midi-note] :as info} (note-info root)
         root (or midi-note (+ MIDDLE-C (:interval info)))
         scale (resolve-scale scale-name)]
     (mapv #(validate-midi-note-number! (+ root (nth-interval scale %)))
           ;; fix: don't assume each scale has 7 degrees
           (range (inc (count scale))))))
  ;; breaking change: don't prepend root
  ([root scale-name degrees]
   (let [{:keys [midi-note] :as info} (note-info root)
         root (or midi-note (+ MIDDLE-C (:interval info)))
         degrees (resolve-degrees degrees)]
     (map #(validate-midi-note-number!
             (+ root (nth-interval scale-name %)))
          degrees))))

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
     :m7+9      #{0 3 7 10 14}
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
  (concat [(- (last elems) n)] (pop (vec elems))))

(defn invert-chord
  "Move a chord voicing up or down.

    ;first inversion
    (invert-chord [60 64 67] 1) ;=> (64 67 72)

    ; second inversion
    (invert-chord [60 64 67] 2) ;=> (67 72 76)
  "
  [notes shift]
  (cond
    (pos? shift) (recur (inc-first notes 12) (dec shift))
    (neg? shift) (recur (dec-last notes 12) (inc shift))
    (zero? shift) notes))

(defn chord
  "Returns a set of notes for the specified chord.

  The root must be in any valid midi note format, as per [[note]]. e.g. `:C4`,
  `\"Bb4\"`, `60`.

  (chord :c4 :major)  ; c major           -> (60 64 67)
  (chord :a4 :minor)  ; a minor           -> (69 72 76)
  (chord :Bb4 :dim)   ; b flat diminished -> (70 73 76)"
  ([root chord-name]
   (chord root chord-name 0))
  ([root chord-name inversion]
   (let [root (note root)
         chord (sort (resolve-chord chord-name))
         notes (map #(+ % root) chord)]
     (invert-chord notes inversion))))

(defn rand-chord
  "Generates a random list of MIDI notes with cardinality num-pitches
  bound within the range of the specified root and pitch-range and
  only containing pitches within the specified chord-name. Similar to
  Impromptu's pc:make-chord"
  ([] (rand-chord MIDDLE-C))
  ([root] (rand-chord root :major))
  ([root chord-name] (rand-chord root chord-name 4))
  ([root chord-name num-pitches]
   (let [root (note root)]
     (if (<= MIDI-LOWEST-NOTE (- root 12) (+ root 12) MIDI-HIGHEST-NOTE)
       (rand-chord (- root 12) :major num-pitches (+ root 12))
       (if (<= MIDI-LOWEST-NOTE (- root 12))
         (rand-chord (- root 12) :major num-pitches (+ root 12))
         (rand-chord root :major num-pitches (+ root 24))))))
  ([root chord-name num-pitches pitch-range]
   (rand-chord root chord-name num-pitches pitch-range 0))
  ([root chord-name num-pitches pitch-range inversion]
   (let [chord (chord root chord-name inversion)
         root (note root)
         max-pitch (validate-midi-note-number! (+ pitch-range root))
         roots (range 0 max-pitch 12)
         notes (flatten (map (fn [root] (map #(+ root %) chord)) roots))
         notes (take-while #(<= % max-pitch) notes)]
     (map validate-midi-note-number!
          (sort (choose-n num-pitches notes))))))

; midicps
(defn midi->hz
  "Convert a midi note number to a frequency in hz."
  [note]
  (let [note (if (int? note)
               note ;; SC doesn't check bounds
               (p/note note))]
    (* 440.0 (Math/pow 2.0 (/ (- note 69.0) 12.0)))))

; cpsmidi
(defn hz->midi
  "Convert from a frequency to the nearest midi note number."
  [freq]
  ;; SC doesn't check bounds
  (Math/round
    (+ 69
       (* 12
          (/ (Math/log (* freq 0.0022727272727))
             (Math/log 2))))))

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
  equal-tempered tuning i.e. dividing all 12 semitones equally across
  an octave. This is currently the standard tuning."
  [base-freq interval]
  (* base-freq (java.lang.Math/pow 2 (/ interval 12))))

(defn interval-freq
  "Returns the frequency of the given interval using the specified
  scale and tuning (defaulting to ionian and equal-tempered
  respectively)."
  ([base-freq n] (interval-freq base-freq n :ionian :equal-tempered))
  ([base-freq n scale tuning]
   (case tuning
     :equal-tempered (nth-equal-tempered-freq base-freq
                                              ;;TODO unit test, these args were backwards
                                              (nth-interval scale n)))))

(defn find-scale-name
  "Return the name of the first matching scale found in SCALE
  or nil if not found.

  (find-scale-name [2 1 2 2 2 2 1])
  ;=> :melodic-minor-asc OR :melodic-minor"
  [scale]
  (reverse-get SCALE scale))

(defn find-pitch-class-name
  "Given a midi number representing a note, returns the canonical name of the note
  independent of octave.

  (find-pitch-class-name 62) ;=> :D
  (find-pitch-class-name 74) ;=> :D
  (find-pitch-class-name 75) ;=> :Eb"
  [note]
  (REVERSE-NOTES (mod note 12)))

(defn find-note-name
  "Given a midi number representing a note, returns a keyword
  representing the canonical name of the note including octave number.
  Reverse of the fn note. Returns note if nil.

  (find-note-name 45) ;=> :A2
  (find-note-name 57) ;=> :A3
  (find-note-name 58) ;=> :Bb3"
  [note]
  (when (some? note)
    (validate-midi-note-number! note)
    (let [octave (dec (int (/ note 12)))]
      (keyword (str (name (find-pitch-class-name note)) octave)))))

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

(defn resolve-chord-notes
  "Coerces notes to midi note numbers. Notes default to being higher than
  its immediate predecessor, or octave 4 if none.
  
  (resolve-chord-notes [:C :E :G]) => [60 64 67]
  (resolve-chord-notes [:C :E :G :B :D :F]) => [60 64 67 71 74 77]
  (resolve-chord-notes [:C :E3 :G :B 60 :D :F]) => [60 64 67 71 74 77]"
  [notes]
  (if (every? integer? notes)
    notes
    (let [;; default to octave 4
          root (note (first notes))]
      (reduce (fn [out notes]
                (let [{:keys [midi-note interval]} (note-info notes)]
                  (or (some->> midi-note (conj out))
                      ;; above previous
                      (let [prev (peek out)
                            pinfo (note-info prev)
                            above? (< (:interval pinfo) interval)
                            ;; can fail, might want custom errors
                            midi-note (if above?
                                        (octave-note (:octave pinfo) interval)
                                        (octave-note (inc (:octave pinfo)) interval))]
                        (conj out midi-note)))))
              [root] (next notes)))))

(defn find-chord
  "Find the chord for a given set or sequence of notes.
  First note will default to octave 4 and subsequent notes
  default to be higher than previous ones for sequential notes.
  For sets, all notes default to octave 4.

  Usage examples:

  (find-chord [60 64 67]) ;=> {:root :C, :chord-type :M}"
  [notes]
  (let [notes (if (sequential? notes)
                (resolve-chord-notes notes)
                (into #{} (map note) notes))]
    (loop [note 0]
      (when (< note (count notes))
        (let [mod-notes (select-root notes note)
              chord (find-chord-with-low-root mod-notes)
              root (find-pitch-class-name (first (sort mod-notes)))]
          (if chord
            {:root root :chord-type chord}
            (recur (inc note))))))))

(defn chord-degree
  "Returns the notes constructed by picking thirds in a given scale
  from in a given root. Useful if you want to try out playing standard
  chord progressions. For example:

  (chord-degree :i :c4 :ionian) ;=> (60 64 67 71)
  (chord-degree :ii :c4 :melodic-minor-asc) ;=> (62 65 69 72)
  "
  ([degree root mode]
   (chord-degree degree root mode 4))
  ([degree root mode num-notes]
   (let [d-int (degree->int degree)
         num-degrees (- (+ d-int (* num-notes 2)) 1)]
     (take-nth 2 (drop (degree->int degree) (scale root mode (range num-degrees)))))))

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

(ns
  ^{:doc "Functions that define tuning systems from various musical traditions and theories."
     :author "Jeff Rose"}
  overtone.music.tuning
  (:use [overtone.music.pitch]
        [clojure.math.numeric-tower]))

;; TODO: Not only should we pre-compute the frequency values for standard tunings,
;; but it would be cool to let people explore different tunings while creating
;; new synth instruments.  So, if they have a base frequency chosen we should be
;; able to generate tunings, scales, and arpeggios to let them hear their
;; instrument in "western mode", "arabic mode", "indian mode", "chinese mode", etc.


;; Diatonic Just --> Indian music


;; Equal Temperament
;;
;;   The octave is divided into a fixed number of notes, where the ratio of
;; one note to the next is constant.  Typically the A at 440hz is used as the
;; stationary point around which all other notes can be calculated.

;; 12-tone equal temperament --> Western music
;; ratio = (Math/pow 2 (/ 1 12)) => 1.0594630943592953
;; (perform '((:edo 12 100 440) 73 76 79))
;; (perform '(:midi 60 69 81))

;; 24-tone equal temperament --> Arabic music
;; ratio = (Math/pow 2 (/ 1 24)) => 1.029302236643492
;; (perform '((:arabic 100 440) 73 76 79))



;; Helper Functions
(defn list-flatten-first [x]
    (first (flatten (list x))))

(defn get-or-fn [map key func]
    (if-let [result (map key)]
        result
        (func key)))

(defn map-or-fn [m keys func]
    (map #(get-or-fn m % func) keys))

(defn perfmap [note initial freq notemap]
    (let [pos (mod (- note initial) (count notemap))
          octave (quot (- note initial (- (count notemap) 1 )) (count notemap))]
        (* freq (nth notemap pos) (expt (ceil (reduce max notemap)) octave))))

(defn collapse-to-ntave [number ntave]
    (condp > number
             1 (recur (* number ntave) ntave)
             ntave number
             (recur (/ number ntave) ntave)))

(defn note-set-from-generator [generator initpower finpower ntave]
    (let [tempset (for [exponent (range initpower finpower)] (expt generator exponent))]
        (sort (map #(collapse-to-ntave % ntave) tempset))))


;; Public Face - the tuning multimethods.

(defmulti perfn
    "Multimethod that returns a function taking note numbers to frequencies in \\s^{-1}\\"
    list-flatten-first)

(defmulti tunednotes
    "Multimethod that returns a function taking note keywords to note numbers.

    A note keyword is usually of the form :[notename][pitchmodifiers][ntave], for example, :A4, :cb6 or :Bbb0, however this is entirely dependent on the kind of scale.

    TunedNotes functions should ideally return nonmatching symbols or numbers unmodified. Particularly numbers, which could be note numbers."
    list-flatten-first)

(defmulti reversenotes
    "Multimethod that returns a function taking note numbers to note symbols."
    list-flatten-first)

(defn perform [[opts & notes]]
    "Takes a set of options and a list of note numbers/keywords, and runs the notes through the appropriate tuning system to give frequencies."
    (map (perfn opts) (map-or-fn (tunednotes opts) notes identity)))

(defn canonical-note-names [argz]
    "Returns a function that takes note names to their canonical selves within the relevant tuning system."
    (fn [note]
        ((reversenotes argz) ((tunednotes argz) note))))


;;Implementation of some tuning systems.

(defmethod perfn :ed [[_ divisions multiplier initial freq]]
    "Divides a multiplier-tave (e.g. 2-tave = octave, 3-tave, &c.) into divisions divisions. Maps initial to a note with frequency freq."
    (fn [note]
        (* freq (Math/pow multiplier (/ (- note initial) divisions)))))

(defmethod perfn :edo [[_ divisions initial freq]]
    "Equal divisions of the octave."
    (perfn (list :ed divisions 2 initial freq)))

(defmethod perfn :midi [_]
    (perfn (list :edo 12 69 440)))

(defmethod tunednotes :midi [_]
    "Returns a function that resolves notes to MIDI number format. Resolves upper and lower-case keywords and strings in MIDI note format. If given a string or keyword in a different format, throws an exception. Anything else is returned unmodified.

     Usage examples:
     (note :F#7)    ;=> 102
     (note :db5)    ;=> 73"
    (fn [n]
        (if (or (keyword? n) (string? n))
            (let [match (re-find (re-pattern "([a-gA-G])([#bB]*)([-0-9]+)") (name n))
                  _ (when (nil? match)
                        (throw (IllegalArgumentException.
                            (str "Unable to resolve note: " n ". Does not appear to be in MIDI format i.e. C#4"))))
                  [_ pitchclass modifier octavestr] match
                  octave (Integer. octavestr)
                  _ (when (< octave -1)
                        (throw (IllegalArgumentException.
                            (str "Unable to resolve note: " n ". Octave is out of range. Lowest octave value for midi is -1"))))]
                (+ (get {"c"  0, "d"  2, "e"  4, "f"  5, "g"  7, "a"  9, "b"  11} (clojure.string/lower-case pitchclass))
                   (- (count (filter #(= \b %) modifier)))
                   (count (filter #(= \# %) modifier))
                   (* 12 octave)
                   12))
            n)))

(defmethod reversenotes :midi [_]
    (fn [note]
        (let [degree (rem note 12)
              octave (- (floor (/ note 12)) 1)
              _ (when (< octave -1)
                    (throw (IllegalArgumentException.
                           (str "Unable to resolve note: " note ". Octave is out of range. Lowest octave value for midi is -1"))))]
            (keyword (str (nth '("c" "c#" "d" "d#" "e" "f" "f#" "g" "g#" "a" "a#" "b") degree) octave)))))

(defmethod perfn :default [_]
    (perfn :midi))

(defmethod tunednotes :default [_]
    (tunednotes :midi))

(defn parsemodifiers
    "Parses standard modifiers b, #, es, eh, ih and is for note names. Returns shifts where 1 is equivalent to a semitone."
    [modifiers]
    (let [matches (map first (concat (re-seq #"([ie][sh])" modifiers) (re-seq #"([b#])" modifiers)))]
        (+
            (- (count (filter #(= "b" %) matches)))
            (- (count (filter #(= "es" %) matches)))
            (* -0.5 (count (filter #(= "eh" %) matches)))
            (* 0.5 (count (filter #(= "ih" %) matches)))
            (count (filter #(= "is" %) matches))
            (count (filter #(= "#" %) matches)))))


;; Some other tuning systems

(defmethod perfn :arabic [[symb initial freq]]
    (perfn (list :edo 24 initial freq)))

(defmethod perfn :qcmeantone [[symb initial freq]]
    (fn [note]
        (perfmap note initial freq (note-set-from-generator (expt 5 1/4) -5 7 2))))




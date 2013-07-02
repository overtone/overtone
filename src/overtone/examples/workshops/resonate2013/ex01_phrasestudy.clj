(ns
  ^{:doc "Workshop exercises in musical pattern creation with Overtone."
    :author "Karsten Schmidt"}
  overtone.examples.workshops.resonate2013.ex01_phrasestudy
  (:use
   [overtone.live]
   [overtone.inst.piano]))

;; All examples are wrapped in (comment) to stop them from
;; executing all at once when the file is loaded...
;; To execute an example in the REPL, select a single form,
;; e.g. "(piano 60)" below and press Command+Enter
;; Selection of a whole form (in Eclipse) can be done easily by clicking in
;; front of the opening bracket and then pressing Command+Shift+right...
(comment
  (piano 60)
  (piano (note :c4)) ; 60
  (piano (note :d4)) ; 62
  (piano (note :g4)) ; 67
  (piano (note :c5)) ; 72

  ;; play a note 2 secs in the future
  ;; the `now` fn returns the current timestamp
  (at (+ (now) 2000) (piano 72))

  ;; play progression C4 D4 G4 C5
  ;; notes will be played every 200 milliseconds
  ;; the fn passed to `map-indexed` takes two arguments: index & note
  (dorun (map-indexed (fn [i n] (at (+ (now) (* i 200)) (piano n))) [60 62 67 72]))
  ;; same, but using fn reader macro w/ anonymous arguments (% = 1st argument, %2 = 2nd arg, etc.)
  (dorun (map-indexed #(at (+ (now) (* % 200)) (piano %2)) [60 62 67 72]))
  ;; play progression relative to C4 (MIDI note offset = 60)
  (dorun (map-indexed #(at (+ (now) (* % 200)) (piano (+ 60 %2))) [0 2 7 12 24 19 14 12]))
  ;; play one octave higher (60 + 12 = 72)
  (dorun (map-indexed #(at (+ (now) (* % 200)) (piano (+ 72 %2))) [0 2 7 12]))
  ;; play minor variation (7 -> 6)
  (dorun (map-indexed #(at (+ (now) (* % 200)) (piano (+ 72 %2))) [0 2 6 12]))
  )

;; the examples in this namespace are all about thinking of music as sequences
;; and getting to know essential Clojure functions which allow us to construct
;; and manipulate sequences efficiently

;; first off, we define some arpeggio patterns used as
;; basic musical building blocks
(def a [0 2 7 12]) ; major
(def b [0 2 6 12]) ; diminished
(def c [0 2 7 12 24 19 14 12]) ; up & down

(comment
  ;; play a phrase of concatenated patterns: 2x A, 2x B
  (dorun (map-indexed #(at (+ (now) (* % 200)) (piano (+ 72 %2))) (concat a a b b)))

  ;; repeat the constructed phrase twice
  ;; (flatten) removes any nesting from a given sequence
  (dorun (map-indexed #(at (+ (now) (* % 200)) (piano (+ 72 %2))) (flatten (repeat 2 (concat a a b b)))))

  ;; the `->>` is a so called threading-macro, which weaves the result of each form
  ;; as last argument into the next form and so allows us to think about the code as
  ;; a data transformation pipeline (it produces the same effect as the previous example):
  ;; 1) we use the vector [a a b b]
  ;;    => [[0 2 7 12] [0 2 7 12] [0 2 6 12] [0 2 6 12]]
  ;; 2) weave it as last argument into (repeat 2)
  ;;    => (repeat 2 [[0 2 7 12] [0 2 7 12] [0 2 6 12] [0 2 6 12]]),
  ;;    which produces this sequence:
  ;;    ([[0 2 7 12] [0 2 7 12] [0 2 6 12] [0 2 6 12]]
  ;;     [[0 2 7 12] [0 2 7 12] [0 2 6 12] [0 2 6 12]])
  ;; 3) take the result of step 2 and weave it as last arg into (flatten)
  ;;    => (flatten ([[0 2 7 12] [0 2 7 12] [0 2 6 12] [0 2 6 12]]
  ;;                 [[0 2 7 12] [0 2 7 12] [0 2 6 12] [0 2 6 12]]))
  ;;    which produces (0 2 7 12 0 2 7 12 0 2 6 12 0 2 6 12
  ;;                    0 2 7 12 0 2 7 12 0 2 6 12 0 2 6 12) (no more nesting)
  ;;
  ;; This final flat collection is then used as input for the `map-indexed` fn for playback
  (dorun (map-indexed #(at (+ (now) (* % 200)) (piano (+ 72 %2))) (->> [a a b b] (repeat 2) (flatten))))
  )

;; define counter running at 120 bpm
;; this counter keeps running automatically in the background
(def metro (metronome 120))

(comment
  ;; play phrase using metronome
  (dorun (map-indexed #(at (metro (+ (metro) (/ % 2))) (piano (+ 72 %2))) (->> [a a b b] (repeat 2) (flatten))))
  )

;; now let's start separating concerns: note playback vs. phrase playback

(defn play-note-at
  "Plays note with `inst` at relative beat `index` from `start`,
  based on tempo of `metronome` and given note `duration`."
  [metro start index dur inst note]
  (at (metro (+ start (* 4 index dur))) (inst note)))

;; This next function uses a few new concepts:
;; 1) It makes use of partial function application to hardcode a number of parameters.
;;    In this case we use `partial` to produce a version of the play-note-at fn,
;;    which has the first 2 arguments (the metronome itself and the metronome's current beat count) hardcoded.
;;    This pre-configured play fn is then used to play the notes
;; 2) It uses so called let-bindings, local symbol definitions which are only existing within
;;    the scope of let's body (in this case, the constructed `play` fn)
;; 3) The play-phrases-1 fn uses variable argument counts, allowing us to specify any number of phrases
;;    to be played sequentially. This is achieved with the `&` separator in the function's argument list
;;    The given phrases to be played are all collected into a single sequence automatically
(defn play-phrases-1
  "Plays the given phrases `n` times with `inst` and transposed by `offset`.
  Tempo is based on metronome and given note `duration`."
  [metro inst dur offset n & phrases]
  (let [play (partial play-note-at metro (metro))]
    (->> phrases
         (repeat n)
         (flatten)
         (map-indexed #(play % dur inst (+ (note offset) %2)))
         (dorun))))

(comment
  ;; play the same combined pattern as earlier, but also allows us to
  ;; specify note durations (1/16th), root note (:c4) and number of repetitions (2)
  (play-phrases-1 metro piano 1/16 :c4 2 a a b b))

;; continue separating concerns: pattern repetition vs. pattern playback...

;; So far, play-phrases still is doing more than one thing and we can
;; refactor it further by splitting out and increasing the flexibility of
;; the pattern/phrase generation. More flexibility can be obtained by
;; allowing us to specify the root note as absolute value, e.g. :c4 or
;; use a number which is then applied as relative offset to transpose patterns
;; This is what the `if` does below...
(defn repeat-phrases
  "Returns a lazyseq of the given patterns relative to `offset` (a note or int) and repeated `n` times."
  [n offset & phrases]
  (->> phrases
       (repeat n)
       (flatten)
       (map (partial + (if (keyword? offset) (note offset) offset)))))

;; To ease the combination of shorter phrases into longer ones,
;; we could also define a custom data format and use this helper function:
(defn specs->phrases
  "Takes a number of specs in the form of `[count root-note phrases]` and calls
  repeat-phrases on each. Concatenates all into a single flat seq."
  [& phrases]
  (mapcat #(apply repeat-phrases %) phrases))

;; This then allows us to constuct a longer pattern like this:
;; 2x AABB at C4, 1x AABB at G4 and 2x pattern C at D4
(def pattern
  "Defines a more complex pattern using different keys
  and a longer pattern for more variation"
  (specs->phrases [2 :c4 a a b b] [1 :g4 a a b b] [2 :d4 c]))

(comment
  ;; Since all our patterns are made of sequences of 4 notes, we can use the
  ;; (partition) fn to re-create that grouping (e.g. for debug purposes)
  ;; So the partitioned pattern looks again like that:
  ;; => ((60 62 67 72) (60 62 67 72) (60 62 66 72) ...)
  (partition 4 pattern)

  ;; This can be of course applied recursively to create ever more
  ;; deeply nested sequences
  (partition 4 (partition 4 pattern))
  )

;; With these new changes done, we can limit the responsibility of the play-phrases fn
;; to simply play notes... Here's version 2:
(defn play-phrases-2
  [metro inst dur phrases]
  (let [play (partial play-note-at metro (metro))]
    (dorun (map-indexed #(play % dur inst %2) phrases))))

(comment
  (play-phrases-2 metro piano 1/16 pattern)
  (play-phrases-2 metro piano 1/16 (repeat-phrases 2 0 pattern))
  (play-phrases-2 metro piano 1/16 (repeat-phrases 2 0 pattern (reverse pattern))))

;; Let's create some more pattern variations
(def mirror-pattern
  "The original pattern inverted in time."
  (reverse pattern))

(def long-pattern
  "The original pattern followed by mirror-pattern."
  (concat pattern mirror-pattern))

(comment
  ;; Here we also make use of the relative transposing feature of `specs->phrases` creating
  ;; a new long phrase which goes down to G3 (-5) and D3 (-10) towards the end
  (play-phrases-2 metro piano 1/16 (specs->phrases [1 0 long-pattern] [1 -5 pattern] [1 -10 (reverse pattern)])))

;; And to show even more possibilities, an even longer progression
;; Here we also use `take` & `drop` to only use parts of existing phrases
(def progression
  (->> [[1 0 long-pattern]                         ; c4
        [1 -5 pattern]                             ; g3
        [1 -8 (take 32 mirror-pattern)]            ; e3
        [1 -10 (take 16 mirror-pattern)]           ; d3
        [1 -5 (take 16 (drop 16 mirror-pattern))]] ; g3
       (apply specs->phrases)
       (apply repeat-phrases 2 0)))

(comment
  (play-phrases-2 metro piano 1/16 progression))

;; Until now all notes have been played with the same duration, but it's now time
;; to start thinking about imposing a certain rhythm onto our phrase(s)

;; Example rhythm using syncopation to emphasize every 4th note (longer)
(def rhythm [3/8 5/24 5/24 5/24])

;; The note durations of the rhythm all add up to 1 bar...
(comment (apply + rhythm)) ; => 1

;; Since our note phrases are just sequences of numbers, we can apply the same
;; functions we've built on our rhythmic phrases too. Below we construct a more
;; complex rhythmic pattern (using 0 as an offset to keep the original values)
;; The generated rhythm is: (3/8 1/8 3/8 1/8 1/8 1/8 1/8 1/8 1/8 1/8 1/8 1/8)
(def alt-rhythm (specs->phrases [2 0 [3/8 1/8]] [8 0 [1/8]]))

;; The the total duration of this rhythm is 2 bars
(comment (apply + alt-rhythm)) ; => 2

;; The next function is used to compute start times for all notes in a phrase
;; based on a given rhythmic pattern...
;; Notable things:
;; 1) We use the ->> threading syntax again to describe the transformation of
;;    how the rhythm is applied to our melody
;; 2) The (cycle) fn produces an infinite repetition of the given sequence:
;;    E.g. (cycle [1 2]) => (1 2 1 2 1 2 ...)
;; 3) Since we are only interested in a finite amount of notes, we use (take)
;;    to only take the first x items from that infinite sequence (without it
;;    the machine would freeze :)
;; 4) We also use (reductions) again to build up a sequence of increasing timestamps.
;;    The last argument (0) is the time offset for the first note
;;    This timing sequence is then combined with the notes in the (map) fn
(defn rhythmic-phrase
  "Takes a rhythm sequence, a speed factor and phrase, returns a lazyseq
  of vector pairs [note time] with the time values being the start times of each
  note in the phrase based on the rhythm. The start times are in bar measures."
  [rhythm factor phrase]
  (->> rhythm
       (cycle)
       (take (count phrase))
       (reductions (fn[t d] (+ t (* d factor 4))) 0)
       (map (fn [n t] [n t]) phrase)))

;; Now that the musical phrases include timing, we also need to
;; create a new playback fn which uses this timing information...
(defn play-rhythmic-phrase-1
  [metro inst rhythm factor phrase]
  (let [t0 (metro)
        play (fn [[n t]] (at (metro (+ t0 t)) (inst n)))]
    (dorun (map play (rhythmic-phrase rhythm factor phrase)))))

(comment
  ;; Let's testdrive the rhythms...
  (play-rhythmic-phrase-1 metro piano rhythm 1/2 progression)
  (play-rhythmic-phrase-1 metro piano alt-rhythm 1 progression)
  )

;; Maybe it's also a good time to introduce a couple of custom synths
;; Both instruments below are almost identical and only differ in their
;; oscillator waveform used...

(definst fatso-saw
  "Defines a simple synth using a slightly detuned stereo sawtooth oscillator
  with a percussive envelope. Accepts a MIDI note, duration (in secs) and volume."
  [note 60 dur 1.0 amp 1.0]
  (let [freq (midicps note)
        src (saw [freq (* freq 0.51)])
        env (env-gen (perc (* 0.1 dur) dur amp) :action FREE)]
    (* src env)))

(definst fatso-pwm
  "Defines a simple synth using a slightly detuned stereo squarewave oscillator
  with a percussive envelope. Accepts a MIDI note, duration (in secs) and volume."
  [note 60 dur 1.0 amp 1.0]
  (let [freq (midicps note)
        src (pulse [freq (* freq 0.51)])
        env (env-gen (perc (* 0.1 dur) dur amp) :action FREE)]
    (* src env)))

(comment
  ;; Testing 1,2,3...
  (fatso-saw)
  (fatso-pwm)
  ;; Testing the synth with different pitches & durations
  (fatso-saw 60 0.25)
  (fatso-saw 55 0.5)
  (fatso-saw 52 1.0)
  (fatso-saw 50 2.0)
  (fatso-saw 48 4.0)
  )

;; Let's build a preset for fatso with a very short duration (0.15 secs)...
(def knightrider #(fatso-saw % 0.15))

(comment
  ;; ...and play the whole thing pitched down by 1 octave (-12 semitones)
  (play-rhythmic-phrase-1 metro knightrider alt-rhythm 1/2 (map #(- % 12) progression))
  )

;; Now let's do some more rhythm related refactoring...
;; We tried to make it easy for ourselves and have play-rhythmic-phrase-1
;; applying the given rhythm to our melody during playback...
;;
;; However, now we'd like to do something more interesting and build a
;; simple arpeggiator, which plays each note twice (at double speed & different pitches)
;; and therefore also needs access to our rhythm (and manipulates it).
(defn arpeggiate
  "Takes a rhythm pattern and note sequence, applies the rhythm as template,
  but plays each note twice at double speed (once the original, followed by
  original one octave higher). Returns lazyseq of vector pairs [note duration]."
  [rhythm melody]
  (mapcat
   (fn [note dur] [[note (/ dur 2)] [(+ note 12) (/ dur 2)]])
   melody (cycle rhythm)))

;; Now (arpeggiate alt-rhythm pattern) produces this sequence of note/duration pairs:
;; => ([60 3/16] [72 3/16] [62 1/16] [74 1/16] [67 3/16] [79 3/16] ...)
;; Our play-rhythmic-phrase-1 function can't handle this data, so we need to rewrite it.
;; And we should also use this as an opportunity to create another fn with its sole
;; role of computing timestamps (in bar measure) for each of the notes in such a sequence
(defn rhythm-timings
  "Take a sequence of [note duration] pairs and computes timestamp (in bar measure)
  for each note. Returns another sequence [note time]."
  [factor phrase]
  (let [notes (map first phrase)
        durations (map second phrase)
        timings (reductions (fn[t d] (+ t (* d factor 4))) 0 durations)]
    (map (fn [n t] [n t]) notes timings)))

;; Finally, we remove any rhythm application from our playback fn and
;; have it deal only with scheduling of notes based on the rhythm
;; already present in the phrase.
(defn play-rhythmic-phrase-2
  [metro inst rhythm-phrase]
  (let [t0 (metro)
        play (fn [[n t]] (at (metro (+ t0 t)) (inst n)))]
    (dorun (map play rhythm-phrase))))

;; All together now:
;; Take the whole long phrase, pitch it -12, then arpeggiate with rhythm at double speed...
(def rhythmic-progression
  (->> progression
    (map #(- % 12))
    (arpeggiate rhythm)
    (rhythm-timings 1/2)))

(comment
  ;; Play it again, Sam (Aaron)! :)
  (play-rhythmic-phrase-2 metro knightrider rhythmic-progression)
  )

(defn inc-duration
  "Note timestamp transformation function to slowly increase note
  duration the further into the sequence a note is. Enforces min/max
  durations: 0.15 - 3.5 secs"
  [t]
  (min (max (* t 0.01) 0.15) 3.5))

(defn play-rhythmic-phrase-3
  "Like play-rhythmic-phrase-2 fn, but manipulates note durations by passing their
  timestamp through given transform fn."
  [metro inst transform rhythm-phrase]
  (let [t0 (metro)
        play (fn [[n t]] (at (metro (+ t0 t)) (inst n (transform t))))]
    (dorun (map play rhythm-phrase))))

(comment
  ;; https://soundcloud.com/toxi/res13-seq-2
  (play-rhythmic-phrase-3 metro fatso-saw inc-duration rhythmic-progression)
  ;; https://soundcloud.com/toxi/res13-seq1
  (play-rhythmic-phrase-3 metro fatso-pwm inc-duration rhythmic-progression)
  )

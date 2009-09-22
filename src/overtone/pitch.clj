(ns overtone.pitch)

;; The goal here is to build up a set of functions that help to describe, model, 
;; generate and analyze musical ideas.

;; * Of all possible 7 note scales, the major scale has the highest number
;; of consonant intervals

;; * Diatonic function
;;   - in terms of centeredness around the root, this is the order
;; of chord degrees: I, V, IV, vi, iii, ii, vii (The first 3 chords 
;; being major, second 3 minor, and last diminished)
    
;; * Each note in a scale acts as either a generator or a collector of other notes, 
;; depending on their relations in time within a sequence.

;; MIDI
(def midi-range (range 128))
(def middle-C 60)

(def octave 12)
(def fifth  7)
(def third  4)

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
     :minor7 minor7
     :m7     minor7
     :augmented aug
     :a         aug
     :diminished dim
     :i          dim}))

;(defn chord [key chord ]
;  (map #(+ %1 base) (:major CHORD)))

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
    [base intervals]
    ))

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

(defn from-scale [lower upper ])

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

(defn choose 
  "Choose a random note from notes."
  [notes]
  (get notes (rand-int (count notes))))

(defn chosen-from [notes]
  (let [num-notes (count notes)]
    (repeatedly #(get notes (rand-int num-notes)))))

;(def notes (octaves (range 2 6) (scale :C :major)))
;(play notes rhythm groove)

;; TODO: Some functions to help with building synths and using multiple oscillators that you want separated by musical intervals in frequency.  For example, given a base freq get back any interval up or down.
  
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; This comes from an Impromptu library, pc-lib.scm.  It has lots of interesting
;; ideas, but I don't think about music in the same way so while I was initially
;; thinking of just translating this to clojure I think instead it will just stick 
;; around here for a bit to teach and inspire...
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; aa-cell pitch class library
;; A collection of functions for working with pitch class sets

;; A pitch class in this library is taken to be a
;; list of MIDI note values from the first octave (0-11)
;; from which other pitches are compared using modulo 12.
;; Therefore, 0 = C, 1 = C#, etc..

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
;; Define basic diatonic major
;(define *pc:diatonic-major*
;   '((i . (0 . ^))
;     (i6 . (0 . ^6))
;     (i64 . (0 . ^64))
;     (i7 . (0 . ^7))
;     (i- . (0 . -))
;     (i-7 . (0 . -7))                 
;     (n . (1 . ^)) ; neopolitan
;     (n6 . (1 . ^6)) ; neopolitan
;     (ii . (2 . -))
;     (ii6 . (2 . -6))
;     (ii7 . (2 . -7))                     
;     (ii9 . (2 . -9))                 
;     (ii^ . (2 . ^))
;     (ii^7 . (2 . ^7))                                      
;     (iii . (4 . -))
;     (iii6 . (4 . -6))     
;     (iii7 . (4 . -7))
;     (iii^ . (4 . ^))
;     (iii^7 . (4 . ^7))                 
;     (iv . (5 . ^))
;     (iv6 . (5 . ^6))
;     (iv7 . (5 . ^7))
;     (iv- . (5 . -))
;     (iv-7 . (5 . -7))                 
;     (v . (7 . ^))
;     (v6 . (7 . ^6))
;     (v7 . (7 . 7))
;     (v- . (7 . -))
;     (v-7 . (7 . -7))                 
;     (vi . (9 . -))
;     (vi6 . (9 . -6))
;     (vi7 . (9 . -7))
;     (vi^ . (9 . ^))
;     (vi^7 . (9 . ^7))                 
;     (viio . (11 . o))
;     (viio7 . (11 . o7))
;     (vii . (11 . o))
;     (vii7 . (11 . -7b5))
;     ))
;
;; Define basic diatonic minor
;(define *pc:diatonic-minor*
;   '((i . (0 . -))
;     (i6 . (0 . -6))
;     (i64 . (0 . -64))
;     (i7 . (0 . -7))
;     (i^ . (0 . ^))
;     (i^6 . (0 . ^6))     
;     (i^64 . (0 . ^64))          
;     (i^7 . (0 . ^7))
;     (n . (1 . ^)) ; neopolitan
;     (n6 . (1 . ^6)) ; neopolitan     
;     (ii . (2 . o))
;     (ii6 . (2 . o6))     
;     (ii7 . (2 . o7))                     
;     (ii- . (2 . -))
;     (ii-6 . (2 . -6))     
;     (ii-7 . (2 . -7))                                      
;     (ii^ . (2 . ^))
;     (ii^7 . (2 . ^7))                                                       
;     (iii . (3 . ^))
;     (iii6 . (3 . ^6))     
;     (iii7 . (3 . ^7))
;     (iii- . (3 . -))
;     (iii-6 . (3 . -6))     
;     (iii-7 . (3 . -7))
;     (iv . (5 . -))
;     (iv6 . (5 . -6))     
;     (iv7 . (5 . -7))
;     (iv^ . (5 . ^))
;     (iv^6 . (5 . ^6))     
;     (iv^7 . (5 . ^7))                 
;     (v . (7 . ^))
;     (v^ . (7 . ^))     
;     (v6 . (7 . ^6))     
;     (v7 . (7 . 7))                 
;     (v- . (7 . -))
;     (v-6 . (7 . -6))     
;     (v-6 . (7 . -6))     
;     (v-7 . (7 . -))
;     (vi . (8 . ^))
;     (vi6 . (8 . ^6))     
;     (vi7 . (8 . ^7))
;     (vi- . (8 . -))
;     (vi-6 . (8 . -6))     
;     (vi-7 . (8 . -7))                 
;     (vii . (10 . ^))
;     (vii6 . (10 . ^6))     
;     (vii7 . (10 . ^7))
;     (viio . (11 . o)) ;raised 7 (dim)
;     (viio6 . (11 . o6)) ;raised 7 (dim)     
;     (viio7 . (11 . o7)) ; raised 7 (dim)
;     ))
;
;;; various scales defined as pc sets
;(define *pc:scales*
;   '((pentatonic . (2 2 3 2))
;     (wholetone . (2 2 2 2 2))
;     (chromatic . (1 1 1 1 1 1 1 1 1 1 1))
;     (octatonic . (2 1 2 1 2 1 2))                      
;     (messiaen1 . (2 2 2 2 2))                                            
;     (messiaen2 . (2 1 2 1 2 1 2))                                            
;     (messiaen3 . (2 1 1 2 1 1 2 1))
;     (messiaen4 . (1 1 3 1 1 1 3))
;     (messiaen5 . (1 4 1 1 4))
;     (messiaen6 . (2 2 1 1 2 2 1))
;     (messiaen7 . (1 1 1 2 1 1 1 1 2))
;     (ionian . (2 2 1 2 2 2))
;     (dorian . (2 1 2 2 2 1))
;     (phrygian . (1 2 2 2 1 2))
;     (lydian . (2 2 2 1 2 2))
;     (lydian-mixolydian . (2 1 2 1 2 1 2))
;     (mixolydian . (2 2 1 2 2 1))
;     (aeolian . (2 1 2 2 1 2))
;     (locrian . (1 2 2 1 2 2))))
;
;; Define basic chord symbols
;(define *pc:chord-syms*
;   '((^ . (0 4 7))
;     (^sus . (0 5 7))
;     (^6 . (4 7 0))
;     (^64 . (7 0 4))
;     (^7 . (0 4 7 11))          
;     (^65 . (4 7 11 0))
;     (^43 . (7 11 0 4))
;     (^42 . (11 0 4 7))
;     (^2 . (11 0 4 7))     
;     (^7#4 . (0 4 7 11 6))     
;     (^9 . (0 4 7 11 2))
;     (7 . (0 4 7 10)) 
;     (65 . (4 7 10 0))
;     (43 . (7 10 0 4))
;     (2 . (10 0 4 7))
;     (42 . (10 0 4 7))     
;     (- . (0 3 7))
;     (-sus . (0 5 7))
;     (-6 . (3 7 0))
;     (-64 . (7 0 3))     
;     (-7 . (0 3 7 10))
;     (-65 . (3 7 10 0))
;     (-43 . (7 10 0 3))
;     (-42 . (10 0 3 7))
;     (-2 . (10 0 3 7))                
;     (-9 . (0 3 7 10 2))                           
;     (o . (0 3 6))
;     (o6 . (3 6 0))
;     (o64 . (6 0 3))
;     (o7 . (0 3 6 8))
;     (o65 . (3 6 8 0))
;     (o43 . (6 8 0 3))
;     (o42 . (8 0 3 6))
;     (o2 . (8 0 3 6))
;     (-7b5 . (0 3 6 9))))
;
;;; returns a scale based on a chord (standard jazz translations)
;(define *pc:chord->scale*
;   '((i . (0 . ionian))
;     (i7 . (0 . ionian))
;     (ii . (2 . dorian))
;     (ii7 . (2 . dorian))
;     (ii9 . (2 . dorian))
;     (iii . (4 . dorian))
;     (iii7 . (4 . dorian))
;     (iv . (5 . lydian))
;     (iv7 . (5 . lydian))
;     (v . (7 . mixolydian))
;     (v7 . (7 . mixolydian))
;     (vi . (9 . dorian))
;     (vi7 . (9 . dorian))
;     (vii . (11 . locrian))
;     (vii7 . (11 . locrian))))
;
;;; A predicate for calculating if pitch is in pc
;;;
;;; arg 1: pitch to check against pc
;;; arg 2: pc to check pitch against
;;; 
;;; retuns true or false
;;;
;(defn pc:? [pitch pc]
;      (if (list? (member (fmod pitch 12) pc))
;          #t
;          #f))) 
;
;;; quantize pc
;;; Always selects a higher value before a lower value where distance is equal.
;;;
;;; arg 1: pitch to quantize to pc
;;; arg 2: pc to quantize pitch against
;;;
;;; returns quntized pitch or #f if non available
;;;
;(defn pc:quantize [pitch pc]
;      (let loop ((inc 0))
;         (cond ((pc:? (+ pitch inc) pc) (+ pitch inc))
;               ((pc:? (- pitch inc) pc) (- pitch inc))
;               ((< inc 7) (loop (+ inc 1)))
;               (else (print-notification "no pc value to quantize to" pitch pc)
;                     #f)))))
;
;;; select random pitch from pitch class 
;;; bounded by lower and upper (inclusive lower exclusive upper)
;;;
;;; arg 1: lower bound (inclusive) 
;;; arg 2: upper bound (exclusive)
;;; arg 3: pitch class
;;;
;;; returns -1 if no valid pitch was found 
;;;
;(defn pc:random [lower upper pc]
;      (if (null? pc) 
;          -1
;          (let loop ((val (random lower upper)) (count 0))
;             (if (> count 50) 
;                 -1                   
;                 (if (memv (fmod val 12) pc) 
;                     val
;                     (loop (random lower upper) (+ count 1))))))))
;
;;; select pitch from pitch class relative to a given pitch
;;; 
;;; 1st: bass pitch
;;; 2nd: pc relationship to bass pitch (max is abs 7) 
;;; 3rd: pitch class  
;;;
;;; example: 
;;; (pc:relative 64 -2 '(0 2 4 5 7 9 11)) => 60
;;; (pc:relative 69 3 '(0 2 4 5 7 9 11)) => 74 
;;; 
;(defn pc:relative [pitch i pc]
;      (if (= i 0) pitch
;          (let ((inc (if (negative? i) - +)))
;             (let loop ((p (inc pitch 1)) (cnt 0))
;                (if (pc:? p pc) (set! cnt (inc cnt 1)))
;                (if (= cnt i) p
;                    (loop (inc p 1) cnt)))))))
;
;;; pc:make-chord
;;; creates a list of "number" pitches between "lower" and "upper" 
;;; bounds from the given "pc".  a division of the bounds
;;; by the number of elements requested breaks down the selection into
;;; equal ranges from which each pitch is selected.
;;; make-chord attempts to select pitches of all degrees of the pc.
;;; it is possible for elements of the returned chord to be -1 if no 
;;; possible pc is available for the given range. 
;;; non-deterministic (i.e. result can vary each time)
;;;
;;; arg1: lower bound (inclusive)
;;; arg2: upper bound (exclusive)
;;; arg3: number of pitches in chord 
;;; arg4: pitch class 
;;;
;;; example: c7  
;;; (pc:make-chord 60 85 4 '(0 4 7 10)) => (60 70 76 79) 
;;; 
;(defn pc:make-chord [lower upper number pc]
;      (let ((chord '()))
;         (let loop ((l lower)
;                    (u upper)
;                    (n number)
;                    (p pc))
;            (if (< n 1) 
;                (cl:sort (cl:remove -1 chord) <) ; lowest pitch to highest pitch remove -1s
;                (let* ((range (- u l))
;                       (gap (round (/ range n)))
;                       (pitch (pc:random l (+ l gap) p)))
;                   (if (< pitch 0) ; if new pitch is -1 try from whole range
;                       (set! chord (cons (pc:random lower upper p) chord))
;                       (set! chord (cons pitch chord)))
;                   (loop (+ l gap)
;                         u
;                         (- n 1)
;                         (if (> (length p) 1) 
;                             (cl:remove (fmod (car chord) 12) p)
;                             pc))))))))
;
;;; Returns a scale degree of a given value (pitch) based on a pc
;(defn pc:degree [value pc]
;      (let loop ((i 1)
;                 (lst pc))
;         (if (null? lst)
;             (begin (print-notification "pitch not in pc") -1)
;             (if (= (car lst) (fmod value 12))
;                 i
;                 (loop (+ i 1) (cdr lst)))))))
;
;;; retrograde list
;(define retrograde reverse)
;
;;; invert list paying no attention to key
;(defn invert [lst . args]      
;      (let ((pivot (if (null? args)
;                       (car lst)
;                       (car args))))
;         (cons (car lst) (map (lambda (i)
;                                 (- pivot (- i pivot)))
;                              (cdr lst))))))
;
;;; transpose list paying no attention to key
;(defn transpose [val lst]
;      (map (lambda (i)
;              (+ i val))
;           lst))) 
;
;;; expand/contract list by factor paying no attention to key
;(defn expand/contract [lst factor]
;      (cons (car lst)
;            (let loop ((old (car lst))
;                       (l (cdr lst))
;                       (current (car lst))
;                       (newlst '()))
;               (if (null? l)
;                   (reverse newlst)
;                   (loop (car l)
;                         (cdr l)
;                         (+ current (* factor (- (car l) old)))
;                         (cons (real->integer (+ current (* factor (- (car l) old))))
;                               newlst)))))))
;
;;; quantize the values of lst to pc
;(defn pc:quantize-list [lst pc]
;      (map (lambda (i)
;              (pc:quantize i pc))
;           lst)))
;
;;; invert the values of lst quantizing to pc
;(defn pc:invert [lst pc . args]
;      (if (null? args)
;          (pc:quantize-list (invert lst) pc)
;          (pc:quantize-list (invert lst (car args)) pc))))   
;
;;; transpose the values of lst quantizing to pc
;(defn pc:transpose [val lst pc]
;      (pc:quantize-list (transpose val lst) pc)))
;
;;; expand/contract lst by factor quantizing to pc
;(defn pc:expand/contract [lst factor pc]
;      (pc:quantize-list (expand/contract lst factor) pc)))
;
;;; returns a scale type based on a chord type (basic jazz modal theory)
;(defn pc:chord->scale [root type]
;      (pc:scale (fmod (+ (cadr (assoc type *pc:chord->scale*)) root))
;                (cddr (assoc type *pc:chord->scale*)))))
;
;;; returns a scale type based on a given root
;(defn pc:scale [root type]
;      (if (assoc type *pc:scales*)
;          (let loop ((l (cdr (assoc type *pc:scales*)))
;                     (current root)
;                     (newlst '()))
;             (if (null? l)
;                 (reverse (cons current newlst))
;                 (loop (cdr l) (fmod (+ current (car l)) 12) (cons current newlst))))
;          (begin (print-notification "Scale type not found." *pc:scales*) #f))))
;
;;; returns a chord following basic diatonic harmony rules
;;; based on root (0 for C etc.) maj/min ('- or '^) and degree (i-vii) 
;(defn pc:diatonic [root maj/min degree]   
;      (let ((val (assoc degree     
;                        (if (equal? '^ maj/min)
;                            *pc:diatonic-major*
;                            *pc:diatonic-minor*))))
;         (pc:chord (modulo (+ root (cadr val)) 12) (cddr val)))))
;
;;; returns a chord given a root and type
;;; see *pc:chord-syms* for currently available types
;;; 
;;; e.g. (pc:chord 0 '^7)  => '(0 4 7 11)
;(defn pc:chord [root type]
;      (let ((chord (assoc type *pc:chord-syms*)))
;         (if chord
;             (let loop ((l (cdr chord))
;                        (newlst '()))
;                (if (null? l)
;                    (reverse newlst)
;                    (loop (cdr l) (cons (fmod (+ (car l) root) 12) newlst))))
;             (begin (print-notification "Chord type not found." chords) #f)))))
;

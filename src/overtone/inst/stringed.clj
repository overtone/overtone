;; A Stringed Instrument Generator Macro.
;;
;; See the guitar.clj for how to use this macro and
;; overtone/examples/instruments/guitar_inst.clj for example usage.
;; Other instruments (like bass-guitar, ukelele, mandolin, etc.) may
;; use the same basic instrument.
(ns overtone.inst.stringed
  (:use [overtone.music pitch time]
        [overtone.studio inst]
        [overtone.sc envelope node server ugens]
        [overtone.sc.cgens mix]))

;; ======================================================================
(defmacro gen-stringed-inst
  "Macro to generate a stringed instrument with distortion, reverb and
   a low-pass filter.  Use the pluck-strings and strum-strings helper
  functions to play the instrument.

   Note: the strings need to be silenced with a gate -> 0 transition
   before a gate -> 1 transition activates it.  Testing
   showed it needed > 25 ms between these transitions to be effective."
  [name num-strings]
  (let [note-ins (apply vector
                        (map #(symbol (format "note-%d" %)) (range num-strings)))
        note-default-ins (apply vector
                                (flatten (map vector
                                              note-ins
                                              (repeat num-strings {:default 60 :min 0 :max 127}))))
        gate-ins (apply vector
                        (map #(symbol (format "gate-%d" %)) (range num-strings)))
        gate-default-ins (apply vector (flatten (map vector
                                                     gate-ins
                                                     (repeat num-strings {:default 0}))))
        both-default-ins (into note-default-ins gate-default-ins)
        note-gate-pairs (apply vector (map vector note-ins gate-ins))
        ]
    `(definst ~name
       [~@both-default-ins
        ~'dur       {:default 10.0}
        ~'decay     {:default 30} ;; pluck decay
        ~'coef      {:default 0.3 :min -1 :max 1} ;; pluck coef
        ~'noise-amp {:default 0.8 :min 0.0 :max 1.0}
        ~'pre-amp   {:default 6.0}
        ~'amp       {:default 1.0}
        ;; by default, no distortion, no reverb, no low-pass
        ~'distort   {:default 0.0 :min 0.0 :max 0.9999999999}
        ~'rvb-mix   {:default 0.0 :min 0.0 :max 1.0}
        ~'rvb-room  {:default 0.0 :min 0.0 :max 1.0}
        ~'rvb-damp  {:default 0.0 :min 0.0 :max 1.0}
        ~'lp-freq   {:default 20000}
        ~'lp-rq     {:default 1.0}]
       (let [strings# (map #(let [frq#  (midicps (first %))
                                  nze#  (~'* ~'noise-amp (pink-noise))
                                  plk#  (pluck nze#
                                               (second %)
                                               (/ 1.0 8.0)
                                               (~'/ 1.0 frq#)
                                                ~'decay
                                                ~'coef)]
                              (leak-dc (~'* plk#
                                            (env-gen (asr 0.0001 1 0.1)
                                                     :gate (second %)))
                                       0.995))
                           ~note-gate-pairs)
             src# (~'* ~'pre-amp (mix strings#))
             ;; distortion from fx-distortion2 
             k#   (~'/ (~'* 2 ~'distort) (~'- 1 ~'distort))
             dis# (~'/ (~'* src# (~'+ 1 k#))
                       (~'+ 1 (~'* k# (~'abs src#))))
             vrb# (free-verb dis# ~'rvb-mix ~'rvb-room ~'rvb-damp)
             fil# (rlpf vrb# ~'lp-freq ~'lp-rq)]
         (~'* ~'amp fil#)))))

;; ======================================================================
;; common routines for stringed instruments

(defn- fret-to-note
  "given a fret-offset, add to the base note index with special
  handling for -1"
  [base-note offset]
  (if (>= offset 0)
    (+ base-note offset)
    offset))

(defn- mkarg
  "useful for making arguments for the instruments strings"
  [s i]
  (keyword (format "%s-%d" s i)))

;; ======================================================================
;; Main helper functions used to play the instrument: pick or strum
(defn pick-string
  "pick the instrument's string depending on the fret selected.  A
   fret value less than -1 will cause no event; -1 or greater causes
   the previous note to be silenced; 0 or greater will also cause a
   new note event."
  ([the-strings the-inst string-index fret t]
     (let [the-note (fret-to-note (nth the-strings string-index) fret)] 
       ;; turn off the previous note
       (if (>= the-note -1)
         (at t (ctl the-inst (mkarg "gate" string-index) 0)))
       ;; NOTE: there needs to be some time between these
       ;; FIXME: +50 seems conservative.  Find minimum.
       (if (>= the-note 0)
         (at (+ t 50) (ctl the-inst
                           (mkarg "note" string-index) the-note
                           (mkarg "gate" string-index) 1)))))
  ([the-chord-frets the-inst string-index fret]
     (pick-string the-chord-frets the-inst string-index fret (now))))

;; ======================================================================
(defn strum-strings
  "strum a chord on the instrument in a direction (:up or :down) with
   a strum duration of strum-time at t.  If the-chord is a vector, use
   it directly for fret indexes."
  ([chord-fret-map the-strings the-inst the-chord direction strum-time t]
     (let [num-strings (count (chord-fret-map :A))
           ;; ex: [-1 3 2 0 1 0]
           chord-frets (if (vector? the-chord)
                         ;; FIXME -- assert len(the-chord) is right?
                         the-chord ; treat the chord as a series of frets
                         (chord-fret-map the-chord))
           ;; account for unplayed strings for delta time calc. Code
           ;; gets a bit complicated to deal with the case where
           ;; strings are muted and don't count towards the
           ;; strum-time.
           ;; ex: (0 0 1 2 3 4)
           fret-times (map first
                           (rest (reductions
                                  #(vector (if (>= (second %1) 0)
                                             (inc (first %1))
                                             (first %1))
                                           %2)
                                  [0 -1]
                                  chord-frets)))]
       (dotimes [i num-strings]
         (let [j (if (= direction :up) (- num-strings 1 i) i)
               max-t (apply max fret-times)
               dt (if (> max-t 0)
                    (* 1000 (/ strum-time max-t))
                    0)
               fret-delta (if (= direction :up)
                            (- max-t (nth fret-times i))
                            (nth fret-times i))]
           (pick-string the-strings the-inst j
                        (nth chord-frets j)
                        (+ t (* fret-delta dt)))))))
  ([chord-fret-map the-strings the-inst the-chord direction strum-time]
     (strum-strings chord-fret-map the-strings the-inst the-chord
                    direction strum-time (now)))
  ([chord-fret-map the-strings the-inst the-chord direction]
     (strum-strings chord-fret-map the-strings the-inst the-chord
                    direction 0.05 (now)))
  ([chord-fret-map the-strings the-inst the-chord]
     (strum-strings chord-fret-map the-strings the-inst the-chord
                    :down 0.05 (now))))


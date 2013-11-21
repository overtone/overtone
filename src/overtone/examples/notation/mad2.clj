(ns overtone.examples.notation.mad2
  (:use overtone.live
        overtone.inst.synth))

(definst tone [note 60 amp 0.3 dur 0.4]
  (let [snd (sin-osc (midicps note))
        env (env-gen (perc 0.01 dur) :action FREE)]
    (* env snd amp)))

(defn defpitch
  [p-sym pitch]
  (intern *ns* p-sym pitch))

(defn def-pitches
  "Define vars for all pitches."
  []
  (doseq [octave (range 8)]
    (doseq [n (range 7)]
      (let [n-char (char (+ 97 n))
            p-sym (symbol (str n-char octave))
            note (octave-note octave (get NOTES (keyword (str n-char))))]
        (defpitch p-sym note)
        (when-let [sharp (get NOTES (keyword (str n-char "#")))]
          (defpitch (symbol (str n-char "#" octave))
                    (octave-note octave sharp)))
        (when-let [flat (get NOTES (keyword (str n-char "b")))]
          (defpitch (symbol (str n-char "b" octave))
                    (octave-note octave flat)))))))

(def-pitches)

(defn i2p
  "Convert intervals to pitches.  Supports nested collections as well."
  [intervals scale root]
  (map (fn [i]
         (cond
           (coll? i) (i2p i scale root)
           (nil? i) nil
           :default (+ root (degree->interval i scale))))
       intervals))

(defn play-over
  "Play a sequence of notes starting at t over a fixed duration of time.
  Note: both t and dur are in milliseconds."
  [inst notes t dur]
  (let [interval (/ dur (count notes))]
    (doall
      (map-indexed
        (fn [idx pitch]
          (let [cur-t (+ t (* idx interval))]
            (cond
              (coll? pitch)
              (play-over inst pitch cur-t interval)

              (number? pitch)
              (at cur-t
                  (inst pitch 0.8 (/ interval 1000.0))))))
        notes))))

(defn play
  "Play bars of notes on an instrument with a metronome."
  [inst bars m & [t-sig]]
  (when bars
    (let [t-sig (or t-sig 4)
          beats-per-bar t-sig ; (numerator t-sig)
          ms-per-bar (- (m beats-per-bar) (m 0))
          bar (first bars)
          beat (m)
          bar-start (m beat)]
      (play-over inst bar bar-start ms-per-bar)
      (apply-by (m (+ beat beats-per-bar)) #'play [inst (next bars) m t-sig]))))

(def metro (metronome 120))

;[[e4 g4 e4] [e5 b4 g4 d4 a4 e4 g4 a4]], the derezzed example, could be:
;in an aeolian scale, starting on E4.
(def _ nil)
(def derez [[:i :iii :i] [:i* :v :iii :vii :iv. :i :iii :iv]])
(def pitches (i2p derez :aeolian e4))

;(play tone pitches metro)

(comment

  (play ks1-demo (i2p [[:i [:v _ :v] :i] [:i [:i :iv] :i _ :v _ :i]] :diatonic f4) metro)

  (do
    (play grunge-bass
          (cycle (i2p [[:i [:v _ :v] :i] [:i [:i :iv] :i _ :v _ :i]]
                      :diatonic f2))
          metro)

    (play pad
          (cycle (i2p [[:i [:v _ :v] :i] [:i [:i :iv] :i _ :v _ :i]]
                      :diatonic f3))
          metro))
)
;; (stop)

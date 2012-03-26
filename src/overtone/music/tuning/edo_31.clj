(ns overtone.music.tuning.edo-31
    (:use [overtone.music.tuning]
          [overtone.music.pitch]
          [clojure.java.io]
          [clojure.set]
          [clojure.math.numeric-tower]))

;(defmethod tunednotes :edo-31 [symb]
;    (fn [note-name]
;        (let [symbol (.toLowerCase (name note-name)) ; TODO This should be (lower-case x)
;              note (first symbol)
;              modifier (rest symbol)]
;            (mod (+ (get {\c  0, \d  5, \e  10, \f  13, \g  18, \a  23, \b  28} note)
;                    (* -2 (count (filter #(= \b %) modifier)))
;                    (* 2 (count (filter #(= \# %) modifier)))) 31)))

(defmethod reversenotes :edo-31 [[symb initial _]] ; TODO This function doesn't effing work.
    (fn [notenumber] (get {0 :c, 1 :dbb, 2 :c#, 3 :db, 4 :c##,
                      5 :d, 6 :ebb, 7 :d#, 8 :eb, 9 :d##,
                      10 :e, 11 :fb, 12 :e#,
                      13 :f, 14 :gbb, 15 :f#, 16 :gb, 17 :f##,
                      18 :g, 19 :abb, 20 :g#, 21 :ab, 22 :g##,
                      23 :a, 24 :bbb, 25 :a#, 26 :bb, 27 :a##,
                      28 :b, 29 :cb, 30 :b#} notenumber)))

(defmethod perfn :edo-31 [[symb initial freq]]
    (perfn (list :edo 31 initial freq)))

(defmethod tunednotes :edo-31 [[symb initial _]]
    (fn [n]
        (if (or (keyword? n) (string? n))
            (let [match (re-find lilypondpattern (name n))
                  _ (when (nil? match)
                    (throw (IllegalArgumentException.
                               (str "Unable to resolve note: " n ". Does not appear to be in Quarter-Tone Meantone format i.e. C#4"))))
                  [_ pitchclass modifiers octavestr] match
                  shift (* 2 (floor (parsemodifiers modifiers)))
                  octave (Integer. octavestr)]
                (+ (get {"c"  0, "d"  5, "e"  10, "f"  13, "g"  18, "a"  23, "b"  28} (clojure.string/lower-case pitchclass))
                    (int (floor shift))
                    (* 31 octave)
                    31))
            n)))
(ns
    ^{:doc "Functions that allow use of the 31-edo scale system."
      :author "David \"DAemon\" Allen"}
    overtone.music.tuning.31edo
    (:use [overtone.music.tuning]
     [overtone.music.pitch]
     [clojure.java.io]
     [clojure.set]
     [clojure.math.numeric-tower]))

(defn notes-31-edo [note-name]
    (let [symbol (.toLowerCase (name note-name)) ; TODO This should be (lower-case x)
          note (first symbol)
          modifier (rest symbol)]
        (mod (+ (get {\c  0, \d  5, \e  10, \f  13, \g  18, \a  23, \b  28} note)
              (* -2 (count (filter #(= \b %) modifier)))
              (* 2 (count (filter #(= \# %) modifier)))) 31)))
(def reverse-notes-31-edo {0 :c, 1 :dbb, 2 :c#, 3 :db, 4 :c##,
                           5 :d, 6 :ebb, 7 :d#, 8 :eb, 9 :d##,
                           10 :e, 11 :fb, 12 :e#,
                           13 :f, 14 :gbb, 15 :f#, 16 :gb, 17 :f##,
                           18 :g, 19 :abb, 20 :g#, 21 :ab, 22 :g##,
                           23 :a, 24 :bbb, 25 :a#, 26 :bb, 27 :a##,
                           28 :b, 29 :cb, 30 :b#})

(defmethod perfn :31edo [[symb initial freq]]
    (perfn (list :edo 31 initial freq)))
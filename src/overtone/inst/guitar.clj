;; A Guitar Instrument
;; see overtone/examples/instruments/guitar_inst.clj for example usage
(ns overtone.inst.guitar
  (:use [overtone.inst.stringed]
        [overtone.music pitch]      
        [overtone.studio inst]
        [overtone.sc envelope node server ugens]
        [overtone.sc.cgens mix]))

;; ======================================================================
;; A map of chords to frets held for that chord.  This is not all
;; possible guitar chords, just some of them as there are many
;; alternatives to choose from.  Add more as you find/need them.
;;
;; You can pass in your own arrays to strum, too.  The values are the
;; fret number of the string to press.  This selects the note to play.
;;   -1 indicates you mute that string
;;   -2 indicates you leave that string alone & keep the current state
;;      of either playing or not
;;
(def guitar-chord-frets
  {:A    [ -1  0  2  2  2  0 ]
   :A7   [ -1  0  2  0  2  0 ]
   :A9   [  0  0  2  4  2  3 ]
   :Am   [  0  0  2  2  1  0 ]
   :Am7  [  0  0  2  0  1  0 ]

   :Bb   [ -1  1  3  3  3  1 ]
   :Bb7  [ -1 -1  3  3  3  4 ]
   :Bb9  [ -1 -1  0  1  1  1 ]
   :Bbm  [ -1 -1  3  3  2  1 ]
   :Bbm7 [  1  1  3  1  2  1 ]

   :B    [ -1 -1  4  4  4  2 ]
   :B7   [ -1  2  1  2  0  2 ]
   :B9   [  2 -1  1  2  2  2 ]
   :Bm   [ -1 -1  4  4  3  2 ]
   :Bm7  [ -1  2  0  2  0  2 ]

   :C    [ -1  3  2  0  1  0 ]
   :C7   [ -1  3  2  3  1  0 ]
   :C9   [  3  3  2  3  3  3 ]
   :Cm   [  3  3  5  5  4  3 ]
   :Cm7  [  3  3  5  3  4  3 ]

   :Db   [ -1 -1  3  1  2  1 ]
   :Db7  [ -1 -1  3  4  2  4 ]
   :Db9  [  4 -1  3  4  4  4 ]
   :Dbm  [ -1 -1  2  1  2  0 ]
   :Dbm7 [ -1  3  2  1  0  0 ]

   :D    [ -1 -1  0  2  3  2 ]
   :D7   [ -1 -1  0  2  1  2 ]
   :D9   [ -1 -1  4  2  1  0 ]
   :Dm   [ -1  0  0  2  3  1 ]
   :Dm7  [ -1 -1  0  2  1  1 ]

   :Eb   [ -1 -1  5  3  4  3 ]
   :Eb7  [ -1 -1  1  3  2  3 ]
   :Eb9  [ -1 -1  1  0  2  1 ]
   :Ebm  [ -1 -1  4  3  4  2 ]
   :Ebm7 [ -1 -1  1  3  2  2 ]

   :E    [  0  2  2  1  0  0 ]
   :E7   [  0  2  0  1  0  0 ]
   :E9   [  0  2  0  1  3  2 ]
   :Em   [  0  2  2  0  0  0 ]
   :Em7  [  0  2  2  0  3  0 ]

   :F    [  1  3  3  2  1  1 ]
   :F7   [  1 -1  2  2  1 -1 ]
   :F9   [  1  0  3  0  1 -1 ]
   :Fm   [  1  3  3  1  1  1 ]
   :Fm7  [  1  3  3  1  4  1 ]

   :Gb   [  2  4  4  3  2  2 ]
   :Gb7  [ -1 -1  4  3  2  1 ]
   :Gb9  [ -1  4 -1  3  5  4 ]
   :Gbm  [  2  4  4  2  2  2 ]
   :Gbm7 [  2 -1  2  2  2 -1 ]

   :G    [  3  2  0  0  0  3 ]
   :G7   [  3  2  0  0  0  1 ]
   :G9   [ -1 -1  0  2  0  1 ]
   :Gm   [ -1 -1  5  3  3  3 ]
   :Gm7  [ -1  1  3  0  3 -1 ]

   :Ab   [ -1 -1  6  5  4  4 ]
   :Ab7  [ -1 -1  1  1  1  2 ]
   :Ab9  [ -1 -1  1  3  1  2 ]
   :Abm  [ -1 -1  6  4  4  4 ]
   :Abm7 [ -1 -1  4  4  7  4 ]

   :Gadd5 [  3  2  0  0  3  3 ]
   :Cadd9 [ -1  3  2  0  3  3 ]
   :Dsus4 [ -1 -1  0  2  3  3 ]

   })

;; ======================================================================
;; an array of 6 guitar strings: EADGBE
(def guitar-string-notes (map note [:e2 :a2 :d3 :g3 :b3 :e4]))

;; ======================================================================
;; Main helper functions.  Use pick or strum to play the instrument.
(def pick (partial pick-string guitar-string-notes))
(def strum (partial strum-strings guitar-chord-frets guitar-string-notes))

;; ======================================================================
;; Create the guitar definst.  Now via the power of macros
(gen-stringed-inst guitar 6)

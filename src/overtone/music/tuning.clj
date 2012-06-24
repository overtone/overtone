(ns
  ^{:doc "Functions that define tuning systems from various musical traditions and theories."
     :author "Jeff Rose"}
  overtone.music.tuning
  (:use [overtone.music pitch]))

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

;; 24-tone equal temperament --> Arabic music
;; ratio = (Math/pow 2 (/ 1 24)) => 1.029302236643492

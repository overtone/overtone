(ns ^{:doc "Default vals and fns required  to manipulate ugens."
      :author "Jeff Rose"}
  overtone.sc.ugen.defaults
  (:use
   [overtone util]))

;; Outputs have a specified calculation rate
;;   0 = scalar rate - one sample is computed at initialization time only.
;;   1 = control rate - one sample is computed each control period.
;;   2 = audio rate - one sample is computed for each sample of audio output.
(def RATES {:ir 0
            :kr 1
            :ar 2
            :dr 3})

(def REVERSE-RATES (invert-map RATES))

(def UGEN-RATE-PRECEDENCE [:ir :dr :ar :kr])
(def UGEN-DEFAULT-RATES #{:ar :kr})

(def UGEN-RATE-SORT-FN
  (zipmap UGEN-RATE-PRECEDENCE (range (count UGEN-RATE-PRECEDENCE))))

(def NO-ARG-DOC-FOUND "-")

(def DEFAULT-ARG-DOCS
  {"bufnum" "A buffer or buffer index value."
   "freq" "frequency in hz (cycles per second)"
   "freq1" "frequency in hz (cycles per second)"
   "freq2" "frequency in hz (cycles per second)"
   "freq3" "frequency in hz (cycles per second)"
   "phase" "the start point within a cycle"
   "loop" "a boolean switch to turn on looping"
   "in" "the input signal"
   })

(def DOC-WIDTH 60)


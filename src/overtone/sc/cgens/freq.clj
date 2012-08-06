(ns overtone.sc.cgens.freq
  (:use [overtone.sc defcgen ugens]))

(defcgen add-cents
  "Add n-cents to freq."
  [freq    {:doc "Input frequency source"}
   n-cents {:default 1 :doc "Number of cents to add"}]
  "Returns a frequency which is the result of adding n-cents to the src
  frequency. A cent is a logarithmic measurement of pitch, where
  1-octave equals 1200 cents."
  (:ar (* freq (pow 2 (/ n-cents 1200))))
  (:kr (* freq (pow 2 (/ n-cents 1200)))))

(ns overtone.sc.cgens.freq
  (:use
   overtone.sc.defcgen
   overtone.sc.ugens
   overtone.algo.chance))

(defcgen add-cents
  "Add n-cents to freq."
  [freq    {:doc "Input frequency source"}
   n-cents {:default 1 :doc "Number of cents to add"}]
  "Returns a frequency which is the result of adding n-cents to the src
  frequency. A cent is a logarithmic measurement of pitch, where
  1-octave equals 1200 cents."
  (:ar (* freq (pow 2 (/ n-cents 1200))))
  (:kr (* freq (pow 2 (/ n-cents 1200)))))

(defcgen freq-spread
  "Turn a single frequency into multiple slightly offset frequencies."
  [freq   {:doc "Center frequency"}
   n      {:doc     "Number of copies to make"
           :default 3}
   spread {:doc     "How far frequencies can deviate, in semitones"
           :default 0.1}]
  "Multiplies a single frequency into multiple, using multichannel expansion,
  with each frquency randomly offset within a small range around the base
  frequency. Using `splay` on the result creates pleasing sounds."
  (:ar (repeatedly n #(* freq (+ 1 (midiratio (srand spread))))))
  (:kr (repeatedly n #(* freq (+ 1 (midiratio (srand spread)))))))

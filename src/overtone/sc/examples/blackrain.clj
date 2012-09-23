(ns overtone.sc.examples.blackrain
  (:use [overtone.sc.machinery defexample]
        [overtone.sc ugens]))

(defexamples iir-filter
  (:low-pass
   "Create a low pass filter asdfasdf"
   "Modifying rq changes the characteristics of the filter around the cutoff frequency.  Here we use iir-filter to create a low pass filter.  The iir-filter has a 24db/oct rolloff.  For input signal, we'll use pink noise.  For the cutoff frequency we'll use frequencies ranging from 20 to 20000 Hz exponentially, depending on the mouse-x coordinate.  The rq ranges from 0.001 to 1 linearly depending on the mouse-y coordinate."
   rate :ar
   []
   "(iir-filter (pink-noise) (mouse-x 20 20000 EXP) (mouse-y 0.001 1))"
   contributor "Colleen"))

(defexamples b-moog
   (:compare-filters
   "Compare low, high, and bandpass"
   "Some longer description here."
   rate :ar
   []
   "(b-moog (pink-noise 265 (mouse-x (/ 0.001 265) (/ 10000 265)) (mouse-y 0.01 2.99)))"
   contributor "Colleen"))

(ns overtone.examples.ugens.blowpass4
  (:use [overtone.live]))

(demo 10
      (b-low-pass4
       (sound-in [0 1])
       (mouse-x 10 20000 EXPONENTIAL)
       (mouse-y 0.1 1 LINEAR)
       0.5))

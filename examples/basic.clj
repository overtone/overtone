(ns basic
  (:use overtone.live))

(refer-ugens)

(defsynth ticker [freq]
  (impulse.kr 120))

(ns overtone.core.ugen.extra
  (:require [overtone.ugens :as ug]))

(defn mix 
  "Mix down (sum) a set of input channels into a single channel."
  [& inputs]
  (reduce ug/+ inputs))

(defn square 
  "a square wave generator."
  [freq]
  (ug/pulse freq 0.5))


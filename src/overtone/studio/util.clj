(ns
  ^{:doc "Util synths"
     :author "Sam Aaron & Jeff Rose"}
  overtone.studio.util
  (:use [overtone.libs event]
        [overtone.sc synth ugens]))

;; Some utility synths for signal routing and scoping

(defsynth bus->buf [bus 20 buf 0]
  (record-buf (in bus) buf))

(defsynth bus->bus [in-bus 20 out-bus 0]
  (out out-bus (in in-bus)))

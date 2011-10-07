(ns
    ^{:doc "Namespace containing all default ugens and cgens"
      :author "Sam Aaron"}
  overtone.sc.gens
  (:use [overtone.util.ns :only [immigrate]])
  (:require [overtone.sc.cgens audio-in oscillators demand mix]
            [overtone.sc ugens]))

(immigrate
 'overtone.sc.ugens
 'overtone.sc.cgens.audio-in
 'overtone.sc.cgens.oscillators
 'overtone.sc.cgens.demand
 'overtone.sc.cgens.mix)

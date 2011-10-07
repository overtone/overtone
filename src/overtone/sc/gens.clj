(ns
    ^{:doc "Namespace containing all default ugens and cgens"
      :author "Sam Aaron"}
  overtone.sc.gens
  (:use [overtone.util.ns :only [immigrate]])
  (:require [overtone.sc.machinery.cgens audio-in oscillators demand mix]
            [overtone.sc.machinery ugens]))

(immigrate
 'overtone.sc.machinery.ugens
 'overtone.sc.machinery.cgens.audio-in
 'overtone.sc.machinery.cgens.oscillators
 'overtone.sc.machinery.cgens.demand
 'overtone.sc.machinery.cgens.mix)

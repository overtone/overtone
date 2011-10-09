(ns
    ^{:doc "Namespace containing all default ugens and cgens"
      :author "Sam Aaron"}
  overtone.sc.gens
  (:use [overtone.util.ns :only [immigrate]])
  (:require [overtone.sc.machinery.cgens oscillators demand mix io]
            [overtone.sc.machinery ugens]))

(immigrate
 'overtone.sc.machinery.ugens
 'overtone.sc.machinery.cgens.oscillators
 'overtone.sc.machinery.cgens.demand
 'overtone.sc.machinery.cgens.mix
 'overtone.sc.machinery.cgens.io)

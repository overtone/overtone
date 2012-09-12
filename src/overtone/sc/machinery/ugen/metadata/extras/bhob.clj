(ns overtone.sc.machinery.ugen.metadata.extras.bhob
  (:use [overtone.sc.machinery.ugen common check]))

(def specs
  [
   {:name "Dbrown2"
    :args [{:name "lo" :doc "minimum value"}
           {:name "hi" :doc "maximum value"}
           {:name "step" :doc "maximum step for each new value"}
           {:name "dist" :doc "gendyn distribution (see gendy1)"}
           {:name "length" :doc "number of values to create"}]
    :rates #{:dr}
    :summary "demand rate brownian movement with Gendyn distributions"
    :doc "Dbrown2 returns numbers in the continuous range between lo and
          hi. The arguments can be a number or any other ugen."}

   {:name "MoogLadder"
    :summary "Moog Filter Emulation"
    :args [{:name "input" :doc "Audio input"}
           {:name "ffreq" :default 440 :doc "Cutoff freq"}
           {:name "res" :default 0 :doc "Resonance (0 -> 1)"}]
    :doc "Moog Filter."}

   ])

(ns overtone.sc.machinery.ugen.metadata.extras.bhob
  (:use [overtone.sc.machinery.ugen common check]))

(def specs
  [

   {:name "DoubleNestedAllpassN"
    :summary "Double Nested Allpass Filter N"
    :args [{:name "in"
            :doc "Input signal"}

           {:name "max-delay1"
            :default 0.0047
            :doc ""}

           {:name "delay1"
            :default 0.0047
            :doc ""}

           {:name "gain1"
            :default 0.15
            :doc ""}

           {:name "max-delay2"
            :default 0.022
            :doc ""}

           {:name "delay2"
            :default 0.022
            :doc ""}

           {:name "gain2"
            :default 0.25
            :doc ""}

           {:name "max-delay3"
            :default 0.0083
            :doc ""}

           {:name "delay3"
            :default 0.0083
            :doc ""}

           {:name "gain3"
            :default 0.3
            :doc ""}]
    :rates #{:ar}
    :doc ""}

   {:name "DoubleNestedAllpassL"
    :summary "Double Nested Allpass Filter L"
    :args [{:name "in"
            :doc "Input signal"}

           {:name "max-delay1"
            :default 0.0047
            :doc ""}

           {:name "delay1"
            :default 0.0047
            :doc ""}

           {:name "gain1"
            :default 0.15
            :doc ""}

           {:name "max-delay2"
            :default 0.022
            :doc ""}

           {:name "delay2"
            :default 0.022
            :doc ""}

           {:name "gain2"
            :default 0.25
            :doc ""}

           {:name "max-delay3"
            :default 0.0083
            :doc ""}

           {:name "delay3"
            :default 0.0083
            :doc ""}

           {:name "gain3"
            :default 0.3
            :doc ""}]
    :rates #{:ar}
       :doc ""}

   {:name "DoubleNestedAllpassC"
    :summary "Double Nested Allpass Filter C"
    :args [{:name "in"
            :doc "Input signal"}

           {:name "max-delay1"
            :default 0.0047
            :doc ""}

           {:name "delay1"
            :default 0.0047
            :doc ""}

           {:name "gain1"
            :default 0.15
            :doc ""}

           {:name "max-delay2"
            :default 0.022
            :doc ""}

           {:name "delay2"
            :default 0.022
            :doc ""}

           {:name "gain2"
            :default 0.25
            :doc ""}

           {:name "max-delay3"
            :default 0.0083
            :doc ""}

           {:name "delay3"
            :default 0.0083
            :doc ""}

           {:name "gain3"
            :default 0.3
            :doc ""}]
    :rates #{:ar}
    :doc ""}

   {:name "Dbrown2"
    :summary "demand rate brownian movement with Gendyn distributions"
    :args [{:name "lo"
            :doc "minimum value"}

           {:name "hi"
            :doc "maximum value"}

           {:name "step"
            :doc "maximum step for each new value"}

           {:name "dist"
            :doc "gendyn distribution (see gendy1)"}

           {:name "length"
            :doc "number of values to create"}]

    :rates #{:dr}
    :doc "Dbrown2 returns numbers in the continuous range between lo and
          hi. The arguments can be a number or any other ugen."}


   {:name "MoogLadder"
    :summary "Moog Filter Emulation"
    :args [{:name "input"
            :doc "Audio input"}

           {:name "ffreq"
            :default 440
            :doc "Cutoff freq"}

           {:name "res"
            :default 0
            :doc "Resonance (0 -> 1)"}]
    :doc "Moog Filter."}


   {:name "GaussTrig"
    :summary "Impulses around a certain frequency"
    :args [{:name "freq"
            :default 440
            :doc "mean frequency"}

           {:name "dev"
            :default 0.3
            :doc "random deviation from mean (0 <= dev < 1)"}]
    :rates #{:ar :kr}
    :doc "Impulses around a certain frequency"}])

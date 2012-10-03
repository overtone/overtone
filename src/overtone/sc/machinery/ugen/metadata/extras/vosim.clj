(ns overtone.sc.machinery.ugen.metadata.extras.vosim
  (:use [overtone.sc.machinery.ugen common check]))

(def specs
  [
   {:name "VOSIM"
    :summary "Vosim pulse generator."
    :args [{:name "trig"
            :default 0.1
            :doc "Starts a vosim pulse when a transition from non-positive to positive occurs and no other vosim  is still going."
            :rates #{:ar :kr}}

           {:name "freq"
            :default 400.0
            :doc "The frequency of the squared sinewave."
            :rates #{:ar :kr}}

           {:name "nCycles"
            :default 1
            :doc "The number of squared sinewaves to use in one vosim pulse."}

           {:name "decay"
            :default 0.9
            :doc "The decay factor."
            :rates #{:ar :kr}}]
    
    :rates #{:ar}
    :doc "Vosim pulse generator."}])

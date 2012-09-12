(ns overtone.sc.machinery.ugen.metadata.extras.blackrain
  (:use [overtone.sc.machinery.ugen common check]))

(def specs
  [

   {:name "AmplitudeMod"
    :summary "Amplitude Follower"
    :args [{:name "input"
            :default 0
            :doc "Input signal"}

           {:name "attack-time"
            :default 0.01
            :doc "60dB convergence time for following attacks."}

           {:name "release-time"
            :default 0.01
            :doc "60dB convergence time for following decays."}]

    :rates #{:ar :kr}
    :doc "Tracks the peak amplitude of a signal.  As a opposed to
          Amplitude, AmplitudeMod, allows attack and release times to be
          modulated once the UGen has been instantiated. There is a tiny
          overhead associated to this."}

   ])

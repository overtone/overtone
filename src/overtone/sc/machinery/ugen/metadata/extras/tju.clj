(ns overtone.sc.machinery.ugen.metadata.extras.tju
  (:use [overtone.sc.machinery.ugen common check]))

(def specs
  [
   {:name "DFM1"
    :summary "Digitally modelled analog filter"
    :args [{:name "in"
            :doc "The input signal"}

           {:name "freq"
            :default 1000
            :doc "The cutoff frequency of the filter" }

           {:name "res"
            :default 0.1
            :doc "The filter resonance (values > 1.0 may lead to
                  self-oscillation)"}

           {:name "inputgain"
            :default 1
            :doc "Gain applied to the input signal which can be used to
                  generate distortion"}

           {:name "type"
            :default 0
            :doc "Set to 0.0 for low-pass behaviour or 1.0 for
                  high-pass"}

           {:name "noiselevel"
            :default 0.0003
            :doc "the noisiness of the filter"}]
    :rates #{:ar}
    :doc "Provides low-pass and high-pass filtering. The filter can be
          overdriven and will self-oscillate at high resonances." }])

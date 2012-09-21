(ns overtone.sc.machinery.ugen.metadata.extras.distortion
  (:use [overtone.sc.machinery.ugen common check]))

(def specs
  [
   {:name "Decimator"
    :summary "Reduce effective sample rate and bit depth"
    :args [{:name "in"
            :doc "Input signal"}

           {:name "rate"
            :default 44100
            :doc "The sample rate the sample will be resampled at"}

           {:name "bits"
            :default 24
            :doc "The bit depth that the signal will be reduced to"}]

    :rates #{:ar}
    :doc "Decimates (reduces the effective sample rate), and reduces the
          bit depth of the input signal, allows non integer values for
          smooth transitions between clean and lofi signals."}])

(ns overtone.sc.machinery.ugen.metadata.extras.distortion
  (:use [overtone.sc.machinery.ugen common check]))

(def specs
  [
   {:name "CrossoverDistortion"
    :summary "Port of ladspa crossover distortion"
    :args [{:name "in"
            :doc "The input signal"}

           {:name "amp"
            :default 0.5
            :doc "Controls the point at which the output signal becomes linear."}

           {:name "smooth"
            :default 0.5
            :doc "Controls degree of smoothing of the crossover point."}]

    :rates #{:ar}
    :doc "This is a simulation of the distortion that happens in class B
          and AB power amps when the signal crosses 0.For class B
          simulations the smooth value should be set to about 0.3 +/-
          0.2 and for AB it should be set to near 1.0."}


   {:name "SmoothDecimator"
    :summary "Port of ladspa smooth decimator"
    :args [{:name "in"
            :doc "The input signal"}

           {:name "rate"
            :default 44100
            :doc "The rate at which the output signal will be resampled."}

           {:name "smoothing"
            :default 0.5
            :doc "The amount of smoothing on the output signal."}]
    :rates #{:ar}
    :doc "Decimates (reduces the effective sample rate)."}


   {:name "SineShaper"
    :summary "Port of ladspa wave shaper"
    :args [{:name "in"
            :doc "The input signal"}

           {:name "limit"
            :default 1
            :doc ""}]
    :rates #{:ar}
    :doc "Sine-based wave shaper"}


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
          smooth transitions between clean and lofi signals."}


   {:name "Disintegrator"
    :summary "Port of ladspa disintegrator"
    :args [{:name "in"
            :doc "The input signal"}

           {:name "probability"
            :default 0.5
            :doc ""}

           {:name "multiplier"
            :default 0
            :doc ""}]

    :rates #{:ar}
    :doc "Amplifies random half-cycles of its input by multiplier. Set
          multiplier to 0 and vary probability for a weird fade effect,
          or set multiplier to -1 and probability to 0.5 to turn pitched
          sounds into noise." }])

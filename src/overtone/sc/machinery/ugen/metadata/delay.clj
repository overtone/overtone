(ns overtone.sc.machinery.ugen.metadata.delay
  (:use [overtone.sc.machinery.ugen common check]))

(def specs
  [
   {:name "Delay1",
    :args [{:name "in"
            :default 0.0
            :doc "input to be delayed."}]
    :check (nth-input-stream? 0)
    :doc "delay input signal by one frame of samples. Note: for
          audio-rate signals the delay is 1 audio frame, and for
          control-rate signals the delay is 1 control period." }

   {:name "Delay2" :extends "Delay1"
    :doc "delay input signal by two frames of samples"}

   {:name "DelayN",
    :args [{:name "in"
            :default 0.0,
            :mode :as-ar
            :doc "the input signal"}

           {:name "max-delay-time",
            :default 0.2
            :doc "the maximum delay time in seconds. Used to initialize
                  the delay buffer size"}

           {:name "delay-time"
            :default 0.2
            :doc "delay time in seconds"}]

    :check (nth-input-stream? 0)
    :doc "simple delay line, no interpolation. See also DelayL which
          uses linear interpolation, and DelayC which uses cubic
          interpolation. Cubic interpolation is more computationally
          expensive than linear, but more accurate." }

   {:name "DelayL" :extends "DelayN"
    :doc "simple delay line, linear interpolation."}

   {:name "DelayC" :extends "DelayN"
    :doc "simple delay line, cubic interpolation."}

   {:name "CombN",
    :args [{:name "in",
            :default 0.0,
            :mode :as-ar
            :doc "the input signal"}

           {:name "max-delay-time",
            :default 0.2
            :doc "the maximum delay time in seconds. Used to initialize
                  the delay buffer size"}

           {:name "delay-time",
            :default 0.2
            :doc "delay time in seconds"}

           {:name "decay-time",
            :default 1.0
            :doc "time for the echoes to decay by 60 decibels. If this
                  time is negative then the feedback coefficient will be
                  negative, thus emphasizing only odd harmonics at an
                  octave lower." }]
    :check (nth-input-stream? 0)
    :doc "comb delay line, no interpolation. See also CombL which uses
          linear interpolation, and CombC which uses cubic
          interpolation. Cubic interpolation is more computationally
          expensive than linear, but more accurate." }

   {:name "CombL" :extends "CombN"
    :doc "comb delay line, linear interpolation"}

   {:name "CombC" :extends "CombN"
    :doc "comb delay line, cubic interpolation"}

   {:name "AllpassN" :extends "CombN"
    :doc "all pass delay line, no interpolation. See also AllpassC which
         uses cubic interpolation, and AllpassL which uses linear
         interpolation. Cubic interpolation is more computationally
         expensive than linear, but more accurate." }

   {:name "AllpassL" :extends "CombN"
    :doc "all pass delay line, linear interpolation"}

   {:name "AllpassC" :extends "CombN"
    :doc "all pass delay line, cubic interpolation"}

   {:name "BufDelayN",
    :args [{:name "buf", :default 0.0 :doc "buffer number"}
           {:name "in", :default 0.0 :mode :as-ar :doc "the input signal"}
           {:name "delay-time", :default 0.2 :doc "delay time in seconds"}]
    :check (nth-input-stream? 1)
    :doc "buffer based simple delay line with no interpolation. See also
         BufDelayL which uses linear interpolation, and BufDelayC which
         uses cubic interpolation. Cubic interpolation is more
         computationally expensive than linear, but more accurate." }

   {:name "BufDelayL" :extends "BufDelayN"
    :doc "buffer based simple delay line with linear interpolation"}

   {:name "BufDelayC" :extends "BufDelayN"
    :doc "buffer based simple delay line with cubic interpolation"}

   {:name "BufCombN",
    :args [{:name "buf",
            :default 0
            :doc "buffer number"}

           {:name "in",
            :default 0.0,
            :mode :as-ar :doc "the input signal"}

           {:name "delay-time",
            :default 0.2
            :doc "delay time in seconds"}

           {:name "decay-time",
            :default 1.0
            :doc "time for the echoes to decay by 60 decibels. If this
                  time is negative then the feedback coefficient will be
                  negative, thus emphasizing only odd harmonics at an
                  octave lower." }],
    :rates #{:ar}
    :check (nth-input-stream? 1)
    :doc "buffer based comb delay line with no interpolation. See also
          [BufCombL] which uses linear interpolation, and BufCombC which
          uses cubic interpolation. Cubic interpolation is more
          computationally expensive than linear, but more accurate." }

   {:name "BufCombL" :extends "BufCombN"
    :doc "buffer based comb delay line with linear interpolation"}

   {:name "BufCombC" :extends "BufCombN"
    :doc "buffer based comb delay line with cubic interpolation"}

   {:name "BufAllpassN" :extends "BufCombN"
    :doc "buffer based all pass delay line with no interpolation. See
          also BufAllpassC which uses cubic interpolation, and
          BufAllpassL which uses linear interpolation. Cubic
          interpolation is more computationally expensive than linear,
          but more accurate." }

   {:name "BufAllpassL" :extends "BufCombN"
    :doc "buffer based all pass delay line with linear interpolation"}

   {:name "BufAllpassC" :extends "BufCombN"
    :doc "buffer based all pass delay line with cubic interpolation"}
   ])

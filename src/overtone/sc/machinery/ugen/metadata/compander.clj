(ns overtone.sc.machinery.ugen.metadata.compander
  (:use [overtone.sc.machinery.ugen common]))

(def specs
  [
;;amplitude
      {:name "Amplitude"
       :args [{:name "in"
               :default 0.0
               :modulatable true
               :doc "input signal"}

              {:name "attack-time"
               :default 0.01
               :modulatable false
               :doc "60dB convergence time for following attacks"}

              {:name "release-time"
               :default 0.01
               :modulatable false
               :doc "60dB convergence time for following decays"}]
       :summary "Amplitude follower"
       :doc "Tracks the peak amplitude of a signal."
       :scsynth-location "FilterUgens"
       :sclang-location "Compander"
       :rates #{:ar :kr}
       :auto-rate true}

;;compander
      {:name "Compander",
       :args [{:name "in"
               :default 0.0
               :modulatable true
               :doc "The signal to be compressed / expanded / gated"}

              {:name "control"
               :default 0.0
               :modulatable true
               :doc "The signal whose amplitude determines the gain applied to the input signal. Often the same as in (for standard gating or compression) but should be different for ducking."}

              {:name "thresh"
               :default 0.5
               :modulatable true
               :doc "Control signal amplitude threshold, which determines the break point between slope-below and slope-above. Typically a value between 0 and 1. "}

              {:name "slope-below"
               :default 1.0
               :modulatable true
               :doc "Slope of the amplitude curve below the threshold. A value of 1 means the output amplitude will match the control signal amplitude."}

              {:name "slope-above"
               :default 1.0
               :modulatable true
               :doc "Slope of the amplitude curve above the threshold. A value of 1 means the output amplitude will match the control signal amplitude."}

              {:name "clamp-time"
               :default 0.01
               :modulatable true
               :doc "Time taken for the amplitude adjustment to kick in fully (in seconds). This is usually pretty small, not much more than 10 milliseconds (the default value). Also known as the time of the attack phase."}

              {:name "relax-time"
               :default 0.1
               :modulatable true
               :doc "The amount of time for the amplitude adjustment to be released. Usually a bit longer than clamp-time; if both times are too short, you can get some (possibly unwanted) artifacts. Also known as the time of the release phase."}]
       :summary "General purpose hard-knee dynamic range processor."
       :scsynth-location "FilterUgens"
       :sclang-location "Compander"
       :rates #{:ar}
       :check (first-n-inputs-ar 2)
       :doc "The compander will modify the amplitude of the in signal based on an analysis of the control signal. Typically the in and control signals are the same. The amplitude of the control signal is calcuated using RMS (Root Mean Square) and the final amplitude of the in signal is calculated as a function of the amplitude threshold, and slopes either side (below and above) with some temporal modifications in terms of attack and release phases. It is a hard-knee processor which means that the response curve is a sharp angle rather than a rounded edge.

If the control amplitude is less than the threshold, the slope below is used to calculate the amplitude modification. If this is steep (greater than 1) this will reduce the amplitude of quiet signals (the quieter the control amplitude the greater the reduction affect).  Values < 1.0 are possible, but it means that a very low-level control signal will cause the input signal to be amplified, which would raise the noise floor.

If the control amplitude is greater than the threshold, the slope above is used to calculate the amplitude modification. If this is steep (greater than 1) this will create expansion - loud signals will be made louder). Less than 1 will achieve compressions (louder signals are attenuated).

The clamp and relax times modify when the amplitude modification takes place and ends.

May be used to define: compressers, expanders, limiters, gates and duckers.

For more information see: http://en.wikipedia.org/wiki/Audio_level_compression
"}


;;normalizer
      {:name "Normalizer"
       :args [{:name "in"
               :doc "The input signal"}

              {:name "level"
               :default 1.0
               :doc "The peak output amplitude level to which to normalize the input"}

              {:name "dur"
               :default 0.01
               :doc "The buffer delay time. Shorter times will produce smaller delays and quicker transient response times, but may introduce amplitude modulation artifacts. (AKA lookAheadTime)"}]
       :summary ""
       :rates #{:ar}
       :doc "flattens dynamics. Normalizes the input amplitude to the given level. Normalize will not overshoot
like Compander will, but it needs to look ahead in the audio. Thus there is a
delay equal to twice the lookAheadTime."}

      ;; Limiter : Normalizer {}

      {:name "Limiter", :extends "Normalizer"
       :doc "Limits the input amplitude to the given level. Limiter will not overshoot like Compander will, but it needs to look ahead in the audio. Thus there is a delay equal to twice the lookAheadTime. Limiter, unlike Compander, is completely transparent for an in range signal."}
      ])

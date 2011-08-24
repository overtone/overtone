(ns overtone.sc.ugen.metadata.compander
  (:use [overtone.sc.ugen common constants]))

(def specs
     [
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
       :auto-rate true}

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
               :doc "Control signal amplitude threshold, which determines the break point between slopeBelow and slopeAbove. Usually 0..1. The control signal amplitude is calculated using RMS."}

              {:name "slope-below"
               :default 1.0
               :modulatable true
               :doc "Slope of the amplitude curve below the threshold. If this slope > 1.0, the amplitude will drop off more quickly the softer the control signal gets; when the control signal is close to 0 amplitude, the output should be exactly zero -- hence, noise gating. Values < 1.0 are possible, but it means that a very low-level control signal will cause the input signal to be amplified, which would raise the noise floor."}

              {:name "slope-above"
               :default 1.0
               :modulatable true
               :doc "Same thing, but above the threshold. Values < 1.0 achieve compression (louder signals are attenuated); > 1.0, you get expansion (louder signals are made even louder). For 3:1 compression, you would use a value of 1/3 here."}

              {:name "clamp-time"
               :default 0.01
               :modulatable true
               :doc "The amount of time it takes for the amplitude adjustment to kick in fully. This is usually pretty small, not much more than 10 milliseconds (the default value)."}

              {:name "relax-time"
               :default 0.1
               :modulatable true
               :doc "The amount of time for the amplitude adjustment to be released. Usually a bit longer than clamp-time; if both times are too short, you can get some (possibly unwanted) artifacts."}]
       :summary "General purpose (hard-knee) dynamics processor."
       :scsynth-location "FilterUgens"
       :sclang-location "Compander"
       :rates #{:ar}
       :doc "May be used to define: compressers, expanders, limiters, gates and duckers"}

      ;; Normalizer : UGen {
      ;;   var buffer;
      ;;   *ar { arg in = 0.0, level = 1.0, dur = 0.01;
      ;;     ^this.multiNew('audio', in, level, dur)
      ;;   }
      ;; }

      {:name "Normalizer",
       :args [{:name "in", :doc "The input signal"}
              {:name "level", :default 1.0 :doc "The peak output amplitude level to which to normalize the input"}
              {:name "dur", :default 0.01 :doc "The buffer delay time. Shorter times will produce smaller delays and quicker transient response times, but may introduce amplitude modulation artifacts. (AKA lookAheadTime)"}],
       :rates #{:ar}
       :doc "flattens dynamics. Normalizes the input amplitude to the given level. Normalize will not overshoot
like Compander will, but it needs to look ahead in the audio. Thus there is a
delay equal to twice the lookAheadTime."}

      ;; Limiter : Normalizer {}

      {:name "Limiter", :extends "Normalizer"
       :doc "Limits the input amplitude to the given level. Limiter will not overshoot like Compander will, but it needs to look ahead in the audio. Thus there is a delay equal to twice the lookAheadTime. Limiter, unlike Compander, is completely transparent for an in range signal."}
      ])

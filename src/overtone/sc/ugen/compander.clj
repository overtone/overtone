(ns overtone.sc.ugen.compander
  (:use [overtone.sc.ugen common constants]))

(def specs
     [
      ;; Amplitude : UGen {
      ;;   *ar { arg in = 0.0, attackTime = 0.01, releaseTime = 0.01, mul = 1.0, add = 0.0;
      ;;     ^this.multiNew('audio', in, attackTime, releaseTime).madd(mul, add)
      ;;   }
      ;;   *kr { arg in = 0.0, attackTime = 0.01, releaseTime = 0.01, mul = 1.0, add = 0.0;
      ;;     ^this.multiNew('control', in, attackTime, releaseTime).madd(mul, add)
      ;;   }
      ;;                   }

      {:name "Amplitude",
       :args [{:name "in", :default 0.0 :doc "input signal"}
              {:name "attackTime", :default 0.01 :doc "60dB convergence time for following attacks"}
              {:name "releaseTime", :default 0.01 :doc "60dB convergence time for following decays"}]
       :doc "amplitude follower. Tracks the peak amplitude of a signal."
       :auto-rate true}

      ;; Compander : UGen {
      ;;   *ar { arg in = 0.0, control = 0.0, thresh = 0.5, slopeBelow = 1.0, slopeAbove = 1.0,
      ;;     clampTime = 0.01, relaxTime = 0.1, mul = 1.0, add = 0.0;
      ;;     ^this.multiNew('audio', in, control, thresh, slopeBelow, slopeAbove,
      ;;       clampTime, relaxTime).madd(mul, add)
      ;;   }
      ;; }

      {:name "Compander",
       :args [{:name "in", :default 0.0 :doc "The signal to be compressed / expanded / gated"}
              {:name "control", :default 0.0 :doc "The signal whose amplitude determines the gain applied to the input signal. Often the same as in (for standard gating or compression) but should be different for ducking."}
              {:name "thresh", :default 0.5 :doc "Control signal amplitude threshold, which determines the break point between slopeBelow and slopeAbove. Usually 0..1. The control signal amplitude is calculated using RMS."}
              {:name "slopeBelow", :default 1.0 :doc "Slope of the amplitude curve below the threshold. If this slope > 1.0, the amplitude will drop off more quickly the softer the control signal gets; when the control signal is close to 0 amplitude, the output should be exactly zero -- hence, noise gating. Values < 1.0 are possible, but it means that a very low-level control signal will cause the input signal to be amplified, which would raise the noise floor."}
              {:name "slopeAbove", :default 1.0 :doc "Same thing, but above the threshold. Values < 1.0 achieve compression (louder signals are attenuated); > 1.0, you get expansion (louder signals are made even louder). For 3:1 compression, you would use a value of 1/3 here."}
              {:name "clampTime", :default 0.01 :doc "The amount of time it takes for the amplitude adjustment to kick in fully. This is usually pretty small, not much more than 10 milliseconds (the default value). I often set it as low as 2 milliseconds (0.002)."}
              {:name "relaxTime", :default 0.1 :doc "The amount of time for the amplitude adjustment to be released. Usually a bit longer than clampTime; if both times are too short, you can get some (possibly unwanted) artifacts."}],
       :rates #{:ar}
       :doc "General purpose (hard-knee) dynamics processor: compresser, expander, limiter, gate, ducker"
       :auto-rate true}

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

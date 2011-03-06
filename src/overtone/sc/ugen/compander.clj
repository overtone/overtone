(ns overtone.sc.ugen.compander)

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
       :args [{:name "in", :default 0.0}
              {:name "attackTime", :default 0.01}
              {:name "releaseTime", :default 0.01}]
       :muladd true
       :doc "amplitude follower"}

      ;; Compander : UGen {
      ;;   *ar { arg in = 0.0, control = 0.0, thresh = 0.5, slopeBelow = 1.0, slopeAbove = 1.0,
      ;;     clampTime = 0.01, relaxTime = 0.1, mul = 1.0, add = 0.0;
      ;;     ^this.multiNew('audio', in, control, thresh, slopeBelow, slopeAbove,
      ;;       clampTime, relaxTime).madd(mul, add)
      ;;   }
      ;; }

      {:name "Compander",
       :args [{:name "in", :default 0.0}
              {:name "control", :default 0.0}
              {:name "thresh", :default 0.5}
              {:name "slopeBelow", :default 1.0}
              {:name "slopeAbove", :default 1.0}
              {:name "clampTime", :default 0.01}
              {:name "relaxTime", :default 0.1}],
       :rates #{:ar}
       :muladd true
       :doc "compresser, expander, limiter, gate, ducker"}

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
       :doc "flattens dynamics"}

      ;; Limiter : Normalizer {}

      {:name "Limiter", :extends "Normalizer"
       :doc "Limits the input amplitude to the given level. Limiter will not overshoot like Compander will, but it needs to look ahead in the audio. Thus there is a delay equal to twice the lookAheadTime. Limiter, unlike Compander, is completely transparent for an in range signal."}
      ])

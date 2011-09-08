(ns overtone.sc.ugen.metadata.delay
  (:use [overtone.sc.ugen common constants]))

(def specs

  [
   ;; Delay1 : UGen {
   ;; 	*ar { arg in = 0.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('audio', in).madd(mul, add)
   ;; 	}
   ;; 	*kr { arg in = 0.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('control', in).madd(mul, add)
   ;; 	}
   ;; }

   {:name "Delay1",
    :args [{:name "in", :default 0.0 :doc "input to be delayed."}]
    :doc "delay input signal by one frame of samples. Note: for audio-rate signals the delay is 1 audio frame, and for control-rate signals the delay is 1 control period."}

   ;; Delay2 : Delay1 { }

   {:name "Delay2" :extends "Delay1"
    :doc "delay input signal by two frames of samples"}

   ;; these delays use real time allocated memory.

   ;; DelayN : UGen {
   ;; 	*ar { arg in = 0.0, maxdelaytime = 0.2, delaytime = 0.2, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('audio', in.asAudioRateInput, maxdelaytime, delaytime).madd(mul, add)
   ;; 	}
   ;; 	*kr { arg in = 0.0, maxdelaytime = 0.2, delaytime = 0.2, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('control', in, maxdelaytime, delaytime).madd(mul, add)
   ;; 	}
   ;; }

   {:name "DelayN",
    :args [{:name "in", :default 0.0, :mode :as-ar :doc "the input signal"}
           {:name "maxdelaytime", :default 0.2 :doc "the maximum delay time in seconds. used to initialize the delay buffer size"}
           {:name "delaytime", :default 0.2 :doc "delay time in seconds"}]
    :doc "simple delay line, no interpolation. See also DelayL which uses linear interpolation, and DelayC which uses cubic interpolation. Cubic interpolation is more computationally expensive than linear, but more accurate."}

   ;; DelayL : DelayN {}

   {:name "DelayL" :extends "DelayN"
    :doc "simple delay line, linear interpolation."}

   ;; DelayC : DelayN {}

   {:name "DelayC" :extends "DelayN"
    :doc "simple delay line, cubic interpolation."}

   ;; CombN : UGen {
   ;;   *ar { arg in = 0.0, maxdelaytime = 0.2, delaytime = 0.2, decaytime = 1.0, mul = 1.0, add = 0.0;
   ;;     ^this.multiNew('audio', in.asAudioRateInput, maxdelaytime, delaytime, decaytime).madd(mul, add)
   ;;   }
   ;;   *kr { arg in = 0.0, maxdelaytime = 0.2, delaytime = 0.2, decaytime = 1.0, mul = 1.0, add = 0.0;
   ;;     ^this.multiNew('control', in, maxdelaytime, delaytime, decaytime).madd(mul, add)
   ;;   }
   ;; }

   {:name "CombN",
    :args [{:name "in", :default 0.0, :mode :as-ar :doc "the input signal"}
           {:name "maxdelaytime", :default 0.2 :doc "the maximum delay time in seconds. used to initialize the delay buffer size"}
           {:name "delaytime", :default 0.2 :doc "delay time in seconds"}
           {:name "decaytime", :default 1.0 :doc "time for the echoes to decay by 60 decibels. If this time is negative then the feedback coefficient will be negative, thus emphasizing only odd harmonics at an octave lower."}]
    :doc "comb delay line, no interpolation. See also CombL which uses linear interpolation, and CombC which uses cubic interpolation. Cubic interpolation is more computationally expensive than linear, but more accurate."}

   {:name "CombL" :extends "CombN"
    :doc "comb delay line, linear interpolation"}

   {:name "CombC" :extends "CombN"
    :doc "comb delay line, cubic interpolation"}

   {:name "AllpassN" :extends "CombN"
    :doc "all pass delay line, no interpolation. See also AllpassC which uses cubic interpolation, and AllpassL which uses linear interpolation. Cubic interpolation is more computationally expensive than linear, but more accurate."}

   {:name "AllpassL" :extends "CombN"
    :doc "all pass delay line, linear interpolation"}

   {:name "AllpassC" :extends "CombN"
    :doc "all pass delay line, cubic interpolation"}

   ;; these delays use shared buffers.

   ;; BufDelayN : UGen {
   ;; 	*ar { arg buf = 0, in = 0.0, delaytime = 0.2, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('audio', buf, in.asAudioRateInput, delaytime).madd(mul, add)
   ;; 	}
   ;; 	*kr { arg buf = 0, in = 0.0, delaytime = 0.2, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('control', buf, in, delaytime).madd(mul, add)
   ;; 	}
   ;; }

   {:name "BufDelayN",
    :args [{:name "buf", :default 0.0 :doc "buffer number"}
           {:name "in", :default 0.0 :mode :as-ar :doc "the input signal"}
           {:name "delaytime", :default 0.2 :doc "delay time in seconds"}]
    :doc "buffer based simple delay line with no interpolation. See also BufDelayL which uses linear interpolation, and BufDelayC which uses cubic interpolation. Cubic interpolation is more computationally expensive than linear, but more accurate."}

   ;; BufDelayL : BufDelayN {}

   {:name "BufDelayL" :extends "BufDelayN"
    :doc "buffer based simple delay line with linear interpolation"}

   ;; BufDelayC : BufDelayN {}

   {:name "BufDelayC" :extends "BufDelayN"
    :doc "buffer based simple delay line with cubic interpolation"}

   ;; BufCombN : UGen {
   ;; 	*ar { arg buf = 0, in = 0.0, delaytime = 0.2, decaytime = 1.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('audio', buf, in.asAudioRateInput, delaytime, decaytime).madd(mul, add)
   ;; 	}
   ;; }

   {:name "BufCombN",
    :args [{:name "buf", :default 0 :doc "buffer number"}
           {:name "in", :default 0.0, :mode :as-ar :doc "the input signal"}
           {:name "delaytime", :default 0.2 :doc "delay time in seconds"}
           {:name "decaytime", :default 1.0 :doc "time for the echoes to decay by 60 decibels. If this time is negative then the feedback coefficient will be negative, thus emphasizing only odd harmonics at an octave lower."}],
    :rates #{:ar}
    :doc "buffer based comb delay line with no interpolation. See also [BufCombL] which uses linear interpolation, and BufCombC which uses cubic interpolation. Cubic interpolation is more computationally expensive than linear, but more accurate."}

   ;; BufCombL : BufCombN {}

   {:name "BufCombL" :extends "BufCombN"
    :doc "buffer based comb delay line with linear interpolation"}

   ;; BufCombC : BufCombN {}

   {:name "BufCombC" :extends "BufCombN"
    :doc "buffer based comb delay line with cubic interpolation"}

   ;; BufAllpassN : BufCombN {}

   {:name "BufAllpassN" :extends "BufCombN"
    :doc "buffer based all pass delay line with no interpolation. See also BufAllpassC which uses cubic interpolation, and BufAllpassL which uses linear interpolation. Cubic interpolation is more computationally expensive than linear, but more accurate."}

   ;; BufAllpassL : BufCombN {}

   {:name "BufAllpassL" :extends "BufCombN"
    :doc "buffer based all pass delay line with linear interpolation"}

   ;; BufAllpassC : BufCombN {}

   {:name "BufAllpassC" :extends "BufCombN"
    :doc "buffer based all pass delay line with cubic interpolation"}
   ])

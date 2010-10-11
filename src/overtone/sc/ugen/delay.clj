(ns overtone.sc.ugen.delay)

(def specs
     (map
      #(assoc % :muladd true)
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
        :args [{:name "in", :default 0.0}]
        :doc "delay input signal by one frame of samples"}

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
        :args [{:name "in", :default 0.0, :mode :as-ar}
               {:name "maxdelaytime", :default 0.2}
               {:name "delaytime", :default 0.2}]
        :doc "simple delay line, no interpolation."}

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
        :args [{:name "in", :default 0.0, :mode :as-ar}
               {:name "maxdelaytime", :default 0.2}
               {:name "delaytime", :default 0.2}
               {:name "decaytime", :default 1.0}]
        :doc "comb delay line, no interpolation"}

       {:name "CombL" :extends "CombN"
        :doc "comb delay line, linear interpolation"}

       {:name "CombC" :extends "CombN"
        :doc "comb delay line, cubic interpolation"}

       {:name "AllpassN" :extends "CombN"
       :doc "all pass delay line, no interpolation"}

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
        :args [{:name "buf", :default 0.0}
               {:name "in", :default 0.0 :mode :as-ar}
               {:name "delaytime", :default 0.2}]
        :doc "buffer based simple delay line with no interpolation"}

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
        :args [{:name "buf", :default 0}
               {:name "in", :default 0.0, :mode :as-ar}
               {:name "delaytime", :default 0.2}
               {:name "decaytime", :default 1.0}],
        :rates #{:ar}
        :doc "buffer based comb delay line with no interpolation"}

       ;; BufCombL : BufCombN {}

       {:name "BufCombL" :extends "BufCombN"
        :doc "buffer based comb delay line with linear interpolation"}

       ;; BufCombC : BufCombN {}

       {:name "BufCombC" :extends "BufCombN"
        :doc "buffer based comb delay line with cubic interpolation"}

       ;; BufAllpassN : BufCombN {}

       {:name "BufAllpassN" :extends "BufCombN"
        :doc "buffer based all pass delay line with no interpolation"}

       ;; BufAllpassL : BufCombN {}

       {:name "BufAllpassL" :extends "BufCombN"
        :doc "buffer based all pass delay line with linear interpolation"}

       ;; BufAllpassC : BufCombN {}

       {:name "BufAllpassC" :extends "BufCombN"
        :doc "buffer based all pass delay line with cubic interpolation"}
       ]))

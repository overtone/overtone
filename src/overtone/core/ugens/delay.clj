
(ns overtone.core.ugens.delay)

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
        :args [{:name "in", :default 0.0}]}
       
       ;; Delay2 : Delay1 { }

       {:name "Delay2" :extends "Delay1"}

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
               {:name "delaytime", :default 0.2}]}

       ;; DelayL : DelayN {}

       {:name "DelayL" :extends "DelayN"}
       
       ;; DelayC : DelayN {}

       {:name "DelayC" :extends "DelayN"}
       
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
               {:name "decaytime", :default 1.0}]}
       
       {:name "CombL" :extends "CombN"}
       {:name "CombC" :extends "CombN"}

       {:name "AllpassN" :extends "CombN"}
       {:name "AllpassL" :extends "CombN"}
       {:name "AllpassC" :extends "CombN"}       

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
               {:name "delaytime", :default 0.2}]}
       
       ;; BufDelayL : BufDelayN {}

       {:name "BufDelayL" :extends "BufDelayN"}
       
       ;; BufDelayC : BufDelayN {}

       {:name "BufDelayC" :extends "BufDelayN"}

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
        :rates #{:ar}}
       
       ;; BufCombL : BufCombN {}

       {:name "BufCombL" :extends "BufCombN"}
       
       ;; BufCombC : BufCombN {}

       {:name "BufCombC" :extends "BufCombN"}       

       ;; BufAllpassN : BufCombN {}

       {:name "BufAllpassN" :extends "BufCombN"}
       
       ;; BufAllpassL : BufCombN {}

       {:name "BufAllpassL" :extends "BufCombN"}
       
       ;; BufAllpassC : BufCombN {}

       {:name "BufAllpassC" :extends "BufCombN"}]))

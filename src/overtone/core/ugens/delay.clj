
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

       {:name "Delay2" :derived "Delay1"}

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

       {:name "DelayL" :derived "DelayN"}
       
       ;; DelayC : DelayN {}

       {:name "DelayC" :derived "DelayN"}
       
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
       
       ;; CombL : CombN {}

       {:name "CombL" :derived "ConmbN"}
       
       ;; CombC : CombN {}

       {:name "CombC" :derived "ConmbN"}

       ;; AllpassN : CombN {}

       {:name "AllpassN" :derived "ConmbN"}
       
       ;; AllpassL : CombN {}

       {:name "AllpassL" :derived "ConmbN"}
       
       ;; AllpassC : CombN {}

       {:name "AllpassC" :derived "ConmbN"}       

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

       {:name "BufDelayL" :derived "BufDelayN"}
       
       ;; BufDelayC : BufDelayN {}

       {:name "BufDelayC" :derived "BufDelayN"}

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

       {:name "BufCombL" :derived "BufCombN"}
       
       ;; BufCombC : BufCombN {}

       {:name "BufCombC" :derived "BufCombN"}       

       ;; BufAllpassN : BufCombN {}

       {:name "BufAllpassN" :derived "BufCombN"}
       
       ;; BufAllpassL : BufCombN {}

       {:name "BufAllpassL" :derived "BufCombN"}
       
       ;; BufAllpassC : BufCombN {}

       {:name "BufAllpassC" :derived "BufCombN"}]))

(ns overtone.core.ugens.ff-osc)

;; ugens in this file are fromm FSinOsc.sc except PSinGrain

(def specs
     [

      ;; FSinOsc : UGen { 
      ;;  *ar { arg freq=440.0, iphase = 0.0, mul = 1.0, add = 0.0;
      ;;    ^this.multiNew('audio', freq, iphase).madd(mul, add)
      ;;  }
      ;;  *kr { arg freq=440.0, iphase = 0.0, mul = 1.0, add = 0.0;
      ;;    ^this.multiNew('control', freq, iphase).madd(mul, add)
      ;;  }
      ;; }

      {:name "FSinOsc",
       :args [{:name "freq", :default 440.0}
              {:name "iphase", :default 0.0}]
       :muladd true}
      
      ;; Klang : UGen {
      ;;  *ar { arg specificationsArrayRef, freqscale = 1.0, freqoffset = 0.0;
      ;;      ^this.multiNewList(['audio', freqscale, 
      ;;            freqoffset, specificationsArrayRef] )
      ;;  }
      ;;  *new1 { arg rate, freqscale, freqoffset, arrayRef;
      ;;    var specs, freqs, amps, phases;
      ;;    # freqs, amps, phases = arrayRef.dereference;
      ;;    specs = [freqs, 
      ;;        amps ?? {Array.fill(freqs.size,1.0)}, 
      ;;        phases ?? {Array.fill(freqs.size,0.0)}
      ;;        ].flop.flat;
      ;;    ^super.new.rate_(rate).addToSynth.init([freqscale,freqoffset] ++ specs); 
      ;;  }
      ;;    init { arg theInputs;
      ;;      // store the inputs as an array
      ;;      inputs = theInputs;
      ;;    }
      ;;  argNamesInputsOffset { ^2 }
      ;; }

      {:name "Klang",
       :args [{:name "specs", :mode :not-expanded}
              {:name "freqscale", :default 1.0}
              {:name "freqoffset", :default 0.0}],
       :rates #{:ar}
       :init (fn [rate [specs & args]]
               (let [[freqs amps phases] specs
                     amps (or amps (repeat 1.0))
                     phases (or phases (repeat 0.0))
                     faps (map vector freqs amps phases)]
                 (apply concat args faps)))
       :doc "(klang:ar [[440 880][1 0.9]] 1.1)"}
      
      ;; Klank : UGen { 
      ;;  *ar { arg specificationsArrayRef, input, freqscale = 1.0, freqoffset = 0.0, decayscale = 1.0;
      ;;      ^this.multiNewList(['audio',  input, freqscale, 
      ;;            freqoffset, decayscale, specificationsArrayRef] )
      ;;  }
      ;;  *new1 { arg rate, input, freqscale, freqoffset, decayscale, arrayRef;
      ;;    var specs, freqs, amps, times;
      ;;    # freqs, amps, times = arrayRef.dereference;
      ;;    specs = [freqs, 
      ;;        amps ?? {Array.fill(freqs.size,1.0)}, 
      ;;        times ?? {Array.fill(freqs.size,1.0)}
      ;;        ].flop.flat;
      ;;    ^super.new.rate_(rate).addToSynth.init([input,freqscale,freqoffset,decayscale] ++ specs); 
      ;;  }
      ;;    init { arg theInputs;
      ;;      // store the inputs as an array
      ;;      inputs = theInputs;
      ;;    }
      ;;  argNamesInputsOffset { ^2 }
      ;; }

      {:name "Klank",
       :args [{:name "specs", :mode :not-expanded}
              {:name "input"}
              {:name "freqscale", :default 1.0}
              {:name "freqoffset", :default 0.0}
              {:name "decayscale", :default 1.0}],
       :rates #{:ar}
       :init (fn [rate [specs & args]]
               (let [[freqs amps times] specs
                     amps (or amps (repeat 1.0))
                     times (or times (repeat 1.0))
                     fats (map vector freqs amps times)]
                 (apply concat args fats)))}

      ;; Blip : UGen {  
      ;;  *ar { arg freq=440.0, numharm = 200.0, mul = 1.0, add = 0.0;
      ;;    ^this.multiNew('audio', freq, numharm).madd(mul, add)
      ;;  }
      ;; }

      {:name "Blip",
       :args [{:name "freq", :default 440.0}
              {:name "numharm", :default 200.0}],
       :rates #{:ar}
       :muladd true}
      
      ;; Saw : UGen { 
      ;;  *ar { arg freq=440.0, mul = 1.0, add = 0.0;
      ;;    ^this.multiNew('audio', freq).madd(mul, add)
      ;;  }
      ;; }

      {:name "Saw",
       :args [{:name "freq", :default 440.0}],
       :rates #{:ar}
       :muladd true}
      
      ;; Pulse : UGen { 
      ;;  *ar { arg freq=440.0, width = 0.5, mul = 1.0, add = 0.0;
      ;;    ^this.multiNew('audio', freq, width).madd(mul, add)
      ;;  }
      ;; }

      {:name "Pulse",
       :args [{:name "freq", :default 440.0}
              {:name "width", :default 0.5}],
       :rates #{:ar}
       :muladd true}

      ;; from PSinGrain.sc
      ;; 	fixed frequency sine oscillator
      ;; 	arguments :
      ;; 		freq - frequency in cycles per second. Must be a scalar.
      ;; 		dur - grain duration
      ;; 		amp - amplitude of grain
      ;; 	This unit generator uses a very fast algorithm for generating a sine
      ;; 	wave at a fixed frequency.
      ;; PSinGrain : UGen {	
      ;; 	*ar { arg freq = 440.0, dur = 0.2, amp = 1.0;
      ;; 		^this.multiNew('audio', freq, dur, amp)
      ;; 	}
      ;; }
      
      {:name "PSinGrain",
       :args [{:name "freq", :default 440.0}
              {:name "dur", :default 0.2}
              {:name "amp", :default 1.0}],
       :rates #{:ar}}])
(ns overtone.core.ugen.ff-osc)

(def specs
     [
      {:name "FSinOsc",
       :args [{:name "freq", :default 440.0}
              {:name "iphase", :default 0.0}]
       :doc "very fast sine wave generator"}

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
       :doc "a bank of fixed frequency sine oscillators
            (more efficient than multiple sin-osc)"}

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
                 (apply concat args fats)))
       :doc ""}

      {:name "Blip",
       :args [{:name "freq", :default 440.0}
              {:name "numharm", :default 200.0}],
       :rates #{:ar}
       :doc "band Limited ImPulse generator"}

      {:name "Saw",
       :args [{:name "freq", :default 440.0}],
       :rates #{:ar}
       :doc "band limited sawtooth wave generator"}

      {:name "Pulse",
       :args [{:name "freq", :default 440.0}
              {:name "width", :default 0.5}],
       :rates #{:ar}
       :doc "band limited pulse wave generator with pulse width modulation"}

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

(ns overtone.sc.ugen.ff-osc)

(def specs
     [
      {:name "FSinOsc",
       :args [{:name "freq", :default 440.0 :doc "frequency in Hertz"}
              {:name "iphase", :default 0.0 :doc "phase offset or modulator in radians"}]
       :doc "Very fast sine wave generator (2 PowerPC instructions per output sample!) implemented using a ringing filter.  This generates a much cleane sine wave than a table lookup oscillator and is a lot faster. However, the amplitude of the wave will vary with frequency. Generally the amplitude will go down as you raise the frequency and go up as you lower the frequency.

  WARNING: In the current implementation, the amplitude can blow up if the frequency is modulated by certain alternating signals."}

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
       :args [{:name "specs", :mode :not-expanded :doc "An array of three arrays frequencies, amplitudes and phases:  1) an array of filter frequencies, 2) an Array of filter amplitudes, or nil. If nil, then amplitudes default to 1.0, 3) an Array of initial phases, or nil. If nil, then phases default to 0.0."}
              {:name "freqscale", :default 1.0 :doc "a scale factor multiplied by all frequencies at initialization time."}
              {:name "freqoffset", :default 0.0 :doc "an offset added to all frequencies at initialization time."}],
       :rates #{:ar}
       :init (fn [rate [specs & args]]
               (let [[freqs amps phases] specs
                     amps (or amps (repeat 1.0))
                     phases (or phases (repeat 0.0))
                     faps (map vector freqs amps phases)]
                 (apply concat args faps)))
       :doc "Klang is a bank of fixed frequency sine oscillators. Klang is more efficient than creating individual oscillators but offers less flexibility.

  The parameters in specificationsArrayRef can't be changed after it has been started.
  For a modulatable but less efficient version, see dyn-klang."}

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
       :args [{:name "specs", :mode :not-expanded :doc "An array of three arrays: frequencies, amplitudes and ring times: *all arrays should have the same length*  1) an Array of filter frequencies. 2)  an Array of filter amplitudes, or nil. If nil, then amplitudes default to 1.0 3) an Array of 60 dB decay times for the filters."}
              {:name "input" :doc "the excitation input to the resonant filter bank."}
              {:name "freqscale", :default 1.0 :doc "a scale factor multiplied by all frequencies at initialization time."}
              {:name "freqoffset", :default 0.0 :doc "an offset added to all frequencies at initialization time."}
              {:name "decayscale", :default 1.0 :doc "a scale factor multiplied by all ring times at initialization time."}],
       :rates #{:ar}
       :init (fn [rate [specs & args]]
               (let [[freqs amps times] specs
                     amps               (or amps (repeat 1.0))
                     times              (or times (repeat 1.0))
                     fats               (map vector freqs amps times)]
                 (apply concat args fats)))
       :doc "Klank is a bank of fixed frequency resonators which can be used to simulate the resonant modes of an object. Each mode is given a ring time, which is the time for the mode to decay by 60 dB.

  The parameters in specificationsArrayRef can't be changed after it has been started.
  For a modulatable but less efficient version, see dyn-klank."}

      {:name "Blip",
       :args [{:name "freq", :default 440.0 :doc "Frequency in Hertz (control rate)"}
              {:name "numharm", :default 200.0 :doc "Number of harmonics. This may be lowered internally if it would cause aliasing."}],
       :rates #{:ar}
       :doc "Band Limited ImPulse generator. All harmonics have equal amplitude. This is the equivalent of buzz in MusicN languages.
  WARNING: This waveform in its raw form could be damaging to your ears at high amplitudes or for long periods.

  It is improved from other implementations in that it will crossfade in a control period when the number of harmonics changes, so that there are no audible pops. It also eliminates the divide in the formula by using a 1/sin table (with special precautions taken for 1/0).  The lookup tables are linearly interpolated for better quality.

  Synth-O-Matic (1990) had an impulse generator called blip, hence that name here rather than 'buzz'."}


      {:name "Saw",
       :args [{:name "freq", :default 440.0 :doc "Frequency in Hertz (control rate)."}],
       :rates #{:ar}
       :doc "band limited sawtooth wave generator"}

      {:name "Pulse",
       :args [{:name "freq", :default 440.0, :doc "Frequency in Hertz (control rate)"}
              {:name "width", :default 0.5, :doc "Pulse width ratio from zero to one. 0.5 makes a square wave (control rate)"}],
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
       :rates #{:ar}}
      ])

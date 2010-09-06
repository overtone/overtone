(ns overtone.core.ugen.osc
  (:use (overtone.core.ugen common)))

(def specs
     [
      {:name "Osc",
       :args [{:name "bufnum"}
              {:name "freq", :default 440.0}
              {:name "phase", :default 0.0}],
       :muladd true
       :doc "wavetable oscillator"}

      {:name "SinOsc",
       :args [{:name "freq", :default 440.0}
              {:name "phase", :default 0.0}],
       :muladd true
       :doc "sine table lookup oscillator"}

      {:name "SinOscFB",
       :args [{:name "freq", :default 440.0}
              {:name "feedback", :default 0.0}],
       :muladd true
       :doc "very fast sine oscillator"}

      {:name "OscN",
       :args [{:name "bufnum"}
              {:name "freq", :default 440.0}
              {:name "phase", :default 0.0}],
       :muladd true
       :doc "noninterpolating wavetable oscillator"}

      {:name "VOsc",
       :args [{:name "bufpos"}
              {:name "freq", :default 440.0}
              {:name "phase", :default 0.0}],
       :muladd true
       :doc "a variable wavetable lookup oscillator which can be swept smoothly across wavetables"}

      {:name "VOsc3",
       :args [{:name "bufpos"}
              {:name "freq1", :default 110.0}
              {:name "freq2", :default 220.0}
              {:name "freq3", :default 440.0}],
       :muladd true
       :doc "three variable wavetable oscillators"}

      {:name "COsc",
       :args [{:name "bufnum"}
              {:name "freq", :default 440.0}
              {:name "beats", :default 0.5}],
       :muladd true
       :doc "a chorusing wavetable lookup oscillator that produces the sum of two signals at (freq +/- (beats / 2))"}

      {:name "Formant",
       :args [{:name "fundfreq", :default 440.0}
              {:name "formfreq", :default 1760.0}
              {:name "bwfreq", :default 880.0}],
       :rates #{:ar},
       :muladd true
       :doc "generates a set of harmonics around a formant frequency at a given fundamental frequency"}

      {:name "LFSaw",
       :args [{:name "freq", :default 440.0}
              {:name "iphase", :default 0.0}]
       :muladd true
       :doc "low freq (i.e. not band limited) sawtooth oscillator"}

      {:name "LFPar" :extends "LFSaw"
       :doc "a non band-limited parabolic oscillator outputing a high of 1 and a low of zero."}

      {:name "LFCub" :extends "LFSaw"
       :doc "an oscillator outputting a sine like shape made of two cubic pieces"}

      {:name "LFTri" :extends "LFSaw"
       :doc "a non-band-limited triangle oscillator"}

      ;; LFGauss : UGen {
      ;; 	*ar {
      ;; 		arg duration = 1, width = 0.1, iphase = 0.0, loop = 1, doneAction = 0;
      ;; 		^this.multiNew('audio', duration, width, iphase, loop, doneAction)
      ;; 	}
      ;; 	*kr {
      ;; 		arg duration = 1, width = 0.1, iphase = 0.0, loop = 1, doneAction = 0;
      ;; 		^this.multiNew('control', duration, width, iphase, loop, doneAction)
      ;; 	}
      ;; 	range { arg min = 0, max = 1;
      ;; 		^this.linlin(this.minval, 1, min, max)
      ;; 	}
      ;; 	minval {
      ;; 		var width = inputs[1];
      ;; 		^exp(1.0 / (-2.0 * squared(width)))
      ;; 	}
      ;; }

      {:name "LFGauss",
       :args [{:name "duration", :default 1}
              {:name "width", :default 0.1}
              {:name "iphase", :default 0.0}
              {:name "loop", :default 1}
              {:name "doneAction", :default 0 :map DONE-ACTIONS}]
       :doc "a non-band-limited gaussian function oscillator"}

      ;; LFPulse : UGen {
      ;; 	*ar {
      ;; 		arg freq = 440.0, iphase = 0.0, width = 0.5, mul = 1.0, add = 0.0;
      ;; 		^this.multiNew('audio', freq, iphase, width).madd(mul, add)
      ;; 	}
      ;; 	*kr {
      ;; 		arg freq = 440.0, iphase = 0.0, width = 0.5, mul = 1.0, add = 0.0;
      ;; 		^this.multiNew('control', freq, iphase, width).madd(mul, add)
      ;; 	}
      ;; 	signalRange { ^\unipolar }
      ;; }

      {:name "LFPulse",
       :args [{:name "freq", :default 440.0}
              {:name "iphase", :default 0.0}
              {:name "width", :default 0.5}]
       :muladd true
       :signal-range :unipolar
       :doc "low freq (i.e. not band limited) pulse wave oscillator"}

      {:name "VarSaw",
       :args [{:name "freq", :default 440.0}
              {:name "iphase", :default 0.0}
              {:name "width", :default 0.5}]
       :muladd true
       :doc "a variable duty cycle saw wave oscillator"}

      ;; Impulse : UGen {
      ;; 	*ar {
      ;; 		arg freq = 440.0, phase = 0.0, mul = 1.0, add = 0.0;
      ;; 		^this.multiNew('audio', freq, phase).madd(mul, add)
      ;; 	}
      ;; 	*kr {
      ;; 		arg freq = 440.0, phase = 0.0, mul = 1.0, add = 0.0;
      ;; 		^this.multiNew('control', freq, phase).madd(mul, add)
      ;; 	}
      ;; 	signalRange { ^\unipolar }
      ;; }

      {:name "Impulse",
       :args [{:name "freq", :default 440.0}
              {:name "phase", :default 0.0}]
       :muladd true
       :signal-range :unipolar
       :doc "non band limited impulse oscillator"}

      ;; SyncSaw : UGen {
      ;; 	*ar {
      ;; 		arg syncFreq = 440.0, sawFreq = 440.0, mul = 1.0, add = 0.0;
      ;; 		^this.multiNew('audio', syncFreq, sawFreq).madd(mul, add)
      ;; 	}
      ;; 	*kr {
      ;; 		arg syncFreq = 440.0, sawFreq = 440.0, mul = 1.0, add = 0.0;
      ;; 		^this.multiNew('control', syncFreq, sawFreq).madd(mul, add)
      ;; 	}
      ;; }

      {:name "SyncSaw",
       :args [{:name "syncFreq", :default 440.0}
              {:name "sawFreq", :default 440.0}]
       :muladd true
       :doc "hard sync sawtooth wave oscillator"}

      {:name "WrapIndex" :extends "Index"
       :doc "the input signal value is truncated to an integer value and used as an index into the table
            (out of range index values are wrapped)"}

      {:name "IndexInBetween" :extends "Index"
       :doc "finds the (lowest) point in the buffer at which the input signal lies in-between the two values, and returns the index"}

      {:name "DetectIndex" :extends "Index"
       :doc ""}

      {:name "Shaper" :extends "Index"
       :doc "performs waveshaping on the input signal by indexing into a table"}

      {:name "DegreeToKey",
       :args [{:name "bufnum"}
              {:name "in", :default 0.0}
              {:name "octave", :default 12.0}]
       :muladd true
       :doc "the input signal value is truncated to an integer value and used as an index into an octave repeating table of note values
            (indices wrap around the table)"}

      ;; Select : UGen {
      ;; 	*ar {
      ;; 		arg which, array;
      ;; 		^this.multiNewList(['audio', which] ++ array)
      ;; 	}
      ;; 	*kr {
      ;; 		arg which, array;
      ;; 		^this.multiNewList(['control', which] ++ array)
      ;; 	}
      ;;  	checkInputs {
      ;;  		if (rate == 'audio', {
      ;;  			for(1, inputs.size - 1, { arg i;
      ;;  				if (inputs.at(i).rate != 'audio', {
      ;;  					^("input was not audio rate: " + inputs.at(i));
      ;;  				});
      ;;  			});
      ;;  		});
      ;;  		^this.checkValidInputs
      ;;  	}
      ;; }

      {:name "Select",
       :args [{:name "which"}
              {:name "array", :array true}]
       :doc "select the output signal from an array of inputs"}

      {:name "Vibrato",
       :args [{:name "freq", :default 440.0}
              {:name "rate", :default 6}
              {:name "depth", :default 0.02}
              {:name "delay", :default 0.0}
              {:name "onset", :default 0.0}
              {:name "rateVariation", :default 0.04}
              {:name "depthVariation", :default 0.1}
              {:name "iphase", :default 0.0}]
       :doc ""}
      ])

(def specs-collide
  [{:name "Index",
       :args [{:name "bufnum"}
              {:name "in", :default 0.0}]
       :muladd true
       :doc "the input signal value is truncated to an integer and used as an index into the table"}])



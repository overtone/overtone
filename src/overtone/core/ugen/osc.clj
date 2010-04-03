(ns overtone.core.ugen.osc
  (:use (overtone.core.ugen common)))

(def specs
     [

      ;; Osc : UGen {	
      ;; 	*ar { 
      ;; 		arg bufnum, freq=440.0, phase=0.0, mul=1.0, add=0.0;
      ;; 		^this.multiNew('audio', bufnum, freq, phase).madd(mul, add)
      ;; 	}
      ;; 	*kr {
      ;; 		arg bufnum, freq=440.0, phase=0.0, mul=1.0, add=0.0;
      ;; 		^this.multiNew('control', bufnum, freq, phase).madd(mul, add)
      ;; 	}
      ;; }

      {:name "Osc",
       :args [{:name "bufnum"}
              {:name "freq", :default 440.0}
              {:name "phase", :default 0.0}],
       :muladd true
       :doc "wavetable oscillator"}

      ;; SinOsc : UGen {	
      ;; 	*ar { 
      ;; 		arg freq=440.0, phase=0.0, mul=1.0, add=0.0;
      ;; 		^this.multiNew('audio', freq, phase).madd(mul, add)
      ;; 	}
      ;; 	*kr {
      ;; 		arg freq=440.0, phase=0.0, mul=1.0, add=0.0;
      ;; 		^this.multiNew('control', freq, phase).madd(mul, add)
      ;; 	}
      ;; }

      {:name "SinOsc",
       :args [{:name "freq", :default 440.0}
              {:name "phase", :default 0.0}],
       :muladd true
       :doc "sine table lookup oscillator"}

      ;; SinOscFB : UGen {	
      ;; 	*ar { 
      ;; 		arg freq=440.0, feedback=0.0, mul=1.0, add=0.0;
      ;; 		^this.multiNew('audio', freq, feedback).madd(mul, add)
      ;; 	}
      ;; 	*kr {
      ;; 		arg freq=440.0, feedback=0.0, mul=1.0, add=0.0;
      ;; 		^this.multiNew('control', freq, feedback).madd(mul, add)
      ;; 	}
      ;; }

      {:name "SinOscFB",
       :args [{:name "freq", :default 440.0}
              {:name "feedback", :default 0.0}],
       :muladd true
       :doc "very fast sine oscillator"}
      
      ;; OscN : UGen {	
      ;; 	*ar { 
      ;; 		arg bufnum, freq=440.0, phase=0.0, mul=1.0, add=0.0;
      ;; 		^this.multiNew('audio', bufnum, freq, phase).madd(mul, add)
      ;; 	}
      ;; 	*kr {
      ;; 		arg bufnum, freq=440.0, phase=0.0, mul=1.0, add=0.0;
      ;; 		^this.multiNew('control', bufnum, freq, phase).madd(mul, add)
      ;; 	}
      ;; }

      {:name "OscN",
       :args [{:name "bufnum"}
              {:name "freq", :default 440.0}
              {:name "phase", :default 0.0}],
       :muladd true
       :doc "noninterpolating wavetable oscillator"}
      
      ;; VOsc : UGen {	
      ;; 	*ar { 
      ;; 		arg bufpos, freq=440.0, phase=0.0, mul=1.0, add=0.0;
      ;; 		^this.multiNew('audio', bufpos, freq, phase).madd(mul, add)
      ;; 	}
      ;; 	*kr {
      ;; 		arg bufpos, freq=440.0, phase=0.0, mul=1.0, add=0.0;
      ;; 		^this.multiNew('control', bufpos, freq, phase).madd(mul, add)
      ;; 	}
      ;; }

      {:name "VOsc",
       :args [{:name "bufpos"}
              {:name "freq", :default 440.0}
              {:name "phase", :default 0.0}],
       :muladd true}
      
      ;; VOsc3 : UGen {	
      ;; 	*ar { 
      ;; 		arg bufpos, freq1=110.0, freq2=220.0, freq3=440.0, mul=1.0, add=0.0;
      ;; 		^this.multiNew('audio', bufpos, freq1, freq2, freq3).madd(mul, add)
      ;; 	}
      ;; 	*kr {
      ;; 		arg bufpos, freq1=110.0, freq2=220.0, freq3=440.0, mul=1.0, add=0.0;
      ;; 		^this.multiNew('control', bufpos, freq1, freq2, freq3).madd(mul, add)
      ;; 	}
      ;; }

      {:name "VOsc3",
       :args [{:name "bufpos"}
              {:name "freq1", :default 110.0}
              {:name "freq2", :default 220.0}
              {:name "freq3", :default 440.0}],
       :muladd true}
      
      ;; COsc : UGen {	
      ;; 	*ar { 
      ;; 		arg bufnum, freq=440.0, beats=0.5, mul=1.0, add=0.0;
      ;; 		^this.multiNew('audio', bufnum, freq, beats).madd(mul, add)
      ;; 	}
      ;; 	*kr {
      ;; 		arg bufnum, freq=440.0, beats=0.5, mul=1.0, add=0.0;
      ;; 		^this.multiNew('control', bufnum, freq, beats).madd(mul, add)
      ;; 	}
      ;; }
      
      {:name "COsc",
       :args [{:name "bufnum"}
              {:name "freq", :default 440.0}
              {:name "beats", :default 0.5}],
       :muladd true
       :doc "chorusing oscillator"}
      
      ;; Formant : UGen {
      ;; 	*ar {
      ;; 		arg fundfreq = 440.0, formfreq = 1760.0, bwfreq = 880.0, mul = 1.0, add = 0.0;
      ;; 		^this.multiNew('audio', fundfreq, formfreq, bwfreq).madd(mul, add)
      ;; 	}
      ;; }

      {:name "Formant",
       :args [{:name "fundfreq", :default 440.0}
              {:name "formfreq", :default 1760.0}
              {:name "bwfreq", :default 880.0}],
       :rates #{:ar},
       :muladd true
       :doc "formant oscillator"}
      
      ;; LFSaw : UGen {
      ;; 	*ar {
      ;; 		arg freq = 440.0, iphase = 0.0, mul = 1.0, add = 0.0;
      ;; 		^this.multiNew('audio', freq, iphase).madd(mul, add)
      ;; 	}
      ;; 	*kr {
      ;; 		arg freq = 440.0, iphase = 0.0, mul = 1.0, add = 0.0;
      ;; 		^this.multiNew('control', freq, iphase).madd(mul, add)
      ;; 	}
      ;; }

      {:name "LFSaw",
       :args [{:name "freq", :default 440.0}
              {:name "iphase", :default 0.0}]
       :muladd true
       :doc "low freq (i.e. not band limited) sawtooth oscillator"}
      
      ;; LFPar : LFSaw {}

      {:name "LFPar" :extends "LFSaw"}
      
      ;; LFCub : LFSaw {}

      {:name "LFCub" :extends "LFSaw"}
      
      ;; LFTri : LFSaw {}

      {:name "LFTri" :extends "LFSaw"}
      
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
              {:name "doneAction", :default 0 :map DONE-ACTIONS}]}

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
      
      ;; VarSaw : UGen {
      ;; 	*ar {
      ;; 		arg freq = 440.0, iphase = 0.0, width = 0.5, mul = 1.0, add = 0.0;
      ;; 		^this.multiNew('audio', freq, iphase, width).madd(mul, add)
      ;; 	}
      ;; 	*kr {
      ;; 		arg freq = 440.0, iphase = 0.0, width = 0.5, mul = 1.0, add = 0.0;
      ;; 		^this.multiNew('control', freq, iphase, width).madd(mul, add)
      ;; 	}
      ;; }

      {:name "VarSaw",
       :args [{:name "freq", :default 440.0}
              {:name "iphase", :default 0.0}
              {:name "width", :default 0.5}]
       :muladd true}

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

      ;; Index : UGen {
      ;; 	*ar {
      ;; 		arg bufnum, in = 0.0, mul = 1.0, add = 0.0;
      ;; 		^this.multiNew('audio', bufnum, in).madd(mul, add)
      ;; 	}
      ;; 	*kr {
      ;; 		arg bufnum, in = 0.0, mul = 1.0, add = 0.0;
      ;; 		^this.multiNew('control', bufnum, in).madd(mul, add)
      ;; 	}
      ;; }

      {:name "Index",
       :args [{:name "bufnum"}
              {:name "in", :default 0.0}]
       :muladd true}

      ;; WrapIndex : Index {}
      
      {:name "WrapIndex" :extends "Index"}

      ;; IndexInBetween : Index {}

      {:name "IndexInBetween" :extends "Index"}

      ;; DetectIndex : Index {}

      {:name "DetectIndex" :extends "Index"}

      ;; Shaper : Index {}

      {:name "Shaper" :extends "Index"}

      ;; DegreeToKey : UGen {
      ;; 	*ar {
      ;; 		arg bufnum, in = 0.0, octave = 12.0, mul = 1.0, add = 0.0;
      ;; 		^this.multiNew('audio', bufnum, in, octave).madd(mul, add)
      ;; 	}
      ;; 	*kr {
      ;; 		arg bufnum, in = 0.0, octave = 12.0, mul = 1.0, add = 0.0;
      ;; 		^this.multiNew('control', bufnum, in, octave).madd(mul, add)
      ;; 	}
      ;; }

      {:name "DegreeToKey",
       :args [{:name "bufnum"}
              {:name "in", :default 0.0}
              {:name "octave", :default 12.0}]
       :muladd true}
      
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
              {:name "array", :array true}]}

      ;; Vibrato : UGen {
      ;; 	*ar {
      ;; 		arg freq = 440.0, rate = 6, depth = 0.02, delay = 0.0, onset = 0.0,
      ;; 				rateVariation = 0.04, depthVariation = 0.1, iphase = 0.0;
      ;; 		^this.multiNew('audio', freq, rate, depth, delay, onset, rateVariation, depthVariation, iphase)
      ;; 	}
      ;; 	*kr {
      ;; 		arg freq = 440.0, rate = 6, depth = 0.02, delay = 0.0, onset = 0.0,
      ;; 				rateVariation = 0.04, depthVariation = 0.1, iphase = 0.0;
      ;; 		^this.multiNew('control', freq, rate, depth, delay, onset, rateVariation, depthVariation, iphase)
      ;; 	}
      ;; }

      {:name "Vibrato",
       :args [{:name "freq", :default 440.0}
              {:name "rate", :default 6}
              {:name "depth", :default 0.02}
              {:name "delay", :default 0.0}
              {:name "onset", :default 0.0}
              {:name "rateVariation", :default 0.04}
              {:name "depthVariation", :default 0.1}
              {:name "iphase", :default 0.0}]}])

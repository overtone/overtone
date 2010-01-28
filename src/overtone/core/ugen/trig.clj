(ns overtone.core.ugen.trig
  (:use (overtone.core.ugen common)))

(def specs
     [
      ;; Trig1 : UGen {
      
      ;; 	*ar { arg in = 0.0, dur = 0.1;
      ;; 		^this.multiNew('audio', in, dur)
      ;; 	}
      ;; 	*kr { arg in = 0.0, dur = 0.1;
      ;; 		^this.multiNew('control', in, dur)
      ;; 	}
      ;; 	signalRange { ^\unipolar }
      ;; }
      {:name "Trig1",
       :args [{:name "in", :default 0.0}
              {:name "dur", :default 0.1}]
       :signal-range :unipolar}
      
      ;; Trig : Trig1 {}
      
      {:name "Trig", :extends "Trig1"}

      ;; TDelay : Trig1 {
      ;;  	checkInputs { ^this.checkSameRateAsFirstInput }
      ;; }
      
      {:name "TDelay",
       :args [{:name "in", :default 0.0}
              {:name "dur", :default 0.1}]
       :signal-range :unipolar
       :check (same-rate-as-first-input)}

      ;; SendTrig : UGen {
      ;; 	*ar { arg in = 0.0, id = 0, value = 0.0;
      ;; 		this.multiNew('audio', in, id, value);
      ;; 		^0.0		// SendTrig has no output
      ;; 	}
      ;; 	*kr { arg in = 0.0, id = 0, value = 0.0;
      ;; 		this.multiNew('control', in, id, value);
      ;; 		^0.0		// SendTrig has no output
      ;; 	}
      ;;  	checkInputs { ^this.checkSameRateAsFirstInput }
      ;; 	numOutputs { ^0 }
      ;; 	writeOutputSpecs {}
      ;; }

      {:name "SendTrig",
       :args [{:name "in", :default 0.0}
              {:name "id", :default 0}
              {:name "value", :default 0.0}],
       :num-outs 0
       :check (same-rate-as-first-input)}
      
      ;; SendReply : SendTrig {
      ;; 	*kr { arg trig = 0.0, cmdName = '/reply', values, replyID = -1;
      ;; 		if(values.containsSeqColl.not) { values = values.bubble };
      ;; 		[trig, cmdName, values, replyID].flop.do { |args|
      ;; 			this.new1('control', *args);
      ;; 		};
      ;; 		^0.0		// SendReply has no output
      ;; 	}
      
      ;; 	*ar { arg trig = 0.0, cmdName = '/reply', values, replyID = -1;
      ;; 		if(values.containsSeqColl.not) { values = values.bubble };
      ;; 		[trig, cmdName, values, replyID].flop.do { |args|
      ;; 			this.new1('audio', *args);
      ;; 		};
      ;; 		^0.0		// SendReply has no output
      ;; 	}
      
      ;; 	*new1 { arg rate, trig = 0.0, cmdName = '/reply', values, replyID = -1;
      ;; 		var ascii = cmdName.ascii;
      ;; 		^super.new1(*[rate, trig, replyID, ascii.size].addAll(ascii).addAll(values));
      ;; 	}
      ;; }


      ;; TODO
       
      
      ;; Latch : UGen {
      
      ;; 	*ar { arg in = 0.0, trig = 0.0;
      ;; 		^this.multiNew('audio', in, trig)
      ;; 	}
      ;; 	*kr { arg in = 0.0, trig = 0.0;
      ;; 		^this.multiNew('control', in, trig)
      ;; 	}
      ;; }

      {:name "Latch",
       :args [{:name "in", :default 0.0}
              {:name "trig", :default 0.0}]}

      ;; Gate : Latch {}

      {:name "Gate", :extends "Latch"}
      
      ;; PulseCount : UGen {
      ;; 	*ar { arg trig = 0.0, reset = 0.0;
      ;; 		^this.multiNew('audio', trig, reset)
      ;; 	}
      ;; 	*kr { arg trig = 0.0, reset = 0.0;
      ;; 		^this.multiNew('control', trig, reset)
      ;; 	}
      ;;  	checkInputs { ^this.checkSameRateAsFirstInput }
      ;; }
      
      {:name "PulseCount",
       :args [{:name "trig", :default 0.0}
              {:name "reset", :default 0.0}]
       :check (same-rate-as-first-input)}

      ;; SetResetFF : PulseCount {}

      {:name "SetResetFF", :extends "PulseCount"}
            
      ;; Peak : PulseCount {}
      
      {:name "Peak", :extends "PulseCount"}
      
      ;; RunningMin : Peak {}
      
      {:name "RunningMin", :extends "PulseCount"}
      
      ;; RunningMax : Peak {}
      
      {:name "RunningMax", :extends "PulseCount"}

      ;; Stepper : UGen {
      
      ;; 	*ar { arg trig=0, reset=0, min=0, max=7, step=1, resetval;
      ;; 		^this.multiNew('audio', trig, reset, min, max, step, resetval ? min)
      ;; 	}
      ;; 	*kr { arg trig=0, reset=0, min=0, max=7, step=1, resetval;
      ;; 		^this.multiNew('control', trig, reset, min, max, step, resetval ? min)
      ;; 	}
      ;;  	checkInputs { ^this.checkSameRateAsFirstInput }
      ;; }

      {:name "Stepper",
       :args [{:name "trig", :default 0}
              {:name "reset", :default 0}
              {:name "min", :default 0}
              {:name "max", :default 7}
              {:name "step", :default 1}
              {:name "resetval" :default 1}] ; TODO MAYBE? allow :default :min
       :check (same-rate-as-first-input)}
      
      ;; PulseDivider : UGen {
      ;; 	*ar { arg trig = 0.0, div = 2.0, start = 0.0;
      ;; 		^this.multiNew('audio', trig, div, start)
      ;; 	}
      ;; 	*kr { arg trig = 0.0, div = 2.0, start = 0.0;
      ;; 		^this.multiNew('control', trig, div, start)
      ;; 	}
      ;; }

      {:name "PulseDivider",
       :args [{:name "trig", :default 0.0}
              {:name "div", :default 2.0}
              {:name "start", :default 0.0}]}
      
      ;; ToggleFF : UGen {
      ;; 	*ar { arg trig = 0.0;
      ;; 		^this.multiNew('audio', trig)
      ;; 	}
      ;; 	*kr { arg trig = 0.0;
      ;; 		^this.multiNew('control', trig)
      ;; 	}
      ;; }

      {:name "ToggleFF",
       :args [{:name "trig", :default 0.0}]}

      ;; ZeroCrossing : UGen {
      ;; 	*ar { arg in = 0.0;
      ;; 		^this.multiNew('audio', in)
      ;; 	}
      ;; 	*kr { arg in = 0.0;
      ;; 		^this.multiNew('control', in)
      ;; 	}
      ;;  	checkInputs { ^this.checkSameRateAsFirstInput }
      ;; }
      
      {:name "ZeroCrossing",
       :args [{:name "in", :default 0.0}]
       :check (same-rate-as-first-input)}
      
      ;; Timer : UGen {
      ;; 	// output is the time between two triggers
      ;; 	*ar { arg trig = 0.0;
      ;; 		^this.multiNew('audio', trig)
      ;; 	}
      ;; 	*kr { arg trig = 0.0;
      ;; 		^this.multiNew('control', trig)
      ;; 	}
      ;;  	checkInputs { ^this.checkSameRateAsFirstInput }
      ;; }
      
      {:name "Timer",
       :args [{:name "trig", :default 0.0}]
       :check (same-rate-as-first-input)}
      
      ;; Sweep : UGen {
      ;; 	// output sweeps up in value at rate per second
      ;; 	// the trigger resets to zero
      ;; 	*ar { arg trig = 0.0, rate = 1.0;
      ;; 		^this.multiNew('audio', trig, rate)
      ;; 	}
      ;; 	*kr { arg trig = 0.0, rate = 1.0;
      ;; 		^this.multiNew('control', trig, rate)
      ;; 	}
      ;; }
      
      {:name "Sweep",
       :args [{:name "trig", :default 0.0}
              {:name "rate", :default 1.0}]}
      
      ;; Phasor : UGen {
      ;; 	*ar { arg trig = 0.0, rate = 1.0, start = 0.0, end = 1.0, resetPos = 0.0;
      ;; 		^this.multiNew('audio', trig, rate, start, end, resetPos)
      ;; 	}
      ;; 	*kr { arg trig = 0.0, rate = 1.0, start = 0.0, end = 1.0, resetPos = 0.0;
      ;; 		^this.multiNew('control', trig, rate, start, end, resetPos)
      ;; 	}
      ;; }
      
      {:name "Phasor",
       :args [{:name "trig", :default 0.0}
              {:name "rate", :default 1.0}
              {:name "start", :default 0.0}
              {:name "end", :default 1.0}
              {:name "resetPos", :default 0.0}]}
      
      ;; PeakFollower : UGen {
      
      ;; 	*ar { arg in = 0.0, decay = 0.999;
      ;; 		^this.multiNew('audio', in, decay)
      ;; 	}
      ;; 	*kr { arg in = 0.0, decay = 0.999;
      ;; 		^this.multiNew('control', in, decay)
      ;; 	}
      ;; }
      
      {:name "PeakFollower",
       :args [{:name "in", :default 0.0}
              {:name "decay", :default 0.999}]}
      
      ;; Pitch : MultiOutUGen {
      
      ;; 	*kr { arg in = 0.0, initFreq = 440.0, minFreq = 60.0, maxFreq = 4000.0, 
      ;; 			execFreq = 100.0, maxBinsPerOctave = 16, median = 1, 
      ;; 			ampThreshold = 0.01, peakThreshold = 0.5, downSample = 1;
      ;; 		^this.multiNew('control', in, initFreq, minFreq, maxFreq, execFreq,
      ;; 			maxBinsPerOctave, median, ampThreshold, peakThreshold, downSample)
      ;; 	}
      ;; 	init { arg ... theInputs;
      ;; 		inputs = theInputs;
      ;; 		^this.initOutputs(2, rate);
      ;; 	}
      ;; }
      
      {:name "Pitch",
       :args [{:name "in", :default 0.0}
              {:name "initFreq", :default 440.0}
              {:name "minFreq", :default 60.0}
              {:name "maxFreq", :default 4000.0}
              {:name "execFreq", :default 100.0}
              {:name "maxBinsPerOctave", :default 16}
              {:name "median", :default 1}
              {:name "ampThreshold", :default 0.01}
              {:name "peakThreshold", :default 0.5}
              {:name "downSample", :default 1}],
       :rates #{:kr},
       :num-outs 2}
      
      ;; InRange : UGen {
      ;; 	*ar { arg in = 0.0, lo = 0.0, hi = 1.0;
      ;; 		^this.multiNew('audio', in, lo, hi)
      ;; 	}
      ;; 	*kr { arg in = 0.0, lo = 0.0, hi = 1.0;
      ;; 		^this.multiNew('control', in, lo, hi)
      ;; 	}
      ;; }

      {:name "InRange",
       :args [{:name "in", :default 0.0}
              {:name "lo", :default 0.0}
              {:name "hi", :default 1.0}]}

      ;; Fold : InRange {}
      
      {:name "Fold", :extends "InRange"}
      
      ;; Clip : InRange {}

      {:name "Clip", :extends "InRange"}
      
      ;; Wrap : InRange {}

      {:name "Wrap", :extends "InRange"}
      
      ;; Schmidt : InRange {}

      {:name "Schmidt", :extends "InRange"}
      
      ;; InRect : UGen
      ;; {
      ;; 	*ar { arg x = 0.0, y = 0.0, rect;
      ;; 		^this.multiNew('audio', x, y, rect.left, rect.top, 
      ;; 			rect.right, rect.bottom)
      ;; 	}
      ;; 	*kr { arg x = 0.0, y = 0.0, rect;
      ;; 		^this.multiNew('control', x, y, rect.left, rect.top, 
      ;; 			rect.right, rect.bottom)
      ;; 	}
      ;; }

	  ;; TODO maybe allow a rect datatype as arg
	  ;;      and write init function to handle it
      {:name "InRect",
       :args [{:name "x", :default 0.0}
              {:name "y", :default 0.0}
              {:name "left"}
              {:name "top"}
              {:name "right"}
              {:name "bottom"}]}

      ;; //Trapezoid : UGen
      ;; //{
      ;; //	*ar { arg in = 0.0, a = 0.2, b = 0.4, c = 0.6, d = 0.8;
      ;; //		^this.multiNew('audio', in, a, b, c, d)
      ;; //	}
      ;; //	*kr { arg in = 0.0, a = 0.2, b = 0.4, c = 0.6, d = 0.8;
      ;; //		^this.multiNew('control', in, a, b, c, d)
      ;; //	}
      ;; //}

      {:name "Trapezoid",
       :args [{:name "in", :default 0.0}
              {:name "a", :default 0.2}
              {:name "b", :default 0.4}
              {:name "c", :default 0.6}
              {:name "d", :default 0.8}]}
      
      ;; MostChange : UGen
      ;; {
      ;; 	*ar { arg a = 0.0, b = 0.0;
      ;; 		^this.multiNew('audio', a, b)
      ;; 	}
      ;; 	*kr { arg a = 0.0, b = 0.0;
      ;; 		^this.multiNew('control', a, b)
      ;; 	}
      ;; }
      
      {:name "MostChange",
       :args [{:name "a", :default 0.0}
              {:name "b", :default 0.0}]}
      
      ;; LeastChange : MostChange {}

      {:name "LeastChange", :extends "MostChange"}
      
      ;; LastValue : UGen {
      
      ;; 	*ar { arg in=0.0, diff=0.01;
      ;; 		^this.multiNew('audio', in, diff)
      ;; 	}
      ;; 	*kr { arg in=0.0, diff=0.01;
      ;; 		^this.multiNew('control', in, diff)
      ;; 	}
      ;; }

      {:name "LastValue",
       :args [{:name "in", :default 0.0}
              {:name "diff", :default 0.01}]}])


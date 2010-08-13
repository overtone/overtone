(ns overtone.core.ugen.io
  (:use (overtone.core.ugen common)))

(def specs
     [
      ;; from DiskIO.sc
      ;; DiskOut : UGen {
      ;; 	*ar { arg bufnum, channelsArray;
      ;; 		this.multiNewList(['audio', bufnum] ++ channelsArray.asArray)
      ;; 		^0.0		// DiskOut has no output
      ;; 	}
      ;; 	numOutputs { ^0 }
      ;; 	writeOutputSpecs {}
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

      {:name "DiskOut",
       :args [{:name "bufnum"}
              {:name "channelsArray" :mode :append-sequence}],
       :rates #{:ar},
       :num-outs 0
       :check (all-but-first-input-ar "channelsArray must all be audio rate")
       :doc "stream audio out to disk file"}

      ;; from DiskIO.sc
      ;; DiskIn : MultiOutUGen {
      ;; 	*ar { arg numChannels, bufnum, loop = 0;
      ;; 		^this.multiNew('audio', numChannels, bufnum, loop)
      ;; 	}
      ;; 	init { arg numChannels, bufnum, loop = 0;
      ;; 		inputs = [bufnum, loop];
      ;; 		^this.initOutputs(numChannels, rate)
      ;; 	}
      ;; }

      {:name "DiskIn",
       :args [{:name "numChannels" :mode :num-outs}
              {:name "bufnum"}
              {:name "loop", :default 0}],
       :rates #{:ar}
       :doc "stream audio in from disk file"}

      ;; from DiskIO.sc
      ;; VDiskIn : MultiOutUGen {
      ;; 	*ar { arg numChannels, bufnum, rate = 1, loop = 0, sendID = 0;
      ;; 		^this.multiNew('audio', numChannels, bufnum, rate, loop, sendID)
      ;; 	}
      ;; 	init { arg numChannels, bufnum, argRate = 1, loop = 0, sendID = 0;
      ;; 		inputs = [bufnum, argRate, loop, sendID];
      ;; 		^this.initOutputs(numChannels, rate)
      ;; 	}
      ;; }

      {:name "VDiskIn",
       :args [{:name "numChannels" :mode :num-outs}
              {:name "bufnum"}
              {:name "rate", :default 1}
              {:name "loop", :default 0}
              {:name "sendID", :default 0}],
       :rates #{:ar}}

      ;; from InOut.sc
      ;; AbstractIn : MultiOutUGen {
      ;;  	*isInputUGen { ^true }
      ;; }

      ;; from InOut.sc
      ;; In : AbstractIn {
      ;; 	*ar { arg bus = 0, numChannels = 1;
      ;; 		^this.multiNew('audio', numChannels, bus)
      ;; 	}
      ;; 	*kr { arg bus = 0, numChannels = 1;
      ;; 		^this.multiNew('control', numChannels, bus)
      ;; 	}
      ;; 	init { arg numChannels ... argBus;
      ;; 		inputs = argBus.asArray;
      ;; 		^this.initOutputs(numChannels, rate)
      ;; 	}
      ;; }

      {:name "In",
       :args [{:name "bus", :default 0}
              {:name "numChannels", :mode :num-outs :default 1}]
       :doc "read from a bus"}

      ;; from InOut.sc
      ;; LocalIn : AbstractIn {
      ;; 	*ar { arg numChannels = 1;
      ;; 		^this.multiNew('audio', numChannels)
      ;; 	}
      ;; 	*kr { arg numChannels = 1;
      ;; 		^this.multiNew('control', numChannels)
      ;; 	}
      ;; 	init { arg numChannels;
      ;; 		^this.initOutputs(numChannels, rate)
      ;; 	}
      ;; }

      {:name "LocalIn",
       :args [{:name "numChannels", :mode :num-outs :default 1}]}

      ;; from InOut.sc
      ;; LagIn : AbstractIn {
      ;; 	*kr { arg bus = 0, numChannels = 1, lag = 0.1;
      ;; 		^this.multiNew('control', numChannels, bus, lag)
      ;; 	}
      ;; 	init { arg numChannels ... argInputs;
      ;; 		inputs = argInputs.asArray;
      ;; 		^this.initOutputs(numChannels, rate)
      ;; 	}
      ;; }

      {:name "LagIn",
       :args [{:name "bus", :default 0}
              {:name "numChannels", :mode :num-outs :default 1}
              {:name "lag", :default 0.1}],
       :rates #{:kr}}

      ;; from InOut.sc
      ;; InFeedback : AbstractIn {
      ;; 	*ar { arg bus = 0, numChannels = 1;
      ;; 		^this.multiNew('audio', numChannels, bus)
      ;; 	}
      ;; 	init { arg numChannels ... argBus;
      ;; 		inputs = argBus.asArray;
      ;; 		^this.initOutputs(numChannels, rate)
      ;; 	}
      ;; }

      {:name "InFeedback",
       :args [{:name "bus", :default 0}
              {:name "numChannels", :mode :num-outs :default 1}],
       :rates #{:ar}
       :doc "read signal from a bus with a current or one cycle old timestamp"}

      ;; from InOut.sc
      ;; InTrig : AbstractIn {
      ;; 	*kr { arg bus = 0, numChannels = 1;
      ;; 		^this.multiNew('control', numChannels, bus)
      ;; 	}
      ;; 	init { arg numChannels ... argBus;
      ;; 		inputs = argBus.asArray;
      ;; 		^this.initOutputs(numChannels, rate)
      ;; 	}
      ;; }

      {:name "InTrig",
       :args [{:name "bus", :default 0}
              {:name "numChannels", :mode :num-outs :default 1}],
       :rates #{:kr}
       :doc "generates a trigger any time the bus is set"}

      ;; from InOut.sc
      ;; SharedIn : AbstractIn {
      ;; 	*kr { arg bus = 0, numChannels = 1;
      ;; 		^this.multiNew('control', numChannels, bus)
      ;; 	}
      ;; 	init { arg numChannels ... argBus;
      ;; 		inputs = argBus.asArray;
      ;; 		^this.initOutputs(numChannels, rate)
      ;; 	}
      ;; }

      {:name "SharedIn",
       :args [{:name "bus", :default 0}
              {:name "numChannels", :mode :num-outs :default 1}],
       :rates #{:kr}
       :doc "read from a shared control bus (internal dsp only)"}

      ;; from InOut.sc
      ;; AbstractOut : UGen {
      ;; 	numOutputs { ^0 }
      ;; 	writeOutputSpecs {}
      ;;  	checkInputs {
      ;;  		if (rate == 'audio', {
      ;;  			for(this.class.numFixedArgs, inputs.size - 1, { arg i;
      ;;  				if (inputs.at(i).rate != 'audio', {
      ;;  					^(" input at index " + i +
      ;;  						"(" + inputs.at(i) + ") is not audio rate");
      ;;  				});
      ;;  			});
      ;;  		});
      ;;  		^this.checkValidInputs
      ;;  	}

      ;;  	*isOutputUGen { ^true }

      ;;  	numAudioChannels {
      ;;  		^inputs.size - this.class.numFixedArgs
      ;;  	}
      ;;  	writesToBus { ^this.subclassResponsibility(thisMethod) }
      ;; }

      ;; from InOut.sc
      ;; Out : AbstractOut {
      ;; 	*ar { arg bus, channelsArray;
      ;; 		channelsArray = this.replaceZeroesWithSilence(channelsArray.asArray);
      ;; 		this.multiNewList(['audio', bus] ++ channelsArray)
      ;; 		^0.0		// Out has no output
      ;; 	}
      ;; 	*kr { arg bus, channelsArray;
      ;; 		this.multiNewList(['control', bus] ++ channelsArray.asArray)
      ;; 		^0.0		// Out has no output
      ;; 	}
      ;; 	*numFixedArgs { ^1 }
      ;; 	writesToBus { ^true }
      ;; }

      {:name "Out",
       :args [{:name "bus"}
              {:name "channelsArray" :mode :append-sequence}],
       :num-outs 0
       :check (when-ar
               (all-but-first-input-ar "channelsArray must all be audio rate"))
       :doc "write a signal to a bus, adding to any existing contents"}

      ;; from InOut.sc
      ;; ReplaceOut : Out {}

      {:name "ReplaceOut", :extends "Out"
       :doc "write signal to a bus, replacing the contents rather than adding to it."}

      ;; OffsetOut : Out {
      ;; 	*kr { ^this.shouldNotImplement(thisMethod) }
      ;; }

      {:name "OffsetOut" :extends "Out"
       :rates #{:ar}
       :check (all-but-first-input-ar "channelsArray must all be audio rate")
       :doc "write signal to a bus with sample accurate timing"}

      ;; from InOut.sc
      ;; LocalOut : AbstractOut {
      ;; 	*ar { arg channelsArray;
      ;; 		channelsArray = this.replaceZeroesWithSilence(channelsArray.asArray);
      ;; 		this.multiNewList(['audio'] ++ channelsArray)
      ;; 		^0.0		// LocalOut has no output
      ;; 	}
      ;; 	*kr { arg channelsArray;
      ;; 		this.multiNewList(['control'] ++ channelsArray.asArray)
      ;; 		^0.0		// LocalOut has no output
      ;; 	}
      ;; 	*numFixedArgs { ^0 }
      ;; 	writesToBus { ^false }
      ;; }

      {:name "LocalOut",
       :args [{:name "channelsArray" :mode :append-sequence}],
       :num-outs 0
       :check (when-ar (all-inputs-ar "all channels must be audio rate"))}

      ;; from InOut.sc
      ;; XOut : AbstractOut {
      ;; 	*ar { arg bus, xfade, channelsArray;
      ;; 		channelsArray = this.replaceZeroesWithSilence(channelsArray.asArray);
      ;; 		this.multiNewList(['audio', bus, xfade] ++ channelsArray)
      ;; 		^0.0		// Out has no output
      ;; 	}
      ;; 	*kr { arg bus, xfade, channelsArray;
      ;; 		this.multiNewList(['control', bus, xfade] ++ channelsArray.asArray)
      ;; 		^0.0		// Out has no output
      ;; 	}
      ;; 	*numFixedArgs { ^2 }
      ;; 	checkInputs {
      ;;  		if (rate == 'audio', {
      ;;  			for(2, inputs.size - 1, { arg i;
      ;;  				if (inputs.at(i).rate != 'audio', {
      ;;  					^(" input at index " + i +
      ;;  						"(" + inputs.at(i) + ") is not audio rate");
      ;;  				});
      ;;  			});
      ;;  		});
      ;;  		^this.checkValidInputs
      ;;  	}
      ;;  	writesToBus { ^true }
      ;; }

      {:name "XOut",
       :args [{:name "bus"}
              {:name "xfade"}
              {:name "channelsArray" :mode :append-sequence}],
       :num-outs 0
       :check (when-ar (after-n-inputs-rest-ar 2 "all channels must be audio rate"))
       :doc "write signal to a bus, crossfading with the existing content (adding with fader adjustment)"}

      ;; from InOut.sc
      ;; SharedOut : AbstractOut {
      ;; 	*kr { arg bus, channelsArray;
      ;; 		this.multiNewList(['control', bus] ++ channelsArray.asArray)
      ;; 		^0.0		// Out has no output
      ;; 	}
      ;; 	writesToBus { ^false }
      ;; }

      {:name "SharedOut",
       :args [{:name "bus"}
              {:name "channelsArray" :mode :append-sequence}],
       :rates #{:kr},
       :num-outs 0}])

(ns overtone.core.ugens.control)

;; all ugens this file from InOut.sc

(def specs
     [
      ;; ControlName
      ;; {
      ;; 	var <>name, <>index, <>rate, <>defaultValue, <>argNum, <>lag;      
      ;; 	*new { arg name, index, rate, defaultValue, argNum, lag;
      ;; 		^super.newCopyArgs(name.asSymbol, index, rate, defaultValue, argNum, lag ? 0.0)
      ;; 	
      ;; 	printOn { arg stream;
      ;; 		stream << "ControlName  P " << index.asString;
      ;; 		if (name.notNil) { stream << " " << name; };
      ;; 		if (rate.notNil) { stream << " " << rate; };
      ;; 		if (defaultValue.notNil) { stream << " " << defaultValue; };
      ;; 		//stream << "\n"
      ;; 	}	
      ;; }

      ;; Control : MultiOutUGen {
      ;; 	var <values;      
      ;; 	*names { arg names;
      ;; 		var synthDef, index;
      ;; 		synthDef = UGen.buildSynthDef;
      ;; 		index = synthDef.controlIndex;
      ;; 		names = names.asArray;
      ;; 		names.do { |name, i|
      ;; 			synthDef.addControlName(
      ;; 				ControlName(name.asString, index + i, 'control', 
      ;; 					nil, synthDef.allControlNames.size)
      ;; 			);
      ;; 		};
      ;; 	}
      ;; 	*kr { arg values;
      ;; 		^this.multiNewList(['control'] ++ values.asArray)
      ;; 	}
      ;; 	*ir { arg values;
      ;; 		^this.multiNewList(['scalar'] ++ values.asArray)
      ;; 	}
      ;; 	init { arg ... argValues;
      ;; 		var ctlNames, lastControl;
      ;; 		values = argValues;
      ;; 		if (synthDef.notNil) {
      ;; 			specialIndex = synthDef.controls.size;
      ;; 			synthDef.controls = synthDef.controls.addAll(values);
      ;; 			ctlNames = synthDef.controlNames; 
      
      ;; 			if (ctlNames.size > 0) { 		
      ;; 					// the current control is always the last added, so:
      ;; 				lastControl = synthDef.controlNames.last;
      ;; 				if(lastControl.defaultValue.isNil) { 
      ;; 						// only write if not there yet:
      ;; 					lastControl.defaultValue_(values.unbubble);
      ;; 				}
      ;; 			};

      ;; 			synthDef.controlIndex = synthDef.controlIndex + values.size;

      ;; 		};
      ;; 		^this.initOutputs(values.size, rate)
      ;; 	}
      ;; 	*isControlUGen { ^true }
      ;; }

      {:name "Control"
       :args [{:name "values"}]
       :rates #{:kr :ir}
       :init (fn [rate [values] spec]
               {})}
      

      ;; AudioControl : MultiOutUGen {
      ;; 	var <values;
      
      ;; 	*names { arg names;
      ;; 		var synthDef, index;
      ;; 		synthDef = UGen.buildSynthDef;
      ;; 		index = synthDef.controlIndex;
      ;; 		names = names.asArray;
      ;; 		names.do { |name, i|
      ;; 			synthDef.addControlName(
      ;; 				ControlName(name.asString, index + i, 'audio', 
      ;; 					nil, synthDef.allControlNames.size)
      ;; 			);
      ;; 		};
      ;; 	}
      ;; 	*ar { arg values;
      ;; 		^this.multiNewList(['audio'] ++ values.asArray)
      ;; 	}
      ;; 	init { arg ... argValues;
      ;; 		values = argValues;
      ;; 		if (synthDef.notNil) {
      ;; 			specialIndex = synthDef.controls.size;
      ;; 			synthDef.controls = synthDef.controls.addAll(values);
      ;; 			synthDef.controlIndex = synthDef.controlIndex + values.size;
      ;; 		};
      ;; 		^this.initOutputs(values.size, rate)
      ;; 	}
      ;; 	*isAudioControlUGen { ^true }
      ;; }

      ;; TrigControl : Control {}

      ;; LagControl : Control {	
      ;;  	*kr { arg values, lags;
      ;; 		var outputs;

      ;; 		values = values.asArray;
      ;; 		lags = lags.asArray;
      ;; 		if (values.size != lags.size, {
      ;; 			"LagControl values.size != lags.size".error; 
      ;; 			^nil 
      ;; 		});
      ;; 		values = values.clump(16);
      ;; 		lags = lags.clump(16);
      ;; 		outputs = [];
      ;; 		values.size.do({ arg i;
      ;; 			outputs = outputs ++ this.multiNewList(['control'] ++ values.at(i) ++ lags.at(i));
      ;; 		});
      ;; 		^outputs
      ;; 	}
      ;; 	*ir {
      ;; 		^this.shouldNotImplement(thisMethod)
      ;; 	}
      ;; 	init { arg ... stuff;
      ;; 		var lags, size, size2;
      ;; 		size = stuff.size;
      ;; 		size2 = size >> 1;
      ;; 		values = stuff[ .. size2-1];
      ;; 		inputs = stuff[size2 .. size-1];
      ;; 		if (synthDef.notNil, { 
      ;; 			specialIndex = synthDef.controls.size;
      ;; 			synthDef.controls = synthDef.controls.addAll(values);
      ;; 			synthDef.controlIndex = synthDef.controlIndex + values.size;
      ;; 		});
      ;; 		^this.initOutputs(values.size, rate)
      ;; 	}
      ;; }

      ])
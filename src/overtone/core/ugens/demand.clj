
(ns overtone.core.ugens.demand)

(def specs
     [
      ;; Demand : MultiOutUGen {
      ;;  *ar { arg trig, reset, demandUGens;
      ;;    ^this.multiNewList(['audio', trig, reset] ++ demandUGens.asArray)
      ;;  }
      ;;  *kr { arg trig, reset, demandUGens;
      ;;    ^this.multiNewList(['control', trig, reset] ++ demandUGens.asArray)
      ;;  }
      ;;  init { arg ... argInputs;
      ;;    inputs = argInputs;
      ;;    ^this.initOutputs(inputs.size - 2, rate)
      ;;  }
      ;;    checkInputs { ^this.checkSameRateAsFirstInput } 
      ;; }

      {:name "Demand",
       :args [{:name "trig"}
              {:name "reset"}
              {:name "demandUGens", :mode :append-sequence-set-num-outs}],
       :check (same-rate-as-first-input)}
      
      ;; Duty : UGen {
      ;;  *ar { arg dur = 1.0, reset = 0.0, level = 1.0, doneAction = 0;
      ;;    ^this.multiNew('audio', dur, reset, doneAction, level)
      ;;  }
      ;;  *kr { arg dur = 1.0, reset = 0.0, level = 1.0, doneAction = 0;
      ;;    ^this.multiNew('control', dur, reset, doneAction, level)
      ;;  }
      ;;   checkInputs {
      ;;    ^if(inputs.at(0).rate === \demand) {
      ;;      if (inputs.at(1).rate !== \demand and: { inputs.at(1).rate !== \scalar } and: 
      ;;        { inputs.at(1).rate !== rate }) { 
      ;;          ("reset input is not" + rate + "rate: " +
      ;;              inputs.at(1) + inputs.at(1).rate);
      ;;        }
      ;;      } {
      ;;        this.checkValidInputs
      ;;      }
      ;;    }
      ;; }

      {:name "Duty",
       :args [{:name "dur", :default 1.0}
              {:name "reset", :default 0.0}
              {:name "level", :default 1.0}
              {:name "doneAction", :default :none}]
       :check (fn [rate num-outs [dur reset & _] spec]
                (if (and (dr? dur)
                         (not (or (dr? reset)
                                  (ir? reset)
                                  (rate-of? reset rate))))
                  "TODO write error string. and understad why this is an error"))}
      
      ;; TDuty : Duty {
      ;;  *ar { arg dur = 1.0, reset = 0.0, level = 1.0, doneAction = 0, gapFirst = 0;
      ;;    ^this.multiNew('audio', dur, reset, doneAction, level, gapFirst)
      ;;  }
      ;;  *kr { arg dur = 1.0, reset = 0.0, level = 1.0, doneAction = 0, gapFirst = 0;
      ;;    ^this.multiNew('control', dur, reset, doneAction, level, gapFirst)
      ;;  }
      ;; }

      {:name "TDuty" :extends "Duty"
       :args [{:name "dur", :default 1.0}
              {:name "reset", :default 0.0}
              {:name "level", :default 1.0}
              {:name "doneAction", :default 0}
              {:name "gapFirst", :default 0}]}


      ;; DemandEnvGen : UGen {

      ;;  *kr { arg level, dur, shape = 1, curve = 0, gate = 1.0, reset = 1.0,
      ;;        levelScale = 1.0, levelBias = 0.0, timeScale = 1.0, doneAction=0;
      ;;    ^this.multiNew('control', level, dur, shape, curve, gate, reset, 
      ;;        levelScale, levelBias, timeScale, doneAction)
      ;;  }
      ;;  *ar { arg level, dur, shape = 1, curve = 0, gate = 1.0, reset = 1.0, 
      ;;        levelScale = 1.0, levelBias = 0.0, timeScale = 1.0, doneAction=0;
      ;;          if(gate.rate === 'audio' or: { reset.rate === 'audio' }) {
      ;;            if(gate.rate !== 'audio') { gate = K2A.ar(gate) };
      ;;            if(reset.rate !== 'audio') { reset = K2A.ar(reset) };
      ;;          };
      ;;    ^this.multiNew('audio', level, dur, shape, curve, gate, reset, 
      ;;        levelScale, levelBias, timeScale, doneAction)
      ;;  }
      ;; }

      {:name "DemandEnvGen",
       :args [{:name "level"}
              {:name "dur"}
              {:name "shape", :default 1}
              {:name "curve", :default 0}
              {:name "gate", :default 1.0}
              {:name "reset", :default 1.0}
              {:name "levelScale", :default 1.0}
              {:name "levelBias", :default 0.0}
              {:name "timeScale", :default 1.0}
              {:name "doneAction", :default :none}]
       :init (fn [rate [l d s c gate reset ls lb ts da] spec]
               (if (or (ar? gate) (ar? reset))
                 [l d s c (as-ar gate) (as-ar reset) ls lb ts da]))}
      
      ;; DUGen : UGen {
      ;;  init { arg ... argInputs;
      ;;    super.init(*argInputs);
      ;;    this.forceAudioRateInputsIntoUGenGraph;
      ;;  }
      ;;  forceAudioRateInputsIntoUGenGraph {
      ;;      inputs.do { |in| if(in.rate == \audio) { in <! 0 } }; }
      ;;
      ;;    // some n-ary op special cases
      ;;
      ;;    linlin { arg inMin, inMax, outMin, outMax, clip=\minmax;
      ;;        ^((this.prune(inMin, inMax, clip)-inMin)/(inMax-inMin) * (outMax-outMin) + outMin);
      ;;  }
      ;;
      ;;  linexp { arg inMin, inMax, outMin, outMax, clip=\minmax;
      ;;    ^(pow(outMax/outMin, (this-inMin)/(inMax-inMin)) * outMin)
      ;;      .prune(inMin, inMax, clip);
      ;;  }
      ;;
      ;;  explin { arg inMin, inMax, outMin, outMax, clip=\minmax;
      ;;    ^(log(this.prune(inMin, inMax, clip)/inMin)) 
      ;;      / (log(inMax/inMin)) * (outMax-outMin) + outMin
      ;;  }
      ;;
      ;;  expexp { arg inMin, inMax, outMin, outMax, clip=\minmax;
      ;;    ^pow(outMax/outMin, log(this.prune(inMin, inMax, clip/inMin) / log(inMax/inMin)) * outMin)
      ;;  }
      ;; }
      ;;
      ;; Dseries : DUGen {
      ;;  *new { arg start = 1, step = 1, length = inf;
      ;;    ^this.multiNew('demand', length, start, step)
      ;;  } 
      ;; }

      ;; TODO understand forceAudioRateInputsIntoUGenGraph
      ;; which is implemented in the pseudo-ugen DUGen parent of below
      
      {:name "Dseries",
       :args [{:name "start", :default 1}
              {:name "step", :default 1}
              {:name "length", :default 100.0}], ; TODO inf
       :rates #{:dr}}

      ;; Dgeom : DUGen {
      ;;  *new { arg start = 1, grow = 2, length = inf;
      ;;    ^this.multiNew('demand', length, start, grow)
      ;;  } 
      ;; }

      {:name "Dgeom",
       :args [{:name "start", :default 1}
              {:name "grow", :default 2}
              {:name "length", :default 100.0}], ; TODO inf
       :rates #{:dr}}
      
      ;; Dbufrd : DUGen {
      ;;  *new { arg bufnum = 0, phase = 0.0, loop = 1.0;
      ;;    ^this.multiNew('demand', bufnum, phase, loop) 
      ;;  }
      ;; }
      
      {:name "Dbufrd",
       :args [{:name "bufnum", :default 0.0}
              {:name "phase", :default 0.0}
              {:name "loop", :default 1.0}],
       :rates #{:dr}}
      
      ;; Dbufwr : DUGen {
      ;;  *new { arg input = 0.0, bufnum = 0, phase = 0.0, loop = 1.0;
      ;;    ^this.multiNew('demand', bufnum, phase, input, loop)
      ;;  }
      ;; }
      
      {:name "Dbufwr",
       :args [{:name "input", :default 0.0}
              {:name "bufnum", :default 0}
              {:name "phase", :default 0.0}
              {:name "loop", :default 1.0}],
       :rates #{:dr}}
      
      ;; ListDUGen : DUGen {
      ;;  *new { arg list, repeats = 1;
      ;;    ^this.multiNewList(['demand', repeats] ++ list)
      ;;  }
      ;; }

      ;; Dseq : ListDUGen {}

      {:name "Dseq",
       :args [{:name "list", :array true}
              {:name "repeats", :default 1}],
       :rates #{:dr}}
      
      ;; Dser : ListDUGen {}

      {:name "Dser" :extends "Dseq"}
      
      ;; Dshuf : ListDUGen {}

      {:name "Dshuf" :extends "Dseq"}
      
      ;; Drand : ListDUGen {}

      {:name "Drand" :extends "Dseq"}
      
      ;; Dxrand : ListDUGen {}
      
      {:name "Dxrand" :extends "Dseq"}
      
      ;; Dswitch1 : DUGen {
      ;;  *new { arg list, index;
      ;;    ^this.multiNewList(['demand', index] ++ list)
      ;;  }
      ;; }

      {:name "Dswitch1",
       :args [{:name "list", :array true}
              {:name "index"}],
       :rates #{:dr}}
      
      ;; Dswitch : Dswitch1 {}

      {:name "Dswitch" :extends "Dswitch1"}
      
      ;; Dwhite : DUGen {
      ;;  *new { arg lo = 0.0, hi = 1.0, length = inf;
      ;;    ^this.multiNew('demand', length, lo, hi)
      ;;  }
      ;; }

      {:name "Dwhite",
       :args [{:name "lo", :default 0.0}
              {:name "hi", :default 1.0}
              {:name "length", :default 100}], ; TODO inf ??
       :rates #{:dr}}
      
      ;; Diwhite : Dwhite {}
      
      {:name "Diwhite" :extends "Dwhite"}
      
      ;; Dbrown : DUGen {
      ;;  *new { arg lo = 0.0, hi = 1.0, step = 0.01, length = inf;
      ;;    ^this.multiNew('demand', length, lo, hi, step)
      ;;  }
      ;; }

      {:name "Dbrown",
       :args [{:name "lo", :default 0.0}
              {:name "hi", :default 1.0}
              {:name "step", :default 0.01}
              {:name "length", :default 100}], ; TODO inf ??
       :rates #{:dr}}
      
      ;; Dibrown : Dbrown {}

      {:name "Dibrown" :extends "Dbrown"}
      
      ;; Dstutter : DUGen {
      ;;  *new { arg n, in;
      ;;    ^this.multiNew('demand', n, in)
      ;;  }
      ;; }
      
      {:name "Dstutter",
       :args [{:name "n"}
              {:name "in"}],
       :rates #{:dr}}
      
      ;; Donce : DUGen {
      ;;  *new { arg in;
      ;;    ^this.multiNew('demand', in)
      ;;  }
      ;; }
      
      {:name "Donce",
       :args [{:name "in"}],
       :rates #{:dr}}

      ;; TODO how does this one work ??
      ;; Dpoll : DUGen {
      ;;  *new { arg in, label, run = 1, trigid = -1;
      ;;    ^this.multiNew('demand', in, label, run, trigid)
      ;;  }
      
      ;;  *new1 { arg rate, in, label, run, trigid;
      ;;    label = label ?? { "DemandUGen(%)".format(in.class) };
      ;;    label = label.ascii;
      ;;    ^super.new.rate_(rate).addToSynth.init(*[in, trigid, run, label.size] ++ label);
      ;;  }
      ;; }

      ])
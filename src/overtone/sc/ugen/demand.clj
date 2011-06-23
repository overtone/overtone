(ns overtone.sc.ugen.demand
  (:use (overtone.sc.ugen common)))

(def specs
     [
      {:name "Demand",
       :args [{:name "trig"}
              {:name "reset"}
              {:name "demandUGens", :mode :append-sequence-set-num-outs}],
       :check (same-rate-as-first-input)
       :doc "On every trigger it demands the next value from each of the demand ugens passed as args.  Used to pull values from the other demand rate ugens."}

      {:name "Duty",
       :args [{:name "dur", :default 1.0}
              {:name "reset", :default 0.0}
              {:name "level", :default 1.0}
              {:name "action", :default :none :map DONE-ACTIONS}]
       :check (fn [rate num-outs [dur reset & _] spec]
                (if (and (dr? dur)
                         (not (or (dr? reset)
                                  (ir? reset)
                                  (rate-of? reset rate))))
                  "TODO write error string. and understad why this is an error"))
       :doc "Expects demand ugen args for dur and level.  Uses successive dur values to determine how long to wait before emitting each level value."}

      {:name "TDuty" :extends "Duty"
       :args [{:name "dur", :default 1.0}
              {:name "reset", :default 0.0}
              {:name "level", :default 1.0}
              {:name "action", :default 0 :map DONE-ACTIONS}
              {:name "gapFirst", :default 0}]}

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
              {:name "action", :default :none :map DONE-ACTIONS}]
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
              {:name "length", :default INFINITE}]
       :rates #{:dr}
       :doc "Emits a series of incrementing values as they are demanded."}

      {:name "Dgeom",
       :args [{:name "start", :default 1}
              {:name "grow", :default 2}
              {:name "length", :default INFINITE}]
       :rates #{:dr}}

      {:name "Dbufrd",
       :args [{:name "bufnum", :default 0.0}
              {:name "phase", :default 0.0}
              {:name "loop", :default 1.0}],
       :rates #{:dr}}

      {:name "Dbufwr",
       :args [{:name "input", :default 0.0}
              {:name "bufnum", :default 0}
              {:name "phase", :default 0.0}
              {:name "loop", :default 1.0}],
       :rates #{:dr}}

      {:name "Dseq",
       :args [{:name "list", :mode :append-sequence, :array true}
              {:name "repeats", :default 1}],
       :rates #{:dr}
       :doc "Output a sequence of values, possibly repeating multiple times."}

      {:name "Dser"
       :args [{:name "list", :mode :append-sequence, :array true}
              {:name "count", :default 1}],
       :doc "Generates a sequence of values Like dseq, except outputs only count total values, rather than repeating."}

      {:name "Dshuf" :extends "Dseq"
       :doc "Shuffle a sequence once and then output it one or more times."}

      {:name "Drand" :extends "Dseq"
       :doc "Generate a random ordering of an input sequence."}

      {:name "Dxrand" :extends "Dseq"
       :doc "Generate a random ordering of the given sequence without repeating any element until all elements have been returned."}

      {:name "Dswitch1",
       :args [{:name "list", :mode :append-sequence, :array true}
              {:name "index"}],
       :rates #{:dr}
       :doc "A demand rate switch that can be used to select one of multiple demand rate inputs."}

      {:name "Dswitch" :extends "Dswitch1"}

      {:name "Dwhite",
       :args [{:name "lo", :default 0.0}
              {:name "hi", :default 1.0}
              {:name "length", :default INFINITE}]
       :rates #{:dr}
       :doc "Generate a sequence of length random values between lo and hi."}

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
              {:name "length", :default INFINITE}]
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

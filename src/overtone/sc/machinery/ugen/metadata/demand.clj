(ns overtone.sc.machinery.ugen.metadata.demand
  (:use [overtone.sc.machinery.ugen common check]))

(def specs
  [
   {:name "Demand",
    :args [{:name "trig"
            :doc "Can be any signal. A trigger happens when the signal
                  changes from non-positive to positive." }

           {:name "reset"
            :default 0
            :doc "Resets the list of ugens when triggered."}

           {:name "demand-ugens"
            :mode :append-sequence-set-num-outs
            :doc "list of demand rate ugens"}]

    :check [(arg-is-demand-ugen-or-list-of-demand-ugens? :demand-ugens) ]
    :auto-rate true
    :doc "On every trigger it demands the next value from each of the
          demand ugens passed as args.  Used to pull values from the
          other demand rate ugens.

          By design, a reset trigger only resets the demand ugens; it
          does not reset the value at Demand's output. Demand continues
          to hold its value until the next value is demanded, at which
          point its output value will be the first expected item in the
          list." }

   {:name "Duty",
    :args [{:name "dur"
            :default 1.0}

           {:name "reset"
            :default 0.0}

           {:name "action"
            :default 0
            :doc "Default: NO-ACTION"}

           {:name "level"
            :default 1.0}]

    :check (fn [rate num-outs [dur reset & _] ugen spec]
             (if (and (dr? dur)
                      (not (or (dr? reset)
                               (ir? reset)
                               (rate= reset rate))))
               (format "Duration argument must be demand rate, and the
                        reset must be either a constant or demand
                        rate.  (dur: %s, reset: %s)" dur reset)))
    :internal-name true
    :doc "This ugen has been internalised for scserver
          compatibility. Please use the duty cgen instead." }


   {:name "TDuty" :extends "Duty"
    :args [{:name "dur"
            :default 1.0}

           {:name "reset"
            :default 0.0}

           {:name "action"
            :default 0
            :doc "Default: NO-ACTION"}

           {:name "level"
            :default 1.0}

           {:name "gapFirst"
            :default 0}]

    :internal-name true
    :doc "This ugen has been internalised for scserver
          compatibility. Please use the tduty cgen instead." }


   {:name "DemandEnvGen",
    :args [{:name "level"
            :doc "demand ugen (or other ugen) returning level values"}

           {:name "dur"
            :doc "demand ugen (or other ugen) returning time values"}

           {:name "shape"
            :default 1
            :doc "demand ugen (or other ugen) returning shape number -
                  the number given is the shape number"}

           {:name "curve"
            :default 0
            :doc "demand ugen (or other ugen) returning curve values -
                  if shape is 5, this is the curve factor. The possible
                  values are: 0 - flat segments, 1 - linear segments,
                  the default, 2 - natural exponential growth and
                  decay. In this case, the levels must all be nonzero
                  and the have the same sign, 3 - sinusoidal S shaped
                  segments, 4 - sinusoidal segments shaped like the
                  sides of a Welch window, a Float - a curvature value
                  for all segments, an Array of Floats - curvature
                  values for each segments." }

           {:name "gate"
            :default 1.0
            :doc "control rate gate if gate is x >= 1, the ugen runs, if
                  gate is 0 > x > 1, the ugen is released at the next
                  level (doneAction), if gate is x <= 0, the ugen is
                  sampled end held"}

           {:name "reset"
            :default 1.0
            :doc "if reset crosses from nonpositive to positive, the
                  ugen is reset at the next level. If it is > 1, it is
                  reset immediately." }

           {:name "levelScale"
            :default 1.0
            :doc "demand ugen returning level scaling values"}

           {:name "levelBias"
            :default 0.0
            :doc "demand ugen returning level offset values"}

           {:name "timeScale"
            :default 1.0
            :doc "demand ugen returning time scaling values"}

           {:name "action"
            :default 0
            :doc "Default: NO-ACTION"}]
    ;;TODO: Figure out if this init fn is useful in any way
    ;;       :init (fn [rate [l d s c gate reset ls lb ts da] spec]
    ;;               (if (or (ar? gate) (ar? reset))
    ;;                 [l d s c (as-ar gate) (as-ar reset) ls lb ts da]))
    :doc "Plays back break point envelope contours (levels, times,
          shapes) given by demand ugens. The next values are called when
          the next node is reached." }

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
   ;; TODO: understand forceAudioRateInputsIntoUGenGraph
   ;; which is implemented in the pseudo-ugen DUGen parent of below


   {:name "Dseries",
    :args [{:name "length"
            :default Float/POSITIVE_INFINITY
            :doc "Default: positive infinity."}

           {:name "start"
            :default 1}

           {:name "step"
            :default 1}]

    :rates #{:dr}
    :internal-name true
    :doc "This ugen has been internalised for scserver
          compatibility. Please use the dseries cgen instead."}


   {:name "Dgeom",
    :args [{:name "length"
            :default Float/POSITIVE_INFINITY
            :doc "Default: positive infinity"}

           {:name "start"
            :default 1}

           {:name "grow"
            :default 2}]

    :rates #{:dr}
    :internal-name true
    :doc "This ugen has been internalised for scserver
          compatibility. Please use the dgeom cgen instead."}


   {:name "Dbufrd",
    :args [{:name "bufnum"
            :default 0.0
            :doc "buffer number to read from"}

           {:name "phase"
            :default 0.0
            :doc "index into the buffer"}

           {:name "loop"
            :default 1.0
            :doc "when phase exceeds number of frames in buffer, loops
                  when set to 1"}],
    :rates #{:dr}
    :doc "Read values from a buffer on demand, using phase (index) value
          that is also pulled on demand. All inputs can be either demand
          ugen or any other ugen." }


   {:name "Dbufwr",
    :args [{:name "bufnum"
            :default 0}

           {:name "phase"
            :default 0.0}

           {:name "input"
            :default 0.0}

           {:name "loop"
            :default 1.0}]

    :rates #{:dr}
    :internal-name true
    :doc "This ugen has been internalised for scserver
          compatibility. Please use the dbufwr cgen instead." }


   {:name "Dseq",
    :args [{:name "list"
            :mode :append-sequence,
            :array true
            :doc "array of values or other ugens"}

           {:name "num-repeats"
            :default 1
            :doc "number of repeats"}]

    :rates #{:dr}
    :doc "Demand rate sequence generator. Outputs a sequence of values,
          possibly repeating multiple times. Use INF as a repeat val to
          create an endless loop." }


   {:name "Dser"
    :args [{:name "list"
            :mode :append-sequence
            :array true
            :doc "array of values or other ugens"}

           {:name "count"
            :default 1
            :doc "number of values to return"}],

    :rates #{:dr}
    :doc "Demand rate sequence generator. Generates a sequence of values
          like dseq, except outputs only count total values, rather than
          repeating."}


   {:name "Dshuf" :extends "Dseq"
    :doc "Demand rate random sequence generator. Shuffle a sequence once
         and then output it one or more times." }


   {:name "Drand" :extends "Dseq"
    :doc "Demand rate random sequence generator. Generate a random
          ordering of an input sequence." }


   {:name "Dxrand" :extends "Dseq"
    :doc "Demand rate random sequence generator. Generate a random
          ordering of the given sequence without repeating any element
          until all elements have been returned." }


   {:name "Dswitch1",
    :args [{:name "list"
            :mode :append-sequence
            :array true :doc "array of values or other ugens"}

           {:name "index"
            :doc "which of the inputs to return"}]

    :rates #{:dr}
    :doc "A demand rate switch that can be used to select one of
          multiple demand rate inputs." }


   {:name "Dswitch" :extends "Dswitch1"
    :doc "A demand rate switch. In difference to Dswitch1, Dswitch
          embeds all items of an input demand ugen first before looking
          up the next index." }


   {:name "Dwhite",
    :args [
           {:name "length"
            :default Float/POSITIVE_INFINITY
            :doc "Default: positive infinity" }

           {:name "lo"
            :default 0.0 }

           {:name "hi"
            :default 1.0 }]

    :rates #{:dr}
    :internal-name true
    :doc "This ugen has been internalised for scserver
          compatibility. Please use the dwhite cgen instead." }


   {:name "Diwhite" :extends "Dwhite"
    :doc "This ugen has been internalised for scserver
          compatibility. Please use the diwhite cgen instead." }


   {:name "Dbrown",
    :args [{:name "length"
            :default Float/POSITIVE_INFINITY
            :doc "Default: positive infinity"}

           {:name "lo"
            :default 0.0}

           {:name "hi"
            :default 1.0}

           {:name "step"
            :default 0.01}]
    :rates #{:dr}
    :internal-name true
    :doc "This ugen has been internalised for scserver
          compatibility. Please use the dbrown cgen instead." }


   {:name "Dibrown" :extends "Dbrown"
    :doc "This ugen has been internalised for scserver
          compatibility. Please use the dibrown cgen instead." }


   {:name "Dstutter",
    :args [{:name "num-repeats"
            :doc "number of repeats (can be a demand ugen)"}

           {:name "in"
            :doc "input ugen"}]

    :rates #{:dr}
    :check (nth-input-stream? 1)
    :doc "Replicates input values n times on demand.  Both inputs can be
          demand rate ugens." }


   {:name "Donce",
    :args [{:name "in"}],
    :rates #{:dr}}


   {:name "Dpoll",
    :args [{:name "in"
            :doc "demand ugen to poll values from"}

           {:name "trig-id"
            :default -1
            :doc "if greater than 0, a '/tr' message is sent back to the
                  client (similar to SendTrig)"}

           {:name "label"
            :default "dpoll-val"
            :mode :append-string
            :doc "a label string"}

           {:name "run"
            :default 1.0
            :doc "activation switch 0 or 1 (can be a demand ugen)"}]
   :rates #{:dr}
    :check (arg-is-demand-ugen? :in)
    :doc "Print the value of an input demand ugen. The print-out is in
          the form: label: value block offset: offset.

          WARNING: Printing values from the Server is intensive for the
          CPU. Poll should be used for debugging purposes."}])

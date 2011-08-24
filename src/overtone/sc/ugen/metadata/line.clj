(ns overtone.sc.ugen.metadata.line
  (:use [overtone.sc.ugen common constants]))

(def specs
     [
      {:name "Line",
       :args [{:name "start", :default 0.0 :doc "Starting value"}
              {:name "end", :default 1.0 :doc "Ending value"}
              {:name "dur", :default 1.0 :doc "Duration in seconds"}
              {:name "action", :default NO-ACTION :doc "A done action to be evaluated when the line is completed."}]
       :doc "Generates a line from the start value to the end value."}

      {:name "XLine",
       :args [{:name "start", :default 1.0 :doc "Starting value"}
              {:name "end", :default 2.0 :doc "Ending value"}
              {:name "dur", :default 1.0 :doc "Duration in seconds"}
              {:name "action", :default NO-ACTION :doc "A done action to be evaluated when the line is completed."}]
       :doc "Generates an exponential curve from the start value to the end value. Both the start and end values
 must be non-zero and have the same sign."}

      {:name "LinExp",
       :args [{:name "in", :default 0.0 :doc "Input to convert"}
              {:name "srclo", :default 0.0 :doc "Lower limit of input range"}
              {:name "srchi", :default 1.0 :doc "Upper limit of input range"}
              {:name "dstlo", :default 1.0 :doc "Lower limit of output range"}
              {:name "dsthi", :default 2.0 :doc "Upper limit of output range"}]
       :doc "Convert from a linear range to an exponential range. The dstlo and dsthi arguments must be nonzero and have the same sign."
       :auto-rate true}

      {:name "LinLin",
       :args [{:name "in", :default 0.0 :doc "Input to convert"}
              {:name "srclo", :default 0.0 :doc "Lower limit of input range"}
              {:name "srchi", :default 1.0 :doc "Upper limit of input range"}
              {:name "dstlo", :default 1.0 :doc "Lower limit of output range"}
              {:name "dsthi", :default 2.0 :doc "Upper limit of output range"}]
       :doc "Map values from one linear range to another"
       :auto-rate true}

      {:name "AmpComp",
       :args [{:name "freq", :default 261.6256 :doc "Input frequency value. For freq == root, the output is 1.0."} ; default value of (midicps 60)
              {:name "root", :default 261.6256 :doc "Root freq relative to which the curve is calculated (usually lowest freq)"}
              {:name "exp", :default 0.3333 :doc "Exponent: how steep the curve decreases for increasing freq"}],
       :rates #{:ir :ar :kr}
       :check (when-ar (first-input-ar "freq must be audio rate"))
       :doc "amplitude compensation: because higher frequencies are normally perceived as louder. Note that for frequencies very much smaller than root the amplitudes can become very high. In this case limit the freqor use amp-comp-a"}

      {:name "AmpCompA" :extends "AmpComp"
       :args [{:name "freq", :default 1000.0 :doc "Input frequency value. For freq == root, the output is rootAmp"}
              {:name "root", :default 0 :doc "Root freq relative to which the curve is calculated (usually lowest freq)"}
              {:name "minAmp", :default 0.32 :doc "Amplitude at the minimum point of the curve (around 2512 Hz) "}
              {:name "rootAmp", :default 1.0 :doc "Amplitude at the root frequency."}]
       :doc "amplitude compensation: ANSI a-weighting curve for lowering highs. Apart from freq, the args are not modulatable."}


      {:name "K2A",
       :args [{:name "in", :default 0.0 :doc "input signal"}],
       :rates #{:ar}
       :doc "control rate to audio rate converter via linear interpolation."}


      {:name "A2K",
       :args [{:name "in", :default 0.0 :doc "input signal"}],
       :rates #{:kr}
       :doc "audio rate to control rate converter via linear interpolation"}


      {:name "T2K" :extends "A2K"
       :check (first-input-ar)
       :doc "audio rate trigger to control rate trigger converter. Uses the maxiumum trigger in the input during each control period."}

      {:name "T2A",
       :args [{:name "in", :default 0.0 :doc "input signal"}
              {:name "offset", :default 0 :doc "sample offset within control period"}],
       :rates #{:ar}
       :doc "control rate trigger to audio rate trigger converter (maximally one per control period)."}

      {:name "DC",
       :args [{:name "in", :mode :append-sequence-set-num-outs :doc "constant value to output, cannot be modulated, set at initialisation time"}]
       :doc "outputs the initial value you give it."}

      {:name "Silent",
       :args [{:name "numChannels", :mode :num-outs, :default 1 :doc "Number of channels of silence."}],
       :rates #{:ar}
       :doc "Continuously outputs 0"}])

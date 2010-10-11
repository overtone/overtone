(ns overtone.sc.ugen.line
  (:use (overtone.sc.ugen common)))

(def specs
     [
      {:name "Line",
       :args [{:name "start", :default 0.0}
              {:name "end", :default 1.0}
              {:name "dur", :default 1.0}
              {:name "action", :default 0 :map DONE-ACTIONS}]
       :doc "Generates a line from the start value to the end value."}

      {:name "XLine",
       :args [{:name "start", :default 1.0}
              {:name "end", :default 2.0}
              {:name "dur", :default 1.0}
              {:name "action", :default 0 :map DONE-ACTIONS}]
       :doc "Generates an exponential curve from the start value to the end value. Both the start and end values
 must be non-zero and have the same sign."}

      {:name "LinExp",
       :args [{:name "in", :default 0.0}
              {:name "srclo", :default 0.0}
              {:name "srchi", :default 1.0}
              {:name "dstlo", :default 1.0}
              {:name "dsthi", :default 2.0}]
       :doc "Convert from a linear range to an exponential range."}

      {:name "LinLin",
       :args [{:name "in", :default 0.0}
              {:name "srclo", :default 0.0}
              {:name "srchi", :default 1.0}
              {:name "dstlo", :default 1.0}
              {:name "dsthi", :default 2.0}]
       :doc "map values from one linear range to another"}

      {:name "AmpComp",
       :args [{:name "freq", :default 261.6256} ; default value of (midicps 60)
              {:name "root", :default 261.6256}
              {:name "exp", :default 0.3333}],
       :rates #{:ir :ar :kr}
       :check (when-ar (first-input-ar "freq must be audio rate"))
       :doc "amplitude compensation: because higher frequencies are normally perceived as louder"}

      {:name "AmpCompA" :extends "AmpComp"
       :args [{:name "freq", :default 1000.0}
              {:name "root", :default 0}
              {:name "minAmp", :default 0.32}
              {:name "rootAmp", :default 1.0}]
       :doc "amplitude compensation: ANSI a-weighting curve for lowering highs"}


      {:name "K2A",
       :args [{:name "in", :default 0.0}],
       :rates #{:ar}
       :doc "control rate to audio rate converter"}


      {:name "A2K",
       :args [{:name "in", :default 0.0}],
       :rates #{:kr}
       :doc "audio rate to control rate converter"}


      {:name "T2K" :extends "A2K"
       :check (first-input-ar)
       :doc "audio rate to control rate trigger converter"}

      {:name "T2A",
       :args [{:name "in", :default 0.0}
              {:name "offset", :default 0}],
       :rates #{:ar}
       :doc "control rate to audio rate trigger converter"}

      {:name "DC",
       :args [{:name "in", :mode :append-sequence-set-num-outs}]
       :doc ""}

      {:name "Silent",
       :args [{:name "numChannels", :mode :num-outs, :default 1}],
       :rates #{:ar}
       :doc ""}])

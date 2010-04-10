(ns overtone.core.ugen.trig
  (:use (overtone.core.ugen common)))

;TODO: 
;* figure out what the signal-range is about
;* implement [same-rate-as-first-input] checker
(def specs
     [
      {:name "Trig1",
       :args [{:name "in", :default 0.0}
              {:name "dur", :default 0.1}]
       :signal-range :unipolar
       :doc "outputs one for dur seconds whenever the input goes from negative to positive"}
      
      {:name "Trig", :extends "Trig1"
       :doc "outputs the level of the triggering input when it goes from negative to positive"}

      {:name "TDelay",
       :args [{:name "in", :default 0.0}
              {:name "dur", :default 0.1}]
       :signal-range :unipolar
       :check [same-rate-as-first-input]
       :doc "delays an input trigger by dur, ignoring other triggers in the meantime"}

      {:name "SendTrig",
       :args [{:name "in", :default 0.0}
              {:name "id", :default 0}
              {:name "value", :default 0.0}],
       :num-outs 0
       :check [same-rate-as-first-input]
       :doc "on receiving a trigger sends a :trigger event with id and value"}
      
      {:name "SendReply",
       :args [{:name "trig", :default 0.0}
              {:name "cmd-name", :default "/reply"}
              {:name "values", :default 0.0 :mode :append-sequence}
              {:name "reply-id", :default -1}]
       :doc "send an array of values from the server to all notified clients"}

      {:name "Latch",
       :args [{:name "in", :default 0.0}
              {:name "trig", :default 0.0}]
       :doc "holds the input signal value when triggered"}

      {:name "Gate", :extends "Latch"
       :doc "lets signal flow when trig is positive, otherwise holds last input value"}
      
      {:name "PulseCount",
       :args [{:name "trig", :default 0.0}
              {:name "reset", :default 0.0}]
       :check [same-rate-as-first-input]
       :doc "each input trigger increments a counter value that is output."}

      {:name "SetResetFF", :extends "PulseCount"}
            
      {:name "Peak", :extends "PulseCount"
       :doc "outputs the peak amplitude of the signal so far, a trigger resets to current value"}
      
      {:name "RunningMin", :extends "PulseCount"
       :doc ""}
      
      {:name "RunningMax", :extends "PulseCount"
       :doc ""}

      {:name "Stepper",
       :args [{:name "trig", :default 0}
              {:name "reset", :default 0}
              {:name "min", :default 0}
              {:name "max", :default 7}
              {:name "step", :default 1}
              {:name "resetval" :default 1}] ; TODO MAYBE? allow :default :min
       :check [same-rate-as-first-input]
       :doc "triggers increment a counter that loops around from max to min"}
      
      {:name "PulseDivider",
       :args [{:name "trig", :default 0.0}
              {:name "div", :default 2.0}
              {:name "start-val", :default 0.0}]
       :doc "outputs a trigger every div input triggers"}
      
      {:name "ToggleFF",
       :args [{:name "trig", :default 0.0}]
       :doc "flip-flops between zero and one each trigger"}

      {:name "ZeroCrossing",
       :args [{:name "in", :default 0.0}]
       :check [same-rate-as-first-input]
       :doc ""}
      
      {:name "Timer",
       :args [{:name "trig", :default 0.0}]
       :check [same-rate-as-first-input]
       :doc "outputs time since last trigger"}
      
      {:name "Sweep",
       :args [{:name "trig", :default 0.0}
              {:name "rate", :default 1.0}]
       :doc "outputs a linear increasing signal by rate/second on trigger"}
      
      {:name "Phasor",
       :args [{:name "trig", :default 0.0}
              {:name "rate", :default 1.0}
              {:name "start", :default 0.0}
              {:name "end", :default 1.0}
              {:name "resetPos", :default 0.0}]
       :doc "output a signal increasing at rate from start to end, often used to index into buffers"}
      
      {:name "PeakFollower",
       :args [{:name "in", :default 0.0}
              {:name "decay", :default 0.999}]
       :doc "outputs the peak signal amplitude, falling with decay over time until reaching signal level"}
      
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
       :num-outs 2
       :doc ""}
      
      {:name "InRange",
       :args [{:name "in", :default 0.0}
              {:name "lo", :default 0.0}
              {:name "hi", :default 1.0}]
       :doc "tests if a signal is between lo and hi"}

      {:name "Fold", :extends "InRange"}
      
      {:name "Clip", :extends "InRange"}
      
      {:name "Wrap", :extends "InRange"}
      
      {:name "Schmidt", :extends "InRange"
       :doc "outout one when signal greater than high, and zero when lower than low."}
      
	  ;; TODO maybe allow a rect datatype as arg
	  ;;      and write init function to handle it
      {:name "InRect",
       :args [{:name "x", :default 0.0}
              {:name "y", :default 0.0}
              {:name "left"}
              {:name "top"}
              {:name "right"}
              {:name "bottom"}]
       :doc "outputs one if the 2d coordinate of x,y input values falls inside a rectangle, else zero"}

      {:name "Trapezoid",
       :args [{:name "in", :default 0.0}
              {:name "a", :default 0.2}
              {:name "b", :default 0.4}
              {:name "c", :default 0.6}
              {:name "d", :default 0.8}]}
      
      {:name "MostChange",
       :args [{:name "a", :default 0.0}
              {:name "b", :default 0.0}]
       :doc "output whichever signal changed the most"}
      
      {:name "LeastChange", :extends "MostChange"
       :doc "output whichever signal changed the least"}
      
      {:name "LastValue",
       :args [{:name "in", :default 0.0}
              {:name "diff", :default 0.01}]
       :doc "output the last value before the input changed by a threshold of diff"}])


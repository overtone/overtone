(ns overtone.sc.machinery.ugen.metadata.trig
  (:use [overtone.sc.machinery.ugen common]))

;;TODO:
;;* figure out what the signal-range is about
;;* implement [same-rate-as-first-input] checker
;;* why are some ugens marked as unipolar and is this marking consistant?
;;* same as above for the same-rate-as-first-input check

(def specs
     [

      {:name "TWindex"
       :args [{:name "trig" :doc "trigger. Trigger can be any signal. A trigger happens when the signal changes from non-positive to positive."}
              {:name "array", :array true :doc "list of probabilities"}
              {:name "normalize", :default 0 :doc "normalise flag - 0 off, 1 on"}]
       :default-rate :kr
       :doc "When triggered, returns a random index value based on array as a list of probabilities. By default the list of probabilities should sum to 1.0, when the normalize flag is set to 1, the values get normalized by the ugen (less efficient)"}

      {:name "Trig1"
       :args [{:name "trig", :default 0.0 :doc "trigger. Trigger can be any signal. A trigger happens when the signal changes from non-positive to positive."}
              {:name "dur", :default 0.1 :doc "duration of the trigger output in seconds."}]
       :signal-range :unipolar
       :default-rate :kr
       :doc "outputs one for dur seconds whenever the input goes from negative to positive"}

      {:name "Trig", :extends "Trig1"
       :doc "When a nonpositive to positive transition occurs at the input, Trig outputs the level of the triggering input for the specified duration, otherwise it outputs zero."}

      {:name "TDelay"
       :args [{:name "trig", :default 0.0 :doc "input trigger signal."}
              {:name "dur", :default 0.1 :doc "delay time in seconds."}]
       :signal-range :unipolar
       :default-rate :kr
       :check [(same-rate-as-first-input)]
       :doc "delays an input trigger by dur, ignoring other triggers in the meantime"}

      {:name "SendTrig"
       :args [{:name "in", :default 0.0 :doc "input trigger signal"}
              {:name "id", :default 0 :doc "an integer that will be passed with the trigger message. This is useful if you have more than one SendTrig in a SynthDef"}
              {:name "value", :default 0.0 :doc "a UGen or float that will be polled at the time of trigger, and its value passed with the trigger message"}]
       :default-rate :kr
       :num-outs 0
       :check [(same-rate-as-first-input)]
       :doc "on receiving a trigger sends a :trigger event with id and value. This command is the mechanism that synths can use to trigger events in clients.

The trigger message sent back to the client is this:

/tr						a trigger message

	int - node ID

	int - trigger ID

	float - trigger value

"}

      {:name "SendReply"
       :args [{:name "trig", :default 0.0 :doc "input trigger signal"}
              {:name "cmd-name", :default "/reply" :doc "a string or symbol, as a message name." :mode :append-string}
              {:name "values", :default 0.0 :mode :append-sequence :doc "array of ugens, or valid ugen inputs"}
              {:name "reply-id", :default -1 :doc "integer id (similar to send-trig)"}]
       :default-rate :kr
       :doc "send an array of values from the server to all notified clients

cmd-name

	int - node ID

	int - reply ID

	... floats - values."}

      {:name "Latch"
       :args [{:name "in", :default 0.0 :doc "input signal."}
              {:name "trig", :default 0.0 :doc "trigger. Trigger can be any signal. A trigger happens when the signal changes from non-positive to positive."}]
       :default-rate :kr
       :doc "holds the input signal value when triggered"}

      {:name "Gate", :extends "Latch"
       :doc "lets signal flow when trig is positive, otherwise holds last input value"}

      {:name "PulseCount"
       :args [{:name "trig", :default 0.0 :doc "trigger. Trigger can be any signal. A trigger happens when the signal changes from non-positive to positive."}
              {:name "reset", :default 0.0 :doc "resets the counter to zero when triggered."}]
       :check (same-rate-as-first-input)
       :default-rate :kr
       :doc "each input trigger increments a counter value that is output."}

      {:name "SetResetFF", :extends "PulseCount"
       :doc "When a trigger is received the output is set to 1.0 Subsequent triggers have no effect When a trigger is received in the reset input, the output is set back to 0.0

One use of this is to have some precipitating event cause something to happen until you reset it.
"}

      {:name "Peak", :extends "PulseCount"
       :doc "outputs the peak amplitude of the signal so far, a trigger resets to current value"}

      {:name "RunningMin", :extends "Latch"
       :doc "Outputs the minimum value received at the input. When triggered, the minimum output value is reset to the current value."}

      {:name "RunningMax", :extends "Latch"
       :doc "Outputs the maximum value received at the input. When triggered, the maximum output value is reset to the current value."}

      {:name "Stepper"
       :args [{:name "trig", :default 0 :doc "trigger. Trigger can be any signal. A trigger happens when the signal changes from non-positive to positive."}
              {:name "reset", :default 0 :doc "resets the counter to resetval when triggered."}
              {:name "min", :default 0 :doc "minimum value of the counter."}
              {:name "max", :default 7 :doc "maximum value of the counter."}
              {:name "step", :default 1 :doc "step value each trigger. May be negative."}
              {:name "resetval" :default 1 :doc "value to which the counter is reset when it receives a reset trigger."}] ; TODO MAYBE? allow :default :min
       :check (same-rate-as-first-input)
       :default-rate :kr
       :doc "triggers increment a counter which is output as a signal. The counter loops around from max to min by step increments"}

      {:name "PulseDivider"
       :args [{:name "trig", :default 0.0 :doc "trigger. Trigger can be any signal. A trigger happens when the signal changes from non-positive to positive."}
              {:name "div", :default 2.0 :doc "number of pulses to divide by."}
              {:name "start-val", :default 0.0 :doc "starting value for the trigger count. This lets you start somewhere in the middle of a count, or if startCount is negative it adds that many counts to the first time the output is triggers."}]
       :default-rate :kr
       :doc "outputs a trigger every div input triggers"}

      {:name "ToggleFF"
       :args [{:name "trig", :default 0.0 :doc "trigger input"}]
       :default-rate :kr
       :doc "flip-flops between zero and one each trigger"}

      {:name "ZeroCrossing"
       :args [{:name "in", :default 0.0 :doc "input signal"}]
       :default-rate :kr
       :check (same-rate-as-first-input)
       :doc "Outputs a frequency based upon the distance between interceptions of the X axis. The X intercepts are determined via linear interpolation so this gives better than just integer wavelength resolution. This is a very crude pitch follower, but can be useful in some situations."}

      {:name "Timer"
       :args [{:name "trig", :default 0.0 :doc "trigger input"}]
       :check (same-rate-as-first-input)
       :default-rate :kr
       :doc "outputs time since last trigger"}

      {:name "Sweep"
       :args [{:name "trig", :default 0.0 :doc "trigger input"}
              {:name "rate", :default 1.0 :doc "rate in seconds"}]
       :default-rate :kr
       :doc "outputs a linear increasing signal by rate/second on trigger"}

      {:name "Phasor"
       :args [{:name "trig", :default 0.0 :doc "When triggered, reset value to resetPos (default: 0, Phasor outputs start initially)"}
              {:name "rate", :default 1.0 :doc "The amount of change per sample i.e at a rate of 1 the value of each sample will be 1 greater than the preceding sample"}
              {:name "start", :default 0.0 :doc "Starting point of the ramp"}
              {:name "end", :default 1.0 :doc "End point of the ramp"}
              {:name "resetPos", :default 0.0  :doc "The value to jump to upon receiving a trigger"}]
       :default-rate :kr
       :doc "Phasor is a linear ramp between start and end values. When its trigger input crosses from non-positive to positive, Phasor's output will jump to its reset position. Upon reaching the end of its ramp Phasor will wrap back to its start. N.B. Since end is defined as the wrap point, its value is never actually output."}

      {:name "PeakFollower"
       :args [{:name "in", :default 0.0 :doc "input signal."}
              {:name "decay", :default 0.999 :doc "decay factor."}]
       :default-rate :kr
       :doc "outputs the peak signal amplitude, falling with decay over time until reaching signal level"}

      {:name "Pitch"
       :args [{:name "in", :default 0.0 :doc ""}
              {:name "initFreq", :default 440.0}
              {:name "minFreq", :default 60.0}
              {:name "maxFreq", :default 4000.0}
              {:name "execFreq", :default 100.0}
              {:name "maxBinsPerOctave", :default 16}
              {:name "median", :default 1}
              {:name "ampThreshold", :default 0.01}
              {:name "peakThreshold", :default 0.5}
              {:name "downSample", :default 1}]
       :rates #{:kr}
       :num-outs 2
       :doc "Autocorrelation pitch follower

This is a better pitch follower than ZeroCrossing, but more costly of CPU. For most purposes the default settings can be used and only in needs to be supplied. Pitch returns two values (via an Array of OutputProxys, see the OutputProxy help file), a freq which is the pitch estimate and hasFreq, which tells whether a pitch was found. Some vowels are still problematic, for instance a wide open mouth sound somewhere between a low pitched short 'a' sound as in 'sat', and long 'i' sound as in 'fire', contains enough overtone energy to confuse the algorithm."}

      {:name "InRange"
       :args [{:name "in", :default 0.0 :doc "input signal"}
              {:name "lo", :default 0.0 :doc "low threshold"}
              {:name "hi", :default 1.0 :doc "high threshold"}]
       :default-rate :kr
       :doc "Tests if a signal is between lo and hi"}

      {:name "Fold", :extends "InRange"
       :doc "Folds input wave to within the lo and hi thresholds. This differs from the BinaryOpUGen fold2 in that it allows one to set both low and high thresholds."}

      {:name "Clip"
       :args [{:name "in", :default 0.0 :doc "The signal to be clipped"}
              {:name "lo", :default 0.0, :doc "Low threshold of clipping. Must be less then hi"}
              {:name "hi", :default 1.0, :doc "High threshold of clipping. Must be greater then lo"}]
       :default-rate :kr
       :doc "Clip a signal outside given thresholds. This differs from the BinaryOpUGen clip2 in that it allows one to set both low and high thresholds."}

      {:name "Wrap", :extends "InRange"
       :doc "Wraps input wave to the low and high thresholds. This differs from the BinaryOpUGen wrap2 in that it allows one to set both low and high thresholds."}

      {:name "Schmidt", :extends "InRange"
       :doc "outout one when signal greater than high, and zero when lower than low."}

	  ;; TODO maybe allow a rect datatype as arg
	  ;;      and write init function to handle it
      {:name "InRect"
       :args [{:name "x", :default 0.0 :doc "X component signal"}
              {:name "y", :default 0.0 :doc "Y component signal"}
              {:name "left"}
              {:name "top"}
              {:name "right"}
              {:name "bottom"}]
       :default-rate :kr
       :doc "outputs one if the 2d coordinate of x,y input values falls inside a rectangle, else zero"}

      {:name "Trapezoid"
       :args [{:name "in", :default 0.0}
              {:name "a", :default 0.2}
              {:name "b", :default 0.4}
              {:name "c", :default 0.6}
              {:name "d", :default 0.8}]
       :default-rate :kr}

      {:name "MostChange"
       :args [{:name "a", :default 0.0 :doc "first input"}
              {:name "b", :default 0.0 :doc "second input"}]
       :default-rate :kr
       :doc "output whichever signal changed the most"}

      {:name "LeastChange", :extends "MostChange"
       :doc "output whichever signal changed the least"}

      {:name "LastValue"
       :args [{:name "in", :default 0.0 :doc "input signal"}
              {:name "diff", :default 0.01 :doc "difference threshold"}]
       :default-rate :kr
       :doc "output the last value before the input changed by a threshold of diff"}])

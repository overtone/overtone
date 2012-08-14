(ns overtone.sc.machinery.ugen.metadata.trig
  (:use [overtone.sc.machinery.ugen common check]))

;;TODO:
;;* figure out what the signal-range is about
;;* implement [same-rate-as-first-input] checker
;;* why are some ugens marked as unipolar and is this marking consistant?
;;* same as above for the same-rate-as-first-input check

(def specs
  [

   {:name "TWindex"
    :description "Triggered window"
    :doc "When triggered, returns a random index value based on array as
          a list of probabilities. By default the list of probabilities
          should sum to 1.0, when the normalize flag is set to 1, the
          values get normalized by the ugen (less efficient)."

    :args [{:name "trig"
            :doc "Trigger - can be any signal. A trigger happens when
                  the signal changes from non-positive to positive." }

           {:name "array",
            :doc  "list of probabilities"
            :array true }

           {:name "normalize",
            :doc "normalise flag - 0 off, 1 on"
            :default 0}]
    :default-rate :kr}


   {:name "Trig1"
    :description "Timed trigger"
    :doc "Outputs one for dur seconds whenever the input goes from
          negative to positive, otherwise outputs 0."

    :args [{:name "trig",
            :doc "trigger. Trigger can be any signal. A trigger happens
                  when the signal changes from non-positive to
                  positive."
            :default 0.0}

           {:name "dur",
            :doc  "duration of the trigger output in seconds."
            :default 0.1 }]
    :signal-range :unipolar
    :default-rate :kr}


   {:name "Trig" :extends "Trig1"
    :description "Timed trigger"
    :doc "When a nonpositive to positive transition occurs at the input,
          Trig outputs the level of the triggering input for the
          specified duration, otherwise it outputs zero."}


   {:name "TDelay"
    :description "Trigger delay"
    :doc "Delays an input trigger by dur, ignoring other triggers in the
          meantime"

    :args [{:name "trig",
            :doc "input trigger signal."
            :default 0.0 }

           {:name "dur"
            :doc "delay time in seconds."
            :default 0.1 }]
    :signal-range :unipolar
    :default-rate :kr
    :check [(same-rate-as-first-input)]}


   {:name "SendTrig"
    :description "Send a /tr OSC message to Overtone"
    :doc "On receiving a trigger sends a :trigger event with id and
          value. This command is the mechanism that synths can use to
          trigger events in clients.

          The trigger message sent back to the client is this:

          /tr - trigger message

          int   - node ID

          int   - trigger ID

          float - trigger value"

    :args [{:name "in"
            :doc "input trigger signal"
            :default 0.0 }

           {:name "id"
            :doc "an integer that will be passed with the trigger
                  message. This is useful if you have more than one
                  SendTrig in a SynthDef"
            :default 0 }

           {:name "value"
            :doc "A UGen or float that will be polled at the time of
                  trigger, and its value passed with the trigger
                  message"
            :default 0.0 }]
    :default-rate :kr
    :num-outs 0
    :check [(same-rate-as-first-input)]}


   {:name "SendReply"
    :description "Send information via OSC to Overtone"
    :doc "Send an array of values from the server via an
          message. The OSC message is formed with cmd-name as the path,
          followed by two compulsary args: node-id (the id of the node
          that sent the message) and reply-id (the value specified in
          the params). These args are then followed by the list of
          values specified in the params.

          For example, if the ugen is used as follows:

          (send-reply tr  \"/foobar\" [1 2 3] 42)

          When the trig tr triggers, Overtone will receive an event that
          looks like the following (where 32 represents the node-id of
          the synth that sent the message):

          {:path \"/foobar\" :args (32 42 1 2 3)}

          You can register to respond to this event as follows:

          (on-event \"/foobar\" (fn [msg] (println msg)) ::handle-foobar)"
    :args [{:name "trig"
            :doc "Input trigger signal"
            :default 0.0 }

           {:name "cmd-name"
            :doc "A string or symbol, as a message name."
            :default "/reply"
            :mode :append-string }

           {:name "values"
            :doc "Array of ugens, or valid ugen inputs"
            :default 0.0
            :mode :append-sequence }

           {:name "reply-id"
            :doc "Integer id (similar to that used by send-trig)"
            :default -1 }]
    :default-rate :kr}


   {:name "Latch"
    :description "Sample and hold"
    :doc "Holds input signal value when triggered."
    :args [{:name "in"
            :doc "Input signal."
            :default 0.0 }

           {:name "trig"
            :doc "Trigger. Trigger can be any signal. A trigger happens
                  when the signal changes from non-positive to
                  positive."
            :default 0.0}]
    :default-rate :kr}


   {:name "Gate" :extends "Latch"
    :description "Gate or hold"
    :doc "Lets signal flow when trig is positive, otherwise holds last
          input value"}


   {:name "PulseCount"
    :description "Pulse counter"
    :doc "Each input trigger increments a counter value that is output."
    :args [{:name "trig"
            :doc "Trigger. Trigger can be any signal. A trigger happens
                  when the signal changes from non-positive to positive."
            :default 0.0 }

           {:name "reset"
            :doc "Resets the counter to zero when triggered."
            :default 0.0 }]
    :check (same-rate-as-first-input)
    :default-rate :kr}


   {:name "SetResetFF", :extends "PulseCount"
    :description "Set-reset flip flop"
    :doc "When a trigger is received the output is set to 1.0 Subsequent
          triggers have no effect When a trigger is received in the
          reset input, the output is set back to 0.0

          One use of this is to have some precipitating event cause
          something to happen until you reset it."}


   {:name "Peak", :extends "PulseCount"
    :description "Track peak signal amplitude"
    :doc "Outputs the peak amplitude of the signal so far, a trigger
          resets to current value"}


   {:name "RunningMin", :extends "Latch"
    :description "Track minimum level"
    :doc "Outputs the minimum value received at the input. When
          triggered, the minimum output value is reset to the current
          value." }


   {:name "RunningMax", :extends "Latch"
    :description "Track maximum level"
    :doc "Outputs the maximum value received at the input. When
          triggered, the maximum output value is reset to the current
          value." }


   {:name "Stepper"
    :description "Pulse counter"
    :doc "Triggers increment a counter which is output as a signal. The
          counter loops around from max to min by step increments"

    :args [{:name "trig"
            :doc "Trigger. Trigger can be any signal. A trigger happens
                  when the signal changes from non-positive to
                  positive."
            :default 0}

           {:name "reset"
            :doc "Resets the counter to resetval when triggered."
            :default 0}

           {:name "min"
            :doc "minimum value of the counter."
            :default 0}

           {:name "max"
            :doc "maximum value of the counter."
            :default 7 }

           {:name "step"
            :doc "step value each trigger. May be negative."
            :default 1 }

           {:name "resetval"
            :doc "value to which the counter is reset when it receives a reset trigger."
            :default 1 }]
    :check (same-rate-as-first-input)
    :default-rate :kr}


   {:name "PulseDivider"
    :description "Pulse divider"
    :doc "Outputs a trigger every div input triggers"
    :args [{:name "trig"
            :doc "Trigger. Trigger can be any signal. A trigger happens
                   when the signal changes from non-positive to
                   positive."
            :default 0.0}

           {:name "div"
            :doc "Number of pulses to divide by."
            :default 2.0}

           {:name "start-val"
            :doc "Starting value for the trigger count. This lets you
                   start somewhere in the middle of a count, or if
                   startCount is negative it adds that many counts to
                   the first time the output is triggers."
            :default 0.0}]
    :default-rate :kr}


   {:name "ToggleFF"
    :description "Toggle flip flop"
    :doc "Flip-flops between zero and one each trigger"
    :args [{:name "trig"
            :doc "trigger input"
            :default 0.0 }]
    :default-rate :kr}


   {:name "ZeroCrossing"
    :description "Zero crossing frequency follower"
    :doc "Outputs a frequency based upon the distance between
          interceptions of the X axis. The X intercepts are determined
          via linear interpolation so this gives better than just
          integer wavelength resolution. This is a very crude pitch
          follower, but can be useful in some situations."
    :args [{:name "in"
            :doc "input signal"
            :default 0.0 }]
    :default-rate :kr
    :check (same-rate-as-first-input)}


   {:name "Timer"
    :description "Trigger timer"
    :doc "Outputs time since last trigger"

    :args [{:name "trig"
            :doc "trigger input"
            :default 0.0 }]
    :check (same-rate-as-first-input)
    :default-rate :kr}


   {:name "Sweep"
    :description "Triggered linear ramp"
    :doc "outputs a linear increasing signal by rate/second when trig input crosses from non-positive to positive"

    :args [{:name "trig"
            :doc "trigger input"
            :default 0.0}

           {:name "rate"
            :doc "rate in seconds"
            :default 1.0 }]
    :default-rate :kr}


   {:name "Phasor"
    :description "Resettable linear ramp between two levels"
    :doc "Phasor is a linear ramp between start and end values. When its
          trigger input crosses from non-positive to positive, Phasor's
          output will jump to its reset position. Upon reaching the end
          of its ramp Phasor will wrap back to its start. N.B. Since end
          is defined as the wrap point, its value is never actually
          output."

    :args [{:name "trig"
            :doc "When triggered, reset value to resetPos (default: 0,
                  phasor outputs start initially)"
            :default 0.0}

           {:name "rate"
            :doc "The amount of change per sample i.e at a rate of 1 the
                  value of each sample will be 1 greater than the
                  preceding sample"
            :default 1.0}

           {:name "start"
            :doc "Starting point of the ramp"
            :default 0.0}

           {:name "end"
            :doc "End point of the ramp"
            :default 1.0}

           {:name "reset-pos"
            :doc "The value to jump to upon receiving a trigger"
            :default 0.0}]
    :default-rate :kr}


   {:name "PeakFollower"
    :description "Track peak signal amplitude"
    :doc "Outputs the peak signal amplitude, falling with decay over
         time until reaching signal level"
    :args [{:name "in"
            :doc "input signal."
            :default 0.0}

           {:name "decay"
            :doc "decay factor."
            :default 0.999}]
    :default-rate :kr}


   {:name "Pitch"
    :description "Autocorrelation pitch follower"
    :doc "This is a better pitch follower than zero-crossing, but more
          costly of CPU. For most purposes the default settings can be
          used and only in needs to be supplied. Pitch returns two
          values (via an Array of OutputProxys, a freq which is the
          pitch estimate and has-freq, which tells whether a pitch was
          found. Some vowels are still problematic, for instance a wide
          open mouth sound somewhere between a low pitched short 'a'
          sound as in 'sat', and long 'i' sound as in 'fire', contains
          enough overtone energy to confuse the algorithm. None of these
          settings are time variable."
    :args [{:name "in"
            :doc "Input signal"}

           {:name "init-freq"
            :doc "Value of output pitch until first pitch detected."
            :default 440.0}

           {:name "min-freq",
            :doc "Minimum frequency of execution."
            :default 60.0}

           {:name "max-freq",
            :doc "Maximum frequency of execution."
            :default 4000.0}

           {:name "exec-freq",
            :doc "The target rate to periodically execute in
                  cps. Clipped between min-freq and max-freq."
            :default 100.0}

           {:name "max-bins-per-octave",
            :doc "Number of lags for course search. A larger value will
                 cause the coarse search to take longer, a smaller value
                 will cause the subsequent fine search to take longer."
            :default 16}

           {:name "median",
            :doc "Median filter value of length median on the output
                  estimation. Helps eliminate outliers and jitter. Value
                  of 1 means no filter."
            :default 1}

           {:name "amp-threshold",
            :doc "Minum peak to peak amplitude of input signal before
                  pitch estimation is performed."
            :default 0.01}

           {:name "peak-threshold",
            :doc "Finds the next peak that is above peak-threshold times
                  the amplitude of the peak at lag zero. A value of 0.5
                  does a pretty good job of eliminating overtones."
            :default 0.5}

           {:name "down-sample",
            :doc "Down sample the input signal by an integer
                  factor. Helps reduce CPU overthead. Also reduces pitch
                  resolution."
            :default 1}

           {:name "clar"
            :doc "Clarity measurement (purity of the pitched signal) if
                  greater than 0."
            :default 0}]
    :rates #{:kr}
    :num-outs 2
    :check (nth-input-stream? 0)}


   {:name "InRange"
    :description "Tests if a signal is within a given range"
    :doc "If in is >= lo and <= hi output 1.0, otherwise output
          0.0. Output is initially zero."
    :args [{:name "in"
            :doc "input signal"
            :default 0.0}

           {:name "lo",
            :doc "low threshold"
            :default 0.0}

           {:name "hi",
            :doc "high threshold"
            :default 1.0}]
    :default-rate :kr}

   {:name "Fold", :extends "InRange"
    :description "Fold a signal outside given thresholds."
    :doc "Folds input wave to within the lo and hi thresholds. This
         differs from the ugen fold2 in that it allows one to
         set both low and high thresholds."}

   {:name "Clip"
    :description "Clip a signal outside given thresholds."
    :doc "Clip a signal outside given thresholds. This differs from the
          ugen clip2 in that it allows one to set both low and high
          thresholds."
    :args [{:name "in",
            :doc "The signal to be clipped"
            :default 0.0}

           {:name "lo",
            :doc "Low threshold of clipping. Must be less then hi"
            :default 0.0,}

           {:name "hi",
            :doc "High threshold of clipping. Must be greater then lo"
            :default 1.0,}]
    :default-rate :kr}

   {:name "Wrap", :extends "InRange"
    :description "Wrap a signal outside given thresholds."
    :doc "Wraps input wave to the low and high thresholds. This differs
         from the ugen wrap2 in that it allows one to set both
         low and high thresholds." }

   {:name "Schmidt", :extends "InRange"
    :description "Schmidt trigger"
    :doc "Outout one when signal greater than high, and zero when lower
         than low." }

   ;; TODO maybe allow a rect datatype as arg
   ;;      and write init function to handle it
   {:name "InRect"
    :description "Test if a point is within a given rectangle."
    :doc "Outputs one if the 2d coordinate of x,y input values falls
          inside a rectangle, else zero"
    :args [{:name "x"
            :doc "X component signal"
            :default 0.0}

           {:name "y"
            :doc "Y component signal"
            :default 0.0}

           {:name "left"}
           {:name "top"}
           {:name "right"}
           {:name "bottom"}]
    :default-rate :kr}

   {:name "Trapezoid"
    :args [{:name "in", :default 0.0}
           {:name "a", :default 0.2}
           {:name "b", :default 0.4}
           {:name "c", :default 0.6}
           {:name "d", :default 0.8}]
    :default-rate :kr}

   {:name "MostChange"
    :description "Output most changed"
    :doc "output whichever signal changed the most"
    :args [{:name "a"
            :doc "first input"
            :default 0.0 }

           {:name "b",
            :doc "second input"
            :default 0.0}]
    :default-rate :kr}

   {:name "LeastChange", :extends "MostChange"
    :description "Output least changed"
    :doc "output whichever signal changed the least"}

   {:name "LastValue"
    :description "Output the last value before the input changed"
    :doc "Output the last value before the input changed by a threshold
          of diff"
    :args [{:name "in",
            :doc "input signal"
            :default 0.0}

           {:name "diff",
            :doc "difference threshold"
            :default 0.01}]
    :default-rate :kr}])

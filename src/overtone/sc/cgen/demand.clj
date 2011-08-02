(ns overtone.sc.cgen.demand
  (:use [overtone.sc.ugen]
        [overtone.sc.ugen constants]
        [overtone.sc.cgen]))

(defcgen duty
  "Expects demand ugen args for dur and level.  Uses successive dur values to determine how long to wait before emitting each level value.

A value is demanded each ugen in the list and output according to a stream of duration values.
The unit generators in the list should be 'demand' rate.

When there is a trigger at the reset input, the demand rate ugens in the list and the duration are reset. The reset input may also be a demand ugen, providing a stream of reset times."

  [dur {:default 1.0 :doc "time values. Can be a demand ugen or any signal. The next level is acquired after duration.
"}
   reset {:default 0.0 :doc "trigger or reset time values. Resets the list of ugens and the duration ugen when triggered. The reset input may also be a demand ugen, providing a stream of reset times."}

   level {:default 1.0 :doc "demand ugen providing the output values."}
   action {:default NO-ACTION :doc "action to perform when the duration stream ends."}]
  (:ar (internal:duty:ar dur reset action level))
  (:kr (internal:duty:kr dur reset action level)))

(defcgen t-duty
  "A value is demanded each ugen in the list and output  as a trigger according to a stream of duration values. The unit generators in the list should be 'demand' rate.

When there is a trigger at the reset input, the demand rate ugens in the list and the duration are reset.The reset input may also be a demand ugen, providing a stream of reset times."

  [dur {:default 1.0 :doc "time values. Can be a demand ugen or any signal. The next trigger value is acquired after the duration provided by the last time value."}
   reset {:default 0.0 :doc "trigger or reset time values. Resets the list of ugens and the duration ugen when triggered. The reset input may also be a demand ugen, providing a stream of reset times."}
   level {:default 1.0 :doc "demand ugen providing the output values."}
   action {:default NO-ACTION :doc "action to perform when the duration stream ends"}]
  (:ar (internal:t-duty:ar dur reset action level))
  (:kr (internal:t-duty:kr dur reset action level)))

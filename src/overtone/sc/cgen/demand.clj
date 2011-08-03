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

(defcgen dseries
  "Generate a series of incrementing values on demand."
  [start {:default 1 :doc "start value"}
   step {:default 1 :doc "step value"}
   length {:default INFINITE :doc "number of values to create"}]
  (:dr (internal:dseries:dr length start step)))

(defcgen dgeom
  "Generate a geometric sequence on demand. The arguments can be a number or any other ugen"
  [start {:default 1, :doc "start value"}
   grow {:default 2, :doc "value by which to grow ( x = x[-1] * grow )"}
   length {:default INFINITE :doc "doc number of values to create"}]
  (:dr (internal:dgeom:dr length start grow)))

(defcgen dbufwr
  "Write a demand sequence into a buffer. All inputs can be either demand ugen or any other ugen."
  [input {:default 0.0 :doc "single channel input"}
   bufnum {:default 0, :doc "buffer number to read from (single channel buffer)"}
   phase {:default 0.0, :doc "index into the buffer"}
   loop {:default 1.0, :doc "when phase exceeds number of frames in buffer, loops when set to 1"}]
  (:dr (internal:dbufwr:dr bufnum phase input loop)))

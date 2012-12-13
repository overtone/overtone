(ns overtone.sc.cgens.info
  (:use [overtone.sc defcgen ugens]))


(defcgen poll
  "Print current value of ugen"
  [trig  {:doc "a non-positive to positive transition telling Poll to return a value"}
   in    {:default 1 :doc "the signal you want to poll"}
   label {:default "polled-val" :doc "A string or symbol to be printed with the polled value"}]
  "Print the current output value of a ugen. Does this via OSC
   communication, therefore there's no need for a trig-id arg - see
   send-trig or send-reply for more capable alternatives."
  (:kr (send-reply:kr trig (str "/overtone/internal/poll/" (name label)) in))
  (:ar (send-reply:ar trig (str "/overtone/internal/poll/" (name label)) in))
  (:default :kr))

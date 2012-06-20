(ns overtone.sc.cgens.env
  (:use [overtone.sc defcgen ugens]))

(defcgen hold
  "Hold an input source for a set period of time and then stop."
  [in           {:doc "input source."
                 :default 0.0}
   hold-time    {:doc "hold time in seconds."
                 :default 1.0}
   release-time {:doc "release time in seconds"
                 :default 0.01}
   done         {:doc "action to take after release"
                 :default nil}]
  "Hold an input source for a set period of time and then stop by
   applying a simple envelope. Takes a hold-time, a release-time, and a
   done action."
  (:ar (let [gate (trig 1 hold-time)
             env  (linen gate 0 1 release-time done)]
         (* in env))))

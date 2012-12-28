(ns overtone.examples.synthesis.poll
  (:use [overtone.live]))

;;With poll you can get scsynth to output debug messages containing the contents of a given stream to stdout.

;;Here we're polling the value of the line ugen at the rate
;;specified by the impulse ugen - 10 times per second.
;;We also specify a string to be prepended to the log message:
(run (poll:kr (impulse:kr 10) (line:kr 0 1 1) "polled-val:"))


;;It's also possible to poll demand ugens with dpoll:
(run (duty:kr 0.5 0 (dpoll (dseries 0 1 INF) -1 "dpolled-val")))


;;Poll only when the mouse is on the left hand side of the screen
(run 10 (duty:kr 0.5 0 (dpoll (dseries 0 1 INF) 0 "dpolled-val" (> 0.5 (mouse-x)))))

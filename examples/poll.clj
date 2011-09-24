(ns examples.poll
  (:use [overtone.live]))

;;With poll you can get scsynth to output debug messages containing the contents of a given stream to stdout.
;;These messages should appear at the same place you see the scsynth boot info.


;;Here we're polling the value of the line ugen at the rate
;;specified by the impulse ugen - 10 times per second.
;;We also specify a string to be prepended to the log message:
(demo (poll:kr (impulse:kr 10) (line:kr 0 1 1) "polled-val:"))


;;It's also possible to poll demand ugens with dpoll:
(demo (duty:kr 0.5 0 0 (dpoll (dseries INF 0 1) -1 "dpolled-val")))


;;Poll only when the mouse is on the left hand side of the screen
(demo 10 (duty:kr 0.5 0 0 (dpoll (dseries INF 0 1) 0 "dpolled-val" (> 0.5 (mouse-x)))))

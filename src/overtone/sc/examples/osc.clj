(ns overtone.sc.examples.osc
  (:use [overtone.sc.machinery defexample]
        [overtone.sc ugens]
        [overtone.sc.cgens info]))

(defexamples impulse
  (:poll
   "Poll an impulse to view its output"
   "Observe how the impulse outputs a steady stream of 0 values interspersed with 1 every 0.5s. Modify the poll-rate and note how if you increase it, you will see more output and more 0s yet never more two 1s per second"
   rate :kr
   [poll-rate {:default 5 :doc "Rate to poll the impulse ugen. Increase to poll more frequently."}]
   "(poll (impulse:kr poll-rate) (impulse:kr 2))"
   contributor "Sam Aaron"))

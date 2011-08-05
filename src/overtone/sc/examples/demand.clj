(ns overtone.sc.examples.demand
  (:use [overtone.sc.ugen]
        [overtone.sc.ugen constants]
        [overtone.sc.cgen.demand]
        [overtone.sc.example]))

(defexamples dibrown
  (:rand-walk
   "Random walk through freqs with rate determined by mouse-x"
   "Here we use dibrown to create an infinite sequence of values between 0 and max where each successive value is no more than step plus or minus the last value. This creates a walk-like effect. We then pull out the values from dibrown using the demand ugen, pulling at the rate defined by the impulse - which is between 1 and 40 depending on the mouse-x coord. Therefore, you can use the mouse to speed up and slow down the walk."
   rate :ar
   [max {:default 15 :doc "Max walk range. Increase to allow for higher freqs."}
    step {:default 1 :doc "Step size. Increase to allow for larger steps (will sound more random)"}]
   "
   (let [vals (dibrown 0 max step INF)
         trig (impulse:kr (mouse-x 1 40 1))
         freq (+ 340 (* 30 (demand trig 0 vals)))]
     (* 0.1 (sin-osc freq)))"
   contributor "Sam Aaron"))

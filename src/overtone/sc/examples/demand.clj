(ns overtone.sc.examples.demand
  (:use [overtone.sc.machinery defexample]
        [overtone.sc ugens]
        [overtone.sc.cgens demand info]))

(defexamples dibrown
  (:rand-walk
   "Random whole number walk through freqs with rate determined by mouse-x"
   "Here we use dibrown to create an infinite sequence of values between 0 and max where each successive value is a whole number no more than step plus or minus the last value. This creates a walk-like effect. We then pull out the values from dibrown using the demand ugen, pulling at the rate defined by the impulse - which is between 1 and 40 depending on the mouse-x coord. Therefore, you can use the mouse to speed up and slow down the walk.

   We poll the current value so you can see the output as well as hear it"
   rate :ar
   [max {:default 15 :doc "Max walk range. Increase to allow for higher freqs."}
    step {:default 1 :doc "Step size. Increase to allow for larger steps (will sound more random)"}]
   "
   (let [vals (dibrown 0 15 1 INF)
         trig (impulse:kr (mouse-x 1 40 1))
         val (demand trig 0 vals)
         poll (poll trig val \"dibrown val:\")
         freq (+ 340 (* 30 val))]
     (* 0.1 (sin-osc freq)))"
   contributed-by "Sam Aaron"))

(defexamples dbrown
  (:rand-walk
   "Random floating point number walk through freqs with rate determined by mouse-x"
   "Here we use dbrown to create an infinite sequence of values between 0 and max where each successive value is a float no more than step plus or minus the last value. This creates a walk-like effect. We then pull out the values from dibrown using the demand ugen, pulling at the rate defined by the impulse - which is between 1 and 40 depending on the mouse-x coord. Therefore, you can use the mouse to speed up and slow down the walk.

    We poll the current value so you can see the output as well as hear it"
   rate :ar
   [max {:default 15 :doc "Max walk range. Increase to allow for higher freqs."}
    step {:default 1 :doc "Step size. Increase to allow for larger steps (will sound more random)"}]
   "
   (let [vals (dbrown 0 15 1 INF)
         trig (impulse:kr (mouse-x 1 40 1))
         val (demand trig 0 vals)
         poll (poll trig val \"dbrown val:\")
         freq (+ 340 (* 30 val))]
     (* 0.1 (sin-osc freq)))"
   contributed-by "Sam Aaron"))

(defexamples diwhite
  (:rand-seq
   "Play a random sequence of integers mapped to freqs with rate determined by mouse-x"
   "Here we use diwhite to create an infinite sequence of random integer values between 0 and max. We then pull out the values from diwhite using the demand ugen, pulling at the rate defined by the impulse - which is between 1 and 40 depending on the mouse-x coord. Therefore, you can use the mouse to speed up and slow down the walk.

    We poll the current value so you can see the output as well as hear it"
   rate :ar
   [max {:default 15 :doc "Max walk range. Increase to allow for higher freqs."}]
   "
   (let [vals (diwhite 0 15 INF)
         trig (impulse:kr (mouse-x 1 40 1))
         val (demand:kr trig 0 vals)
         poll (poll trig val \"diwhite val:\")
         freq (+ 340 (* 30 val))]
     (* 0.1 (sin-osc freq)))"
   contributed-by "Sam Aaron"))

(defexamples dwhite
  (:rand-seq
   "Play a random sequence of floats mapped to freqs with rate determined by mouse-x"
   "Here we use dwhite to create an infinite sequence of random floating point values between 0 and max. We then pull out the values from diwhite using the demand ugen, pulling at the rate defined by the impulse - which is between 1 and 40 depending on the mouse-x coord. Therefore, you can use the mouse to speed up and slow down the walk.

    We poll the current value so you can see the output as well as hear it"
   rate :ar
   [max {:default 15 :doc "Max walk range. Increase to allow for higher freqs."}]
   "
   (let [vals (dwhite 0 15 INF)
         trig (impulse:kr (mouse-x 1 40 1))
         val (demand:kr trig 0 vals)
         poll (poll trig val \"dwhite val:\")
         freq (+ 340 (* 30 val))]
     (* 0.1 (sin-osc freq)))"
   contributed-by "Sam Aaron"))

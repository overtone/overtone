(ns overtone.sc.examples.trig
  (:use [overtone.sc.machinery defexample]
        [overtone.sc ugens]))

(defexamples send-reply
  (:count
   "Send back an OSC message containing a rolling count"
   "Here our example consists of three parts. Firstly we create a trigger called tr which will fire trigger signal every 'rate' times per second. This is used to drive both the stepper and the send-reply. The stepper simply counts from 0 to 12 inclusive repeating back to 0 - each tick of the count is driven by tr. Similarly tr drives the emission of a new OSC message created by send-reply. This specifies an OSC path = /count in this case as well as a list of args (here we just send the step val back) as well as a unique id which allows us to identify this particular reply in a sea of other similar replies.

  In order to view the responses, you need to add an event handler such as the following:

   (on-event \"/count\" (fn [msg] (println msg)) ::foo)"

   rate :kr
   [rate {:default 3 :doc "Rate of count in times per second. Increase to up the count rate"}]
   "
  (let [tr   (impulse rate)
        step (stepper tr 0 0 12)]
    (send-reply tr \"/count\" [step] 42))"
   contributor "Sam Aaron"))

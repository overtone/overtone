(ns examples.internal-metro
  (:use [overtone.live]))

;; A basic demo of how you can use internal impulses to drive the rhythm of a synth

(demo 5
     (let [src1      (sin-osc 440)
           src2      (sin-osc 880)
           root-trig (impulse:kr 100)
           t1        (pulse-divider:kr root-trig 20)
           t2        (pulse-divider:kr root-trig 10)]
       (* 0.2
          (+ (* (decay t1 0.1) src1)
             (* (decay t2 0.1) src2)))))


;; Here's how you can separate the impulses out across synths and connect them
;; together with a control bus

(def c-bus (control-bus))

(defsynth root-trig [rate 100]
 (out:kr c-bus (impulse:kr rate)))

(definst pingr [freq 440 div 20]
 (let [src1 (sin-osc freq)
       t1 (pulse-divider:kr (in:kr c-bus) div)]
   (* (decay t1 0.1) src1)))

(def r-trig (root-trig))
(pingr)
(pingr 440 50)
(pingr 990 40)
(kill pingr)
(ctl r-trig :rate 50)
(stop)

;; Creating an internal metro synth to send trig messages back
;; to Overtone to use for whatever purpose you need.
;;
;; Here, we create a synth called metro-synth which has two control params:
;; c-bus and rate. c-bus represents the bus to output trigger information to
;; and rate is the beats-per-second. We then pass the rate to an impulse ugen
;; running at control rate - we bind this to the var trigger. This trigger is
;; pthen used for three things:
;;
;; 1) it's passed through a stepper which wraps a count between min and max which we bind to count
;; 2) we send the current value of count out as an osc message when the trigger fires
;; 3) we output the trigger to the control bus with id c-bus
;;
;; Next we register an osc handler which will be called when the osc message
;; with path "/tr" is received. We give this handler the id :metro-synth so we
;; can refer to it in the future. Finally we pass an anonymous fn which simply
;; prints out the osc msg received. Clearly this fn could do a lot more
;; interesting things ;-)


(defsynth metro-synth [c-bus 0 rate 1]
  (let [trigger (impulse:kr rate)
        count (stepper:kr trigger :min 1 :max 4)]
    (send-trig:kr trigger count)
    (out:kr c-bus trigger)))

(on-event "/tr" #(println "trigger: " %) ::metro-synth)

(metro-synth)

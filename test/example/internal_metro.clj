(ns scratch.internal-metro
  (:use [overtone.live]))

;; A basic demo of how you can use internal impulses to drive the rhythm of a synth

(demo 5
     (let [src1      (sin-osc 440)
           src2      (sin-osc 880)
           root-trig (impulse:kr 100)
           t1        (pulse-divider:kr root-trig 20)
           t2        (pulse-divider:kr root-trig 10)]
  (+ (* (decay t1 0.1) src1)
     (* (decay t2 0.1) src2))))


;; Here's how you can separate the impulses out across synths and connect them
;; together with a control bus

(def c-bus (control-bus))

(defsynth root-trig [rate 100]
 (out c-bus (impulse:kr rate)))

(definst pingr [freq 440 div 20]
 (let [src1 (sin-osc freq)
       t1 (pulse-divider:kr (in:kr c-bus) div)]
   (* (decay t1 0.1) src1)))

(def r-trig (root-trig))
(pingr)
(pingr 440 50)
(pingr 990 40)
(ctl r-trig :rate 50)
(stop)

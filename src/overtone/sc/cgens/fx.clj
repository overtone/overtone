(ns overtone.sc.cgens.fx
  (:use [overtone.sc defcgen ugens]))

(defcgen distortion2
  "Basic distortion"
  [in     {:default nil :doc "The input signal"}
   amount {:default 0.5 :doc "The amount of distortion to add"}]
  (:ar (let [k   (/ (* 2 amount) (- 1 amount))
             snd (/ (* in (+ 1 k)) (+ 1 (* k (abs in))))]
         snd)))

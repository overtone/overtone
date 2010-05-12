(ns devices.monome
  (:require [overtone.core.log :as log])
  (:use overtone.live))

(declare s)
(declare hat)

(defn hit-handler [msg]
  (log/debug "hit-handler: " msg)
  (let [[x y] (:args msg)]
    (cond
      (and (= 0 x) (= 7 y)) (hit hat)
      :default (hit :sin :pitch (+ 30 x) :dur (* y 50)))))

(defn start []
  (def s (osc-server 1234))
  (def hat (load-sample "/home/rosejn/projects/overtone/instruments/samples/kit/open-hat.wav"))

  (osc-handle s "/hit" hit-handler)
)


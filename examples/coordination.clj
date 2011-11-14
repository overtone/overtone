(ns examples.coordination
  (:use [overtone.live]
        [overtone.inst.synth])
  (:require [polynome.core :as poly]))

;;ping

(def m (poly/init "/dev/tty.usbserial-m64-0790"))

(poly/on-press m (fn [x y s]
                 (println "pressed " [x y])
                 (let [t (now)]
                   (if (and (= 7 x)
                            (= 0 y))
                     (if-let [event (poly/find-event s #(and (= 0 (:x %))
                                                        (= 0 (:y %))))]
                       (when (and (< (:time event) t)
                                  (< (- t (:time event)) 500))
                         (println "ping")
                         (ping)))))))

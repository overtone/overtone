(ns itchy-test
  (:require 
     [overtone.itchy :as itchy]
     [overtone.patches :as patches]))

(itchy/connect)
(itchy/debug)

(def synth (itchy/instrument itchy/SUB))
(itchy/modify synth (:orb patches/sub))

(def drums (itchy/instrument itchy/DRUM))
(itchy/modify drums (:high-filtered patches/drum))

(defn play-range [id from to & [sleep]]
  (let [sleep (or sleep 250)]
    (doseq [i (range from to)]
      (itchy/note id i)
      (Thread/sleep sleep))))

;(play-range synth 0 400 25)

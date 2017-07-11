(ns overtone.examples.compositions.euclidean-rhythms
  (:use overtone.live)
  (:require [overtone.algo.euclidean-rhythm :refer [euclidean-rhythm]]))

(def metro (metronome 200))

(definst sine-blip [freq 400]
  (let [snd (sin-osc freq)
        env (env-gen (perc 0.02 0.7) :action FREE)]
    (* 0.2 env (sin-osc freq))))

(defn player [m num r sound]
  (at (m num)
      (if (= 1 (first r))
        (sound)
        ))
  (apply-at (m (inc num)) #'player [m (inc num) (next r) sound]))

(def notes (vec (map (comp midi->hz note) [:c3 :g3 :d3])))

(player metro (metro) (cycle (euclidean-rhythm 3 8)) (partial sine-blip (notes 0)))
(player metro (metro) (cycle (euclidean-rhythm 4 4)) (partial sine-blip (notes 1)))
(player metro (metro) (cycle (euclidean-rhythm 5 13)) (partial sine-blip (notes 2)))

;; (stop)

(ns overtone.examples.compositions.euclidean-rhythms
  (:use
   overtone.live
   [overtone.algo.euclidean-rhythm :only [euclidean-rhythm]]))

(def notes (vec (map (comp midi->hz note) [:c3 :e3 :g3])))

(defcgen polycomponent
  "rhythmic sine osc"
  [bpm {:default 120}
   pattern {:default [1 0]}
   freq {:default 440}]
  (:ar
   (let [env (decay2 (t2a (demand (impulse:kr (/ bpm 30)) 0 (dseq pattern INF))) 0.1 0.7)]
     (* 0.5 env (sin-osc freq))
     )))

(definst polyrhythm []
  (let [low (polycomponent 220 (euclidean-rhythm 2 5) (notes 0))
        mid (polycomponent 220 (euclidean-rhythm 1 7) (notes 2))
        hi  (polycomponent 220 (euclidean-rhythm 3 8) (notes 1))]
    (+ hi low mid)))

(polyrhythm) ;; play

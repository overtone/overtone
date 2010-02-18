(ns examples.basic
  (:use overtone.live))

(refer-ugens)
(boot)

(defsynth ticker [freq 2]
  (* (sin-osc 440) (env-gen (perc 0.1 0.2) (sin-osc:kr freq))))

(defn wah-wah [freq depth]
  (* depth (sin-osc:kr freq)))

(defsynth sizzle [amp 0.4 depth 10 freq 220 lfo 8] 
  (* amp (saw (+ freq (wah-wah lfo depth))))

(ns overtone.examples.instruments.space
  (:use overtone.live))

(defsynth space-theremin
  [out-bus 0
   freq 440
   amp 0.1
   gate 1
   lfo-rate 6
   lfo-width 0.5
   cutoff 4000
   rq 0.25
   lag-time 0.1
   pan 0]
  (let [lfo    (lf-tri:kr (+ lfo-rate (mul-add (lf-noise1:kr 5) 0.3 0.3)) (rand 2))
        osc    (* 0.5 (saw (midicps (+ (cpsmidi (lag freq lag-time))
                                       (* lfo lfo-width)))))
        filter (b-low-pass4 osc (lag cutoff (* lag-time 4)) rq)
        env    (env-gen:ar (adsr 0.6 0 1 0.05) gate FREE)]
    (out out-bus (pan2 (* filter env (lag amp (* 4 lag-time))) pan))))

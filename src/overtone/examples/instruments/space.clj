(ns overtone.examples.instruments.space
  (:use overtone.live))

;; Recipe from Mitchell Sigman (2011) Steal this Sound. Milwaukee, WI: Hal Leonard Books
;; Adapted from a translated version by Nick Collins

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



(defsynth space-reverb
  [out-bus 0
   in-bus 0
   gate 1
   threshold 0.1
   amp 0.1]
  (let [env             (linen gate 0.1 1 0.1 FREE)
        num-combs       6
        num-allpass     4
        src             (in in-bus 2)
        reverb-predelay (delay-n src 0.048 0.048)
        y               (mix (repeat num-combs (comb-l reverb-predelay 0.1 (ranged-rand 0.01 0.1) 5)))
        y               (loop [cnt num-allpass
                               res y]
                          (if (<= cnt 0)
                            res
                            (recur (dec cnt)
                                   (allpass-n res
                                              0.051
                                              [(ranged-rand 0.01 0.05)
                                               (ranged-rand 0.01 0.05)]
                                              1))))]
    (out out-bus (* env amp (pan2 y)))))

;;(def st (space-theremin :out-bus 10 :amp 0.8 :cutoff 1000))
;;(space-reverb [:after st] :in-bus 10)
;;(stop)

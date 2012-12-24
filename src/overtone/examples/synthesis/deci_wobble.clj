(ns overtone.examples.synthesis.deci-wobble
  (:use overtone.live))

;; Adapted from the Decimator Wobble Bass described here:
;; http://www.phrontist.org/2010/06/decimator-wobble-bass/

(defsynth deci-wobble []
  (let [temp-freq (/ 140 60 3)
        trig      (impulse temp-freq)
        note      (demand trig 0 (dseq [40 43 47 47 40 37 43 28] INF))
        note      (slew:kr note 300 20)
        num-smp   (/ (sample-rate) temp-freq)
        rate      (/ (* 2 Math/PI) num-smp)
        rate      (* rate 0.5 (demand:kr trig 0 (dseq [0.5 6 6 12 2 8 6 12] INF)))
        wobble    (lag (cos (phasor:ar trig rate Math/PI (* 2 Math/PI))) 0.01)
        sub       (* (lin-lin wobble -1 1 0 1)
                     (sin-osc (/ (midicps note) 2 )))
        sub       [sub sub]
        snd       (+ (var-saw (midicps note) :width (lin-lin wobble -1 1 0.45 0.55))
                     sub)
        snd       (decimator snd 20000 (lin-lin wobble -1 1 1.2 8))
        snd       (moog-ladder snd (lin-lin wobble -1 1 (midicps note) 25000) (lin-lin wobble -1 1 0.03 0.1))
        snd       (* 0.75 [snd snd])
        snd       [(delay-c snd 1 (lin-lin wobble -1 1 0 0.0012)) (delay-c snd 1 (lin-lin wobble -1 1 0.0012 0))]
        snd       (* snd (linen:kr trig 0.01 2 (/ 1.3 temp-freq) :action NO-ACTION))
        ]
    (out 0 snd)))

;; to play:
;; (deci-wobble)
;; (stop)

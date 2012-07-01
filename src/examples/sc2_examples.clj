(ns examples.sc2-example
  (:use [overtone.live]))

; Here are a few ways to define the same example from SC2, analog bubbles.
; They all produce an identical synthdef, so it's a matter of style I guess.
(demo 10
  (let [freqs (midicps (mul-add (lf-saw:kr 0.4 0) 24
                                (mul-add (lf-saw:kr [8 7.23] 0) 3 80)))]
    (comb-n (* (sin-osc freqs 0) 0.04) 0.2 0.2 4)))

(demo 10
  (let [o (-> (lf-saw:kr [8 7.23]) (* 3) (+ 80))
        f (-> (lf-saw:kr 0.4) (* 24) (+ o))
        s (-> (sin-osc (midicps f)) (* 0.3))]
    (comb-n s 0.2 0.2 4)))

(demo 10
  (let [o (mul-add (lf-saw:kr [8 7.23]) 3 80)
        f (mul-add (lf-saw:kr 0.4) 24 o)
        s (mul-add (sin-osc (midicps f)) 0.3 0)]
    (comb-n s 0.2 0.2 4)))

(demo 10
  (let [o (+ 80 (* 3 (lf-saw:kr [8 7.23])))
        f (+ o  (* 24 (lf-saw:kr 0.4)))
        s (* 0.3 (sin-osc (midicps f)))]
    (comb-n s 0.2 0.2 4)))

;;resonant pulses
(demo 5
      (comb-l
       (rlpf (* 0.05 (lf-pulse (mul-add (f-sin-osc:kr 0.05 0) 80 160) 0 0.4))
             (mul-add (f-sin-osc:kr [0.6 0.7] 0) 3600 4000)
             0.2)
       0.3 [0.2 0.25] 2))

;;scratchy
(demo
  (rhpf (* (max (brown-noise [0.5 0.5] -0.49) 0) 20)
        5000
        1))

;; TODO: Not sure why this is just spitting out noise...
;;sprinkler
(demo
  (bpz2:ar (white-noise:ar (mul-add (lf-pulse:kr (mouse-x:kr 0.2 50) 0 0.25) 0.1 0))))

;;FIXME
;;moto-rev
;; (demo 10
;;   (clip2 (rlpf (lf-pulse (mul-add (sin-osc:kr 0.2 0) 10 21) 0.1) 100 0.1) 0.4))

(ns examples.fx
  (:use overtone.live))

; All of these are based off the compander ugen.  Of course you can just use it
; directly in your synths, but it's nice to be able to stick on

;; This file has some demos to show you what the fx in overtone.studio.fx do.  These
;; are setup so you can experiment with the parameters by moving the mouse around.

;; First a fat synth to use as our source sound

(defsynth bizzle [out-bus 10 amp 0.5]
  (out out-bus
       (* amp
          (+ (* (decay2 (* (impulse 10 0)
                           (+ (* (lf-saw:kr 0.3 0) -0.3) 0.3))
                        0.001)
                0.3)
             (apply + (pulse [80 81]))))))

; Give it a try
(def biz (bizzle 0))
(kill biz)

;; Next, create a bus to connect the source synth with the fx synth:
(def b (audio-bus))

; All of these are based off the compander ugen.  Of course you can just use it
; directly in your synths, but it's nice to be able to stick on
(defsynth compressor-demo [in-bus 10]
  (let [source (in in-bus)]
    (out 0 (pan2 (compander source source (mouse-y:kr 0.0 1) 1 0.5 0.01 0.01)))))

;(bizzle b)
;(compressor-demo b)
;(stop)

(defsynth limiter-demo [in-bus 10]
  (let [source (in in-bus)]
    (out 0 (pan2 (compander source source (mouse-y:kr 0.0 1) 1 0.1 0.01 0.01)))))

(defsynth sustainer-demo [in-bus 10]
  (let [source (in in-bus)]
    (out 0 (pan2 (compander source source (mouse-y:kr 0.0 1) 0.1 1 0.01 0.01)))))
;;(bizzle b)
;;(limiter-demo b)
;;(stop)

;;(bizzle b)
;;(sustainer-demo b)
;;(stop)

; Here is a different sample synth to try out the reverb and echo effects
(defsynth pling [out-bus 10
                 rate 0.3 amp 0.5]
  (out out-bus
       (* (decay (impulse rate) 0.25)
          (* amp (lf-cub 1200 0)))))

;(def p (pling 0))
;(kill p)

(defsynth reverb-demo [in-bus 10]
  (out 0 (pan2 (free-verb (in in-bus) 0.5 (mouse-y:kr 0.0 1) (mouse-x:kr 0.0 1)))))
;(pling)
;(reverb-demo)
;(stop)

(defsynth echo-demo [in-bus 10]
  (let [source (in in-bus)
        echo (comb-n source 0.5 (mouse-x:kr 0 1) (mouse-y:kr 0 1))]
    (out 0 (pan2 (+ echo (in in-bus) 0)))))

;(pling)
;(echo-demo)
;(stop)
; If you have a microphone or some other source of external input, you can read it in
; and then run it through fx like this.
(defsynth ext-source [out-bus 10]
  (out out-bus (in (num-output-buses:ir))))

;; Fetch a spoken countdown from freesound.org
(def count-down (sample (freesound-path 71128)))

;; Play it unmodified:
;;(count-down)

;; From Designing Sound in SuperCollider
(defsynth schroeder-reverb
  []
  (let [input (pan2 (play-buf 1 count-down) -0.5)
        delrd (local-in 4)
        output (+ input [(first delrd) (second delrd)])
        sig [(+ (first output) (second output)) (- (first output) (second output))
             (+ (nth delrd 2) (nth delrd 3)) (- (nth delrd 2) (nth delrd 3))]
        sig [(+ (nth sig 0) (nth sig 2)) (+ (nth sig 1) (nth sig 3))
             (- (nth sig 0) (nth sig 2)) (- (nth sig 0) (nth sig 2))]
        sig (* sig [0.4 0.37 0.333 0.3])
        deltimes (- (* [101 143 165 177] 0.001) (control-dur))
        lout (local-out (delay-c sig deltimes deltimes))
        ]
    (out 0 output)))

;;Spooky!
;;(schroeder-reverb)

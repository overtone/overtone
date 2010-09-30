(ns examples.fourier
  (:use overtone.live
    (overtone.gui scope curve)))

(refer-ugens)

(def buf (buffer 2048))

; Bounce around cutting a single band out of white noise.
(defsynth random-noise-band []
  (let [src (* 0.8 (white-noise))
        freqs (fft (:id buf) src)
	    filtered (pv-rand-comb freqs 0.95 (impulse:kr 0.4))]
  (ifft filtered)))

; Cut off noise at a wall
; HELP ME!
(defsynth cut-it-out []
    (let [src (* 0.2 (white-noise))
          freqs (fft (:id buf) src)
	      filtered (pv-brick-wall (:id buf) (sin-osc:kr 0.1))]
    (ifft filtered)))

(defsynth saw-tips [freq 440 thresh 0.5]
    (let [src (* 0.8 (saw freq))
          freqs (fft (:id buf) src)
	      filtered (pv-local-max (:id buf) thresh)]
    (ifft filtered)))

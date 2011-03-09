(ns examples.fourier
  (:use overtone.live))
;    (overtone.gui scope curve)))

(def buf (buffer 2048))

; Bounce around cutting a single band out of white noise.
(definst random-noise-band [rate 0.4]
  (let [src (* 0.8 (white-noise))
        freqs (fft buf src)
	    filtered (pv-rand-comb freqs 0.95 (impulse:kr rate))]
  (ifft filtered)))

; Cut off noise at a wall
; HELP ME!
(defsynth cut-it-out []
    (let [src (* 0.2 (white-noise))
          freqs (fft (:id buf) src)
	      filtered (pv-brick-wall buf (sin-osc:kr 0.1))]
    (out 0 (pan2 (ifft filtered)))))

(definst saw-tips [freq 440 thresh 0.5]
    (let [src (* 0.8 (saw freq))
          freqs (fft buf src)
	      filtered (pv-local-max (:id buf) thresh)]
      (ifft filtered)))


(comment definst local-noise []
  (let [in (* [0.1 0.1] (white-noise))
        chain (fft (local-buf 2048 2) in)
        chain (pv-brick-wall chain (sin-osc:kr [0.1 0.11]))]
    (ifft chain)))

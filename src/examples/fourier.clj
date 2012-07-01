(ns examples.fourier
  (:use overtone.live))

(def buf (buffer 2048))

; Bounce around cutting a single band out of white noise.
(demo 10
  (let [rate 10
        src (* 0.8 (white-noise))
        freqs (fft buf src)
	    filtered (pv-rand-comb freqs 0.95 (impulse:kr rate))]
  (ifft filtered)))

; Cut off noise at a wall
(demo 15
    (let [src (* 0.2 (white-noise))
          freqs (fft (:id buf) src)
	      filtered (pv-brick-wall buf (sin-osc:kr 0.1))]
    (out 0 (pan2 (ifft filtered)))))

;;saw-tips
(demo 5
  (let [freq     440
        thresh    0.5
        src      (* 0.8 (saw freq))
        freqs    (fft buf src)
        filtered (pv-local-max (:id buf) thresh)]
      (ifft filtered)))

(demo
  (let [in (* [0.1 0.1] (white-noise))
        chain (fft (local-buf 2048 2) in)
        chain (pv-brick-wall chain (sin-osc:kr [0.1 0.11]))]
    (ifft chain)))

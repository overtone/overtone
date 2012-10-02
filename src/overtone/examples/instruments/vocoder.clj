(ns overtone.examples.instruments.vocoder
  (:use overtone.live))

(def a (buffer 2048))
(def b (buffer 2048))

(demo 10
      (let [input  (sound-in) ; mic
            src    (white-noise) ; synth - try replacing this with other sound sources
            formed (pv-mul (fft a input) (fft b src))
            audio  (ifft formed)]
        (pan2 (* 0.1 audio))))

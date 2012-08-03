(ns examples.samples
  (:use overtone.live
        [overtone.gui.scope :only [pscope]]))

;;; Read me, and evaluate line by line as you go.  To evaluate a form
;;; highlight it with the mouse and type <control-e>, using the "e" for
;;; evaluate.  The repl window below will show the output of everything
;;; you evaluate.  This is helpful for documentation too.  All ugen
;;; functions have doc strings.  Try evaluating these:

(odoc buf-rd)
(odoc buf-frames)
(odoc sin-osc)
(odoc lf-tri)
(odoc phasor)

;; In order to play samples instantly they have to be in memory.  (You
;; use memory buffers for other synths too, for example in an echo
;; effect.)

;; Load a sample into a buffer. If this is the first time you're using
;; sample 35809 from freesound, then it'll take a few seconds to
;; download...

(def flute-buf (load-sample (freesound-path 35809)))

;; Now the audio data for the sample is loaded into a buffer.  You can
;; view the buffer in the scope window too.  Click in the scope tab on
;; the right, and evaluate this.
(pscope :buf flute-buf)

;; If you just want to play a buffer and adjust the speed or looping
;; play-buf is probably the easiest way.
(odoc sample-player)

(sample-player flute-buf)

;; to stop, just use the (stop) fn:
(stop)


;; Try layering these looped versions, eval each line
;; [buf 0 rate 1.0 start-pos 0.0 loop? 0 vol 1]
(sample-player flute-buf :rate 1 :loop? true)
(sample-player flute-buf :rate 0.5 :loop? true)
(sample-player flute-buf :rate 1.5 :loop? true)
(sample-player flute-buf :rate 0.25 :loop? true)
(sample-player flute-buf :rate 2 :loop? true)

;; When you've had enough, then stop them:
(stop)

;;sample-player uses the ugen play-buf behind the scenes to play the
;;buffer. This plays the sample linearly with options such as rate and
;;loop? However, we're not just limited to playing back buffers
;;linearly, we can do all sorts of crazy stuff...


;; buf-rd takes an oscillator as the index into the buffer its reading.
;; This lets you sweep back and forth across a buffer in any direction
;; and at any rate.

;; zip back and forth across the buffer with a sin wave
(demo 10 (buf-rd 2 flute-buf (* (sin-osc 0.1) (buf-frames flute-buf))))


;; randomly play at different rates
(demo 10 (buf-rd 2 flute-buf (* (lf-noise1 1) (buf-frames flute-buf))))
(demo 10 (buf-rd 2 flute-buf (* (lf-noise1 10) (buf-frames flute-buf))))


;; the triangle waves give a sense of building
(demo 10 (buf-rd 2 flute-buf (+ (lf-tri 0.1) (* (lf-tri 0.23) (buf-frames flute-buf)))))


;; experiment with different ways of modulating the rate
(demo 10 (buf-rd 2 flute-buf (* (sin-osc (* 0.1 (sin-osc 0.02))) (buf-frames:ir 0))))

(ns examples.samples
  (:use overtone.live))

;;; Read me, and evaluate line by line as you go.  To evaluate a form highlight it with the mouse
;;; and type <control-e>, using the "e" for evaluate.  The repl window below will show the output
;;; of everything you evaluate.  This is helpful for documentation too.  All ugen functions
;;; have doc strings.  Try evaluating these:

(doc buf-rd)
(doc buf-frames)
(doc sin-osc)
(doc lf-tri)
(doc phasor)

; In order to play samples instantly they have to be in memory.  (You use memory
; buffers for other synths too, for example in an echo effect.)

; Load a sample into a buffer. You'll need to change this file path to point to a
; wav file on your machine.
(def flute-buf (load-sample "/home/rosejn/studio/samples/flutes/flutey-echo-intro-blast.wav"))

; Now the audio data for the sample is loaded into a buffer.  You can view the buffer in the
; scope window too.  Click in the scope tab on the right, and evaluate this.
(scope :buf flute-buf)

; If you just want to play a buffer and adjust the speed or looping
; play-buf is probably the easiest way.
(doc play-buf)


(synth (play-buf 1 flute-buf 1 1 0 0 FREE))

; Try layering these looped versions, eval each line
(synth (play-buf 1 flute-buf 1 1 0 1))
(*1)
(synth (play-buf 1 flute-buf 0.5 1 0 1))
(*1)
(synth (play-buf 1 flute-buf 1.5 1 0 1))
(*1)
(synth (play-buf 1 flute-buf 0.25 1 0 1))
(*1)
(synth (play-buf 1 flute-buf 2 1 0 1))
(*1)
(reset)


; buf-rd takes an oscillator as the index into the buffer its reading.  This lets you sweep
; back and forth across a buffer in any direction and at any rate.

; zip back and forth across the buffer with a sin wave
(synth (buf-rd 1 flute-buf (* (sin-osc 0.1) (buf-frames flute-buf))))
(*1)
(reset)

; randomly play at different rates
(synth (buf-rd 1 flute-buf (* (lf-noise1 1) (buf-frames flute-buf))))
(*1)
(reset)
(synth (buf-rd 1 flute-buf (* (lf-noise1 10) (buf-frames flute-buf))))
(*1)
(reset)

; the triangle waves give a sense of building
(synth (buf-rd 1 flute-buf (+ (lf-tri 0.1) (* (lf-tri 0.23) (buf-frames flute-buf)))))
(*1)
(reset)

; experiment with different ways of modulating the rate
(synth (buf-rd 1 flute-buf (* (sin-osc (* 0.1 (sin-osc 0.02))) (buf-frames:ir 0))))
(*1)
(reset)

; this would be a typical way to play back at normal rates, using a phasor.
; phasors just count from a start value to an end value. Using buf-frames
; we can get the size of the buffer, and buf-rate-scale figures out any
; sampling rate difference.

(defsynth buf-player [buf 0 rate 1 loop? 0]
  (buf-rd 1 buf (phasor 0 (* rate (buf-rate-scale 0)) 0 (buf-frames:ir 0))
          loop? 2))

; evaluate these one on top of another to layer the sample at different pitches
(buf-player (:id flute-buf) 1)
(buf-player (:id flute-buf) 0.5)
(buf-player (:id flute-buf) 1.5)
(buf-player (:id flute-buf) 3)
(buf-player (:id flute-buf) 0.25)
(reset)

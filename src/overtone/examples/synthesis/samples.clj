(ns overtone.examples.synthesis.samples
  (:use overtone.live
        [overtone.studio.scope :only [pscope]]))

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

(def flute (freesound 35809))

;; Now the audio data for the sample is loaded into a buffer.  You can
;; view the buffer in the scope window too.  Click in the scope tab on
;; the right, and evaluate this.
(pscope flute)

;; You can play the sample by calling it like a function:

(flute)

;; Try layering these looped versions, eval each line
;; [buf 0 rate 1.0 start-pos 0.0 loop? 0 amp 1]
(flute :rate 1 :loop? true)
(flute :rate 0.5 :loop? true)
(flute :rate 1.5 :loop? true)
(flute :rate 0.25 :loop? true)
(flute :rate 2 :loop? true)

;; When you've had enough, then stop them:
(stop)

;; You can load arbirary wav and aiff files from your computer by
;; passing a path to the sample function:

(def foo (sample "~/Desktop/foo.wav"))

;; This can also triggered y calling it as a function:

(foo)

;;The freesound and sample player play-buf ugen behind the scenes to
;;play the buffer. This plays the sample linearly with options such as
;;rate and loop? However, we're not just limited to playing back buffers
;;linearly, we can do all sorts of crazy stuff...


;; buf-rd takes an oscillator as the index into the buffer its reading.
;; This lets you sweep back and forth across a buffer in any direction
;; and at any rate.

;; zip back and forth across the buffer with a sin wave
(demo 10 (buf-rd 2 flute (* (sin-osc 0.1) (buf-frames flute))))

;; randomly scrub around the buffer
(demo 10 (buf-rd 2 flute (* (lf-noise1 1) (buf-frames flute))))
(demo 10 (buf-rd 2 flute (* (lf-noise1 10) (buf-frames flute))))

;; the triangle waves give a sense of building
(demo 10 (buf-rd 2 flute (+ (lf-tri 1.1) (*  (lin-lin (lf-tri 0.23) -1 1 0 1) (buf-frames flute)))))

;; experiment with different ways of modulating the rate
(demo 10 (buf-rd 2 flute (* (lin-lin (sin-osc 0.02) -1 1 0 1 ) (buf-frames:ir flute))))

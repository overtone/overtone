(ns examples.tutorial
  (:use overtone.live))

; Add all of the synthesis functions to the current namespace.
; We do it this way because we also need to override a number
; of the built-in functions from clojure.core, like +, -, *, etc...
(refer-ugens)

; Boot the SuperCollider server
(boot)

; In Overtone you define synthesizers that can be executed on the SuperCollider
; server.  Create an anonymous synth like this.  It returns a function, that 
; when called will trigger the synth.
(def foo (synth [] (sin-osc 440)))

; A shortcut for doing the same thing, just like def and defn.
(defsynth foo [] (sin-osc 440))

; Now if we trigger the synth, it will return the ID of the instance of 
; the synth that was created.  Turn your volume down, because this is
; going to make some noise.
(def id (foo))

;; Now you can kill this instance like so
(kill id)

; For future reference, if you want to stop all sound immediately you can
; call (reset).

; Often times in audio synthesis you want to generate multiple channels
; of audio, whether it be for stereo, or for creating multiple signals that will
; eventually get mixed back down to one or two channels.  To help with this,
; you can do something called multi-channel expansion.  It's simple.  Anywhere
; you can pass a value as an argument to a synthesis function, you can also
; pass a seq of values.
(defsynth bar [] (sin-osc [440 442]))
(bar)
(reset)

; As you might have noticed, the synths can take parameters too, so that you can
; control their input values both when you instantiate a synth, and later while
; they are running.  It works almost like a regular function definition, except 
; you need to include default values.  Here is our sin wave oscillator that now
; has a controllable frequency.

(defsynth baz [freq 440]
  (sin-osc freq))

(baz 200)
(baz 800)
(baz 400)
(reset)

(defsynth ping [freq 600]
  (* (env-gen (perc 0.02 0.4) 1 1 0 1 :free) (sin-osc freq)))

(def server (osc-server 5708))
(osc-handle server "/play" #(ping (+ 100 (* 100 (first (:args %))))))



(ns examples.tutorial
  (:use overtone.live))

; Welcome to Project Overtone!

; For starters lets just make some noise and get the basics of working with
; Overtone figured out.  Overtone is implemented in Clojure, and it uses the
; SuperCollider synthesis server for the back-end audio engine.  (So if you are
; already familiar with SC then much of this will be familiar to you.)  You
; should be reading this file in an editor that lets you interactively evaluate
; Clojure code.

; In Overtone you create synthesizers to generate audio.  You can think of a
; synthesizer definition as the design or blueprint for a signal processor.
; (In Max/MSP or PureData they call this a patch.)

; The synth macro takes a synth design, compiles it, loads it into the
; audio server, and returns a function that can be used to trigger the synth.
; Here is a 440 hz sin wave:
(def foo (synth (out 0 (pan2 (sin-osc 440)))))

; Trigger the synth by calling it like a regular function.  When called synth
; functions return an ID number representing an instance ID, which can be used
; to kill the synth or modify its parameters.
(def id (foo))  ; trigger the synth, saving its ID
(kill id)       ; kill the instance

; Use defsynth to create synthesizers and assign the player function to a symbol
; in the current namespace, just like fn and defn in clojure.core.  Note that
; synthesizer parameters must always have a default value.
(defsynth my-sin [freq 440]
  (out 0 (pan2 (sin-osc freq))))

; play the sin wave at a couple frequences
(my-sin)     ; uses the default
(my-sin 220) ; an octave lower
(my-sin 447)

; If you lose a synth ID or things are going crazy and you just need to kill
; all the current synths, call stop to clear all the live synths.
(stop)

; The classic saw wave, creates even and odd harmonics with a bright sound
(definst sawzall [freq 440]
  (* (env-gen (perc 0.1 0.8) :action FREE)
     (saw freq)))

(sawzall)

; Triangle wave
(definst triangular [freq 120]
   (* (env-gen (perc 0.1 4.8) :action FREE)
     (lf-tri freq)))
(triangular 320)

; Square wave
(definst sq [freq 120]
   (* (env-gen (perc 0.1 4.8) :action FREE)
     (square freq)))
(sq 320)

; White noise
(definst noisey []
     (* (env-gen (perc 0.1 1.8) :action FREE)
     (white-noise)))
(noisey)

; Pink noise
(definst pink-noisey []
     (* (env-gen (perc 0.1 1.8) :action FREE)
     (pink-noise)))
(pink-noisey)

; A shortcut for doing the same thing, just like def and defn.
(definst foo [] (sin-osc 440))

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
(definst bar [] (sin-osc [440 442]))
(bar)
(stop)

; As you might have noticed, the synths can take parameters too, so that you can
; control their input values both when you instantiate a synth, and later while
; they are running.  It works almost like a regular function definition, except
; you need to include default values.  Here is our sin wave oscillator that now
; has a controllable frequency.

(definst baz [freq 440]
  (sin-osc freq))

(baz 200)
(baz 800)
(baz 400)
(stop)

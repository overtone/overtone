(ns examples.tutorial
  (:use overtone.live))

; Welcome to Project Overtone!

; For starters lets just make some noise and get the basics of working with
; Overtone figured out.  Overtone is implemented in Clojure, and it uses the
; SuperCollider synthesis server for the back-end audio engine.  (So if you are
; already familiar with SC then much of this will be familiar to you.)  You
; should either be reading this file inside the Overtone application, or else
; in an editor that lets you interactively evaluate Clojure code.

; Start by getting all of the synthesis functions into the current namespace.
; We do it this way because we also need to override a number
; of the built-in functions from clojure.core, like +, -, *, etc...  But this
; could change in the future.
(refer-ugens)

; Boot the audio engine by either clicking the boot button to the top right,
; or else call boot.
(boot)

; In Overtone you create synthesizers to generate audio.  You can think of a
; synthesizer definition as the design or blueprint for a signal processor.
; (In Max/MSP or PureData they call this a patch.)

; The synth macro takes a synth design, compiles it, loads it into the
; audio server, and returns a function that can be used to trigger the synth.
; Here is a 440 hz sin wave:
(def foo (synth (sin-osc 440)))

; Trigger the synth by calling it like a regular function.  When called synth
; functions return an ID number representing an instance ID, which can be used
; to kill the synth or modify its parameters.
(def id (foo))  ; trigger the synth, saving its ID
(kill id)       ; kill the instance

; Use defsynth to create synthesizers and assign the player function to a symbol
; in the current namespace, just like fn and defn in clojure.core.  Note that
; synthesizer parameters must always have a default value.
(defsynth my-sin [freq 440]
  (sin-osc freq))

; play the sin wave at a couple frequences
(my-sin)     ; uses the default
(my-sin 220) ; an octave lower
(my-sin 447)

; If you lose a synth ID or things are going crazy and you just need to kill
; all the current synths, call reset to clear all the live synths.
(reset)

; Square wave, created by a pulse generator
; Adjust the width to create different harmonics
(defsynth square [freq 220 width 0.5]
  (* (env-gen (curve) 1 1 0 1 :free)
     (pulse freq width)))
(square 220 0.1)

(dotimes [i 100] (at (+ (now) (* i 400)) (square (+ 100 (rand-int 1000)) (* 0.01 i))))

; The classic saw wave, creates even and odd harmonics with a bright sound
(defsynth sawzall [freq 440]
  (* (env-gen (perc 0.1 0.8) 1 1 0 1 :free)
     (saw freq)))

(sawzall)

; Triangle wave
(defsynth triangular [freq 120]
   (* (env-gen (perc 0.1 4.8) 1 1 0 1 :free)
     (lf-tri freq)))
(triangular 320)

; White noise
(defsynth noisey []
     (* (env-gen (perc 0.1 1.8) 1 1 0 1 :free)
     (white-noise)))
(noisey)

; Pink noise
(defsynth pink-noisey []
     (* (env-gen (perc 0.1 1.8) 1 1 0 1 :free)
     (pink-noise)))
(pink-noisey)

(defsynth lead [freq 440 R 0.5]
  (* (env-gen (perc 0.02 2.5) 1 1 0 1 :free)
     (rlpf (saw [freq (* 1.5 freq)])
           (* (env-gen (perc 0.03 0.2)) 2 freq) R)))
(lead 660 0.5)

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


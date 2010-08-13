(ns
  ^{:doc "Higher level instrument and studio abstractions."
     :author "Jeff Rose"}
  overtone.studio.rig
  (:use (overtone.core sc synth)
     (overtone.music rhythm pitch))
  (:require overtone.studio.fx))

; The goal is to develop a standard "studio configuration" with
; an fx rack and a set of fx busses, an output bus, etc...

; TODO
;
; Audio input
; * access samples from the microphone

; Busses
; 0 & 1 => default stereo output (to jack)
; 2 & 3 => default stereo input

; Start our busses at 1 to makes space for up to 8 on-board I/O channels
(def BUS-MASTER 16) ; 2 channels wide for stereo

; Two mono busses for doing fx sends
(def BUS-A 18)
(def BUS-B 19)

;(synth :master
;  (out.ar 0 (in.ar BUS-MASTER)))

(def session* (ref
  {:tracks []
   :instruments []
   :players []}))

;(def *fx-bus (ref (Bus/audio (server) 2)))

; A track holds an instrument with a set of effects and patches it into the mixer
; * track group contains:
;     synth group => effect group => fader synth

(defn track [track-name & [n-channels]]
  {})

;(defsynth record-bus [bus-num path]
;  )

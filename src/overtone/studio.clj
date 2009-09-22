(ns overtone.studio
  (:import (de.sciss.jcollider Bus Synth SynthDef Control Buffer))
  (:use (overtone voice sc synth midi time pitch))
  (:require overtone.fx))

; The goal is to develop a standard "studio configuration" with
; an fx rack and a set of fx busses, an output bus, etc...

; TODO
; 
; Audio input
; * access samples from the microphone
; Disk I/O
; * recording to files
; * reading and playing samples

; Busses
; 0 & 1 => default stereo output (to jack)
; 2 & 3 => default stereo input
(def FX-BUS  16) ; Makes space for 8 channels of audio in and out

(def *voices (ref []))
(def *fx-bus (ref (Bus/audio (server) 2)))
(def *fx     (ref []))

(defn sample [path]
  (Buffer/cueSoundFile (server) path))

(defn reset-studio []
  (reset)
;  (doseq [effect @*fx]
;    (.free effect))
  (dosync 
    (ref-set *voices [])
    (ref-set *fx [])))

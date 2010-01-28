(ns 
  #^{:doc "Functions to help work with musical time."
     :author "Jeff Rose"}
  overtone.music.rhythm
  (:import (java.util Timer TimerTask))
  (:use time-utils))

; Rhythm

; A rhythm system should let us refer to time in terms of rhythmic units like beat, bar, measure,
; and it should convert these units to real time units (ms) based on the current BPM and signature settings.

(defn beat-ms
  "Convert 'b' beats to milliseconds at the given 'bpm'."
  [b bpm] (* (/ 60000 bpm) b))

;(defn bar-ms 
;  "Convert b bars to milliseconds at the current bpm."
;  ([] (bar 1))
;  ([b] (* (bar 1) (first @*signature) b)))

; A metronome is used to pull musical content (typically notes) from one or more generators.  
; tpb = ticks-per-beat
(defn metronome [bpm & [tpb]]
  (let [tpb (or tpb 1)
        start (now)
        tick (/ (beat-ms 1 bpm) tpb)]
    {:bpm   bpm
     :tpb   tpb
     :tick  tick
     :start start
     :timer (Timer.)}))

(defn timer-task [fun]
  (proxy [TimerTask] []
               (run [] (fun))))

(defn on-tick [metro fun]
  (.scheduleAtFixedRate (:timer metro) 
                        (timer-task fun) 
                        (long 0) 
                        (long (:tick metro))))

(defn stop [metro]
  (.cancel (:timer metro)))

;== Grooves
;
; A groove represents a pattern of velocities and timing modifications that is 
; applied to a sequence of notes to adjust the feel.
;
; * swing 
; * jazz groove, latin groove 
; * techno grooves (hard on beat one)
; * make something more driving, or more layed back...

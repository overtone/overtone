(ns
  #^{:doc "Functions to help work with musical time."
     :author "Jeff Rose"}
  overtone.music.rhythm
  (:import (java.util Timer TimerTask))
  (:use (overtone.core sc time-utils)))

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

; A metronome is a linear function that given a beat count returns the time in milliseconds.
;
; tpb = ticks-per-beat
(defn metronome [bpm]
  (let [start (now)
        tick-ms  (beat-ms 1 bpm)]
    (fn
      ([] (long (/ (- (now) start) tick-ms)))
      ([beat] (+ (* beat tick-ms) start)))))

; TODO: finish me
(defn rhythm-runner [metro player-fun]
  (loop [bar-count 0]
    (let [next-beat (player-fun)]
      (call-at (metro (dec next-beat))))))

;(defn boomer [beat]
;  (at (*metro* beat) (sinner :freq 200 :dur 0.4))
;  (if (> (rand-int 10) 4)
;    (let [freq (+ 800 (rand-int 800))
;          dur (* 0.1 (rand-int 8))]
;      (at (*metro* (+ 0.5 beat)) (sinner :freq freq :dur dur))
;      (at (*metro* (+ 0.75 beat)) (sinner :freq freq  :dur (* 0.8 dur)))))
;  (call-at (*metro* (+ 0.5 beat)) #'boomer (+ 1 beat)))

;(boomer (*metro*))

; TODO: experiment using a function to pull musical content (typically notes) from one or more generators.


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

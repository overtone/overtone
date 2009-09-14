(ns overtone.studio
  (:import (de.sciss.jcollider Bus Synth SynthDef Control))
  (:use (overtone sc synth music midi))
  (:require overtone.fx))

(start-synth)

; Busses
; 0 & 1 => default stereo output (to jack)
; 2 & 3 => default stereo input
(def FX-BUS  16) ; Makes space for 8 channels of audio in and out

(def *voices (ref []))
(def *fx-bus (ref (Bus/audio (server) 2)))
(def *fx     (ref []))

(defn voice [synth & defaults]
  (let [new-voice {:type     :voice
                   :synth    synth
                   :controls {}
                   :defaults (apply hash-map defaults)}]
    (dosync (alter *voices conj new-voice))
    new-voice))

(defn voice? [obj]
  (and (map? obj) 
       (= :voice (:type obj))))

(defn- synth-args [arg-map]
  (if (empty? arg-map) 
    [(make-array String 0) (make-array (. Float TYPE) 0)]
    [(into-array (for [k (keys arg-map)] 
                   (cond 
                     (keyword? k) (name k)
                     (string? k) k)))
     (float-array (for [v (vals arg-map)] (float v)))]))

(defn trigger 
  "Create a new instance of a studio voice or a standalone synth."
  [voice arg-map]
  (let [arg-map (if (voice? voice) (assoc arg-map "out" FX-BUS) arg-map) 
        [arg-names arg-vals] (synth-args arg-map)]
    (cond 
      (map? voice) (Synth. (:synth voice) arg-names arg-vals (target))
      (string? voice) (Synth. voice arg-names arg-vals (target))
      (= de.sciss.jcollider.SynthDef (type voice)) (.play voice (target) arg-names arg-vals))))

(defn effect [synthdef & args]
  (let [arg-map (assoc (apply hash-map args) "bus" FX-BUS)
        new-effect {:def synthdef
                    :effect (trigger synthdef arg-map)}]
    (dosync (alter *fx conj new-effect))
    new-effect))

(defn reset-studio []
  (reset)
  (doseq [effect @*fx]
    (.free effect))
  (dosync 
    (ref-set *voices [])
    (ref-set *fx [])))

(defn update 
  "Update a voice or standalone synth with new settings."
  [voice & args]
  (let [[names vals] (synth-args (apply hash-map args))
        synth        (if (voice? voice) (:synth voice) voice)]
    (.set synth names vals)))

(defn release 
  [synth]
  (.release synth))

(defn note [voice note-num dur & args]
  (let [synth (trigger voice (assoc (apply hash-map args) :note note-num))]
    (schedule #(release synth) dur)
    synth))

(defn now []
  (System/currentTimeMillis))

; Can't figure out why this isn't working... need sleep.
(defn play [time-ms voice note-num dur & args]
  (let [on-time  (- time-ms (now))
        rel-time (+ on-time dur)]
    (if (<= on-time 0)
      (let [synth (trigger voice (assoc (apply hash-map args) :note-num note-num))]
        (schedule #(release synth) rel-time))
      (schedule #(apply note voice note-num dur args) on-time))))


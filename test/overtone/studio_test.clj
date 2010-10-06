(ns studio-test
  (:use [overtone.core synth ugen envelope sc]
        overtone.studio))

(refer-ugens)

(defn inst-test []
  (definst bar [freq 200]
           (* (env-gen (perc 0.1 0.8) 1 1 0 1 :free)
              (rlpf (saw freq) (* 1.1 freq) 0.3)
              0.4))

  (definst buz [freq 200]
           (* (env-gen (perc 0.1 0.8) 1 1 0 1 :free)
              (+ (sin-osc (/ freq 2))
                 (rlpf (saw freq) (* 1.1 freq) 0.3))
              0.4)))

(defonce sequencer-metro* (ref (metronome 128)))
(defonce sequences* (ref {}))

(defn sequence-pattern [inst pat]
  (dosync (alter sequences* assoc inst pat)))

(defn- sequencer-player [beat]
  (doseq [[inst pat] @sequences]
    (pat inst))
  (apply-at #'sequence-player (@sequencer-metro* (inc beat)) (inc beat)))

(defn sequencer-play []
  (sequencer-player (metro)))

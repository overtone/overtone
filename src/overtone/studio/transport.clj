(ns overtone.studio.transport
  (:use [overtone.music rhythm]))

(def DEFAULT-BPM 128)

(def ^:dynamic *clock* (metronome DEFAULT-BPM))

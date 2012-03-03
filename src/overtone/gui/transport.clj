(ns overtone.gui.transport
  (:use [overtone.music rhythm]
        [overtone.gui color]
        seesaw.core))

(def ^:dynamic *master-metronome* (metronome 130))

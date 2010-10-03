(ns overtone.live
  (:use overtone.util)
  (:require clojure.stacktrace
            midi
            osc
            byte-spec
            (overtone.core config time-utils log sc ugen synth synthdef envelope sample)
            (overtone.music rhythm pitch tuning)
            overtone.studio
            (overtone.studio fx)))

(immigrate
  'clojure.stacktrace
  'osc
  'midi
  'overtone.core.time-utils
  'overtone.core.util
  'overtone.core.event
  'overtone.core.sc
  'overtone.core.ugen
  'overtone.core.synth
  'overtone.core.sample
  'overtone.core.synthdef
  'overtone.core.envelope
  'overtone.music.rhythm
  'overtone.music.pitch
  'overtone.music.tuning
  'overtone.studio
  'overtone.studio.fx
  )

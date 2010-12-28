(ns overtone.live
  (:use overtone.ns)
  (:require clojure.stacktrace
            midi osc byte-spec
            (overtone config time-utils log)
            (overtone.sc core ugen synth synthdef envelope sample sc-lang)
            (overtone.music rhythm pitch tuning)
            (overtone.studio core fx)
            (overtone.console viz)))

(immigrate
  'clojure.stacktrace
  'osc
  'midi
  'overtone.time-utils
  'overtone.util
  'overtone.event
  'overtone.sc.core
  'overtone.sc.ugen
  'overtone.sc.synth
  'overtone.sc.sample
  'overtone.sc.synthdef
  'overtone.sc.envelope
  'overtone.sc.sc-lang
  'overtone.music.rhythm
  'overtone.music.pitch
  'overtone.music.tuning
  'overtone.studio.core
  'overtone.studio.fx
  'overtone.console.viz
  )

(defonce _auto-boot_ (boot))

(wait-until-booted)

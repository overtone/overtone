
(ns overtone.live
  (:use overtone.ns)
  (:require clojure.stacktrace
            midi osc byte-spec
            [overtone config time-utils log]
            [overtone.sc allocator core ugen node synth synthdef
             trigger buffer envelope bus sample sc-lang]
            [overtone.music rhythm pitch tuning]
            [overtone.studio core util fx]
            [overtone.console viz]
            [overtone.viz scope]))

(immigrate
  'osc
  'midi
  'overtone.log
  'overtone.time-utils
  'overtone.util
  'overtone.event
  'overtone.sc.core
  'overtone.sc.node
  'overtone.sc.buffer
  'overtone.sc.trigger
  'overtone.sc.ugen
  'overtone.sc.synth
  'overtone.sc.sample
  'overtone.sc.synthdef
  'overtone.sc.envelope
  'overtone.sc.sc-lang
  'overtone.sc.bus
  'overtone.music.rhythm
  'overtone.music.pitch
  'overtone.music.tuning
  'overtone.studio.core
  'overtone.studio.fx
  'overtone.console.viz
  'overtone.viz.scope
 )

(defonce _auto-boot_ (boot))

(wait-until-booted)

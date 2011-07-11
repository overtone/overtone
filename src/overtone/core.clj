(ns overtone.core
  (:use overtone.ns)
  (:require clojure.stacktrace
            midi osc byte-spec
            [overtone config time-utils log]
            [overtone.helpers chance scaling]
            [overtone.sc allocator core ugen node synth synthdef
             trigger buffer envelope bus sample sc-lang]
            [overtone.music rhythm pitch tuning]
            [overtone.studio core util fx]
            [overtone.viz scope]))

(immigrate
  'osc
  'midi
  'overtone.log
  'overtone.time-utils
  'overtone.util
  'overtone.helpers.chance
  'overtone.helpers.scaling
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
  'overtone.viz.scope
 )

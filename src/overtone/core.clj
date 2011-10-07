(ns overtone.core
  (:use [overtone.util ns])
  (:require clojure.stacktrace
            [overtone.midi]
            [overtone.osc]
            [overtone.algo chance scaling trig]
            [overtone.sc buffer bus envelope node example
                         sample server synth trigger gens info]
            [overtone.music rhythm pitch tuning time]
            [overtone.studio rig util fx]
            [overtone.repl ugens examples shell]
            [overtone.libs.event]
            [overtone.viz scope]))

(immigrate
 'overtone.osc
 'overtone.midi
 'overtone.algo.chance
 'overtone.algo.scaling
 'overtone.algo.trig
 'overtone.sc.server
 'overtone.sc.node
 'overtone.sc.buffer
 'overtone.sc.trigger
 'overtone.sc.gens
 'overtone.sc.synth
 'overtone.sc.sample
 'overtone.sc.envelope
 'overtone.sc.bus
 'overtone.sc.example
 'overtone.sc.info
 'overtone.music.rhythm
 'overtone.music.pitch
 'overtone.music.tuning
 'overtone.music.time
 'overtone.studio.rig
 'overtone.studio.fx
 'overtone.repl.ugens
 'overtone.repl.examples
 'overtone.repl.shell
 'overtone.libs.event
 'overtone.viz.scope
 )

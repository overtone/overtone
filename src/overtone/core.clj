(ns overtone.core
  (:use [overtone.util ns])
  (:require clojure.stacktrace
            [overtone.midi]
            [overtone.osc]
            [overtone.algo chance scaling trig]
            [overtone.sc buffer bus envelope node example
                         sample server synth trigger ugens]
            [overtone.sc.cgens audio-in oscillators demand mix]
            [overtone.sc.examples demand osc trig compander audio-in]
            [overtone.music rhythm pitch tuning time]
            [overtone.studio rig util fx]
            [overtone.repl ugens examples]
            [overtone.util position]
            [overtone.viz scope]))

(immigrate
 'overtone.osc
 'overtone.midi
 'overtone.algo.chance
 'overtone.algo.scaling
 'overtone.util.position
 'overtone.algo.trig
 'overtone.sc.server
 'overtone.sc.node
 'overtone.sc.buffer
 'overtone.sc.trigger
 'overtone.sc.ugens
 'overtone.sc.synth
 'overtone.sc.sample
 'overtone.sc.envelope
 'overtone.sc.bus
 'overtone.sc.cgens.audio-in
 'overtone.sc.cgens.oscillators
 'overtone.sc.cgens.demand
 'overtone.sc.cgens.mix
 'overtone.sc.example
 'overtone.sc.examples.demand
 'overtone.sc.examples.osc
 'overtone.sc.examples.trig
 'overtone.sc.examples.compander
 'overtone.sc.examples.audio-in
 'overtone.music.rhythm
 'overtone.music.pitch
 'overtone.music.tuning
 'overtone.music.time
 'overtone.studio.rig
 'overtone.studio.fx
 'overtone.repl.ugens
 'overtone.repl.examples
 'overtone.viz.scope
 )

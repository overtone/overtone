(ns overtone.api
  (:use [overtone.libs boot-msg app-icon]
        [overtone.helpers.ns])
  (:require clojure.stacktrace
            [overtone.config store]
            [overtone version osc speech]
            [overtone.algo chance scaling trig fn]
            [overtone.sc bindings buffer bus envelope example info
                         ugens defcgen node sample server synth
                         foundation-groups dyn-vars trig]
            [overtone.sc.cgens oscillators demand mix io buf-io env tap
                               line freq beq-suite berlach bhob info]
            [overtone.music rhythm pitch tuning time]
            [overtone.studio mixer inst util fx wavetable midi midi-player core]
            [overtone.repl ugens examples shell inst debug graphviz]
            [overtone.libs asset event]
            [overtone.gui scope mixer control]
            [overtone.samples freesound]))

(defn immigrate-overtone-api []
  (immigrate
   'overtone.osc
   'overtone.algo.chance
   'overtone.algo.scaling
   'overtone.algo.trig
   'overtone.algo.fn
   'overtone.config.store
   'overtone.sc.bindings
   'overtone.sc.buffer
   'overtone.sc.bus
   'overtone.sc.envelope
   'overtone.sc.example
   'overtone.sc.info
   'overtone.sc.node
   'overtone.sc.sample
   'overtone.sc.server
   'overtone.sc.synth
   'overtone.sc.ugens
   'overtone.sc.defcgen
   'overtone.sc.foundation-groups
   'overtone.sc.dyn-vars
   'overtone.sc.trig
   'overtone.sc.cgens.oscillators
   'overtone.sc.cgens.demand
   'overtone.sc.cgens.mix
   'overtone.sc.cgens.io
   'overtone.sc.cgens.buf-io
   'overtone.sc.cgens.env
   'overtone.sc.cgens.tap
   'overtone.sc.cgens.line
   'overtone.sc.cgens.freq
   'overtone.sc.cgens.beq-suite
   'overtone.sc.cgens.berlach
   'overtone.sc.cgens.bhob
   'overtone.sc.cgens.info
   'overtone.studio.mixer
   'overtone.studio.inst
   'overtone.studio.fx
   'overtone.studio.wavetable
   'overtone.studio.midi
   'overtone.studio.midi-player
   'overtone.studio.core
   'overtone.music.rhythm
   'overtone.music.pitch
   'overtone.music.tuning
   'overtone.music.time
   'overtone.speech
   'overtone.repl.ugens
   'overtone.repl.examples
   'overtone.repl.shell
   'overtone.repl.inst
   'overtone.repl.debug
   'overtone.repl.graphviz
   'overtone.libs.asset
   'overtone.libs.event
   'overtone.samples.freesound
   'overtone.version))

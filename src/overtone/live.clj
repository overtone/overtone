(ns overtone.live
  (:use [overtone.util lib ns])
  (:require clojure.stacktrace
            [overtone.config store]
            [overtone.midi]
            [overtone.osc]
            [overtone.algo chance scaling trig]
            [overtone.sc bindings buffer bus envelope example info
                         ugens defcgen mixer node sample server synth trigger]

            [overtone.sc.cgens oscillators demand mix io buf-io env tap]
            [overtone.music rhythm pitch tuning time]
            [overtone.studio inst mixer util fx wavetable]
            [overtone.repl ugens examples shell inst]
            [overtone.libs asset event freesound]
            [overtone.version]
            [overtone.viz scope]
            [overtone.gui control]))

(immigrate
 'overtone.osc
 'overtone.midi
 'overtone.algo.chance
 'overtone.algo.scaling
 'overtone.algo.trig
 'overtone.config.store
 'overtone.sc.bindings
 'overtone.sc.buffer
 'overtone.sc.bus
 'overtone.sc.envelope
 'overtone.sc.example
 'overtone.sc.info
 'overtone.sc.mixer
 'overtone.sc.node
 'overtone.sc.sample
 'overtone.sc.server
 'overtone.sc.synth
 'overtone.sc.trigger
 'overtone.sc.ugens
 'overtone.sc.defcgen
 'overtone.sc.cgens.oscillators
 'overtone.sc.cgens.demand
 'overtone.sc.cgens.mix
 'overtone.sc.cgens.io
 'overtone.sc.cgens.buf-io
 'overtone.sc.cgens.env
 'overtone.sc.cgens.tap
 'overtone.music.rhythm
 'overtone.music.pitch
 'overtone.music.tuning
 'overtone.music.time
 'overtone.studio.mixer
 'overtone.studio.inst
 'overtone.studio.fx
 'overtone.studio.wavetable
 'overtone.repl.ugens
 'overtone.repl.examples
 'overtone.repl.shell
 'overtone.repl.inst
 'overtone.libs.asset
 'overtone.libs.event
 'overtone.libs.freesound
 'overtone.version
 'overtone.viz.scope)

(defonce _auto-boot_ (boot-mixer))

(print-ascii-art-overtone-logo (overtone.config.store/config-get :user-name) OVERTONE-VERSION-STR)

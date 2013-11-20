(ns overtone.api
  (:import [java.lang.management ManagementFactory])
  (:use [overtone.libs boot-msg app-icon]
        [overtone.helpers.ns])
  (:require clojure.stacktrace
            [overtone.config store]
            [overtone version osc speech]
            [overtone.algo chance scaling trig fn lists]
            [overtone.sc bindings buffer bus envelope example info
             ugens defcgen node sample server synth
             foundation-groups dyn-vars trig]
            [overtone.sc.cgens oscillators demand mix io buf-io env tap
             line freq beq-suite berlach bhob info]
            [overtone.music rhythm pitch tuning time]
            [overtone.studio mixer inst util fx wavetable midi midi-player core scope]
            [overtone.repl ugens examples shell inst debug graphviz]
            [overtone.libs asset event]
            [overtone.samples freesound]
            [overtone.helpers.doc :refer [fs]]))


;; Currently the default lein setting drastically reduces performance in
;; return for a 200ms improvement of the startup time. See:
;; https://github.com/technomancy/leiningen/pull/1230
(defonce __PRINT_TIERED_COMPILATION_WARNING__
  (let [compiler-bean (ManagementFactory/getCompilationMXBean)
        compiler-name (.getName compiler-bean)
        runtime-bean (ManagementFactory/getRuntimeMXBean)
        input-args    (.getInputArguments runtime-bean)]

    (when-let [arg (and (re-find #"Tiered" compiler-name)
                        (some #(re-find #"TieredStopAtLevel=1" %)
                              input-args))]
      (println
       (fs "**********************************************************
            WARNING: JVM argument TieredStopAtLevel=1 is active, and may
            lead to reduced performance. This happens to currently be
            the default lein setting:

            https://github.com/technomancy/leiningen/pull/1230

            If you didn't intend this JVM arg to be specified, you can
            turn it off in your project.clj file or your global
            ~/.lein/profiles.clj file by adding the key-val

            :jvm-opts ^:replace []
              **********************************************************")))))


(defn immigrate-overtone-api []
  (immigrate
   'overtone.osc
   'overtone.algo.chance
   'overtone.algo.scaling
   'overtone.algo.trig
   'overtone.algo.fn
   'overtone.algo.lists
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

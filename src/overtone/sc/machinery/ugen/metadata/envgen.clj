(ns overtone.sc.machinery.ugen.metadata.envgen
  (:use [overtone.sc.machinery.ugen common check]))

(def specs
     [
      {:name "Done",
       :args [{:name "src" :doc "ugen to monitor"}],
       :rates #{:kr}
       :doc "Outputs a one when the src ugen (typically an envelope) has finished"}

      {:name "FreeSelf",
       :args [{:name "in" :doc "input signal"}],
       :rates #{:kr}
       :doc "Free the enclosing synth when triggered"}

      {:name "PauseSelf",
       :args [{:name "in" :doc "input signal"}],
       :rates #{:kr}
       :doc "Pause the enclosing synth when triggered"}

      {:name "FreeSelfWhenDone",
       :args [{:name "src" :doc "the ugen to check for done"}],
       :rates #{:kr}
       :doc "Free the enclosing synth when the src ugen finishes (e.g. env-gen, play-buf, linen...)"}

      {:name "PauseSelfWhenDone",
       :args [{:name "src" :doc "the ugen to check for done"}],
       :rates #{:kr}
       :doc "Pause the enclosing synth when the src ugen finishes (e.g. env-gen, play-buf, linen...)"}

      {:name "Pause",
       :args [{:name "gate" :doc "when gate is 0,  node is paused, when 1 it runs"}
              {:name "id" :doc "node to be paused"}],
       :rates #{:kr}
       :doc "Pause a specified node when triggered"}

      {:name "Free",
       :args [{:name "trig" :doc "when triggered, frees node"}
              {:name "id" :doc "node to be freed"}],
       :rates #{:kr}
       :doc "Free the specified node when triggered"}

      {:name "EnvGen",
       :args [{:name "envelope" :mode :append-sequence :doc "an Array of Controls."}
              {:name "gate", :default 1.0 :doc "this triggers the envelope and holds it open while > 0. If the Env is fixed-length (e.g. Env.linen, Env.perc), the gate argument is used as a simple trigger. If it is an sustaining envelope (e.g. Env.adsr, Env.asr), the envelope is held open until the gate becomes 0, at which point is released.
"}
              {:name "levelScale", :default 1.0 :doc "scales the levels of the breakpoints."}
              {:name "levelBias", :default 0.0 :doc "offsets the levels of the breakpoints."}
              {:name "timeScale", :default 1.0 :doc "scales the durations of the segments."}
              {:name "action", :default 0 :doc "an integer representing an action to be executed when the env is finished playing. This can be used to free the enclosing synth, etc."}]
       :doc "envelope generator, interpolates across a path of control points over time, see the overtone.sc.envelope functions to generate the control points array

  Note:

The actual minimum duration of a segment is not zero, but one sample step for audio rate and one block for control rate. This may result in asynchronicity when in two envelopes of different number of levels, the envelope times add up to the same total duration. Similarly, when modulating times, the new time is only updated at the end of the current segment - this may lead to asynchronicity of two envelopes with modulated times.
"}
               ;(let [envec (TODO turn env object into vector)]

      {:name "Linen",
       :args [{:name "gate", :default 1.0 :doc "Input trigger"}
              {:name "attackTime", :default 0.01 :doc "Time taken to rise to susLevel in seconds"}
              {:name "susLevel", :default 1.0 :doc "Level to hold the envelope at until gate is triggered"}
              {:name "releaseTime", :default 1.0 :doc "Time to fall from susLevel back to 0 after the gate has been triggered"}
              {:name "action", :default 0 :doc "done action"}],
       :rates #{:kr}
       :doc "A linear envelope generator, rises to susLevel over attackTime seconds and after the gate goes non-positive falls over releaseTime to finally perform an option doneAction"}

      ;; TODO figure out what an IEnvGen is and write init
      {:name "IEnvGen"
       :args [{:name "ienvelope" :doc "an InterplEnv (this is static for the life of the UGen)"}
              {:name "index" :doc "a point to access within the InterplEnv"}]
       :doc "Plays back break point envelopes from the index point."
;;       :init (fn [rate [env & args] spec])
       }])

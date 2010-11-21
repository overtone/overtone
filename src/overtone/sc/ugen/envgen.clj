(ns overtone.sc.ugen.envgen
  (:use (overtone.sc.ugen common)))

(def specs
     [
      {:name "Done",
       :args [{:name "src"}],
       :rates #{:kr}
       :doc "Outputs a one when the src ugen (typically an envelope) has finished"}

      {:name "FreeSelf",
       :args [{:name "in"}],
       :rates #{:kr}
       :doc "Free the enclosing synth when triggered"}

      {:name "PauseSelf",
       :args [{:name "in"}],
       :rates #{:kr}
       :doc "Pause the enclosing synth when triggered"}

      {:name "FreeSelfWhenDone",
       :args [{:name "src"}],
       :rates #{:kr}
       :doc "Free the enclosing synth when the src ugen finishes (e.g. env-gen, play-buf, linen...)"}

      {:name "PauseSelfWhenDone",
       :args [{:name "src"}],
       :rates #{:kr}
       :doc "Pause the enclosing synth when the src ugen finishes (e.g. env-gen, play-buf, linen...)"}

      {:name "Pause",
       :args [{:name "gate"}
              {:name "id"}],
       :rates #{:kr}
       :doc "Pause the enclosing synth when triggered"}

      {:name "Free",
       :args [{:name "trig"}
              {:name "id"}],
       :rates #{:kr}
       :doc "Free the enclosing synth when triggered"}

      {:name "EnvGen",
       :args [{:name "envelope" :mode :append-sequence}
              {:name "gate", :default 1.0}
              {:name "levelScale", :default 1.0}
              {:name "levelBias", :default 0.0}
              {:name "timeScale", :default 1.0}
              {:name "action", :default :none :map DONE-ACTIONS}]
       :doc "envelope generator, interpolates across a path of control points over time, see the
            overtone.sc.envelope functions to generate the control points array"}
               ;(let [envec (TODO turn env object into vector)]

      {:name "Linen",
       :args [{:name "gate", :default 1.0}
              {:name "attackTime", :default 0.01}
              {:name "susLevel", :default 1.0}
              {:name "releaseTime", :default 1.0}
              {:name "action", :default :none :map DONE-ACTIONS}],
       :rates #{:kr}
       :doc "a linear envelope generator, rises to susLevel over attackTime seconds and after the
            gate goes non-positive falls over releaseTime to finally perform an option doneAction"}

      ;; TODO figure out what an IEnvGen is and write init
      {:name "IEnvGen"
       :args [{:name "ienvelope"}
              {:name "index"}]
       :muladd true
       :init (fn [rate [env & args] spec]
               )}])

(ns overtone.sc.machinery.ugen.metadata.extras.glitch
  (:use [overtone.sc.machinery.ugen common check]))

(def specs
  [
   {:name "GlitchRHPF", :args [{:name "in", :default 0.0 :doc "input signal to be processed"}
                               {:name "freq", :default 440.0 :doc "cutoff frequency"}
                               {:name "rq", :default 1.0 :doc "the reciprocal of Q.  bandwidth / cutoffFreq"}]
    :check (nth-input-stream? 0)
    :doc "Old skool resonant high pass filter (not using double precision floats)"
    :auto-rate true}

   {:name "GlitchHPF",
    :args [{:name "in", :default 0.0 :doc "input signal to be processed"}
           {:name "freq", :default 440.0 :doc "cutoff frequency"}]
    :check (nth-input-stream? 0)
    :doc "Old skool second order high pass filter (not using double precision floats)"
    :auto-rate true}])

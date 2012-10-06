(ns
  ^{:doc "Higher level instrument and studio abstractions."
     :author "Jeff Rose & Sam Aaron"}
  overtone.studio.core
  (:use [overtone.sc defaults server])
  (:require [overtone.config.log :as log]))

(defonce studio* (atom {:synth-group       nil
                        :instruments       {}
                        :instrument-group  nil
                        :master-volume     DEFAULT-MASTER-VOLUME
                        :input-gain        DEFAULT-MASTER-GAIN
                        :bus-mixers        {:in []
                                            :out []}
                        :recorder          nil}))

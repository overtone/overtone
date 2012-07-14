(ns
  ^{:doc "Higher level instrument and studio abstractions."
     :author "Jeff Rose & Sam Aaron"}
  overtone.studio.core
  (:use [overtone.sc defaults server])
  (:require [overtone.config.log :as log]))

(defonce studio* (atom {:synth-group       nil
                        :input-group       nil
                        :root-group        nil
                        :mixer-group       nil
                        :monitor-group     nil
                        :instruments       {}
                        :instrument-group  nil
                        :master-volume     DEFAULT-MASTER-VOLUME
                        :input-gain        DEFAULT-MASTER-GAIN
                        :bus-mixers        {:in []
                                            :out []}
                        :recorder          nil}))

(defn root-group
  []
  (ensure-connected!)
  (:root-group @studio*))

(defn main-mixer-group
  []
  (ensure-connected!)
  (:mixer-group @studio*))

(defn main-monitor-group
  []
  (ensure-connected!)
  (:monitor-group @studio*))

(defn main-input-group
  []
  (ensure-connected!)
  (:input-group @studio*))


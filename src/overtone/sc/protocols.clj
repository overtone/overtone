(ns overtone.sc.protocols)

(defprotocol IKillable
  :extend-via-metadata true
  (kill* [this] "Kill a synth element (node, or group, or ...)."))

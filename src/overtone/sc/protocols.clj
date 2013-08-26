(ns overtone.sc.protocols)

(defprotocol IKillable
  (kill* [this] "Kill a synth element (node, or group, or ...)."))

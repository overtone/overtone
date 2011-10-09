(ns overtone.sc.machinery.defaults
  (:require [overtone.at-at :as at-at]))

(def MAX-NODES
  "Maximum number of executing nodes allowed in the server"
  4000)

(def MAX-BUFFERS
  "Maximum number of audio buffers allowed in the server"
  4000)

(def MAX-SDEFS
  "Maximum number of SynthDefs allowed in the server"
  2000)

(def MAX-AUDIO-BUS
  "Maximum number of audio busses allowed in the server"
  1000)

(def MAX-CONTROL-BUS
  "Maximum number of control busses allowed in the server"
  4000)

(def AUDIO-BUS-RESERVE-COUNT
  "Number of audio busses to reserve. These busses won't be available to users
  via overtone.sc.bus/audio-bus"
  50)

(def SERVER-PORT
  "Default port number used when booting external server. If nil, a random port is used"
  nil)

(def N-RETRIES
  "Number of times to attempt to connect to an externally booted server"
  50)

(def REPLY-TIMEOUT
  "Max number of milliseconds to wait for a reply from the server"
  500)

(def MAX-OSC-SAMPLES
  "Max number of samples supported in a UDP OSC message"
  8192)

(def SC-POOL
  "make an at-at pool for all default scheduling"
  (at-at/mk-pool))

(def SC-PATHS
  "Default system paths to an externally installed SuperCollider server for
  various operating systems."
  {:linux ["scsynth"]
   :windows ["C:/Program Files/SuperCollider/scsynth.exe"
             "D:/Program Files/SuperCollider/scsynth.exe"
             "E:/Program Files/SuperCollider/scsynth.exe"
             "C:/Program Files (x86)/SuperCollider/scsynth.exe"
             "D:/Program Files (x86)/SuperCollider/scsynth.exe"
             "E:/Program Files (x86)/SuperCollider/scsynth.exe"]
   :mac  ["/Applications/SuperCollider/scsynth"] })

(def SC-ARGS
  "Extra arguments required to correctly boot an external SuperCollider server
  for various operating systems."
  {:linux []
   :windows []
   :mac   ["-U" "/Applications/SuperCollider/plugins"]})

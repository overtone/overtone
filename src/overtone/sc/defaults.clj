(ns overtone.sc.defaults
  (:use [overtone.helpers.file :only [dir-exists?]]
        [overtone.helpers.lib :only [windows-sc-path]])
  (:require [overtone.at-at :as at-at]))

(def DEFAULT-MASTER-VOLUME
  "Initial value for the master volume of the mixer"
  0.8)

(def DEFAULT-MASTER-GAIN
  "Initial value for the master gain of the mixer"
  1)

(def DEFAULT-VOICE-VOLUME
  "Initial value for the volume of a voice"
  1.0)

(def DEFAULT-VOICE-PAN
  "Initial value for the pan of a voice (center)"
  0.0)

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
   :windows [(str (windows-sc-path) "\\scsynth.exe")]
   :mac  ["/Applications/SuperCollider/scsynth"
          "/Applications/SuperCollider.app/Contents/Resources/scsynth"
          "/Applications/SuperCollider/SuperCollider.app/Contents/Resources/scsynth"]})

(def SC-OS-SPECIFIC-ARGS
  "Extra arguments required to correctly boot an external SuperCollider
  server for various operating systems."
  {:linux   {}
   :windows {}
   :mac     {:ugens-paths  ["/Applications/SuperCollider/plugins"
                            "/Applications/SuperCollider.app/Contents/Resources/plugins"
                            "/Applications/SuperCollider/SuperCollider.app/Contents/Resources/plugins"]}})

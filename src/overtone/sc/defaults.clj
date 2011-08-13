(ns overtone.sc.defaults)

;; ## SCSynth limits
(def MAX-NODES 4000)
(def MAX-BUFFERS 4000)
(def MAX-SDEFS 2000)
(def MAX-AUDIO-BUS 1000)
(def MAX-CONTROL-BUS 4000)

;;Number of audio busses to reserve. These busses won't be available to users
;;via overtone.sc.bus/audio-bus
(def AUDIO-BUS-RESERVE-COUNT 50)

(ns overtone.sc.defaults
  (:use [overtone.helpers.file :only [dir-exists?]])
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
   :windows ["C:/Program Files/SuperCollider/scsynth.exe"
             "D:/Program Files/SuperCollider/scsynth.exe"
             "E:/Program Files/SuperCollider/scsynth.exe"
             "C:/Program Files (x86)/SuperCollider/scsynth.exe"
             "D:/Program Files (x86)/SuperCollider/scsynth.exe"
             "E:/Program Files (x86)/SuperCollider/scsynth.exe"]
   :mac  ["/Applications/SuperCollider/scsynth"
          "/Applications/SuperCollider.app/Contents/Resources/scsynth"
          "/Applications/SuperCollider/SuperCollider.app/Contents/Resources/scsynth"]})

(def SC-OS-SPECIFIC-ARGS
  "Extra arguments required to correctly boot an external SuperCollider
  server for various operating systems."
  {:linux   {}
   :windows {}
   :mac     {:ugens-path  ["/Applications/SuperCollider/plugins"
                           "/Applications/SuperCollider.app/Contents/Resources/plugins"
                           "/Applications/SuperCollider/SuperCollider.app/Contents/Resources/plugins"]}})

(def SC-ARG-INFO
  "Default arguments for starting up a SuperCollider process. Does not
  include -u or -t which should be determined by analysing :port
  and :udp?."
  {:port             {:default 57710            :desc "Port number"}
   :udp?             {:default 1                :desc "1 means use UDP, 0 means use TCP"}
   :user-ugens-paths {:default nil              :desc "A list of paths to additional ugen directories. This list will be prepended to the default ugens list"}
   :max-control-bus  {:default 4096  :flag "-c" :desc "Number of control bus channels"}
   :max-audio-bus    {:default 128   :flag "-a" :desc "Number of audio bus channels"}
   :max-input-bus    {:default 8     :flag "-i" :desc "Number of input bus channels"}
   :max-output-bus   {:default 8     :flag "-o" :desc "Number of output bus channels"}
   :block-size       {:default 64    :flag "-z" :desc "Block size"}
   :hw-buffer-size   {:default nil   :flag "-Z" :desc "Hardware buffer size"}
   :hw-sample-rate   {:default nil   :flag "-S" :desc "Hardware sample rate"}
   :max-buffers      {:default 1024  :flag "-b" :desc "Number of sample buffers"}
   :max-nodes        {:default 1024  :flag "-n" :desc "Max number of executing nodes allowed in the server"}
   :max-sdefs        {:default 1024  :flag "-d" :desc "Max number of synthdefs allowed"}
   :rt-mem-size      {:default 8192  :flag "-m" :desc "Real time memory size"}
   :max-w-buffers    {:default 64    :flag "-w" :desc"Number of wire buffers"}
   :num-rand-seeds   {:default 64    :flag "-r" :desc"Number of random seeds"}
   :load-sdefs?      {:default 1     :flag "-D" :desc "Load synthdefs on boot? 0 or 1"}
   :rendezvous?      {:default 1     :flag "-R" :desc "Publish to rendezvous? 0 or 1"}
   :max-logins       {:default 64    :flag "-l" :desc "Maximum number of named return addresses stored - also maximum number of txp connections accepted."}
   :pwd              {:default nil   :flag "-p" :desc "When using TCP, the session password must be the first command sent."}
   :non-realtime     {:default nil   :flag "-N" :desc "Non-realtime mode. Requires a space separated string of <cmd-fielname> <input-filename> <output-filename> <sample-rate> <header-format> <sample-format>"}
   :in-streams       {:default nil   :flag "-I" :desc "Input streams enabled"}
   :out-streams      {:default nil   :flag "-O" :desc "Output streams enabled"}
   :hw-device-name   {:default nil   :flag "-H" :desc "Hardware device name"}
   :verbosity        {:default 0     :flag "-v" :desc "Verbosity mode. 0 is normal behaviour, -1 suppress information messages, -2 suppresses informational and many error messages"}
   :ugens-paths      {:default nil   :flag "-U" :desc "A list of paths of ugen directories. If specified, the standard paths are NOT searched for plugins."}
   :restricted-path  {:default nil   :flag "-P" :desc "Prevents file-accesing OSC commands from accessing files outside the specified path."}})

(defn sc-arg-default
  "Return the default value for the sc arg"
  [arg-name]
  (-> arg-name SC-ARG-INFO :default))

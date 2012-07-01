(ns overtone.sc.machinery.server.args
  (:use [overtone.sc.defaults]
        [overtone.config store]
        [overtone.helpers.system])
  (:require [overtone.helpers.math :as math]
            [overtone.jna-path]))

(def SC-ARG-INFO
  "Default arguments for starting up a SuperCollider process. Does not
  include -u or -t which should be determined by analysing :port
  and :udp?."
  {:port                     {:default 57710            :desc "Port number"}
   :udp?                     {:default 1                :desc "1 means use UDP, 0 means use TCP"}
   :max-control-bus          {:default 4096  :flag "-c" :desc "Number of control bus channels"}
   :max-audio-bus            {:default 128   :flag "-a" :desc "Number of audio bus channels"}
   :max-input-bus            {:default 8     :flag "-i" :desc "Number of input bus channels"}
   :max-output-bus           {:default 8     :flag "-o" :desc "Number of output bus channels"}
   :block-size               {:default 64    :flag "-z" :desc "Block size"}
   :hw-buffer-size           {:default nil   :flag "-Z" :desc "Hardware buffer size"}
   :hw-sample-rate           {:default nil   :flag "-S" :desc "Hardware sample rate"}
   :max-buffers              {:default 1024  :flag "-b" :desc "Number of sample buffers"}
   :max-nodes                {:default 1024  :flag "-n" :desc "Max number of executing nodes allowed in the server"}
   :max-sdefs                {:default 1024  :flag "-d" :desc "Max number of synthdefs allowed"}
   :rt-mem-size              {:default 8192  :flag "-m" :desc "Real time memory size"}
   :max-w-buffers            {:default 64    :flag "-w" :desc "Number of wire buffers"}
   :num-rand-seeds           {:default 64    :flag "-r" :desc "Number of random seeds"}
   :load-sdefs?              {:default 1     :flag "-D" :desc "Load synthdefs on boot? 0 or 1"}
   :rendezvous?              {:default 0     :flag "-R" :desc "Publish to rendezvous? 0 or 1"}
   :max-logins               {:default 64    :flag "-l" :desc "Maximum number of named return addresses stored - also maximum number of txp connections accepted."}
   :pwd                      {:default nil   :flag "-p" :desc "When using TCP, the session password must be the first command sent."}
   :realtime?                {:default 1                :desc "Run in realtime mode? If 0 then the other nrt flags must be set"}
   :nrt-cmd-filename         {:default nil              :desc "Command filename for non-realtime mode"}
   :nrt-input-filename       {:default nil              :desc "Input filename for non-realtime mode"}
   :nrt-output-filename      {:default nil              :desc "Output filename for non-realtime mode"}
   :nrt-output-header-format {:default nil              :desc "Header format for non-realtime mode"}
   :nrt-output-sample-format {:default nil              :desc "Sample format for non-realtime mode"}
   :in-streams               {:default nil   :flag "-I" :desc "Input streams enabled"}
   :out-streams              {:default nil   :flag "-O" :desc "Output streams enabled"}
   :hw-device-name           {:default nil   :flag "-H" :desc "Hardware device name"}
   :verbosity                {:default 0     :flag "-v" :desc "Verbosity mode. 0 is normal behaviour, -1 suppress information messages, -2 suppresses informational and many error messages"}
   :ugens-paths              {:default nil   :flag "-U" :desc "A list of paths of ugen directories. If specified, the standard paths are NOT searched for plugins."}
   :restricted-path          {:default nil   :flag "-P" :desc "Prevents file-accesing OSC commands from accessing files outside the specified path."}})



(defn- number
  [arg-name val]
  (cond
    (number? val) val
    (string? val) (Integer. val)
    :else (throw (Exception. (str "Cannot convert sc-arg " arg-name " to val: " val)))))

(defn- truth-int
  [arg-name val]
  (cond
    (= 0 val) 0
    (= 1 val) 1
    (= true val) 1
    (= false val) 0
    :else (throw (Exception. (str "Unable to convert sc-arg " arg-name " to a bool-like int. Expected one of 0, 1, true, false. Got: " val)))))

(defn- next-power-of-two
  [arg-name val]
  (let [val (number arg-name val)]
    (math/next-power-of-two val)))

(defn- mac-only!
  [arg-name val]
  (when-not (mac-os?)
    (throw (Exception. (str "Unable to use sc-arg " arg-name " on non mac platforms."))))
  val)

(defn- arg-identity
  [k v]
  v)

(def SC-ARG-INFO-CLEANUP-FNS
  {:port                     number
   :udp?                     truth-int
   :max-control-bus          number
   :max-audio-bus            number
   :max-input-bus            number
   :max-output-bus           number
   :block-size               next-power-of-two
   :hw-buffer-size           next-power-of-two
   :hw-sample-rate           number
   :max-buffers              next-power-of-two
   :max-nodes                next-power-of-two
   :max-sdefs                next-power-of-two
   :rt-mem-size              number
   :max-w-buffers            number
   :num-rand-seeds           number
   :load-sdefs?              truth-int
   :rendezvous?              truth-int
   :max-logins               next-power-of-two
   :pwd                      arg-identity
   :realtime?                truth-int
   :nrt-cmd-filename         arg-identity
   :nrt-input-filename       arg-identity
   :nrt-output-filename      arg-identity
   :nrt-output-header-format arg-identity
   :nrt-output-sample-format arg-identity
   :in-streams               mac-only!
   :out-streams              mac-only!
   :hw-device-name           arg-identity
   :hw-out-device-name       mac-only!
   :verbosity                number
   :ugens-paths              arg-identity
   :restricted-path          arg-identity})

(def native-arg-defaults
  {:pwd                      ""
   :in-streams               ""
   :out-streams              ""
   :restricted-path          ""
   :nrt-cmd-filename         ""
   :nrt-input-filename       ""
   :nrt-output-filename      ""
   :nrt-output-header-format ""
   :nrt-output-sample-foramt ""
   :hw-device-name           ""
   :hw-out-device-name       ""})

(def non-modifiable-native-scsynth-options
  {:mBufLength                        64
   :mNumSharedControls                0
   :mSharedControls                   nil
   :mMemoryLocking                    0
   :mPreferredSampleRate              0
   :mPreferredHardwareBufferFrameSize 0
   :mSharedMemoryID                   0
   :mUGensPluginPath                  (System/getProperty "jna.library.path")})

(defn- cleanup-sc-args
  [args]
  (into {} (map (fn [[k v]] [k ((get SC-ARG-INFO-CLEANUP-FNS k (fn [k v] v))  k v)]) args)))

(defn sc-default-args
  "Return a map of keyword to default value for each scsynth arg. Reads
  info from SC-ARG-INFO"
  []
  (reduce (fn [res [arg-name {default :default}]]
            (if default
              (merge res {arg-name default})
              res))
          {}
          SC-ARG-INFO))

(defn merge-sc-args
  ([user-opts] (merge-sc-args user-opts {}))
  ([user-opts default-opts]
     (let [opts (merge (sc-default-args)
                       (SC-OS-SPECIFIC-ARGS (config-get :os))
                       default-opts
                       (config-get :sc-args {})
                       user-opts)]
       (cleanup-sc-args opts))))

(defn ensure-native-sc-args-valid!
  [args]
  (doseq [[k v] args]
    (when (nil? v)
      (println (str "Warning - native sc-arg " k " was nil"))
;;      (throw (Exception. (str "Error - native sc-arg " k " was nil")))
      ))
  args)

(defn merge-native-sc-args
  [user-opts]
  (let [args (merge-sc-args user-opts)
        args (merge args non-modifiable-native-scsynth-options)
        args (into {} (map (fn [[k v]]
                             [k (if (and (nil? v)
                                         (contains? native-arg-defaults k))
                                  (get native-arg-defaults k)
                                  v)])
                           args))]
    (ensure-native-sc-args-valid! args)
    args))

;;TODO ensure bitsets use the correct sizes based on user options
(defn sc-arg-default
  "Return the default value for the sc arg"
  [arg-name]
  (-> arg-name SC-ARG-INFO :default))

(ns dev.core
  (:require [tech.jna :as jna]
            [clj-native.structs :refer [make-struct-constructors parse-structs byref]])
  (:import
   [overtone.sc
    Types$WorldOptions$ByReference
    Types$World$ByReference
    Types$SndBuf$ByReference]
   ;; [com.sun.jna Native Structure Structure$ByReference]
   ))

(set! *warn-on-reflection* true)

;; :import [tech.svm Types$SVMNode$ByReference Types$SVMProblem$ByReference
;;             Types$SVMParameter$ByReference
;;             Types$SVMModel$ByReference
;;             Types$SVMModel
;;             Types$SVMNode
;;             Types$PrintString]

;; (declare world-options)
;; (gen-class
 ;; :name WorldOptions
 ;; :extends Structure
 ;; :implements [Structure$ByReference]
 ;; :main false
 ;; :constructors
 ;; {[String
 ;;   Integer
 ;;   Integer
 ;;   Integer
 ;;   Integer
 ;;   Integer
 ;;   Integer
 ;;   Integer
 ;;   Integer
 ;;   Integer
 ;;   Integer
 ;;   Integer
 ;;   Integer
 ;;   Float
 ;;   Boolean
 ;;   Boolean] []}
 ;; :exposes {mPassword {:get }}
 ;; :methods [[mPassword [] String]
 ;;           [mNumBuffers [] ]]
 ;; )

(System/setProperty "jna.library.path" "native/linux/x86_64")

#_(def scsynth-ptr* (jna/load-library "scsynth"))
#_(def user-types (atom {}))
#_(defonce __scstructs__
  (parse-structs
   '[[world-options
      :mPassword constchar*
      :mNumBuffers i32
      :mMaxLogins i32
      :mMaxNodes i32
      :mMaxGraphDefs i32
      :mMaxWireBufs i32
      :mNumAudioBusChannels i32
      :mNumInputBusChannels i32
      :mNumOutputBusChannels i32
      :mNumControlBusChannels i32
      :mBufLength i32
      :mRealTimeMemorySize i32
      :mNumSharedControls i32
      :mSharedControls float*
      :mRealTime byte
      :mMemoryLocking byte
      :mNonRealTimeCmdFilename constchar*
      :mNonRealTimeInputFilename constchar*
      :mNonRealTimeOutputFilename constchar*
      :mNonRealTimeOutputHeaderFormat constchar*
      :mNonRealTimeOutputSampleFormat constchar*
      :mPreferredSampleRate i32
      :mNumRGens i32
      :mPreferredHardwareBufferFrameSize i32
      :mLoadGraphDefs i32
      :mInputStreamsEnabled constchar*
      :mOutputStreamsEnabled constchar*
      :mInDeviceName constchar*
      :mVerbosity i32
      :mRendezvous byte
      :mUGensPluginPath constchar*
      :mOutDeviceName constchar*
      :mRestrictedPath constchar*
      :mSharedMemoryID i32]]
   user-types))

#_(def WorldOptions (eval (make-struct-constructors nil (first __scstructs__))))
#_(def WorldOptionsClassName (:classname (first __scstructs__)))

;; (def world-options
;;   (reify Structure$ByReference
;;     (mPassword [_] )))

;; (deftype WorldOptions [world-options]
;;   Structure$ByReference
;;   (mPassword [this] (.mPassword world-options)))

;; (extend-protocol PToPtr
;;   Pointer
;;   (is-jna-ptr-convertible? [item] true)
;;   (->ptr-backing-store [item] item)
;;   PointerByReference
;;   (is-jna-ptr-convertible? [item] true)
;;   (->ptr-backing-store [item] (.getValue ^PointerByReference item))
;;   Structure
;;   (is-jna-ptr-convertible? [item] true)
;;   (->ptr-backing-store [item] (.getPointer item)))

#_(jna/def-jna-fn "c" World_Cleanup
  "Set byte memory to a value"
  com.sun.jna.Pointer
  [world-options WorldOptionsClassName])

;; 000000000005dc20 T World_CopySndBuf
;; 0000000000063c30 T World_New
;; 0000000000063160 T World_NonRealTimeSynthesis
;; 0000000000020b80 T World_OpenTCP
;; 0000000000021e00 T World_OpenUDP
;; 0000000000020b70 T World_SendPacket
;; 0000000000020a90 T World_SendPacketWithContext
;; 0000000000063ba0 T World_WaitForQuit


;; (jna/def-jna-fn "c" World_CopySndBuf
;;   "Set byte memory to a value"
;;   com.sun.jna.Pointer
;;   [world-options com.sun.jna.Pointer])

(def non-modifiable-native-scsynth-options
  {:mBufLength                        64
   :mNumSharedControls                0
   :mSharedControls                   nil
   :mMemoryLocking                    0
   :mPreferredSampleRate              0
   :mPreferredHardwareBufferFrameSize 0
   :mSharedMemoryID                   0
   ;; ugens-plugins-path needs to be seperated from libscsynth paths
   :mUGensPluginPath (str (System/getProperty "jna.library.path") "/plugins")})

(def SC-ARG-INFO
  "Default arguments for starting up a SuperCollider process. Does not
  include -u or -t which should be determined by analysing :port
  and :udp?."
  {:port                     {:default 57711             :desc "Port number"}
   :udp?                     {:default 1                 :desc "1 means use UDP, 0 means use TCP"}
   :max-control-bus          {:default 4096   :flag "-c" :desc "Number of control bus channels"}
   :max-audio-bus            {:default 512    :flag "-a" :desc "Number of audio bus channels"}
   :max-input-bus            {:default 8      :flag "-i" :desc "Number of input bus channels"}
   :max-output-bus           {:default 8      :flag "-o" :desc "Number of output bus channels"}
   :block-size               {:default 64     :flag "-z" :desc "Block size"}
   :hw-buffer-size           {:default nil    :flag "-Z" :desc "Hardware buffer size"}
   :hw-sample-rate           {:default nil    :flag "-S" :desc "Hardware sample rate"}
   :max-buffers              {:default 1024   :flag "-b" :desc "Number of sample buffers"}
   :max-nodes                {:default 1024   :flag "-n" :desc "Max number of executing nodes allowed in the server"}
   :max-sdefs                {:default 1024   :flag "-d" :desc "Max number of synthdefs allowed"}
   :rt-mem-size              {:default 8192   :flag "-m" :desc "Real time memory size"}
   :max-w-buffers            {:default 64     :flag "-w" :desc "Number of wire buffers"}
   :num-rand-seeds           {:default 64     :flag "-r" :desc "Number of random seeds"}
   :load-sdefs?              {:default 0      :flag "-D" :desc "Load synthdefs on boot? 0 or 1"}
   :rendezvous?              {:default 0      :flag "-R" :desc "Publish to rendezvous? 0 or 1"}
   :max-logins               {:default 64     :flag "-l" :desc "Maximum number of named return addresses stored - also maximum number of TCP connections accepted."}
   :pwd                      {:default nil    :flag "-p" :desc "When using TCP, the session password must be the first command sent."}
   :realtime?                {:default 1                 :desc "Run in realtime mode? If 0 then the other nrt flags must be set"}
   :nrt-cmd-filename         {:default nil               :desc "Command filename for non-realtime mode"}
   :nrt-input-filename       {:default nil               :desc "Input filename for non-realtime mode"}
   :nrt-output-filename      {:default nil               :desc "Output filename for non-realtime mode"}
   :nrt-output-header-format {:default nil               :desc "Header format for non-realtime mode"}
   :nrt-output-sample-format {:default nil               :desc "Sample format for non-realtime mode"}
   :in-streams               {:default nil    :flag "-I" :desc "Input streams enabled"}
   :out-streams              {:default nil    :flag "-O" :desc "Output streams enabled"}
   :hw-device-name           {:default nil    :flag "-H" :desc "Hardware device name"}
   :verbosity                {:default 0      :flag "-v" :desc "Verbosity mode. 0 is normal behaviour, -1 suppress information messages, -2 suppresses informational and many error messages"}
   :ugens-paths              {:default nil    :flag "-U" :desc "A list of paths of ugen directories. If specified, the standard paths are NOT searched for plugins."}
   :restricted-path          {:default nil    :flag "-P" :desc "Prevents file-accesing OSC commands from accessing files outside the specified path."}})

(defmacro maybe-set! [ptr val]
  `(when ~val (set! ~ptr ~val)))

(defn set-world-options!
  [^Types$WorldOptions$ByReference ptr option-map]
  (maybe-set! (.mPassword ptr) (:pwd option-map))
  (maybe-set! (.mNumBuffers ptr) (:max-buffers option-map))
  (maybe-set! (.mMaxLogins ptr) (:max-logins option-map))
  (maybe-set! (.mMaxNodes ptr) (:max-nodes option-map))
  (maybe-set! (.mMaxGraphDefs ptr) (:max-sdefs option-map))
  (maybe-set! (.mMaxWireBufs ptr)                      (:max-w-buffers option-map))
  (maybe-set! (.mNumAudioBusChannels ptr)              (:max-audio-bus option-map))
  (maybe-set! (.mNumInputBusChannels ptr)              (:max-input-bus option-map))
  (maybe-set! (.mNumOutputBusChannels ptr)             (:max-output-bus option-map))
  (maybe-set! (.mNumControlBusChannels ptr)            (:max-control-bus option-map))
  (maybe-set! (.mBufLength ptr)                        (:block-size option-map))
  (maybe-set! (.mRealTimeMemorySize ptr)               (:rt-mem-size option-map))
  (maybe-set! (.mNumSharedControls ptr)                (:mNumSharedControls option-map))
  (maybe-set! (.mSharedControls ptr)                   (:mSharedControls option-map))
  (maybe-set! (.mRealTime ptr)                         (:realtime? option-map))
  (maybe-set! (.mMemoryLocking ptr)                    (:mMemoryLocking option-map))
  (maybe-set! (.mNonRealTimeCmdFilename ptr)           (:mNonRealTimeCmdFilename option-map))
  (maybe-set! (.mNonRealTimeInputFilename ptr)         (:mNonRealTimeInputFilename option-map))
  (maybe-set! (.mNonRealTimeOutputFilename ptr)        (:mNonRealTimeOutputFilename option-map))
  (maybe-set! (.mNonRealTimeOutputHeaderFormat ptr)    (:mNonRealTimeOutputHeaderFormat option-map))
  (maybe-set! (.mNonRealTimeOutputSampleFormat ptr)    (:mNonRealTimeOutputSampleFormat option-map))
  (maybe-set! (.mPreferredSampleRate ptr)              (:mPreferredSampleRate option-map))
  (maybe-set! (.mNumRGens ptr)                         (:num-rand-seeds option-map))
  (maybe-set! (.mPreferredHardwareBufferFrameSize ptr) (:mPreferredHardwareBufferFrameSize option-map))
  (maybe-set! (.mLoadGraphDefs ptr)                    (:load-sdefs? option-map))
  (maybe-set! (.mInputStreamsEnabled ptr)              (:in-streams option-map))
  (maybe-set! (.mOutputStreamsEnabled ptr)             (:out-streams option-map))
  (maybe-set! (.mInDeviceName ptr)                     (:hw-device-name option-map))
  (maybe-set! (.mVerbosity ptr)                        (:verbosity option-map))
  (maybe-set! (.mRendezvous ptr)                       (:rendezvous? option-map))
  (maybe-set! (.mUGensPluginPath ptr)                  (:mUGensPluginPath option-map))
  (maybe-set! (.mOutDeviceName ptr)                    (:hw-out-device-name option-map))
  (maybe-set! (.mRestrictedPath ptr)                   (:mRestrictedPath option-map))
  (maybe-set! (.mSharedMemoryID ptr)                   (:mSharedMemoryID option-map)))

(jna/def-jna-fn "scsynth" World_CopySndBuf
  "Direct and Native access to SC's buffers"
  com.sun.jna.Pointer
  [world (partial jna/ensure-type Types$WorldOptions$ByReference)
   index int ;;(partial jna/ensure-type Integer/TYPE)
   sound-buffer (partial jna/ensure-type Types$SndBuf$ByReference)
   only-if-changed? (partial jna/ensure-type Byte/TYPE)
   did-change? (partial jna/ensure-type ByteBuffer)
   ])

(jna/def-jna-fn "scsynth" World_New
  "Instantiates SC"
  Types$World$ByReference
  [world-options (partial jna/ensure-type Types$WorldOptions$ByReference)])

(jna/def-jna-fn "scsynth" World_OpenTCP
  "Makes direct-native Supercollider to open a TCP port"
  com.sun.jna.Pointer
  [world (partial jna/ensure-type Types$World$ByReference)
   bind-to String
   port int
   max-connections int
   backlog int])

(jna/def-jna-fn "scsynth" World_OpenUDP
  "Makes direct-native Supercollider to open a UDP port"
  com.sun.jna.Pointer
  [world (partial jna/ensure-type Types$World$ByReference)
   bind-to String
   port int
   max-connections int])

(defn -main []
  (let [world-options (Types$WorldOptions$ByReference.)
        defaults (merge
                  (reduce-kv #(if (and (:default %3)
                                       (if (string? (:default %3))
                                         (not-empty (:default %3))
                                         true))
                                (assoc %1 %2  (:default %3))
                                %1) {} SC-ARG-INFO)
                  non-modifiable-native-scsynth-options)
        _ (set-world-options! world-options defaults)
        world (World_New world-options)]
      (World_OpenUDP world "127.0.0.1" 9010 256)
    )
  )

;; (pr scsynth-ptr*)

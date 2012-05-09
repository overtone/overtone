(ns overtone.sc.machinery.server.native
  (:import [java.nio ByteOrder ByteBuffer])
  (:use [overtone.helpers.file :only (get-current-directory)]
        [clj-native.direct :only [defclib loadlib]]
        [clj-native.structs :only [byref]]
        [clj-native.callbacks :only [callback]]))

(def LIBSCSYNTH-PATH "native/macosx/x86_64")
(System/setProperty "jna.library.path"
                    (str (get-current-directory) "/" LIBSCSYNTH-PATH))

(defclib
  lib-scsynth
  (:libname "scsynth")
  (:structs
    (rate
      :sample-rate double
      :buf-rate    double
      :radians-per-sample double)

      ; supercollider/include/server/SC_WorldOptions.h
      (world-options
        :mPassword                          constchar*
        :mNumBuffers                        i32
        :mMaxLogins                         i32
        :mMaxNodes                          i32
        :mMaxGraphDefs                      i32
        :mMaxWireBufs                       i32
        :mNumAudioBusChannels               i32
        :mNumInputBusChannels               i32
        :mNumOutputBusChannels              i32
        :mNumControlBusChannels             i32
        :mBufLength                         i32
        :mRealTimeMemorySize                i32
        :mNumSharedControls                 i32
        :mSharedControls                    float
        :mRealTime                          bool
        :mMemoryLocking                     bool
        :mNonRealTimeCmdFilename            constchar*
        :mNonRealTimeInputFilename          constchar*
        :mNonRealTimeOutputFilename         constchar*
        :mNonRealTimeOutputHeaderFormat     constchar*
        :mNonRealTimeOutputSampleFormat     constchar*
        :mPreferredSampleRate               i32
        :mNumRGens                          i32
        :mPreferredHardwareBufferFrameSize  i32
        :mLoadGraphDefs                     i32
        :mInputStreamsEnabled               constchar*
        :mOutputStreamsEnabled              constchar*
        :mInDeviceName                      constchar*
        :mVerbosity                         i32
        :mRendezvous                        bool
        :mUGensPluginPath                   constchar*
        :mOutDeviceName                     constchar*
        :mRestrictedPath                    constchar*
        :mSharedMemoryID                    i32)

    ; supercollider/include/plugin_interface/SC_SndBuf.h
    (sound-buffer
      :samplerate double
      :sampledur  double
      :data       float*
      :channels   i32
      :samples    i32
      :frames     i32
      :mask       i32
      :mask1      i32
      :coord      i32
      :sndfile    void*)

    ; supercollider/include/plugin_interface/SC_World.h
    (world
      :hidden-world void*
      :interface-table void*
      :sample-rate double
      :buf-length  i32
      :buf-counter i32
      :num-audio-bus-channels   i32
      :num-control-bus-channels i32
      :num-inputs               i32
      :num-outputs              i32
      :audio-busses             float*
      :control-busses           float*
      :audio-bus-touched        i32*
      :control-bus-touched      i32*
      :num-snd-bufs             i32
      :snd-bufs                 sound-buffer*
      :snd-bufs-non-realtime    sound-buffer*
      :snd-buf-updates          void*
      :top-group                void*
      :full-rate                rate
      :buf-rate                 rate
      :num-rgens                i32
      :rgen                     void*
      :num-units                i32
      :num-graphs               i32
      :num-groups               i32
      :sample-offset            i32
      :nrt-lock                 void*
      :num-shared-controls      i32
      :shared-controls          float*
      :real-time?               bool
      :running?                 bool
      :dump-osc                 i32
      :driver-lock              void*
      :subsample-offset         float
      :verbosity                i32
      :error-notification       i32
      :local-error-notificaiton i32
      :rendezvous?              bool
      :restricted-path          constchar*)
    )

  (:callbacks

    ; supercollider/include/common/SC_Reply.h
   (reply-callback [void* char* i32] void))

  ; TODO: void* here is actually world*
  (:functions

    ; supercollider/include/server/SC_WorldOptions.h
    (world-new World_New [world-options*] void*)
    (world-run World_WaitForQuit [void*])
    (world-cleanup World_Cleanup [void*])

    (world-open-udp-port World_OpenUDP [void* i32] i32)
    (world-open-tcp-port World_OpenTCP [void* i32] i32)
    (world-send-packet World_SendPacket [void* i32 byte* reply-callback] bool)
    (world-copy-sound-buffer World_CopySndBuf [void* i32 sound-buffer* bool bool] i32)))

(loadlib lib-scsynth)

(def default-options
  {:mPassword                          ""
   :mNumBuffers                        1024
   :mMaxLogins                         64
   :mMaxNodes                          1024
   :mMaxGraphDefs                      1024
   :mMaxWireBufs                       64
   :mNumAudioBusChannels               128
   :mNumInputBusChannels               8
   :mNumOutputBusChannels              8
   :mNumControlBusChannels             4096
   :mBufLength                         64
   :mRealTimeMemorySize                8192
   :mNumSharedControls                 0
   :mSharedControls                    0
   :mRealTime                          true
   :mMemoryLocking                     false
   :mNonRealTimeCmdFilename            ""
   :mNonRealTimeInputFilename          ""
   :mNonRealTimeOutputFilename         ""
   :mNonRealTimeOutputHeaderFormat     ""
   :mNonRealTimeOutputSampleFormat     ""
   :mPreferredSampleRate               0
   :mNumRGens                          64
   :mPreferredHardwareBufferFrameSize  0
   :mLoadGraphDefs                     1
   :mInputStreamsEnabled               ""
   :mOutputStreamsEnabled              ""
   :mInDeviceName                      ""
   :mVerbosity                         0
   :mRendezvous                        true
   :mUGensPluginPath                   ""
   :mOutDeviceName                     ""
   :mRestrictedPath                    ""
   :mSharedMemoryID                    0})

; Most likely there is a better way to do this...
(defn set-world-options!
  [ptr option-map]
   (set! (.mPassword ptr) (:mPassword option-map))
   (set! (.mNumBuffers ptr) (:mNumBuffers option-map))
   (set! (.mMaxLogins ptr) (:mMaxLogins option-map))
   (set! (.mMaxNodes ptr) (:mMaxNodes option-map))
   (set! (.mMaxGraphDefs ptr) (:mMaxGraphDefs option-map))
   (set! (.mMaxWireBufs ptr) (:mMaxWireBufs option-map))
   (set! (.mNumAudioBusChannels ptr) (:mNumAudioBusChannels option-map))
   (set! (.mNumInputBusChannels ptr) (:mNumInputBusChannels option-map))
   (set! (.mNumOutputBusChannels ptr) (:mNumOutputBusChannels option-map))
   (set! (.mNumControlBusChannels ptr) (:mNumControlBusChannels option-map))
   (set! (.mBufLength ptr) (:mBufLength option-map))
   (set! (.mRealTimeMemorySize ptr) (:mRealTimeMemorySize option-map))
   (set! (.mNumSharedControls ptr) (:mNumSharedControls option-map))
   (set! (.mSharedControls ptr) (:mSharedControls option-map))
   (set! (.mRealTime ptr) (:mRealTime option-map))
   (set! (.mMemoryLocking ptr) (:mMemoryLocking option-map))
   (set! (.mNonRealTimeCmdFilename ptr) (:mNonRealTimeCmdFilename option-map))
   (set! (.mNonRealTimeInputFilename ptr) (:mNonRealTimeInputFilename option-map))
   (set! (.mNonRealTimeOutputFilename ptr) (:mNonRealTimeOutputFilename option-map))
   (set! (.mNonRealTimeOutputHeaderFormat ptr) (:mNonRealTimeOutputHeaderFormat option-map))
   (set! (.mNonRealTimeOutputSampleFormat ptr) (:mNonRealTimeOutputSampleFormat option-map))
   (set! (.mPreferredSampleRate ptr) (:mPreferredSampleRate option-map))
   (set! (.mNumRGens ptr) (:mNumRGens option-map))
   (set! (.mPreferredHardwareBufferFrameSize ptr) (:mPreferredHardwareBufferFrameSize option-map))
   (set! (.mLoadGraphDefs ptr) (:mLoadGraphDefs option-map))
   (set! (.mInputStreamsEnabled ptr) (:mInputStreamsEnabled option-map))
   (set! (.mOutputStreamsEnabled ptr) (:mOutputStreamsEnabled option-map))
   (set! (.mInDeviceName ptr) (:mInDeviceName option-map))
   (set! (.mVerbosity ptr) (:mVerbosity option-map))
   (set! (.mRendezvous ptr) (:mRendezvous option-map))
   (set! (.mUGensPluginPath ptr) (:mUGensPluginPath option-map))
   (set! (.mOutDeviceName ptr) (:mOutDeviceName option-map))
   (set! (.mRestrictedPath ptr) (:mRestrictedPath option-map))
   (set! (.mSharedMemoryID ptr) (:mSharedMemoryID option-map)))

(defn scsynth
  "Load libscsynth and start the synthesis server with the given options.  Returns
  the World pointer."
  ([recv-fn] (scsynth default-options recv-fn))
  ([options-map recv-fn]
   (let [options (byref world-options)
         cb      (callback reply-callback
                           (fn [addr msg-buf msg-size]
                             (recv-fn (.order msg-buf ByteOrder/BIG_ENDIAN) msg-size)))]
     (set-world-options! options options-map)
     {:world (world-new options)
      :callback cb})))

(defn scsynth-send
  [{:keys [world callback] :as sc} ^ByteBuffer buf]
  (world-send-packet world (.limit buf) buf callback))

(defn scsynth-run
  "Starts the synthesis server main loop, and does not return until the /quit message
  is received."
  [sc]
  (world-run (:world sc)))

(ns overtone.core.ugen.info)

(def specs
  [
   {:name "SampleRate" 
    :rates #{:ir}
    :doc "returns the current sample rate"}

   {:name "SampleDur" 
    :rates #{:ir}
    :doc "returns the current sample duration of the server in seconds"}

   {:name "RadiansPerSample" 
    :rates #{:ir}
    :doc ""}

   {:name "ControlRate" 
    :rates #{:ir}
    :doc "returns the current control rate of the server"}

   {:name "ControlDur" 
    :rates #{:ir}
    :doc "returns the current block duration of the server in seconds"}

   {:name "SubsampleOffset" 
    :rates #{:ir}
    :doc "offset from synth start within one sample"}

   {:name "NumOutputBuses" 
    :rates #{:ir}
    :doc "returns the number of output buses allocated on the server"}

   {:name "NumInputBuses" 
    :rates #{:ir}
    :doc "returns the number of output buses allocated on the server"}

   {:name "NumAudioBuses" 
    :rates #{:ir}
    :doc "returns the number of audio buses allocated on the server"}

   {:name "NumControlBuses" 
    :rates #{:ir}
    :doc "returns the number of control buses allocated on the server"}

   {:name "NumBuffers" 
    :rates #{:ir}
    :doc "returns the number of buffers allocated on the server"}

   {:name "NumRunningSynths" 
    :rates #{:ir :kr}
    :doc "returns the number of currently running synths"}

   {:name "BufSampleRate" 
    :args [{:name "buf"}]
    :rates #{:ir}
    :doc "returns the buffers current sample rate"}

   {:name "BufRateScale"
    :args [{:name "buf"}]
    :rates #{:kr :ir}
    :doc "returns a ratio by which the playback of a buffer is to be scaled"}

   {:name "BufFrames"
    :args [{:name "buf"}]
    :rates #{:kr :ir}
    :doc "returns the current number of allocated frames"}

   {:name "BufSamples" 
    :args [{:name "buf"}]
    :rates #{:ir}
    :doc "current number of samples allocated in the buffer"}

   {:name "BufDur" 
    :args [{:name "buf"}]
    :rates #{:ir}
    :doc "returns the current duration of a buffer"}

   {:name "BufChannels" 
    :args [{:name "buf"}]
    :rates #{:ir}
    :doc "current number of channels of soundfile in buffer"}

   {:name "CheckBadValues" 
    :args [{:name "val"}]
    :rates #{:ir}
    :doc "test for infinity, not-a-number, and denormals"}
])

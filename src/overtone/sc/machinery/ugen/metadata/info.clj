(ns overtone.sc.machinery.ugen.metadata.info
  (:use [overtone.sc.machinery.ugen common check]))

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
    :doc "returns the current control rate block duration of the server
          in seconds"}


   {:name "SubsampleOffset"
    :rates #{:ir}
    :doc "offset from synth start within one sample"}


   {:name "NumOutputBuses"
    :rates #{:ir}
    :doc "returns the number of output buses allocated on the server.
          This is the number of hardware outputs provided by the host
          machine such as left and right speakers." }


   {:name "NumInputBuses"
    :rates #{:ir}
    :doc "returns the number of input buses allocated on the
          server. This is the number of hardware inputs provided by the
          host machine such as a mic." }


   {:name "NumAudioBuses"
    :rates #{:ir}
    :doc "returns the number of audio buses allocated on the server."}


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
    :args [{:name "buf"
            :default 0
            :doc "a buffer"}]

    :rates #{:kr :ir}
    :check (nth-input-buffer? 0)
    :doc "returns the buffers current sample rate"}


   {:name "BufRateScale"
    :args [{:name "buf"
            :default 0
            :doc "a buffer"}]

    :rates #{:kr :ir}
    :check (nth-input-buffer? 0)
    :doc "returns a ratio by which the playback of a buffer is to be
          scaled"}


   {:name "BufFrames"
    :args [{:name "buf"
            :default 0
            :doc "a buffer"}]

    :rates #{:kr :ir}
    :check (nth-input-buffer? 0)
    :doc "returns the current number of allocated frames i.e. the size
          of the buffer. This is the equivalent of Clojure's count on a
          seq." }


   {:name "BufSamples"
    :args [{:name "buf"
            :default 0
            :doc "a buffer"}]

    :rates #{:kr :ir}
    :check (nth-input-buffer? 0)
    :doc "current number of samples allocated in the buffer"}


   {:name "BufDur"
    :args [{:name "buf"
            :default 0
            :doc "a buffer"}]

    :rates #{:kr :ir}
    :check (nth-input-buffer? 0)
    :doc "returns the current duration of a buffer in seconds."}


   {:name "BufChannels"
    :args [{:name "buf"
            :default 0
            :doc "a buffer"}]

    :rates #{:kr :ir}
    :check (nth-input-buffer? 0)
    :doc "current number of channels of soundfile in buffer"}


   {:name "CheckBadValues"
    :args [{:name "in"
            :doc "the UGen whose output is to be tested"}

           {:name "id"
            :default 0
            :doc "an id number to identify this UGen." }

           {:name "post"
            :default 2
            :doc "One of three post modes: 0 = no posting; 1 = post a
                  line for every bad value; 2 = post a line only when
                  the floating-point classification changes (e.g.,
                  normal -> NaN and vice versa)"}]

    :rates #{:kr :ir}
    :doc "test for infinity, not-a-number, and denormals.  If one of
          these is found, it posts a warning. Its output is as follows:
          0 = a normal float, 1 = NaN, 2 = infinity, and 3 = a
          denormal." }


   {:name "Poll"
    :args [{:name "trig"
            :default 0.0
            :doc "a non-positive to positive transition telling Poll to
                  return a value"}

           {:name "in"
            :default 0.0
            :doc "the signal you want to poll"}

           {:name "label"
            :default "polled-val"
            :doc "a string or symbol to be printed with the polled
                  value"
            :mode :append-string}

           {:name "trig-id"
            :default -1
            :doc "if greater than 0, a '/tr' message is sent back to the
                  client (similar to SendTrig)"}]

    :internal-name true
    :rates #{:ar :kr}
    :doc "This ugen has been internalised for scserver
          compatibility. Please use the poll cgen instead."}])

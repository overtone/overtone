(ns overtone.sc.machinery.ugen.metadata.io
  (:use [overtone.sc.machinery.ugen common]))

(def specs
     [
      {:name "DiskOut",
       :args [{:name "bufnum" :doc "the number of the buffer to write to (prepared with /b-write)"}
              {:name "channelsArray" :mode :append-sequence :doc "the Array of channels to write to the file."}],
       :rates #{:ar},
       :check (all-but-first-input-ar "channelsArray must all be audio rate")
       :doc "stream audio out to disk file

The output of DiskOut is the number of frames written to disk.

Note that the number of channels in the buffer and the channelsArray must be the same, otherwise DiskOut will fail silently (and not write anything to your file)."}

      {:name "DiskIn",
       :args [{:name "numChannels" :mode :num-outs :doc "Number of channels in the incoming audio."}
              {:name "bufnum" :doc "id of buffer"}
              {:name "loop", :default 0 :doc "Soundfile will loop if 1 otherwise not."}],
       :rates #{:ar}
       :doc "stream audio in from disk file

Continuously play a longer soundfile from disk.  This requires a buffer to be preloaded with one buffer size of sound. If loop is set to 1, the soundfile will loop."}

      {:name "VDiskIn",
       :args [{:name "numChannels" :mode :num-outs :doc "Number of channels in the audio"}
              {:name "bufnum" :doc "id of buffer"}
              {:name "rate", :default 1 :doc "controls the rate of playback. Values below 4 are probably fine, but the higher the value, the more disk activity there is, and the more likelihood there will be a problem."}
              {:name "loop", :default 0 :doc "Soundfile will loop if 1 otherwise not."}
              {:name "sendID", :default 0 :doc "send an osc message with this id and the file position each time the buffer is reloaded: ['/diskin', nodeID, sendID, frame] "}],
       :rates #{:ar}
       :doc "stream in audio from a file (with variable rate)

Continuously play a longer soundfile from disk.  This requires a buffer to be preloaded with one buffer size of sound."}

      {:name "In",
       :args [{:name "bus", :default 0 :doc "the index of the bus to read in from"}
              {:name "num-channels", :mode :num-outs :default 1 :doc "the number of channels (i.e. adjacent buses) to read in. The default is 1. You cannot modulate this number by assigning it to an argument in a SynthDef."}]
       :rates #{:ar :kr}
       :internal-name true
       :doc "Internalised see the in cgen for public version of this gen."}

      {:name "LocalIn",
       :args [{:name "num-channels", :mode :num-outs :default 1 :doc "the number of channels (i.e. adjacent buses) to read in. The default is 1. You cannot modulate this number by assigning it to an argument in a SynthDef."}]
       :rates #{:ar :kr}
       :doc "defines buses that are local to the enclosing synth. These are like the global buses, but are more convenient if you want to implement a self contained effect that uses a feedback processing loop.

There can only be one audio rate and one control rate local-in per SynthDef.

The audio can be written to the bus using local-out."}

      {:name "LagIn",
       :args [{:name "bus", :default 0}
              {:name "num-channels", :mode :num-outs :default 1}
              {:name "lag", :default 0.1}],
       :rates #{:kr}
       :internal-name true
       :doc "Internalised. See the lag-in cgen."}

      {:name "InFeedback",
       :args [{:name "bus", :default 0 :doc "the index of the bus to read in from."}
              {:name "num-channels", :mode :num-outs :default 1 :doc "the number of channels (i.e. adjacent buses) to read in. The default is 1. You cannot modulate this number by assigning it to an argument in a SynthDef."}],
       :rates #{:ar}
       :internal-name true
       :doc "Internalised. See the in-feedback cgen."}

      {:name "InTrig",
       :args [{:name "bus", :default 0 :doc "the index of the bus to read in from."}
              {:name "numChannels", :mode :num-outs :default 1 :doc "the number of channels (i.e. adjacent buses) to read in. The default is 1. You cannot modulate this number by assigning it to an argument in a SynthDef."}],
       :rates #{:kr}
       :doc "generates a trigger any time the bus is set

Any time the bus is 'touched' ie. has its value set (using \"/c_set\" etc.), a single impulse trigger will be generated. Its amplitude is the value that the bus was set to."}

      {:name "SharedIn",
       :args [{:name "bus", :default 0 :doc "the index of the shared control bus to read from"}
              {:name "numChannels", :mode :num-outs :default 1 :doc "the number of channels (i.e. adjacent buses) to read in. The default is 1. You cannot modulate this number by assigning it to an argument in a SynthDef."}],
       :rates #{:kr}
       :doc "read from a shared control bus (internal dsp only)

Reads from a control bus shared between the internal server and the SC client. Control rate only. Writing to a shared control bus from the client is synchronous. When not using the internal server use node arguments or the set method of Bus (or /c_set in messaging style). "}

      {:name "Out",
       :args [{:name "bus" :doc "the index of the buss to write to. The lowest index numbers are written to the audio hardware."}
              {:name "signals" :mode :append-sequence :doc "a list of signals or single output to write out. You cannot change the size of this once a synth has been defined."}],
       :num-outs 0
       :rates #{:ar :kr}
       :auto-rate true
       :check (when-ar
               (all-but-first-input-ar "signals must all be audio rate"))
       :internal-name true
       :doc ""}

      {:name "ReplaceOut", :extends "Out"
       :doc "write signal to a bus, replacing the contents rather than adding to it."
       :internal-name true
       }

      {:name "OffsetOut" :extends "Out"
       :rates #{:ar}
       :check (all-but-first-input-ar "signals-array must all be audio rate")
       :internal-name true
       :doc "Internalised. See offset-out cgen."}

      {:name "LocalOut",
       :args [{:name "channelsArray" :mode :append-sequence :doc "an Array of channels or single output to write out. You cannot change the size of this once a SynthDef has been built."}],
       :num-outs 0
       :check (when-ar (all-inputs-ar "all channels must be audio rate"))
       :doc "write to buses local to a synth

local-out writes to buses that are local to the enclosing synth. The buses should have been defined by a local-in ugen. The channelsArray must be the same number of channels as were declared in the LocalIn. These are like the global buses, but are more convenient if you want to implement a self contained effect that uses a feedback processing loop."}

      {:name "XOut",
       :args [{:name "bus" :doc "the index, or array of indexes, of busses to write to. The lowest index numbers are written to the audio hardware."}
              {:name "xfade" :doc "crossfade level."}
              {:name "channels-array" :mode :append-sequence :doc "an Array of channels or single output to write out. You cannot change the size of this once a SynthDef has been built."}],
       :num-outs 0
       :check (when-ar (after-n-inputs-rest-ar 2 "all channels must be audio rate"))
       :internal-name true
       :doc "Internalised. See x-out cgen."}

      {:name "SharedOut",
       :args [{:name "bus" :doc "the index of the shared control bus to read from"}
              {:name "channelsArray" :mode :append-sequence :doc "an Array of channels or single output to write out. You cannot change the size of this once a SynthDef has been built."}],
       :rates #{:kr},
       :num-outs 0
       :doe "Reads from a control bus shared between the internal server and the SC client. Control rate only. Reading from a shared control bus on the client is synchronous. "}])

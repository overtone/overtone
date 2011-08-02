(ns overtone.sc.ugen.io
  (:use [overtone.sc.ugen common constants]))

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
              {:name "numChannels", :mode :num-outs :default 1 :doc "the number of channels (i.e. adjacent buses) to read in. The default is 1. You cannot modulate this number by assigning it to an argument in a SynthDef."}]
       :rates #{:ar :kr}
       :doc "read from a bus


in:kr is functionally similar to in-feedback. That is it reads all data on the bus whether it is from the current cycle or not. This allows for it to receive data from later in the node order. in:ar reads only data from the current cycle, and will zero data from earlier cycles (for use within that synth; the data remains on the bus). Because of this and the fact that the various out ugens mix their output with data from the current cycle but overwrite data from an earlier cycle it may be necessary to use a private control bus when this type of feedback is desired. There is an example below which demonstrates the problem."}

      {:name "LocalIn",
       :args [{:name "numChannels", :mode :num-outs :default 1 :doc "the number of channels (i.e. adjacent buses) to read in. The default is 1. You cannot modulate this number by assigning it to an argument in a SynthDef."}]
       :rates #{:ar :kr}
       :doc "defines buses that are local to the enclosing synth. These are like the global buses, but are more convenient if you want to implement a self contained effect that uses a feedback processing loop.

There can only be one audio rate and one control rate local-in per SynthDef.

The audio can be written to the bus using local-out."}

      {:name "LagIn",
       :args [{:name "bus", :default 0}
              {:name "numChannels", :mode :num-outs :default 1}
              {:name "lag", :default 0.1}],
       :rates #{:kr}}

      {:name "InFeedback",
       :args [{:name "bus", :default 0 :doc "the index of the bus to read in from."}
              {:name "numChannels", :mode :num-outs :default 1 :doc "the number of channels (i.e. adjacent buses) to read in. The default is 1. You cannot modulate this number by assigning it to an argument in a SynthDef."}],
       :rates #{:ar}
       :doc "read signal from a bus with a current or one cycle old timestamp

When the various output ugens (out, offsetOut, x-out) write data to a bus, they mix it with any data from the current cycle, but overwrite any data from the previous cycle. (replace-out overwrites all data regardless.) Thus depending on node order and what synths are writing to the bus, the data on a given bus may be from the current cycle or be one cycle old at the time of reading. in:ar checks the timestamp of any data it reads in and zeros any data from the previous cycle (for use within that node; the data remains on the bus). This is fine for audio data, as it avoids feedback, but for control data it is useful to be able to read data from any place in the node order. For this reason in:kr also reads data that is older than the current cycle.


In some cases we might also want to read audio from a node later in the current node order. This is the purpose of InFeedback. The delay introduced by this is one block size, which equals about 0.0014 sec at the default block size and sample rate. (See the resonator example below to see the implications of this.)


The variably mixing and overwriting behaviour of the output ugens can make order of execution crucial. (No pun intended.) For example with a node order like the following the InFeedback ugen in Synth 2 will only receive data from Synth 1 (-> = write out; <- = read in):

Synth 1 -> busA					this synth overwrites the output of Synth3 before it reaches Synth 2

Synth 2 (with InFeedback) <- busA

Synth 3 -> busA

If Synth 1 were moved after Synth 2 then Synth 2's InFeedback would receive a mix of the output from Synth 1 and Synth 3. This would also be true if Synth 2 came after Synth1 and Synth 3. In both cases data from Synth 1 and Synth 3 would have the same time stamp (either current or from the previous cycle), so nothing would be overwritten.


Because of this it is often useful to allocate a separate bus for feedback. With the following arrangement Synth 2 will receive data from Synth3 regardless of Synth 1's position in the node order.


Synth 1 -> busA

Synth 2 (with InFeedback) <- busB

Synth 3 -> busB + busA

"}

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
       :args [{:name "bus" :doc "the index, or array of indexes, of busses to write to. The lowest index numbers are written to the audio hardware."}
              {:name "channelsArray" :mode :append-sequence :doc "an Array of channels or single output to write out. You cannot change the size of this once a SynthDef has been built."}],
       :num-outs 0
       :rates #{:ar :kr}
       :auto-rate true
       :check (when-ar
               (all-but-first-input-ar "channelsArray must all be audio rate"))
       :doc "write a signal to a bus, adding to any existing contents

N.B. Out is subject to control rate jitter. Where sample accurate output is needed, use OffsetOut.

When using an array of bus indexes, the channel array will just be copied to each bus index in the array. So (out:ar [bus1 bus2] channels-array) will be the same as (+ (out:ar bus1 channelsArray)  (out:ar bus2 channelsArray))."}

      {:name "ReplaceOut", :extends "Out"
       :doc "write signal to a bus, replacing the contents rather than adding to it."
       }

      {:name "OffsetOut" :extends "Out"
       :rates #{:ar}
       :check (all-but-first-input-ar "channelsArray must all be audio rate")
       :doc "write signal to a bus with sample accurate timing

Output signal to a bus,  the sample offset within the bus is kept exactly; i.e. if the synth is scheduled to be started part way through a control cycle, offset-out will maintain the correct offset by buffering the output and delaying it until the exact time that the synth was scheduled for.

This ugen is used where sample accurate output is needed."}

      {:name "LocalOut",
       :args [{:name "channelsArray" :mode :append-sequence :doc "an Array of channels or single output to write out. You cannot change the size of this once a SynthDef has been built."}],
       :num-outs 0
       :check (when-ar (all-inputs-ar "all channels must be audio rate"))
       :doc "write to buses local to a synth

local-out writes to buses that are local to the enclosing synth. The buses should have been defined by a local-in ugen. The channelsArray must be the same number of channels as were declared in the LocalIn. These are like the global buses, but are more convenient if you want to implement a self contained effect that uses a feedback processing loop."}

      {:name "XOut",
       :args [{:name "bus" :doc "the index, or array of indexes, of busses to write to. The lowest index numbers are written to the audio hardware."}
              {:name "xfade" :doc "crossfade level."}
              {:name "channelsArray" :mode :append-sequence :doc "an Array of channels or single output to write out. You cannot change the size of this once a SynthDef has been built."}],
       :num-outs 0
       :check (when-ar (after-n-inputs-rest-ar 2 "all channels must be audio rate"))
       :doc "write signal to a bus, crossfading with the existing content (adding with fader adjustment)

xfade is a level for the crossfade between what is on the bus and what you are sending.

The algorithm is equivalent to this:

bus_signal = (input_signal * xfade) + (bus_signal * (1 - xfade));"}

      {:name "SharedOut",
       :args [{:name "bus" :doc "the index of the shared control bus to read from"}
              {:name "channelsArray" :mode :append-sequence :doc "an Array of channels or single output to write out. You cannot change the size of this once a SynthDef has been built."}],
       :rates #{:kr},
       :num-outs 0
       :doe "Reads from a control bus shared between the internal server and the SC client. Control rate only. Reading from a shared control bus on the client is synchronous. "}])

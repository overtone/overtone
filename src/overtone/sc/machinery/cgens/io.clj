(ns overtone.sc.machinery.cgens.io
  (:use [overtone.sc.machinery defcgen ugens defaults]
        [overtone.util lib]))

(defcgen out
  "Write signal to a bus"
  [bus {:default 0 :doc "the index of the bus to write to. The lowest index numbers are written to the audio hardware."}
   signals {:doc "a list of signals or single output to write out. You cannot change the size of this once a synth has been defined."}]
  "write a signal to a bus, adding to any existing contents

N.B. Out is subject to control rate jitter. Where sample accurate output is needed, use OffsetOut.

When using an array of bus indexes, the channel array will just be copied to each bus index in the array. So (out:ar [bus1 bus2] channels-array) will be the same as (+ (out:ar bus1 channelsArray)  (out:ar bus2 channelsArray))."
  (:ar (internal:out:ar (+ (num-output-buses) (num-input-buses) bus) signals))
  (:kr (internal:out:kr bus signals)))

(defcgen in
  "Read signal from a bus"
  [bus {:default 0 :doc "the index of the bus to read in from"}
   num-channels {:default 1 :doc "the number of channels (i.e. adjacent buses) to read in. The default is 1. You cannot modulate this number by assigning it to an argument in a SynthDef."}]
  "in:kr is functionally similar to in-feedback. That is it reads all data on the bus whether it is from the current cycle or not. This allows for it to receive data from later in the node order. in:ar reads only data from the current cycle, and will zero data from earlier cycles (for use within that synth; the data remains on the bus). Because of this and the fact that the various out ugens mix their output with data from the current cycle but overwrite data from an earlier cycle it may be necessary to use a private control bus when this type of feedback is desired. There is an example below which demonstrates the problem."
  (:ar (internal:in:ar (+ (num-output-buses) (num-input-buses) bus) num-channels))
  (:kr (internal:in:kr bus num-channels)))


(defcgen sound-in
  "read audio from hardware inputs"
  [bus {:default 0 :doc "the channel (or array of channels) to read in. These start at 0, which will correspond to the first audio input." :modulatable false}]
  "Reads audio from the input of your computer or soundcard. It is a wrapper UGen based on In, which offsets the index such that 0 will always correspond to the first input regardless of the number of inputs present.

N.B. On Intel based Macs, reading the built-in microphone or input may require creating an aggregate device in AudioMIDI Setup."
  (:ar (cond
        (integer? bus) (in:ar (+ (num-output-buses:ir) bus) 1)
        (consecutive-ints? bus) (in:ar (+ (num-output-buses:ir) (first bus)) (count bus))
        :else (in:ar (+ (num-output-buses:ir) bus)))))

(defcgen lag-in
  ""
  [bus {:default 0 :doc "the index of the bus to read in from"}
   num-channels {:default 1 :doc "the number of channels (i.e. adjacent buses) to read in. Not modulatable."}
   lag {:default 0.1 :doc "the lag time"}]
  ""
  (:kr (internal:lag-in:kr bus num-channels lag)))

(defcgen in-feedback
  "read signal from a bus with a current or one cycle old timestamp"
  [bus {:default 0 :doc "the index of the bus to read in from"}
   num-channels {:default 1 :doc "the number of channels (i.e. adjacent buses) to read in. Not modulatable"}]
  "When the various output ugens (out, offsetOut, x-out) write data to a bus, they mix it with any data from the current cycle, but overwrite any data from the previous cycle. (replace-out overwrites all data regardless.) Thus depending on node order and what synths are writing to thep bus, the data on a given bus may be from the current cycle or be one cycle old at the time of reading. in:ar checks the timestamp of any data it reads in and zeros any data from the previous cycle (for use within that node; the data remains on the bus). This is fine for audio data, as it avoids feedback, but for control data it is useful to be able to read data from any place in the node order. For this reason in:kr also reads data that is older than the current cycle.


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

"
  (:ar (internal:in-feedback:ar (+ (num-output-buses) (num-input-buses) bus) num-channels)))

(defcgen replace-out
  "write signal to a bus, replacing contents rather than adding to it"
  [bus {:default 0 :doc "the index of the bus to write to. The lowest index numbers are written to the audio hardware."}
   signals {:doc "a list of signals or single output to write out. You cannot change the size of this once a synth has been defined."}]
  ""
  (:ar (internal:replace-out:ar (+ (num-output-buses) (num-input-buses) bus) signals))
  (:kr (internal:replace-out:kr bus signals)))

(defcgen offset-out
  "write signal to a bus with sample accurate timing"
  [bus {:default 0 :doc "the index of the bus to write to. The lowest index numbers are written to the audio hardware."}
   signals {:doc "a list of signals or single output to write out. You cannot change the size of this once a synth has been defined."}]
  "Output signal to a bus,  the sample offset within the bus is kept exactly; i.e. if the synth is scheduled to be started part way through a control cycle, offset-out will maintain the correct offset by buffering the output and delaying it until the exact time that the synth was scheduled for.

This ugen is used where sample accurate output is needed."
  (:ar (internal:offset-out:ar (+ (num-output-buses) (num-input-buses) bus) signals)))

(defcgen x-out
  "write signal to a bus, crossfading with the existing content"
  [bus {:default 0 :doc "the index of the bus to write to. The lowest index numbers are written to the audio hardware."}
   xfade {:doc "crossfade level"}
   channels-array {:doc "a list of signals or single output to write out. You cannot change the size of this once a synth has been defined."}]
  "xfade is a level for the crossfade between what is on the bus and what you are sending.

The algorithm is equivalent to this:

bus_signal = (input_signal * xfade) + (bus_signal * (1 - xfade));"
  (:ar (internal:x-out:ar (+ (num-output-buses) (num-input-buses) bus) xfade channels-array)))

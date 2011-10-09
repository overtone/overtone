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
  (:kr (internal:out:kr (+ (num-output-buses) (num-input-buses) bus) signals)))

(defcgen in
  "Read signal from a bus"
  [bus {:default 0 :doc "the index of the bus to read in from"}
   num-channels {:default 1 :doc "the number of channels (i.e. adjacent buses) to read in. The default is 1. You cannot modulate this number by assigning it to an argument in a SynthDef."}]
  "in:kr is functionally similar to in-feedback. That is it reads all data on the bus whether it is from the current cycle or not. This allows for it to receive data from later in the node order. in:ar reads only data from the current cycle, and will zero data from earlier cycles (for use within that synth; the data remains on the bus). Because of this and the fact that the various out ugens mix their output with data from the current cycle but overwrite data from an earlier cycle it may be necessary to use a private control bus when this type of feedback is desired. There is an example below which demonstrates the problem."
  (:ar (internal:in:ar (+ (num-output-buses) (num-input-buses) bus) num-channels))
  (:kr (internal:in:kr (+ (num-output-buses) (num-input-buses) bus) num-channels)))


(defcgen sound-in
  "read audio from hardware inputs"
  [bus {:default 0 :doc "the channel (or array of channels) to read in. These start at 0, which will correspond to the first audio input." :modulatable false}]
  "Reads audio from the input of your computer or soundcard. It is a wrapper UGen based on In, which offsets the index such that 0 will always correspond to the first input regardless of the number of inputs present.

N.B. On Intel based Macs, reading the built-in microphone or input may require creating an aggregate device in AudioMIDI Setup."
  (:ar (cond
        (integer? bus) (in:ar (+ (num-output-buses:ir) bus) 1)
        (consecutive-ints? bus) (in:ar (+ (num-output-buses:ir) (first bus)) (count bus))
        :else (in:ar (+ (num-output-buses:ir) bus)))))

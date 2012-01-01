(ns overtone.sc.machinery.cgens.io
  (:use [overtone.sc.machinery defcgen ugens defaults]
        [overtone.helpers seq]))

(defcgen sound-in
  "read audio from hardware inputs"
  [bus {:default 0 :doc "the channel (or array of channels) to read in. These start at 0, which will correspond to the first audio input." :modulatable false}]
  "Reads audio from the input of your computer or soundcard. It is a wrapper UGen based on In, which offsets the index such that 0 will always correspond to the first input regardless of the number of inputs present.

N.B. On Intel based Macs, reading the built-in microphone or input may require creating an aggregate device in AudioMIDI Setup."
  (:ar (cond
        (integer? bus) (in:ar (+ (num-output-buses:ir) bus) 1)
        (consecutive-ints? bus) (in:ar (+ (num-output-buses:ir) (first bus)) (count bus))
        :else (in:ar (+ (num-output-buses:ir) bus)))))

(defcgen scaled-v-disk-in
  "Stream in audio from a file to a buffer with a rate scaled depending on the buffer's sample-rate."
  [num-channels {:doc "The number of channels that the buffer will be. This must be a fixed integer." :modulatable false}
   buf-num {:default 0 :doc "The index of the buffer to use."}
   rate {:default 1 :doc "Rate multiplier. 1.0 is the default rate for the specified buffer, 2.0 is one octave up, 0.5 is one octave down -1.0 is backwards normal rate ... etc. Interpolation is cubic."}
   loop {:default 0.0 :doc "1 means true, 0 means false. This is modulateable."}
   sendID {:default 0 :doc "send an osc message with this id and the file position each time the buffer is reloaded: ['/diskin', nodeID, sendID, frame] "}]
  "Uses buf-rate-scale to determine the rate at which to stream data through the specified buffer for playback with v-disk-in."
  (:ar (v-disk-in:ar num-channels buf-num (* rate (buf-rate-scale:kr buf-num)) loop sendID)))

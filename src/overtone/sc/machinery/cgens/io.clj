(ns overtone.sc.machinery.cgens.io
  (:use [overtone.sc.machinery defcgen ugens defaults]
        [overtone.util lib]))

(defcgen sound-in
  "read audio from hardware inputs"
  [bus {:default 0 :doc "the channel (or array of channels) to read in. These start at 0, which will correspond to the first audio input." :modulatable false}]
  "Reads audio from the input of your computer or soundcard. It is a wrapper UGen based on In, which offsets the index such that 0 will always correspond to the first input regardless of the number of inputs present.

N.B. On Intel based Macs, reading the built-in microphone or input may require creating an aggregate device in AudioMIDI Setup."
  (:ar (cond
        (integer? bus) (in:ar (+ (num-output-buses:ir) bus) 1)
        (consecutive-ints? bus) (in:ar (+ (num-output-buses:ir) (first bus)) (count bus))
        :else (in:ar (+ (num-output-buses:ir) bus)))))

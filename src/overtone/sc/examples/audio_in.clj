(ns overtone.sc.examples.audio-in
  (:use [overtone.sc.machinery defexample]
        [overtone.sc ugens]
        [overtone.sc.cgens audio-in]))

(defexamples sound-in
  (:mono-patch
   "World's most expensive patchcord"
   "Here we simply output the values found on the audio bus representing the first mic (this will be a mono signal). Use headphones to avoid hearing feedback."
   rate :ar
   []
   "
   (sound-in 0)"
   contributed-by "Sam Aaron")

  (:stereo-patch
   "World's most expensive stereo patchcord"
   "Here we simply output the values found on the audio bus representing the first two mic channels (this will be a stereo signal). Use headphones to avoid hearing feedback."
   rate :ar
   []
   "
   (sound-in [0 1])"
   contributed-by "Sam Aaron"))

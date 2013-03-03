(ns overtone.sc.cgens.buf-io
  (:use [overtone.sc defcgen ugens]))

(defcgen scaled-play-buf
  "Play back a sample resident in a buffer with a rate scaled depending
   on the buffer's sample-rate."
  [num-channels {:doc "The number of channels that the buffer will be. This must be a fixed integer." :modulatable false}
   buf-num {:default 0 :doc "The index of the buffer to use."}
   rate {:default 1 :doc "Rate multiplier. 1.0 is the default rate for the specified buffer, 2.0 is one octave up, 0.5 is one octave down -1.0 is backwards normal rate ... etc. Interpolation is cubic."}
   trigger {:default 1.0 :doc "A trigger causes a jump to the startPos. A trigger occurs when a signal changes from <= 0 to > 0."}
   start-pos {:default 0.0 :doc "Sample frame to start playback."}
   loop {:default 0.0 :doc "1 means true, 0 means false. This is modulateable."}
   action {:default 0 :doc "action to be executed when the buffer is finished playing."}]
  "Uses buf-rate-scale to determine the rate with which to play back the specified buffer."
  (:ar (play-buf:ar num-channels buf-num (* rate (buf-rate-scale:kr buf-num)) trigger start-pos loop action))
  (:kr (play-buf:kr num-channels buf-num (* rate (buf-rate-scale:kr buf-num)) trigger start-pos loop action)))

(defcgen local-buf
  "Create a synth-local buffer."
  [num-frames {:doc "The number of frames the buffer should contain."}
   num-channels {:default 1 :doc "The number of channels for the buffer."}]
  "A given local-buf may only be used within the synth it is defined in. More efficient than using a standard buffer"
  (:ir (internal:local-buf:ir num-channels num-frames)))

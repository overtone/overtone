(ns
    ^{:doc "Useful synths for manipulating buffers"
      :author "Sam Aaron"}
  overtone.synth.buffer
  (:use [overtone.core]))

(defsynth mono-player
  "Plays a single channel audio buffer."
  [buf 0 rate 1.0 start-pos 0.0 loop? 0 amp 1 pan 0 out-bus 0]
  (out out-bus (* amp
                  (pan2
                   (scaled-play-buf 1 buf rate
                                    1 start-pos loop?
                                    FREE)
                   pan))))

(defsynth stereo-player
  "Plays a dual channel audio buffer."
  [buf 0 rate 1.0 start-pos 0.0 loop? 0 amp 1 pan 0 out-bus 0]
  (let [s (scaled-play-buf 2 buf rate
                           1 start-pos loop?
                           FREE)]
    (out out-bus (* amp (balance2 (first s) (second s) pan)))))

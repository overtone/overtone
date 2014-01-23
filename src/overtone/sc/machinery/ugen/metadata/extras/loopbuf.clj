(ns overtone.sc.machinery.ugen.metadata.extras.loopbuf
  (:use [overtone.sc.machinery.ugen common check]))

(def specs
  [
   {:name "LoopBuf"
    :summary "Sample looping oscillator"
    :args [{:name "n-channels"
            :doc "number of channels that the buffer will be.  this must
                  be a fixed integer. The architecture of the synth
                  design cannot change after it is compiled.  warning:
                  if you supply a bufnum of a buffer that has a
                  different num-channels then you have specified to the
                  loop-buf, it will fail silently."}

           {:name "bufnum"
            :default 0
            :doc "The index of the buffer to use"}

           {:name "rate"
            :default 1
            :doc "1.0 is normal, 2.0 is one octave up, 0.5 is one octave
                  down -1.0 is backwards normal rate ... etc."}

           {:name "gate"
            :default 1
            :doc "positive gate starts playback from startPos negative
                  gate plays rest of sample from current position"}

           {:name "start-pos"
            :default 0
            :doc "sample frame to start playback"}

           {:name "start-loop"
            :default 0
            :doc "sample frame of start of loop"}

           {:name "end-loop"
            :default 0
            :doc "sample frame of end of loop"}

           {:name "interpolation"
            :default 0
            :doc "1 means no interpolation, 2 is linear, 4 is cubic
                  interpolation"}]

    :rates #{:ar}
    :doc "Plays and loops between two frames of a sample resident in memory."}])


;; TODO: Doesn't yet work - figure out why.
;; (defsynth stereo-loop-buf-example [buf 0 rate 1 glide 0 gate 1 loop-rel 0 start-pos 0 start-loop 0 end-loop 1 amp 1 out-bus 0]
;;   (let [snd (loop-buf 2 buf (* rate (buf-rate-scale:kr buf)) (+ gate loop-rel) start-pos start-loop end-loop )]
;;     (out out-bus (* amp snd))))

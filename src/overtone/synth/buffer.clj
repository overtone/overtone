(ns
    ^{:doc "Useful synths for manipulating buffers"
      :author "Sam Aaron"}
  overtone.synth.buffer
  (:use [overtone.core]))

(defsynth mono-play-buffer-partial [buf 0 rate 1 start 0 end 1 loop? 0 amp 1 out-bus 0]
  (let [n-frames  (buf-frames buf)
        start-pos (* start n-frames)
        end-pos   (* end n-frames)
        phase     (phasor:ar :start start-pos :end end-pos :rate rate)
        snd       (buf-rd 1 buf phase)]
    (free-self (and (not-pos? loop?)
                    (< end-pos (+ (a2k phase) 1))))
    (out out-bus (* amp snd))))

(defsynth stereo-play-buffer-partial [buf 0 rate 1 start 0 end 1 loop? 0 amp 1 out-bus 0]
  (let [n-frames  (buf-frames buf)
        start-pos (* start n-frames)
        end-pos   (* end n-frames)
        phase     (phasor:ar :start start-pos :end end-pos :rate rate)
        snd       (buf-rd 2 buf phase)]
    (free-self (and (not-pos? loop?)
                    (< end-pos (+ (a2k phase) 1))))
    (out out-bus (* amp snd))))

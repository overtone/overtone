(ns examples.monome-sequencer
  (:use overtone.live
        [overtone.inst synth]))
(pad 37)
(rise-fall-pad)
;; Create a buffer to store the pattern in
(def pat (buffer 8))
(buffer-write! pat [1 1 0 1 1 0 1 0])

(def freqs (buffer 8))
(buffer-write! freqs [990 110 220 220 440 1100 200 1000])
(buffer-write! freqs [220 220 220 220 220 220 220 220])
(buffer-write! freqs [110 310 520 720 940 1100 1300 1500])
(def num-sub-beats (buffer 1))
(buffer-set! num-sub-beats 10)

(def bpm (buffer 1))
(buffer-write! bpm 200)

(def beats-per-bar (buffer 1))
(buffer-write! beats-per-bar 8)

(def sub-metro-bus (control-bus))
(def metro-bus (control-bus))
(def beat-bus (control-bus))

(defsynth sub-metro []
  (let [rate (* (buf-rd:kr 1 num-sub-beats) (/ (buf-rd:kr 1 bpm) 60))]
    (out sub-metro-bus (impulse:kr rate))))

(defsynth metro []
  (let [tr (pulse-divider:kr (in:kr sub-metro-bus) (buf-rd:kr 1 num-sub-beats))]
    (out metro-bus tr)))

(defsynth beat-num []
  (let [tr   (in:kr metro-bus)
        beat (stepper tr 1 0 (- (buf-rd:kr 1 beats-per-bar) 1))]
    (out beat-bus beat)))

(definst pingr [dec 0.01]
  (let [tr   (in:kr metro-bus)
        beat (in:kr beat-bus)
        vol  (buf-rd:kr 1 pat beat)
        src  (sin-osc (buf-rd:kr 1 freqs beat))]
    (* vol  (decay tr (* 10 dec)) src)))
(ctl pingr :dec 0.1)


(do
  (sub-metro)
  (metro)
  (beat-num)
  (pingr))

(do  (kill pingr) (pingr))

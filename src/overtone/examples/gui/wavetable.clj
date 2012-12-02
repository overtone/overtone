(ns overtone.examples.gui.wavetable
  (:use overtone.live
        overtone.gui.wavetable))

; Create a wavetable with 8 buffers holding different waveforms
(def table (wavetable 8))

; Bring up the editor, and then draw a random waveform in each box
(def editor (wavetable-editor table))

; The osc ugen can be used to play over a single waveform
(defsynth wave-player
  [buf 0 freq 440]
  (out 0 (* [0.8 0.8] (rlpf (osc buf freq) (* 2 freq)))))

(def player (wave-player (first (:waveforms table)) 660))
(ctl player :buf 7)
(ctl player :freq 220)
(stop)

; The v-osc ugen can interpolate to sweep over multiple waveforms
(defsynth table-player
  [buf-start 0 buf-end 7 freq 440]
  (let [buf (mouse-x buf-start buf-end)
        snd (v-osc buf freq)
        snd (rlpf snd (* 2 freq))]
    (out 0 (* [0.8 0.8] snd))))


; evaluate this form, then move the mouse around
(def player (table-player (first (:waveforms table))
                          (last (:waveforms table)) 220))
(stop)

(defsynth waver
  [buf-start 0 buf-end 7 freq 440 sweep-freq 8]
  (let [buf (+ buf-start (* (- buf-end buf-start) (* 0.5 (+ 1 (sin-osc:kr sweep-freq)))))
        snd (v-osc buf freq)
        snd (rlpf snd (* 2 freq))]
    (out 0 (* [0.8 0.8] snd))))

(def player (waver (first (:waveforms table))
                          (last (:waveforms table))
                   220 8))

(ctl player :sweep-freq 80)
(stop)

; The wavetable can be interpolated too, so evaluating this form will
; make the intermediate waveforms transition smoothly between the start and end.
(linear-interpolate-wavetable table 0 7)

(def player (waver (first (:waveforms table))
                          (last (:waveforms table))
                   220 8))

(stop)

(ns examples.gui
  (:use overtone.core
        [overtone.gui info control wavetable sequencer mixer]
        [overtone.inst synth drum]))

; Show the server info window
(server-info-window)

; Create a synth with metadata that can be used to generate a GUI controller
(defsynth foo [note {:default 60 :min 0 :max 120 :step 1}
               attack {:default 0.002 :min 0.0001 :max 3.0 :step 0.001}
               decay  {:default 0.3 :min 0.0001 :max 3.0 :step 0.001}]
  (out 0 (* [0.8 0.8] (env-gen (perc attack decay) :action FREE)
            (sin-osc (midicps note)))))

; Create the gui controller
(synth-controller foo)

(def m (metronome 85))

(defn foo-player [b]
  (at (m b) (foo))
  (apply-at (m (inc b)) #'foo-player [(inc b)]))

; Start the synth looping, and play with the GUI to hear it change live
(foo-player (m))
(stop)

; Works with instruments too, as long as they have all the necessary metadata.
(definst bar [note {:default 60 :min 0 :max 120 :step 1}
              attack {:default 0.002 :min 0.0001 :max 3.0 :step 0.001}
              decay  {:default 0.3 :min 0.0001 :max 3.0 :step 0.001}]
  (* [0.8 0.8] (env-gen (perc attack decay) :action FREE)
            (sin-osc (midicps note) [0 0.2])))

(synth-controller bar)

(defn bar-player [b]
  (at (m b) (bar))
  (apply-at (m (inc b)) #'bar-player [(inc b)]))

(bar-player (m))

(defn fill-buffer
  [b f]
  (let [size (:size b)]
    (buffer-write! b (map #(f (/ (* % 2 Math/PI) size)) (range 0 size)))
    b))

; Create a buffer to use for our wavetable
(def b (buffer 1024))
(waveform-editor b true)

(defsynth table-player
  [buf 0 freq 440]
  (out 0 (* [0.8 0.8] (osc buf freq))))

(table-player b 660)

;(def waves (load-samples "waveforms/AKWF_cello/*.wav"))
(def table (wavetable 12 1024))
(wavetable-editor table)

; Up the tempo, and try the step sequencer out on a couple of drums
(m :bpm 128)
(step-sequencer m 8 ks1 ping)

(use 'overtone.gui.mixer)
(mixing-console)

(ns examples.gui
  (:use overtone.core
        overtone.gui
        [overtone.inst synth drum]))

;;Work in progress

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Overtone control panel
; * server info
; * master volume control
; * record button
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; Show the server info window
(control-panel)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Mixer
; * adjust volume levels and pan for one or more instruments
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; Create a mixer for some instruments
(mixer rise-fall-pad ks1 ping)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Synth controller
; * manipulate synth and instrument parameters with sliders
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; Create a synth with metadata that can be used to generate a GUI controller
(defsynth foo [note   {:default 60 :min 0 :max 120 :step 1}
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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Waveform and wavetable editors
; * edit a single waveform by hand
; * edit a table of multiple waveforms
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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
(stop)

;(def waves (load-samples "waveforms/AKWF_cello/*.wav"))
(def table (wavetable 12 1024))
(wavetable-editor table)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Step sequencer
; * sequence triggered sounds (typically for rhythmic instruments)
; * one row per instrument
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; Up the tempo, and try the step sequencer out on a couple of drums
(m :bpm 128)
(step-sequencer m 8 ks1 ping)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Stepinator
; * sequence one instrument or parameter value
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; Bring up a monophonic step sequencer
(def pstep (stepinator))

;  * parameters used in synthdefs
(definst step-pad
  [note 60 amp 0.7 attack 0.009 release 0.6]
  (let [freq  (midicps note)
        env   (env-gen (perc attack release) :action FREE)
        f-env (+ freq (* 3 freq (env-gen (perc 0.012 (- release 0.1)))))
        bfreq (/ freq 2)
        sig   (apply +
                     (concat (* 0.7 (sin-osc [bfreq (* 0.99 bfreq)]))
                             (lpf (saw [freq (* freq 1.01)]) f-env)))
        audio (* amp env sig)]
    audio))

; You can access the sequence once when creating a synth
(demo 20
  (let [note (duty (dseq [0.2 0.1] INF)
                   0
                   (dseq (map #(+ 60 %) (:steps @(:state pstep)))))
        src (sin-osc (midicps note))]
    (* [0.2 0.2] src)))

; or access the sequence steps in a player function
(defn step-player [b]
  (at (m b)
      (step-pad (+ 60 (nth (:steps @(:state pstep)) (mod b 16)))))
  (apply-at (m (inc b)) #'step-player [(inc b)]))

(:steps @(:state pstep))

(step-player (m))

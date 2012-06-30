(ns examples.monotron
  (:use overtone.live))

;; Monotron Clone Attempt by Roger Allen.
;;
;; My first definst, so much could easily be wrong below...
;;
;; Source
;; http://korg.com/monotrons
;; http://korg.com/services/products/monotron/monotron_Block_diagram.jpg
;; some inspiration
;; http://www.soundonsound.com/sos/aug10/articles/korg-monotron.htm
;; following patterns from
;; https://github.com/overtone/overtone/blob/master/src/overtone/inst/synth.clj
;;
;; found filter discussion here
;; http://www.timstinchcombe.co.uk/index.php?pge=mono

(defsynth monotron
  "Korg Monotron from website diagram: http://korg.com/services/products/monotron/monotron_Block_diagram.jpg."
  [note     60            ; midi note value
   volume   0.7           ; gain of the output
   mod_pitch_not_cutoff 1 ; use 0 or 1 only to select LFO pitch or cutoff modification
   pitch    0.0           ; frequency of the VCO
   rate     4.0           ; frequency of the LFO
   int      1.0           ; intensity of the LFO
   cutoff   1000.0        ; cutoff frequency of the VCF
   peak     0.5           ; VCF peak control (resonance)
   gate     1.0]          ; another output gain?
  (let [note_freq       (midicps note)
        pitch_mod_coef  mod_pitch_not_cutoff
        cutoff_mod_coef (- 1 mod_pitch_not_cutoff)
        LFO             (* int (saw rate))
        VCO             (saw (+ note_freq pitch (* pitch_mod_coef LFO)))
        vcf_freq        (+ cutoff (* cutoff_mod_coef LFO) note_freq)
        ;; from web vcf reciprocal of Q looks 1-ish
        vcf_bpf_rq       1.0
        ;; from web vcf looks like you should always have a large LPF component
        ;; BPF should get added on top.  Seems like I should scale this to clamp
        ;; the output, though.
        vcf_lpf         (lpf VCO vcf_freq)
        vcf_bpf         (bpf VCO vcf_freq vcf_bpf_rq)
        VCF             (/ (+ vcf_lpf (* peak vcf_bpf)) (+ 1 peak))
        ]
    (out 0 (* gate volume VCF))))

;(comment

;; create some instances of the synth
(do
  (def N0 (monotron 72 0.8 1 0.0 2.5 350.0 800.0 0.8 1.0))
  (def N1 (monotron 76 0.8 1 0.0 2.5 350.0 800.0 0.8 1.0))
  (def N2 (monotron 79 0.8 1 0.0 2.5 350.0 800.0 0.8 1.0)))

;; edit & C-x C-e on any these to play around
(ctl monotron :note   70)
(ctl N0 :note   60)
(ctl N1 :note   64)
(ctl N2 :note   (+ 30 7))
(ctl monotron :volume 0.5)
(ctl monotron :mod_pitch_not_cutoff 1)
(ctl monotron :pitch  0.0)
(ctl monotron :rate   0.5)
(ctl monotron :int    500.0)
(ctl monotron :cutoff 380.0)
(ctl monotron :peak   0.0)
(ctl monotron :gate   1.0)

;; for when you're done
(kill monotron)
(kill N0)
(kill N1)
(kill N2)

;; major chord
(defn maj-tri [note]
  (ctl N0 :note   note)
  (ctl N1 :note   (+ note 4))
  (ctl N2 :note   (+ note 7)))

(maj-tri 59)

;; MIDI Control ==================================================
;; hooking up to the iPad
(def kb (midi-in "Control Session"))
;; simple listener just for testing
(defn midi-listener [event ts]
  (println "listener: " event))

;(midi-handle-events kb #'midi-listener)

;; 'real' listener
;; hook up Control to the ctl messages
;; use monotron_control.js layout
;; FIXME use konstants for controller #s to allow for switching.
(defn midi-listener [event ts]
  ;;(println "listener: " event)
  (cond

   ;; controller 0 is for PITCH
   (and (== 176 (:cmd event)) (== 0 (:note event)))
   (let [v (midi->hz ( / (:vel event) 1.0))]
     (println "pitch" v)
     (ctl monotron :cutoff v))

   ;; controller 1 is for CUTOFF
   (and (== 176 (:cmd event)) (== 1 (:note event)))
   (let [v ( * 30 (:vel event))]
     (println "cutoff" v)
     (ctl monotron :cutoff v))

   ;; controller 2 is for PEAK
   (and (== 176 (:cmd event)) (== 2 (:note event)))
   (let [v ( / (:vel event) 127.0)]
     (println "peak" v)
     (ctl monotron :peak v))

   ;; controller 3 is for INT
   (and (== 176 (:cmd event)) (== 3 (:note event)))
   (let [v ( * 20 (:vel event))]
     (println "int" v)
     (ctl monotron :int v))

   ;; controller 4 is for RATE
   (and (== 176 (:cmd event)) (== 4 (:note event)))
   (let [v ( * 0.25 (:vel event))]
     (println "rate" v)
     (ctl monotron :rate v))

   ;; controller 5 is for NOTE.  Make it one octave
   (and (== 176 (:cmd event)) (== 5 (:note event)))
   (let [v (+ 50 (* 12 (/ (:vel event) 127.0)))]
     (println "note" v)
     (ctl monotron :note v))

   ;; controller 7 is for VOLUME
   (and (== 176 (:cmd event)) (== 7 (:note event)))
   (let [v ( / (:vel event) 127.0)]
     (println "volume" v)
     (ctl monotron :volume v))

   )
  )
  )

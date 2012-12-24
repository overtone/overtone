(ns overtone.examples.instruments.monotron
  (:use overtone.live))

;; (use 'overtone.live)

;; ======================================================================
;; Monotron Clone by Roger Allen.
;;   via some code in https://github.com/rogerallen/explore_overtone
;;
;; Source
;; http://korg.com/monotrons
;; http://korg.com/services/products/monotron/monotron_Block_diagram.jpg
;;
;; Following patterns from
;; https://github.com/overtone/overtone/blob/master/src/overtone/inst/synth.clj
;;
;; Inspiration
;; http://www.soundonsound.com/sos/aug10/articles/korg-monotron.htm
;; http://www.timstinchcombe.co.uk/index.php?pge=mono
(defsynth monotron
  "Korg Monotron from website diagram:
   http://korg.com/services/products/monotron/monotron_Block_diagram.jpg."
  [note     60            ; midi note value
   volume   0.7           ; gain of the output
   mod_pitch_not_cutoff 1 ; use 0 or 1 only to select LFO pitch or cutoff modification
   pitch    0.0           ; frequency of the VCO
   rate     4.0           ; frequency of the LFO
   int      1.0           ; intensity of the LFO
   cutoff   1000.0        ; cutoff frequency of the VCF
   peak     0.5           ; VCF peak control (resonance)
   pan      0             ; stereo panning
   ]
  (let [note_freq       (midicps note)
        pitch_mod_coef  mod_pitch_not_cutoff
        cutoff_mod_coef (- 1 mod_pitch_not_cutoff)
        LFO             (* int (saw rate))
        VCO             (saw (+ note_freq pitch (* pitch_mod_coef LFO)))
        vcf_freq        (+ cutoff (* cutoff_mod_coef LFO) note_freq)
        VCF             (moog-ff VCO vcf_freq peak)
        ]
    (out 0 (pan2 (* volume VCF) pan))))

;; ======================================================================
;; create an instance of the synth
(def N0 (monotron 40 0.8 1 0.0 2.5 350.0 800.0 3.0))

;; edit & C-x C-e on any these to play around
(ctl N0 :note   80)               ;; midi note value: 0 to 127
(ctl N0 :volume 0.7)              ;; gain of the output: 0.0 to 1.0
(ctl N0 :mod_pitch_not_cutoff 0)  ;; use 0 or 1 only to select LFO pitch or cutoff modification
(ctl N0 :pitch  10.0)             ;; this + note is frequency of the VCO
(ctl N0 :rate   1.5)              ;; frequency of the LFO
(ctl N0 :int    800.0)           ;; intensity of the LFO
(ctl N0 :cutoff 600.0)           ;; cutoff frequency of the VCF
(ctl N0 :peak   0.5)              ;; VCF peak control (resonance) 0.0 to 4.0

;; for when you're done.  kill all or just some of the notes
(kill N0)

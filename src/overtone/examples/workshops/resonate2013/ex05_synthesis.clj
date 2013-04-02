(ns
    ^{:doc "A few examples of more complex audio synthesis,
           ported from SuperCollider to Overtone"}
  overtone.examples.workshops.resonate2013.ex05_synthesis
  (:use [overtone.live]))

;; First let's use a tweet by Juan A. Romero
;; (http://soundcloud.com/rukano)
;; This code is written in Supercollider's Smalltalk dialect:
;;
;; play{d=Duty;f=d.kr(1/[1,2,4],0,Dseq([0,3,7,12,17]+24,inf));GVerb.ar(Blip.ar(f.midicps*[1,4,8],LFNoise1.kr(1/4,3,4)).sum,200,8)}

(comment
  ;; A port to Overtone is almost equally succinct, but still hard to
  ;; understand...
  ;; https://soundcloud.com/toxi/rukanos-space-organ
  (demo 60 (g-verb (sum (map #(blip (* (midicps (duty:kr % 0 (dseq [24 27 31 36 41] INF))) %2) (mul-add:kr (lf-noise1:kr 1/2) 3 4)) [1 1/2 1/4] [1 4 8])) 200 8))

  ;; A more easy-on-the-eyes version would look like this:
  (demo 60
        (let [;; First create 3 frequency generators at different
              ;; tempos/rates [1 1/2 1/4]
              ;; Each generator will cycle (at its own pace) through the sequence of
              ;; notes given to dseq and convert notes into actual frequencies
              f (map #(midicps (duty:kr % 0 (dseq [24 27 31 36 41] INF))) [1 1/2 1/4])
              ;; Next we transpose the frequencies over several octaves
              ;; and create a band limited impulse generator (blip) for
              ;; each of the freq gens. The blip allows us to configure the number
              ;; of overtones/harmonics used, which is constantly modulated by a
              ;; noise generator between 1 and 7 harmonics...
              tones (map #(blip (* % %2) (mul-add:kr (lf-noise1:kr 1/4) 3 4)) f [1 4 8])]
          ;; finally, all tones are summed into a single signal
          ;; and passed through a reverb with a large roomsize and decay time...
          (g-verb (sum tones) 200 8)))
  )

;; The following synth is taken from Overtone's bundled examples and
;; based on a Supercollider script by Dan Stowells (w/ comments added by toxi)
;; Creates a dubstep synth with random wobble bassline, kick & snare patterns
(comment
  (demo 60
        (let [bpm 160
              ;; create pool of notes as seed for random base line sequence
              notes (concat (repeat 8 40) [40 41 28 28 28 27 25 35 78])
              ;; create an impulse trigger firing once per bar
              trig (impulse:kr (/ bpm 160))
              ;; create frequency generator for a randomly picked note
              freq (midicps (lag (demand trig 0 (dxrand notes INF)) 0.25))
              ;; switch note durations
              swr (demand trig 0 (dseq [1 6 6 2 1 2 4 8 3 3] INF))
              ;; create a sweep curve for filter below
              sweep (lin-exp (lf-tri swr) -1 1 40 3000)
              ;; create a slightly detuned stereo sawtooth oscillator
              wob (apply + (saw (* freq [0.99 1.01])))
              ;; apply low pass filter using sweep curve to control cutoff freq
              wob (lpf wob sweep)
              ;; normalize to 80% volume
              wob (* 0.8 (normalizer wob))
              ;; apply band pass filter with resonance at 5kHz
              wob (+ wob (bpf wob 5000 20))
              ;; mix in 20% reverb
              wob (+ wob (* 0.2 (g-verb wob 9 5 0.7)))
              ;; create impulse generator from given drum pattern
              kickenv (decay (t2a (demand (impulse:kr (/ bpm 30)) 0
                                          (dseq [1 0 0 0 0 0 1 0 1 0 0 1 0 0 0 0] INF))) 0.7)
              ;; use modulated sine wave oscillator
              kick (* (* kickenv 7) (sin-osc (+ 40 (* kickenv kickenv kickenv 200))))
              ;; clip at max volume to create distortion
              kick (clip2 kick 1)
              ;; snare is just using gated & over-amplified pink noise
              snare (* 3 (pink-noise) (apply + (* (decay (impulse (/ bpm 240) 0.5) [0.4 2]) [1 0.05])))
              ;; send through band pass filter with peak @ 1.5kHz
              snare (+ snare (bpf (* 8 snare) 1500))
              ;; also clip at max vol to distort
              snare (clip2 snare 1)]
          ;; mixdown & clip
          (clip2 (+ wob kick snare) 1)))
  )

(ns example.experiment
  (:use overtone.live))

(definst saw-sin [freq-a 443
                   freq-b 440]
         (+ (* 0.3 (saw freq-a))
            (* 0.3 (sin-osc freq-b 0))))

(defsynth whoah []
  (let [sound (resonz (saw (map #(+ % (* (sin-osc 100) 1000)) [440 443 437])) (x-line 10000 10 10) (line 1 0.05 10))]
  (* (lf-saw:kr (line:kr 13 17 3)) (line:kr 1 0 10) sound)))

(defn square [freq]
  (with-ugens
    (pulse freq 0.5)))

(defn mix [& args]
  (with-ugens
    (reduce + args)))

; Beware!!! Playing this synth might make you crash :~)
(definst vintage-bass [note 60 velocity 100 detune 7 rq 0.4]
  (let [saw1 (* 0.75 (saw (midicps note)))
        saw2 (* 0.32 (saw (+ detune (midicps note))))
        sqr  (* 0.32 (square (midicps (- note 12))))
        amp  (/ 128.0 velocity)
        mx   (* amp (mix saw1 saw2 sqr))
        env-amp (+ 0.25 (* 0.55 amp))
        env (* env-amp (env-gen (adsr) (sin-osc 0.5) 1 0 1 FREE))
        filt (rlpf mx (* env (midicps note)) rq)]
    filt))

(definst round-kick [amp 0.5 decay 0.6 freq 65]
  (* (env-gen (perc 0.01 decay) 1 1 0 1 FREE)
     (sin-osc freq (* java.lang.Math/PI 0.5)) amp))

; Creating pads
(defn ugen-cents
  "Returns a frequency computed by adding n-cents to freq.  A cent is a
  logarithmic measurement of pitch, where 1-octave equals 1200 cents."
  [freq n-cents]
  (with-ugens
    (* freq (pow 2 (/ n-cents 1200)))))

(definst pad [freq 440 split -5]
  (* 0.3
     (saw [freq (ugen-cents freq split)])))

(definst rise-fall-pad [freq 440 split -5 t 4]
  (let [f-env (env-gen (perc t t) 1 1 0 1 FREE)]
    (rlpf (* 0.3 (saw [freq (ugen-cents freq split)]))
          (+ (* 0.6 freq) (* f-env 2 freq)) 0.2)))

(rise-fall-pad)
(rise-fall-pad 440 -10)
(rise-fall-pad 220 5 1)
(rise-fall-pad 660 -3 2)

(definst resonant-pad [freq 440 split -5 t 4 lfo 0.5 depth 10]
  (let [f-env (env-gen (perc t t) 1 1 0 1 FREE)
        lfo (* depth (sin-osc:kr lfo))]
    (rlpf (* 0.3 (+ (square freq) (lf-tri (+ lfo (ugen-cents freq split)))))
          (+ (* 0.8 freq) (* f-env 2 freq)) 3/4)))


(defsynth pan-test [note 60]
  (let [freq (midicps note)
        waves (saw [freq (* 1.5 freq)])
        pans (pan2 waves (saw 0.2))]
    (out 0 (map + (overtone.sc.ugen/parallel-seqs (pan2 waves pans))))))

(defsynth splay-test [note 60]
  (let [freq  (midicps note)
        waves (saw [freq (* 1.5 freq)])
        snd   (splay waves :spread 0.1 :level 0.1 :center -0.75)]
    (out 0 snd)))

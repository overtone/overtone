(ns overtone.instruments
  (:use (overtone synth envelope pitch)))

;; Helpers

(defmacro mix [& args]
  (syn (reduce (fn [mem arg] (list '+ mem arg))
          args)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Drums
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defsynth kick {:out 0 :freq 50 :mod-freq 5 :mod-index 5 
                :sustain 0.4 :amp 0.8 :noise 0.025}
  (let [pitch-contour (line.kr (* 2 :freq), :freq 0.02)
        drum (lpf.ar (pm-osc.ar pitch-contour :mod-freq (/ :mod-index 1.3))
                     1000)
        drum-env (env-gen.ar (perc 0.005, :sustain) :done-free)
        hit (hpf.ar (* :noise (white-noise.ar)) 500)
        hit (lpf.ar hit (lin.kr 6000 500 0.03))
        hit-env (env-gen.ar (perc) :done-free)]
    (out.ar :out (pan2.ar (* :amp (+ (* drum drum-env) (* hit hit-env))) 0))))

(defsynth soft-kick 
  (out.ar 0 (pan2.ar
              (mul-add.ar
                (sin-osc.ar 60 (* Math/PI 2))
                (env-gen.ar (perc 0.001 0.1))
                0) 
              0)))

(defsynth round-kick {:amp 0.5 :decay 0.6 :freq 65}
  (let [env (env-gen.ar (perc 0 :decay) :done-free)
        snd (* :amp (sin-osc.ar :freq (* Math/PI 0.5)))]
    (out.ar 0 (pan2.ar (* snd env) 0))))

(defsynth snare {:out 0 :freq 405 :amp 0.8 :sustain 0.1 
                 :drum-amp 0.25 :crackle-amp 40 :tightness 1000}
  (let [drum-env (* 0.5 (env-gen.ar (perc 0.005 :sustain) :done-free))
        drum-s1 (* drum-env (sin-osc.ar :freq))
        drum-s2 (* drum-env (sin-osc.ar (* :freq 0.53)))
        drum-s3 (* drum-env (pm-osc.ar (saw.ar (* :freq 0.85)) 184 (/ 0.5 1.3)))
        drum (* :drum-amp (mix drum-s1 drum-s2 drum-s3))
        noise (lf-noise-0.ar 20000 0.1)
        filtered (* 0.5 (brf.ar noise 8000 0.1))
        filtered (* 0.5 (brf.ar filtered 5000 0.1))
        filtered (* 0.5 (brf.ar filtered 3600 0.1))
        filtered (* (env-gen.ar (perc 0.005 :sustain) :done-free)
                    (brf.ar filtered 2000 0.0001))
        crackle (* (resonz.ar filtered :tightness) :crackle-amp)]
    (out.ar :out (pan2.ar (* :amp (* 5 (+ drum crackle))) 0))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Basses
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defsynth buzz {:pitch 40 :cutoff 300}
  (let [a (lpf.ar (saw.ar (midicps :pitch)) (+ (lf-noise-1.kr 10) :cutoff))
        b (sin-osc.ar (midicps (- :pitch 12)))]
  (out.ar 0 (pan2.ar (+ a b)))))

;SynthDef("sin", {|out = 0, pitch = 40, dur = 300|
;  Out.ar(out, Pan2.ar( EnvGen.kr(Env.linen(0.001, dur / 1000.0, 0.002), doneAction: 2) * SinOsc.ar(midicps(pitch), 0, 0.8), 0));
;  }).store;
;)
;Synth("sin", ["pitch", 60, "dur", 100]);
;
(defsynth sin {:out 0 :pitch 40 :dur 300}
  (let [snd (sin-osc.ar (midicps :pitch))
        env (env-gen.kr (linen 0.001 0.3 0.002) :done-free)]
    (out.ar :out (pan2.ar (* snd env) 0))))

(defn quick [signal]
  (syn
    (out.ar 0 (pan2.ar signal) 0)))

(defsynth mouse-saw 
  (out.ar 0 (pan2.ar (sin-osc.ar (mouse-y.kr 10 1200 1 0) 0) 0)))

(comment
  (load-synth mouse-saw)
  (hit mouse-saw)
  )

(defsynth line-test 
  (quick (mul-add.ar (sin-osc.ar (line.kr 100 100 0.5) 0 ) 
                     (line.kr 0.5 0 1) 0)))

(defsynth noise-filter {:cutoff 500}
  (quick (lpf.ar (* 0.4 (pink-noise.ar)) :cutoff)))


(comment defsynth harmonic-swimming (quick 
  (let [freq     50
;        partials 20
        partials 1
        z-init   0
        offset   (line.kr 0 -0.02 60)]
    (loop [z z-init
           i 0]
      (if (= partials i) z
        (let [f (max.kr 0 
                    (mul-add.kr 
                        (lf-noise-1.kr 
                            (+ 2 (* 8 (rand))))
                            ; (+ 2 (* 8 (rand)))])
                        0.02 offset))
              newz (mul-add.ar (f-sin-osc.ar (* freq (+ i 1)) 0) f z)]
          (recur newz (inc i))))))))

(defn basic-sound []
  (syn
    (mul-add.ar 
      (decay2.ar (mul-add.ar
                 (impulse.ar 8 0) 
                 (mul-add.kr (lf-saw.kr 0.3 0) -0.3 0.3) 
                 0)
               0.001) 
      0.3 (+ (pulse.ar 80 0.3) (pulse.ar 81 0.3)))))

(defsynth basic-synth
  (quick (basic-sound)))

(defsynth compressed-synth {:attack 0.01 
                            :release 0.01}
  (let [z (basic-sound)]
    (quick 
      (compander.ar z 0 
                    (mouse-x.kr 0.1 1) ; mouse controls gain
                    1 0.5 ; slope below and above the knee
                    :attack  ; clamp time (attack)
                    :release)))) ; relax time (release)

;(defn wand [n]
;  (syn 
;  (mix 
;    (sin-osc.ar (mhz (- n octave)))
;    (lpf.ar (saw.ar [(mhz n) (mhz (+ n fifth))]) 
;          (mul-add.kr
;              (sin-osc.kr 4)
;              30
;              300)))))
;
;(defsynth triangle-test 
;  (out.ar 0 
;          (mul-add.ar 
;            (wand 60)
;            (env-gen.kr 1 1 0 1 2 (triangle 3 0.8))
;            0)))

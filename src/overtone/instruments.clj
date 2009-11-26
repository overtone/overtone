(ns overtone.instruments
  (:use (overtone synth envelope pitch)))

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

(defsynth soft-kick 
  (out.ar 0 (pan2.ar
              (mul-add.ar
                (sin-osc.ar 60 (* Math/PI 2))
                (env-gen.ar (perc 0.001 0.1))
                0) 
              0)))

;SynthDef("round-kick", {|amp= 0.5, decay= 0.6, freq= 65|
;        var env, snd;
;        env= EnvGen.ar(Env.perc(0, decay), doneAction:2);
;        snd= SinOsc.ar(freq, pi*0.5, amp);
;        Out.ar(0, Pan2.ar(snd*env, 0));

(defsynth round-kick {:amp 0.5 :decay 0.6 :freq 65}
  (let [env (env-gen.ar (perc 0 :decay) :done-free)
        snd (* :amp (sin-osc.ar :freq (* Math/PI 0.5)))]
    (out.ar 0 (pan2.ar (* snd env) 0))))

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

;(defmacro mix [& args]
;  (reduce (fn [mem arg] (list '+ mem arg))
;          args))
;
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

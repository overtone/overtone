(ns synth-test
  (:use (overtone sc synth pitch utils))
  (:use clj-backtrace.repl))

(comment 
(boot)
)

(defn quick [signal]
  (syn
    (out.ar 0 (mul-add.ar signal 0.8 0))))

(defsynth mouse-saw 
  (let [ctl (ctl-kr :freq 120)]
    (quick (lpf.ar (saw.ar [(mousex.kr 10 1200 1)
                            (mousey.kr 10 1200 1)]) 400))));(:freq ctl)))))
(comment
(hit (now) mouse-saw)
(reset)
)

(defsynth line-test (quick (mul-add.ar
                              (sin-osc.ar 
                                  (line.kr 100 100 0.5) 0 )
                              (line.kr 0.5 0 1) 0)))

(comment
(boot)
(hit (now) line-test)
)

; TODO: Neither of these is working, and I think it has something to do with the
; JCollider interfacing...  
;(defsynth play-mono
;  (let [ctl (ctl-kr :bufnum 1)]
;    (quick (play-buf.ar 1 (:bufnum ctl) (buf-rate-scale.kr (:bufnum ctl)) 1 0 0))))
;
;(defsynth play-mono-disk 
;  (quick (disk-in.ar 1 2)))
;
;(load-sample "/home/rosejn/projects/overtone/instruments/samples/kit/boom.wav")

;SynthDef(\diskIn2, { | bufnum, out,  gate = 1, sustain,  amp = 1, ar = 0, dr = 0.01 |
;                    Out.ar(out, DiskIn.ar(2, bufnum) 
;                               * Linen.kr(gate, ar, 1, dr, 2)
;                               * EnvGen.kr(Env.linen(0, sustain - ar - dr max: 0 ,dr),1, doneAction: 2) * amp)
;                    });

(defsynth harmonic-swimming (quick 
  (let [freq     50
        partials 20
        z-init   0
        offset   (line.kr 0 -0.02 60)]
    (loop [z z-init
           i 0]
      (if (= partials i) z
        (let [f (max.kr 0 
                    (mul-add.kr 
                        (lf-noise-1.kr 
                            [(+ 2 (* 8 (rand)))
                             (+ 2 (* 8 (rand)))])
                        0.02 offset))
              newz (mul-add.ar (f-sin-osc.ar (* freq (+ i 1)) 0) f z)]
          (recur newz (inc i))))))))
(comment
(hit (now) harmonic-swimming)
(reset)
)

(defsynth soft-kick 
  (out.ar 0 (pan2.ar
              (mul-add.ar
                (sin-osc.ar 60 (* Math/PI 2))
                (env-gen.ar (perc 0.001 0.1))
                0) 
              0)))

(comment
(hit (now) soft-kick)
(reset)
)

(defn wand [n]
  (mix 
    (syn (sin-osc.ar (mhz (- n octave))))
    (syn 
      (lpf.ar (saw.ar [(mhz n) (mhz (+ n fifth))]) 
          (mul-add.kr
              (sin-osc.kr 4)
              30
              300)))))

(defsynth triangle-test 
  (out.ar 0 
          (mul-add.ar 
            (wand 60)
            (env-gen.kr 1 1 0 1 2 (triangle 3 0.8))
            0)))

(comment
(hit (now) triangle-test)
(reset)
)

(defsynth sine-test 
  (out.ar 0 
          (mul-add.ar 
          (wand 67)
            (env-gen.kr 1 1 0 1 2 (sine 3 0.8))
            0)))
(comment
(hit (now) sine-test)
(reset)
)

(defsynth perc-test 
  (out.ar 0 
          (mul-add.ar 
            (wand 62)
            (env-gen.kr 1 1 0 1 2 (perc))
            0)))
(comment
(hit (now) perc-test)
(reset)
)
;(def m (metronome 105))
;(defn bouncer []
;  (hit (now) perc-test)
;  (callback (+ (now) (m)) #'bouncer))
;
(comment 
(bouncer)
  )

;(defn beater []
;  (hit (now) soft-kick)
;  (callback (+ (now) (m)) #'beater))
;
(comment 
(beater)
(reset)
  )


(defsynth linen-test 
  (out.ar 0 
          (mul-add.ar 
            (wand 62)
            (env-gen.kr 1 1 0 1 2 (linen))
            0)))
(comment
(def l (hit (now) linen-test))
(reset)
)

;; TODO: Debug this one...  It should drop off from level to 0.
(defsynth cutoff-test 
  (out.ar 0 
          (mul-add.ar 
            (wand 67)
            (env-gen.kr 1 1 0 1 2 (cutoff))
            0)))
(comment
(def l (hit (now) cutoff-test))
(node-free l)
(reset)
)

(defsynth dadsr-test 
  (out.ar 0 (pan2.ar 
              (mul-add.ar 
                (wand 56)
                (env-gen.kr (sin-osc.ar 1) (dadsr))
                0)
              0)))
(comment
(hit (now) dadsr-test)
(reset)
)

(defsynth adsr-test 
  (out.ar 0 (pan2.ar 
              (mul-add.ar 
                (wand 49)
                (env-gen.kr (sin-osc.ar 1) (adsr))
                0)
              0)))
(comment
(hit (now) adsr-test)
(reset)
)

(defsynth asr-test 
  (out.ar 0 (pan2.ar 
              (mul-add.ar 
                (wand 83)
                (env-gen.kr (sin-osc.ar 1) (asr))
                0)
              0)))
(comment
(hit (now) asr-test)
(reset)
)

;; Dynamic processing (compression, limiting, expanding...)

(defn basic-sound []
  (syn
    (mul-add.ar 
      (decay2.ar (mul-add.ar
                 (impulse.ar 8 0) 
                 (mul-add.kr (lf-saw.kr 0.3 0) -0.3 0.3) 
                 0)
               0.001) 
      0.3 (mix (pulse.ar 80 0.3) (pulse.ar 81 0.3)))))

(defsynth basic-synth
  (quick (basic-sound)))

(defsynth compressed-synth 
  (let [z (basic-sound)
        ctl (ctl-kr :attack  0.01 
                    :release 0.01)]
    (quick 
      (compander.ar z z
                    (mouse-x.kr 0.1 1)
                    1 0.5 ; slope below and above the knee
                    (:attack ctl); clamp time (attack)
                    (:release ctl))))) ; relax time (release)

;// compressor
;play({
;	var z;
;	z = Decay2.ar(
;		Impulse.ar(8, 0,LFSaw.kr(0.3, 0, -0.3, 0.3)), 
;		0.001, 0.3, Mix.ar(Pulse.ar([80,81], 0.3)));
;	Compander.ar(z, z,
;		thresh: MouseX.kr(0.1, 1),
;		slopeBelow: 1,
;		slopeAbove: 0.5,
;		clampTime: 0.01,
;		relaxTime: 0.01
;	);
;})
;)
;// noise gate
;play({
;	var z;
;	z = Decay2.ar(
;		Impulse.ar(8, 0,LFSaw.kr(0.3, 0, -0.3, 0.3)), 
;		0.001, 0.3, Mix.ar(Pulse.ar([80,81], 0.3)));
;	Compander.ar(z, z,
;		thresh: MouseX.kr(0.1, 1),
;		slopeBelow: 10,
;		slopeAbove: 1,
;		clampTime: 0.01,
;		relaxTime: 0.01
;	);
;})
;)
;
;
;(
;// limiter
;play({
;	var z;
;	z = Decay2.ar(
;		Impulse.ar(8, 0,LFSaw.kr(0.3, 0, -0.3, 0.3)), 
;		0.001, 0.3, Mix.ar(Pulse.ar([80,81], 0.3)));
;	Compander.ar(z, z,
;		thresh: MouseX.kr(0.1, 1),
;		slopeBelow: 1,
;		slopeAbove: 0.1,
;		clampTime: 0.01,
;		relaxTime: 0.01
;	);
;})
;)
;
;
;(
;// sustainer
;play({
;	var z;
;	z = Decay2.ar(
;		Impulse.ar(8, 0,LFSaw.kr(0.3, 0, -0.3, 0.3)), 
;		0.001, 0.3, Mix.ar(Pulse.ar([80,81], 0.3)));
;	Compander.ar(z, z,
;		thresh: MouseX.kr(0.1, 1),
;		slopeBelow: 0.1,
;		slopeAbove: 1,
;		clampTime: 0.01,
;		relaxTime: 0.01
;	)*0.1;
;})
;)


;; Audio Input from Jack inputs starts at 8 & 9
(defsynth audio-in-test
  (out.ar 0 [(in.ar 8)
             (delayc.ar (in.ar 9) ;; Delaying right channel half a second
                        3 0.5)]))

 (comment
(hit (now) audio-in-test)
(reset)
   )

;; Playing audio samples (wav files) from disk
;(def buf (sample "/home/rosejn/projects/overtone/samples/kit/boom.wav"))
;(defsynth audio-sample-test
;  (out.ar 0
;          (play-buf.ar (.getBufNum buf)
;                       1.0
;                       (sin-osc.ar 2)
;                       0.0
;                       1.0)))
;
 (comment
(hit (now) audio-sample-test)
(reset)
   )

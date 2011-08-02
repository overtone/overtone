(ns overtone.inst.drum
  (:use overtone.core))

(definst kick [freq 80 mod-freq 5 mod-index 5
                sustain 0.4 amp 0.8 noise 0.025]
  (let [pitch-contour (line:kr (* 2 freq) freq 0.02)
        drum (lpf (sin-osc pitch-contour (sin-osc mod-freq (/ mod-index 1.3))) 1000)
        drum-env (env-gen (perc 0.005 sustain) :action FREE
        hit (hpf (* noise (white-noise)) 500)
        hit (lpf hit (line 6000 500 0.03))
        hit-env (env-gen (perc))]
    (* amp (+ (* drum drum-env) (* hit hit-env)))))

(comment defsynth kick [out 0 ffreq 80 attack 0 release 2 amp 0.1 pan 0]
  (let [snd (apply + (sin-osc [ffreq (* 1.01 ffreq) (* ffreq 1.03)
                      (* ffreq 1.06) (* ffreq 1.1) 0 0.5]))
        snd (+ snd (pink-noise))
        snd (reduce (fn [mem v]
                      (rlpf mem (* ffreq v) (* 0.1 v)))
                    snd (range 1 6))
        snd (+ snd (lpf (white-noise) (* ffreq 6)))
        env (env-gen (perc attack release 1 -50) :action FREE
    (offset-out out (pan2 (* snd amp env) 0))))

(definst small-hat [ffreq 200 rq 0.5
                    attack 0 release 0.025 amp 0.3
                    pan 0]
  (let [snd (white-noise)
        snd (hpf snd ffreq)
        snd (rhpf snd (* ffreq 2) rq)
        snd (* snd (env-gen (perc attack release 1 -10) :action FREE
    (* 2 snd amp)))

(definst c-hat [amp 0.3 t 0.07]
  (let [env (env-gen (perc 0.001 t) 1 1 0 1 FREE
        noise (white-noise)
        sqr (* (env-gen (perc 0.01 0.04)) (pulse 880 0.2))
        filt (bpf (+ sqr noise) 9000 0.5)]
    (* amp env filt)))

(definst o-hat [amp 0.4 t 0.3 low 6000 hi 2000]
  (let [low (lpf (white-noise) low)
        hi (hpf low hi)
        env (line 1 0 t :action FREE
    (* env hi)))

(definst o-hat-demo [amp 0.4 t 0.3 low 6000 hi 2000]
  (let [low (lpf (white-noise) (mouse-x 100 20000))
        hi (hpf low (mouse-y 20 20000))
        env (line 1 0 t :action FREE
    (* env hi)))

(definst round-kick [amp 0.5 decay 0.6 freq 65]
  (let [env (env-gen (perc 0 decay) :action FREE
        snd (* amp (sin-osc freq (* Math/PI 0.5)))]
    (* snd env)))

(definst snare [freq 405 amp 0.2 sustain 0.1
                drum-amp 0.25 crackle-amp 40 tightness 1000]
  (let [drum-env (* 0.5 (env-gen (perc 0.005 sustain) :action FREE
        drum-s1 (* drum-env (sin-osc freq))
        drum-s2 (* drum-env (sin-osc (* freq 0.53)))
        drum-s3 (* drum-env (sin-osc (saw (* freq 0.85))
                                     (sin-osc 184)))
        drum (* drum-amp (+ drum-s1 drum-s2 drum-s3))
        noise (lf-noise0 20000 0.1)
        filtered (* 0.5 (brf noise 8000 0.1))
        filtered (* 0.5 (brf filtered 5000 0.1))
        filtered (* 0.5 (brf filtered 3600 0.1))
        filtered (* (env-gen (perc 0.005 sustain) :action FREE
                    (brf filtered 2000 0.0001))
        crackle (* (resonz filtered tightness) crackle-amp)]
    (* amp (* (+ drum crackle)))))

(definst snare2 [amp 0.5 decay 0.1 freq 1000]
  (let [env (env-gen (perc 0 decay) :action FREE
        snd (rlpf (* (gray-noise) amp) freq (line 0.1 0.9 decay))]
    (* snd env)))

(defsynth tom [amp 0.2 sustain 0.4 mode-level 0.25 freq 90 timbre 1]
  (let [env (env-gen (perc 0.005 sustain) :action FREE
        s1 (* 0.5 env (sin-osc (* freq 0.8)))
        s2 (* 0.5 env (sin-osc freq))
        s3 (* 5 env (sin-osc (saw (* 0.9 freq))
                             (* (sin-osc (* freq 0.85))
                                (/ timbre 1.3))))
        mix (* mode-level (+ s1 s2 s3))
        stick (* 3 (env-gen (perc 0.005 0.01))
                 (crackle 2.01))
        mix2 (* amp (+ mix stick))]
    (out 0 (pan2 mix2))))

; //variation with more sines
; SynthDef("dry-kick", {|amp= 0.5, decay= 0.1, freq= 60|
;         var env, snd;
;         env= EnvGen.ar(Env.perc(0, decay), doneAction:2);
;         snd= Mix(SinOsc.ar([freq, freq*2, freq-15], 0, amp));
;         Out.ar(0, Pan2.ar(snd*env, 0));
;         }).store;
; Synth("dry-kick")
;
; SynthDef("hat",
; 	{arg out = 0, freq = 6000, sustain = 0.1, amp = 0.8;
; 	var root_cymbal, root_cymbal_square, root_cymbal_pmosc;
; 	var initial_bpf_contour, initial_bpf, initial_env;
; 	var body_hpf, body_env;
; 	var cymbal_mix;
;
; 	root_cymbal_square = Pulse.ar(freq, 0.5, mul: 1);
; 	root_cymbal_pmosc = PMOsc.ar(root_cymbal_square,
; 					[freq*1.34, freq*2.405, freq*3.09, freq*1.309],
; 					[310/1.3, 26/0.5, 11/3.4, 0.72772],
; 					mul: 1,
; 					add: 0);
; 	root_cymbal = Mix.new(root_cymbal_pmosc);
; 	initial_bpf_contour = Line.kr(15000, 9000, 0.1);
; 	initial_env = EnvGen.ar(Env.perc(0.005, 0.1), 1.0);
; 	initial_bpf = BPF.ar(root_cymbal, initial_bpf_contour, mul:initial_env);
; 	body_env = EnvGen.ar(Env.perc(0.005, sustain, 1, -2), 1.0, doneAction: 2);
; 	body_hpf = HPF.ar(in: root_cymbal, freq: Line.kr(9000, 12000, sustain),mul: body_env, add: 0);
; 	cymbal_mix = Mix.new([initial_bpf, body_hpf]) * amp;
; 	Out.ar(out, [cymbal_mix, cymbal_mix])
; 	}).store
; Synth("hat")
;
; SynthDef("soft-hat", {|amp= 0.5, decay= 0.1, freq= 6000|
;             var env, snd;
;             env= EnvGen.ar(Env.perc(0, decay), doneAction:2);
;             snd= BPF.ar(GrayNoise.ar(amp), freq, 0.3);
;             Out.ar(0, Pan2.ar(snd*env, 0));
;             }).store;
; Synth("soft-hat")
;
; //variation with cutoff lfo
; SynthDef("noise-hat", {|amp= 0.5, decay= 0.1, freq= 6000|
;             var env, snd;
;             env= EnvGen.ar(Env.perc(0, decay), doneAction:2);
;             snd= BPF.ar(GrayNoise.ar(amp), Line.ar(freq, 50, decay*0.5)
;
;                 , env+0.1);
;             Out.ar(0, Pan2.ar(snd*env, 0));
;             }).store;
; Synth("noise-hat")
;
; SynthDef("bell-hat", {|amp= 0.5, decay= 0.1, freq= 6000|
;             var env, snd, snd2;
;             env= EnvGen.ar(Env.perc(0, decay), doneAction:2);
;             snd= BPF.ar(GrayNoise.ar(amp), Line.ar(freq, 5, decay*0.5)
;
;                 , env+0.1);
;             snd2= Mix(SinOsc.ar([4000, 6500, 5000], 0,  env*0.1));
;             Out.ar(0, Pan2.ar(snd+snd2*env, 0));
;             }).store;
; Synth("bell-hat")
;
; SynthDef("snare",
; 	{arg out = 0, sustain = 0.1, drum_mode_level = 0.25,
; 	snare_level = 40, snare_tightness = 1000,
; 	freq = 405, amp = 0.8;
; 	var drum_mode_sin_1, drum_mode_sin_2, drum_mode_pmosc, drum_mode_mix, drum_mode_env;
; 	var snare_noise, snare_brf_1, snare_brf_2, snare_brf_3, snare_brf_4, snare_reson;
; 	var snare_env;
; 	var snare_drum_mix;
;
; 	drum_mode_env = EnvGen.ar(Env.perc(0.005, sustain), 1.0, doneAction: 2);
; 	drum_mode_sin_1 = SinOsc.ar(freq*0.53, 0, drum_mode_env * 0.5);
; 	drum_mode_sin_2 = SinOsc.ar(freq, 0, drum_mode_env * 0.5);
; 	drum_mode_pmosc = PMOsc.ar(	Saw.ar(freq*0.85),
; 					184,
; 					0.5/1.3,
; 					mul: drum_mode_env*5,
; 					add: 0);
; 	drum_mode_mix = Mix.new([drum_mode_sin_1, drum_mode_sin_2, drum_mode_pmosc]) * drum_mode_level;
;
; // choose either noise source below
; //	snare_noise = Crackle.ar(2.01, 1);
; 	snare_noise = LFNoise0.ar(20000, 0.1);
; 	snare_env = EnvGen.ar(Env.perc(0.005, sustain), 1.0, doneAction: 2);
; 	snare_brf_1 = BRF.ar(in: snare_noise, freq: 8000, mul: 0.5, rq: 0.1);
; 	snare_brf_2 = BRF.ar(in: snare_brf_1, freq: 5000, mul: 0.5, rq: 0.1);
; 	snare_brf_3 = BRF.ar(in: snare_brf_2, freq: 3600, mul: 0.5, rq: 0.1);
; 	snare_brf_4 = BRF.ar(in: snare_brf_3, freq: 2000, mul: snare_env, rq: 0.0001);
; 	snare_reson = Resonz.ar(snare_brf_4, snare_tightness, mul: snare_level) ;
; 	snare_drum_mix = Mix.new([drum_mode_mix, snare_reson]) * 5 * amp;
; 	Out.ar(out, [snare_drum_mix, snare_drum_mix])
; 	}).store
; Synth("snare")
;
; SynthDef("soft-snare", {|amp= 0.5, decay= 0.1, freq= 1000|
;             var env, snd;
;             env= EnvGen.ar(Env.perc(0, decay), doneAction:2);
;             snd= BPF.ar(GrayNoise.ar(amp), freq, 3);
;             Out.ar(0, Pan2.ar(snd*env, 0));
;             }).store;
; Synth("soft-snare")
;
; SynthDef("snare2", {|amp= 0.5, decay= 0.1, freq= 1000|
;             var env, snd;
;             env= EnvGen.ar(Env.perc(0, decay), doneAction:2);
;             snd= RLPF.ar(GrayNoise.ar(amp), freq, Line.ar(0.1, 0.9, decay));
;             Out.ar(0, Pan2.ar(snd*env, 0));
;             }).store;
; Synth("snare2")

; SynthDef("snare3", {|amp= 0.5, decay= 0.1, freq= 1000|
;             var env, snd, env2, snd2;
;             env= EnvGen.ar(Env.perc(0, decay), doneAction:2);
;             env2= EnvGen.ar(Env.perc(0, decay), doneAction:2);
;             snd= RLPF.ar(GrayNoise.ar(amp), freq, Line.ar(0.1, 0.9, decay));
;             snd2= WhiteNoise.ar(amp)*env2;
;             Out.ar(0, Pan2.ar(snd*env, 0));
;             }).store;
; Synth("snare3")
;
; // Some more abstract 'snares'
; SynthDef("snare-x", { |freq=100, sustain=5, amp=0.1|
;         var mod, sound, env, saw, filter;
;         //mod = XLine.kr(freq*1,freq,sustain); von freq nach freq
;         env = EnvGen.ar(Env.perc(0.04, sustain,amp*10,-60),doneAction:2); //amp*10 statt 1
;         saw = WhiteNoise.ar(0.06 * [1,1]) * Saw.ar(10 * [1,1]).sum; //sum?
;         filter = BPF.ar(saw, LFNoise1.kr(0.1).exprange(5000, 20000), 0.9);
;         //sound = SinOsc.ar(mod,0,amp.dup); keine amp
;         Out.ar(0, filter * env);
;         };
;         ).load(s);
; Synth("snare-x")
;
; SynthDef("snare-y", { |sustain=3, amp=0.5, freq=20, hard=0|
;         var sound, env, env2;
;         env = EnvGen.ar(Env.perc(0.025, sustain*(1-hard), 1, -12));
;         env2 = EnvGen.ar(Env.perc(0.05, sustain*1.5), doneAction: 2);
;         sound = Decay.ar(Impulse.ar(0), 0.00072, WhiteNoise.ar(amp.dup)) + WhiteNoise.ar(0.1).dup;
;         sound = sound * env;
;         sound = CombL.ar(sound, freq.reciprocal, freq.reciprocal, sustain);
;         Out.ar(0, sound * env2);
;         };
;         ).load(s);
; Synth("snare-y")
;
; SynthDef("snare-z", { |sustain=3, amp=0.5, freq=20, hard=0|
;         var sound, env, env2;
;         env = EnvGen.ar(Env.perc(0, sustain*(1-hard), 1, -12));
;         env2 = EnvGen.ar(Env.perc(0, sustain*1.5), doneAction: 2);
;         sound = Decay.ar(Impulse.ar(0), sustain * 0.1, GrayNoise.ar([1, 1] * 0.3), WhiteNoise.ar(0.1.dup) + 0.3);
;
;         sound = sound * (LFSaw.ar(Rand(5, 10)).max(0) + 1) * (env * amp);
;         Out.ar(0, sound * env2);
;         };
;         ).load(s);
; Synth("snare-z")

; SynthDef("clap", { |freq=100, sustain=5, amp=0.9|
;         var mod, sound, env, saw, filter;
;         mod = XLine.kr(freq*1,freq,sustain);
;         env = EnvGen.ar(Env.perc(0.02, sustain,1,-200),doneAction:2);
;         saw = LFPulse.kr(0.6,0,0.5) * WhiteNoise.ar(0.07 * [1,1]) * Saw.ar(1000 * [1,1]).sum;
;         filter = BPF.ar(saw, LFNoise1.kr(0.1).exprange(10000, 20000), 0.9);
;
;         sound = SinOsc.ar(mod,0,amp.dup);
;         Out.ar(0, filter * env);
;         };
; ).load(s);
; Synth("clap");
; Synth("clap", ["freq", 300, "amp", 1.5]);

; TODO: figure this one out... :-)
(defsynth clap [freq 100 sustain 5 amp 0.9]
  (let [env (env-gen (perc 0.02 sustain) :action FREE
        saw (apply +
                   (* (lf-pulse:kr 0.6 0 0.5)
               (* 0.07 [(white-noise) (white-noise)])
               [(saw 1000) (saw 1000)]))
        filt (bpf saw (lin-exp (lf-noise1:kr 0.1)
                               -1 1 10000
                               20000)
                  0.9)]
    (out 0 (pan2 (* filt env)))))

(ns overtone.inst.drum
  (:use [overtone.sc ugens envelope synth]
        [overtone.sc.cgens mix oscillators]
        [overtone.studio mixer inst]))

;;; Kick Drums

(definst kick
  [freq       {:default 50 :min 40 :max 140 :step 1}
   env-ratio  {:default 3 :min 1.2 :max 8.0 :step 0.1}
   freq-decay {:default 0.02 :min 0.001 :max 1.0 :step 0.001}
   amp-decay  {:default 0.5 :min 0.001 :max 1.0 :step 0.001}]
  (let [fenv (* (env-gen (envelope [env-ratio 1] [freq-decay] :exp)) freq)
        aenv (env-gen (perc 0.005 amp-decay) :action FREE)]
    (* (sin-osc fenv (* 0.5 Math/PI)) aenv)))

(definst kick2 [freq      {:default 80 :min 10 :max 20000 :step 1}
                amp       {:default 0.8 :min 0.001 :max 1.0 :step 0.001}
                mod-freq  {:default 5 :min 0.001 :max 10.0 :step 0.01}
                mod-index {:default 5 :min 0.001 :max 10.0 :step 0.01}
                sustain   {:default 0.4 :min 0.001 :max 1.0 :step 0.001}
                noise     {:default 0.025 :min 0.001 :max 1.0 :step 0.001}]
  (let [pitch-contour (line:kr (* 2 freq) freq 0.02)
        drum (lpf (sin-osc pitch-contour (sin-osc mod-freq (/ mod-index 1.3))) 1000)
        drum-env (env-gen (perc 0.005 sustain) :action FREE)
        hit (hpf (* noise (white-noise)) 500)
        hit (lpf hit (line 6000 500 0.03))
        hit-env (env-gen (perc))]
    (* amp (+ (* drum drum-env) (* hit hit-env)))))

(definst kick3
  [freq {:default 80 :min 40 :max 140 :step 1}
   amp {:default 0.3 :min 0.001 :max 1 :step 0.001}]
  (let [sub-osc   (sin-osc freq)
        sub-env   (line 1 0 0.7 FREE)
        click-osc  (lpf (white-noise) 1500)
        click-env  (line 1 0 0.02)
        sub-out   (* sub-osc sub-env)
        click-out (* click-osc click-env)]
    (* amp (+ sub-out click-out))))

(definst kick4
  [freq   {:default 80 :min 40 :max 140 :step 1}
   amp    {:default 0.3 :min 0.001 :max 1 :step 0.001}
   attack {:default 0.001 :min 0.001 :max 1.0 :step 0.001}
   decay  {:default 0.4 :min 0.001 :max 1 :step 0.001}]
  (let [env (env-gen (perc attack decay) :action FREE)
        snd (sin-osc freq (* Math/PI 0.5))
        snd (* amp env snd)]
    snd))

(definst dub-kick
  [freq   {:default 80 :min 40 :max 140 :step 1}]
  (let [cutoff-env (perc 0.001 1 freq -20)
        amp-env (perc 0.001 1 1 -8)
        osc-env (perc 0.001 1 freq -8)
        noiz (lpf (white-noise) (+ (env-gen:kr cutoff-env) 20))
        snd  (lpf (sin-osc (+ (env-gen:kr osc-env) 20)) 200)
        mixed (* (+ noiz snd) (env-gen amp-env :action FREE))]
    mixed))

(definst dance-kick
  [freq {:default 50.24 :min 20 :max 400 :step 1}
   attack {:default 0.0001 :min 0.00001 :max 2 :step 0.0001}
   decay  {:default 0.484 :min 0.00001 :max 2 :step 0.0001}
   fattack {:default 0.0001 :min 0.00001 :max 2 :step 0.0001}
   fdecay {:default 0.012 :min 0.00001 :max 2 :step 0.0001}
   amp {:default 0.8 :min 0.01 :max 1 :step 0.01}]
  (let [freq-env (env-gen:kr (perc fattack fdecay))
        wave (sin-osc (+ freq (* 8 freq freq-env)))
        env  (env-gen:kr (perc attack decay) :action FREE)
        src (* env wave)
        dist (clip2 (* 2 (tanh (* 3 (distort (* 1.5 src))))) 0.8)
        eq (b-peak-eq dist 37.67 1 10.4)]
    (* amp eq)))

(definst dry-kick
  [freq   {:default 60 :min 40 :max 140 :step 1}
   amp    {:default 0.3 :min 0.001 :max 1 :step 0.01}
   attack {:default 0.001 :min 0.001 :max 1.0 :step 0.0001}
   decay  {:default 0.2 :min 0.001 :max 1 :step 0.001}]
  (let [env (env-gen (perc attack decay) :action FREE)
        snd (mix (sin-osc [freq (* 2 freq) (- freq 15)] (* Math/PI 0.5)))
        snd (* amp env snd)]
    snd))

(definst quick-kick
  [freq {:default 20.0 :min 20 :max 400 :step 1}
   attack {:default 0.0001 :min 0.00001 :max 2 :step 0.0001}
   decay  {:default 0.374 :min 0.00001 :max 2 :step 0.0001}
   fattack {:default 0.001 :min 0.00001 :max 2 :step 0.0001}
   fdecay {:default 0.282 :min 0.00001 :max 2 :step 0.0001}
   amp {:default 0.8 :min 0.01 :max 1 :step 0.01}]
  (let [freq-env (env-gen:kr (perc fattack fdecay))
        wave (sin-osc (+ (* 0.5 freq) (* 14 freq freq-env)))
        env  (x-line:kr 1 0 decay :action FREE)
        src (* env wave)
        dist (clip2 (* 2 (tanh (* 3 (distort (* 1.5 src))))) 0.8)
        eq (b-peak-eq dist 57.41 1 44)]
    (* amp eq)))

;; Hi-hats

(definst open-hat
  [amp    {:default 0.3 :min 0.001 :max 1 :step 0.01}
   t      {:default 0.3 :min 0.1 :max 1.0 :step 0.01}
   low    {:default 6000 :min 3000 :max 12000 :step 1}
   hi     {:default 2000 :min 1000 :max 8000 :step 1}]
  (let [low (lpf (white-noise) low)
        hi (hpf low hi)
        env (line 1 0 t :action FREE)]
    (* amp env hi)))

(definst closed-hat
  [amp    {:default 0.3 :min 0.001 :max 1 :step 0.01}
   t      {:default 0.1 :min 0.1 :max 1.0 :step 0.01}
   low    {:default 6000 :min 3000 :max 12000 :step 1}
   hi     {:default 2000 :min 1000 :max 8000 :step 1}]
  (let [low (lpf (white-noise) low)
        hi (hpf low hi)
        env (line 1 0 t :action FREE)]
    (* amp env hi)))

(definst hat-demo
  [amp    {:default 0.3 :min 0.001 :max 1 :step 0.01}
   t      {:default 0.3 :min 0.1 :max 1.0 :step 0.01}]
  (let [low (lpf (white-noise) (mouse-x 3000 12000))
        hi (hpf low (mouse-y 1000 8000))
        env (line 1 0 t :action FREE)]
    (* amp env hi)))

(definst closed-hat2
  [amp    {:default 0.3 :min 0.001 :max 1.0 :step 0.001}
   attack {:default 0.001 :min 0.001 :max 1.0 :step 0.0001}
   decay  {:default 0.07 :min 0.001 :max 1.0 :step 0.001}]
  (let [env (env-gen (perc attack decay) 1 1 0 1 FREE)
        noise (white-noise)
        sqr (* (env-gen (perc 0.01 0.04)) (pulse 880 0.2))
        filt (bpf (+ sqr noise) 9000 0.5)]
    (* 0.5 amp env filt)))

(definst hat3
  [amp    {:default 0.3 :min 0.001 :max 1 :step 0.01}
   t      {:default 0.1 :min 0.1 :max 1.0 :step 0.01}
   low    {:default 6000 :min 3000 :max 12000 :step 1}
   hi     {:default 2000 :min 1000 :max 8000 :step 1}]
  (let [low (lpf (white-noise) low)
        hi (hpf low hi)
        env (line 1 0 t :action FREE)]
    (* amp env hi)))
; (hat3 :t 0.1) => closed
; (hat3 :t 0.3) => open

(definst soft-hat
  [freq   {:default 6000 :min 3000 :max 12000 :step 1}
   amp    {:default 0.3 :min 0.001 :max 1 :step 0.01}
   attack {:default 0.0001 :min 0.0001 :max 1.0 :step 0.01}
   decay  {:default 0.1 :min 0.1 :max 1.0 :step 0.01}]
  (let [env (env-gen (perc attack decay) :action FREE)
        noiz (bpf (* amp (gray-noise)) freq 0.3)
        snd (* noiz env)]
    snd))

(comment
  ; there is something wrong with these two...

(definst noise-hat
  [freq   {:default 6000 :min 3000 :max 12000 :step 1}
   amp    {:default 0.3 :min 0.001 :max 1 :step 0.01}
   attack {:default 0.0001 :min 0.1 :max 1.0 :step 0.01}
   decay  {:default 0.1 :min 0.1 :max 1.0 :step 0.01}]
  (let [env (env-gen (perc attack decay) :action FREE)
        noiz (bpf (* amp (gray-noise))
                  (line freq 50 (* decay 0.5))
                  (* env 0.1))
        snd (* noiz env)]
    snd))

(definst bell-hat
  [freq   {:default 6000 :min 3000 :max 12000 :step 1}
   amp    {:default 0.3 :min 0.001 :max 1 :step 0.01}
   attack {:default 0.0001 :min 0.1 :max 1.0 :step 0.01}
   decay  {:default 0.1 :min 0.1 :max 1.0 :step 0.01}]
  (let [env (env-gen (perc attack decay) :action FREE)
        noiz (bpf (* amp (gray-noise)) (line freq 5 (* decay 0.5)) (+ env 0.1))
        wave (* 0.1 env (mix (sin-osc [4000 6500 5000])))
        snd (+ noiz wave)]
    snd))
  )

; SynthDef("hat",
;       {arg out = 0, freq = 6000, sustain = 0.1, amp = 0.8;
;       var root_cymbal, root_cymbal_square, root_cymbal_pmosc;
;       var initial_bpf_contour, initial_bpf, initial_env;
;       var body_hpf, body_env;
;       var cymbal_mix;
;
;       root_cymbal_square = Pulse.ar(freq, 0.5, mul: 1);
;       root_cymbal_pmosc = PMOsc.ar(root_cymbal_square,
;                                       [freq*1.34, freq*2.405, freq*3.09, freq*1.309],
;                                       [310/1.3, 26/0.5, 11/3.4, 0.72772],
;                                       mul: 1,
;                                       add: 0);
;       root_cymbal = Mix.new(root_cymbal_pmosc);
;       initial_bpf_contour = Line.kr(15000, 9000, 0.1);
;       initial_env = EnvGen.ar(Env.perc(0.005, 0.1), 1.0);
;       initial_bpf = BPF.ar(root_cymbal, initial_bpf_contour, mul:initial_env);
;       body_env = EnvGen.ar(Env.perc(0.005, sustain, 1, -2), 1.0, doneAction: 2);
;       body_hpf = HPF.ar(in: root_cymbal, freq: Line.kr(9000, 12000, sustain),mul: body_env, add: 0);
;       cymbal_mix = Mix.new([initial_bpf, body_hpf]) * amp;
;       Out.ar(out, [cymbal_mix, cymbal_mix])
;       }).store
; Synth("hat")

;; Snares

(definst snare
  [freq   {:default 405 :min 100 :max 1000 :step 1}
   amp    {:default 0.3 :min 0.001 :max 1 :step 0.01}
   sustain {:default 0.1 :min 0.01 :max 1.0 :step 0.001}
   decay  {:default 0.1 :min 0.1 :max 1.0 :step 0.01}
   drum-amp 0.25
   crackle-amp 40
   tightness 1000]
  (let [drum-env  (env-gen (perc 0.005 sustain) :action FREE)
        drum-osc  (mix (* drum-env (sin-osc [freq (* freq 0.53)])))
        drum-s3   (* drum-env (pm-osc (saw (* freq 0.85)) 184 (/ 0.5 1.3)))
        drum      (* drum-amp (+ drum-s3 drum-osc))
        noise     (* 0.1 (lf-noise0 20000))
        noise-env (env-gen (perc 0.005 sustain) :action FREE)
        filtered  (* 0.5 (brf noise 8000 0.1))
        filtered  (* 0.5 (brf filtered 5000 0.1))
        filtered  (* 0.5 (brf filtered 3600 0.1))
        filtered  (* (brf filtered 2000 0.0001) noise-env)
        resonance (* (resonz filtered tightness) crackle-amp)]
    (* amp (+ drum resonance))))

(definst snare2
  [freq   {:default 261.62 :min 100 :max 10000 :step 1}
   amp    {:default 0.3 :min 0.001 :max 1 :step 0.01}
   decay  {:default 0.081 :min 0.01 :max 1.0 :step 0.01}]
  (let [snd-env (env-gen (perc 0.001 decay))
        snd-env-b (env-gen (perc 0.001 (* decay 0.28)))
        snd (* 0.1 (lpf (square (- freq (* freq snd-env 0.4) (* freq snd-env-b 0.05))) (* 2.5 freq)))
        amp-env (env-gen (perc 0.001 (+ decay 0.036)) :action FREE)
        noise (* 0.2 amp-env (pink-noise))
        snd (rlpf (* amp-env (+ snd noise)) 10567 0.2)]
    snd))

(definst noise-snare
  [freq   {:default 1000 :min 100 :max 10000 :step 1}
   amp    {:default 0.3 :min 0.001 :max 1 :step 0.01}
   decay  {:default 0.1 :min 0.1 :max 1.0 :step 0.01}]
  (let [env (env-gen (perc 0 decay) :action FREE)
        snd (bpf (gray-noise) freq 3)]
    (* snd env amp)))

(definst tone-snare
  [freq   {:default 1000 :min 100 :max 10000 :step 1}
   amp    {:default 0.3 :min 0.001 :max 1 :step 0.01}]
  (let [filterenv (line 1 0 0.2)
        amp-env   (line 1 0 0.6 :action FREE)
        snd       (pulse 100)
        snd       (lpf snd (+ (* filterenv freq) 30))
        snap-env  (line 1 0 0.2)
        snap-osc  (bpf (hpf (white-noise) 500) 1500)]
    (* amp (+ (* snd amp-env)
              (* snap-env snap-osc)))))

; SynthDef("snare-x", { |freq=100, sustain=5, amp=0.1|
;         var mod, sound, env, saw, filter;
;         //mod = XLine.kr(freq*1,freq,sustain); von freq nach freq
;         env = EnvGen.ar(Env.perc(0.04, sustain,amp*10,-60),doneAction:2); //amp*10 statt 1
;         saw = WhiteNoise.ar(0.06 * [1,1]) * Saw.ar(10 * [1,1]).sum; //sum?
;         filter = BPF.ar(saw, LFNoise1.kr(0.1).exprange(5000, 20000), 0.9);
;         //sound = SinOsc.ar(mod,0,amp.dup); keine amp
;         Out.ar(0, filter * env);

; SynthDef("snare-y", { |sustain=3, amp=0.5, freq=20, hard=0|
;         var sound, env, env2;
;         env = EnvGen.ar(Env.perc(0.025, sustain*(1-hard), 1, -12));
;         env2 = EnvGen.ar(Env.perc(0.05, sustain*1.5), doneAction: 2);
;         sound = Decay.ar(Impulse.ar(0), 0.00072, WhiteNoise.ar(amp.dup)) + WhiteNoise.ar(0.1).dup;
;         sound = sound * env;
;         sound = CombL.ar(sound, freq.reciprocal, freq.reciprocal, sustain);
;         Out.ar(0, sound * env2);
;
; SynthDef("snare-z", { |sustain=3, amp=0.5, freq=20, hard=0|
;         var sound, env, env2;
;         env = EnvGen.ar(Env.perc(0, sustain*(1-hard), 1, -12));
;         env2 = EnvGen.ar(Env.perc(0, sustain*1.5), doneAction: 2);
;         sound = Decay.ar(Impulse.ar(0), sustain * 0.1, GrayNoise.ar([1, 1] * 0.3), WhiteNoise.ar(0.1.dup) + 0.3);
;
;         sound = sound * (LFSaw.ar(Rand(5, 10)).max(0) + 1) * (env * amp);
;         Out.ar(0, sound * env2);

;; Toms

(definst tom
  [freq {:default 90 :min 50 :max 400 :step 1}
   amp {:default 0.3 :min 0.001 :max 1 :step 0.01}
   sustain {:default 0.4 :min 0.01 :max 1.0 :step 0.001}
   mode-level {:default 0.25 :min 0.01 :max 1.0 :step 0.001}
   timbre {:default 1 :min 0.1 :max 5.0 :step 0.1}
   stick-level {:default 0.2 :min 0.0 :max 1.0 :step 0.1}]
  (let [env (env-gen (perc 0.005 sustain) :action FREE)
        s1 (* 0.5 env (sin-osc (* freq 0.8)))
        s2 (* 0.5 env (sin-osc freq))
        s3 (* 5 env (sin-osc (saw (* 0.9 freq))
                             (* (sin-osc (* freq 0.85))
                                (/ timbre 1.3))))
        mix (* mode-level (+ s1 s2 s3))
        stick (* stick-level
                 (env-gen (perc 0.001 0.01))
                 (crackle 2.0))
        mix2 (* amp (+ mix stick))]
    mix2))

;; Percussive elements

(definst clap
  [low {:default 7500 :min 100 :max 10000 :step 1}
   hi  {:default 1500 :min 100 :max 10000 :step 1}
   amp {:default 0.3 :min 0.001 :max 1 :step 0.01}
   decay {:default 0.6 :min 0.1 :max 0.8 :step 0.001}]
  (let [noise      (bpf (lpf (white-noise) low) hi)
        clap-env   (line 1 0 decay :action FREE)
        noise-envs (map #(envelope [0 0 1 0] [(* % 0.01) 0 0.04]) (range 8))
        claps      (apply + (* noise (map env-gen noise-envs)))]
    (* claps clap-env)))

(definst haziti-clap
  [freq   {:default 44.77 :min 20 :max 400 :step 1}
   attack {:default 0.036 :min 0.00001 :max 2 :step 0.0001}
   decay  {:default 1.884 :min 0.00001 :max 2 :step 0.0001}
   rq     {:default 0.08 :min 0.01 :max 1 :step 0.01}
   amp    {:default 0.8 :min 0.01 :max 1 :step 0.01}]
  (let [noiz (white-noise)
        bfreq (* 400 (abs (lf-noise0 80)))
        filt (* 4 (bpf (rhpf noiz 4064.78 rq) bfreq (* 1 rq)))
        env  (x-line 1 0.001 decay :action FREE)
        wave (lf-tri (* (abs (lf-noise0:kr 699)) 4400))
        wenv (env-gen (perc 0.00001 0.008))
        skip (* wave wenv)]
    (* amp (+ (* env filt) skip))))


(definst bing
  [freq {:default 440 :min 110 :max 880 :step 1}
   amp {:default 0.3 :min 0.001 :max 1 :step 0.01}
   attack {:default 0.001 :min 0.0001 :max 1.0 :step 0.001}
   decay  {:default 0.1 :min 0.001 :max 1.0 :step 0.001}]
  (let [env (env-gen (perc attack decay) :action FREE)
        snd (sin-osc freq)]
    (* amp env snd)))

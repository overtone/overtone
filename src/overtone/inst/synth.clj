(ns overtone.inst.synth
  (:use [overtone.sc ugens envelope]
        [overtone.sc.cgens mix line]
        [overtone.sc.machinery.ugen.fn-gen]
        [overtone.music pitch]
        [overtone.studio mixer inst]
        [overtone.algo.chance :only [ranged-rand]]))

;; translated from: https://github.com/supercollider-quarks/SynthDefPool/blob/master/pool/apad_mh.scd
(definst simple-flute [freq 880
                       amp 0.5
                       attack 0.4
                       decay 0.5
                       sustain 0.8
                       release 1
                       gate 1
                       out 0]
  (let [env  (env-gen (adsr attack decay sustain release) gate :action FREE)
        mod1 (lin-lin:kr (sin-osc:kr 6) -1 1 (* freq 0.99) (* freq 1.01))
        mod2 (lin-lin:kr (lf-noise2:kr 1) -1 1 0.2 1)
        mod3 (lin-lin:kr (sin-osc:kr (ranged-rand 4 6)) -1 1 0.5 1)
        sig (distort (* env (sin-osc [freq mod1])))
        sig (* amp sig mod2 mod3)]
    sig))

;;modified version of: https://github.com/supercollider-quarks/SynthDefPool/blob/master/pool/cs80lead_mh.scd
(definst cs80lead
  [freq 880
   amp 0.5
   att 0.75
   decay 0.5
   sus 0.8
   rel 1.0
   fatt 0.75
   fdecay 0.5
   fsus 0.8
   frel 1.0
   cutoff 200
   dtune 0.002
   vibrate 4
   vibdepth 0.015
   gate 1
   ratio 1
   cbus 1
   freq-lag 0.1]
  (let [freq (lag freq freq-lag)
        cuttoff (in:kr cbus)
        env     (env-gen (adsr att decay sus rel) gate :action FREE)
        fenv    (env-gen (adsr fatt fdecay fsus frel 2) gate)

        vib     (+ 1 (lin-lin:kr (sin-osc:kr vibrate) -1 1 (- vibdepth) vibdepth))

        freq    (* freq vib)
        sig     (mix (* env amp (saw [freq (* freq (+ dtune 1))])))]
    sig))

(definst supersaw [freq 440 amp 1]
  (let [input  (lf-saw freq)
        shift1 (lf-saw 4)
        shift2 (lf-saw 7)
        shift3 (lf-saw 5)
        shift4 (lf-saw 2)
        comp1  (> input shift1)
        comp2  (> input shift2)
        comp3  (> input shift3)
        comp4  (> input shift4)
        output (+ (- input comp1) (- input comp2) (- input comp3) (- input comp4))
        output (- output input)
        output (leak-dc:ar (* output 0.25))]
    (* amp output)))

(definst supersaw [freq 440 amp 1]
  (let [input  (lf-saw freq)
        shift1 (lf-saw 4)
        shift2 (lf-saw 7)
        shift3 (lf-saw 5)
        shift4 (lf-saw 2)
        comp1  (> input shift1)
        comp2  (> input shift2)
        comp3  (> input shift3)
        comp4  (> input shift4)
        output (+ (- input comp1) (- input comp2) (- input comp3) (- input comp4))
        output (- output input)
        output (leak-dc:ar (* output 0.25))]
    (* amp output)))

(definst ticker
  [freq 880]
  (* (env-gen (perc 0.001 0.01) :action FREE)
     (sin-osc freq)))

(definst ping
  [note   {:default 72   :min 0     :max 120 :step 1}
   attack {:default 0.02 :min 0.001 :max 1   :step 0.001}
   decay  {:default 0.3  :min 0.001 :max 1   :step 0.001}]
  (let [snd (sin-osc (midicps note))
        env (env-gen (perc attack decay) :action FREE)]
    (* 0.8 env snd)))

(definst tb303
  [note       {:default 60 :min 0 :max 120 :step 1}
   wave       {:default 1 :min 0 :max 2 :step 1}
   r          {:default 0.8 :min 0.01 :max 0.99 :step 0.01}
   attack     {:default 0.01 :min 0.001 :max 4 :step 0.001}
   decay      {:default 0.1 :min 0.001 :max 4 :step 0.001}
   sustain    {:default 0.6 :min 0.001 :max 0.99 :step 0.001}
   release    {:default 0.01 :min 0.001 :max 4 :step 0.001}
   cutoff     {:default 100 :min 1 :max 20000 :step 1}
   env-amount {:default 0.01 :min 0.001 :max 4 :step 0.001}
   amp        {:default 0.5 :min 0 :max 1 :step 0.01}]
  (let [freq       (midicps note)
        freqs      [freq (* 1.01 freq)]
        vol-env    (env-gen (adsr attack decay sustain release)
                            (line:kr 1 0 (+ attack decay release))
                            :action FREE)
        fil-env    (env-gen (perc))
        fil-cutoff (+ cutoff (* env-amount fil-env))
        waves      (* vol-env
                      [(saw freqs)
                       (pulse freqs 0.5)
                       (lf-tri freqs)])
        selector   (select wave waves)
        filt       (rlpf selector fil-cutoff r)]
    (* amp filt)))

(definst mooger
  "Choose 0, 1, or 2 for saw, sin, or pulse"
  [note {:default 60 :min 0 :max 127 :step 1}
   amp  {:default 0.3 :min 0 :max 1 :step 0.01}
   osc1 {:default 0 :min 0 :max 2 :step 1}
   osc2 {:default 1 :min 0 :max 2 :step 1}
   osc1-level {:default 0.5 :min 0 :max 1 :step 0.01}
   osc2-level {:default 0 :min 0 :max 1 :step 0.01}
   cutoff {:default 500 :min 0 :max 20000 :step 1}
   attack {:default 0.0001 :min 0.0001 :max 5 :step 0.001}
   decay {:default 0.3 :min 0.0001 :max 5 :step 0.001}
   sustain {:default 0.99 :min 0.0001 :max 1 :step 0.001}
   release {:default 0.0001 :min 0.0001 :max 6 :step 0.001}
   fattack {:default 0.0001 :min 0.0001 :max 6 :step 0.001}
   fdecay {:default 0.3 :min 0.0001 :max 6 :step 0.001}
   fsustain {:default 0.999 :min 0.0001 :max 1 :step 0.001}
   frelease {:default 0.0001 :min 0.0001 :max 6 :step 0.001}
   gate 1]
  (let [freq       (midicps note)
        osc-bank-1 [(saw freq) (sin-osc freq) (pulse freq)]
        osc-bank-2 [(saw freq) (sin-osc freq) (pulse freq)]
        amp-env    (env-gen (adsr attack decay sustain release) gate :action FREE)
        f-env      (env-gen (adsr fattack fdecay fsustain frelease) gate)
        s1         (* osc1-level (select osc1 osc-bank-1))
        s2         (* osc2-level (select osc2 osc-bank-2))
        filt       (moog-ff (+ s1 s2) (* cutoff f-env) 3)]
    (* amp filt)))

(definst rise-fall-pad
  [freq 440 t 4 amt 0.3 amp 0.8]
  (let [f-env      (env-gen (perc t t) 1 1 0 1 FREE)
        src        (saw [freq (* freq 1.01)])
        signal     (rlpf (* 0.3 src)
                         (+ (* 0.6 freq) (* f-env 2 freq)) 0.2)
        k          (/ (* 2 amt) (- 1 amt))
        distort    (/ (* (+ 1 k) signal) (+ 1 (* k (abs signal))))
        gate       (pulse (* 2 (+ 1 (sin-osc:kr 0.05))))
        compressor (compander distort gate 0.01 1 0.5 0.01 0.01)
        dampener   (+ 1 (* 0.5 (sin-osc:kr 0.5)))
        reverb     (free-verb compressor 0.5 0.5 dampener)
        echo       (comb-n reverb 0.4 0.3 0.5)]
    (* amp echo)))

(definst pad
  [note 60 t 10 amt 0.3 amp 0.1 a 0.4 d 0.5 s 0.8 r 2]
  (let [freq   (midicps note)
        lfo    (+ 2 (* 0.01 (sin-osc:kr 5 (rand 1.5))))
        src    (apply + (saw [freq (* freq lfo)]))
        env    (env-gen (adsr a d s r) (sin-osc:kr 0.2))
        f-env  (x-line:kr 0.001 4 t)
        src    (* env src)
        signal (rlpf src (+ (* 0.3 freq) (* f-env 2 freq)) 0.5)
        k      (/ (* 4 amt) (- 1 amt))
        dist   (clip2 (/ (* (+ 1 k) signal) (+ 1 (* k (abs signal))))
                      0.03)
        snd    (* amp dist (line:kr 1 0 t))]
    src))

(definst overpad
  [note 60 amp 0.7 attack 0.001 release 2]
  (let [freq  (midicps note)
        env   (env-gen (perc attack release) :action FREE)
        f-env (+ freq (* 3 freq (env-gen (perc 0.012 (- release 0.1)))))
        bfreq (/ freq 2)
        sig   (apply +
                     (concat (* 0.7 (sin-osc [bfreq (* 0.99 bfreq)]))
                             (lpf (saw [freq (* freq 1.01)]) f-env)))
        audio (* amp env sig)]
    audio))

(definst buzz
  [pitch 40 cutoff 300 dur 200]
  (let [lpf-lev (* (+ 1 (lf-noise1:kr 10)) 400)
        a       (lpf (saw (midicps pitch)) lpf-lev)
        b       (sin-osc (midicps (- pitch 12)))
        env     (env-gen 1 1 0 1 2 (perc 0.01 (/ dur 1000)))]
    (* env (+ a b))))

(definst bass
  [freq 120 t 0.6 amp 0.5]
  (let [env  (env-gen (perc 0.08 t) :action FREE)
        src  (saw [freq (* 0.98 freq) (* 2.015 freq)])
        src  (clip2 (* 1.3 src) 0.8)
        sub  (sin-osc (/ freq 2))
        filt (resonz (rlpf src (* 4.4 freq) 0.09) (* 2.0 freq) 2.9)]
    (* env amp (fold:ar (distort (* 1.3 (+ filt sub))) 0.08))))

(definst daf-bass [freq 440 gate 1 amp 1 out-bus 0]
  (let [harm [1 1.01 2 2.02 3.5 4.01 5.501]
        harm (concat harm (map #(* 2 %) harm))
        snd  (* 2 (distort (sum (sin-osc (* freq harm)))))
        snd  (+ snd (repeat 2 (sum (sin-osc (/ freq [1 2])))))
        env  (env-gen (adsr 0.001 0.2 0.9 0.25) gate amp :action FREE)]
    (* snd env)))

(definst grunge-bass
  [note 48 amp 0.5 dur 0.1 a 0.01 d 0.01 s 0.4 r 0.01]
  (let [freq    (midicps note)
        env     (env-gen (adsr a d s r) (line:kr 1 0 (+ a d dur r 0.1))
                         :action FREE)
        src     (saw [freq (* 0.98 freq) (* 2.015 freq)])
        src     (clip2 (* 1.3 src) 0.9)
        sub     (sin-osc (/ freq 2))
        filt    (resonz (rlpf src (* 8.4 freq) 0.29) (* 2.0 freq) 2.9)
        meat    (ring4 filt sub)
        sliced  (rlpf meat (* 2 freq) 0.1)
        bounced (free-verb sliced 0.8 0.9 0.2)]
    (* env bounced)))

(definst vintage-bass
  [note 40 velocity 80 t 0.6 amp 0.5 gate 1]
  (let [freq     (midicps note)
        sub-freq (midicps (- note 12))
        velocity (/ velocity 127.0)
        sawz1    (* 0.075 (saw [freq freq]))
        sawz2    (* 0.75 (saw [(- freq 2) (+ 1 freq)]))
        sqz      (* 0.3 (pulse [sub-freq (- sub-freq 1)]))
        mixed    (* 0.1 (mix sawz1 sawz2 sqz))
        env      (env-gen (adsr 0.1 3.3 0.4 0.8) gate :action FREE)
        filt     (* env (moog-ff mixed (* velocity env (+ freq 200)) 2.2))]
    filt))

; B3 modeled a church organ using additive synthesis of 9 sin oscillators
; * Octave under root
; *	Fifth over root
; * Root
; * Octave over root
; * Octave and a fifth over root
; * Two octaves over root
; * Two octaves and a major third over root
; * Two octaves and a fifth over root
; * Three octaves over root
; Work in progress...  just getting started
(comment definst b3
  [note 60 a 0.01 d 3 s 1 r 0.01]
  (let [freq  (midicps note)
        waves (sin-osc [(* 0.5 freq)
                        freq
                        (* (/ 3 2) freq)
                        (* 2 freq)
                        (* freq 2 (/ 3 2))
                        (* freq 2 2)
                        (* freq 2 2 (/ 5 4))
                        (* freq 2 2 (/ 3 2))
                        (* freq 2 2 2)])
        snd   (apply + waves)
        env   (env-gen (adsr a d s r) :action FREE)]
    (* env snd 0.1)))

; Experimenting with Karplus Strong synthesis...
(definst ks1
  [note  {:default 60  :min 10   :max 120  :step 1}
   amp   {:default 0.8 :min 0.01 :max 0.99 :step 0.01}
   dur   {:default 2   :min 0.1  :max 4    :step 0.1}
   decay {:default 30  :min 1    :max 50   :step 1}
   coef  {:default 0.3 :min 0.01 :max 2    :step 0.01}]
  (let [freq (midicps note)
        noize (* 0.8 (white-noise))
        dly (/ 1.0 freq)
        plk   (pluck noize 1 (/ 1.0 freq) dly
                     decay
                     coef)
        dist (distort plk)
        filt (rlpf dist (* 12 freq) 0.6)
        clp (clip2 filt 0.8)
        reverb (free-verb clp 0.4 0.8 0.2)]
    (* amp (env-gen (perc 0.0001 dur) :action FREE) reverb)))

(definst ks1-demo
  [note 60 amp 0.8 gate 1]
  (let [freq (midicps note)
        noize (* 0.8 (white-noise))
        dly (/ 1.0 freq)
        plk   (pluck noize gate (/ 1.0 freq) dly
                     (mouse-x 0.1 50)
                     (mouse-y 0.0001 0.9999))
        dist (distort plk)
        filt (rlpf dist (* 12 freq) 0.6)
        reverb (free-verb filt 0.4 0.8 0.2)]
    (* amp (env-gen (perc 0.0001 2) :action FREE) reverb)))

(definst ks-stringer
  [freq 440 rate 6]
  (let [noize (* 0.8 (white-noise))
        trig  (dust rate)
        coef  (mouse-x -0.999 0.999)
        delay (/ 1.0 (* (mouse-y 0.001 0.999) freq))
        plk   (pluck noize trig (/ 1.0 freq) delay 10 coef)
        filt (rlpf plk (* 12 freq) 0.6)]
    (* 0.8 filt)))

(definst fm-demo
  [note 60 amp 0.2 gate 0]
  (let [freq (midicps note)
        osc-a (* (sin-osc (mouse-x 20 3000))
                 0.3)
        osc-b (* amp (sin-osc (* (mouse-y 3000 0) osc-a)))]
    osc-a))

; From the SC2 examples included with SC
; Don't think it's quite there, but almost...
(definst harmonic-swimming
  [amp 0.5]
  (let [freq     100
        partials 20
        z-init   0
        offset   (line:kr 0 -0.02 60)
        snd (loop [z z-init
                   i 0]
              (if (= partials i)
                z
                (let [f (clip:kr (mul-add
                                   (lf-noise1:kr [(+ 6 (rand 4))
                                                  (+ 6 (rand 4))])
                                   0.2 offset))
                      src  (f-sin-osc (* freq (inc i)))
                      newz (mul-add src f z)]
                  (recur newz (inc i)))))]
    (out 10 (pan2 (* amp snd)))))

(definst whoahaha
  [freq 440 dur 5 osc 100 mul 1000]
  (let [freqs [freq (* freq 1.0068) (* freq 1.0159)]
        sound (resonz (saw (map #(+ % (* (sin-osc osc) mul)) freqs))
                      (x-line 10000 10 25)
                      (line 1 0.05 25))
        sound (apply + sound)]
  (* (lf-saw:kr (line:kr 13 17 3)) (line:kr 1 0 dur FREE) sound)))

(definst bubbles
  [bass-freq 80]
  (let [bub (+ bass-freq (* 3 (lf-saw:kr [8 7.23])))
        glis (+ bub (* 24 (lf-saw:kr 0.4 0)))
        freq (midicps glis)
        src (* 0.04 (sin-osc freq))
        zout (comb-n src :decay-time 4)]
    zout))

; // Originally from the STK instrument models...
(comment definst bowed
  [note 60 velocity 80 gate 1 amp 1
   bow-offset 0 bow-slope 0.5 bow-position 0.75 vib-freq 6.127 vib-gain 0.2]
  (let [freq         (midicps note)
        velocity     (/ velocity 127)
        beta-ratio   (+ 0.027236 (* 0.2 bow-position))
        base-delay   (reciprocal freq)
        [fb1 fb2]    (local-in 2)
        vibrato      (* (sin-osc vib-freq) vib-gain)
        neck-delay   (+ (* base-delay (- 1 beta-ratio)) (* base-delay vibrato))
        neck         (delay-l fb1 0.05 neck-delay)
        nut-refl     (neg neck)
        bridge       (delay-l fb2 0.025 (* base-delay beta-ratio))
        string-filt  (one-pole (* bridge 0.95) 0.55)
        bridge-refl  (neg string-filt)
        adsr         (* amp (env-gen (adsr 0.02 3.005 1.0 0.01) gate :action FREE))
        string-vel   (+ bridge-refl nut-refl)
        vel-diff     (- adsr string-vel)
        slope        (- 5.0 (* 4 bow-slope))
        bow-table    (clip:ar (pow (abs (+ (* (+ vel-diff bow-offset) slope) 0.75 )) -4) 0 1)
        new-vel       (* vel-diff bow-table)]
   (local-out (+ [bridge-refl nut-refl] new-vel))
   (resonz (* bridge 0.5) 500 0.85)))

(comment definst flute
  [gate 1 freq 440 amp 1.0 endreflection 0.5 jetreflection 0.5
   jetratio 0.32 noise-gain 0.15 vibfreq 5.925 vib-gain 0.0 amp 1.0]
  (let [nenv           (env-gen (linen 0.2 0.03 0.5 0.5) gate :action FREE)
        adsr           (+ (* amp 0.2) (env-gen (adsr 0.005 0.01 1.1 0.01) gate :action FREE))
        noise          (* (white-noise) noise-gain)
        vibrato        (sin-osc vibfreq 0 vib-gain)
        delay          (reciprocal (* freq 0.66666))
        lastout        (local-in 1)
        breathpressure (* adsr (+ noise, vibrato))
        filter         (leak-dc (one-pole (neg lastout) 0.7))
        pressurediff   (- breathpressure (* jetreflection filter))
        jetdelay       (delay-l pressurediff 0.025 (* delay jetratio))
        jet            (clip2 (* jetdelay (- (squared jetdelay) 1.0)) 1.0)
        boredelay      (delay-l (+ jet (* endreflection filter) 0.05 delay))]
    (local-out boredelay)
    (* 0.3 boredelay amp nenv)))

; SynthDef(\flute, { arg out=0, gate=1, freq=440, amp=1.0, endReflection=0.5, jetReflection=0.5, jetRatio=0.32, noiseGain=0.15, vibFreq=5.925, vibGain=0.0, outputGain=1.0;
;
;   var nenv = EnvGen.ar(Env.linen(0.2, 0.03, 0.5, 0.5), gate, doneAction: 2);
;   var adsr = (amp*0.2) + EnvGen.ar(Env.adsr(0.005, 0.01, 1.1, 0.01), gate, doneAction: 2);
;   var noise = WhiteNoise.ar(noiseGain);
;   var vibrato = SinOsc.ar(vibFreq, 0, vibGain);
;
;   var delay = (freq*0.66666).reciprocal;
;   var lastOut = LocalIn.ar(1);
;   var breathPressure = adsr*Mix([1.0, noise, vibrato]);
;   var filter = LeakDC.ar(OnePole.ar(lastOut.neg, 0.7));
;   var pressureDiff = breathPressure - (jetReflection*filter);
;   var jetDelay = DelayL.ar(pressureDiff, 0.025, delay*jetRatio);
;   var jet = (jetDelay * (jetDelay.squared - 1.0)).clip2(1.0);
;   var boreDelay = DelayL.ar(jet + (endReflection*filter), 0.05, delay);
;   LocalOut.ar(boreDelay);
;   Out.ar(out, 0.3*boreDelay*outputGain*nenv);
; }).store;
;
; s = Synth("flute", ["freq", 220]);
; s.set("gate", 0);
;
; SynthDef(\blowbotl, { arg out=0, amp=1.0, freq=440, rq=0.0, gate=1, noise=0.0, vibFreq=5.2, vibGain=0.0;
;   var lastOut = LocalIn.ar(1);
;   var adsr = amp*EnvGen.ar(Env.adsr(0.005, 0.01, 1.0, 0.010), gate, doneAction: 2);
;   var vibrato = SinOsc.ar(vibFreq, 0, vibGain);
;   var pressureDiff = (adsr+vibrato) - lastOut;
;   var jet = (pressureDiff * (pressureDiff.squared - 1.0)).clip2(1.0);
;   var randPressure = WhiteNoise.ar(noise)*adsr*(1.0 + pressureDiff);
;
;   var resonator = Resonz.ar(adsr+randPressure - (jet*pressureDiff), freq, rq);
;   LocalOut.ar(resonator);
;   Out.ar(out, LeakDC.ar(resonator));
; }).store
;
; f = Synth(\blowbotl);
; f.set("freq", 100);
; f.free;

; SynthDef(\voicform, { arg out=0, gate=1, freq=440, amp=0.3, voiceGain=1.0, noiseGain=0.0, sweepRate=0.001;
;
;   var voiced = Pulse.ar(freq, 0.1, voiceGain);
;   var onezero = OneZero.ar(voiced, -0.9);
;   var onepole = OnePole.ar(onezero, 0.97 - (amp*0.2));
;   var noise = WhiteNoise.ar(noiseGain*0.1);
;   var excitation = onepole + noise;
;
;   var ffreqs = Control.names([\ffreq]).kr([770, 1153, 2450, 3140]);
;   var fradii = Control.names([\bw]).kr([0.950, 0.970, 0.780, 0.8]);
;   var famps = Control.names([\gain]).kr([1.0, 0.355, 0.0355, 0.011]);
;
;   var filters = TwoPole.ar(excitation, Lag.kr(ffreqs, sweepRate), Lag.kr(fradii, sweepRate), Lag.kr(famps, sweepRate) );
;
;   Out.ar(out, amp*Mix(filters) );
; }).store;
;
; v = Synth(\voicform, target: s)
; v.set("freq", 100);
; v.free;


; SynthDef.new("mcldjospiano1", { | out = 0, freq = 440, gate = 1,
; amp=0.1, pan = 0|
;         var impresp, imps, dels, hammerstr, velocity, string, ampcomp,
; pldelay, cutoff;
;         velocity = Latch.kr(gate, gate);
;
;         cutoff = EnvGen.kr(Env.asr(0.00001, 1, 0.2, curve: -4), gate,
; doneAction: 2) * 15000 + 30;
;
;         // We start off by appromixating the piano's impulse response.
;         impresp = WhiteNoise.ar(1, 0, EnvGen.ar(Env.perc(0.02, 0.02)));
;         impresp = LPF.ar(impresp, freq.expexp(50, 1000, 10000, 500));
;         // FreeVerb is NOT a piano soundboard impulse response! Just a standin
;         impresp = FreeVerb.ar(impresp, 0.8, freq.linlin(300, 600, 0.1, 0.9),
; freq.linlin(300, 600, 0.19, 0.01));
;         impresp = LeakDC.ar(impresp);
;
;         // Then we simulate the multiple strikes of the hammer
; against the string
;         dels = #[0.002, 0.006, 0.009] * freq.explin(100, 1000, 1, 0.01);
;         imps = DelayN.ar(impresp, dels, dels, #[0.85, 0.32, 0.22]);
;         // Note: at higher velocity, the LPF goes higher, making the
; hammer hits more pointy & separate
;         imps = LPF.ar(imps, freq * 2 * #[1, 1.5, 1.5] * velocity * 2, mul: 8);
;         hammerstr = imps.sum;
;
;         // Now push the sound into Pluck, to simulate the string vibration
;         pldelay = (freq * [Rand(0.999, 0.9995), 1, Rand(1.00005,
; 1.001)]).reciprocal;
;         string = Pluck.ar(hammerstr, Impulse.kr(0.000001), pldelay,
; pldelay, 10.5, 0.4);
;         string = LeakDC.ar(string).sum;
;
;         // patch gives un-piano-like amplitude variation across
; pitch; let's compensate
;         ampcomp = freq.max(350).min(1000).linlin(350, 1000, 1, 60);
;         string = string * ampcomp;
;
;         // filter is to damp the string when the note stops
;         string = LPF.ar(string, cutoff);
;
;         Out.ar(out, Pan2.ar(string, pan, (amp * 10)));
; }).store;
;
;
; SynthDef(\piano, { arg outBus, freq, amp, dur, pan;
;         var sig, in, n = 6, max = 0.04, min = 0.01, delay, pitch, detune, hammer;
;         hammer = Decay2.ar(Impulse.ar(0.001), 0.008, 0.04,
;             LFNoise2.ar([2000,4000].asSpec.map(amp), 0.25));
;         sig = Mix.ar(Array.fill(3, { arg i;
;                 detune = #[-0.04, 0, 0.03].at(i);
;                 delay = (1/(freq + detune).midicps);
;                 CombL.ar(hammer, delay, delay, 50 * amp) +
;                 SinOsc.ar(
;                     [(freq * 2) + SinOsc.kr(2, Rand(0, 1.0), 4),
;                     freq * [4.23, 6.5]].flat ,
;                     0,
;                     amp * [0.1, 0.25, 0.3]).sum
;                 }) );
;
;
;         sig = HPF.ar(sig,50) * EnvGen.ar(Env.perc(0.0001,dur, amp, -1), doneAction:2);
;         Out.ar(outBus, Pan2.ar(sig,pan));
; }).send(s);

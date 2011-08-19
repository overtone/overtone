(ns overtone.inst.synth
  (:use overtone.core))

(definst tick [freq 880]
  (* (env-gen (perc 0.001 0.01) :action FREE)
     (sin-osc freq)))

(definst ping [note 60 a 0.2 b 0.2]
  (let [snd (sin-osc (midicps note))
        env (env-gen (perc a b) :action FREE)]
    (* env snd)))

(definst tb303 [note 60 wave 1
                cutoff 100 r 0.9
                attack 0.101 decay 1.8 sustain 0.2 release 0.2
                env 200 gate 0 vol 0.8]
  (let [freq       (midicps note)
        freqs      [freq (* 1.01 freq)]
        vol-env    (env-gen (adsr attack decay sustain release)
                            (line:kr 1 0 (+ attack decay release))
                            :action FREE)
        fil-env    (env-gen (perc))
        fil-cutoff (+ cutoff (* env fil-env))
        waves     [(* vol-env (saw freqs))
                   (* vol-env [(pulse (first freqs) 0.5)
                               (lf-tri (second freqs))])]
        selector   (select wave (apply + waves))
        filt       (rlpf selector fil-cutoff r)]
    filter))

(definst mooger
  [note 60 amp 0.8
   osc1 0 osc2 1     ; Choose 0 1 2 for saw, sin, or pulse
   cutoff 500
   attack 0.1 decay 0.1 sustain 0.7 release 0.2
   fattack 0.1 fdecay 0.1 fsustain 0.9 frelease 0.2
   gate 1]
  (let [freq (midicps note)
        osc-bank-1 [(saw freq) (sin-osc freq) (pulse freq)]
        osc-bank-2 [(saw freq) (sin-osc freq) (pulse freq)]
        amp-env (env-gen (adsr attack decay sustain release) gate :action FREE)
        f-env (env-gen (adsr fattack fdecay fsustain frelease) gate :action FREE)
        s1 (select osc1 osc-bank-1)
        s2 (select osc2 osc-bank-2)
        filt (moog-ff (+ s1 s2) (* cutoff f-env) 3)]
    (* amp filt)))

(definst rise-fall-pad [freq 440 t 4 amt 0.3 amp 0.8]
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

(definst pad [note 60 t 4 amt 0.3 amp 0.8]
  (let [freq       (midicps note)
        f-env      (env-gen (perc t t) 1 1 0 1 FREE)
        src        (saw [freq (* freq (+ 2 (* 0.01 (sin-osc:kr 5 (rand 1.5)))))])
        signal     (rlpf (* 0.9 src)
                         (+ (* 0.3 freq) (* f-env 4 freq)) 0.5)
        k          (/ (* 4 amt) (- 1 amt))
        dist       (clip2 (/ (* (+ 1 k) signal) (+ 1 (* k (abs signal))))
                          0.03)]
    (* amp dist (line:kr 1 0 t))))

(definst buzz [pitch 40 cutoff 300 dur 200]
  (let [a (lpf (saw (midicps pitch)) (* (lf-noise1 :control 10) 400))
        b (sin-osc (midicps (- pitch 12)))
        env (env-gen 1 1 0 1 2 (perc 0.01 (/ dur 1000)))]
    (* env (+ a b))))

(definst bass [freq 120 t 0.6 amp 0.5]
  (let [env (env-gen (perc 0.08 t) :action FREE)
        src (saw [freq (* 0.98 freq) (* 2.015 freq)])
        src (clip2 (* 1.3 src) 0.8)
        sub (sin-osc (/ freq 2))
        filt (resonz (rlpf src (* 4.4 freq) 0.09) (* 2.0 freq) 2.9)]
    (* env amp (fold (distort (* 1.3 (+ filt sub))) 0.08))))

(definst grunge-bass [note 48 amp 0.5 dur 0.1 a 0.01 d 0.01 s 0.4 r 0.01]
  (let [freq (midicps note)
        env (env-gen (adsr a d s r) (line:kr 1 0 (+ a d dur r 0.1))
                     :action FREE)
        src (saw [freq (* 0.98 freq) (* 2.015 freq)])
        src (clip2 (* 1.3 src) 0.9)
        sub (sin-osc (/ freq 2))
        filt (resonz (rlpf src (* 8.4 freq) 0.29) (* 2.0 freq) 2.9)
        meat (ring4 filt sub)
        sliced (rlpf meat (* 2 freq) 0.1)
        bounced (free-verb sliced 0.8 0.9 0.2)]
    (* env bounced)))

; Experimenting with Karplus Strong synthesis...
(definst ks1 [note 60 amp 0.8 dur 2 decay 30 coef 0.3 gate 1]
  (let [freq (midicps note)
        noize (* 0.8 (white-noise))
        dly (/ 1.0 freq)
        plk   (pluck noize gate (/ 1.0 freq) dly
                     decay
                     coef)
        dist (distort plk)
        filt (rlpf dist (* 12 freq) 0.6)
        clp (clip2 filt 0.8)
        reverb (free-verb clp 0.4 0.8 0.2)]
    (* amp (env-gen (perc 0.0001 dur) :action FREE))))

(definst ks1-demo [note 60 amp 0.8 gate 1]
  (let [freq (midicps note)
        noize (* 0.8 (white-noise))
        dly (/ 1.0 freq)
        plk   (pluck noize gate (/ 1.0 freq) dly
                     (mouse-x 0.1 50)
                     (mouse-y 0.0001 0.9999))
        dist (distort plk)
        filt (rlpf dist (* 12 freq) 0.6)
        reverb (free-verb filt 0.4 0.8 0.2)]
    (* amp (env-gen (perc 0.0001 2) :action FREE))))

(definst ks-stringer [freq 440 rate 6]
  (let [noize (* 0.8 (white-noise))
        trig  (dust rate)
        coef  (mouse-x -0.999 0.999)
        delay (/ 1.0 (* (mouse-y 0.001 0.999) freq))
        plk   (pluck noize trig (/ 1.0 freq) delay 10 coef)
        filt (rlpf plk (* 12 freq) 0.6)]
    (* 0.8 filt)))

(definst fm-demo [note 60 amp 0.2 gate 0]
  (let [freq (midicps note)
        osc-a (* (sin-osc (mouse-x 20 3000))
                 0.3)
        osc-b (* amp (sin-osc (* (mouse-y 3000 0) osc-a)))]
    osc-a))

; From the SC2 examples included with SC
; Don't think it's quite there, but almost...
(definst harmonic-swimming [amp 0.5]
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

(definst whoahaha [freq 440 dur 5 osc 100 mul 1000]
  (let [freqs [freq (* freq 1.0068) (* freq 1.0159)]
        sound (resonz (saw (map #(+ % (* (sin-osc osc) mul)) freqs))
                      (x-line 10000 10 25)
                      (line 1 0.05 25))
        sound (apply + sound)]
  (* (lf-saw:kr (line:kr 13 17 3)) (line:kr 1 0 dur FREE))))

(definst bubbles [bass-freq 80]
  (let [bub (+ bass-freq (* 3 (lf-saw:kr [8 7.23])))
        glis (+ bub (* 24 (lf-saw:kr 0.4 0)))
        freq (midicps glis)
        src (* 0.04 (sin-osc freq))
        zout (comb-n src :decaytime 4)]
    zout))

;(def alien-buffer (buffer 2250))
;(definst alien-computer [trig 0.3]
;  (ifft (pv-rand-comb (fft alien-buffer (white-noise))
;                               0.95 (impulse:kr trig))))))
;(defn buzzer [t tick dur notes]
;  (let [note (first notes)]
;    (if (> (rand) 0.9)
;      (do
;        (hit t :buzz :pitch note :dur dur)
;        (hit (+ t tick) :buzz :pitch note :dur dur)
;        (callback (+ t tick (- tick 100)) #'buzzer (+ t tick tick) tick dur (next notes)))
;      (do
;        (hit t :buzz :pitch note :dur dur)
;        (callback (+ t (- tick 100)) #'buzzer (+ t tick) tick dur (next notes))))))
;
;;(buzzer (+ (now) 200) 100 50 (cycle [68 80 48 80 92 75]))
;
;;; Helpers
;
;(defmacro mix [& args]
;  (syn (reduce (fn [mem arg] (list '+ mem arg))
;          args)))
;
;;; Toy synths... clean up eventually
;
;(def sin (synth sin {:out 0 :amp 0.1 :pitch 40 :dur 300}
;  (let [snd (sin-osc.ar (midicps :pitch))
;        env (env-gen.kr (linen 0.001 (/ :dur 1000.0) 0.002) :done-free)]
;    (out.ar :out (pan2.ar (* (* snd env) :amp) 0)))))
;
;(synth mouse-saw
;  (out.ar 0 (pan2.ar (sin-osc.ar (mouse-y.kr 10 1200 1 0) 0) 0)))
;
;(comment
;  (load-synth mouse-saw)
;  (hit mouse-saw)
;  )
;
;(synth noise-filter {:cutoff 500}
;  (quick (lpf.ar (* 0.4 (pink-noise.ar)) :cutoff)))


;(defn basic-sound []
;  (syn
;    (mul-add.ar
;      (decay2.ar (mul-add.ar
;                 (impulse.ar 8 0)
;                 (mul-add.kr (lf-saw.kr 0.3 0) -0.3 0.3)
;                 0)
;               0.001)
;      0.3 (+ (pulse.ar 80 0.3) (pulse.ar 81 0.3)))))
;
;(synth basic-synth
;  (quick (basic-sound)))
;
;(synth compressed-synth {:attack 0.01
;                            :release 0.01}
;  (let [z (basic-sound)]
;    (quick
;      (compander.ar z 0
;                    (mouse-x.kr 0.1 1) ; mouse controls gain
;                    1 0.5 ; slope below and above the knee
;                    :attack  ; clamp time (attack)
;                    :release)))) ; relax time (release)
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
;(synth triangle-test
;  (out.ar 0
;          (mul-add.ar
;            (wand 60)
;            (env-gen.kr 1 1 0 1 2 (triangle 3 0.8))
;            0)))
; TODO: Port the interesting ones to Overtone synthdefs

; SynthDef("vintage-bass", {|out=0, note=40, vel=0.5, gate=1|
;   var f = midicps(note);
;   var f2 = midicps(note-12);
;   var saw = Saw.ar([f, f], 0.075);
;   var saw2 = Saw.ar([f-2, f+1], 0.75);
;   var sq = Pulse.ar([f2, f2-1], 0.5, 0.3);
;   var snd = Mix([saw, saw2, sq], 0, 0.1);
;   var env = EnvGen.kr(Env.adsr(0.1, 3.3, 0.4, 0.3), gate, doneAction: 2);
;   var filt = env * MoogFF.ar(snd, env * vel * f+200, 2.2);
;   Out.ar(0, Pan2.ar(filt, 0))
; }).store;
; b = Synth("vintage-bass", ["note", 30.0, "vel", 0.6]);
; b.set("gate", 1);
; b.set("gate", 0);
;
; /////////////////////////////
; // Originally from the STK instrument models.  SuperCollider port found at lost URL.
;
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
;
; SynthDef(\bowed, { arg out=0, amp=1.0, gate=1, freq=420, bowOffset = 0.0, bowSlope = 0.5, bowPosition = 0.75, vibFreq=6.127, vibGain=0.2;
;   var betaRatio = 0.027236 + (0.2*bowPosition);
;   var baseDelay = freq.reciprocal;
;   var lastOut = LocalIn.ar(2);
;   var vibrato = SinOsc.ar(vibFreq, 0, vibGain);
;   var neckDelay = baseDelay*(1.0-betaRatio) + (baseDelay*vibrato);
;   var neck = DelayL.ar(lastOut[0], 0.05, neckDelay);
;   var bridge = DelayL.ar(lastOut[1], 0.025, baseDelay*betaRatio);
;   var stringFilter = OnePole.ar(bridge*0.95, 0.55);
;   var adsr = amp*EnvGen.ar(Env.adsr(0.02, 0.005, 1.0, 0.01), gate, doneAction: 2);
;   var bridgeRefl = stringFilter.neg;
;   var nutRefl = neck.neg;
;   var stringVel = bridgeRefl + nutRefl;
;   var velDiff = adsr - stringVel;
;   var slope = 5.0 - (4.0*bowSlope);
;   var bowtable = (( ((velDiff+bowOffset)*slope) + 0.75 ).abs ).pow(-4).clip(0, 1);
;   var newVel = velDiff*bowtable;
;   LocalOut.ar([bridgeRefl, nutRefl] + newVel);
;   Out.ar(out, Resonz.ar( bridge*0.5, 500, 0.85 ) );
; }, [\ir, 0,0, 0, 0, 0, 0, 0, 0]).send(s);
;
; Synth("bowed")
; Synth("bowed", ["freq", 200])
;
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
;
;
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
;
; ( //play a little ditty
; Task({
; 36.do({
; 2.do({arg i;
; Synth(\piano, [\freq, [0,2,3,5,7,8,10].choose + (60 + (i * 12)), \outBus, 0,
; \amp, rrand(0.25,0.9), \dur, 1, \pan, 0], s);
; });
; [0.5,1].choose.wait
; });
; }).start
; );
;
;
; (
;  SynthDef(\sine_osc, {
;      arg amp = 0.1, gate = 1;
;
;      // I tried to figure out how to use .do for this but it eluded me this time!
;      var freq_array = [Rand(40.0, 2000.0), Rand(40.0, 2000.0), Rand(40.0, 2000.0), Rand(40.0, 2000.0), Rand(40.0, 2000.0)];
;      var ampmod_array = [MouseX.kr(0.0, 1.0), MouseY.kr(1.0, 0.0)];
;
;      // Five SinOscs with randomly selected frequency are created and spread across the stereo image
;      // The amp mod applied to left and right output channels is controlled by mouse position
;      Out.ar(0, SinOsc.ar(ampmod_array, 0, 1.0) * Splay.ar(SinOsc.ar(freq_array, 0, amp)));
;
;      FreeSelf.kr(1 - gate);
;      }).store;
;  )
;
; d = Synth(\sine_osc);
; d.free;
;
; { Out.ar(1, SinOsc.ar(300, 0)) }.scope;

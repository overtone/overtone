(ns sc-one
  (:use [overtone.live]))

;;page 4
;;play({SinOsc.ar(LFNoise0.kr(12, mul: 600, add: 1000), 0.3)})

(demo 10 (sin-osc (+ 1000 (* 600 (lf-noise0:kr 12))) 0.3))



;;;;;;;;
;;page 5
;;play({RLPF.ar(Dust.ar([12, 15]), LFNoise1.ar(1/[3,4], 1500, 1600), 0.02)})

(demo 10 (rlpf (dust [12 15]) (+ 1600 (* 1500 (lf-noise1 [1/3, 1/4]))) 0.02 ))



;;Page 6
;;///////////// Figure 1.1 Example of additive synthesis
;;
;;play({
;;        var sines = 100, speed = 6;
;;        Mix.fill(sines,
;;        	{arg x;
;;        		Pan2.ar(
;;        			SinOsc.ar(x+1*100,
;;        				mul: max(0,
;;        					LFNoise1.kr(speed) +
;;        					Line.kr(1, -1, 30)
;;        				)
;;        			), rand2(1.0))})/sines})
;;
;;/////////////



(demo 2 (let [sines 5
               speed 1000]
           (* (apply +
                     (map #(pan2 (* (sin-osc (* % 100))
                                    (maximum 0 (+ (lf-noise1:kr speed) (line:kr 1 -1 30))))
                                 (- (clojure.core/rand 2) 1))
                          (range sines)))
              (/ 1 sines))))



;;Page 10
;;///////////// Figure 1.3 Fortuitous futuristic nested music.
;;
;;(
;;play(
;;        {
;;        	CombN.ar(
;;        		SinOsc.ar(
;;        			midicps(
;;        				LFNoise1.ar(3, 24,
;;        					LFSaw.ar([5, 5.123], 0, 3, 80)
;;        				)
;;        			),
;;        			0, 0.4),
;;        		1, 0.3, 2)
;;        }
;;)
;;)
;;
;;/////////////

(demo 10 (let [noise (lf-noise1 3)
               saws  (mul-add (lf-saw [5 5.123]) 3 80)
               freq  (midicps (mul-add noise 24 saws))
               src   (* 0.4 (sin-osc freq))]

           (comb-n src 1 0.3 2)))



;;;;;;;;;
;;page 16
;;{PMOsc.ar(100, 500, 10, 0, 0.5)}.play
;;
;;PMOsc isn't an actual ugen, it's actually a pseudo ugen defined for backwards
;;compatibility with older scsynth implementations. Its definition is as follows:
;;PMOsc  {
;;
;;        *ar { arg carfreq,modfreq,pmindex=0.0,modphase=0.0,mul=1.0,add=0.0;
;;                ^SinOsc.ar(carfreq, SinOsc.ar(modfreq, modphase, pmindex),mul,add)
;;        }
;;
;;        *kr { arg carfreq,modfreq,pmindex=0.0,modphase=0.0,mul=1.0,add=0.0;
;;                ^SinOsc.kr(carfreq, SinOsc.kr(modfreq, modphase, pmindex),mul,add)
;;        }
;;
;;}

(demo (* 0.5 (sin-osc 100 (* 10 (sin-osc 500 0)))))

;;we could also define our own pseudo ugen fn:

(defn pm-osc [car-freq mod-freq pm-index mod-phase]
  (with-ugens
    (sin-osc car-freq (* pm-index (sin-osc mod-freq mod-phase)))))

(demo (* 0.5 (pm-osc 100 500 10 0)))



;;Page 17
;;///////////// Figure 1.4 VCO, VCF, VCA
;;
;;(
;;{
;;        Blip.ar(
;;        	TRand.kr( // frequency or VCO
;;        		100, 1000, // range
;;        		Impulse.kr(Line.kr(1, 20, 60))), // trigger
;;        	TRand.kr( // number of harmonics or VCF
;;        		1, 10, // range
;;        		Impulse.kr(Line.kr(1, 20, 60))), // trigger
;;        	Linen.kr( // mul, or amplitude, VCA
;;        		Impulse.kr(Line.kr(1, 20, 60)), // trigger
;;        		0, // attack
;;        		0.5, // sustain level
;;        		1/Line.kr(1, 20, 60)) // trigger
;;        	)
;;}.play
;;)
;;
;;/////////////

(demo 30
      (let [trigger       (line:kr :start 1, :end 20, :dur 60)
            freq          (t-rand:kr :lo 100, :hi 1000, :trig (impulse:kr trigger))
            num-harmonics (t-rand:kr :lo 1,   :hi 10,   :trig (impulse:kr trigger))
            amp           (linen:kr :gate (impulse:kr trigger) :attackTime 0, :susLevel 0.5, :releaseTime (/ 1 trigger))]
        (* amp (blip freq num-harmonics))))



;;;;;;;;;
;;page 19
;;
;;
;;(
;;{
;;r = MouseX.kr(1/3, 10);
;;SinOsc.ar(mul: Linen.kr(Impulse.kr(r), 0, 1, 1/r))
;;}.play
;;)

(demo 10 (let [rate (mouse-x (/ 1 3) 10)
               amp  (linen:kr :gate (impulse:kr rate), :attackTime 0, :susLevel 1, :releaseTime (/ 1 rate))]
           (* amp (sin-osc))))



;;///////////// Example 1.5 Synthesis example with variables and statements
;;
;;(
;;// run this first
;;p = { // make p equal to this function
;;r = Line.kr(1, 20, 60); // rate
;;// r = LFTri.kr(1/10) * 3 + 7;
;;t = Impulse.kr(r); // trigger
;;// t = Dust.kr(r);
;;e = Linen.kr(t, 0, 0.5, 1/r); // envelope uses r and t
;;f = TRand.kr(1, 10, t); // triggered random also uses t
;;// f = e + 1 * 4;
;;Blip.ar(f*100, f, e) // f, and e used in Blip
;;}.play
;;)
;;
;;p.free;  // run this to stop it
;;
;;///////////// Figure 1.6 Phase modulation with modulator as ratio

(demo 10 (let [r (line:kr :start 1, :end 20, :dur 60)
               ;;r (+ 7 (* 3 (lf-tri:kr 0.1)))
               t (impulse:kr r)
               ;;t (dust:kr r)
               e (linen:kr :gate t, :attackTime 0, :susLevel 0.5, :releaseTime (/ 1 r))
               f (t-rand:kr :lo 1, :hi 10, :trig t)
               ;;f (* 4 (+ 1 e))
               ]
           (* e (blip :freq (* f 100), :numharm f))))


;;Page 21
;;///////////// Figure 1.6 Phase modulation with modulator as ratio
;;
;;(
;;{ // carrier and modulator not linked
;;        r = Impulse.kr(10);
;;        c = TRand.kr(100, 5000, r);
;;        m = TRand.kr(100, 5000, r);
;;        PMOsc.ar(c, m, 12)*0.3
;;}.play
;;)
;;
;;(
;;{
;;        var rate = 4, carrier, modRatio; // declare variables
;;        carrier = LFNoise0.kr(rate) * 500 + 700;
;;        modRatio = MouseX.kr(1, 2.0);
;;        // modulator expressed as ratio, therefore timbre
;;        PMOsc.ar(carrier, carrier*modRatio, 12)*0.3
;;}.play
;;)
;;
;;/////////////

(defn pm-osc [car-freq mod-freq pm-index mod-phase]
  (with-ugens
    (sin-osc car-freq (* pm-index (sin-osc mod-freq mod-phase)))))

(demo 10 (let [r (impulse:kr 10)
               c (t-rand:kr :lo 100, :hi 5000, :trig r)
               m (t-rand:kr :lo 100, :hi 5000, :trig r)]
           (* 0.3 (pm-osc c m 12 0))))

(demo 10 (let [rate 4
               carrier (+ 700 (* 500 (lf-noise0:kr rate)))
               mod-ratio (mouse-x :min 1, :max 2)]
           (* 0.3 (pm-osc carrier (* carrier mod-ratio) 12 9))))



;;Page 22
;;
;;SynthDef("sine", {Out.ar(0, SinOsc.ar)}).play
;;
;;SynthDef("sine", {Out.ar(1, SinOsc.ar)}).play // right channel
;;
;;// or
;;
;;(
;;SynthDef("one_tone_only", {
;;        var out, freq = 440;
;;        out = SinOsc.ar(freq);
;;        Out.ar(0, out)
;;}).play
;;)

(defsynth left-sine [] (out 0 (sin-osc)))
(left-sine)
(stop)

(defsynth right-sine [] (out 1 (sin-osc)))
(right-sine)
(stop)

(defsynth one-tone-only [] (let [freq 440
                                 src  (sin-osc freq)]
                             (out 0 src)))
(one-tone-only)
(stop)



;;Page 23
;;/////////////
;;
;;(
;;SynthDef("different_tones", {
;;        arg freq = 440; // declare an argument and give it a default value
;;        var out;
;;        out = SinOsc.ar(freq)*0.3;
;;        Out.ar(0, out)
;;}).play
;;)
;;
;;/////////////

(defsynth different-tones [freq 440]
  (let [src (* 0.3 (sin-osc freq))]
    (out 0 src)))

;;run all four, then stop all
(different-tones 550)
(different-tones 660)
(different-tones :freq 880)
(different-tones)
(stop)


;;tracking and controlling synths independently
(def a (different-tones :freq (midi->hz 64)))
(def b (different-tones :freq (midi->hz 67)))
(def c (different-tones :freq (midi->hz 72)))
(ctl a :freq (midi->hz 65))
(ctl c :freq (midi->hz 71))
(do
  (ctl a :freq (midi->hz 64))
  (ctl c :freq (midi->hz 72)))
(kill a)
(kill b)
(kill c)




;;Page 24
;;
;;///////////// Figure 1.7 Synth definition
;;
;;(
;;//run this first
;;SynthDef("PMCrotale", {
;;arg midi = 60, tone = 3, art = 1, amp = 0.8, pan = 0;
;;var env, out, mod, freq;
;;
;;freq = midi.midicps;
;;env = Env.perc(0, art);
;;mod = 5 + (1/IRand(2, 6));
;;
;;out = PMOsc.ar(freq, mod*freq,
;;        pmindex: EnvGen.kr(env, timeScale: art, levelScale: tone),
;;        mul: EnvGen.kr(env, timeScale: art, levelScale: 0.3));
;;
;;out = Pan2.ar(out, pan);
;;
;;out = out * EnvGen.kr(env, timeScale: 1.3*art,
;;        levelScale: Rand(0.1, 0.5), doneAction:2);
;;Out.ar(0, out); //Out.ar(bus, out);
;;
;;}).add;
;;)

(defn pm-osc [car-freq mod-freq pm-index mod-phase]
  (with-ugens
    (sin-osc car-freq (* pm-index (sin-osc mod-freq mod-phase)))))

(defsynth pmc-rotale [midi 60 tone 3 art 1 amp 0.8 pan 0]
  (let [freq (midicps midi)
        env (perc 0 art)
        mod (+ 5 (/ 1 (i-rand 2 6)))
        src (* (pm-osc freq (* mod freq) (env-gen:kr env :timeScale art, :levelScale tone) 0)
               (env-gen:kr env :timeScale art, :levelScale 0.3))
        src (pan2 src pan)
        src (* src (env-gen:kr env :timeScale (* art 1.3) :levelScale (rrand 0.1 0.5) :action :free))]
    (out 0 src)))

;;Synth("PMCrotale", ["midi", rrand(48, 72).round(1), "tone", rrand(1, 6)])

(pmc-rotale :midi (ranged-rand 48 72) :tone (ranged-rand 1 6))


;;Page 25
;;
;;~houston = Buffer.read(s, "sounds/a11wlk01-44_1.aiff");
;;~chooston = Buffer.read(s, "sounds/a11wlk01.wav");
;;
;;{PlayBuf.ar(1, ~houston)}.play;
;;{PlayBuf.ar(1, ~chooston)}.play;


;;this assumes you have a separate install of SuperCollider and
;;you're running OS X. Feel free to change the following audio paths
;;to any other audio file on your disk...

(def houston (load-sample "/Applications/SuperCollider/sounds/a11wlk01-44_1.aiff"))
(def chooston (load-sample "/Applications/SuperCollider/sounds/a11wlk01.wav"))

(demo 4 (play-buf 1 houston))
(demo 5 (play-buf 1 chooston))


;;Page 26
;;
;;[~houston.bufnum, ~houston.numChannels, ~houston.path, ~houston.numFrames];
;;[~chooston.bufnum, ~chooston.numChannels, ~chooston.path, ~chooston.numFrames];

;;samples are represented as standard clojure maps
houston
chooston



;;(
;;{
;;        var rate, trigger, frames;
;;        frames = ~houston.numFrames;
;;
;;        rate = [1, 1.01];
;;        trigger = Impulse.kr(rate);
;;        PlayBuf.ar(1, ~houston, 1, trigger, frames * Line.kr(0, 1, 60)) *
;;        EnvGen.kr(Env.linen(0.01, 0.96, 0.01), trigger) * rate;
;;}.play
;;)

(demo 60 (let [frames (num-frames houston)
              rate   [1 1.01]
              trigger (impulse:kr rate)
              src (play-buf 1 houston 1 trigger (* frames (line:kr 0 1 60)))
              env (env-gen:kr (lin-env 0.01 0.96 0.01) trigger)]
          (* src env rate)))

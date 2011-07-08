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
           (* (mix
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

;;we can use the pm-osc pseudo ugen provided by overtone:

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

(demo 10
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



(demo 10 (let [r (impulse:kr 10)
               c (t-rand:kr :lo 100, :hi 5000, :trig r)
               m (t-rand:kr :lo 100, :hi 5000, :trig r)]
           (* [0.3 0.3] (pm-osc c m 12 0))))

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
(do
  (kill a)
  (kill b)
  (kill c))




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
play

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

;; note how the envelope is used to stop clicking between segments. Contrast with the following

(demo 5 (let [frames (num-frames houston)
              rate   [1 1.01]
              trigger (impulse:kr rate)
              src (play-buf 1 houston 1 trigger (* frames (line:kr 0 1 60)))]
          (* src rate)))

;;( // speed and direction change
;;{
;;        var speed, direction;
;;        speed = LFNoise0.kr(12) * 0.2 + 1;
;;        direction = ]LFClipNoise.kr(1/3);
;;        PlayBuf.ar(1, ~houston, (speed * direction), loop: 1);
;;}.play
;;)

(demo 5 (let [speed     (+ 1 (* 0.2 (lf-noise0:kr 12)))
              direction (lf-clip-noise:kr 1/3)]
          (play-buf 1 houston (* speed direction) :loop 1)))


;; Page 27

;;( // if these haven't been used they will hold 0
;;~kbus1 = Bus.control; // a control bus
;;~kbus2 = Bus.control; // a control bus
;;{
;;        var speed, direction;
;;        speed = In.kr(~kbus1, 1) * 0.2 + 1;
;;        direction = In.kr(~kbus2);
;;        PlayBuf.ar(1, ~chooston, (speed * direction), loop: 1);
;;}.play
;;)
;;
;;(
;;// now start the controls
;;{Out.kr(~kbus1, LFNoise0.kr(12))}.play;
;;{Out.kr(~kbus2, LFClipNoise.kr(1/4))}.play;
;;)
;;// Now start the second buffer with the same control input buses,
;;// but send it to the right channel using Out.ar(1 etc.
;;
;;(
;;{
;;        var speed, direction;
;;        speed = In.kr(~kbus1, 1) * 0.2 + 1;
;;        direction = In.kr(~kbus2);
;;        Out.ar(1, PlayBuf.ar(1, ~houston, (speed * direction), loop: 1));
;;}.play;
;;)

(def kbus1 (control-bus))
(def kbus2 (control-bus))

(defsynth src []
  (let [speed (+ 1 (* 0.2 (in:kr kbus1 1)))
        direction (in:kr kbus2)]
    (out 0 (play-buf 1 chooston (* speed direction) :loop 1))))

(defsynth control1 []
  (out:kr kbus1 (lf-noise0:kr 12)))

(defsynth control2 []
  (out:kr kbus2 (lf-clip-noise:kr 1/4)))

(defsynth player []
  (let [speed (+ 1 (* 0.2 (in:kr kbus1 1)))
        direction (in:kr kbus2)]
    (out 1 (play-buf 1 houston (* speed direction) :loop 1))))

(do
  (src)
  (control1)
  (control2)
  (player))

(stop)

;; Page 28

;;~kbus3 = Bus.control;
;;~kbus4 = Bus.control;
;;{Out.kr(~kbus3, SinOsc.kr(3).range(340, 540))}.play;
;;{Out.kr(~kbus4, LFPulse.kr(6).range(240, 640))}.play;
;;SynthDef("Switch", {arg freq = 440; Out.ar(0, SinOsc.ar(freq, 0, 0.3))}).add
;;x = Synth("Switch");
;;x.map(\freq, ~kbus3)
;;x.map(\freq, ~kbus4)

(do

  (def kbus3 (control-bus))
  (def kbus4 (control-bus))

  (defsynth wave-ctl [] (out:kr kbus3 (lin-lin (sin-osc:kr 1) -1 1 340 540)))
  (defsynth pulse-ctl [] (out:kr kbus4 (lin-lin (sin-osc:kr 1) -1 1 240 640)))

  (defsynth switch [freq 440]
    (out 0 (sin-osc freq 0 0.3)))

  (def s (switch))
  (def w (wave-ctl))
  (def p (pulse-ctl)))

;;try evaling these
(map-ctl s :freq kbus3)
(map-ctl s :freq kbus4)

(stop)


;; Page 29

;;(
;;{
;;        Out.ar(0,
;;            Pan2.ar( PlayBuf.ar(1, ~houston, loop: 1) *
;;                SinOsc.ar(LFNoise0.kr(12, mul: 500, add: 600)),
;;            0.5)
;;        )
;;}.play
;;)

(demo 10 (pan2 (* (play-buf 1 houston :loop 1)
                  (sin-osc (+ 600 (* 500 (lf-noise0:kr 12)))))
               0.5))

;;
;;(
;;{
;;        var source, delay;
;;        source = PlayBuf.ar(1, ~chooston, loop: 1);
;;        delay = AllpassC.ar(source, 2, [0.65, 1.15], 10);
;;        Out.ar(0, Pan2.ar(source) + delay)
;;}.play
;;)

(demo 10 (let [source (play-buf 1 chooston :loop 1)
               delay (allpass-c source 2 [0.65 1.15] 10)]
           (+ delay (pan2 source))))


;;//Create and name buses
;;~delay = Bus.audio(s, 2);
;;~mod = Bus.audio(s, 2);
;;~gate = Bus.audio(s, 2);
;;~k5 = Bus.control;
;;
;;~controlSyn= {Out.kr(~k5, LFNoise0.kr(4))}.play //start the control
;;
;;// Start the last item in the chain, the delay
;;~delaySyn = {Out.ar(0, AllpassC.ar(In.ar(~delay, 2), 2, [0.65, 1.15], 10))}.play(~controlSyn, addAction: \addAfter);
;;
;;// Start the next to last item, the modulation
;;~modSyn = {Out.ar(~delay, In.ar(~mod, 2) * SinOsc.ar(In.kr(~k5) * 500 + 1100))}.play(~delaySyn, addAction: \addBefore);
;;
;;//Start the third to last item, the gate
;;~gateSyn = {Out.ar([0, ~mod], In.ar(~gate, 2) * max(0, In.kr(~k5)))}.play(~modSyn, addAction: \addBefore);
;;
;;//make a group for the PlayBuf synths at the head of the chain
;;~pbGroup = Group.before(~controlSyn);
;;
;;// Start one buffer. Since we add to the group, we know where it will go
;;{Out.ar(~gate, Pan2.ar(PlayBuf.ar(1, ~houston, loop: 1), 0.5))}.play(~pbGroup);
;;
;;// Start the other
;;{Out.ar(~gate, Pan2.ar(PlayBuf.ar(1, ~chooston, loop: 1), -0.5))}.play(~pbGroup);

(do
  (def delay-b (audio-bus 2))
  (def mod-b (audio-bus 2))
  (def gate-b (audio-bus 2))
  (def k5-b (control-bus))

  (defsynth control-syn [] (out:kr k5-b (lf-noise0:kr 4)))
  (def c-syn (control-syn))

  (defsynth delay-syn [] (out:ar 0 (allpass-c (in delay-b 2) 2 [0.65 1.15] 10)))
  (def d-syn (delay-syn :pos :after :tgt c-syn))

  (defsynth mod-syn [] (out delay-b (* (in mod-b 2) (sin-osc (+ 1100 (* 500 (in:kr k5-b)))))))
  (def m-syn (mod-syn :pos :before :tgt d-syn))

  (defsynth gate-syn [] (out [0 mod-b] (* (in gate-b 2) (maximum 0 (in:kr k5-b)))))
  (def g-syn (gate-syn :pos :before :tgt m-syn ))

  (def pb-group (group :before c-syn))

  (defsynth hous [] (out gate-b (pan2 (play-buf 1 houston :loop 1) 0.5)))
  (defsynth choos [] (out gate-b (pan2 (play-buf 1 chooston :loop 1) -0.5))))

(hous :tgt pb-group)
(choos :tgt pb-group)

(stop)


;; Page 32

;;// This uses the PMCrotale synth definition
;;(
;;a = ["C", "C#", "D", "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B"];
;;"event, midi, pitch, octave".postln;
;;r = Task({
;;        inf.do({ arg count;
;;        	var midi, oct, density;
;;        	density = 1.0;
;;        	// density = 0.7;
;;        	// density = 0.3;
;;        	midi = [0, 2, 4, 7, 9].choose;
;;        	// midi = [0, 2, 4, 5, 7, 9, 11].choose
;;        	// midi = [0, 2, 3, 5, 6, 8, 9, 11].choose;
;;        	// midi = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11].choose;
;;        	oct = [48, 60, 72].choose;
;;        	if(density.coin,
;;        	    { // true action
;;        		    "".postln;
;;        		    [midi + oct, a.wrapAt(midi),
;;        		    (oct/12).round(1)].post;
;;        		    Synth("PMCrotale",
;;        		    ["midi", midi + oct, "tone", rrand(1, 7),
;;        		    "art", rrand(0.3, 2.0), "amp", rrand(0.3, 0.6), "pan", 1.0.rand2]);
;;        	    }, {["rest"].post}); // false action
;;        	0.2.wait;
;;        });
;;}).start
;; )

(do
  (def a [:C :C# :D :Eb :E :F :F# :G :Ab :A :Bb :B])

  (def cont (atom true))

  (loop []
    (let [density 1
          midi (choose [0 2 4 7 9])
          oct (choose [48 60 72])]
      (if (weighted-coin density)
        (do
          (println "")
          (println [(+ midi oct) (nth (cycle a) midi) (round-to (/ oct 12) 1)])
          (pmc-rotale :midi (+ midi oct)
                      :tone (ranged-rand 1 7)
                      :art (ranged-rand 0.3 2.0)
                      :amp (ranged-rand 0.3 0.6)
                      :pan (ranged-rand -1 1)))
        (println "rest"))
      (Thread/sleep 200)
      (when @cont (recur)))))


;; to stop
(reset! cont false)


;; Page 36

;;// Mix down a few of them tuned to harmonics
;;
;;(
;;{
;;        var fund = 220;
;;        Mix.ar(
;;        	[
;;        	SinOsc.ar(220, mul: max(0, LFNoise1.kr(12))),
;;        	SinOsc.ar(440, mul: max(0, LFNoise1.kr(12))) * 1/2,
;;              SinOsc.ar(660, mul: max(0, LFNoise1.kr(12))) * 1/3,
;;        	SinOsc.ar(880, mul: max(0, LFNoise1.kr(12))) * 1/4,
;;        	SinOsc.ar(1110, mul: max(0, LFNoise1.kr(12))) * 1/5,
;;        	SinOsc.ar(1320, mul: max(0, LFNoise1.kr(12))) * 1/6
;;        	]
;;        	) * 0.3
;;}.play
;;)

(demo 15
      (* 0.3
         (+ (* (sin-osc 220)  (maximum 0 (lf-noise1:kr 12)) 1)
            (* (sin-osc 440)  (maximum 0 (lf-noise1:kr 12)) 1/2)
            (* (sin-osc 660)  (maximum 0 (lf-noise1:kr 12)) 1/3)
            (* (sin-osc 880)  (maximum 0 (lf-noise1:kr 12)) 1/4 )
            (* (sin-osc 1110) (maximum 0 (lf-noise1:kr 12)) 1/5)
            (* (sin-osc 1320) (maximum 0 (lf-noise1:kr 12)) 1/6))))

;; or the more compact but equivalent:

(demo 15
      (let [freqs [220 440 660 880 1110 1320]
            muls  [1   1/2 1/3 1/4 1/5  1/6]
            mk-sin #(* (sin-osc %1) (maximum 0 (lf-noise1 12)) %2)
            sins  (map mk-sin freqs muls)]
        (* (mix sins) 0.3)))


;; Page 37

;;// And a patch
;;(
;;{
;;        Mix.ar(
;;            Array.fill(12,
;;                {arg count;
;;        	        var harm;
;;        	        harm = count + 1 * 110; //remember precedence; count + 1, then * 110
;;        	        SinOsc.ar(harm, mul: max([0, 0], SinOsc.kr(count + 1/4))) * 1/(count + 1)
;;                })
;;    )*0.7}.play
;;)

(demo 15
      (* 0.7
         (mix
          (for [count (range 12)]
            (let [harm (* (inc count) 110)]
              (* (sin-osc harm)
                 (maximum [0 0] (sin-osc:kr (/ (inc count) 4)))
                 (/ 1 (inc count))))))))


;; Page 38

;;(
;;{
;;        var scale, specs, freqs, amps, rings,
;;         numRes = 5, bells = 20, pan;
;;     scale = [60, 62, 64, 67, 69].midicps;
;;         Mix.fill(bells, {
;;                 freqs = Array.fill(numRes, {rrand(1, 15)*(scale.choose)});
;;                 amps = Array.fill(numRes, {rrand(0.3, 0.9)});
;;                 rings = Array.fill(numRes, {rrand(1.0, 4.0)});
;;                 specs = [freqs, amps, rings].round(0.01);
;;                 specs.postln;
;;                 pan = (LFNoise1.kr(rrand(3,6))* 2).softclip;
;;                 Pan2.ar(
;;                     Klank.ar(`specs,
;;                         Dust.ar(1/6, 0.03)),
;;                         pan)
;;         })
;;}.play;
;;)

(demo 10
      (let [num-res 5
            bells   20
            scale   (map midi->hz [60 62 64 67 69])
            mk-bell (fn [] (let [freqs (repeatedly num-res #(* (ranged-rand 1 5) (choose scale)))
                                amps  (repeatedly num-res #(ranged-rand 0.3 0.9))
                                rings (repeatedly num-res #(ranged-rand 1 4))
                                specs [freqs amps rings]
                                _ (println specs)
                                pan (softclip (* 2 (lf-noise1:kr (ranged-rand 3 6))))]
                            (pan2 (klank specs (* 0.03 (dust (/ 1 6)) ))
                                  pan)))]
        (out 0 (mix (repeatedly bells mk-bell)))))

///////////////
// TODO: 
//  * create a standard set of instrument parameters that all instruments
//    of each type should accept.  (midi-note style synths, fx, drums...)
//  * sample support
//  * fx processing

s.boot;
s.quit;
Help.gui;

(
SynthDef("sin", {|out = 0, pitch = 40, dur = 300|
  Out.ar(out, Pan2.ar( EnvGen.kr(Env.linen(0.001, dur / 1000.0, 0.002), doneAction: 2) * SinOsc.ar(midicps(pitch), 0, 0.8), 0));
  }).store;
)
Synth("sin", ["pitch", 60, "dur", 100]);

// Making chords

// Just trigger 3 single note synths at the same time
(
 Synth("sin", ["pitch", 60, "dur", 0.2]);
 Synth("sin", ["pitch", 64, "dur", 0.2]);
 Synth("sin", ["pitch", 67, "dur", 0.2]);
)

// Define a major-chorded synth
(
SynthDef("sin-chord", {|out = 0, pitch = 40, dur = 0.3|
  Out.ar(out, Pan2.ar( EnvGen.kr(Env.linen(0.001, dur, 0.002), doneAction: 2) * SinOsc.ar(midicps([pitch, pitch + 4, pitch + 7]), 0, 0.8), 0));
  }).store;
)
Synth("sin-chord", ["pitch", 60, "dur", 0.2]);

// For testing instruments with a midi keyboard...
(
  var notes, on, off;
  notes = Array.newClear(128);  // one slot per possible MIDI note
  on = NoteOnResponder({ |src, chan, num, veloc|
      notes[num] = Synth("vintage-bass", [\midin, num, \vel, veloc * 0.00315, \gate, 1]);
      });
  off = NoteOffResponder({ |src, chan, num, veloc|
      notes[num].set(\gate, 0);
      });
  q = { on.remove; off.remove; };
)
q.value;

SynthDef("foo", {|note=50|
  Out.ar(0, Saw.ar(midicps(note)));
}).store;

SynthDef("vintage-bass", {|out=0, note=40, vel=0.5, gate=1|
  var f = midicps(note);
  var f2 = midicps(note-12);
  var saw = Saw.ar([f, f], 0.075);
  var saw2 = Saw.ar([f-2, f+1], 0.75);
  var sq = Pulse.ar([f2, f2-1], 0.5, 0.3);
  var snd = Mix([saw, saw2, sq], 0, 0.1);
  var env = EnvGen.kr(Env.adsr(0.1, 3.3, 0.4, 0.3), gate, doneAction: 2);
  var filt = env * MoogFF.ar(snd, env * vel * f+200, 2.2);
  Out.ar(0, Pan2.ar(filt, 0))
}).store;
b = Synth("vintage-bass", ["note", 30.0, "vel", 0.6]);
b.set("gate", 1);
b.set("gate", 0);

/////////////////////////////
// Originally from the STK instrument models.  SuperCollider port found at lost URL.

SynthDef(\flute, { arg out=0, gate=1, freq=440, amp=1.0, endReflection=0.5, jetReflection=0.5, jetRatio=0.32, noiseGain=0.15, vibFreq=5.925, vibGain=0.0, outputGain=1.0;

  var nenv = EnvGen.ar(Env.linen(0.2, 0.03, 0.5, 0.5), gate, doneAction: 2);
  var adsr = (amp*0.2) + EnvGen.ar(Env.adsr(0.005, 0.01, 1.1, 0.01), gate, doneAction: 2);
  var noise = WhiteNoise.ar(noiseGain);
  var vibrato = SinOsc.ar(vibFreq, 0, vibGain);

  var delay = (freq*0.66666).reciprocal;
  var lastOut = LocalIn.ar(1);
  var breathPressure = adsr*Mix([1.0, noise, vibrato]);
  var filter = LeakDC.ar(OnePole.ar(lastOut.neg, 0.7));
  var pressureDiff = breathPressure - (jetReflection*filter);
  var jetDelay = DelayL.ar(pressureDiff, 0.025, delay*jetRatio);
  var jet = (jetDelay * (jetDelay.squared - 1.0)).clip2(1.0);
  var boreDelay = DelayL.ar(jet + (endReflection*filter), 0.05, delay);
  LocalOut.ar(boreDelay);
  Out.ar(out, 0.3*boreDelay*outputGain*nenv);
}).store;

s = Synth("flute", ["freq", 220]);
s.set("gate", 0);

SynthDef(\blowbotl, { arg out=0, amp=1.0, freq=440, rq=0.0, gate=1, noise=0.0, vibFreq=5.2, vibGain=0.0;
  var lastOut = LocalIn.ar(1);
  var adsr = amp*EnvGen.ar(Env.adsr(0.005, 0.01, 1.0, 0.010), gate, doneAction: 2);
  var vibrato = SinOsc.ar(vibFreq, 0, vibGain);
  var pressureDiff = (adsr+vibrato) - lastOut;
  var jet = (pressureDiff * (pressureDiff.squared - 1.0)).clip2(1.0);
  var randPressure = WhiteNoise.ar(noise)*adsr*(1.0 + pressureDiff);
  
  var resonator = Resonz.ar(adsr+randPressure - (jet*pressureDiff), freq, rq);
  LocalOut.ar(resonator);
  Out.ar(out, LeakDC.ar(resonator));
}).store

f = Synth(\blowbotl);
f.set("freq", 100);
f.free;

SynthDef(\bowed, { arg out=0, amp=1.0, gate=1, freq=420, bowOffset = 0.0, bowSlope = 0.5, bowPosition = 0.75, vibFreq=6.127, vibGain=0.2;
  var betaRatio = 0.027236 + (0.2*bowPosition);
  var baseDelay = freq.reciprocal;
  var lastOut = LocalIn.ar(2);
  var vibrato = SinOsc.ar(vibFreq, 0, vibGain);
  var neckDelay = baseDelay*(1.0-betaRatio) + (baseDelay*vibrato);
  var neck = DelayL.ar(lastOut[0], 0.05, neckDelay);
  var bridge = DelayL.ar(lastOut[1], 0.025, baseDelay*betaRatio);
  var stringFilter = OnePole.ar(bridge*0.95, 0.55);
  var adsr = amp*EnvGen.ar(Env.adsr(0.02, 0.005, 1.0, 0.01), gate, doneAction: 2);
  var bridgeRefl = stringFilter.neg;
  var nutRefl = neck.neg;
  var stringVel = bridgeRefl + nutRefl;
  var velDiff = adsr - stringVel;
  var slope = 5.0 - (4.0*bowSlope);
  var bowtable = (( ((velDiff+bowOffset)*slope) + 0.75 ).abs ).pow(-4).clip(0, 1);
  var newVel = velDiff*bowtable;
  LocalOut.ar([bridgeRefl, nutRefl] + newVel);
  Out.ar(out, Resonz.ar( bridge*0.5, 500, 0.85 ) );
}, [\ir, 0,0, 0, 0, 0, 0, 0, 0]).send(s);

Synth("bowed")
Synth("bowed", ["freq", 200])

SynthDef(\voicform, { arg out=0, gate=1, freq=440, amp=0.3, voiceGain=1.0, noiseGain=0.0, sweepRate=0.001;
  
  var voiced = Pulse.ar(freq, 0.1, voiceGain);
  var onezero = OneZero.ar(voiced, -0.9);
  var onepole = OnePole.ar(onezero, 0.97 - (amp*0.2));
  var noise = WhiteNoise.ar(noiseGain*0.1);
  var excitation = onepole + noise;

  var ffreqs = Control.names([\ffreq]).kr([770, 1153, 2450, 3140]);
  var fradii = Control.names([\bw]).kr([0.950, 0.970, 0.780, 0.8]);
  var famps = Control.names([\gain]).kr([1.0, 0.355, 0.0355, 0.011]);
  
  var filters = TwoPole.ar(excitation, Lag.kr(ffreqs, sweepRate), Lag.kr(fradii, sweepRate), Lag.kr(famps, sweepRate) );
  
  Out.ar(out, amp*Mix(filters) );
}).store;

v = Synth(\voicform, target: s)
v.set("freq", 100);
v.free;


SynthDef.new("mcldjospiano1", { | out = 0, freq = 440, gate = 1,
amp=0.1, pan = 0|
        var impresp, imps, dels, hammerstr, velocity, string, ampcomp,
pldelay, cutoff;
        velocity = Latch.kr(gate, gate);

        cutoff = EnvGen.kr(Env.asr(0.00001, 1, 0.2, curve: -4), gate,
doneAction: 2) * 15000 + 30;

        // We start off by appromixating the piano's impulse response.
        impresp = WhiteNoise.ar(1, 0, EnvGen.ar(Env.perc(0.02, 0.02)));
        impresp = LPF.ar(impresp, freq.expexp(50, 1000, 10000, 500));
        // FreeVerb is NOT a piano soundboard impulse response! Just a standin
        impresp = FreeVerb.ar(impresp, 0.8, freq.linlin(300, 600, 0.1, 0.9),
freq.linlin(300, 600, 0.19, 0.01));
        impresp = LeakDC.ar(impresp);

        // Then we simulate the multiple strikes of the hammer
against the string
        dels = #[0.002, 0.006, 0.009] * freq.explin(100, 1000, 1, 0.01);
        imps = DelayN.ar(impresp, dels, dels, #[0.85, 0.32, 0.22]);
        // Note: at higher velocity, the LPF goes higher, making the
hammer hits more pointy & separate
        imps = LPF.ar(imps, freq * 2 * #[1, 1.5, 1.5] * velocity * 2, mul: 8);
        hammerstr = imps.sum;

        // Now push the sound into Pluck, to simulate the string vibration
        pldelay = (freq * [Rand(0.999, 0.9995), 1, Rand(1.00005,
1.001)]).reciprocal;
        string = Pluck.ar(hammerstr, Impulse.kr(0.000001), pldelay,
pldelay, 10.5, 0.4);
        string = LeakDC.ar(string).sum;

        // patch gives un-piano-like amplitude variation across
pitch; let's compensate
        ampcomp = freq.max(350).min(1000).linlin(350, 1000, 1, 60);
        string = string * ampcomp;

        // filter is to damp the string when the note stops
        string = LPF.ar(string, cutoff);

        Out.ar(out, Pan2.ar(string, pan, (amp * 10)));
}).store;


SynthDef(\piano, { arg outBus, freq, amp, dur, pan; 
        var sig, in, n = 6, max = 0.04, min = 0.01, delay, pitch, detune, hammer;
        hammer = Decay2.ar(Impulse.ar(0.001), 0.008, 0.04,
            LFNoise2.ar([2000,4000].asSpec.map(amp), 0.25));
        sig = Mix.ar(Array.fill(3, { arg i;
                detune = #[-0.04, 0, 0.03].at(i);
                delay = (1/(freq + detune).midicps);
                CombL.ar(hammer, delay, delay, 50 * amp) + 
                SinOsc.ar(
                    [(freq * 2) + SinOsc.kr(2, Rand(0, 1.0), 4), 
                    freq * [4.23, 6.5]].flat , 
                    0, 
                    amp * [0.1, 0.25, 0.3]).sum
                }) );


        sig = HPF.ar(sig,50) * EnvGen.ar(Env.perc(0.0001,dur, amp, -1), doneAction:2);
        Out.ar(outBus, Pan2.ar(sig,pan));
}).send(s);

( //play a little ditty
Task({ 
36.do({ 
2.do({arg i;
Synth(\piano, [\freq, [0,2,3,5,7,8,10].choose + (60 + (i * 12)), \outBus, 0, 
\amp, rrand(0.25,0.9), \dur, 1, \pan, 0], s);
});
[0.5,1].choose.wait
});
}).start
);


// Create FM synth SynthDef ======================================
(
 SynthDef("fm-synth", {
     arg freq = 440, amp = 0, gate = 0;

     Out.ar(0, Pan2.ar(SinOsc.ar(MouseY.kr(4000, 0) * SinOsc.ar(MouseX.kr(20, 4000), 0, 
                     MouseButton.kr(0, 1, 0.2), 0, 0.2)), 0, 0.2));

     FreeSelf.kr(1 - gate);  // FreeSelf automatically releases on neg-pos transition
     }).store;
 )

// Test FM synth using test variable
a = Synth('fm-synth');
a.free;

(
 SynthDef(\sine_osc, {
     arg amp = 0.1, gate = 1;

     // I tried to figure out how to use .do for this but it eluded me this time!
     var freq_array = [Rand(40.0, 2000.0), Rand(40.0, 2000.0), Rand(40.0, 2000.0), Rand(40.0, 2000.0), Rand(40.0, 2000.0)];
     var ampmod_array = [MouseX.kr(0.0, 1.0), MouseY.kr(1.0, 0.0)];

     // Five SinOscs with randomly selected frequency are created and spread across the stereo image
     // The amp mod applied to left and right output channels is controlled by mouse position
     Out.ar(0, SinOsc.ar(ampmod_array, 0, 1.0) * Splay.ar(SinOsc.ar(freq_array, 0, amp)));

     FreeSelf.kr(1 - gate);
     }).store;
 )

d = Synth(\sine_osc);
d.free;

{ Out.ar(1, SinOsc.ar(300, 0)) }.scope;

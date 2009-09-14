///////////////
// TODO: 
//  * create a standard set of instrument parameters that all instruments
//    of each type should accept.  (midi-note style synths, fx, drums...)
//  * sample support
//  * fx processing

s.boot;
Help.gui;

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
b = Synth("vintage-bass", ["note", 40.0, "vel", 0.6]);
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

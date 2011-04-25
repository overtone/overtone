## SuperCollider 2.0 Unit Generators: 
  
### Unary Operators 
  
neg .. inversion 
reciprocal .. reciprocal 
abs .. absolute value 
floor .. next lower integer 
ceil .. next higher integer 
frac .. fractional part 
sign .. -1 when a < 0, +1 when a > 0, 0 when a is 0 
squared .. a * a 
cubed .. a * a * a 
sqrt .. square root 
exp .. exponential 
midicps .. MIDI note number to cycles per second 
cpsmidi .. cycles per second to MIDI note number 
midiratio .. convert an interval in MIDI notes into a frequency ratio 
ratiomidi .. convert a frequency ratio to an interval in MIDI notes 
dbamp .. decibels to linear amplitude 
ampdb .. linear amplitude to decibels 
octcps .. decimal octaves to cycles per second 
cpsoct .. cycles per second to decimal octaves 
log .. natural logarithm 
log2 .. base 2 logarithm 
log10 .. base 10 logarithm 
sin .. sine 
cos .. cosine 
tan .. tangent 
asin .. arcsine 
acos .. arccosine 
atan .. arctangent 
sinh .. hyperbolic sine 
cosh .. hyperbolic cosine 
tanh .. hyperbolic tangent 
distort .. distortion 
softclip .. distortion 
isPositive .. 1 when a >= 0, else 0 
isNegative .. 1 when a < 0, else 0 
isStrictlyPositive .. 1 when a > 0, else 0 
  
### Binary Operators 
  
+ .. addition 
- .. subtraction 
* .. multiplication 
/ .. division 
% .. float modulo 
** .. exponentiation 
< .. less than 
<= .. less than or equal 
> .. greater than 
>= .. greater than or equal 
== .. equal 
!= .. not equal 
min .. minimum of two 
max .. maximum of two 
round .. quantization by rounding 
trunc .. quantization by truncation 
atan2 .. arctangent 
hypot .. hypotenuse sqrt(a * a + b * b) 
ring1 .. a * b + a or equivalently: a * (b + 1) 
ring2 .. a * b + a + b 
ring3 .. a * a * b 
ring4 .. a * a * b - a * b * b 
sumsqr .. a * a + b * b 
difsqr .. a * a - b * b 
sqrsum .. (a + b)**2 
sqrdif .. (a - b)**2 
absdif .. fabs(a - b) 
amclip .. two quadrant multiply { 0 when b <= 0, a * b when b > 0 } 
scaleneg .. nonlinear amplification { a when a >= 0, a * b when a < 0 } 
clip2 .. bilateral clipping { b when a > b, -b when a < -b, else a } 
excess .. residual of clipping a - clip2(a,b) 
  
### Oscillators 
  
Osc .. wavetable oscillator 
Osc.ar(table, freq, phase, mul, add) 
  
OscN .. noninterpolating wavetable oscillator 
OscN.ar(table, freq, phase, mul, add) 
  
COsc .. chorusing oscillator 
COsc.ar(table, freq, beats, mul, add) 
  
COsc2 .. dual table chorusing oscillator 
COsc2.ar(table1, table2, freq, beats, mul, add) 
  
OscX4 .. 4 table crossfade oscillator 
OscX4.ar(table1, table2, table3, table4, freq, xpos, ypos, mul, add) 
  
SinOsc .. sine table lookup oscillator 
SinOsc.ar(freq, phase, mul, add) 
  
FSinOsc .. very fast sine oscillator 
FSinOsc.ar(freq, mul, add) 
  
Klang .. bank of fixed frequency sine oscillators 
Klang.ar(inSpecificationsArrayRef, iFreqScale, iFreqOffset, mul, add) 
  
PSinGrain .. sine grain with a parabolic envelope (very fast) 
PSinGrain.ar(freq, dur, amp) 
  
Blip .. band limited impulse oscillator 
Blip.ar(freq, numharm, mul, add) 
  
Saw .. band limited sawtooth oscillator 
Saw.ar(freq, mul, add) 
  
Pulse .. band limited pulse wave oscillator 
Pulse.ar(freq, duty, mul, add) 
  
PMOsc .. phase modulation oscillator pair 
PMOsc.ar(carfreq, modfreq, pmindex, modphase, mul, add) 
  
Formant .. formant oscillator 
Formant.ar(fundfreq, formfreq, bwfreq, mul, add) 
  
Phasor .. sawtooth for phase input 
Phasor.ar(freq, mul, add) 
  
LFSaw .. low freq (i.e. not band limited) sawtooth oscillator 
LFSaw.ar(freq, mul, add) 
  
LFPulse .. low freq (i.e. not band limited) pulse wave oscillator 
LFPulse.ar(freq, width, mul, add) 
  
Impulse .. non band limited impulse oscillator 
Impulse.ar(freq, mul, add) 
  
SyncSaw .. hard sync sawtooth wave oscillator 
SyncSaw.ar(syncFreq, sawFreq, mul, add) 
  
### Noise 
  
WhiteNoise .. white noise 
WhiteNoise.ar(mul, add) 
  
PinkNoise .. pink noise 
PinkNoise.ar(mul, add) 
  
BrownNoise .. brown noise 
BrownNoise.ar(mul, add) 
  
ClipNoise .. clipped noise 
ClipNoise.ar(mul, add) 
  
LFNoise0 .. low frequency noise, no interpolation 
LFNoise0.ar(freq, mul, add) 
  
LFNoise1 .. low frequency noise, linear interpolation 
LFNoise1.ar(freq, mul, add) 
  
LFNoise2 .. low frequency noise, quadratic interpolation 
LFNoise2.ar(freq, mul, add) 
  
LFClipNoise .. low frequency clipped noise 
LFClipNoise.ar(freq, mul, add) 
  
Crackle .. chaotic noise function 
Crackle.ar(chaosParam, mul, add) 
  
Dust .. random positive impulses 
Dust.ar(density, mul, add) 
  
Dust2 .. random bipolar impulses 
Dust2.ar(density, mul, add) 
  
LinCong .. linear congruential generator 
LinCong.ar(iseed, imul, iadd, imod, mul, add) 
  
Rossler .. chaotic function 
Rossler.ar(chaosParam, dt, mul, add) 
  
Latoocarfian .. Clifford Pickover's chaotic function 
Latoocarfian.ar(a, b, c, d, mul, add) 
  
### Filters 
  
FOS .. general first order section 
FOS.ar(in, a0, a1, b1, mul, add) 
  
SOS .. general second order section 
SOS.ar(in, a0, a1, a2, b1, b2, mul, add) 
  
Resonz .. general purpose resonator 
Resonz.ar(in, freq, bwr, mul, add) 
  
Klank .. bank of fixed frequency resonators 
Klank.ar(inSpecificationsArrayRef, iFreqScale, iFreqOffset, iDecayScale, in, mul, add) 
  
OnePole .. one pole filter 
OnePole.ar(in, coef, mul, add) 
  
OneZero .. one zero filter 
OneZero.ar(in, coef, mul, add) 
  
TwoPole .. two pole filter 
TwoPole.ar(in, freq, radius, mul, add) 
  
TwoZero .. two zero filter 
TwoZero.ar(in, freq, radius, mul, add) 
  
RLPF .. resonant low pass filter 
RLPF.ar(in, freq, rq, mul, add) 
  
RHPF .. resonant high pass filter 
RHPF.ar(in, freq, rq, mul, add) 
  
LPF .. Butterworth low pass 
LPF.ar(in, freq, mul, add) 
  
HPF .. Butterworth high pass 
HPF.ar(in, freq, mul, add) 
  
BPF .. Butterworth band pass 
BPF.ar(in, freq, rq, mul, add) 
  
BRF .. Butterworth band reject 
BRF.ar(in, freq, rq, mul, add) 
  
RLPF4 .. fourth order resonant low pass filter 
RLPF4.ar(in, freq, res, mul, add) 
  
Integrator .. integrator 
Integrator.ar(in, coef, mul, add) 
  
Slope .. differentiator scaled by sampling rate 
Slope.ar(in, mul, add) 
  
LeakDC .. removes that ugly DC build up 
LeakDC.ar(in, coef, mul, add) 
  
Decay .. exponential decay 
Decay.ar(in, decayTime, mul, add) 
  
Decay2 .. exponential attack and decay 
Decay2.ar(in, attackTime, decayTime, mul, add) 
  
LPZ1 .. special case: two point sum (one zero low pass) 
LPZ1.ar(in, mul, add) 
  
HPZ1 .. special case: two point difference (one zero high pass) 
HPZ1.ar(in, mul, add) 
  
LPZ2 .. special case: two zero low pass 
LPZ2.ar(in, mul, add) 
  
HPZ2 .. special case: two zero high pass 
HPZ2.ar(in, mul, add) 
  
BPZ2 .. special case: two zero mid pass 
BPZ2.ar(in, mul, add) 
  
BRZ2 .. special case: two zero mid cut 
BRZ2.ar(in, mul, add) 
  
Median .. three point median filter 
Median.ar(in, mul, add) 
  
### Controls 
  
ControlIn .. read an external control source 
ControlIn.kr(source, lagTime) 
  
Osc1 .. single shot function generator 
Osc1.ar(table, dur, mul, add) 
  
EnvGen .. break point envelope 
EnvGen.ar(levelArrayRef, durArrayRef, mul, add, levelScale, levelBias, timeScale) 
  
Slew .. slew rate limit 
Slew.ar(in, up, dn, mul, add) 
  
Trig .. timed trigger 
Trig.ar(in, dur) 
  
Trig1 .. timed trigger 
Trig1.ar(in, dur) 
  
TDelay .. trigger delay 
TDelay.ar(in, delayTime) 
  
SetResetFF .. set/reset flip flop 
SetResetFF.ar(set, reset) 
  
ToggleFF .. toggle flip flop 
ToggleFF.ar(trig) 
  
Latch .. sample and hold 
Latch.ar(in, trig) 
  
Gate .. gate or hold 
Gate.ar(in, trig) 
  
Line .. line 
Line.ar(start, end, dur, mul, add) 
  
XLine .. exponential growth/decay 
XLine.ar(start, end, dur, mul, add) 
  
LinExp .. linear range to exponential range conversion 
LinExp.ar(in, srclo, srchi, dstlo, dsthi, mul, add) 
  
PulseCount .. pulse counter 
PulseCount.ar(trig, reset) 
  
PulseDivider .. pulse divider 
PulseDivider.ar(trig, div) 
  
Sequencer .. clocked values 
Sequencer.ar(sequence, clock, mul, add) 
  
ImpulseSequencer .. clocked single sample impulse outputs 
ImpulseSequencer.ar(levelArrayRef, clock, mul, add) 
  
ZeroCrossing .. zero crossing frequency follower 
ZeroCrossing.ar(in) 
  
### Amplitude Operators 
  
Compander .. compresser, expander, limiter, gate, ducker 
Compander.ar(input, control, threshold, slopeBelow, slopeAbove, clampTime, relaxTime, mul, add) 
  
Normalizer .. flattens dynamics 
Normalizer.ar(input, level, lookAheadTime) 
  
Limiter .. peak limiter 
Limiter.ar(input, level, lookAheadTime) 
  
Amplitude .. amplitude follower 
Amplitude.ar(input, attackTime, releaseTime, mul, add) 
  
Pan2 .. stereo pan (equal power) 
Pan2.ar(in, pos, level) 
  
Pan4 .. quad pan (equal power) 
Pan4.ar(in, xpos, ypos, level) 
  
PanB .. ambisonic B-format pan 
PanB.ar(in, azimuth, elevation, gain) 
  
LinPan2 .. linear stereo pan 
LinPan2.ar(in, pan) 
  
LinPan4 .. linear quad pan 
LinPan4.ar(in, xpan, ypan) 
  
LinXFade2 .. linear stereo cross fade 
LinXFade2.ar(l, r, pan) 
  
LinXFade4 .. linear quad cross fade 
LinXFade4.ar(lf, rf, lb, rb, xpan, ypan) 
  
### Delays 
  
Delay1 .. one sample delay 
Delay1.ar(in, mul, add) 
  
Delay2 .. two sample delay 
Delay2.ar(in, mul, add) 
  
DelayN .. simple delay line, no interpolation 
DelayN.ar(in, maxdtime, delaytime, mul, add) 
  
DelayL .. simple delay line, linear interpolation 
DelayL.ar(in, maxdtime, delaytime, mul, add) 
  
DelayA .. simple delay line, all pass interpolation 
DelayA.ar(in, maxdtime, delaytime, mul, add) 
  
CombN .. comb delay line, no interpolation 
CombN.ar(in, maxdtime, delaytime, decaytime, mul, add) 
  
CombL .. comb delay line, linear interpolation 
CombL.ar(in, maxdtime, delaytime, decaytime, mul, add) 
  
CombA .. comb delay line, all pass interpolation 
CombA.ar(in, maxdtime, delaytime, decaytime, mul, add) 
  
AllpassN .. all pass delay line, no interpolation 
AllpassN.ar(in, maxdtime, delaytime, decaytime, mul, add) 
  
AllpassL .. all pass delay line, linear interpolation 
AllpassL.ar(in, maxdtime, delaytime, decaytime, mul, add) 
  
AllpassA .. all pass delay line, all pass interpolation 
AllpassA.ar(in, maxdtime, delaytime, decaytime, mul, add) 
  
MultiTap .. multi tap delay 
MultiTap.ar(delayTimesArray, levelsArray, in, mul, add) 
  
DelayWr .. write into a delay line 
DelayWr.ar(buffer, in, mul, add) 
  
TapN .. tap a delay line, no interpolation 
TapN.ar(buffer, delaytime, mul, add) 
  
TapL .. tap a delay line, linear interpolation 
TapL.ar(buffer, delaytime, mul, add) 
  
TapA .. tap a delay line, all pass interpolation 
TapA.ar(buffer, delaytime, mul, add) 
  
GrainTap .. granulate a delay line 
GrainTap.ar(buffer, grainDur, pchRatio, pchDispersion, timeDispersion, overlap, mul, add) 
  
PitchShift .. time domain pitch shifter 
PitchShift.ar(in, winSize, pchRatio, pchDispersion, timeDispersion, mul, add) 
  
PingPongN .. ping pong delay, no interpolation 
PingPongN.ar(leftIn, rightIn, maxdtime, delaytime, feedback, mul, add) 
  
PingPongL .. ping pong delay, linear interpolation 
PingPongL.ar(leftIn, rightIn, maxdtime, delaytime, feedback, mul, add) 
  
### Frequency Domain 
  
FFT .. fast fourier transform 
FFT.ar(size, offset, cosTable, inputWindow, outputWindow, realInput, imaginaryInput) 
  
IFFT .. inverse fast fourier transform 
IFFT.ar(size, offset, cosTable, inputWindow, outputWindow, realInput, imaginaryInput) 
  
### Samples and I/O 
  
PlayBuf .. sample playback from a Signal buffer 
PlayBuf.ar(signal, sigSampleRate, playbackRate, offset, loopstart, loopend, mul, add) 
  
RecordBuf .. record or overdub audio to a Signal buffer 
RecordBuf.ar(buffer, in, recLevel, preLevel, reset, run, loopMode) 
  
AudioIn .. read audio from hardware input 
AudioIn.ar(channelNumber) 
  
DiskIn .. stream audio in from disk file 
DiskIn.ar(soundFile, loopFlag, startFrame, numFrames) 
  
DiskOut .. stream audio out to disk file 
DiskOut.ar(soundFile, numFrames, channelArray) 
  
### Event Spawning 
  
Pause .. turn a process on and off 
Pause.ar(eventFunc, level) 
  
Spawn .. timed event generation 
Spawn.ar(eventFunc, numChannels, nextTime, maxRepeats, mul, add) 
  
TSpawn .. signal triggered event generation 
TSpawn.ar(eventFunc, numChannels, maxRepeats, trig, mul, add) 
  
Voicer .. MIDI triggered event generation 
Voicer.ar(eventFunc, numChannels, midiChannel, maxVoices, mul, add) 
  
XFadeTexture .. cross fade events 
XFadeTexture.ar(eventFunc, sustainTime, transitionTime, numChannels, mul, add) 
  
OverlapTexture .. cross fade events 
OverlapTexture.ar(eventFunc, sustainTime, transitionTime, overlap, numChannels, mul, add) 
  
Cycle .. spawn a sequence of events in a cycle 
Cycle.ar(array, numChannels, nextTime, maxRepeats, mul, add) 
  
RandomEvent .. spawn an event at random 
RandomEvent.ar(array, numChannels, nextTime, maxRepeats, mul, add) 
  
SelectEvent .. spawn an event chosen from a list by a function 
SelectEvent.ar(array, selectFunc, numChannels, nextTime, maxRepeats, mul, add) 
  
OrcScore .. play an event list with an orchestra 
OrcScore.ar(orchestra, score, numChannels, nextTime, maxRepeats, mul, add) 
  
### Misc 
  
Scope .. write audio to a SignalView 
Scope.ar(signalView, in) 
  
Mix .. mixdown channels in groups 
Mix.ar(channelsArray) 
  
K2A .. control rate to audio rate converter 
K2A.ar(in) 
  
Sink .. takes any number of inputs and outputs zero. Can be used to force execution order. 
Sink.ar(theInputArray) 
  
OutputProxy .. used as an output place holder by Spawners and Panners, etc. 
There is no reason for a user to create an OutputProxy directly. 

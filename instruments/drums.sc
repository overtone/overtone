// A collection of drums, mostly found on the web and mailing list.
// Thanks especially for the SOS drums from: 
// http://www.soundonsound.com/sos/jan02/articles/synthsecrets0102.asp

s.boot;
s.quit;


SynthDef("kick",
	{ arg out = 0, freq = 50, mod_freq = 5, mod_index = 5, sustain = 0.4, amp = 0.8, beater_noise_level = 0.025;
	var pitch_contour, drum_osc, drum_lpf, drum_env;
	var beater_source, beater_hpf, beater_lpf, lpf_cutoff_contour, beater_env;
	var kick_mix;
	pitch_contour = Line.kr(freq*2, freq, 0.02);
	drum_osc = PMOsc.ar(	pitch_contour,
				mod_freq,
				mod_index/1.3,
				mul: 1,
				add: 0);
	drum_lpf = LPF.ar(in: drum_osc, freq: 1000, mul: 1, add: 0);
	drum_env = drum_lpf * EnvGen.ar(Env.perc(0.005, sustain), 1.0, doneAction: 2);
	beater_source = WhiteNoise.ar(beater_noise_level);
	beater_hpf = HPF.ar(in: beater_source, freq: 500, mul: 1, add: 0);
	lpf_cutoff_contour = Line.kr(6000, 500, 0.03);
	beater_lpf = LPF.ar(in: beater_hpf, freq: lpf_cutoff_contour, mul: 1, add: 0);
	beater_env = beater_lpf * EnvGen.ar(Env.perc, 1.0, doneAction: 2);
	kick_mix = Mix.new([drum_env, beater_env]) * 2 * amp;
	Out.ar(out, [kick_mix, kick_mix])
}).store
Synth("kick")

SynthDef("soft-kick", {|amp= 0.5, decay= 0.1, attack= 0.001, freq= 60|
        var env, snd;
        env= EnvGen.kr(Env.perc(attack, decay), doneAction:2);
        snd= SinOsc.ar(freq, 0, amp);
        Out.ar(0, Pan2.ar(snd*env, 0));
}).send; 

Synth("soft-kick")
Synth("soft-kick", [\freq, 100])
Synth("kick", [\freq, 80, \decay, 0.2]) 

//variation with impulse click
SynthDef("round-kick", {|amp= 0.5, decay= 0.6, freq= 65|
        var env, snd;
        env= EnvGen.ar(Env.perc(0, decay), doneAction:2);
        snd= SinOsc.ar(freq, pi*0.5, amp);
        Out.ar(0, Pan2.ar(snd*env, 0));
}).store; 

Synth("round-kick")
Synth("round-kick", [\freq, 70, \decay, 0.6]) 

//variation with more sines
SynthDef("dry-kick", {|amp= 0.5, decay= 0.1, freq= 60|
        var env, snd;
        env= EnvGen.ar(Env.perc(0, decay), doneAction:2);
        snd= Mix(SinOsc.ar([freq, freq*2, freq-15], 0, amp));
        Out.ar(0, Pan2.ar(snd*env, 0));
        }).store; 
Synth("dry-kick")

SynthDef("big-kick", { |basefreq = 50, envratio = 3, freqdecay = 0.02, ampdecay = 0.5|
        var   fenv = EnvGen.kr(Env([envratio, 1], [freqdecay], \exp), 1) * basefreq,
        aenv = EnvGen.kr(Env.perc(0.005, ampdecay), 1, doneAction:2), out;
        out = SinOsc.ar(fenv.dup, 0.5pi, aenv);
        Out.ar([0,1] ,out);
        }).load(s);
Synth("big-kick")

SynthDef("hat",
	{arg out = 0, freq = 6000, sustain = 0.1, amp = 0.8;
	var root_cymbal, root_cymbal_square, root_cymbal_pmosc;
	var initial_bpf_contour, initial_bpf, initial_env;
	var body_hpf, body_env;
	var cymbal_mix;
	
	root_cymbal_square = Pulse.ar(freq, 0.5, mul: 1);
	root_cymbal_pmosc = PMOsc.ar(root_cymbal_square,
					[freq*1.34, freq*2.405, freq*3.09, freq*1.309],
					[310/1.3, 26/0.5, 11/3.4, 0.72772],
					mul: 1,
					add: 0);
	root_cymbal = Mix.new(root_cymbal_pmosc);
	initial_bpf_contour = Line.kr(15000, 9000, 0.1);
	initial_env = EnvGen.ar(Env.perc(0.005, 0.1), 1.0);
	initial_bpf = BPF.ar(root_cymbal, initial_bpf_contour, mul:initial_env);
	body_env = EnvGen.ar(Env.perc(0.005, sustain, 1, -2), 1.0, doneAction: 2);
	body_hpf = HPF.ar(in: root_cymbal, freq: Line.kr(9000, 12000, sustain),mul: body_env, add: 0);
	cymbal_mix = Mix.new([initial_bpf, body_hpf]) * amp;
	Out.ar(out, [cymbal_mix, cymbal_mix])
	}).store
Synth("hat")

SynthDef("soft-hat", {|amp= 0.5, decay= 0.1, freq= 6000|
            var env, snd;
            env= EnvGen.ar(Env.perc(0, decay), doneAction:2);
            snd= BPF.ar(GrayNoise.ar(amp), freq, 0.3);
            Out.ar(0, Pan2.ar(snd*env, 0));
            }).store; 
Synth("soft-hat") 

//variation with cutoff lfo
SynthDef("noise-hat", {|amp= 0.5, decay= 0.1, freq= 6000|
            var env, snd;
            env= EnvGen.ar(Env.perc(0, decay), doneAction:2);
            snd= BPF.ar(GrayNoise.ar(amp), Line.ar(freq, 50, decay*0.5)

                , env+0.1);
            Out.ar(0, Pan2.ar(snd*env, 0));
            }).store; 
Synth("noise-hat") 

SynthDef("bell-hat", {|amp= 0.5, decay= 0.1, freq= 6000|
            var env, snd, snd2;
            env= EnvGen.ar(Env.perc(0, decay), doneAction:2);
            snd= BPF.ar(GrayNoise.ar(amp), Line.ar(freq, 5, decay*0.5)

                , env+0.1);
            snd2= Mix(SinOsc.ar([4000, 6500, 5000], 0,  env*0.1));
            Out.ar(0, Pan2.ar(snd+snd2*env, 0));
            }).store; 
Synth("bell-hat") 

SynthDef("snare",
	{arg out = 0, sustain = 0.1, drum_mode_level = 0.25,
	snare_level = 40, snare_tightness = 1000,
	freq = 405, amp = 0.8;
	var drum_mode_sin_1, drum_mode_sin_2, drum_mode_pmosc, drum_mode_mix, drum_mode_env;
	var snare_noise, snare_brf_1, snare_brf_2, snare_brf_3, snare_brf_4, snare_reson;
	var snare_env;
	var snare_drum_mix;

	drum_mode_env = EnvGen.ar(Env.perc(0.005, sustain), 1.0, doneAction: 2);
	drum_mode_sin_1 = SinOsc.ar(freq*0.53, 0, drum_mode_env * 0.5);
	drum_mode_sin_2 = SinOsc.ar(freq, 0, drum_mode_env * 0.5);
	drum_mode_pmosc = PMOsc.ar(	Saw.ar(freq*0.85),
					184,
					0.5/1.3,
					mul: drum_mode_env*5,
					add: 0);
	drum_mode_mix = Mix.new([drum_mode_sin_1, drum_mode_sin_2, drum_mode_pmosc]) * drum_mode_level;

// choose either noise source below
//	snare_noise = Crackle.ar(2.01, 1);
	snare_noise = LFNoise0.ar(20000, 0.1);
	snare_env = EnvGen.ar(Env.perc(0.005, sustain), 1.0, doneAction: 2);
	snare_brf_1 = BRF.ar(in: snare_noise, freq: 8000, mul: 0.5, rq: 0.1);
	snare_brf_2 = BRF.ar(in: snare_brf_1, freq: 5000, mul: 0.5, rq: 0.1);
	snare_brf_3 = BRF.ar(in: snare_brf_2, freq: 3600, mul: 0.5, rq: 0.1);
	snare_brf_4 = BRF.ar(in: snare_brf_3, freq: 2000, mul: snare_env, rq: 0.0001);
	snare_reson = Resonz.ar(snare_brf_4, snare_tightness, mul: snare_level) ;
	snare_drum_mix = Mix.new([drum_mode_mix, snare_reson]) * 5 * amp;
	Out.ar(out, [snare_drum_mix, snare_drum_mix])
	}).store
Synth("snare")

SynthDef("soft-snare", {|amp= 0.5, decay= 0.1, freq= 1000|
            var env, snd;
            env= EnvGen.ar(Env.perc(0, decay), doneAction:2);
            snd= BPF.ar(GrayNoise.ar(amp), freq, 3);
            Out.ar(0, Pan2.ar(snd*env, 0));
            }).store; 
Synth("soft-snare") 

SynthDef("snare2", {|amp= 0.5, decay= 0.1, freq= 1000|
            var env, snd;
            env= EnvGen.ar(Env.perc(0, decay), doneAction:2);
            snd= RLPF.ar(GrayNoise.ar(amp), freq, Line.ar(0.1, 0.9, decay));
            Out.ar(0, Pan2.ar(snd*env, 0));
            }).store; 
Synth("snare2") 

SynthDef("snare3", {|amp= 0.5, decay= 0.1, freq= 1000|
            var env, snd, env2, snd2;
            env= EnvGen.ar(Env.perc(0, decay), doneAction:2);
            env2= EnvGen.ar(Env.perc(0, decay), doneAction:2);
            snd= RLPF.ar(GrayNoise.ar(amp), freq, Line.ar(0.1, 0.9, decay));
            snd2= WhiteNoise.ar(amp)*env2;
            Out.ar(0, Pan2.ar(snd*env, 0));
            }).store; 
Synth("snare3") 

// Some more abstract 'snares'
SynthDef("snare-x", { |freq=100, sustain=5, amp=0.1|
        var mod, sound, env, saw, filter;
        //mod = XLine.kr(freq*1,freq,sustain); von freq nach freq
        env = EnvGen.ar(Env.perc(0.04, sustain,amp*10,-60),doneAction:2); //amp*10 statt 1
        saw = WhiteNoise.ar(0.06 * [1,1]) * Saw.ar(10 * [1,1]).sum; //sum?
        filter = BPF.ar(saw, LFNoise1.kr(0.1).exprange(5000, 20000), 0.9);
        //sound = SinOsc.ar(mod,0,amp.dup); keine amp
        Out.ar(0, filter * env);
        };
        ).load(s);
Synth("snare-x")

SynthDef("snare-y", { |sustain=3, amp=0.5, freq=20, hard=0|
        var sound, env, env2;
        env = EnvGen.ar(Env.perc(0.025, sustain*(1-hard), 1, -12));
        env2 = EnvGen.ar(Env.perc(0.05, sustain*1.5), doneAction: 2);
        sound = Decay.ar(Impulse.ar(0), 0.00072, WhiteNoise.ar(amp.dup)) + WhiteNoise.ar(0.1).dup;
        sound = sound * env;
        sound = CombL.ar(sound, freq.reciprocal, freq.reciprocal, sustain);
        Out.ar(0, sound * env2);
        };
        ).load(s);
Synth("snare-y")

SynthDef("snare-z", { |sustain=3, amp=0.5, freq=20, hard=0|
        var sound, env, env2;
        env = EnvGen.ar(Env.perc(0, sustain*(1-hard), 1, -12));
        env2 = EnvGen.ar(Env.perc(0, sustain*1.5), doneAction: 2);
        sound = Decay.ar(Impulse.ar(0), sustain * 0.1, GrayNoise.ar([1, 1] * 0.3), WhiteNoise.ar(0.1.dup) + 0.3);

        sound = sound * (LFSaw.ar(Rand(5, 10)).max(0) + 1) * (env * amp);
        Out.ar(0, sound * env2);
        };
        ).load(s);
Synth("snare-z") 

SynthDef("tom",
	{arg out = 0, sustain = 0.4, drum_mode_level = 0.25,
	freq = 90, drum_timbre = 1.0, amp = 0.8;
	var drum_mode_sin_1, drum_mode_sin_2, drum_mode_pmosc, drum_mode_mix, drum_mode_env;
	var stick_noise, stick_env;
	var drum_reson, tom_mix;

	drum_mode_env = EnvGen.ar(Env.perc(0.005, sustain), 1.0, doneAction: 2);
	drum_mode_sin_1 = SinOsc.ar(freq*0.8, 0, drum_mode_env * 0.5);
	drum_mode_sin_2 = SinOsc.ar(freq, 0, drum_mode_env * 0.5);
	drum_mode_pmosc = PMOsc.ar(	Saw.ar(freq*0.9),
								freq*0.85,
								drum_timbre/1.3,
								mul: drum_mode_env*5,
								add: 0);
	drum_mode_mix = Mix.new([drum_mode_sin_1, drum_mode_sin_2, drum_mode_pmosc]) * drum_mode_level;
	stick_noise = Crackle.ar(2.01, 1);
	stick_env = EnvGen.ar(Env.perc(0.005, 0.01), 1.0) * 3;
	tom_mix = Mix.new([drum_mode_mix, stick_env]) * 2 * amp;
	Out.ar(out, [tom_mix, tom_mix])
	}).store
Synth("tom")

SynthDef("clap", { |freq=100, sustain=5, amp=0.9|
        var mod, sound, env, saw, filter;
        mod = XLine.kr(freq*1,freq,sustain);
        env = EnvGen.ar(Env.perc(0.02, sustain,1,-200),doneAction:2);
        saw = LFPulse.kr(0.6,0,0.5) * WhiteNoise.ar(0.07 * [1,1]) * Saw.ar(1000 * [1,1]).sum;
        filter = BPF.ar(saw, LFNoise1.kr(0.1).exprange(10000, 20000), 0.9);

        sound = SinOsc.ar(mod,0,amp.dup);
        Out.ar(0, filter * env);
        };
).load(s);
Synth("clap");
Synth("clap", ["freq", 300, "amp", 1.5]);


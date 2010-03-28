SynthDef("my_PlayBuf", { arg out=0,bufnum=0;
	Out.ar(out,
		PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum))
	)
}).send(s);
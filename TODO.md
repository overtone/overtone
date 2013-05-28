;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


## Helpers

* rather than using a plain multiply for volume adjustment, we should have
a built-in helper that uses dbamp and multiply to adjust volume logarithmically,
corresponding to perception.
 - use a range of 0 - 1.0, so it works automatically with GUI controls and other
   ugens.

## Studio

* have a mechanism to route sound to a preview channel for listening to
something in headphones to try it out.

* create a basic EQ that can be used on any track

* implement a portamento helper on top of slew

## General

* view a table or tree of running synths with the ability to kill and maybe
modify control params

* allow for re-arranging nodes and groups using a tree/table view

## Synths and Audio:

The following synth doesn't compile. Figure out how to correctly handle args to :ir ugens:

    (defsynth my-phasor-player [buf 0 rate 1]
      (let [num-samps (buf-samples:ir buf)
            pos       (phasor:ar 0 rate 0 num-samps)]
        (out 0 (buf-rd 1 buf pos))))



## SC Tweets:

    {loop{play{GVerb.ar(LeakDC.ar(Crackle.ar([a=Phasor.ar(1,Line.kr(0.0020.rand,0,9),0,1),a],0.5,0)))*0.05*EnvGen.kr(Env.sine(9))};5.wait}}.fork

    {loop{play{|q|c=EnvGen.kr(Env.sine(6),b=(6..1));Pan2.ar(PMOsc.ar(a=40*b*b.rand,a/2-1,1+q*c*90,c,0.01).sum.tan,2.0.rand-1)*c*5};1.wait}}.fork

    play{b=LocalBuf(2**20,2).clear;n=LFNoise2.ar(Rand(500)!2);w=Warp1.ar(2,b,n+1/2,[a=0.5,1,2,4]).sum/50;RecordBuf.ar(LeakDC.ar(w+n),b,0,a,a);w}

    {loop{play{GVerb.ar(Pan2.ar(HenonC.ar(11025/(16.rand+1),0.4.rand+1,0.3.rand),2.rand-1).tanh)*0.01*EnvGen.kr(Env.sine(4))};0.8.wait}}.fork

    play{n=12;Splay.ar(Ringz.ar(Decay2.ar(Impulse.ar({2.0.rand.round(0.25)}!n),0.01,0.1,0.1),{|i|50+(i*50)+4.0.rand2}!n,{8.0.rand}!n),0.1)}

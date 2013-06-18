* There should be an unmap-node-controls function

* Why is there such a difference between the datastructure representing
  a ugen fn and the datastructure representing a ugen (SCUgen)? Seems
  that they should be identical except for the addition of an :args key
  for the SCUgen.

* sc.synth currently places all synth instances into the synth-group by default,
which is an Overtone specific feature that should probably be done in studio.
Maybe we should have the creation of synthdefs be on overtone.sc.synth, but
the live creation of them and specialization in studio?  Discuss on list.

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

* create a couple functions to append a ugen to the root of a synth, for
attaching out, pan, etc...

* inst and synth forms should evalute the defaults rather than expecting them to
be numbers (to use equations, constant vars, etc...)

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

* figure out how best to attach meta-data to the [unary,binary]-op-ugen

## Synths and Audio:

* make midi->hz and friends multimethods (or use types and protocols?) so they
operate immediately on numbers, but generate ugens on input proxy or ugen
arguments.

* add docs to Unary and Binary op ugens

## SC Tweets:

{loop{play{GVerb.ar(LeakDC.ar(Crackle.ar([a=Phasor.ar(1,Line.kr(0.0020.rand,0,9),0,1),a],0.5,0)))*0.05*EnvGen.kr(Env.sine(9))};5.wait}}.fork

{loop{play{|q|c=EnvGen.kr(Env.sine(6),b=(6..1));Pan2.ar(PMOsc.ar(a=40*b*b.rand,a/2-1,1+q*c*90,c,0.01).sum.tan,2.0.rand-1)*c*5};1.wait}}.fork

play{b=LocalBuf(2**20,2).clear;n=LFNoise2.ar(Rand(500)!2);w=Warp1.ar(2,b,n+1/2,[a=0.5,1,2,4]).sum/50;RecordBuf.ar(LeakDC.ar(w+n),b,0,a,a);w}

{loop{play{GVerb.ar(Pan2.ar(HenonC.ar(11025/(16.rand+1),0.4.rand+1,0.3.rand),2.rand-1).tanh)*0.01*EnvGen.kr(Env.sine(4))};0.8.wait}}.fork

play{n=12;Splay.ar(Ringz.ar(Decay2.ar(Impulse.ar({2.0.rand.round(0.25)}!n),0.01,0.1,0.1),{|i|50+(i*50)+4.0.rand2}!n,{8.0.rand}!n),0.1)}


play{Splay.ar({|i|HPF.ar(a=Pulse;a.ar(a.ar(i+4/32).lag3(0.1,8-i)+1*99,a.ar(j=i+1)*a.ar(i+8/j)+a.ar(8/j).lag3(8)),50)}!8)/2}//

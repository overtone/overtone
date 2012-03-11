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

* route all synths and samplers to a main mixer bus, providing panning, EQ, and
volume controls.
  - figure out how FX should work into this

* have a mechanism to route sound to a preview channel for listening to
something in headphones to try it out.

* create a basic EQ that can be used on any track

* basic record functionality for root group
 - Sam's disk-out examples

* create a basic volume and pan control node that can be used for the master
volume and per synth-track control

* implementing some basic midi mapping

* fill out metronome functionality

* hook up the sequencer

* implement a portamento helper on top of slew

* Instrument groups are nil if they're created before the server is booted

## General

* create a function that prints out the currently running synths
 - maybe use node-tree + info from the synth-groups

* view a table or tree of running synths with the ability to kill and maybe
modify control params

* allow for re-arranging nodes and groups using a tree/table view

* figure out how best to attach meta-data to the [unary,binary]-op-ugen

## Synths and Audio:

* make midi->hz and friends multimethods (or use types and protocols?) so they
operate immediately on numbers, but generate ugens on input proxy or ugen
arguments.

* add docs to Unary and Binary op ugens

* add another argument mode to ugens so that buffers and samples can be passed
to ugens as arguments and their :id property will be used
  - get rid of UGen wrapper function currently doing this for all ugens

* implement the rest of the argument modes for ugens, as described in
  docs/dev/core/ugen_definition.mdml.
 (currently there is just :append-seq)

## Ugens

## SC Tweets:

{loop{play{GVerb.ar(LeakDC.ar(Crackle.ar([a=Phasor.ar(1,Line.kr(0.0020.rand,0,9),0,1),a],0.5,0)))*0.05*EnvGen.kr(Env.sine(9))};5.wait}}.fork

{loop{play{|q|c=EnvGen.kr(Env.sine(6),b=(6..1));Pan2.ar(PMOsc.ar(a=40*b*b.rand,a/2-1,1+q*c*90,c,0.01).sum.tan,2.0.rand-1)*c*5};1.wait}}.fork

play{b=LocalBuf(2**20,2).clear;n=LFNoise2.ar(Rand(500)!2);w=Warp1.ar(2,b,n+1/2,[a=0.5,1,2,4]).sum/50;RecordBuf.ar(LeakDC.ar(w+n),b,0,a,a);w}

{loop{play{GVerb.ar(Pan2.ar(HenonC.ar(11025/(16.rand+1),0.4.rand+1,0.3.rand),2.rand-1).tanh)*0.01*EnvGen.kr(Env.sine(4))};0.8.wait}}.fork

play{n=12;Splay.ar(Ringz.ar(Decay2.ar(Impulse.ar({2.0.rand.round(0.25)}!n),0.01,0.1,0.1),{|i|50+(i*50)+4.0.rand2}!n,{8.0.rand}!n),0.1)}

== 0.2 Release

* Get Undo/Redo working again

* Cleanup resizing so everything works correctly and allows for customizing the
workspace.

== Studio

* route all synths and samplers to a main mixer bus, providing panning, EQ, and
volume controls.
  - figure out how FX should work into this

* have a mechanism to route sound to a preview channel for listening to
something in headphones to try it out.

* create a basic EQ that can be used on any track

* basic record functionality for root group

* create a basic volume and pan control node that can be used for the master
volume and per synth-track control

* implementing some basic midi mapping

* fill out metronome functionality

* hook up the sequencer

== General

* make things work where there are multiple "roots" in a synthdef graph
 - got around it for now, but we want this for spectrograms...

* figure out if the play-buf ugen should be able to have the number of channels
as an input parameter, in which case we need to modify the num-outs mode.

* creating a group for every synth might be overkill, because it ends up filling
up our group ID space unneccessarily.  Maybe we need to keep synth a bit lower
level and start working on a higher level abstraction that automatically does
some of these things, definst?

* do something different with overtone.live rather than the immigrate stuff so
we can more easily develop in Overtone libs and then use or require just what
we are changing (currently their are conflicts because vars overshadow the
immigrated vars in overtone.live)

* create a function that prints out the currently running synths
 - maybe use node-tree + info from the synth-groups

* view a table or tree of running synths with the ability to kill and maybe
modify control params

* allow for re-arranging nodes and groups using a tree/table view

* write a number adjustment label to replace the lame looking JSpinner 
 - use a regular JLabel, and take min, max, step args
 - on drag adjust the value and call handlers

* figure out how best to attach meta-data to the [unary,binary]-op-ugen

== Editor 

* create and open new source file in the editor, file-new

* Add Undo/Redo to the editor

* make text editor have two modes, sort of VI style
 - non-editing mode makes functions clickable and puts docs into a help panel
 - support vi commands for motions, search and replace, and selection

* add more key-commands for evaluation
 - current top level form 
 - whole file (or current-selection)
 - current enclosing top-level form
 - current immediate form

* either improve the jsyntax clojure parser so we can have nice highlighting of
ugen names and Overtone functions, or else replace it with our own.  (Maybe
from paredit.clj...)

* add some par-edit style form modification commands
 - look into paredit.clj on github

* get some basic keymaps setup with basic, vim and emacs inspired bindings

* implement motion commands

* contro [+|-] to adjust font size

== Curve Editor

* add support for different curve types for the line segments
 - the curve functions are implemented in overtone.core.envelope, so we can 
hopefully do this by just rendering the lines emitted by these functions.

* support add and removing control points

* support setting loop and release control points ala SuperColliders envelopes

* adjust max size in seconds

* scale in and out

* add animated, path-following nodes showing the current location of the synth
being driven by the envelope

== Oscilloscope

* zooming

* paint and erase waveform data

* implement a wave drawing window so data can be directly inserted into a buffer

=== Spectrogram

* do FFT and view frequency data

* apply various PV filters directly to active buffer and view output

== Synths and Audio:

* make midi->hz and friends multimethods (or use types and protocols?) so they
operate immediately on numbers, but generate ugens on input proxy or ugen
arguments.

* do some sanity checking regarding the default group, make sure we reset
correctly, add synths and nodes correctly, etc...

* add docs to Unary and Binary op ugens 

* add another argument mode to ugens so that buffers and samples can be passed
to ugens as arguments and their :id property will be used 
  - get rid of UGen wrapper function currently doing this for all ugens

* implement the rest of the argument modes for ugens, as described in
  docs/dev/core/ugen_definition.mdml.
 (currently there is just :append-seq)

* Test and expand the Scope synth to support really nice audio and fft data
visualization.
 - also create level meter, pan, and EQ synths

* Create a mini Clojure/Java implementation of the SuperCollider interface on
top of Java sound so we can run inside of the browser, on phones, etc...

== Midi and Devices:

* Start thinking about some kind of generic device layer abstraction so we can
simplify the task of adding support for new controllers.  Maybe we can have a
learn mode where you plug in a device, hit learn, and then it builds a
configuration profile for your device by letting you hit controls and then
optionally label things or something...

* Put some nice error messages in the midi code so you know when you pass the
wrong type of device, etc... 

== OSC: 

* complete implementation of osc bundle reception, and beef up the unit tests
 - bundles need to be recursively decoded
 - should return a collection of packets, and each bundle and message should
   have the src-host and src-port attached so we can reply to anything.

== GUI

* Create a node based synthdef viewer, and then editor (FlowControl)

== Networking

* search, browse and download remote synth libraries 


-----------------------------

* maybe write out metronome time stamp so we can continue on boot after restart?

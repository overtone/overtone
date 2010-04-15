* basic record functionality for root group
* make sure scope always works for root group
* try limit scope CPU usage
* implementing some basic midi mapping
* view a table or tree of running synths with the ability to kill and maybe
modify control params
* fill out metronome functionality
* hook up the sequencer
* get some basic keymaps setup with basic, vim and emacs inspired bindings
* implement motion commands
* allow for re-arranging nodes and groups using a tree/table view
* create a basic volume and pan control node that can be used for the master
volume and per synth-track control
* create a basic EQ that can be used on any track
* do some sanity checking regarding the default group, make sure we reset
correctly, add synths and nodes correctly, etc...
* implement a wave drawing window so data can be directly inserted into a buffer
* complete the jline support in the repl so we can get history and tab
completion working
* either improve the jsyntax clojure parser so we can have nice highlighting of
ugen names and Overtone functions, or else replace it with our own.  (Maybe
from paredit.clj...)
* add docs to Unary and Binary op ugens 


== 0.2 Release

- make things work where there are multiple "roots" in a synthdef graph

== General:

* create a function that prints out the currently running synths

* write a number adjustment label to replace the lame looking JSpinner 
 - use a regular JLabel, and take min, max, step args
 - on drag adjust the value and call handlers

* create some kind of standardized tool-box for things like the color chooser
and other random side-bar tools.

* figure out how to organize stuff in scene window
 - tabs, collapsing, resizing, etc...

* either find icons that work better or just use some styled text that feel like
hyperlinks for the editor buttons

* figure out how best to attach meta-data to the [unary,binary]-op-ugen
* functions

== Editor 

* make text editor have two modes, sort of VI style
 - non-editing mode makes functions clickable and puts docs into a help panel
 - support vi commands for motions, search and replace, and selection

* add some key-commands for evaluation
 - whole file (or current-selection)
 - current enclosing top-level form
 - current immediate form

* add some par-edit style form modification commands

== Curve Editor

* add support for different curve types for the line segments

* support add and removing control points

* support setting loop and release control points ala SuperCollider's envelopes

* adjust max size in seconds

* scale in and out

* add animated, path-following nodes showing the current location of the synth
being driven by the envelope

== Oscilloscope

* zooming

* paint and erase waveform data

=== Spectrogram

* do FFT and view frequency data

* apply various PV filters directly to active buffer and view output

== Synths and Audio:

* implement the rest of the argument modes for ugens, as described in
  docs/dev/core/ugen_definition.mdml.
 (currently there is just :append-seq)

* Test and expand the Scope synth to support really nice audio and fft data
visualization.
 - also create level meter, pan, and EQ synths

* Create a fully native Clojure implementation of the SuperCollider interface on
* top of Java sound
so we can run inside of the browser, on phones, etc...

== Midi and Devices:

* Start thinking about some kind of generic device layer abstraction so we can
simplify the task of adding support for new controllers.  Maybe we can have a
learn mode where you plug in a device, hit learn, and then it builds a
configuration profile for your device by letting you hit controls and then
optionally label things or something...

* Put some nice error messages in the midi code so you know when you pass the
* wrong type of device, etc... 

== OSC: 

* complete implementation of osc bundle reception, and beef up the unit tests
 - bundles need to be recursively decoded
 - should return a collection of packets, and each bundle and message should
   have the src-host and src-port attached so we can reply to anything.

== GUI

* Create a node based synthdef viewer, and then editor

== Dynamic Editor

* accesses functions from 

== Networking

* search, browse and download remote synth libraries 


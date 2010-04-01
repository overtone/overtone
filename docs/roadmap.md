### Project:
* put together a one-shot ubuntu install script that sets you up with
scsynth, the necessary jar-files, etc...

* create a series of livecode session movies going through how things work

### Features:

#### Application 

* Audio samples: loading, playing, and manipulating
 - support a variety of file formats, not just wav

#### Audio

* audio in and out
 - select line-in/out, mic...
 - show a scope along side the chooser and with a single click connect up
   so it's easy to see what you are hooking up with quickly

* SynthDefs 
 - sound exploration mode:
  * bring up the scope and an FFT window with some helpful controls for exploring sound spaces
  * either bring up a window or make it easy to do in code:
   - connect arpeggiators, chord progressors, midi-in, etc., to your
live instrument definitions so you can mess with the parameters and the synthdef while 
hearing useful audio input
 - develop more of a dsl for:
   * defining instruments
   * managing voices, fx, and busses
- synthdefs should automatically register their information with a synthdef library
area so users can easily browse available instruments and fx.

* Implement some basic optimizations for BinaryOpUGens to do things like:
 - pre-compute constants
 - combine '*' and '+' ops into MulAdd
 * look at BasicOpsUGen.sc in supercollider source
 * we can do this either at the form processing phase, connected to (replace-arithmetic ...)
   or in the ugen connection phase in (detail-ugens ...)

#### Musical

* Tempo and time management 
 
* Arpeggiator(s)
* Chord progressors
 - create a library of things like 12-bar-blues, etc., and make it easy
to connect them up to instruments.

* Scales
 - add more knowledge about scale construction, modes, etc
 - create a library of interesting scales

* musical processes
 - lazy sequences of notes (combine or separate freq, velocity, duration?)
 - markov models (basically gets us finite state machines too right?)
(markov funky-jazz
  a 3 -> b  ; b 3 times more likely than c
  a -> c
  b -> d
  c -> d
  d -> a)

* synchronization 
 - so an overtone client can be in step with an externally generated clock
 - so multiple overtone clients can be in step

* Re-write the tune-up script in clojure using the shell-out (sh ...) function from clojure.contrib
 - will need to figure out how to correctly manage classpath bullshit...

### Visualization and Dynamic Interface Generation

It would be very cool if voices, fx and generators could register various named knobs, triggers and observable sequences that could be used to interact and visualize live-coded musical systems.

* penumbra GL/shaders for clojure http://github.com/ztellman/penumbra/tree/master
* Game engine http://www.jmonkeyengine.com/

### Distributed, real-time jamming
Use: Plasma, graph based networking

### Cloud storage and collaboration 
Use: google app-engine/wave cloud server

### VST Plugin capabliity
Use: jVSTWrapper (http://jvstwrapper.sourceforge.net/

### Computer vision (multi-touch detection and object tracking)

#### Touch APIs:
* Multitouch lib from TU-Berlin http://code.google.com/p/multitouch/
* Sparsh UI http://code.google.com/p/sparsh-ui/

#### Custom image processing:
* http://javavis.sourceforge.net/
* Java OpenCV http://ubaa.net/shared/processing/opencv/
* Or implement processing pipeline in C++ with OpenCV and use 
  clojure-jna to interface: http://github.com/Chouser/clojure-jna/tree/master


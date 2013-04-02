# resonate 2013 clojure/overtone workshop

This namespace contains a collection of examples created for and during the [Clojure](http://clojure.org) &
[Overtone](https://github.com/overtone/overtone) workshop on 21 March
2013 in Belgrade as part of [Resonate 2013 festival](http://resonate.io/2013/).

The original repo URL is: http://hg.postspectacular.com/resonate-2013/

All examples are fully documented and meant to be executed
interactively in the REPL:

- **ex01_phrasestudy**: generating patterns using clojure sequence API
- **ex02_bday**: defining a scale-free melody and manipulating it
- **ex03_graph**: using [Incanter](http://incanter.org/) to explain harmonics and their impact
on waveforms
- **ex04_midi**: building a simple MIDI controllable drumkit w/ samples
  from [Freesound.org](http://freesound.org)
- **ex05_synthesis**: a closer look at Overtone's & [Supercollider's](http://supercollider.sourceforge.net/)
synthesis building blocks
- **ex06_quilstep**: using [Quil](https://github.com/quil/quil)
    (Clojure wrapper for [Processing](http://processing.org)) to create a simple GUI for the dubstep synth from ex05

# Workshop environment (Eclipse)

Clojure can be integrated with may different tools, but if you're
completely new to Clojure, I recommend using Eclipse with the [Counterclockwise](https://code.google.com/p/counterclockwise/) (CCW) plugin:

## Setup Eclipse + CCW

- [Download Eclipse](http://eclipse.org/downloads/) (e.g. Classic or Eclipse for Java developers)
- Launch Eclipse
- Choose/confirm workspace folder (only on 1st start)
- Choose `Help > Install new software...`
- In the dialog, press `Add...` to create a new plugin update site and
use these details:
    - **Name**: CCW
    - **URL**: http://ccw.cgrand.net/updatesite/
- Tick the box for "Clojure Programming", then click `Next` and follow
  instructions
- Restart Eclipse

## Import project

- Choose `File > Import... > General > Existing project`
- Browse to folder where you checked out/downloaded this project
- Tick box next to "resonate-2013"
- Click `Finish`

## Launching project REPL

- Open one of the examples in the project's `src` folder
- Right click in the editor and choose `Clojure > Load File in REPL` or press (Alt+Command+L)

Alternatively...

- Right click on "resonate-2013" project name in Package Explorer
- Choose `Run As > Clojure Application`

# Using Leiningen

[Leiningen](http://leiningen.org) is the de-facto standard build tool
for Clojure (It's also included in the Eclipse CCW plugin).
If you prefer running a REPL directly via the command line, simply:

- Clone or download this repo
- `cd` into the project directory
- `lein repl` to start the REPL

## License

Copyright © 2013 Karsten Schmidt

Distributed under the MIT License, same as Overtone.

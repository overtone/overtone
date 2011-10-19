          _____                 __
         / __  /_  _____  _____/ /_____  ____  ___
        / / / / | / / _ \/ ___/ __/ __ \/ __ \/ _ \
       / /_/ /| |/ /  __/ /  / /_/ /_/ / / / /  __/
       \____/ |___/\___/_/   \__/\____/_/ /_/\___/

                          Programmable Music.

## Live-coding & musical exploration

Overtone is an Open Source toolkit for creating synthesizers and making music.  It provides:

* A Clojure API to the SuperCollider synthesis engine
* A growing library of musical functions (scales, chords, rhythms, arpeggiators, etc.)
* Metronome and timing system to support live-coding and sequencing
* Plug and play midi device I/O
* Simple Open Sound Control (OSC) message handling

## Quick Setup

### Installation

    # Install cake (or lein)
    # http://clojure-cake.org/

    $ cake new insane-noises

    # add the following dependencies to insane-noises/project.clj
    # [org.clojure/clojure "1.3.0"]
    # [overtone "0.5.0"]

    $ cd insane-noises
    $ cake deps


### Server Option A: Internal
    # Linux users - start jackd

    $ cake repl
    user=> (use 'overtone.live)


### Server Option B: External
    # Download and install SuperCollider:
    # http://supercollider.sourceforge.net/downloads/

    $ cake repl
    user=> (use 'overtone.core)
    user=> (boot-external-server)


### Your First Sounds

    ; sin-osc creates a sine wave at the specified Hz (440 in this case)
    ; and pan2 makes the signal stereo
    ; demo simply plays the synth for the specified time in seconds:

    user=> (demo 5 (pan2 (sin-osc 440)))

    ; Defining a new synthesizer instrument with the definst macro will return a
    ; function which can be used to trigger the inst.

    user=> (definst beep [freq 440] (sin-osc freq))
    user=> (beep)
    user=> (stop) # (this command will kill all running synths)

    ; Call the ctl function to modulate any params and to eventually kill that instrument:

    user=> (beep)
    user=> (ctl beep :freq 880)
    user=> (kill beep) # (this will just kill the specific instrument)

## External & Internal Servers

Overtone supports both internal and external instances of `scsynth` - the SuperCollider server. The internal server is good for quick setup (there are no external dependencies to install and allows fast access to server buffers for transferring sound data and using the scopes). The external server requires a separate installation of SuperCollider itself but is more robust in that crashes in the server (through malformed synth designs etc.) don't also crash the JVM (which is the case for the internal server). It is also possible to connect multiple separate clients to an already running external scsynth instance.

Note - the internal server is not currently supported for all architecture/operating system combinations. However, the external server should work everywhere.

## Getting Started Videos

     ___|)_______________|\________________|\______________|\_______________|\________
    |___/___||___________|_________________|_______________|________________|_________||
    |__/|___||.________,-.___( )___o-;___,-.___o-;__( )__,-.________o-; __,-.___o-;__.||
    |_/(|,\_||.___(_)__`-'___|______/____`-'____/___|____`-'___(_)___/____`-'____/___.||
    |_\_|_/_||____|__________|______________________|__________|______________________||
        |         |          |/                     |/         |
      (_|         |/                                           |/

Head over to Vimeo for a fast-paced 4 minute introduction to live-coding with Overtone to see what's possible

  http://vimeo.com/22798433

There are also the following tutorials:

* Setting up an Overtone Development Environment - Running on Edge http://vimeo.com/25102399
* How to Hack Overtone with Emacs http://vimeo.com/25190186

## Cheat Sheet

For a quick glance at all the exciting functionality Overtone puts at your musical fingertips check out the cheat sheet:

  https://github.com/downloads/overtone/overtone/overtone-cheat-sheet.pdf

## Project Info

### Requirements

* Clojure 1.3
* clojure/core.incubator
* clojure/data.json
* overtone/scsynth-jna (for the internal server)
* SuperCollider (for an external server)
* overtone/at-at
* overtone/osc-clj
* overtone/byte-spec
* overtone/midi-clj
* clj-glob

### Mailing List

Join the Overtone <a href="http://groups.google.com/group/overtone">mailing list</a>.

### Source Repository

Downloads and the source repository can be found on GitHub:

  http://github.com/overtone/overtone

Clone the repository on GitHub to get started developing, and if you are ready
to submit a patch then fork your own copy and do a pull request.

### Cake & Lein Support

Overtone and its dependencies are on http://clojars.org, and the dependency for
your project.clj is:

    [overtone "0.5.0"]

## Acknowledgements

To help us tune the JVM for realtime performance, we use YourKit.

YourKit is kindly supporting open source projects with its full-featured Java Profiler.
YourKit, LLC is the creator of innovative and intelligent tools for profiling
Java and .NET applications. Take a look at YourKit's leading software products:

[YourKit Java Profiler](http://www.yourkit.com/java/profiler/index.jsp) and
[YourKit .NET Profiler](http://www.yourkit.com/.net/profiler/index.jsp)

## Contributors

* Jeff Rose
* Sam Aaron
* Fabian Aussems
* Christophe McKeon
* Pepijn de Vos
* Marius Kempe
* Nicolas Buduroi
* Iain Wood
* Marmaduke Woodman
* Thomas Karolski
* Nick Orton
* Kevin Neaton
* Chris Ford
* Philip Potter

                                                              888
                                                              888
             _ooooooooo._                                     888
          ,o888PP""""PP88   .d88b.  888  888  .d88b.  888d888 888888 .d88b.  88888b.   .d88b.
        d88P''          '  d88""88b 888  888 d8P  Y8b 888P"   888   d88""88b 888 "88b d8P  Y8b
      ,88P                 88    88 Y88  88P 88888888 888     888   88    88 888  888 88888888
     ,88                   Y88..88P  Y8bd8P  Y8b.     888     Y88b. Y88..88P 888  888 Y8b.
    ,88'                    "Y88P"    Y88P    "Y8888  888      "Y888 "Y88P"  888  888  "Y8888
    d8P
    d8b                        88[
    `88                       J88
     Y8b                     ,88'
      Y8b.                  d88'
       `Y8b._            _o88P
         `Y888oo.____ooo888P'
            '"PP888888PP''



# Collaborative Programmable Music.

## A Live-programmable music exploration & collaboration toolkit.

Overtone is an Open Source toolkit for designing synthesizers and collaborating with music.  It provides:

* A Clojure API to the SuperCollider synthesis engine
* A growing library of musical functions (scales, chords, rhythms, arpeggiators, etc.)
* Metronome and timing system to support live-programming and sequencing
* Plug and play MIDI device I/O
* Simple Open Sound Control (OSC) message handling
* Pre-cache - a system for locally caching external assets such as .wav files
* An API for querying and fetching sounds from http://freesound.org

## Quick Setup


### Installation

```sh
    # Install lein
    # https://github.com/technomancy/leiningen

    $ lein new insane-noises

    # add the following dependencies to insane-noises/project.clj
    # [org.clojure/clojure "1.3.0"]
    # [overtone "0.7.0"]

    $ cd insane-noises
    $ lein deps
```

### Server Option A: Internal

```sh
    # Linux users - start jackd
    # (see https://github.com/overtone/overtone/wiki/Installing-and-starting-jack)

    $ lein repl
```
```clj
    user=> (use 'overtone.live)
```

### Server Option B: External

```sh
    # Download and install SuperCollider:
    # http://supercollider.sourceforge.net/downloads/

    $ lein repl
```
```clj
    user=> (use 'overtone.core)
    user=> (boot-external-server)
```


### Your First Sounds

     ___|)_______________|\________________|\______________|\_______________|\________
    |___/___||___________|_________________|_______________|________________|_________||
    |__/|___||.________,-.___( )___o-;___,-.___o-;__( )__,-.________o-; __,-.___o-;__.||
    |_/(|,\_||.___(_)__`-'___|______/____`-'____/___|____`-'___(_)___/____`-'____/___.||
    |_\_|_/_||____|__________|______________________|__________|______________________||
        |         |          |/                     |/         |
      (_|         |/                                           |/

```clj
    ; sin-osc creates a sine wave at the specified Hz (440 in this case)
    ; and pan2 makes the signal stereo
    ; demo simply plays the synth for the specified time in seconds:

    user=> (demo 5 (pan2 (sin-osc 440)))

    ; Defining a new synthesizer instrument with the definst macro will return a
    ; function which can be used to trigger the inst.

    user=> (definst beep [freq 440] (sin-osc freq))
    user=> (beep)
    user=> (stop) ; (this command will kill all running synths)

    ; Call the ctl function to modulate any params and to eventually kill that instrument:

    user=> (beep)
    user=> (ctl beep :freq 880)
    user=> (kill beep) ; (this will just kill the specific instrument)
```

## Documentation

### Detailed Setup Instructions

For a more detailed set of setup instructions (including details specific to Windows and Linux) head over to the [Overtone wiki installation page](https://github.com/overtone/overtone/wiki/Installing-Overtone)

### Getting Started

We will try to maintain documentation for all aspects of the system in the [project wiki](https://github.com/overtone/overtone/wiki/Home), you'll find tutorials and examples on topics such as synthesizing new sounds from scratch, live-coding and generating musical scores on the fly. If you see anything missing, please feel free to add it yourself, or hit us up on the [mailing list](http://groups.google.com/group/overtone) and we'll sort somethign out.

## External & Internal Servers

Overtone supports both internal and external instances of `scsynth` - the SuperCollider server. The internal server is good for quick setup (there are no external dependencies to install and allows fast access to server buffers for transferring sound data and using the scopes). The external server requires a separate installation of SuperCollider itself but is more robust in that crashes in the server (through malformed synth designs etc.) don't also crash the JVM (which is the case for the internal server). It is also possible to connect multiple separate clients to an already running external scsynth instance.

Note - the internal server is not currently supported for all architecture/operating system combinations. However, the external server should work everywhere.

## Getting Started Videos

                           .-.`
                           +hhhhh+`
                          +hhysyhhs
                          hs.   -sh/
                          :`     `yh.
                                  +ho
                                  `sh:
                                   /hs
                                  .hhh:
                                 /yhhhy/
                                `shhhyh/
                                +hhhh:oh`
                               :hhhh+ .ho
                              +yhhhs/  sh+
                             `yhhhy`   .hs
                             ohhhh-     +h:
                            /hhhh+      -hy.   oso
                           +hhhhs        ohy  shhhy
                          .yhhhy`        `yhs  +yhs
                         `shhhh-          -hhhhhhh/
                         ohhhho            /yhhhhs


Head over to Vimeo for a fast-paced 4 minute introduction to live-coding with Overtone to see what's possible

  http://vimeo.com/22798433

For a nice overview of the basics of creating and playing with synthesized
instruments in Overtone checkout Philip Potter's 20 minute workshop:

  http://skillsmatter.com/podcast/scala/clojurex-unpanel-2894

There are also the following tutorials:

* Setting up an Overtone Development Environment - Running on Edge http://vimeo.com/25102399
* How to Hack Overtone with Emacs http://vimeo.com/25190186

## Interviews

Overtone has generated quite a bit of interest. Here's a list of available interviews which go into further depth on the background and philosophy of Overtone:

* http://twit.tv/show/floss-weekly/197
* http://mostlylazy.com/2011/11/18/episode-0-0-2-sam-aaron-and-overtone-at-clojure-conj-2011/
* http://codebassradio.net/2011/11/29/runtime-expectations-episode-13-hot-clojure-conj/ (scroll down to the section with Sam Aaron, Ghadi Shayban, and Daniel Spiewak)
* http://clojure.com/blog/2012/01/04/take5-sam-aaron.html



## Cheat Sheet

For a quick glance at all the exciting functionality Overtone puts at your musical fingertips check out the cheat sheet:

https://github.com/overtone/overtone/raw/master/docs/cheatsheet/overtone-cheat-sheet.pdf

## Project Info

### Requirements

* [Clojure 1.3](http://clojure.org/)
* [clojure/core.incubator](https://github.com/clojure/core.incubator)
* [clojure/data.json](https://github.com/clojure/data.json)
* [overtone/scsynth-jna](http://clojars.org/overtone/scsynth-jna) (for the internal server)
* [SuperCollider](http://supercollider.sourceforge.net/) (for an external server)
* [overtone/at-at](http://github.com/overtone/at-at/)
* [overtone/osc-clj](http://github.com/overtone/osc-clj)
* [overtone/byte-spec](http://github.com/overtone/byte-spec)
* [overtone/midi-clj](http://github.com/overtone/midi-clj)
* [clj-glob](https://github.com/jkk/clj-glob)

### Mailing List

We encourage you to join the [mailing list](http://groups.google.com/group/overtone) to see what other people are getting up to with Overtone. Use it to ask questions, show off what you've made and even meet fellow Overtoners in your area so you can meet up for impromptu jam sessions. All we ask is that you be considerate, courteous and respectful and that you share as much of your code as possible so we can all learn how to make crazy cool sounds together.

### Source Repository

Downloads and the source repository can be found on GitHub:

  http://github.com/overtone/overtone

Clone the repository on GitHub to get started developing, and if you are ready
to submit a patch then fork your own copy and do a pull request.

###  Lein Support

Overtone and its dependencies are on http://clojars.org, and the dependency for
your project.clj is:

    [overtone "0.7.0"]

## Acknowledgements

To help us tune the JVM for realtime performance, we use YourKit.

YourKit is kindly supporting open source projects with its full-featured Java Profiler.
YourKit, LLC is the creator of innovative and intelligent tools for profiling
Java and .NET applications. Take a look at YourKit's leading software products:

[YourKit Java Profiler](http://www.yourkit.com/java/profiler/index.jsp) and
[YourKit .NET Profiler](http://www.yourkit.com/.net/profiler/index.jsp)

## Contributors

See: https://github.com/overtone/overtone/graphs/contributors

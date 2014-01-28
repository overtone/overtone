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

Overtone is an Open Source toolkit for designing synthesizers and
collaborating with music.  It provides:

* A Clojure API to the SuperCollider synthesis engine
* A growing library of musical functions (scales, chords, rhythms,
  arpeggiators, etc.)
* Metronome and timing system to support live-programming and sequencing
* Plug and play MIDI device I/O
* A full Open Sound Control (OSC) client and server implementation.
* Pre-cache - a system for locally caching external assets such as .wav
  files
* An API for querying and fetching sounds from http://freesound.org
* A global concurrent event stream

## Quick Start

### Installation

```sh
    # Install lein2
    # https://github.com/technomancy/leiningen

    $ lein new insane-noises

    # add the following dependencies to insane-noises/project.clj
    # [org.clojure/clojure "1.5.1"]
    # [overtone "0.9.1"]

    $ cd insane-noises
    $ lein repl
```

### Making sounds


```clj
    ;; boot the server
    user=> (use 'overtone.live)

    ;; listen to the joys of a simple sine wave
    user=> (demo (sin-osc))

    ;; or something more interesting...
    user=>(demo 7 (lpf (mix (saw [50 (line 100 1600 5) 101 100.5]))
                  (lin-lin (lf-tri (line 2 20 5)) -1 1 400 4000)))
```

### Detailed Instructions

For a more detailed set of setup instructions (including details
specific to Windows and Linux) head over to the
[Overtone wiki installation page](https://github.com/overtone/overtone/wiki/Installing-Overtone)

We maintain documentation for all aspects of the system in the
[project wiki](https://github.com/overtone/overtone/wiki/Home), you'll
find tutorials and examples on topics such as synthesizing new sounds
from scratch, live-coding and generating musical scores on the fly. If
you see anything missing, please feel free to add it yourself, or hit us
up on the [mailing list](http://groups.google.com/group/overtone) and
we'll sort something out.

## Cheat Sheet

For a quick glance at all the exciting functionality Overtone puts at
your musical fingertips check out the cheat sheet:

https://github.com/overtone/overtone/raw/master/docs/cheatsheet/overtone-cheat-sheet.pdf

## Overtone Powered Bands

A list of bands using Overtone to generate sounds:

* [Meta-eX](http://meta-ex.com)
* [Repl Electric](http://repl-electric.com)

## Community

### Mailing List

We encourage you to join the
[mailing list](http://groups.google.com/group/overtone) to see what
other people are getting up to with Overtone. Use it to ask questions,
show off what you've made and even meet fellow Overtoners in your area
so you can meet up for impromptu jam sessions. All we ask is that you be
considerate, courteous and respectful and that you share as much of your
code as possible so we can all learn how to make crazy cool sounds
together.

### Twitter

Follow `@overtone` on Twitter: http://twitter.com/overtone

### Web

Our main website is hosted on GitHub: http://overtone.github.io

##  Videos

### Introductions

Head over to Vimeo for a fast-paced 4 minute introduction to live-coding
with Overtone to see what's possible

  http://vimeo.com/22798433

For a nice overview of the basics of creating and playing with
synthesized instruments in Overtone checkout Philip Potter's 20 minute
workshop:

  http://skillsmatter.com/podcast/scala/clojurex-unpanel-2894

Chris Ford also delivers a beautifully paced introduction to fundamental music
concepts from basic sine waves to Bach's Goldberg Variations with live examples throughout:

  http://skillsmatter.com/podcast/home/functional-composition

There are also the following tutorials:

* Setting up an Overtone Development Environment - Running on Edge
  http://vimeo.com/25102399
* How to Hack Overtone with Emacs http://vimeo.com/25190186

### Presentations

* Rich Hickey - Harmonikit: http://www.youtube.com/watch?v=bhkdyCPYgLs
* Sam Aaron - Programming Music With Overtone: http://www.youtube.com/watch?v=imoWGsipe4k
* Chris Ford - Functional Composition: http://www.youtube.com/watch?v=Mfsnlbd-4xQ
* Meta-eX - Live Coding with Meta-eX: https://www.youtube.com/watch?v=zJqH5bNcIN0

### Interviews

Overtone has generated quite a bit of interest. Here's a list of
available interviews which go into further depth on the background and
philosophy of Overtone:

* http://twit.tv/show/floss-weekly/197
* http://mostlylazy.com/2011/11/18/episode-0-0-2-sam-aaron-and-overtone-at-clojure-conj-2011/
* http://codebassradio.net/2011/11/29/runtime-expectations-episode-13-hot-clojure-conj/
  (scroll down to the section with Sam Aaron, Ghadi Shayban, and Daniel Spiewak)
* http://clojure.com/blog/2012/01/04/take5-sam-aaron.html

### Performances

* Sam Aaron Live @ Arnolfini:  https://vimeo.com/46867490
* Meta-eX Live @ Music Tech Fest: http://youtu.be/zJqH5bNcIN0?t=15m25s



## Source Repository

Downloads and the source repository can be found on GitHub:

  http://github.com/overtone/overtone

Clone the repository on GitHub to get started developing, and if you are
ready to submit a patch then fork your own copy and do a pull request.

##  Lein Support

Overtone and its dependencies are on http://clojars.org, and the
dependency for your `project.clj` is:

    [overtone "0.9.1"]

## Contributors

See: https://github.com/overtone/overtone/graphs/contributors

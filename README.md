<img src="https://github.com/downloads/overtone/overtone/overtone-logo.png" alt="Overtone Logo" title="Overtone" />


## Programmable Music


### Live-coding and musical exploration

Overtone is a toolkit for creating synthesizers and making music.  It provides:

* a Clojure API to the SuperCollider synthesis engine
* a growing library of musical functions (scales, chords, rhythms, arpeggiators, etc.)
* metronome and timing system to support live-coding and sequencing
* plug and play midi device I/O
* simple Open Sound Control (OSC) message handling

### Live-Coding Video Introduction

<a href="http://vimeo.com/22798433">
  <img src="https://github.com/downloads/overtone/live-coding-emacs/live-coding-config-in-use-2.png" alt="Live-Coding with Overtone" title="Live-Coding Video Introduction" />
</a>

Head over to vimeo for a fast-paced 4 minute introduction to live-coding with Overtone to see what's
possible

  http://vimeo.com/22798433

### Cheat Sheet

For a quick glance at all the functionality Overtone puts at your musical fingertips check out the cheat sheet:

  https://github.com/downloads/overtone/overtone/overtone-cheat-sheet-a4.pdf

### Project Info:

#### Lein, Cake and Maven support

Overtone and its dependencies are on http://clojars.org, and the dependency for
your project.clj is:

    [overtone "<version>"]

The current version is 0.4.0 but search on Clojars to get the latest
release.

#### Source Repository

Downloads and the source repository can be found on GitHub:

  http://github.com/overtone/overtone

The project is free and open source.  Clone the repository on GitHub to get
started developing, and if you are ready to submit a patch then fork your own
copy and do a pull request.

#### Mailing List

Join the Overtone <a href="http://groups.google.com/group/overtone">mailing list</a>.

### Ubuntu Quick Setup:

    sudo apt-get install jack-tools ant sun-java6-jdk fftw3 qjackctl

You'll need to get the jack audio daemon running, and we recommend qjackctl to
figure out what command will be best to use.  Then once you have it dialed in you can
switch to using the terminal.  For best performance you need to install a
realtime enabled kernel, which allows the audio system to get high scheduled
immediately when there is data to process.  With purely generative music this
isn't such a big deal, but if you want to jam with other instruments or process
external sound in realtime then you'll want to invest the effort in setting up
an rt-kernel.  Ubuntu studio makes it pretty easy, especially if you aren't
experienced in compiling the kernel.  In the meantime, just turn-off the
realtime support in the qjacktl options, and the audio server should boot.

Future versions will also support ALSA audio.

Download and install leiningen wherever you local executables go:

    wget http://github.com/technomancy/leiningen/raw/stable/bin/lein
    chmod u+x lein
    mv lein ~/bin
    lein self-install

Now get Overtone:

    $ git clone git://github.com/overtone/overtone.git

    $ cd overtone
    $ lein deps

    ; In Linux you can create a .jackdrc file with this command
    ; to automatically start the jack server on boot, or you will need
    ; to run it manually to start the Jack audio server.
    $ jackd -r -d alsa -r 44100 ; or use qjackctl for a gui

    $ lein repl

    user=> (use 'overtone.live)

    ; sin-osc creates a sine wave at the specified Hz (440 in this case)
    ; and pan2 makes the signal stereo
    ; demo simply plays the synth for the specified time in seconds:

    user=> (demo 5 (pan2 (sin-osc 440))))


    ; Defining a new synthesizer instrument with the definst macro will return a function which
    ; can be used to trigger the inst.

    user=> (definst beep [freq 440] (sin-osc freq))
    user=> (beep)
    user=> (stop)

    ; Call the ctl function to modulate any params and to eventually kill that instrumetn:

    user=> (beep)
    user=> (ctl beep :freq 880)
    user=> (kill beep)
    user=> (quit)


### Getting Started Videos

* Setting up an Overtone Development Environment - Running on Edge http://vimeo.com/25102399
* How to Hack Overtone with Emacs http://vimeo.com/25190186

### Acknowledgements

To help us tune the JVM for realtime performance, we use YourKit.

YourKit is kindly supporting open source projects with its full-featured Java Profiler.
YourKit, LLC is the creator of innovative and intelligent tools for profiling
Java and .NET applications. Take a look at YourKit's leading software products:

[YourKit Java Profiler](http://www.yourkit.com/java/profiler/index.jsp) and
[YourKit .NET Profiler](http://www.yourkit.com/.net/profiler/index.jsp)

### Contributors

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

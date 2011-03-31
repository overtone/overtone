          ____                  __
         / __( )_  _____  _____/ /_____  ____  ___
        / / / / | / / _ \/ ___/ __/ __ \/ __ \/ _ \
       / /_/ /| |/ /  __/ /  / /_/ /_/ / / / /  __/
       \____/ |___/\___/_/   \__/\____/_/ /_/\___/

---------------------------------------------------------

#### Live-coding and musical exploration

Overtone is a toolkit for creating synthesizers and making music.  It provides:

* a Clojure API to the SuperCollider synthesis engine
* a growing library of musical functions (scales, chords, rhythms, arpeggiators, etc.)
* metronome and timing system to support live-coding and sequencing
* plug and play midi device I/O
* simple Open Sound Control (OSC) message handling
*

### Project Info:

#### Lein, Cake and Maven support

Overtone and its dependencies are on http://clojars.org, and the dependency for
your project.clj is:

[overtone "<version>"]

The current version is 0.1.3-SNAPSHOT, but search on Clojars to get the latest
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

    $ git clone git://github.com/rosejn/overtone.git

    $ cd overtone
    $ lein deps

    ; In Linux you can create a .jackdrc file with this command
    ; to automatically start the jack server on boot, or you will need
    ; to run it manually to start the Jack audio server.
    $ jackd -r -d alsa -r 44100 ; or use qjackctl for a gui

    $ lein repl

    user=> (use 'overtone.live)
    user=> (synth (out 0 (pan2 (sin-osc 440))))

    ; Defining a new synthesizer with the synth macro will return a function.

    user=> (*1)
    5

    ; Call the function to trigger the synth and set its control parameters.
    ; It will return an ID that can be used to kill or adjust parameters for
    ; the synth instance.

    user=> (kill 5)

    user=> (quit)

### Contributors

* Jeff Rose
* Jon Rose
* Sam Aaron
* Fabian Aussems
* Christophe McKeon
* Pepijn de Vos
* Marius Kempe

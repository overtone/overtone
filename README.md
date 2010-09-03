          ____                  __
         / __( )_  _____  _____/ /_____  ____  ___
        / / / / | / / _ \/ ___/ __/ __ \/ __ \/ _ \
       / /_/ /| |/ /  __/ /  / /_/ /_/ / / / /  __/
       \____/ |___/\___/_/   \__/\____/_/ /_/\___/

---------------------------------------------------------

#### Live-coding and musical exploration

Overtone is a Clojure based musical generation and manipulation system for live-coding and more.

### Project Info:

#### Source Repository
Downloads and the source repository can be found on GitHub:

  http://github.com/rosejn/overtone

The project is 100% open source and free, and contributions of code,
documentation, feedback, thoughts and ideas are welcome.  Clone the repository on GitHub to get
started, and if you are ready to submit a patch fork your own copy and then do a pull request.  
Make sure to jump onto the mailing list before getting started so we don't duplicate our efforts.

#### Mailing List

If you are looking for some help or you are interested in joining in the
project, start by joining the  
<a href="http://groups.google.com/group/overtone">mailing list</a>.

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

Download jdk zip file and put in correct location...

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
    user=> (refer-ugens)    ;; Add the ugen functions to the namespace

    user=> (boot :external) ;; for external supercollider
    user=> (boot :internal) ;; or for internal supercollider and scope support

    user=> (server-log) ; check for errors

    user=> (synth (sin-osc 440)) ; define an anonymous synth
    user=> (*1) ; play it...  returns a node-id
    user=> (kill <node-id>) ; put the number returned from above

    user=> (quit)

### General Setup:

Install:

* Super Collider (http://supercollider.sourceforge.net/)
  - Make sure it's available on your path.

* Java 6 JDK

* Leiningen

* Linux users will need a working Jackd setup, as well as the jack\_lsp, and
jack\_connect utilities (jack-tools package in Ubuntu).

At this point you have enough to write musical scripts and make noise, but you
won't be able to do any livecoding unless you have an interactive Clojure
environment setup.  I use the vimclojure plugin for vim, but it should be
possible to use emacs and slime, or netbeans and enclojure, or eclipse, or
whatever else as long as you can evaluate clojure expressions inside the
editor.

Cross platform installation of software, even pure Java, is a pain.  If you get
Overtone running on your favorite platform and it requires steps not outlined
here, please drop us an email describing what you had to do so we can
include it in this README.

### Project Map

* TODO => The list of stuff that's going to get done
* docs
  * roadmap => future directions
  * metadata => details about how UGen functions are generated
  * kick.clj => documented synthesizer definition in Overtone format
  * ugens => some basic ugen documentation (eventually to become meta-data)
  * events => info about the event system and events published by Overtone
  * data-model => modeling the app for use with a graph database
* lib => where lein deps puts jar file dependencies
* native => where lein native-deps puts the native library code
* test => the meager tests that will eventually grow
* script => random helper scripts, most of which are becoming obsolete
* src
  * devices => to become device "drivers" (e.g. for specific MIDI controllers)
  * examples => how to use Overtone (contributions welcome, basic to advanced)
  * lib => various libraries used by the project if you have the submodules
  checked out
  * overtone
    * app => the stand-alone Overtone application code
    * core => the guts of the audio system and core functionality
    * gui => general purpose GUI components (mostly implemented as SGNode
    objects using the scenario 2D scenegraph library created for JavaFX)
    * music => musically oriented code related to notes, chords, rhythm, etc.
    * studio => higher level audio components built on the core

### Development

To setup with all the dependent libraries developed as a part of Overtone you
should grab the submodules too:

    git submodule init 
    git submodule update

Note, the deps from clojars will be used when you run the code, so you don't
need these unless you want to develop on one of the sub-libraries.  (osc,
supercollider interfacing, event, etc...)

### Contributors

* Jeff Rose
* Jon Rose
* Sam Aaron
* Fabian Aussems
* Christophe McKeon 

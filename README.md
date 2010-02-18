  Overtone
==============

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

    sudo apt-get install supercollider-server jack-tools ant sun-java6-jdk

Download jdk zip file and put in correct location...

Download and install leiningen wherever you local executables go:

    wget http://github.com/technomancy/leiningen/raw/stable/bin/lein 
    chmod u+x lein
    mv lein ~/bin  
    lein self-install

Now get Overtone:

    $ git clone git://github.com/mozinator/overtone.git

    $ cd overtone

    $ jackd -r -d alsa -r 44100
    $ echo play | jack_transport

    $ lein deps      
    $ lein repl

    user=> (use 'overtone.live)
    user=> (refer-ugens)
    user=> (boot)

    user=> (print-server-log) ; check for errors

    $ jack_connect SuperCollider:out_1 system:playback_1
    $ jack_connect SuperCollider:out_2 system:playback_2

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

### Contributors

* Jeff Rose
* Jon Rose
* Sam Aaron
* Fabian Aussems
* Christophe McKeon 

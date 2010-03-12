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

Now get Overtone and its submodules:

    git clone git://github.com/rosejn/overtone.git

    cd overtone

    git submodule init

    git submodule update

    lein deps
    lein native-deps

    ; In Linux you will need to create a .jackdrc file that holds the command 
    ; that will be used to automatically start the jack server.  On my 
    ; laptop it looks like this:
    ;
    ;  /usr/bin/jackd -R -dalsa -dhw:0 -r44100 -p1024 -n3

    lein repl

    (use 'overtone.live)
    (refer-ugens)
    (boot) ;; for external supercollider
    (booti) ;; for internal supercollider

    (synth (sin-osc 440)) ; define an anonymous synth
    (*1) ; play it...  returns a node-id
    (kill <node-id>) ; put the number returned from above

    ;; Define a simple synth with a saw wave that has
    ;; a vibrato effect and a percussive style envelope.
    (defsynth foo [freq 220, lfo 8, depth 20,
                   rise 0.05, fall 0.6]
      (* (env-gen (perc rise fall) 1 1 0 1 :free) 
         (saw (+ freq (* 10 (sin-osc:kr lfo))))))
    
    ; Call with none or some arguments and it uses the defaults
    (foo)
    (foo 220)
    (foo 220 10)
    
    ; Or call with keyword style args
    (foo :rise 0.1 :fall 2.0) 

    (quit)

### General Setup:

Install:

* Super Collider (http://supercollider.sourceforge.net/)
  - Make sure it's available on your path.

* Java 6 JDK

* Leiningen

* Linux users will need a working Jackd setup, as well as the jack\_lsp, and
jack\_connect utilities (jack-tools package in Ubuntu).

Beyond those requirements, you can run "ant deps" to retrieve the necessary jar
files, which are currently about 5 megs.  The footprint will go down
substantially soon though.

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

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

The project mailing list is hosted at http://librelist.com/, and you can subscribe by simply
sending a message to:

  overtone@librelist.com

Note, the list will receive the first message you send, and you will automatically be subscribed.


### Ubuntu Quick Setup:

    sudo apt-get install supercollider-server jack-tools ant sun-java6-jdk

    ;; Download jdk zip file and put in correct location...

    git clone git://github.com/rosejn/overtone.git

    cd overtone

    ant deps

    qjackctl&

    ;; Start by just using Jack with Alsa and the default settings, but if you get
    ;; serious you'll want to read up on tuning jack to minimize audio latency
    ;; and get realtime scheduling of audio threads.

    ; Turn down the speakers to a medium/low volume

    ant test

    ;; All tests should pass (however minimal they might be), and you should
    ;; hear some tones played on your speakers.

### General Setup:

Install:

* Super Collider (http://supercollider.sourceforge.net/)
  - Make sure it's available on your path.

* Java 6 JDK

* Apache Ant build tool 
  - If someone contributes a Maven file that would be sweet...

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

### Getting Started:

    <%= gist 234818 %>

For now you can look in the "tests" for examples on how to make noise and do things.
Submissions of cool musical examples, tutorials, and general fixes and features
are very much welcome and encouraged.

I use the tune-up script to quickly setup the audio environment and the nailgun
server used by vimclojure.  Over time this will become a clojure script that is
more easily customizable, but for now the Ruby script should be easy enough to
understand and modify.

### Contributors

* Jeff Rose
* Sam Aaron

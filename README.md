  Overtone
==============

#### Live-coding and musical exploration

Overtone is a Clojure based musical generation and manipulation system for live-coding and more.

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

* Linux users will need a working Jackd setup, as well as the jack_lsp, and
jack_connect utilities (jack-tools package in Ubuntu).

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

<script src="http://gist.github.com/234818.js"></script>

For now you can look in the "tests" for examples on how to make noise and do things.
Submissions of cool musical examples, tutorials, and general fixes and features
are very much welcome and encouraged.  

I use the tune-up script to quickly setup the audio environment and the nailgun
server used by vimclojure.  Over time this will become a clojure script that is
more easily customizable, but for now the Ruby script should be easy enough to
understand and modify.

### Related Projects:

* [Impromptu](http://impromptu.moso.com.au/)

* [Fluxus](http://impromptu.moso.com.au/)

* [Chuck](http://chuck.cs.princeton.edu/)

* [csound](http://www.csounds.com/)

* [PureData](http://puredata.info/)

* [JCollider](http://www.sciss.de/jcollider/)

* [SCRuby](http://github.com/maca/scruby) - Ruby SuperCollider

* [RSC3](http://www.slavepianos.org/rd/sw/rsc3/README) - Scheme SuperCollider

* [Common Music](http://commonmusic.sourceforge.net/cm/res/doc/cm.html#toolbox)

* [JFugue](http://www.jfugue.org/javadoc/index.html)

* [JMusic](http://jmusic.ci.qut.edu.au/)


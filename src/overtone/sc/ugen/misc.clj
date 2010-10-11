(ns overtone.sc.ugen.misc
  (:use (overtone.sc.ugen common)))

(def specs
     [

      ;; from PitchShift.sc
      ;; PitchShift : UGen {
      ;;    checkInputs { ^this.checkSameRateAsFirstInput }
      ;;  *ar { arg in = 0.0, windowSize = 0.2, pitchRatio = 1.0,
      ;;      pitchDispersion = 0.0, timeDispersion = 0.0, mul = 1.0, add = 0.0;
      ;;    ^this.multiNew('audio', in, windowSize, pitchRatio,
      ;;      pitchDispersion, timeDispersion).madd(mul, add)
      ;;  }
      ;; }

      {:name "PitchShift",
       :args [{:name "in"}
              {:name "windowSize", :default 0.2}
              {:name "pitchRatio", :default 1.0}
              {:name "pitchDispersion", :default 0.0}
              {:name "timeDispersion", :default 0.0}],
       :rates #{:ar}
       :muladd true
       :check (same-rate-as-first-input)}

      ;; from Pluck.sc
      ;; Pluck : UGen {
      ;;  *ar { arg in = 0.0, trig = 1.0, maxdelaytime = 0.2, delaytime = 0.2, decaytime = 1.0,
      ;;      coef = 0.5, mul = 1.0, add = 0.0;
      ;;    ^this.multiNew('audio', in, trig, maxdelaytime, delaytime, decaytime, coef).madd(mul, add)}
      ;;  }

      {:name "Pluck",
       :args [{:name "in", :default 0.0}
              {:name "trig", :default 1.0}
              {:name "maxdelaytime", :default 0.2}
              {:name "delaytime", :default 0.2}
              {:name "decaytime", :default 1.0}
              {:name "coef", :default 0.5}],
       :rates #{:ar}
       :muladd true}

      ;; TODO write some functions implementing these classand buffer  methods
      ;; Partitioned Convolution, from PartConv.sc
      ;; PartConv : UGen
      ;; {
      ;;  *ar { arg in, fftsize, irbufnum,mul = 1.0, add = 0.0;
      ;;    ^this.multiNew('audio', in, fftsize, irbufnum).madd(mul, add);
      ;;  }
      ;;
      ;;  *calcNumPartitions {arg fftsize, irbuffer;
      ;;    var siz, partitionsize;
      ;;
      ;;    partitionsize=fftsize.div(2);
      ;;
      ;;    siz= irbuffer.numFrames;
      ;;    ^((siz/partitionsize).roundUp);
      ;;    //bufsize = numpartitions*fftsize;
      ;;  }
      ;;
      ;;  *calcBufSize {arg fftsize, irbuffer;
      ;;    ^ fftsize* (PartConv.calcNumPartitions(fftsize,irbuffer));
      ;;  }
      ;; }
      ;;
      ;; + Buffer {
      ;;  preparePartConv { arg buf, fftsize;
      ;;    server.listSendMsg(["/b_gen", bufnum, "PreparePartConv", buf.bufnum, fftsize]);
      ;;  }
      ;; }

      {:name "PartConv",
       :args [{:name "in"}
              {:name "fftsize"}
              {:name "irbufnum"}],
       :rates #{:ar}
       :muladd true}

      ;; from Hilbert.sc
      ;; Hilbert : MultiOutUGen {
      ;;  *ar { arg in, mul = 1, add = 0;
      ;;    ^this.multiNew('audio', in).madd(mul, add);
      ;;  }

      ;;  init { arg ... theInputs;
      ;;    inputs = theInputs;
      ;;    ^this.initOutputs(2, rate);
      ;;  }
      ;; }

      {:name "Hilbert",
       :args [{:name "in"}],
       :rates #{:ar},
       :muladd true
       :num-outs 2}

      ;; // single sideband amplitude modulation, using optimized Hilbert phase differencing network
      ;; // basically coded by Joe Anderson, except Sean Costello changed the word HilbertIIR.ar
      ;; // to Hilbert.ar

      ;; FreqShift : UGen {
      ;;  *ar {
      ;;    arg in,     // input signal
      ;;    freq = 0.0,   // shift, in cps
      ;;    phase = 0.0,  // phase of SSB
      ;;    mul = 1.0,
      ;;    add = 0.0;
      ;;    ^this.multiNew('audio', in, freq, phase).madd(mul, add)
      ;;  }
      ;; }

      {:name "FreqShift",
       :args [{:name "in"}
              {:name "freq", :default 0.0}
              {:name "phase", :default 0.0}],
       :rates #{:ar}
       :muladd true}


      ;; from GVerb.sc
      ;;       GVerb : MultiOutUGen {
      ;;  *ar { arg in, roomsize = 10, revtime = 3, damping = 0.5, inputbw =  0.5, spread = 15,
      ;;      drylevel = 1, earlyreflevel = 0.7, taillevel = 0.5, maxroomsize = 300, mul = 1,
      ;;      add = 0;
      ;;    ^this.multiNew('audio', in, roomsize, revtime, damping, inputbw, spread, drylevel,
      ;;      earlyreflevel, taillevel, maxroomsize).madd(mul, add);
      ;;  }

      ;;  init {arg ... theInputs;
      ;;    inputs = theInputs;
      ;;    ^this.initOutputs(2, rate);
      ;;  }
      ;; }

      {:name "GVerb",
       :args [{:name "in"}
              {:name "roomsize", :default 10.0}
              {:name "revtime", :default 3.0}
              {:name "damping", :default 0.5}
              {:name "inputbw", :default 0.5}
              {:name "spread", :default 15.0}
              {:name "drylevel", :default 1.0}
              {:name "earlyreflevel", :default 0.7}
              {:name "taillevel", :default 0.5}
              {:name "maxroomsize", :default 300.0}],
       :rates #{:ar},
       :num-outs 2}

      ;; from FreeVerb.sc
      ;; // blackrain's freeverb ugen.

      ;; FreeVerb : UGen {
      ;; 	*ar { arg in, mix = 0.33, room = 0.5, damp = 0.5, mul = 1.0, add = 0.0;
      ;; 		^this.multiNew('audio', in, mix, room, damp).madd(mul, add)
      ;; 	}
      ;; }

      {:name "FreeVerb",
       :args [{:name "in"}
              {:name "mix", :default 0.33}
              {:name "room", :default 0.5}
              {:name "damp", :default 0.5}],
       :rates #{:ar}
       :muladd true}

      ;; FreeVerb2 : MultiOutUGen {
      ;; 	*ar { arg in, in2, mix = 0.33, room = 0.5, damp = 0.5, mul = 1.0, add = 0.0;
      ;; 		^this.multiNew('audio', in, in2, mix, room, damp).madd(mul, add)
      ;; 	}
      ;; 	init { arg ... theInputs;
      ;; 		inputs = theInputs;
      ;; 		channels = [
      ;; 			OutputProxy(rate, this, 0),
      ;; 			OutputProxy(rate, this, 1)
      ;; 		];
      ;; 		^channels
      ;; 	}
      ;; }

      {:name "FreeVerb2",
       :args [{:name "in"}
              {:name "in2"}
              {:name "mix", :default 0.33}
              {:name "room", :default 0.5}
              {:name "damp", :default 0.5}],
       :rates #{:ar},
       :num-outs 2
       :muladd true}

      ;; from MoogFF.sc
      ;; /**
      ;; "MoogFF" - Moog VCF digital implementation.
      ;; As described in the paper entitled
      ;; "Preserving the Digital Structure of the Moog VCF"
      ;; by Federico Fontana
      ;; appeared in the Proc. ICMC07, Copenhagen, 25-31 August 2007

      ;; Original Java code Copyright F. Fontana - August 2007
      ;; federico.fontana@univr.it

      ;; Ported to C++ for SuperCollider by Dan Stowell - August 2007
      ;; http://www.mcld.co.uk/
      ;; */

      ;; MoogFF : Filter {

      ;;  *ar { | in, freq=100, gain=2, reset=0, mul=1, add=0 |
      ;;    ^this.multiNew('audio', in, freq, gain, reset).madd(mul, add)
      ;;  }
      ;;  *kr { | in, freq=100, gain=2, reset=0, mul=1, add=0 |
      ;;    ^this.multiNew('control', in, freq, gain, reset).madd(mul, add)
      ;;  }
      ;; }

      {:name "MoogFF",
       :args [{:name "freq", :default 100.0}
              {:name "gain", :default 2.0}
              {:name "reset", :default 0.0}]
       :muladd true}

      ;; from PhysicalModel.sc
      ;; Spring : UGen {
      ;;  *ar { arg in=0.0, spring=1, damp=0;
      ;;    ^this.multiNew('audio', in, spring, damp)
      ;;  }
      ;; }

      {:name "Spring",
       :args [{:name "in", :default 0.0}
              {:name "spring", :default 0.0}
              {:name "damp", :default 0.0}]}

      ;; from PhysicalModel.sc
      ;; Ball : UGen {
      ;;  *ar { arg in=0.0, g=1, damp=0, friction=0.01;
      ;;    ^this.multiNew('audio', in, g, damp, friction)
      ;;  }
      ;; }

      {:name "Ball",
       :args [{:name "in", :default 0.0}
              {:name "g", :default 1.0}
              {:name "damp", :default 0.0}
              {:name "friction", :default 0.01}]}

      ;; from PhysicalModel.sc
      ;; TBall : UGen {
      ;;  *ar { arg in=0.0, g=10, damp=0, friction=0.01;
      ;;    ^this.multiNew('audio', in, g, damp, friction)
      ;;  }
      ;; }

      {:name "TBall",
       :args [{:name "in", :default 0.0}
              {:name "g", :default 10.0}
              {:name "damp", :default 0.0}
              {:name "friction", :default 0.01}]}

      ;; from CheckBadValues.sc
      ;;  CheckBadValues : UGen {
      ;;  *ar {arg in = 0.0, id = 0, post = 2;
      ;;    ^this.multiNew('audio', in, id, post);
      ;;  }

      ;;  *kr {arg in = 0.0, id = 0, post = 2;
      ;;    ^this.multiNew('control', in, id, post);
      ;;  }

      ;;  checkInputs {
      ;;      if ((rate==\audio) and:{ inputs.at(0).rate != \audio}) {
      ;;        ^("audio-rate, yet first input is not audio-rate");
      ;;      };
      ;;      ^this.checkValidInputs
      ;;    }
      ;; }

      {:name "CheckBadValues",
       :args [{:name "in", :default 0.0}
              {:name "id", :default 0.0}
              {:name "post", :default 2.0}]
       :check (when-ar (same-rate-as-first-input))}

      ;; from Gendyn.sc
      ;;       //GENDYN by Iannis Xenakis implemented for SC3 by
      ;; sicklincoln with some refinements
      ;; Gendy1 : UGen {
      ;;   *ar { arg ampdist=1, durdist=1, adparam=1.0, ddparam=1.0, minfreq=440, maxfreq=660, ampscale= 0.5, durscale=0.5, initCPs= 12, knum, mul=1.0,add=0.0;
      ;;    ^this.multiNew('audio', ampdist, durdist, adparam,
      ;;   ddparam, minfreq, maxfreq, ampscale, durscale,
      ;;    initCPs, knum ? initCPs).madd( mul, add )
      ;; }
      ;; *kr {arg ampdist=1, durdist=1, adparam=1.0, ddparam=1.0, minfreq=20, maxfreq=1000, ampscale= 0.5, durscale=0.5, initCPs= 12, knum,mul=1.0,add=0.0;
      ;;   ^this.multiNew('control', ampdist, durdist, adparam, ddparam, minfreq, maxfreq, ampscale, durscale, initCPs, knum ? initCPs).madd( mul, add )
      ;;  }
      ;;}

      {:name "Gendy1",
       :args [{:name "ampdist", :default 1.0}
              {:name "durdist", :default 1.0}
              {:name "adparam", :default 1.0}
              {:name "ddparam", :default 1.0}
              {:name "minfreq", :default 440.0}
              {:name "maxfreq", :default 660.0}
              {:name "ampscale", :default 0.5}
              {:name "durscale", :default 0.5}
              {:name "initCPs", :default 12}
              {:name "knum" :default 12}]
       :muladd true}

      ;; Gendy2 : UGen {
      ;;      *ar { arg ampdist=1, durdist=1, adparam=1.0, ddparam=1.0, minfreq=440, maxfreq=660, ampscale= 0.5, durscale=0.5, initCPs= 12, knum, a=1.17, c=0.31, mul=1.0,add=0.0;
      ;;               ^this.multiNew('audio', ampdist, durdist, adparam, ddparam, minfreq, maxfreq, ampscale, durscale, initCPs, knum ? initCPs, a, c).madd( mul, add )
      ;;           }
      ;;      *kr {arg ampdist=1, durdist=1, adparam=1.0, ddparam=1.0, minfreq=20, maxfreq=1000, ampscale= 0.5, durscale=0.5, initCPs= 12, knum, a=1.17, c=0.31, mul=1.0,add=0.0;
      ;;              ^this.multiNew('control', ampdist, durdist, adparam, ddparam, minfreq, maxfreq, ampscale, durscale, initCPs, knum ? initCPs, a, c).madd( mul, add )
      ;;           }
      ;;         }

      {:name "Gendy2",
       :args [{:name "ampdist", :default 1.0}
              {:name "durdist", :default 1.0}
              {:name "adparam", :default 1.0}
              {:name "ddparam", :default 1.0}
              {:name "minfreq", :default 440.0}
              {:name "maxfreq", :default 660.0}
              {:name "ampscale", :default 0.5}
              {:name "durscale", :default 0.5}
              {:name "initCPs", :default 12}
              {:name "knum" :default 12}
              {:name "a", :default 1.17}
              {:name "c", :default 0.31}]
       :muladd true}

      ;; Gendy3 : UGen {

      ;;      *ar { arg ampdist=1, durdist=1, adparam=1.0, ddparam=1.0, freq=440, ampscale= 0.5, durscale=0.5, initCPs= 12, knum, mul=1.0,add=0.0;
      ;;               ^this.multiNew('audio', ampdist, durdist, adparam, ddparam, freq, ampscale, durscale, initCPs, knum ? initCPs).madd( mul, add )
      ;;           }

      ;;      *kr {arg ampdist=1, durdist=1, adparam=1.0, ddparam=1.0, freq=440, ampscale= 0.5, durscale=0.5, initCPs= 12, knum, mul=1.0,add=0.0;
      ;;              ^this.multiNew('control', ampdist, durdist, adparam, ddparam, freq, ampscale, durscale, initCPs, knum ? initCPs).madd( mul, add )
      ;;           }

      ;;         }

      {:name "Gendy3",
       :args [{:name "ampdist", :default 1.0}
              {:name "durdist", :default 1.0}
              {:name "adparam", :default 1.0}
              {:name "ddparam", :default 1.0}
              {:name "freq", :default 440.0}
              {:name "ampscale", :default 0.5}
              {:name "durscale", :default 0.5}
              {:name "initCPs", :default 12}
              {:name "knum" :default 12}]
       :muladd true}])

;; TODO investigate Poll
;; from Poll.sc
;; Poll : UGen {
;;  *ar { arg trig, in, label, trigid = -1;
;;    this.multiNewList(['audio', trig, in, label, trigid]);
;;    ^in;
;;  }
;;  *kr { arg trig, in, label, trigid = -1;
;;    this.multiNewList(['control', trig, in, label, trigid]);
;;    ^in;
;;  }
;;  *new { arg trig, in, label, trigid = -1;
;;    var rate = in.asArray.collect(_.rate).unbubble;
;;    this.multiNewList([rate, trig, in, label, trigid]);
;;    ^in;
;;  }
;;  *new1 { arg rate, trig, in, label, trigid;
;;    label = label ?? {  "UGen(%)".format(in.class) };
;;    label = label.asString.collectAs(_.ascii, Array);
;;    if(rate === \scalar) { rate = \control };
;;    if(trig.isNumber) { trig = Impulse.multiNew(rate, trig, 0) };
;;    ^super.new.rate_(rate).addToSynth.init([trig, in, trigid, label.size] ++ label);
;;  }

;;    checkInputs { ^this.checkSameRateAsFirstInput }

;;    init { arg theInputs;
;;      // store the inputs as an array
;;      inputs = theInputs;
;;    }
;; }

;; /*
;; s.boot;

;; {Poll.ar(Impulse.ar(5), Line.ar(0, 1, 1), \test2)}.play(s);
;; {SinOsc.ar(220, 0, 1).poll(Impulse.ar(15), "test")}.play(s);

;; o = OSCresponderNode(s.addr, '/tr', {arg time, resp, msg;
;;  msg.postln;
;;  }).add

;; {Poll.ar(Impulse.ar(5), Line.ar(0, 1, 1), \test2, 1234)}.play(s);

;; o.remove;
;; s.quit;
;; */

;; /*
;; s.boot;

;; {Poll.ar(Impulse.ar(5), Line.ar(0, 1, 1), \test2)}.play(s);
;; {SinOsc.ar(220, 0, 1).poll(Impulse.ar(15), "test")}.play(s);

;; o = OSCresponderNode(s.addr, '/tr', {arg time, resp, msg;
;;  msg.postln;
;;  }).add

;; {Poll.ar(Impulse.ar(5), Line.ar(0, 1, 1), \test2, 1234)}.play(s);

;; o.remove;
;; s.quit;
;; */

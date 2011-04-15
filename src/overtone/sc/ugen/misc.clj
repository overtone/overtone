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
       :args [{:name "in", :doc "The input signal."}
              {:name "windowSize", :default 0.2, :doc "The size of the grain window in seconds. This value cannot be modulated."}
              {:name "pitchRatio", :default 1.0, :doc "The ratio of the pitch shift. Must be from 0.0 to 4.0"}
              {:name "pitchDispersion", :default 0.0, :doc "The maximum random deviation of the pitch from the pitchRatio."}
              {:name "timeDispersion", :default 0.0, :doc "A random offset of from zero to timeDispersion seconds is added to the delay
of each grain. Use of some dispersion can alleviate a hard comb filter effect due to uniform grain placement. It can also be an effect in itself. timeDispersion can be no larger than windowSize."}],
       :rates #{:ar}
       :check (same-rate-as-first-input)
       :doc "A time domain granular pitch shifter. Grains have a triangular amplitude envelope and an overlap of 4:1."}

      {:name "Pluck",
       :args [{:name "in", :default 0.0, :doc "An excitation signal."}
              {:name "trig", :default 1.0, :doc "Upon a negative to positive transition, the excitation signal will be fed into the delay line."}
              {:name "maxdelaytime", :default 0.2, :doc "The max delay time in seconds (initializes the internal delay buffer)."}
              {:name "delaytime", :default 0.2, :doc "Delay time in seconds."}
              {:name "decaytime", :default 1.0, :doc "Time for the echoes to decay by 60 decibels. Negative times emphasize odd partials."}
              {:name "coef", :default 0.5, :doc "The coef of the internal OnePole filter. Values should be between -1 and +1 (larger values will be unstable... so be careful!)."}],
       :rates #{:ar}
       :doc "Implements the Karplus-Strong style of synthesis, where a delay line (normally starting with noise) is filtered and fed back on itself so that over time it becomes periodic."}

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
       :args [{:name "in", :doc "Processing target."}
              {:name "fftsize", :doc "Spectral convolution partition size (twice partition size). You must ensure that the blocksize divides the partition size and there are at least two blocks per partition (to allow for amortisation)"}
              {:name "irbufnum", :doc "Prepared buffer of spectra for each partition of the inpulse response"}],
       :rates #{:ar}
       :doc "Partitioned convolution. Various additional buffers must be supplied. Mono impulse response only! If inputting multiple channels, you'll need independent PartConvs, one for each channel. But the charm is: impulse response can be as large as you like (CPU load increases with IR size. Various tradeoffs based on fftsize choice, due to rarer but larger FFTs. This plug-in uses amortisation to spread processing and avoid spikes). Normalisation factors difficult to anticipate; convolution piles up multiple copies of the input on top of itself, so can easily overload. "}

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
      ;;TODO: SC docs call the second param shift - check which is correct
      {:name "FreqShift",
       :args [{:name "in", :doc "The signal to process"}
              {:name "freq", :default 0.0, :doc "Amount of shift in cycles per second"}
              {:name "phase", :default 0.0, :doc "Phase of the frequency shift (0 - 2pi)"}],
       :rates #{:ar},
       :doc "FreqShift implements single sideband amplitude modulation, also known as frequency shifting, but not to be confused with pitch shifting.  Frequency shifting moves all the components of a signal by a fixed amount but does not preserve the original harmonic relationships."}


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
       :args [{:name "in", :doc "mono input"}
              {:name "roomsize", :default 10.0, :doc "in squared meters."}
              {:name "revtime", :default 3.0, :doc "in seconds"}
              {:name "damping", :default 0.5, :doc "0 to 1, high frequency rolloff, 0 damps the reverb signal completely, 1 not at all"}
              {:name "inputbw", :default 0.5, :doc "0 to 1, same as damping control, but on the input signal"}
              {:name "spread", :default 15.0, :doc "a control on the stereo spread and diffusion of the reverb signal"}
              {:name "drylevel", :default 1.0, :doc "amount of dry signal"}
              {:name "earlyreflevel", :default 0.7, :doc "amount of early reflection level"}
              {:name "taillevel", :default 0.5, :doc "amount of tail level"}
              {:name "maxroomsize", :default 300.0, :doc "to set the size of the delay lines."}],
       :rates #{:ar},
       :num-outs 2,
       :doc "A two-channel reverb UGen, based on the \"GVerb\" LADSPA effect by Juhana Sadeharju (kouhia at nic.funet.fi)."}

      ;; from FreeVerb.sc
      ;; // blackrain's freeverb ugen.

      ;; FreeVerb : UGen {
      ;; 	*ar { arg in, mix = 0.33, room = 0.5, damp = 0.5, mul = 1.0, add = 0.0;
      ;; 		^this.multiNew('audio', in, mix, room, damp).madd(mul, add)
      ;; 	}
      ;; }

      {:name "FreeVerb",
       :args [{:name "in", :doc "The input signal"}
              {:name "mix", :default 0.33, :doc "Dry/wet balance. range 0..1"}
              {:name "room", :default 0.5, :doc "Room size. rage 0..1"}
              {:name "damp", :default 0.5, :doc "Reverb HF damp. range 0..1"}],
       :rates #{:ar}
       :doc "A reverb coded from experiments with faust. Valid parameter range from 0 to 1. Values outside this range are clipped by the UGen."}

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
       :args [{:name "in", :doc "Input signal channel 1"}
              {:name "in2", :doc "Input signal channel 2"}
              {:name "mix", :default 0.33, :doc "Dry/wet balance. range 0..1"}
              {:name "room", :default 0.5, :doc "Room size. rage 0..1"}
              {:name "damp", :default 0.5, :doc "Reverb HF damp. range 0..1"}],
       :rates #{:ar},
       :num-outs 2,
       :doc "A two-channel reverb coded from experiments with faust. Valid parameter range from 0 to 1. Values outside this range are clipped by the UGen."}

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
       :args [{:name "in", :default 0.0 :doc "The imput signal"}
              {:name "freq", :default 100.0, :doc "The cutoff frequency"}
              {:name "gain", :default 2.0, :doc "The filter resonance gain, between zero and 4"}
              {:name "reset", :default 0.0, :doc "When greater than zero, this will reset the state of the digital filters at the beginning of a computational block."}]
       :doc "A digital implementation of the Moog VCF (filter)."}

      ;; from PhysicalModel.sc
      ;; Spring : UGen {
      ;;  *ar { arg in=0.0, spring=1, damp=0;
      ;;    ^this.multiNew('audio', in, spring, damp)
      ;;  }
      ;; }

      {:name "Spring",
       :args [{:name "in", :default 0.0, :doc "Modulated input force"}
              {:name "spring", :default 0.0, :doc "Spring constant (incl. mass)"}
              {:name "damp", :default 0.0, :doc "Damping"}]
       :doc "Physical model of resonating spring"}

      ;; from PhysicalModel.sc
      ;; Ball : UGen {
      ;;  *ar { arg in=0.0, g=1, damp=0, friction=0.01;
      ;;    ^this.multiNew('audio', in, g, damp, friction)
      ;;  }
      ;; }

      {:name "Ball",
       :args [{:name "in", :default 0.0, :doc "modulated surface level"}
              {:name "g", :default 1.0, :doc "gravity"}
              {:name "damp", :default 0.0, :doc "damping on impact"}
              {:name "friction", :default 0.01, :doc "proximity from which on attraction to surface starts"}]
       :doc "models the path of a bouncing object that is reflected by a vibrating surface"}

      ;; from PhysicalModel.sc
      ;; TBall : UGen {
      ;;  *ar { arg in=0.0, g=10, damp=0, friction=0.01;
      ;;    ^this.multiNew('audio', in, g, damp, friction)
      ;;  }
      ;; }

      {:name "TBall",
       :args [{:name "in", :default 0.0, :doc "modulated surface level"}
              {:name "g", :default 10.0, :doc "gravity"}
              {:name "damp", :default 0.0, :doc "damping on impact"}
              {:name "friction", :default 0.01, :doc "proximity from which on attraction to surface starts"}]
       :doc "models the impacts of a bouncing object that is reflected by a vibrating surface"}

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
       :args [{:name "in", :default 0.0, :doc "the UGen whose output is to be tested"}
              {:name "id", :default 0.0, :doc "an id number to identify this UGen. The default is 0."}
              {:name "post", :default 2.0, :doc "One of three post modes: 0 = no posting, 1 = post a line for every bad value, 2 = post a line only when the floating-point classification changes (e.g., normal -> NaN and vice versa)"}]
       :check (when-ar (same-rate-as-first-input))
       :doc "tests for infinity, NaN (not a number), and denormals. If one of these is found, it posts a warning. Its output is as follows: 0 = a normal float, 1 = NaN, 2 = infinity, and 3 = a denormal."}

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
       :args [{:name "ampdist", :default 1.0, :doc "Choice of probability distribution for the next perturbation of the amplitude of a control point. The distributions are (adapted from the GENDYN program in Formalized Music): 0- LINEAR,1- CAUCHY, 2- LOGIST, 3- HYPERBCOS, 4- ARCSINE, 5- EXPON, 6- SINUS, Where the sinus (Xenakis' name) is in this implementation taken as sampling from a third party oscillator. See example below."}
              {:name "durdist", :default 1.0, :doc "Choice of distribution for the perturbation of the current inter control point duration."}
              {:name "adparam", :default 1.0, :doc "A parameter for the shape of the amplitude probability distribution, requires values in the range 0.0001 to 1 (there are safety checks in the code so don't worry too much if you want to modulate!)
"}
              {:name "ddparam", :default 1.0, :doc "A parameter for the shape of the duration probability distribution, requires values in the range 0.0001 to 1"}
              {:name "minfreq", :default 440.0, :doc "Minimum allowed frequency of oscillation for the Gendy1 oscillator, so gives the largest period the duration is allowed to take on."}
              {:name "maxfreq", :default 660.0, :doc "Maximum allowed frequency of oscillation for the Gendy1 oscillator, so gives the smallest period the duration is allowed to take on. "}
              {:name "ampscale", :default 0.5, :doc "Normally 0.0 to 1.0, multiplier for the distribution's delta value for amplitude. An ampscale of 1.0 allows the full range of  -1 to 1 for a change of amplitude."}
              {:name "durscale", :default 0.5, :doc "Normally 0.0 to 1.0, multiplier for the distribution's delta value for duration. An ampscale of 1.0 allows the full range of  -1 to 1 for a change of duration."}
              {:name "initCPs", :default 12, :doc "Initialise the number of control points in the memory. Xenakis specifies 12. There would be this number of control points per cycle of the oscillator, though the oscillator's period will constantly change due to the duration distribution."}
              {:name "knum" :default 12, :doc "Current number of utilised control points, allows modulation."}]
       :doc "An implementation of the dynamic stochastic synthesis generator conceived by Iannis Xenakis and described in Formalized Music (1992, Stuyvesant, NY: Pendragon Press) chapter 9 (pp 246-254) and chapters 13 and 14 (pp 289-322). The BASIC program in the book was written by Marie-Helene Serra so I think it helpful to credit her too. The program code has been adapted to avoid infinities in the probability distribution functions. The distributions are hard-coded in C but there is an option to have new amplitude or time breakpoints sampled from a continuous controller input."}

      ;; Gendy2 : UGen {
      ;;      *ar { arg ampdist=1, durdist=1, adparam=1.0, ddparam=1.0, minfreq=440, maxfreq=660, ampscale= 0.5, durscale=0.5, initCPs= 12, knum, a=1.17, c=0.31, mul=1.0,add=0.0;
      ;;               ^this.multiNew('audio', ampdist, durdist, adparam, ddparam, minfreq, maxfreq, ampscale, durscale, initCPs, knum ? initCPs, a, c).madd( mul, add )
      ;;           }
      ;;      *kr {arg ampdist=1, durdist=1, adparam=1.0, ddparam=1.0, minfreq=20, maxfreq=1000, ampscale= 0.5, durscale=0.5, initCPs= 12, knum, a=1.17, c=0.31, mul=1.0,add=0.0;
      ;;              ^this.multiNew('control', ampdist, durdist, adparam, ddparam, minfreq, maxfreq, ampscale, durscale, initCPs, knum ? initCPs, a, c).madd( mul, add )
      ;;           }
      ;;         }

      {:name "Gendy2",
       :args [{:name "ampdist", :default 1.0, :doc "Choice of probability distribution for the next perturbation of the amplitude of a control point. The distributions are (adapted from the GENDYN program in Formalized Music): 0- LINEAR, 1- CAUCHY, 2- LOGIST, 3- HYPERBCOS, 4- ARCSINE, 5- EXPON, 6- SINUS, Where the sinus (Xenakis' name) is in this implementation taken as sampling from a third party oscillator. "}
              {:name "durdist", :default 1.0, :doc "Choice of distribution for the perturbation of the current inter control point duration. "}
              {:name "adparam", :default 1.0, :doc "A parameter for the shape of the amplitude probability distribution, requires values in the range 0.0001 to 1 (there are safety checks in the code so don't worry too much if you want to modulate!)"}
              {:name "ddparam", :default 1.0, :doc "A parameter for the shape of the duration probability distribution, requires values in the range 0.0001 to 1"}
              {:name "minfreq", :default 440.0, :doc "Minimum allowed frequency of oscillation for the Gendy1 oscillator, so gives the largest period the duration is allowed to take on."}
              {:name "maxfreq", :default 660.0, :doc "Maximum allowed frequency of oscillation for the Gendy1 oscillator, so gives the smallest period the duration is allowed to take on."}
              {:name "ampscale", :default 0.5, :doc "Normally 0.0 to 1.0, multiplier for the distribution's delta value for amplitude. An ampscale of 1.0 allows the full range of  -1 to 1 for a change of amplitude."}
              {:name "durscale", :default 0.5, :doc "Normally 0.0 to 1.0, multiplier for the distribution's delta value for duration. An ampscale of 1.0 allows the full range of  -1 to 1 for a change of duration."}
              {:name "initCPs", :default 12, :doc "Initialise the number of control points in the memory. Xenakis specifies 12. There would be this number of control points per cycle of the oscillator, though the oscillator's period will constantly change due to the duration distribution."}
              {:name "knum" :default 12, :doc "Current number of utilised control points, allows modulation. "}
              {:name "a", :default 1.17, :doc "parameter for Lehmer random number generator perturbed by Xenakis as in ((old*a)+c)%1.0"}
              {:name "c", :default 0.31, :doc "parameter for Lehmer random number generator perturbed by Xenakis"}]
       :doc "See gendy1 help file for background. This variant of GENDYN is closer to that presented in Hoffmann, Peter. (2000) The New GENDYN Program. Computer Music Journal 24:2, pp 31-38. "}

      ;; Gendy3 : UGen {

      ;;      *ar { arg ampdist=1, durdist=1, adparam=1.0, ddparam=1.0, freq=440, ampscale= 0.5, durscale=0.5, initCPs= 12, knum, mul=1.0,add=0.0;
      ;;               ^this.multiNew('audio', ampdist, durdist, adparam, ddparam, freq, ampscale, durscale, initCPs, knum ? initCPs).madd( mul, add )
      ;;           }

      ;;      *kr {arg ampdist=1, durdist=1, adparam=1.0, ddparam=1.0, freq=440, ampscale= 0.5, durscale=0.5, initCPs= 12, knum, mul=1.0,add=0.0;
      ;;              ^this.multiNew('control', ampdist, durdist, adparam, ddparam, freq, ampscale, durscale, initCPs, knum ? initCPs).madd( mul, add )
      ;;           }

      ;;         }

      {:name "Gendy3",
       :args [{:name "ampdist", :default 1.0, :doc "Choice of probability distribution for the next perturbation of the amplitude of a control point. The distributions are (adapted from the GENDYN program in Formalized Music): 0- LINEAR,1- CAUCHY, 2- LOGIST, 3- HYPERBCOS, 4- ARCSINE, 5- EXPON, 6- SINUS, Where the sinus (Xenakis' name) is in this implementation taken as sampling from a third party oscillator."}
              {:name "durdist", :default 1.0, :doc "Choice of distribution for the perturbation of the current inter control point duration."}
              {:name "adparam", :default 1.0, :doc "A parameter for the shape of the amplitude probability distribution, requires values in the range 0.0001 to 1 (there are safety checks in the code so don't worry too much if you want to modulate!)"}
              {:name "ddparam", :default 1.0, :doc "A parameter for the shape of the duration probability distribution, requires values in the range 0.0001 to 1"}
              {:name "freq", :default 440.0, :doc "Oscillation frquency."}
              {:name "ampscale", :default 0.5, :doc "Normally 0.0 to 1.0, multiplier for the distribution's delta value for amplitude. An ampscale of 1.0 allows the full range of  -1 to 1 for a change of amplitude."}
              {:name "durscale", :default 0.5, :doc "Normally 0.0 to 1.0, multiplier for the distribution's delta value for duration. An ampscale of 1.0 allows the full range of  -1 to 1 for a change of duration."}
              {:name "initCPs", :default 12, :doc "Initialise the number of control points in the memory. Xenakis specifies 12. There would be this number of control points per cycle of the oscillator, though the oscillator's period will constantly change due to the duration distribution."}
              {:name "knum" :default 12, :doc "Current number of utilised control points, allows modulation."}]
      :doc "See Gendy1 help file for background. This variant of GENDYN normalises the durations in each period to force oscillation at the desired pitch. The breakpoints still get perturbed as in Gendy1. There is some glitching in the oscillator caused by the stochastic effects: control points as they vary cause big local jumps of amplitude. Put ampscale and durscale low to minimise this. All parameters can be modulated at control rate except for initCPs which is used only at initialisation."}])

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

(ns overtone.sc.machinery.ugen.metadata.misc
  (:use [overtone.sc.machinery.ugen common check]))

(def specs
     [
      {:name "PitchShift",
       :args [{:name "in", :doc "The input signal."}
              {:name "window-size",
               :default 0.2,
               :doc "The size of the grain window in seconds. This value
                     cannot be modulated." }

              {:name "pitch-ratio",
               :default 1.0,
               :doc "The ratio of the pitch shift. Must be from 0.0 to
                     4.0"}

              {:name "pitch-dispersion",
               :default
               0.0,
               :doc "The maximum random deviation of the pitch from the
                     pitchRatio." }

              {:name "time-dispersion",
               :default 0.0,
               :doc "A random offset of from zero to timeDispersion
                     seconds is added to the delay of each grain. Use of
                     some dispersion can alleviate a hard comb filter
                     effect due to uniform grain placement. It can also
                     be an effect in itself. timeDispersion can be no
                     larger than windowSize." }],
       :rates #{:ar}
       :check [(same-rate-as-first-input)
               (nth-input-stream? 0)]
       :doc "A time domain granular pitch shifter. Grains have a
             triangular amplitude envelope and an overlap of 4:1." }

      {:name "Pluck",
       :args [{:name "in",
               :default 0.0,
               :doc "An excitation signal."}

              {:name "trig",
               :default 1.0,
               :doc "Upon a negative to positive transition, the
                     excitation signal will be fed into the delay line." }

              {:name "maxdelaytime",
               :default 0.2,
               :doc "The max delay time in seconds (initializes the
                     internal delay buffer)." }

              {:name "delaytime",
               :default 0.2,
               :doc "Delay time in seconds."}

              {:name "decaytime",
               :default 1.0,
               :doc "Time for the echoes to decay by 60
                     decibels. Negative times emphasize odd partials."}

              {:name "coef",
               :default 0.5,
               :doc "The coef of the internal OnePole filter. Values
                     should be between -1 and +1 (larger values will be
                     unstable... so be careful!)." }],
       :rates #{:ar}
       :check (nth-input-stream? 0)
       :doc "Implements the Karplus-Strong style of synthesis, where a
             delay line (normally starting with noise) is filtered and
             fed back on itself so that over time it becomes periodic."}

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
       :args [{:name "in",
               :doc "Processing target."}

              {:name "fftsize",
               :doc "Spectral convolution partition size (twice
                     partition size). You must ensure that the blocksize
                     divides the partition size and there are at least
                     two blocks per partition (to allow for
                     amortisation)"}

              {:name "irbufnum",
               :doc "Prepared buffer of spectra for each partition of
                     the inpulse response"}],
       :rates #{:ar}
       :check (nth-input-stream? 0)
       :doc "Partitioned convolution. Various additional buffers must be
             supplied. Mono impulse response only! If inputting multiple
             channels, you'll need independent PartConvs, one for each
             channel. But the charm is: impulse response can be as large
             as you like (CPU load increases with IR size. Various
             tradeoffs based on fftsize choice, due to rarer but larger
             FFTs. This plug-in uses amortisation to spread processing
             and avoid spikes). Normalisation factors difficult to
             anticipate; convolution piles up multiple copies of the
             input on top of itself, so can easily overload."}

      {:name "Hilbert",
       :args [{:name "in"}],
       :rates #{:ar},
       :check (nth-input-stream? 0)
       :num-outs 2}

      {:name "FreqShift",
       :args [{:name "in",
               :doc "The signal to process"}

              {:name "freq",
               :default 0.0,
               :doc "Amount of shift in cycles per second"}

              {:name "phase",
               :default 0.0,
               :doc "Phase of the frequency shift (0 - 2pi)"}],

       :rates #{:ar},
       :check (nth-input-stream? 0)
       :doc "FreqShift implements single sideband amplitude modulation,
             also known as frequency shifting, but not to be confused
             with pitch shifting.  Frequency shifting moves all the
             components of a signal by a fixed amount but does not
             preserve the original harmonic relationships."}

      {:name "GVerb",
       :args [{:name "in",
               :doc "mono input"}

              {:name "roomsize",
               :default 10.0,
               :doc "in squared meters."}

              {:name "revtime",
               :default 3.0,
               :doc "in seconds"}

              {:name "damping",
               :default 0.5,
               :doc "0 to 1, high frequency rolloff, 0 damps the reverb
                     signal completely, 1 not at all"}

              {:name "inputbw",
               :default 0.5,
               :doc "0 to 1, same as damping control, but on the input
                     signal"}

              {:name "spread",
               :default 15.0,
               :doc "a control on the stereo spread and diffusion of the
                     reverb signal"}

              {:name "drylevel",
               :default 1.0,
               :doc "amount of dry signal"}

              {:name "earlyreflevel",
               :default 0.7,
               :doc "amount of early reflection level"}

              {:name "taillevel",
               :default 0.5
               :doc "amount of tail level"}

              {:name "maxroomsize", :
               default 300.0,
               :doc "to set the size of the delay lines." }],

       :rates #{:ar},
       :num-outs 2,
       :check (nth-input-stream? 0)
       :doc "A two-channel reverb UGen, based on the \"GVerb\" LADSPA
             effect by Juhana Sadeharju (kouhia at nic.funet.fi).

             WARNING - in the current version of the server, there are
             severe noise issues when you attempt to modify the roomsize
             or set it to a value greater than 40." }

      {:name "FreeVerb",
       :args [{:name "in",
               :doc "The input signal"}

              {:name "mix",
               :default 0.33,
               :doc "Dry/wet balance. range 0..1"}

              {:name "room",
               :default 0.5,
               :doc "Room size. rage 0..1"}

              {:name "damp",
               :default 0.5,
               :doc "Reverb HF damp. range 0..1"}],

       :rates #{:ar}
       :check (nth-input-stream? 0)
       :doc "A reverb coded from experiments with faust. Valid parameter
             range from 0 to 1. Values outside this range are clipped by the UGen."}

      {:name "FreeVerb2",
       :args [{:name "in",
               :doc "Input signal channel 1"}

              {:name "in2",
               :doc "Input signal channel 2"}

              {:name "mix",
               :default 0.33,
               :doc "Dry/wet balance. range 0..1"}

              {:name "room",
               :default 0.5,
               :doc "Room size. rage 0..1"}

              {:name "damp",
               :default 0.5,
               :doc "Reverb HF damp. range 0..1"}],

       :rates #{:ar},
       :num-outs 2,
       :check [(nth-input-stream? 0)
               (nth-input-stream? 1)]
       :doc "A two-channel reverb coded from experiments with
             faust. Valid parameter range from 0 to 1. Values outside
             this range are clipped by the UGen." }

      {:name "MoogFF",
       :args [{:name "in",
               :default 0.0
               :doc "The input signal"}

              {:name "freq",
               :default 100.0,
               :doc "The cutoff frequency"}

              {:name "gain",
               :default 2.0,
               :doc "The filter resonance gain, between zero and 4"}

              {:name "reset",
               :default 0.0,
               :doc "When greater than zero, this will reset the state
                     of the digital filters at the beginning of a
                     computational block."}]

       :rates #{:ar :kr}
       :check (nth-input-stream? 0)
       :doc "A digital implementation of the Moog VCF (filter)."}

      {:name "Spring",
       :args [{:name "in",
               :default 0.0,
               :doc "Modulated input force"}

              {:name "spring",
               :default 0.0,
               :doc "Spring constant (incl. mass)"}

              {:name "damp",
               :default 0.0,
               :doc "Damping"}]

       :rates #{:ar}
       :doc "Physical model of resonating spring"}


      {:name "Ball",
       :args [{:name "in",
               :default 0.0,
               :doc "modulated surface level"}

              {:name "g",
               :default 1.0,
               :doc "gravity"}

              {:name "damp",
               :default 0.0,
               :doc "damping on impact"}

              {:name "friction",
               :default 0.01,
               :doc "proximity from which on attraction to surface starts"}]

       :rates #{:ar}
       :doc "models the path of a bouncing object that is reflected by a
             vibrating surface"}

      {:name "TBall",
       :args [{:name "in",
               :default 0.0,
               :doc "modulated surface level"}

              {:name "g",
               :default 10.0,
               :doc "gravity"}

              {:name "damp",
               :default 0.0,
               :doc "damping on impact"}

              {:name "friction",
               :default 0.01,
               :doc "proximity from which on attraction to surface
                    starts"}]

       :rates #{:ar}
       :doc "models the impacts of a bouncing object that is reflected
            by a vibrating surface"}


      {:name "Gendy1",
       :args [{:name "ampdist",
               :default 1.0,
               :doc "Choice of probability distribution for the next
                     perturbation of the amplitude of a control
                     point. The distributions are (adapted from the
                     GENDYN program in Formalized Music): 0- LINEAR,1-
                     CAUCHY, 2- LOGIST, 3- HYPERBCOS, 4- ARCSINE, 5-
                     EXPON, 6- SINUS, Where the sinus (Xenakis' name) is
                     in this implementation taken as sampling from a
                     third party oscillator. See example below." }

              {:name "durdist",
               :default 1.0,
               :doc "Choice of distribution for the perturbation of the
                     current inter control point duration." }

              {:name "adparam",
               :default 1.0,
               :doc "A parameter for the shape of the amplitude
                     probability distribution, requires values in the
                     range 0.0001 to 1 (there are safety checks in the
                     code so don't worry too much if you want to
                     modulate!)
"}
              {:name "ddparam",
               :default 1.0,
               :doc "A parameter for the shape of the duration
                     probability distribution, requires values in the
                     range 0.0001 to 1"}

              {:name "minfreq",
               :default 440.0,
               :doc "Minimum allowed frequency of oscillation for the
                     Gendy1 oscillator, so gives the largest period the
                     duration is allowed to take on." }

              {:name "maxfreq",
               :default 660.0,
               :doc "Maximum allowed frequency of oscillation for the
                     Gendy1 oscillator, so gives the smallest period the
                     duration is allowed to take on. "}

              {:name "ampscale",
               :default 0.5,
               :doc "Normally 0.0 to 1.0, multiplier for the
                     distribution's delta value for amplitude. An
                     ampscale of 1.0 allows the full range of -1 to 1
                     for a change of amplitude." }

              {:name "durscale",
               :default 0.5,
               :doc "Normally 0.0 to 1.0, multiplier for the
                     distribution's delta value for duration. An
                     ampscale of 1.0 allows the full range of -1 to 1
                     for a change of duration." }

              {:name "init-cps",
               :default 12,
               :doc "Initialise the number of control points in the
                     memory. Xenakis specifies 12. There would be this
                     number of control points per cycle of the
                     oscillator, though the oscillator's period will
                     constantly change due to the duration
                     distribution." }

              {:name "knum"
               :default 12,
               :doc "Current number of utilised control points, allows
                     modulation." }]

       :rates #{:ar :kr}
       :doc "An implementation of the dynamic stochastic synthesis
             generator conceived by Iannis Xenakis and described in
             Formalized Music (1992, Stuyvesant, NY: Pendragon Press)
             chapter 9 (pp 246-254) and chapters 13 and 14 (pp
             289-322). The BASIC program in the book was written by
             Marie-Helene Serra so I think it helpful to credit her
             too. The program code has been adapted to avoid infinities
             in the probability distribution functions. The
             distributions are hard-coded in C but there is an option to
             have new amplitude or time breakpoints sampled from a
             continuous controller input." }

      {:name "Gendy2",
       :args [{:name "ampdist",
               :default 1.0,
               :doc "Choice of probability distribution for the next
                     perturbation of the amplitude of a control
                     point. The distributions are (adapted from the
                     GENDYN program in Formalized Music): 0- LINEAR, 1-
                     CAUCHY, 2- LOGIST, 3- HYPERBCOS, 4- ARCSINE, 5-
                     EXPON, 6- SINUS, Where the sinus (Xenakis' name) is
                     in this implementation taken as sampling from a
                     third party oscillator. "}

              {:name "durdist",
               :default 1.0,
               :doc "Choice of distribution for the perturbation of the
                     current inter control point duration. "}

              {:name "adparam",
               :default 1.0,
               :doc "A parameter for the shape of the amplitude
                     probability distribution, requires values in the
                     range 0.0001 to 1 (there are safety checks in the
                     code so don't worry too much if you want to
                     modulate!)" }

              {:name "ddparam",
               :default 1.0,
               :doc "A parameter for the shape of the duration
                     probability distribution, requires values in the
                     range 0.0001 to 1"}

              {:name "minfreq",
               :default 440.0,
               :doc "Minimum allowed frequency of oscillation for the
                     Gendy1 oscillator, so gives the largest period the
                     duration is allowed to take on." }

              {:name "maxfreq",
               :default 660.0,
               :doc "Maximum allowed frequency of oscillation for the
                     Gendy1 oscillator, so gives the smallest period the
                     duration is allowed to take on." }

              {:name "ampscale",
               :default 0.5,
               :doc "Normally 0.0 to 1.0, multiplier for the
                     distribution's delta value for amplitude. An
                     ampscale of 1.0 allows the full range of -1 to 1
                     for a change of amplitude." }

              {:name "durscale",
               :default 0.5,
               :doc "Normally 0.0 to 1.0, multiplier for the
                     distribution's delta value for duration. An
                     ampscale of 1.0 allows the full range of -1 to 1
                     for a change of duration." }

              {:name "init-cps",
               :default 12,
               :doc "Initialise the number of control points in the
                     memory. Xenakis specifies 12. There would be this
                     number of control points per cycle of the
                     oscillator, though the oscillator's period will
                     constantly change due to the duration
                     distribution." }

              {:name "knum"
               :default 12,
               :doc "Current number of utilised control points, allows
                     modulation. "}

              {:name "a",
               :default 1.17,
               :doc "parameter for Lehmer random number generator
                     perturbed by Xenakis as in ((old*a)+c)%1.0"}

              {:name "c",
               :default 0.31,
               :doc "parameter for Lehmer random number generator
                     perturbed by Xenakis"}]

       :rates #{:ar :kr}
       :doc "See gendy1 help file for background. This variant of GENDYN
             is closer to that presented in Hoffmann, Peter. (2000) The
             New GENDYN Program. Computer Music Journal 24:2, pp
             31-38. "}


      {:name "Gendy3",
       :args [{:name "ampdist",
               :default 1.0,
               :doc "Choice of probability distribution for the next
                     perturbation of the amplitude of a control
                     point. The distributions are (adapted from the
                     GENDYN program in Formalized Music): 0- LINEAR,1-
                     CAUCHY, 2- LOGIST, 3- HYPERBCOS, 4- ARCSINE, 5-
                     EXPON, 6- SINUS, Where the sinus (Xenakis' name) is
                     in this implementation taken as sampling from a
                     third party oscillator." }

              {:name "durdist",
               :default 1.0,
               :doc "Choice of distribution for the perturbation of the
                     current inter control point duration." }

              {:name "adparam",
               :default 1.0,
               :doc "A parameter for the shape of the amplitude
                     probability distribution, requires values in the
                     range 0.0001 to 1 (there are safety checks in the
                     code so don't worry too much if you want to
                     modulate!)" }

              {:name "ddparam",
               :default 1.0,
               :doc "A parameter for the shape of the duration
                     probability distribution, requires values in the
                     range 0.0001 to 1"}

              {:name "freq",
               :default 440.0,
               :doc "Oscillation frquency."}

              {:name "ampscale",
               :default 0.5,
               :doc "Normally 0.0 to 1.0, multiplier for the
                     distribution's delta value for amplitude. An
                     ampscale of 1.0 allows the full range of -1 to 1
                     for a change of amplitude." }

              {:name "durscale",
               :default 0.5,
               :doc "Normally 0.0 to 1.0, multiplier for the
                     distribution's delta value for duration. An
                     ampscale of 1.0 allows the full range of -1 to 1
                     for a change of duration." }

              {:name "init-cps",
               :default 12,
               :doc "Initialise the number of control points in the
                     memory. Xenakis specifies 12. There would be this
                     number of control points per cycle of the
                     oscillator, though the oscillator's period will
                     constantly change due to the duration
                     distribution." }

              {:name "knum"
               :default 12,
               :doc "Current number of utilised control points, allows
                     modulation." }]

       :rates #{:ar :kr}
      :doc "See Gendy1 help file for background. This variant of GENDYN
            normalises the durations in each period to force oscillation
            at the desired pitch. The breakpoints still get perturbed as
            in Gendy1. There is some glitching in the oscillator caused
            by the stochastic effects: control points as they vary cause
            big local jumps of amplitude. Put ampscale and durscale low
            to minimise this. All parameters can be modulated at control
            rate except for initCPs which is used only at
            initialisation." }])

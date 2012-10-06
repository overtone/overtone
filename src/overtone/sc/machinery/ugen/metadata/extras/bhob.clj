(ns overtone.sc.machinery.ugen.metadata.extras.bhob
  (:use [overtone.sc.machinery.ugen common check]))


(def specs
  [
   {:name "Henon2DN"
    :summary "hénon map 2D chaotic generator with no interpolation"
    :args [{:name "minfreq"
            :default 11025
            :default:kr 40
            :doc "iteration frequency in Hertz"}

           {:name "maxfreq"
            :default 22050
            :default:kr 100
            :doc "iteration frequency in Hertz"}

           {:name "a"
            :default 1.4
            :doc "equation variable"}

           {:name "b"
            :default 0.3
            :doc "equation variable"}

           {:name "x0"
            :default 0.30501993062401
            :doc "initial value of x"}

           {:name "y0"
            :default 0.20938865431933
            :doc "initial value of y"}]

    :rates #{:ar :kr}
    :doc "The Hénon map is a discrete-time dynamical system. It is one
          of the most studied examples of dynamical systems that exhibit
          chaotic behaviour.

          The map depends on two parameters, a and b, which for the
          canonical Hénon map have values of a = 1.4 and b = 0.3. For
          the canonical values the Hénon map is chaotic. For other
          values of a and b the map may be chaotic, intermittent, or
          converge to a periodic orbit. An overview of the type of
          behaviour of the map at different parameter values may be
          obtained from its orbit diagram.

          For more information see the wikipedia article:

          http://en.wikipedia.org/wiki/Hénon_map" }

   {:name "Henon2DL"
    :extends "Henon2DN"
    :summary "hénon map 2D chaotic generator with linear interpolation"}

   {:name "Henon2DC"
    :extends "Henon2DN"
    :summary "hénon map 2D chaotic generator with cubic interpolation"}

   {:name "HenonTrig"
    :extends "Henon2DN"
    :summary "hénon map 2D chaotic trigger"
    :args [{:name "minfreq"
            :default 5
            :doc "iteration frequency in Hertz"}

           {:name "maxfreq"
            :default 10
            :doc "iteration frequency in Hertz"}

           {:name "a"
            :default 1.4
            :doc "Hénon equation variable"}

           {:name "b"
            :default 0.3
            :doc "Hénon equation variable"}

           {:name "x0"
            :default 0.30501993062401
            :doc "initial value of x"}

           {:name "y0"
            :default 0.20938865431933
            :doc "initial value of y"}]

    :rates #{:ar :kr}}


   {:name "Gbman2DN"
    :summary "gingerbreadman map 2D chaotic generator with no interpolation"
    :args [{:name "minfreq"
            :default 11025
            :default:kr 40
            :doc "iteration frequency in Hertz"}

           {:name "maxfreq"
            :default 22050
            :default:kr 100
            :doc "iteration frequency in Hertz"}

           {:name "x0"
            :default 1.2
            :doc "initial value of x"}

           {:name "y0"
            :default 2.1
            :doc "initial value of y"}]

    :rates #{:ar :kr}
    :doc "A two-dimensional piecewise linear chaotic 2D map. See
          wikipedia article for more information:

          http://en.wikipedia.org/wiki/Gingerbreadman_map"}

   {:name "Gbman2DL"
    :extends "Gbman2DN"
    :summary "gingerbreadman map 2D chaotic generator with linear interpolation"}

   {:name "Gbman2DC"
    :extends "Gbman2DN"
    :summary "gingerbreadman map 2D chaotic generator with cubic interpolation"}

   {:name "GbmanTrig"
    :extends "Gbman2DN"
    :summary "gingerbreadman map 2D chaotic trigger"
    :args [{:name "minfreq"
            :default 5
            :doc ""}

           {:name "maxfreq"
            :default 10
            :doc ""}

           {:name "x0"
            :default 1.2
            :doc ""}

           {:name "y0"
            :default 2.1
            :doc ""}]}

   {:name "Standard2DN"
    :summary "standard map 2D chaotic generator with no interpolation"
    :args [{:name "minfreq"
            :default 11025
            :default:kr 40
            :doc "iteration frequency in Hertz"}

           {:name "maxfreq"
            :default 22050
            :default:kr 100
            :doc "iteration frequency in Hertz"}

           {:name "k"
            :default 1.4
            :doc "perturbation amount"}

           {:name "x0"
            :default 4.9789799812499
            :doc "initial value of x"}

           {:name "y0"
            :default 5.7473416156381
            :doc "initial value of y"}]

    :rates #{:ar :kr}
    :doc "The standard map is an area-preserving chaotic map from a
          square with side 2pi onto itself. This map is also known as
          the Chirikov–Taylor map or as the Chirikov standard map. For
          more information see the wikipedia article:

          http://en.wikipedia.org/wiki/Standard_map"}

   {:name "Standard2DL"
    :extends "Standard2DN"
    :summary "standard map 2D chaotic generator with linear interpolation"}

   {:name "Standard2DC"
    :extends "Standard2DN"
    :summary "standard map 2D chaotic generator with cubic interpolation"}

   {:name "StandardTrig"
    :extends "Standard2DN"
    :summary "standard map 2D chaotic trigger"
    :args [{:name "minfreq"
            :default 5
            :doc "iteration frequency in Hertz"}

           {:name "maxfreq"
            :default 10
            :doc "iteration frequency in Hertz"}

           {:name "k"
            :default 1.4
            :doc "perturbation amount"}

           {:name "x0"
            :default 4.9789799812499
            :doc "initial value of x"}

           {:name "y0"
            :default 5.7473416156381
            :doc "initial value of y"}]}


   {:name "Latoocarfian2DN"
    :summary "latoocarfian 2D chaotic generator with no interpolation"
    :args [{:name "minfreq"
            :default 11025
            :default:kr 40
            :doc "iteration frequency in Hertz"}

           {:name "maxfreq"
            :default 22050
            :default:kr 100
            :doc "iteration frequency in Hertz"}

           {:name "a"
            :default 1
            :doc "Latoocarfian equation variable"}

           {:name "b"
            :default 3
            :doc "Latoocarfian equation variable"}

           {:name "c"
            :default 0.5
            :doc "Latoocarfian equation variable"}

           {:name "d"
            :default 0.5
            :doc "Latoocarfian equation variable"}

           {:name "x0"
            :default 0.34082301375036
            :doc "initial value of x"}

           {:name "y0"
            :default -0.38270086971332
            :doc "initial value of y"}]

    :rates #{:ar :kr}
    :doc "This is a function given in Clifford Pickover's book Chaos In
          Wonderland, pg 26.

          The function has four parameters a, b, c, and d. The function is:

          xnew = sin(y * b) + c * sin(x * b);

          ynew = sin(x * a) + d * sin(y * a);

          x = xnew;

          y = ynew;

          output = x;

          x values determine frequencies; y values determine amplitudes.
          Stable ranges for a & b tend to be between -3 to + 3. c & d
          between 0.5 and 1.5.  There are combinations within these
          ranges that are unstable, so be prepared to tweak this
          oscillator." }

   {:name "Latoocarfian2DL"
    :extends "Latoocarfian2DN"
    :summary "latoocarfian 2D chaotic generator with linear interpolation"}

   {:name "Latoocarfian2DC"
    :extends "Latoocarfian2DN"
    :summary "latoocarfian 2D chaotic generator with cubic interpolation"}

   {:name "LatoocarfianTrig"
    :extends "Latoocarfian2DN"
    :summary "latoocarfian 2D chaotic trigger"
    :args [{:name "minfreq"
            :default 5
            :doc ""}

           {:name "maxfreq"
            :default 10
            :doc "iteration frequency in Hertz"}

           {:name "a"
            :default 1
            :doc "iteration frequency in Hertz"}

           {:name "b"
            :default 3
            :doc "Latoocarfian equation variable"}

           {:name "c"
            :default 0.5
            :doc "Latoocarfian equation variable"}

           {:name "d"
            :default 0.5
            :doc "Latoocarfian equation variable"}

           {:name "x0"
            :default 0.34082301375036
            :doc "intial value of x"}

           {:name "y0"
            :default -0.38270086971332
            :doc "initial value of y"}]}

   {:name "Lorenz2DN"
    :summary "lorenz 2D chaotic generator with no interpolation"
    :args [{:name "minfreq"
            :default 11025
            :default:kr 40
            :doc "iteration frequency in Hertz"}

           {:name "maxfreq"
            :default 22050
            :default:kr 100
            :doc "iteration frequency in Hertz"}

           {:name "s"
            :default 10
            :doc "Lorenz equation variable"}

           {:name "r"
            :default 28
            :doc "Lorenz equation variable"}

           {:name "b"
            :default 2.6666667
            :doc "Lorenz equation variable"}

           {:name "h"
            :default 0.02
            :doc "Lorenz equation variable"}

           {:name "x0"
            :default 0.090879182417163
            :doc "initial value of x"}

           {:name "y0"
            :default 2.97077458055
            :doc "initial value of y"}

           {:name "z0"
            :default 24.282041054363
            :doc "initial value of z"}]

    :rates #{:ar :kr}
    :doc "The Lorenz system is a system of ordinary differential
          equations (the Lorenz equations) first studied by Edward
          Lorenz. It is notable for having chaotic solutions for certain
          parameter values and initial conditions. In particular, the
          Lorenz attractor is a set of chaotic solutions of the Lorenz
          system which, when plotted, resemble a butterfly or figure
          eight.

          The equation is as follows:

          x' = s(y - x)

          y' = x(r - z) - y

          z' = xy - bz

          The time step amount determines the rate at which the ODE is
          evaluated.  Higher values will increase the rate, but cause
          more instability.  This generator uses a different algorithm
          than the LorenzN/L/C ugen included with current distributions.
          The resulting sound is somewhat different, and it also means
          that becomes unstable around 0.02." }

   {:name "Lorenz2DL"
    :extends "Lorenz2DN"
    :summary "lorenz 2D chaotic generator with linear interpolation"}

   {:name "Lorenz2DC"
    :extends "Lorenz2DN"
    :summary "lorenz 2D chaotic generator with cubic interpolation"}

   {:name "LorenzTrig"
    :extends "Lorenz2DN"
    :summary "lorenz 2D chaotic trigger"}

   {:name "Fhn2DN"
    :summary "FitzHughNagumo Neuron Firing Oscillator with no interpolation"
    :args [{:name "minfreq"
            :default 11025
            :default:kr 40
            :doc "iteration frequency in Hertz"}

           {:name "maxfreq"
            :default 22050
            :default:kr 100
            :doc "iteration frequency in Hertz"}

           {:name "urate"
            :default 0.1
            :doc "update rate for u"}

           {:name "wrate"
            :default 0.1
            :doc "update rate for w"}

           {:name "b0"
            :default 0.6
            :doc "equation constant"}

           {:name "b1"
            :default 0.8
            :doc "equation constant"}

           {:name "i"
            :default 0
            :doc "arbitrary external impulse; i.e. pulse wave, trigger,
                 lfnoise, or nothing." }

           {:name "u0"
            :default 0
            :doc "reset value for u"}

           {:name "w0"
            :default 0
            :doc "reset value for w"}]

    :rates #{:ar :kr}
    :doc "The FitzHugh–Nagumo model is a simplified version of the
          Hodgkin–Huxley model which models in a detailed manner
          activation and deactivation dynamics of a spiking neuron. For
          more information see the wikipedia article:

          http://en.wikipedia.org/wiki/FitzHugh–Nagumo_model"}

   {:name "Fhn2DL"
    :extends "Fhn2DN"
    :summary "FitzHughNagumo Neuron Firing Oscillator with linear interpolation"}

   {:name "Fhn2DC"
    :extends "Fhn2DN"
    :summary "FitzHughNagumo Neuron Firing Oscillator with cubic interpolation"}


   {:name "FhnTrig"
    :extends "Fhn2DN"
    :summary "FitzHughNagumo Neuron Firing Oscillator trigger"
    :args [{:name "minfreq"
            :default 4
            :doc "iteration frequency in Hertz"}

           {:name "maxfreq"
            :default 10
            :doc "iteration frequency in Hertz"}

           {:name "urate"
            :default 0.1
            :doc "update rate for u"}

           {:name "wrate"
            :default 0.1
            :doc "update rate for 2"}

           {:name "b0"
            :default 0.6
            :doc "equation constant"}

           {:name "b1"
            :default 0.8
            :doc "equation constant"}

           {:name "i"
            :default 0
            :doc "arbitrary external impulse; i.e. pulse wave, trigger,
                  lfnoise or nothing"}

           {:name "u0"
            :default 0
            :doc "reset value for u"}

           {:name "w0"
            :default 0
            :doc "reset value for w"}]}

   {:name "PV_CommonMag"
    :summary "returns common magnitudes"
    :args [{:name "buffer-a"
            :doc "FFT buffer a"}

           {:name "buffer-b"
            :doc "FFT buffer b"}

           {:name "tolerance"
            :default 0
            :doc "magnitudes within which test will pass"}

           {:name "remove"
            :default 0
            :doc "scale uncommon magnitudes"}]
    :rates #{:kr}
    :doc "Returns magnitudes common to buffer-a & buffer-b within a
         tolerance level." }

   {:name "PV_CommonMul"
    :args [{:name "buffer-a"
            :doc ""}

           {:name "buffer-b"
            :doc ""}

           {:name "tolerance"
            :default 0
            :doc ""}

           {:name "remove"
            :default 0
            :doc ""}]
    :rates #{:kr}
    :doc ""}

   {:name "PV_MagMinus"
    :args [{:name "buffer-a"
            :doc ""}

           {:name "buffer-b"
            :doc ""}

           {:name "remove"
            :default 1
            :doc ""}]
    :rates #{:kr}
    :doc ""}

   {:name "PV_MagGate"
    :args [{:name "buffer"
            :doc ""}

           {:name "thresh"
            :default 1
            :doc ""}

           {:name "remove"
            :default 1
            :doc ""}]
    :rates #{:kr}
    :doc ""}

   {:name "PV_Compander"
    :args [{:name "buffer"
            :doc ""}

           {:name "thresh"
            :default 50
            :doc ""}

           {:name "slope-below"
            :default 1
            :doc ""}

           {:name "slope-above"
            :default 1
            :doc ""}]
    :rates #{:kr}
    :doc ""}


   {:name "PV_MagScale"
    :args [{:name "buffer-a"
            :doc ""}

           {:name "buffer-b"
            :doc ""}]

    :rates #{:kr}
    :doc ""}

   {:name "PV_Morph"
    :args [{:name "buffer-a"
            :doc ""}

           {:name "buffer-b"
            :doc ""}

           {:name "morph"
            :default 0
            :doc ""}]

    :rates #{:kr}
    :doc ""}

   {:name "PV_XFade"
    :args [{:name "buffer-a"
            :doc ""}

           {:name "buffer-b"
            :doc ""}

           {:name "fade"
            :default 0
            :doc ""}]

    :rates #{:kr}
    :doc ""}

   {:name "PV_SoftWipe"
    :args [{:name "buffer-a"
            :doc ""}

           {:name "buffer-b"
            :doc ""}

           {:name "wipe"
            :default 0
            :doc ""}]

    :rates #{:kr}
    :doc ""}

   {:name "PV_Cutoff"
    :args [{:name "buffer-a"
            :doc ""}

           {:name "buffer-b"
            :doc ""}

           {:name "wipe"
            :default 0
            :doc ""}]

    :rates #{:kr}
    :doc ""}

   {:name "NestedAllpassN"
    :summary ""
    :args [{:name "in"
            :doc "Input signal"}

           {:name "max-delay1"
            :default 0.036
            :doc ""}

           {:name "delay1"
            :default 0.036
            :doc ""}

           {:name "gain1"
            :default 0.08
            :doc ""}

           {:name "max-delay2"
            :default 0.03
            :doc ""}

           {:name "delay2"
            :default 0.03
            :doc ""}

           {:name "gain2"
            :default 0.3
            :doc ""}]

    :rates #{:ar}
    :doc ""}

   {:name "NestedAllpassL"
    :summary ""
    :args [{:name "in"
            :doc "Input signal"}

           {:name "max-delay1"
            :default 0.036
            :doc ""}

           {:name "delay1"
            :default 0.036
            :doc ""}

           {:name "gain1"
            :default 0.08
            :doc ""}

           {:name "max-delay2"
            :default 0.03
            :doc ""}

           {:name "delay2"
            :default 0.03
            :doc ""}

           {:name "gain2"
            :default 0.3
            :doc ""}]

    :rates #{:ar}
    :doc ""}

   {:name "NestedAllpassC"
    :summary ""
    :args [{:name "in"
            :doc "Input signal"}

           {:name "max-delay1"
            :default 0.036
            :doc ""}

           {:name "delay1"
            :default 0.036
            :doc ""}

           {:name "gain1"
            :default 0.08
            :doc ""}

           {:name "max-delay2"
            :default 0.03
            :doc ""}

           {:name "delay2"
            :default 0.03
            :doc ""}

           {:name "gain2"
            :default 0.3
            :doc ""}]

    :rates #{:ar}
    :doc ""}

   {:name "DoubleNestedAllpassN"
    :summary "Double Nested Allpass Filter N"
    :args [{:name "in"
            :doc "Input signal"}

           {:name "max-delay1"
            :default 0.0047
            :doc ""}

           {:name "delay1"
            :default 0.0047
            :doc ""}

           {:name "gain1"
            :default 0.15
            :doc ""}

           {:name "max-delay2"
            :default 0.022
            :doc ""}

           {:name "delay2"
            :default 0.022
            :doc ""}

           {:name "gain2"
            :default 0.25
            :doc ""}

           {:name "max-delay3"
            :default 0.0083
            :doc ""}

           {:name "delay3"
            :default 0.0083
            :doc ""}

           {:name "gain3"
            :default 0.3
            :doc ""}]
    :rates #{:ar}
    :doc ""}

   {:name "DoubleNestedAllpassL"
    :summary "Double Nested Allpass Filter L"
    :args [{:name "in"
            :doc "Input signal"}

           {:name "max-delay1"
            :default 0.0047
            :doc ""}

           {:name "delay1"
            :default 0.0047
            :doc ""}

           {:name "gain1"
            :default 0.15
            :doc ""}

           {:name "max-delay2"
            :default 0.022
            :doc ""}

           {:name "delay2"
            :default 0.022
            :doc ""}

           {:name "gain2"
            :default 0.25
            :doc ""}

           {:name "max-delay3"
            :default 0.0083
            :doc ""}

           {:name "delay3"
            :default 0.0083
            :doc ""}

           {:name "gain3"
            :default 0.3
            :doc ""}]
    :rates #{:ar}
    :doc ""}

   {:name "DoubleNestedAllpassC"
    :summary "Double Nested Allpass Filter C"
    :args [{:name "in"
            :doc "Input signal"}

           {:name "max-delay1"
            :default 0.0047
            :doc ""}

           {:name "delay1"
            :default 0.0047
            :doc ""}

           {:name "gain1"
            :default 0.15
            :doc ""}

           {:name "max-delay2"
            :default 0.022
            :doc ""}

           {:name "delay2"
            :default 0.022
            :doc ""}

           {:name "gain2"
            :default 0.25
            :doc ""}

           {:name "max-delay3"
            :default 0.0083
            :doc ""}

           {:name "delay3"
            :default 0.0083
            :doc ""}

           {:name "gain3"
            :default 0.3
            :doc ""}]
    :rates #{:ar}
    :doc ""}


   {:name "MoogLadder"
    :summary "Moog Filter Emulation"
    :args [{:name "input"
            :doc "Audio input"}

           {:name "ffreq"
            :default 440
            :doc "Cutoff freq"}

           {:name "res"
            :default 0
            :doc "Resonance (0 -> 1)"}]
    :rates #{:ar :kr}
    :doc "Moog Filter."}


   {:name "RLPFD"
    :summary ""
    :args [{:name "input"
            :doc "Audio input"}

           {:name "ffreq"
            :default 440
            :doc "Cutoff freq"}

           {:name "res"
            :default 0
            :doc "Resonance (0 -> 1)"}

           {:name "dist"
            :default 0
            :doc "Resonance (0 -> 1)"}]
    :rates #{:ar :kr}
    :doc ""}


   {:name "Streson"
    :summary ""
    :args [{:name "input"
            :doc "Audio input"}

           {:name "delay-time"
            :default 0.003
            :doc ""}

           {:name "res"
            :default 0.9
            :doc "Resonance (0 -> 1)"}]

    :rates #{:ar :kr}
    :doc ""}


   {:name "NLFiltN"
    :summary ""
    :args [{:name "input"
            :doc "Audio input"}

           {:name "a"
            :doc ""}

           {:name "b"
            :doc ""}

           {:name "d"
            :doc ""}

           {:name "c"
            :doc ""}

           {:name "l"
            :doc ""}]

    :rates #{:ar :kr}
    :doc ""}

   {:name "NLFiltL"
    :summary ""
    :args [{:name "input"
            :doc "Audio input"}

           {:name "a"
            :doc ""}

           {:name "b"
            :doc ""}

           {:name "d"
            :doc ""}

           {:name "c"
            :doc ""}

           {:name "l"
            :doc ""}]

    :rates #{:ar :kr}
    :doc ""}

   {:name "NLFiltC"
    :summary ""
    :args [{:name "input"
            :doc "Audio input"}

           {:name "a"
            :doc ""}

           {:name "b"
            :doc ""}

           {:name "d"
            :doc ""}

           {:name "c"
            :doc ""}

           {:name "l"
            :doc ""}]

    :rates #{:ar :kr}
    :doc ""}

   {:name "GaussTrig"
    :summary "Impulses around a certain frequency"
    :args [{:name "freq"
            :default 440
            :doc "mean frequency"}

           {:name "dev"
            :default 0.3
            :doc "random deviation from mean (0 <= dev < 1)"}]
    :rates #{:ar :kr}
    :doc "Impulses around a certain frequency"}

   {:name "LFBrownNoise0"
    :summary ""
    :args [{:name "freq"
            :default 20
            :doc ""}

           {:name "dev"
            :default 1
            :doc ""}

           {:name "dist"
            :default 0
            :doc ""}]
    :rates #{:ar :kr}
    :doc ""}

   {:name "LFBrownNoise1"
    :summary ""
    :args [{:name "freq"
            :default 20
            :doc ""}

           {:name "dev"
            :default 1
            :doc ""}

           {:name "dist"
            :default 0
            :doc ""}]
    :rates #{:ar :kr}
    :doc ""}

   {:name "LFBrownNoise2"
    :summary ""
    :args [{:name "freq"
            :default 20
            :doc ""}

           {:name "dev"
            :default 1
            :doc ""}

           {:name "dist"
            :default 0
            :doc ""}]
    :rates #{:ar :kr}
    :doc ""}

   {:name "TBrownRand"
    :summary ""
    :args [{:name "lo"
            :default 0
            :doc ""}

           {:name "hi"
            :default 1
            :doc ""}

           {:name "dev"
            :default 1
            :doc ""}

           {:name "dist"
            :default 0
            :doc ""}

           {:name "trig"
            :default 0
            :doc ""}]
    :rates #{:ar :kr}
    :doc ""}

   {:name "Dbrown2"
    :summary "demand rate brownian movement with Gendyn distributions"
    :args [{:name "lo"
            :doc "minimum value"}

           {:name "hi"
            :doc "maximum value"}

           {:name "step"
            :doc "maximum step for each new value"}

           {:name "dist"
            :doc "gendyn distribution (see gendy1)"}

           {:name "length"
            :doc "number of values to create"}]

    :rates #{:dr}
    :doc "Dbrown2 returns numbers in the continuous range between lo and
          hi. The arguments can be a number or any other ugen."}


   {:name "DGauss"
    :summary ""
    :args [
           {:name "length"
            :default Float/POSITIVE_INFINITY
            :doc ""}

           {:name "lo"
            :doc ""}

           {:name "hi"
            :doc ""}
           ]
    :rates #{:dr}
    :internal-name true
    :doc ""}


   {:name "TGaussRand"
    :summary ""
    :args [
           {:name "lo"
            :default 0
            :doc ""}

           {:name "hi"
            :default 1
            :doc ""}

           {:name "trig"
            :default 0
            :doc ""}]

    :rates #{:ar :kr}
    :doc ""}

   {:name "TBetaRand"
    :summary ""
    :args [
           {:name "lo"
            :default 0
            :doc ""}

           {:name "hi"
            :default 1
            :doc ""}

           {:name "prob1"
            :doc ""}

           {:name "prob2"
            :doc ""}

           {:name "trig"
            :default 0
            :doc ""}]

    :rates #{:ar :kr}
    :doc ""}

   {:name "Gendy4"
    :summary ""
    :args [
           {:name "ampdist"
            :default 1
            :doc ""}

           {:name "adparam"
            :default 1
            :doc ""}

           {:name "ddparam"
            :default 1
            :doc ""}

           {:name "minfreq"
            :default 440
            :doc ""}

           {:name "maxfreq"
            :default 660
            :doc ""}

           {:name "ampscale"
            :default 0.5
            :doc ""}

           {:name "durscale"
            :default 0.5
            :doc ""}

           {:name "init-cps"
            :default 12
            :doc ""}

           {:name "knum"
            :default 12
            :doc ""}]

    :rates #{:ar :kr}
    :doc ""}

   {:name "Gendy5"
    :summary ""
    :args [
           {:name "ampdist"
            :default 1
            :doc ""}

           {:name "adparam"
            :default 1
            :doc ""}

           {:name "ddparam"
            :default 1
            :doc ""}

           {:name "minfreq"
            :default 440
            :doc ""}

           {:name "maxfreq"
            :default 660
            :doc ""}

           {:name "ampscale"
            :default 0.5
            :doc ""}

           {:name "durscale"
            :default 0.5
            :doc ""}

           {:name "init-cps"
            :default 12
            :doc ""}

           {:name "knum"
            :default 12
            :doc ""}]

    :rates #{:ar :kr}
    :doc ""}


   {:name "TGrains2"
    :summary ""
    :args [
           {:name "num-channels"
            :mode :num-outs
            :doc ""}

           {:name "trigger"
            :default 0
            :doc ""}

           {:name "bufnum"
            :default 0
            :doc ""}

           {:name "rate"
            :default 1
            :doc ""}

           {:name "center-pos"
            :default 0
            :doc ""}

           {:name "dur"
            :default 0.1
            :doc ""}

           {:name "pan"
            :default 0
            :doc ""}

           {:name "amp"
            :default 0.1
            :doc ""}

           {:name "att"
            :default 0.5
            :doc ""}

           {:name "dec"
            :default 0.5
            :doc ""}

           {:name "interp"
            :default 4
            :doc ""}]
    :check (num-outs-greater-than 1)
    :rates #{:ar}
    :doc ""}

   {:name "TGrains3"
    :summary ""
    :args [
           {:name "num-channels"
            :mode :num-outs
            :doc ""}

           {:name "trigger"
            :default 0
            :doc ""}

           {:name "bufnum"
            :default 0
            :doc ""}

           {:name "rate"
            :default 1
            :doc ""}

           {:name "center-pos"
            :default 0
            :doc ""}

           {:name "dur"
            :default 0.1
            :doc ""}

           {:name "pan"
            :default 0
            :doc ""}

           {:name "amp"
            :default 0.1
            :doc ""}

           {:name "att"
            :default 0.5
            :doc ""}

           {:name "dec"
            :default 0.5
            :doc ""}

           {:name "interp"
            :default 4
            :doc ""}]
    :check (num-outs-greater-than 1)
    :rates #{:ar}
    :doc ""}

   ;; Phenon : Pattern {
   ;;      var <>a, <>b, <>x, <>y, <>n;
   ;;      *new { |a=1.3, b=0.3, x=0.30501993062401, y=0.20938865431933, n=true|
   ;;              ^super.newCopyArgs(a, b, x, y, n);
   ;;      }
   ;;      embedInStream {|inval|
   ;;              var locala, localb, localx, localy, localn, newx;
   ;;              locala = a.copy; localb = b.copy; localx = x.copy; localy = y.copy;
   ;;              localn = n.copy;
   ;;              loop {
   ;;                      newx = localy + 1 - (locala * localx.squared);
   ;;                      localy = localb * (localx);
   ;;                      localx = newx;
   ;;                      (localn).if(
   ;;                              { ([localx, localy] * [0.77850360953955, 2.5950120317984] + 1 * 0.5).yield },
   ;;                              { [localx, localy].yield }
   ;;                      );
   ;;              };
   ;;              ^inval
   ;;      }
   ;; }

   ;; Platoo : Pattern {
   ;;      var <>a, <>b, <>c, <>d, <>x, <>y, <>n;
   ;;      *new {|a=3.0, b= -2.0, c=0.7, d=0.9, x=0.34082301375036, y= -0.38270086971332, n=true|
   ;;              ^super.newCopyArgs(a, b, c, d, x, y, n);
   ;;      }
   ;;      embedInStream {|inval|
   ;;              var locala, localb, localc, locald, localx, localy, localn, newx;
   ;;              locala=a.copy; localb=b.copy; localc=c.copy; locald=d.copy; localx=x.copy; localy=y.copy;
   ;;              localn=n.copy;
   ;;              loop {
   ;;                      newx=sin(localb*localy)+(localc*sin(localb*localx));
   ;;                      localy=sin(locala*localy)+(locald*sin(locala*localx));
   ;;                      localx=newx;
   ;;                      (localn).if(
   ;;                              { ([localx, localy] * [2.8213276124707, 2.4031871436393] + 1 * 0.5).yield },
   ;;                              { [localx, localy].yield }
   ;;                      )
   ;;              };
   ;;              ^inval
   ;;      }
   ;; }

   ;; Plorenz : Pattern {
   ;;      var <>s, <>r, <>b, <>h, <>x, <>y, <>z, <>h;
   ;;      *new {|s=10, r=28, b=2.66666666667, h=0.01, x=0.090879182417163, y=2.97077458055, z=24.282041054363|
   ;;              ^super.newCopyArgs(s, r, b, h, x, y, z);
   ;;      }
   ;;      embedInStream {|inval|
   ;;              var localx, localy, localz, localh, sigma, rayleigh, ratio, newx, newy;
   ;;              localx=x.copy; localy=y.copy; localz=z.copy; localh=h.copy;
   ;;              sigma=s.copy; rayleigh=r.copy; ratio=b.copy;
   ;;              loop {
   ;;                      newx=localh*sigma*(localy-localx)+localx;
   ;;                      newy=localh*(localx.neg*localz+(rayleigh*localx)-localy)+localy;
   ;;                      localz=localh*(localx*localy-(ratio*localz))+localz;
   ;;                      localx=newx; localy=newy;
   ;;                      ([localx, localy, localz] * [0.048269545768799, 0.035757929840258, 0.019094390581019] + 1 * 0.5).yield
   ;;              }
   ;;              ^inval
   ;;      }
   ;; }

   ;; Pquad : Pattern {
   ;;      var <>a, <>b, <>c, <>x, <>n;
   ;;      *new {|a= -3.741, b=3.741, c=0, x=0.1, n=true|
   ;;              ^super.newCopyArgs(a, b, c, x, n);
   ;;      }
   ;;      embedInStream {|inval|
   ;;              var locala, localb, localc, localx, localn;
   ;;              locala=a.copy; localb=b.copy; localc=c.copy; localx=x.copy; localn=n.copy;
   ;;              loop {
   ;;                      localx=(locala*localx.squared) + (localb*localx) + localc;
   ;;                      (localn).if({ (localx * 1.0693715927735).yield },
   ;;                              {localx.yield}
   ;;                      )
   ;;              }
   ;;              ^inval
   ;;      }
   ;; }

   ;; PlinCong : Pattern {
   ;;      var <>a, <>c, <>m, <>x, <>n;
   ;;      *new {|a=1.1, c=0.1, m=0.5, x=0.0, n=true|
   ;;              ^super.newCopyArgs(a, c, m, x, n);
   ;;      }
   ;;      embedInStream {|inval|
   ;;              var locala, localc, localm, localx, localn;
   ;;              locala=a.copy; localc=c.copy; localm=m.copy; localx=x.copy; localn=n.copy;
   ;;              loop {
   ;;                      localx=((locala * localx) + localc) % localm;
   ;;                      (localn).if({ (localx * 2.0000515599933).yield },
   ;;                              {localx.yield}
   ;;                      )
   ;;              }
   ;;              ^inval
   ;;      }
   ;; }

   ;; Pstandard : Pattern {
   ;;      var <>k, <>x, <>y, <>n;
   ;;      *new {|k=1.5, x=4.9789799812499, y=5.7473416156381, n=true|
   ;;              ^super.newCopyArgs(k, x, y, n)
   ;;      }
   ;;      embedInStream {|inval|
   ;;              var localk, localx, localy, localn;
   ;;              localk=k.copy; localx=x.copy; localy=y.copy; localn=n.copy;
   ;;              loop {
   ;;                      localy=(localk * sin(localx) + localy) % 2pi;
   ;;                      localx=(localx + localy) % 2pi;
   ;;                      (localn).if(
   ;;                              { ([localx, localy] * [0.1591583187703, 0.15915788974082]).yield },
   ;;                              { [localx, localy].yield }
   ;;                      );
   ;;              }
   ;;              ^inval
   ;;      }
   ;; }

   ;; Pgbman : Pattern {
   ;;      var <>x, <>y, <>n;
   ;;      *new {|x=1.2, y=2.1, n=true|
   ;;              ^super.newCopyArgs(x, y, n)
   ;;      }
   ;;      embedInStream {|inval|
   ;;              var localx, localy, localn, last_x;
   ;;              localx=x.copy; localy=y.copy; localn=n.copy;
   ;;              loop {
   ;;                      last_x=localx;
   ;;                      (last_x < 0.0).if({ localx = 1.0 - localy - last_x }, { localx = 1.0 - localy + last_x });
   ;;                      localy = last_x;
   ;;                      (localn).if({ (localx * 0.12788595029832).yield },
   ;;                              {localx.yield}
   ;;                      )
   ;;              }
   ;;              ^inval
   ;;      }
   ;; }

   ;; Pfhn : Pattern {
   ;;      var <>a, <>b, <>c, <>d, <>i, <>u, <>v, <>n;
   ;;      *new {|a=0.7, b=0.8, c=1.0, d=1.0, i, u= -0.1, v=0.1, n=true|
   ;;              ^super.newCopyArgs(a, b, c, d, i, u, v, n)
   ;;      }
   ;;      embedInStream {|inval|
   ;;              var la, lb, lc, ld, li, lu, lv, ln, newu;
   ;;              la=a.copy; lb=b.copy; lc=c.copy; ld=d.copy; li=i.copy; lu=u.copy; lv=v.copy;
   ;;              ln=n.copy;
   ;;              li=li ? Pseq([0.0], inf).asStream;
   ;;              loop {
   ;;                      newu=lc * (lu.cubed * -0.33333333 - lv + lu + li.next) + lu;
   ;;                      lv=ld * (lb * lu + la - lv) + lv;
   ;;                      lu=newu;
   ;;                      if ((lu > 1.0) || (lu < -1.0)) {
   ;;                              lu=((lu - 1)%4.0 - 2.0).abs - 1.0;
   ;;                      };
   ;;                      (ln).if({ [lu + 1 * 0.5, lv * 0.5 + 1 * 0.5].yield },
   ;;                              { [lu, lv].yield }
   ;;                      );
   ;;              }
   ;;              ^inval
   ;;      }
   ;;                 }

   ;; BhobLoShelf {
   ;;      *ar {|in, freq, amp|
   ;;              var wc, a0, allpass;
   ;;              wc=pi * freq * SampleDur.ir;
   ;;              a0=(1 - wc)/(1 + wc);
   ;;              allpass=FOS.ar(in, a0.neg, 1, a0, -1);
   ;;              ^(0.5 * (in + allpass + (amp * (in-allpass))))
   ;;      }
   ;; }

   ;; BhobHiShelf {
   ;;      *ar {|in, freq, amp|
   ;;              var wc, a0, allpass;
   ;;              wc=pi * freq * SampleDur.ir;
   ;;              a0=(1 - wc)/(1 + wc);
   ;;              allpass=FOS.ar(in, a0.neg, 1, a0, 1);
   ;;              ^(0.5 * (in + allpass + (amp * (in-allpass))))
   ;;      }
   ;; }

   ;; BhobTone {
   ;;      *ar {|in, tone|
   ;;              ^Mix([HiShelf.ar(in, 10000, tone), LoShelf.ar(in, 100, tone.reciprocal)])
   ;;      }
   ;;}

   ])

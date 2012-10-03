(ns overtone.sc.machinery.ugen.metadata.extras.bhob
  (:use [overtone.sc.machinery.ugen common check]))


(def specs
  [
   {:name "Henon2DN"
    :summary ""
    :args [{:name "minfreq"
            :default 11025
            :default:kr 40
            :doc ""}

           {:name "maxfreq"
            :default 22050
            :default:kr 100
            :doc ""}

           {:name "a"
            :default 1.4
            :doc ""}

           {:name "b"
            :default 0.3
            :doc ""}

           {:name "x0"
            :default 0.30501993062401
            :doc ""}

           {:name "y0"
            :default 0.20938865431933
            :doc ""}]

    :rates #{:ar :kr}
    :doc ""}

   {:name "Henon2DL"
    :summary ""
    :args [{:name "minfreq"
            :default 11025
            :default:kr 40
            :doc ""}

           {:name "maxfreq"
            :default 22050
            :default:kr 100
            :doc ""}

           {:name "a"
            :default 1.4
            :doc ""}

           {:name "b"
            :default 0.3
            :doc ""}

           {:name "x0"
            :default 0.30501993062401
            :doc ""}

           {:name "y0"
            :default 0.20938865431933
            :doc ""}]

    :rates #{:ar :kr}
    :doc ""}

   {:name "Henon2DC"
    :summary ""
    :args [{:name "minfreq"
            :default 11025
            :default:kr 40
            :doc ""}

           {:name "maxfreq"
            :default 22050
            :default:kr 100
            :doc ""}

           {:name "a"
            :default 1.4
            :doc ""}

           {:name "b"
            :default 0.3
            :doc ""}

           {:name "x0"
            :default 0.30501993062401
            :doc ""}

           {:name "y0"
            :default 0.20938865431933
            :doc ""}]

    :rates #{:ar :kr}
    :doc ""}

   {:name "HenonTrig"
    :summary ""
    :args [{:name "minfreq"
            :default 5
            :doc ""}

           {:name "maxfreq"
            :default 10
            :doc ""}

           {:name "a"
            :default 1.4
            :doc ""}

           {:name "b"
            :default 0.3
            :doc ""}

           {:name "x0"
            :default 0.30501993062401
            :doc ""}

           {:name "y0"
            :default 0.20938865431933
            :doc ""}]

    :rates #{:ar :kr}
    :doc ""}


   {:name "Gbman2DN"
    :summary ""
    :args [{:name "minfreq"
            :default 11025
            :default:kr 40
            :doc ""}

           {:name "maxfreq"
            :default 22050
            :default:kr 100
            :doc ""}

           {:name "x0"
            :default 1.2
            :doc ""}

           {:name "y0"
            :default 2.1
            :doc ""}]

    :rates #{:ar :kr}
    :doc ""}

   {:name "Gbman2DL"
    :summary ""
    :args [{:name "minfreq"
            :default 11025
            :default:kr 40
            :doc ""}

           {:name "maxfreq"
            :default 22050
            :default:kr 100
            :doc ""}

           {:name "x0"
            :default 1.2
            :doc ""}

           {:name "y0"
            :default 2.1
            :doc ""}]

    :rates #{:ar :kr}
    :doc ""}

   {:name "Gbman2DC"
    :summary ""
    :args [{:name "minfreq"
            :default 11025
            :default:kr 40
            :doc ""}

           {:name "maxfreq"
            :default 22050
            :default:kr 100
            :doc ""}

           {:name "x0"
            :default 1.2
            :doc ""}

           {:name "y0"
            :default 2.1
            :doc ""}]

    :rates #{:ar :kr}
    :doc ""}

   {:name "GbmanTrig"
    :summary ""
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
            :doc ""}]

    :rates #{:ar :kr}
    :doc ""}

   {:name "Standard2DN"
    :summary ""
    :args [{:name "minfreq"
            :default 11025
            :default:kr 40
            :doc ""}

           {:name "maxfreq"
            :default 22050
            :default:kr 100
            :doc ""}

           {:name "k"
            :default 1.4
            :doc ""}

           {:name "x0"
            :default 4.9789799812499
            :doc ""}

           {:name "y0"
            :default 5.7473416156381
            :doc ""}]

    :rates #{:ar :kr}
    :doc ""}

   {:name "Standard2DL"
    :summary ""
    :args [{:name "minfreq"
            :default 11025
            :default:kr 40
            :doc ""}

           {:name "maxfreq"
            :default 22050
            :default:kr 100
            :doc ""}

           {:name "k"
            :default 1.4
            :doc ""}

           {:name "x0"
            :default 4.9789799812499
            :doc ""}

           {:name "y0"
            :default 5.7473416156381
            :doc ""}]

    :rates #{:ar :kr}
    :doc ""}

   {:name "Standard2DC"
    :summary ""
    :args [{:name "minfreq"
            :default 11025
            :default:kr 40
            :doc ""}

           {:name "maxfreq"
            :default 22050
            :default:kr 100
            :doc ""}

           {:name "k"
            :default 1.4
            :doc ""}

           {:name "x0"
            :default 4.9789799812499
            :doc ""}

           {:name "y0"
            :default 5.7473416156381
            :doc ""}]

    :rates #{:ar :kr}
    :doc ""}

   {:name "StandardTrig"
    :summary ""
    :args [{:name "minfreq"
            :default 5
            :doc ""}

           {:name "maxfreq"
            :default 10
            :doc ""}

           {:name "k"
            :default 1.4
            :doc ""}

           {:name "x0"
            :default 4.9789799812499
            :doc ""}

           {:name "y0"
            :default 5.7473416156381
            :doc ""}]

    :rates #{:ar :kr}
    :doc ""}


   {:name "Latoocarfian2DN"
    :summary ""
    :args [{:name "minfreq"
            :default 11025
            :default:kr 40
            :doc ""}

           {:name "maxfreq"
            :default 22050
            :default:kr 100
            :doc ""}

           {:name "a"
            :default 1
            :doc ""}

           {:name "b"
            :default 3
            :doc ""}

           {:name "c"
            :default 0.5
            :doc ""}

           {:name "d"
            :default 0.5
            :doc ""}

           {:name "x0"
            :default 0.34082301375036
            :doc ""}

           {:name "y0"
            :default -0.38270086971332
            :doc ""}]

    :rates #{:ar :kr}
    :doc ""}

   {:name "Latoocarfian2DL"
    :summary ""
    :args [{:name "minfreq"
            :default 11025
            :default:kr 40
            :doc ""}

           {:name "maxfreq"
            :default 22050
            :default:kr 100
            :doc ""}

           {:name "a"
            :default 1
            :doc ""}

           {:name "b"
            :default 3
            :doc ""}

           {:name "c"
            :default 0.5
            :doc ""}

           {:name "d"
            :default 0.5
            :doc ""}

           {:name "x0"
            :default 0.34082301375036
            :doc ""}

           {:name "y0"
            :default -0.38270086971332
            :doc ""}]

    :rates #{:ar :kr}
    :doc ""}


   {:name "Latoocarfian2DC"
    :summary ""
    :args [{:name "minfreq"
            :default 11025
            :default:kr 40
            :doc ""}

           {:name "maxfreq"
            :default 22050
            :default:kr 100
            :doc ""}

           {:name "a"
            :default 1
            :doc ""}

           {:name "b"
            :default 3
            :doc ""}

           {:name "c"
            :default 0.5
            :doc ""}

           {:name "d"
            :default 0.5
            :doc ""}

           {:name "x0"
            :default 0.34082301375036
            :doc ""}

           {:name "y0"
            :default -0.38270086971332
            :doc ""}]

    :rates #{:ar :kr}
    :doc ""}

   {:name "LatoocarfianTrig"
    :summary ""
    :args [{:name "minfreq"
            :default 5
            :doc ""}

           {:name "maxfreq"
            :default 10
            :doc ""}

           {:name "a"
            :default 1
            :doc ""}

           {:name "b"
            :default 3
            :doc ""}

           {:name "c"
            :default 0.5
            :doc ""}

           {:name "d"
            :default 0.5
            :doc ""}

           {:name "x0"
            :default 0.34082301375036
            :doc ""}

           {:name "y0"
            :default -0.38270086971332
            :doc ""}]

    :rates #{:ar :kr}
    :doc ""}

   {:name "Lorenz2DN"
    :summary ""
    :args [{:name "minfreq"
            :default 11025
            :default:kr 40
            :doc ""}

           {:name "maxfreq"
            :default 22050
            :default:kr 100
            :doc ""}

           {:name "s"
            :default 10
            :doc ""}

           {:name "r"
            :default 28
            :doc ""}

           {:name "b"
            :default 2.6666667
            :doc ""}

           {:name "h"
            :default 0.02
            :doc ""}

           {:name "x0"
            :default 0.090879182417163
            :doc ""}

           {:name "y0"
            :default 2.97077458055
            :doc ""}

           {:name "z0"
            :default 24.282041054363
            :doc ""}]

    :rates #{:ar :kr}
    :doc ""}

   {:name "Lorenz2DL"
    :summary ""
    :args [{:name "minfreq"
            :default 11025
            :default:kr 40
            :doc ""}

           {:name "maxfreq"
            :default 22050
            :default:kr 100
            :doc ""}

           {:name "s"
            :default 10
            :doc ""}

           {:name "r"
            :default 28
            :doc ""}

           {:name "b"
            :default 2.6666667
            :doc ""}

           {:name "h"
            :default 0.02
            :doc ""}

           {:name "x0"
            :default 0.090879182417163
            :doc ""}

           {:name "y0"
            :default 2.97077458055
            :doc ""}

           {:name "z0"
            :default 24.282041054363
            :doc ""}]

    :rates #{:ar :kr}
    :doc ""}

   {:name "Lorenz2DC"
    :summary ""
    :args [{:name "minfreq"
            :default 11025
            :default:kr 40
            :doc ""}

           {:name "maxfreq"
            :default 22050
            :default:kr 100
            :doc ""}

           {:name "s"
            :default 10
            :doc ""}

           {:name "r"
            :default 28
            :doc ""}

           {:name "b"
            :default 2.6666667
            :doc ""}

           {:name "h"
            :default 0.02
            :doc ""}

           {:name "x0"
            :default 0.090879182417163
            :doc ""}

           {:name "y0"
            :default 2.97077458055
            :doc ""}

           {:name "z0"
            :default 24.282041054363
            :doc ""}]

    :rates #{:ar :kr}
    :doc ""}

   {:name "LorenzTrig"
    :summary ""
    :args [{:name "minfreq"
            :default 11025
            :default:kr 40
            :doc ""}

           {:name "maxfreq"
            :default 22050
            :default:kr 100
            :doc ""}

           {:name "s"
            :default 10
            :doc ""}

           {:name "r"
            :default 28
            :doc ""}

           {:name "b"
            :default 2.6666667
            :doc ""}

           {:name "h"
            :default 0.02
            :doc ""}

           {:name "x0"
            :default 0.090879182417163
            :doc ""}

           {:name "y0"
            :default 2.97077458055
            :doc ""}

           {:name "z0"
            :default 24.282041054363
            :doc ""}]

    :rates #{:ar :kr}
    :doc ""}

   {:name "Fhn2DN"
    :summary ""
    :args [{:name "minfreq"
            :default 11025
            :default:kr 40
            :doc ""}

           {:name "maxfreq"
            :default 22050
            :default:kr 100
            :doc ""}

           {:name "urate"
            :default 0.1
            :doc ""}

           {:name "wrate"
            :default 0.1
            :doc ""}

           {:name "b0"
            :default 0.6
            :doc ""}

           {:name "b1"
            :default 0.8
            :doc ""}

           {:name "i"
            :default 0
            :doc ""}

           {:name "u0"
            :default 0
            :doc ""}

           {:name "w0"
            :default 0
            :doc ""}]

    :rates #{:ar :kr}
    :doc ""}

   {:name "Fhn2DL"
    :summary ""
    :args [{:name "minfreq"
            :default 11025
            :default:kr 40
            :doc ""}

           {:name "maxfreq"
            :default 22050
            :default:kr 100
            :doc ""}

           {:name "urate"
            :default 0.1
            :doc ""}

           {:name "wrate"
            :default 0.1
            :doc ""}

           {:name "b0"
            :default 0.6
            :doc ""}

           {:name "b1"
            :default 0.8
            :doc ""}

           {:name "i"
            :default 0
            :doc ""}

           {:name "u0"
            :default 0
            :doc ""}

           {:name "w0"
            :default 0
            :doc ""}]

    :rates #{:ar :kr}
    :doc ""}

   {:name "Fhn2DC"
    :summary ""
    :args [{:name "minfreq"
            :default 11025
            :default:kr 40
            :doc ""}

           {:name "maxfreq"
            :default 22050
            :default:kr 100
            :doc ""}

           {:name "urate"
            :default 0.1
            :doc ""}

           {:name "wrate"
            :default 0.1
            :doc ""}

           {:name "b0"
            :default 0.6
            :doc ""}

           {:name "b1"
            :default 0.8
            :doc ""}

           {:name "i"
            :default 0
            :doc ""}

           {:name "u0"
            :default 0
            :doc ""}

           {:name "w0"
            :default 0
            :doc ""}]

    :rates #{:ar :kr}
    :doc ""}

   {:name "FhnTrig"
    :summary ""
    :args [{:name "minfreq"
            :default 4
            :doc ""}

           {:name "maxfreq"
            :default 10
            :doc ""}

           {:name "urate"
            :default 0.1
            :doc ""}

           {:name "wrate"
            :default 0.1
            :doc ""}

           {:name "b0"
            :default 0.6
            :doc ""}

           {:name "b1"
            :default 0.8
            :doc ""}

           {:name "i"
            :default 0
            :doc ""}

           {:name "u0"
            :default 0
            :doc ""}

           {:name "w0"
            :default 0
            :doc ""}]

    :rates #{:ar :kr}
    :doc ""}

   {:name "PV_CommonMag"
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
            :default INF
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

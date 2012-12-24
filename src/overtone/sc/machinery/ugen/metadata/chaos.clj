(ns overtone.sc.machinery.ugen.metadata.chaos
  (:use [overtone.sc.machinery.ugen common check]))

;; /*
;; Non-linear Dynamic Sound Generators
;;    Lance Putnam 2004
;; lance@uwalumni.com
;;
;; This is a set of iterative functions and differential equations that
;; are known to exhibit chaotic behavior.  Internal calculations are
;; done with 64-bit words to ensure greater accuracy.
;;
;; The name of the function is followed by one of N, L, or C.  These
;; represent the interpolation method used between function iterations.
;;      N -> None
;;      L -> Linear
;;      C -> Cubic
;; */

;; ChaosGen : UGen {}

(def specs
  (map
   #(assoc %
      :rates #{:ar})
   [

    {:name "QuadN",
     :args [{:name "freq"
             :default 22050.0
             :doc "iteration frequency in Hertz"}

            {:name "a"
             :default 1.0
             :doc "1st coefficient"}

            {:name "b"
             :default -1.0
             :doc "2nd coefficient"}

            {:name "c"
             :default -0.75
             :doc "3rd coefficient"}

            {:name "xi"
             :default 0.0
             :doc "initial value of x"}]

     :doc "a non-interpolating (general quadratic map chaotic) sound
           generator based on the difference equation: xn+1 = axn2 + bxn
           + c "}

    ;; QuadL : QuadN {}

    {:name "QuadL" :extends "QuadN"
     :doc "a linear-interpolating (general quadratic map chaotic) sound
           generator based on the difference equation: xn+1 = axn2 + bxn
           + c"}

    ;; QuadC : QuadN {}

    {:name "QuadC" :extends "QuadN"
     :doc "a cubic-interpolating (general quadratic map chaotic) sound
           generator based on the difference equation: xn+1 = axn2 + bxn
           + c"}

    {:name "CuspN",
     :args [{:name "freq"
             :default 22050.0
             :doc "iteration frequency in Hertz"}

            {:name "a"
             :default 1.0
             :doc "first coefficient"}

            {:name "b"
             :default 1.9
             :doc "2nd coefficient"}

            {:name "xi"
             :default 0.0 :doc "initial value of x"}]

     :doc "a non-interpolating (cusp map chaotic) sound generator based
           on the difference equation: xn+1 = a - b*sqrt(|xn|)"}

    ;; CuspL : CuspN {}

    {:name "CuspL" :extends "CuspN"
     :doc "a linear-interpolating (cusp map chaotic) sound generator
           based on the difference equation: xn+1 = a - b*sqrt(|xn|)"}


    {:name "GbmanN",
     :args [{:name "freq"
             :default 22050.0
             :doc "iteration frequency in Hz"}

            {:name "xi"
             :default 1.2
             :doc "initial value of x"}

            {:name "yi"
             :default 2.1
             :doc "initial value of y"}]

     :doc "A non-interpolating (gingerbreadman map chaotic) sound
          generator based on the difference equations: xn+1 = 1 - yn +
          |xn| yn+1 = xn"}

    ;; GbmanL : GbmanN {}

    {:name "GbmanL" :extends "GbmanN"
     :doc "A linear-interpolating (gingerbreadman map chaotic) sound
           generator based on the difference equations: xn+1 = 1 - yn +
           |xn| yn+1 = xn"}


    {:name "HenonN",
     :args [{:name "freq"
             :default 22050.0
             :doc "iteration frequency in Hertz"}

            {:name "a"
             :default 1.4
             :doc "1st coefficient"}

            {:name "b"
             :default 0.3
             :doc "2nd coefficient"}

            {:name "x0"
             :default 0.0
             :doc "initial value of x"}

            {:name "x1"
             :default 0.0
             :doc "second value of x"}]

     :doc "a non-interpolating (henon map chaotic) sound generator based
           on the difference equation: x[n+2] = 1 - a*(x[n+1]^)2 +
           bx[n]. This equation was discovered by French astronomer
           Michel Hénon while studying the orbits of stars in globular
           clusters." }


    {:name "HenonL" :extends "HenonN"
     :doc "a linear-interpolating (henon map chaotic) sound generator
           based on the difference equation: x[n+2] = 1 - a*(x[n+1]^)2 +
           bx[n]. This equation was discovered by French astronomer
           Michel Hénon while studying the orbits of stars in globular
           clusters." }


    {:name "HenonC" :extends "HenonN"
     :doc "a cubic-interpolating (henon map chaotic) sound generator
           based on the difference equation: x[n+2] = 1 - a*(x[n+1]^)2 +
           bx[n]. This equation was discovered by French astronomer
           Michel Hénon while studying the orbits of stars in globular
           clusters." }

    {:name "LatoocarfianN",
     :args [{:name "freq"
             :default 22050.0
             :doc "iteration frequency in Hertz"}

            {:name "a"
             :default 1.0
             :doc "1st coefficient"}

            {:name "b"
             :default 3.0
             :doc "2nd coefficient"}

            {:name "c"
             :default 0.5
             :doc "3rd coefficient"}

            {:name "d"
             :default 0.5
             :doc "4th coefficient"}

            {:name "xi"
             :default 0.5
             :doc "initial value of x"}

            {:name "yi"
             :default 0.5
             :doc "initial value of y"}]

     :doc "a non-interpolating (latoocarfian chaotic) sound
           generator. Parameters a and b should be in the range from -3
           to +3, and parameters c and d should be in the range from 0.5
           to 1.5. The function can, depending on the parameters given,
           give continuous chaotic output, converge to a single
           value (silence) or oscillate in a cycle (tone)." }


    {:name "LatoocarfianL" :extends "LatoocarfianN"
     :doc "a linear-interpolating (latoocarfian chaotic) sound
           generator. Parameters a and b should be in the range from -3
           to +3, and parameters c and d should be in the range from 0.5
           to 1.5. The function can, depending on the parameters given,
           give continuous chaotic output, converge to a single
           value (silence) or oscillate in a cycle (tone)." }


    {:name "LatoocarfianC" :extends "LatoocarfianN"
     :doc "a cubic-interpolating (latoocarfian chaotic) sound
           generator. Parameters a and b should be in the range from -3
           to +3, and parameters c and d should be in the range from 0.5
           to 1.5. The function can, depending on the parameters given,
           give continuous chaotic output, converge to a single
           value (silence) or oscillate in a cycle (tone)." }


    {:name "LinCongN",
     :args [{:name "freq"
             :default 22050.0
             :doc "iteration frequency in Hertz."}

            {:name "a"
             :default 1.1
             :doc "multiplier amount"}

            {:name "c"
             :default 0.13
             :doc "increment amount"}

            {:name "m"
             :default 1.0
             :doc "modulus amount"}

            {:name "xi"
             :default 0.0
             :doc "initial value of x"}]

     :doc "a non-interpolating (linear congruential chaotic) sound
           generator. The output signal is automatically scaled to a
           range of [-1, 1]." }


    {:name "LinCongL" :extends "LinCongN"
     :doc "a linear-interpolating (linear congruential chaotic) sound
           generator. The output signal is automatically scaled to a
           range of [-1, 1]." }


    {:name "LinCongC" :extends "LinCongN"
     :doc "a cubic-interpolating (linear congruential chaotic) sound
           generator. The output signal is automatically scaled to a
           range of [-1, 1]." }


    {:name "StandardN",
     :args [{:name "freq"
             :default 22050.0
             :doc "iteration frequency in Hertz"}

            {:name "k"
             :default 1.0
             :doc "perturbation amount"}

            {:name "xi"
             :default 0.5
             :doc "initial value of x"}

            {:name "yi"
             :default 0.0
             :doc "initial value of y"}]

     :doc "standard map chaotic generator. The standard map is an area
           preserving map of a cylinder discovered by the plasma
           physicist Boris Chirikov." }


    {:name "StandardL" :extends "StandardN"
     :doc "linear-interpolating standard map chaotic generator. The
           standard map is an area preserving map of a cylinder
           discovered by the plasma physicist Boris Chirikov." }


    {:name "FBSineN",
     :args [{:name "freq"
             :default 22050.0
             :doc "iteration frequency in Hertz"}

            {:name "im"
             :default 1.0
             :doc "index multiplier amount"}

            {:name "fb"
             :default 0.1
             :doc "feedback amount"}

            {:name "a"
             :default 1.1
             :doc "phase multiplier amount"}

            {:name "c"
             :default 0.5
             :doc "phase increment amount"}

            {:name "xi"
             :default 0.1
             :doc "initial value of x"}

            {:name "yi"
             :default 0.1
             :doc "initial value of y"}]

     :doc "a non-interpolating feedback sine with chaotic phase indexing
           sound generator. This uses a linear congruential function to
           drive the phase indexing of a sine wave.  For im = 1, fb = 0,
           and a = 1 a normal sinewave results." }


    {:name "FBSineL" :extends "FBSineN"
     :doc "a linear-interpolating feedback sine with chaotic phase
           indexing sound generator. This uses a linear congruential
           function to drive the phase indexing of a sine wave.  For im
           = 1, fb = 0, and a = 1 a normal sinewave results." }

    {:name "FBSineC" :extends "FBSineN"
     :doc "a cubic-interpolating feedback sine with chaotic phase
           indexing sound generator. This uses a linear congruential
           function to drive the phase indexing of a sine wave.  For im
           = 1, fb = 0, and a = 1 a normal sinewave results." }

    {:name "LorenzL",
     :args [{:name "freq"
             :default 22050.0
             :doc "iteration frequency in Hertz"}

            {:name "s"
             :default 10.0
             :doc "1st variable"}

            {:name "r"
             :default 28.0
             :doc "2nd variable"}

            {:name "b"
             :default 2.667
             :doc "3rd variable"}

            {:name "h"
             :default 0.05
             :doc "integration time stamp"}

            {:name "xi"
             :default 0.1
             :doc "initial value of x"}

            {:name "yi"
             :default 0.0
             :doc "initial value of y"}

            {:name "zi"
             :default 0.0
             :doc "initial value of z"}]

     :doc "lorenz chaotic generator. A strange attractor discovered by
           Edward N. Lorenz while studying mathematical models of the
           atmosphere. The time step amount h determines the rate at
           which the ODE is evaluated.  Higher values will increase the
           rate, but cause more instability.  A safe choice is the
           default amount of 0.05." }]))

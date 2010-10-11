(ns overtone.sc.ugen.random)

(def specs
     [

      ;; TODO: discover why this is commented out and either uncomment or remove
      ;; Rand : UGen {
      ;; 	// uniform distribution
      ;; 	*new { arg lo = 0.0, hi = 1.0;
      ;; 		^this.multiNew('scalar', lo, hi)
      ;; 	}
      ;; }

      ;;{:name "Rand"
      ;; :args [{:name "lo", :default 0.0}
      ;;        {:name "hi", :default 1.0}]
      ;; :rates #{:ir}}

      {:name "RandSeed"
       :args [{:name "trig", :default 0.0}
              {:name "seed", :default 56789}]
       :rates #{:ir :kr}
       :fixed-outs 0
       :doc "When the trigger signal changes from nonpositive to positve, the synth's random generator seed is reset to the given value. All synths that use the same random number generator reproduce the same sequence of numbers again."}


      {:name "RandID"
       :args [{:name "seed", :default 0}]
       :rates #{:ir :kr}
       :fixed-outs 0
       :doc "Choose which random number generator to use for this synth. All synths that use the same generator reproduce the same sequence of numbers when the same seed is set again."}


      {:name "IRand"
       :args [{:name "lo", :default 0.0}
              {:name "hi", :default 127.0}]
       :rates #{:ir}
       :doc "Generates a single random integer value in uniform distribution from lo to hi"}


      {:name "TRand"
       :args [{:name "lo", :default 0.0}
              {:name "hi", :default 1.0}
              {:name "trig", :default 0.0}]
       :rates #{:kr :ar}
       :doc "Generates a random float value in uniform distribution from lo to hi each time the trig signal changes from nonpositive to positive values"}


      {:name "TIRand"
       :args [{:name "lo", :default 0}
              {:name "hi", :default 127}
              {:name "trig", :default 0.0}]
       :rates #{:kr :ar}
       :doc "Generates a random integer value in uniform distribution from lo to hi each time the trig signal changes from nonpositive to positive values"}

      ;; TODO: discover why this is commented out and either uncomment or remove
      ;; LinRand : UGen {
      ;; 	// linear distribution
      ;; 	// if minmax <= 0 then skewed towards lo.
      ;; 	// else skewed towards hi.
      ;; 	*new { arg lo = 0.0, hi = 1.0, minmax = 0;
      ;; 		^this.multiNew('scalar', lo, hi, minmax)
      ;; 	}
      ;; }


      {:name "NRand"
       :args [{:name "lo", :default 0.0}
              {:name "hi", :default 1.0}
              {:name "n", :default 0}]
       :rates #{:ir}
       :doc "Generates a single random float value in a sum of n uniform distributions from lo to hi.

n = 1 : uniform distribution - same as Rand
n = 2 : triangular distribution
n = 3 : smooth hump
as n increases, distribution converges towards gaussian"}


      {:name "ExpRand"
       :args [{:name "lo", :default 0.01}
              {:name "hi", :default 1.0}]
       :rates #{:ir}
       :doc "Generates a single random float value in an exponential distributions from lo to hi."}



      {:name "TExpRand"
       :args [{:name "lo", :default 0.01}
              {:name "hi", :default 1.0}
              {:name "trig", :default 0.0}]
       :rates #{:ar :kr}
       :doc "Generates a random float value in exponential distribution from lo to hi each time the trig signal changes from nonpositive to positive values lo and hi must both have the same sign and be non-zero."}


      {:name "CoinGate"
       :args [{:name "prob"}
              {:name "trig"}]
       :rates #{:kr :ir}
       :doc "When it receives a trigger, it tosses a coin, and either passes the trigger or doesn't."}


      {:name "LinRand"
       :args [{:name "lo", :default 0.0}
              {:name "hi", :default 1.0}
              {:name "minmax", :default 0}]
       :rates #{:ir}
       :doc "Generates a single random float value in linear distribution from lo to hi, skewed towards lo if minmax < 0, otherwise skewed towards hi."}])

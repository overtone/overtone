(ns overtone.core.ugens.noise)

;; 	Noise Generators

;; 	WhiteNoise.ar(mul, add)
;; 	BrownNoise.ar(mul, add)
;; 	PinkNoise.ar(mul, add)
;; 	Crackle.ar(chaosParam, mul, add)
;; 	LFNoise0.ar(freq, mul, add)
;; 	LFNoise1.ar(freq, mul, add)
;; 	LFNoise2.ar(freq, mul, add)
;; 	Dust.ar(density, mul, add)
;; 	Dust2.ar(density, mul, add)

;; 	White, Brown, Pink generators have no modulatable parameters
;; 	other than multiply and add inputs.

;; 	The chaos param for ChaosNoise should be from 1.0 to 2.0

(def specs
     [

      ;; RandSeed : UGen {
      ;; 	*kr { arg trig = 0.0, seed=56789;
      ;; 		this.multiNew('control', trig, seed)
      ;; 		^0.0		// RandSeed has no output
      ;; 	}
      ;; 	*ir { arg trig = 0.0, seed=56789;
      ;; 		this.multiNew('scalar', trig, seed)
      ;; 		^0.0		// RandSeed has no output
      ;; 	}
      ;; }

      {:name "RandSeed",
       :args [{:name "trig", :default 0.0}
              {:name "seed", :default 56789}],
       :rates #{:ir :kr},
       :fixed-outs 0}
      
      ;; RandID : UGen {
      ;; 	// choose which random number generator to use for this synth .
      ;; 	*kr { arg id=0;
      ;; 		this.multiNew('control', id)
      ;; 		^0.0		// RandID has no output
      ;; 	}
      ;; 	*ir { arg id=0;
      ;; 		this.multiNew('scalar', id)
      ;; 		^0.0		// RandID has no output
      ;; 	}
      ;; }

      {:name "RandID",
       :args [{:name "id", :default 0}],
       :rates #{:ir :kr},
       :fixed-outs 0}

      ;; Rand : UGen {
      ;; 	// uniform distribution
      ;; 	*new { arg lo = 0.0, hi = 1.0;
      ;; 		^this.multiNew('scalar', lo, hi)
      ;; 	}
      ;; }

      {:name "Rand",
       :args [{:name "lo", :default 0.0}
              {:name "hi", :default 1.0}],
       :rates #{:ir}}

      ;; IRand : UGen {
      ;; 	// uniform distribution of integers
      ;; 	*new { arg lo = 0, hi = 127;
      ;; 		^this.multiNew('scalar', lo, hi)
      ;; 	}
      ;; }

      {:name "IRand",
       :args [{:name "lo", :default 0.0}
              {:name "hi", :default 127.0}],
       :rates #{:ir}}

      ;; TRand : UGen {
      ;; 	// uniform distribution
      ;; 	*ar { arg lo = 0.0, hi = 1.0, trig = 0.0;
      ;; 		^this.multiNew('audio', lo, hi, trig)
      ;; 	}
      ;; 	*kr { arg lo = 0.0, hi = 1.0, trig = 0.0;
      ;; 		^this.multiNew('control', lo, hi, trig)
      ;; 	}
      ;; }

      {:name "TRand",
       :args [{:name "lo", :default 0.0}
              {:name "hi", :default 1.0}
              {:name "trig", :default 0.0}]}
      
      ;; TIRand : UGen {
      ;; 	// uniform distribution of integers
      ;; 	*kr { arg lo = 0, hi = 127, trig = 0.0;
      ;; 		^this.multiNew('control', lo, hi, trig)
      ;; 	}
      ;;        *ar { arg lo = 0, hi = 127, trig = 0.0;
      ;;            ^this.multiNew('audio', lo, hi, trig)	}
      ;; }

      {:name "TIRand",
       :args [{:name "lo", :default 0}
              {:name "hi", :default 127}
              {:name "trig", :default 0.0}]}

      ;; LinRand : UGen {
      ;; 	// linear distribution
      ;; 	// if minmax <= 0 then skewed towards lo.
      ;; 	// else skewed towards hi.
      ;; 	*new { arg lo = 0.0, hi = 1.0, minmax = 0;
      ;; 		^this.multiNew('scalar', lo, hi, minmax)
      ;; 	}
      ;; }

      {:name "LinRand",
       :args [{:name "lo", :default 0.0}
              {:name "hi", :default 1.0}
              {:name "minmax", :default 0}],
       :rates #{:ir}}

      ;; NRand : UGen {
      ;; 	// sum of N uniform distributions.
      ;; 	// n = 1 : uniform distribution - same as Rand
      ;; 	// n = 2 : triangular distribution
      ;; 	// n = 3 : smooth hump
      ;; 	// as n increases, distribution converges towards gaussian
      ;; 	*new { arg lo = 0.0, hi = 1.0, n = 0;
      ;; 		^this.multiNew('scalar', lo, hi, n)
      ;; 	}
      ;; }

      {:name "NRand",
       :args [{:name "lo", :default 0.0}
              {:name "hi", :default 1.0}
              {:name "n", :default 0}],
       :rates #{:ir}}
      
      ;; ExpRand : UGen {
      ;; 	// exponential distribution
      ;; 	*new { arg lo = 0.01, hi = 1.0;
      ;; 		^this.multiNew('scalar', lo, hi)
      ;; 	}
      ;; }

      {:name "ExpRand",
       :args [{:name "lo", :default 0.01}
              {:name "hi", :default 1.0}],
       :rates #{:ir}}

      ;; TExpRand : UGen {
      ;; 	// uniform distribution
      ;; 	*ar { arg lo = 0.01, hi = 1.0, trig = 0.0;
      ;; 		^this.multiNew('audio', lo, hi, trig)
      ;; 	}
      ;; 	*kr { arg lo = 0.01, hi = 1.0, trig = 0.0;
      ;; 		^this.multiNew('control', lo, hi, trig)
      ;; 	}
      ;; }

      {:name "TExpRand",
       :args [{:name "lo", :default 0.01}
              {:name "hi", :default 1.0}
              {:name "trig", :default 0.0}]}
      
      ;; CoinGate : UGen {
      ;; 	*ar { arg prob, in;
      ;; 		^this.multiNew('audio', prob, in)
      ;; 	}
      ;; 	*kr { arg prob, in;
      ;; 		^this.multiNew('control', prob, in)
      ;; 	}
      ;; }

      {:name "CoinGate",
       :args [{:name "prob"}
              {:name "in"}]}
      
      ;; TWindex : UGen {
      ;; 	*ar {
      ;; 		arg in, array, normalize=0;
      ;; 		^this.multiNewList(['audio', in, normalize] ++ array)
      ;; 	}
      ;; 	*kr {
      ;; 		arg in, array, normalize=0;
      ;; 		^this.multiNewList(['control', in, normalize] ++ array)
      ;; 	}
      ;; }

      {:name "TWindex",
       :args [{:name "in"}
              {:name "array", :array true}
              {:name "normalize", :default 0}]}
      
      ;; WhiteNoise : UGen {
      
      ;; 	*ar { arg mul = 1.0, add = 0.0;
      ;; 		// support this idiom from SC2.
      ;; 		if (mul.isArray, {
      ;; 			^{ this.multiNew('audio') }.dup(mul.size).madd(mul, add)
      ;; 		},{
      ;; 			^this.multiNew('audio').madd(mul, add)
      ;; 		});
      ;; 	}
      ;; 	*kr { arg mul = 1.0, add = 0.0;
      ;; 		if (mul.isArray, {
      ;; 			^{ this.multiNew('control') }.dup(mul.size).madd(mul, add)
      ;; 		},{
      ;; 			^this.multiNew('control').madd(mul, add)
      ;; 		});
      ;; 	}
      
      ;; }

      {:name "WhiteNoise",
       :args []
       :muladd true}
      
      ;; BrownNoise : WhiteNoise {}

      {:name "BrownNoise",
       :args []
       :muladd true}

      ;; PinkNoise : WhiteNoise {}

      {:name "PinkNoise",
       :args []
       :muladd true}

      ;; ClipNoise : WhiteNoise {}

      {:name "ClipNoise",
       :args []
       :muladd true}

      ;; GrayNoise : WhiteNoise {}

      {:name "GrayNoise",
       :args []
       :muladd true}


      ;; //NoahNoise : WhiteNoise {}
      ;; TODO is this ugen still supported?

      {:name "NoahNoise",
       :args []
       :muladd true}

      ;; Crackle : UGen {
      
      ;; 	*ar { arg chaosParam=1.5, mul = 1.0, add = 0.0;
      ;; 		^this.multiNew('audio', chaosParam).madd(mul, add)
      ;; 	}
      ;; 	*kr { arg chaosParam=1.5, mul = 1.0, add = 0.0;
      ;; 		^this.multiNew('control', chaosParam).madd(mul, add)
      ;; 	}
      ;; }

      {:name "Crackle",
       :args [{:name "chaosParam", :default 1.5}]
       :muladd true}
      
      ;; Logistic : UGen {
      
      ;; 	*ar { arg chaosParam=3.0, freq = 1000.0, init= 0.5, mul = 1.0, add = 0.0;
      ;; 		^this.multiNew('audio', chaosParam, freq, init).madd(mul, add)
      ;; 	}
      ;; 	*kr { arg chaosParam=3.0, freq = 1000.0, init=0.5, mul = 1.0, add = 0.0;
      ;; 		^this.multiNew('control', chaosParam, freq, init).madd(mul, add)
      ;; 	}
      ;; }

      {:name "Logistic",
       :args [{:name "chaosParam", :default 3.0}
              {:name "freq", :default 1000.0}
              {:name "init", :default 0.5}]
       :muladd true}

      ;; LFNoise0 : UGen {
      
      ;; 	*ar { arg freq=500.0, mul = 1.0, add = 0.0;
      ;; 		^this.multiNew('audio', freq).madd(mul, add)
      ;; 	}
      ;; 	*kr { arg freq=500.0, mul = 1.0, add = 0.0;
      ;; 		^this.multiNew('control', freq).madd(mul, add)
      ;; 	}
      ;; }

      {:name "LFNoise0",
       :args [{:name "freq", :default 500.0}]
       :muladd true}
      
      ;; LFNoise1 : LFNoise0 {}

      {:name "LFNoise1",
       :args [{:name "freq", :default 500.0}]
       :muladd true}

      ;; LFNoise2 : LFNoise0 {}

      {:name "LFNoise2",
       :args [{:name "freq", :default 500.0}]
       :muladd true}

      ;; LFClipNoise : LFNoise0 {}

      {:name "LFClipNoise",
       :args [{:name "freq", :default 500.0}]
       :muladd true}

      ;; LFDNoise0 : LFNoise0 {}

      {:name "LFDNoise0",
       :args [{:name "freq", :default 500.0}]
       :muladd true}

      ;; LFDNoise1 : LFNoise0 {}

      {:name "LFDNoise1",
       :args [{:name "freq", :default 500.0}]
       :muladd true}

      ;; LFDNoise3 : LFNoise0 {}

      {:name "LFDNoise3",
       :args [{:name "freq", :default 500.0}]
       :muladd true}

      ;; LFDClipNoise : LFNoise0 {}

      {:name "LFDClipNoise",
       :args [{:name "freq", :default 500.0}]
       :muladd true}

      ;; Hasher : UGen {
      ;; 	*ar { arg in = 0.0, mul = 1.0, add = 0.0;
      ;; 		^this.multiNew('audio', in).madd(mul, add)
      ;; 	}
      ;; 	*kr { arg in = 0.0, mul = 1.0, add = 0.0;
      ;; 		^this.multiNew('control', in).madd(mul, add)
      ;; 	}
      ;; }

      {:name "Hasher",
       :args [{:name "in", :default 0.0}]
       :muladd true}
      
      ;; MantissaMask : UGen {
      ;; 	*ar { arg in = 0.0, bits=3, mul = 1.0, add = 0.0;
      ;; 		^this.multiNew('audio', in, bits).madd(mul, add)
      ;; 	}
      ;; 	*kr { arg in = 0.0, bits=3, mul = 1.0, add = 0.0;
      ;; 		^this.multiNew('control', in, bits).madd(mul, add)
      ;; 	}
      ;; }

      {:name "MantissaMask",
       :args [{:name "in", :default 0.0}
              {:name "bits", :default 3}]
       :muladd true}

      ;; Dust : UGen {
      
      ;; 	*ar { arg density = 0.0, mul = 1.0, add = 0.0;
      ;; 		^this.multiNew('audio', density).madd(mul, add)
      ;; 	}
      ;; 	*kr { arg density = 0.0, mul = 1.0, add = 0.0;
      ;; 		^this.multiNew('control', density).madd(mul, add)
      ;; 	}
      ;; 	signalRange { ^\unipolar }
      ;; }

      {:name "Dust",
       :args [{:name "density", :default 0.0}]
       :muladd true}

      ;; Dust2 : UGen {
      ;; 	*ar { arg density = 0.0, mul = 1.0, add = 0.0;
      ;; 		^this.multiNew('audio', density).madd(mul, add)
      ;; 	}
      ;; 	*kr { arg density = 0.0, mul = 1.0, add = 0.0;
      ;; 		^this.multiNew('control', density).madd(mul, add)
      ;; 	}
      ;; }

      {:name "Dust2",
       :args [{:name "density", :default 0.0}]
       :muladd true}])


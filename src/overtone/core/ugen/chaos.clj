(ns overtone.core.ugen.chaos)

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
;; 	N -> None
;; 	L -> Linear
;; 	C -> Cubic
;; */

;; ChaosGen : UGen {}

(def specs
     (map
      #(assoc %
              :muladd true
              :rates #{:ar})
      [
       ;; // General Quadratic Map
       ;; QuadN : ChaosGen {
       ;; 	const <equation="x1 = a*x0^2 + b*x0 + c";
       ;; 	*ar { arg freq=22050, a=1, b= -1, c= -0.75, xi=0, mul=1, add=0;
       ;; 		^this.multiNew('audio', freq, a, b, c, xi).madd(mul, add)
       ;; 	}
       ;; }
       
       {:name "QuadN",
        :args [{:name "freq", :default 22050.0}
               {:name "a", :default 1.0}
               {:name "b", :default -1.0}
               {:name "c", :default -0.75}
               {:name "xi", :default 0.0}]
        :doc "a non-interpolating sound generator based on the difference equation: xn+1 = axn2 + bxn + c "}
       
       ;; QuadL : QuadN {}
       
       {:name "QuadL" :extends "QuadN"
        :doc "a linear-interpolating sound generator based on the difference equation: xn+1 = axn2 + bxn + c"}
       
       ;; QuadC : QuadN {}
       
       {:name "QuadC", :extends "QuadN"
        :doc "a cubic-interpolating sound generator based on the difference equation: xn+1 = axn2 + bxn + c"}
       
       ;; // Cusp Map
       ;; CuspN : ChaosGen {
       ;; 	const <equation="x1 = a - b*sqrt(|x0|)";
       ;; 	*ar { arg freq=22050, a=1, b=1.9, xi=0, mul=1, add=0;
       ;; 		^this.multiNew('audio', freq, a, b, xi).madd(mul, add)
       ;; 	}
       ;; }

       {:name "CuspN",
        :args [{:name "freq", :default 22050.0}
               {:name "a", :default 1.0}
               {:name "b", :default 1.9}
               {:name "xi", :default 0.0}]
        :doc "a non-interpolating sound generator based on the difference equation: xn+1 = a - b*sqrt(|xn|)"}
       
       ;; CuspL : CuspN {}
       
       {:name "CuspL" :extends "CuspN"
        :doc "a linear-interpolating sound generator based on the difference equation: xn+1 = a - b*sqrt(|xn|)"}
       
       ;; // Gingerbreadman Map
       ;; GbmanN : ChaosGen {
       ;; 	const <equation="x1 = 1 - y0 + |x0|\ny1 = x0";
       ;; 	*ar { arg freq=22050, xi=1.2, yi=2.1, mul=1, add=0;
       ;; 		^this.multiNew('audio', freq, xi, yi).madd(mul, add)
       ;; 	}
       ;; }

       {:name "GbmanN",
        :args [{:name "freq", :default 22050.0}
               {:name "xi", :default 1.2}
               {:name "yi", :default 2.1}]
        :doc "A non-interpolating sound generator based on the difference equations:
xn+1 = 1 - yn + |xn|
yn+1 = xn"}
       
       ;; GbmanL : GbmanN {}

       {:name "GbmanL" :extends "GbmanN"
        :doc "A linear-interpolating sound generator based on the difference equations:
xn+1 = 1 - yn + |xn|
yn+1 = xn"}
       
       ;; // Henon Map
       ;; HenonN : ChaosGen {
       ;; 	const <equation="x2 = 1 - a*(x1^2) + b*x0";
       ;; 	*ar { arg freq=22050, a=1.4, b=0.3, x0=0, x1=0, mul=1.0, add=0.0;
       ;; 		^this.multiNew('audio', freq, a, b, x0, x1).madd(mul, add)
       ;; 	}
       ;; }

       {:name "HenonN",
        :args [{:name "freq", :default 22050.0}
               {:name "a", :default 1.4}
               {:name "b", :default 0.3}
               {:name "x0", :default 0.0}
               {:name "x1", :default 0.0}]
        :doc "a non-interpolating sound generator based on the difference equation:
x[n+2] = 1 - a*(x[n+1]^)2 + bx[n]"}
       
       ;; HenonL : HenonN {}

       {:name "HenonL" :extends "HenonN"
        :doc "a linear-interpolating sound generator based on the difference equation:
x[n+2] = 1 - a*(x[n+1]^)2 + bx[n]"}
       
       ;; HenonC : HenonN {}

       {:name "HenonC" :extends "HenonN"
        :doc "a cubic-interpolating sound generator based on the difference equation:
x[n+2] = 1 - a*(x[n+1]^)2 + bx[n]"}
       
       ;; // Latoocarfian
       ;; LatoocarfianN : ChaosGen {
       ;; 	const <equation="x1 = sin(b*y0) + c*sin(b*x0)\ny1 = sin(a*x0) + d*sin(a*y0)";
       ;; 	*ar { arg freq=22050, a=1, b=3, c=0.5, d=0.5, xi=0.5, yi=0.5, mul=1.0, add=0.0;
       ;; 		^this.multiNew('audio', freq, a, b, c, d, xi, yi).madd(mul, add)
       ;; 	}
       ;; }

       {:name "LatoocarfianN",
        :args [{:name "freq", :default 22050.0}
               {:name "a", :default 1.0}
               {:name "b", :default 3.0}
               {:name "c", :default 0.5}
               {:name "d", :default 0.5}
               {:name "xi", :default 0.5}
               {:name "yi", :default 0.5}]
        :doc "a non-interpolating latoocarfian chaotic sound generator"}
       
       ;; LatoocarfianL : LatoocarfianN {}

       {:name "LatoocarfianL" :extends "LatoocarfianN"
        :doc "a linear-interpolating latoocarfian chaotic sound generator"}
       
       ;; LatoocarfianC : LatoocarfianN {}

       {:name "LatoocarfianC" :extends "LatoocarfianN"
        :doc "a cubic-interpolating latoocarfian chaotic sound generator"}
       
       ;; // Linear Congruential
       ;; LinCongN : ChaosGen {
       ;; 	const <equation="x1 = ((a * x0) + c) % m";
       ;; 	*ar { arg freq=22050, a=1.1, c=0.13, m=1.0, xi=0, mul=1.0, add=0.0;
       ;; 		^this.multiNew('audio', freq, a, c, m, xi).madd(mul, add)
       ;; 	}
       ;; }

       {:name "LinCongN",
        :args [{:name "freq", :default 22050.0}
               {:name "a", :default 1.1}
               {:name "c", :default 0.13}
               {:name "m", :default 1.0}
               {:name "xi", :default 0.0}]
        :doc "a non-interpolating linear congruential chaotic sound generator"}
       
       ;; LinCongL : LinCongN {}

       {:name "LinCongL" :extends "LinCongN"
        :doc "a linear-interpolating linear congruential chaotic sound generator"}
       
       ;; LinCongC : LinCongN {}

       {:name "LinCongC" :extends "LinCongN"
        :doc "a cubic-interpolating linear congruential chaotic sound generator"}
       
       ;; // Standard Map
       ;; StandardN : ChaosGen {
       ;; 	const <equation="x1 = (x0 + y1) % 2pi\ny1 = (y0 + k*sin(x0)) % 2pi";
       ;; 	*ar { arg freq=22050, k=1.0, xi=0.5, yi=0, mul=1.0, add=0.0;
       ;; 		^this.multiNew('audio', freq, k, xi, yi).madd(mul, add)
       ;; 	}
       ;; }

       {:name "StandardN",
        :args [{:name "freq", :default 22050.0}
               {:name "k", :default 1.0}
               {:name "xi", :default 0.5}
               {:name "yi", :default 0.0}]
        :doc "standard map chaotic generator"}
       
       ;; StandardL : StandardN {}

       {:name "StandardL" :extends "StandardN"
        :doc "linear-interpolating standard map chaotic generator"}
       
       ;; // Feedback Sine with Linear Congruential Phase Indexing
       ;; FBSineN : ChaosGen {
       ;; 	const <equation="x1 = sin(im*y0 + fb*x0)\ny1 = (a*y0 + c) % 2pi";
       ;; 	*ar { arg freq=22050, im=1, fb=0.1, a=1.1, c=0.5, xi=0.1, yi=0.1, mul=1, add=0;
       ;; 		^this.multiNew('audio',freq,im,fb,a,c,xi,yi).madd(mul, add)
       ;; 	}
       ;; }

       {:name "FBSineN",
        :args [{:name "freq", :default 22050.0}
               {:name "im", :default 1.0}
               {:name "fb", :default 0.1}
               {:name "a", :default 1.1}
               {:name "c", :default 0.5}
               {:name "xi", :default 0.1}
               {:name "yi", :default 0.1}]
        :doc "a non-interpolating feedback sine with chaotic phase indexing sound generator"}
       
       ;; FBSineL : FBSineN {}

       {:name "FBSineL" :extends "FBSineN"
        :doc "a linear-interpolating feedback sine with chaotic phase indexing sound generator"}
       
       ;; FBSineC : FBSineN {}
       {:name "FBSineC" :extends "FBSineN"
        :doc "a cubic-interpolating feedback sine with chaotic phase indexing sound generator"}
       
       ;; // ODEs
       ;; // 'h' is integration time-step

       ;; // Lorenz Attractor
       ;; LorenzL : ChaosGen {
       ;; 	const <equation="x' = s*(y-x)\ny' = x*(r-z)-y\nz' = x*y - b*z";
       ;; 	*ar { arg freq=22050, s=10, r=28, b=2.667, h=0.05, xi=0.1, yi=0, zi=0, mul=1.0, add=0.0;
       ;; 		^this.multiNew('audio', freq, s, r, b, h, xi, yi, zi).madd(mul, add)
       ;; 	}	
       ;; }

       {:name "LorenzL",
        :args [{:name "freq", :default 22050.0}
               {:name "s", :default 10.0}
               {:name "r", :default 28.0}
               {:name "b", :default 2.667}
               {:name "h", :default 0.05}
               {:name "xi", :default 0.1}
               {:name "yi", :default 0.0}
               {:name "zi", :default 0.0}]
        :doc "lorenz chaotic generator"}]))

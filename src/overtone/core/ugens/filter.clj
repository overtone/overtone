
(ns overtone.core.ugens.filter)

;; Filter : UGen {
;;  	checkInputs { ^this.checkSameRateAsFirstInput }
;; }      

(def muladd-specs 
  [
   ;; Resonz : Filter {
   ;; 	*ar { arg in = 0.0, freq = 440.0, bwr = 1.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('audio', in, freq, bwr).madd(mul, add)
   ;; 	}
   ;; 	*kr { arg in = 0.0, freq = 440.0, bwr = 1.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('control', in, freq, bwr).madd(mul, add)
   ;; 	}
   ;; }

   {:name "Resonz",
    :args [{:name "in", :default 0.0}
           {:name "freq", :default 440.0}
           {:name "bwr", :default 1.0}]}
   
   ;; OnePole : Filter {
   ;; 	*ar { arg in = 0.0, coef = 0.5, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('audio', in, coef).madd(mul, add)
   ;; 	}
   ;; 	*kr { arg in = 0.0, coef = 0.5, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('control', in, coef).madd(mul, add)
   ;; 	}
   ;; }

   {:name "OnePole",
    :args [{:name "in", :default 0.0}
           {:name "coef", :default 0.5}]
    }
   
   ;; OneZero : OnePole {}

   {:name "OneZero",
    :args [{:name "in", :default 0.0}
           {:name "coef", :default 0.5}]}
   
   ;; TwoPole : Filter {
   
   ;; 	*ar { arg in = 0.0, freq = 440.0, radius = 0.8, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('audio', in, freq, radius).madd(mul, add)
   ;; 	}
   ;; 	*kr { arg in = 0.0, freq = 440.0, radius = 0.8, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('control', in, freq, radius).madd(mul, add)
   ;; 	}
   ;; }

   {:name "TwoPole",
    :args [{:name "in", :default 0.0}
           {:name "freq", :default 440.0}
           {:name "radius", :default 0.8}]}
   
   ;; TwoZero : TwoPole {}
   {:name "TwoZero",
    :args [{:name "in", :default 0.0}
           {:name "freq", :default 440.0}
           {:name "radius", :default 0.8}]}
   
   ;; APF : TwoPole {}
   {:name "APF",
    :args [{:name "in", :default 0.0}
           {:name "freq", :default 440.0}
           {:name "radius", :default 0.8}]}
   
   ;; Integrator : Filter {
   ;; 	*ar { arg in = 0.0, coef = 1.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('audio', in, coef).madd(mul, add)
   ;; 	}
   ;; 	*kr { arg in = 0.0, coef = 1.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('control', in, coef).madd(mul, add)
   ;; 	}
   ;; }
   
   {:name "Integrator",
    :args [{:name "in", :default 0.0}
           {:name "coef", :default 1.0}]}
   
   ;; Decay : Filter {
   
   ;; 	*ar { arg in = 0.0, decayTime = 1.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('audio', in, decayTime).madd(mul, add)
   ;; 	}
   ;; 	*kr { arg in = 0.0, decayTime = 1.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('control', in, decayTime).madd(mul, add)
   ;; 	}
   ;; }
   
   {:name "Decay",
    :args [{:name "in", :default 0.0}
           {:name "decayTime", :default 1.0}]}
   
   ;; Decay2 : Filter {
   
   ;; 	*ar { arg in = 0.0, attackTime = 0.01, decayTime = 1.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('audio', in, attackTime, decayTime).madd(mul, add)
   ;; 	}
   ;; 	*kr { arg in = 0.0, attackTime = 0.01, decayTime = 1.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('control', in, attackTime, decayTime).madd(mul, add)
   ;; 	}
   ;; }
   
   {:name "Decay2",
    :args [{:name "in", :default 0.0}
           {:name "attackTime", :default 0.01}
           {:name "decayTime", :default 1.0}]}
   
   ;; Lag : Filter {
   
   ;; 	*ar { arg in = 0.0, lagTime = 0.1, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('audio', in, lagTime).madd(mul, add)
   ;; 	}
   ;; 	*kr { arg in = 0.0, lagTime = 0.1, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('control', in, lagTime).madd(mul, add)
   ;; 	}
   ;; }
   
   {:name "Lag",
    :args [{:name "in", :default 0.0}
           {:name "lagTime", :default 0.1}]}
   
   ;; Lag2 : Lag {}
   
   {:name "Lag2",
    :args [{:name "in", :default 0.0}
           {:name "lagTime", :default 0.1}]}
   
   ;; Lag3 : Lag {}
   
   {:name "Lag3",
    :args [{:name "in", :default 0.0}
           {:name "lagTime", :default 0.1}]}
   
   ;; Ramp : Lag {}
   
   {:name "Ramp",
    :args [{:name "in", :default 0.0}
           {:name "lagTime", :default 0.1}]}

   ;; /// added by nescivi - 15 may 2007
   ;; LagUD : Filter {
   
   ;; 	*ar { arg in = 0.0, lagTimeU = 0.1, lagTimeD = 0.1,  mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('audio', in, lagTimeU, lagTimeD).madd(mul, add)
   ;; 	}
   ;; 	*kr { arg in = 0.0, lagTimeU = 0.1, lagTimeD = 0.1, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('control', in, lagTimeU, lagTimeD).madd(mul, add)
   ;; 	}
   ;; }
   
   {:name "LagUD", :args [{:name "in", :default 0.0} {:name "lagTimeU", :default 0.1} {:name "lagTimeD", :default 0.1}]}
   ;; Lag2UD : LagUD {}
   {:name "Lag2UD", :args [{:name "in", :default 0.0} {:name "lagTimeU", :default 0.1} {:name "lagTimeD", :default 0.1}]}
   ;; Lag3UD : LagUD {}
   {:name "Lag3UD", :args [{:name "in", :default 0.0} {:name "lagTimeU", :default 0.1} {:name "lagTimeD", :default 0.1}]}
   ;; LeakDC : Filter {
   
   ;; 	*ar { arg in = 0.0, coef = 0.995, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('audio', in, coef).madd(mul, add)
   ;; 	}
   ;; 	*kr { arg in = 0.0, coef = 0.9, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('control', in, coef).madd(mul, add)
   ;; 	}
   ;; }

   {:name "LeakDC", :args [{:name "in", :default 0.0} {:name "coef", :default 0.995}]}
   ;; RLPF : Filter {
   
   ;; 	*ar { arg in = 0.0, freq = 440.0, rq = 1.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('audio', in, freq, rq).madd(mul, add)
   ;; 	}
   ;; 	*kr { arg in = 0.0, freq = 440.0, rq = 1.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('control', in, freq, rq).madd(mul, add)
   ;; 	}
   ;; }
   {:name "RLPF", :args [{:name "in", :default 0.0} {:name "freq", :default 440.0} {:name "rq", :default 1.0}]}
   ;; RHPF : RLPF {}
   {:name "RHPF", :args [{:name "in", :default 0.0} {:name "freq", :default 440.0} {:name "rq", :default 1.0}]}

   ;; LPF : Filter {
   
   ;; 	*ar { arg in = 0.0, freq = 440.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('audio', in, freq).madd(mul, add)
   ;; 	}
   ;; 	*kr { arg in = 0.0, freq = 440.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('control', in, freq).madd(mul, add)
   ;; 	}
   ;; }
   {:name "LPF", :args [{:name "in", :default 0.0} {:name "freq", :default 440.0}]}
   ;; HPF : LPF {}
   {:name "HPF", :args [{:name "in", :default 0.0} {:name "freq", :default 440.0}]}
   ;; BPF : Filter {
   
   ;; 	*ar { arg in = 0.0, freq = 440.0, rq = 1.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('audio', in, freq, rq).madd(mul, add)
   ;; 	}
   ;; 	*kr { arg in = 0.0, freq = 440.0, rq = 1.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('control', in, freq, rq).madd(mul, add)
   ;; 	}
   ;; }
   {:name "BPF", :args [{:name "in", :default 0.0} {:name "freq", :default 440.0} {:name "rq", :default 1.0}]}
   ;; BRF : BPF {}
   {:name "BRF", :args [{:name "in", :default 0.0} {:name "freq", :default 440.0} {:name "rq", :default 1.0}]}
   ;; MidEQ : Filter {
   
   ;; 	*ar { arg in = 0.0, freq = 440.0, rq = 1.0, db = 0.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('audio', in, freq, rq, db).madd(mul, add)
   ;; 	}
   ;; 	*kr { arg in = 0.0, freq = 440.0, rq = 1.0, db = 0.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('control', in, freq, rq, db).madd(mul, add)
   ;; 	}
   ;; }
   {:name "MidEQ", :args [{:name "in", :default 0.0} {:name "freq", :default 440.0} {:name "rq", :default 1.0} {:name "db", :default 0.0}]}
   ;; LPZ1 : Filter {
   
   ;; 	*ar { arg in = 0.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('audio', in).madd(mul, add)
   ;; 	}
   ;; 	*kr { arg in = 0.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('control', in).madd(mul, add)
   ;; 	}
   ;; }
   {:name "LPZ1", :args [{:name "in", :default 0.0}]}
   

   ;; HPZ1 : LPZ1 {}
   {:name "HPZ1", :args [{:name "in", :default 0.0}]}
   

   ;; Slope : Filter {
   
   ;; 	*ar { arg in = 0.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('audio', in).madd(mul, add)
   ;; 	}
   ;; 	*kr { arg in = 0.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('control', in).madd(mul, add)
   ;; 	}
   ;; }
   {:name "Slope", :args [{:name "in", :default 0.0}]}
   ;; LPZ2 : Filter {
   
   ;; 	*ar { arg in = 0.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('audio', in).madd(mul, add)
   ;; 	}
   ;; 	*kr { arg in = 0.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('control', in).madd(mul, add)
   ;; 	}
   ;; }
   {:name "LPZ2", :args [{:name "in", :default 0.0}]}
   ;; HPZ2 : LPZ2 {}
   {:name "HPZ2", :args [{:name "in", :default 0.0}]}
   ;; BPZ2 : LPZ2 {}
   {:name "BPZ2", :args [{:name "in", :default 0.0}]}
   ;; BRZ2 : LPZ2 {}
   {:name "BRZ2", :args [{:name "in", :default 0.0}]}
   
   ;; Median : Filter {
   ;; 	*ar { arg length=3, in = 0.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('audio', length, in).madd(mul, add)
   ;; 	}
   ;; 	*kr { arg length=3, in = 0.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('control', length, in).madd(mul, add)
   ;; 	}
   ;; 	checkInputs {
   ;;  		if (rate == 'audio', {
   ;;  			if (inputs.at(1).rate != 'audio', { 
   ;;  				^"input was not audio rate";
   ;;  			});
   ;;  		});
   ;;  		^this.checkValidInputs
   ;;  	}
   ;; }

   {:name "Median", :args [{:name "length", :default 3.0} {:name "in", :default 0.0}]}


   ;; Slew : Filter {
   ;; 	*ar { arg in = 0.0, up = 1.0, dn = 1.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('audio', in, up, dn).madd(mul, add)
   ;; 	}
   ;; 	*kr { arg in = 0.0, up = 1.0, dn = 1.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('control', in, up, dn).madd(mul, add)
   ;; 	}
   ;; }
   {:name "Slew", :args [{:name "in", :default 0.0} {:name "up", :default 1.0} {:name "dn", :default 1.0}]}
   
   ;; FOS : Filter {
   ;; 	*ar { arg in = 0.0, a0 = 0.0, a1 = 0.0, b1 = 0.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('audio', in, a0, a1, b1).madd(mul, add)
   ;; 	}
   ;; 	*kr { arg in = 0.0, a0 = 0.0, a1 = 0.0, b1 = 0.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('control', in, a0, a1, b1).madd(mul, add)
   ;; 	}
   ;; }
   {:name "FOS", 
    :args [{:name "in", :default 0.0} {:name "a0", :default 0.0} {:name "a1", :default 0.0} {:name "b1", :default 0.0}]}
   
   ;; SOS : Filter {
   ;; 	*ar { arg in = 0.0, a0 = 0.0, a1 = 0.0, a2 = 0.0, b1 = 0.0, b2 = 0.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('audio', in, a0, a1, a2, b1, b2).madd(mul, add)
   ;; 	}
   ;; 	*kr { arg in = 0.0, a0 = 0.0, a1 = 0.0, a2 = 0.0, b1 = 0.0, b2 = 0.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('control', in, a0, a1, a2, b1, b2).madd(mul, add)
   ;; 	}
   ;; }
   {:name "SOS",
    :args [{:name "in", :default 0.0}
           {:name "a0", :default 0.0}
           {:name "a1", :default 0.0}
           {:name "a2", :default 0.0}
           {:name "b1", :default 0.0}
           {:name "b2", :default 0.0}]}

   ;; Ringz : Filter {
   
   ;; 	*ar { arg in = 0.0, freq = 440.0, decaytime = 1.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('audio', in, freq, decaytime).madd(mul, add)
   ;; 	}
   ;; 	*kr { arg in = 0.0, freq = 440.0, decaytime = 1.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('control', in, freq, decaytime).madd(mul, add)
   ;; 	}
   ;; }
   
   {:name "Ringz",
    :args [{:name "in", :default 0.0}
           {:name "freq", :default 440.0}
           {:name "decaytime", :default 1.0}]}
   
   ;; Formlet : Filter {
   
   ;; 	*ar { arg in = 0.0, freq = 440.0, attacktime = 1.0, decaytime = 1.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('audio', in, freq, attacktime, decaytime).madd(mul, add)
   ;; 	}
   ;; 	*kr { arg in = 0.0, freq = 440.0, attacktime = 1.0, decaytime = 1.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('control', in, freq, attacktime, decaytime).madd(mul, add)
   ;; 	}
   ;; }

   {:name "Formlet",
    :args [{:name "in", :default 0.0}
           {:name "freq", :default 440.0}
           {:name "attacktime", :default 1.0}
           {:name "decaytime", :default 1.0}]}])

;; // the doneAction arg lets you cause the EnvGen to stop or end the 
;; // synth without having to use a PauseSelfWhenDone or FreeSelfWhenDone ugen. 
;; // It is more efficient to use a doneAction.
;; // doneAction = 0   do nothing when the envelope has ended.
;; // doneAction = 1   pause the synth running, it is still resident.
;; // doneAction = 2   remove the synth and deallocate it.
;; // doneAction = 3   remove and deallocate both this synth and the preceeding node.
;; // doneAction = 4   remove and deallocate both this synth and the following node.
;; // doneAction = 5   remove and deallocate this synth and free all children in the preceeding group (if it is a group).
;; // doneAction = 6   remove and deallocate this synth and free all children in the following group (if it is a group).

;; DetectSilence : Filter {

;; 	*ar { arg in = 0.0, amp = 0.0001, time = 0.1, doneAction = 0;
;; 		^this.multiNew('audio', in, amp, time, doneAction)
;; 		//		^0.0		// DetectSilence has no output
;; 	}
;; 	*kr { arg in = 0.0, amp = 0.0001, time = 0.1, doneAction = 0;
;; 		^this.multiNew('control', in, amp, time, doneAction)
;; 		//		^0.0		// DetectSilence has no output
;; 	}
;; }

(def detect-silence
  {:name "DetectSilence",
   :args [{:name "in", :default 0.0}
          {:name "amp", :default 0.0001}
          {:name "time", :default 0.1}
          {:name "doneAction", :default 0}],
   :num-outs 0
   :check-inputs check-same-rate-as-first-input
   :init parse-done-action})

(def specs
     (conj (map #(assoc :muladd true
                        :check-inputs check-same-rate-as-first-input)
                muladd-specs)
           detect-silence))
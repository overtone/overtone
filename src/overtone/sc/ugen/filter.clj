(ns overtone.sc.ugen.filter
  (:use (overtone.sc.ugen common)))

;; Filter : UGen {
;;  	checkInputs { ^this.checkSameRateAsFirstInput }
;; }

(def muladd-specs
  [
   {:name "Resonz",
    :args [{:name "in", :default 0.0}
           {:name "freq", :default 440.0}
           {:name "bwr", :default 1.0}]
    :doc "a two pole resonant filter"}

   {:name "OnePole",
    :args [{:name "in", :default 0.0}
           {:name "coef", :default 0.5}]
    :doc "A one pole filter. Implements the formula:
out(i) = ((1 - abs(coef)) * in(i)) + (coef * out(i-1))"}

   {:name "OneZero",
    :args [{:name "in", :default 0.0}
           {:name "coef", :default 0.5}]
    :doc "A one zero filter. Implements the formula :
out(i) = ((1 - abs(coef)) * in(i)) + (coef * in(i-1))"}

   {:name "TwoPole",
    :args [{:name "in", :default 0.0}
           {:name "freq", :default 440.0}
           {:name "radius", :default 0.8}]
    :doc "a two pole filter"}

   {:name "TwoZero",
    :args [{:name "in", :default 0.0}
           {:name "freq", :default 440.0}
           {:name "radius", :default 0.8}]
    :doc "a two zero filter"}

   ;; APF : TwoPole {}
   {:name "APF",
    :args [{:name "in", :default 0.0}
           {:name "freq", :default 440.0}
           {:name "radius", :default 0.8}]
    :doc ""}

   {:name "Integrator",
    :args [{:name "in", :default 0.0}
           {:name "coef", :default 1.0}]
    :doc "leaky integrator"}

   {:name "Decay",
    :args [{:name "in", :default 0.0}
           {:name "decayTime", :default 1.0}]
    :doc "triggered exponential decay"}

   {:name "Decay2",
    :args [{:name "in", :default 0.0}
           {:name "attackTime", :default 0.01}
           {:name "decayTime", :default 1.0}]
    :doc "triggered exponential attack and exponential decay"}

   {:name "Lag",
    :args [{:name "in", :default 0.0}
           {:name "lagTime", :default 0.1}]
    :doc "exponential lag, useful for smoothing out control signals"}

   {:name "Lag2",
    :args [{:name "in", :default 0.0}
           {:name "lagTime", :default 0.1}]
    :doc "equivalent to Lag.kr(Lag.kr(in, time), time), resulting in a smoother transition"}

   {:name "Lag3",
    :args [{:name "in", :default 0.0}
           {:name "lagTime", :default 0.1}]
    :doc "equivalent to Lag.kr(Lag.kr(Lag.kr(in, time), time), time), resulting in a smoother transition"}

   {:name "Ramp",
    :args [{:name "in", :default 0.0}
           {:name "lagTime", :default 0.1}]
    :doc "similar to Lag but with a linear rather than exponential lag, useful for smoothing out control signals"}

   {:name "LagUD",
    :args [{:name "in", :default 0.0}
           {:name "lagTimeU", :default 0.1}
           {:name "lagTimeD", :default 0.1}]
    :doc "the same as Lag except that you can supply a different 60 dB time for when the signal goes up, from when the signal goes down"}

   {:name "Lag2UD",
    :args [{:name "in", :default 0.0}
           {:name "lagTimeU", :default 0.1}
           {:name "lagTimeD", :default 0.1}]
    :doc "Lag2 is equivalent to Lag.kr(Lag.kr(in, time), time)"}

   {:name "Lag3UD",
    :args [{:name "in", :default 0.0}
           {:name "lagTimeU", :default 0.1}
           {:name "lagTimeD", :default 0.1}]
    :doc "Lag3UD is equivalent to LagUD.kr(LagUD.kr(LagUD.kr(in, timeU, timeD), timeU, timeD), timeU, timeD)"}

   {:name "LeakDC",
    :args [{:name "in", :default 0.0}
           {:name "coef", :default 0.995}]
    :doc "removes a DC offset from signal"}

   {:name "RLPF"
    :args [{:name "in", :default 0.0}
           {:name "freq", :default 440.0}
           {:name "rq", :default 1.0}]
    :doc "a resonant low pass filter"}

   {:name "RHPF", :args [{:name "in", :default 0.0}
                         {:name "freq", :default 440.0}
                         {:name "rq", :default 1.0}]
    :doc "a resonant high pass filter"}

   {:name "LPF",
    :args [{:name "in", :default 0.0}
           {:name "freq", :default 440.0}]
    :doc "a second order Butterworth low pass filter"}

   {:name "HPF",
    :args [{:name "in", :default 0.0}
           {:name "freq", :default 440.0}]
    :doc "a second order high pass filter"}

   {:name "BPF",
    :args [{:name "in", :default 0.0}
           {:name "freq", :default 440.0}
           {:name "rq", :default 1.0}]
    :doc "a second order Butterworth bandpass filter"}

   {:name "BRF",
    :args [{:name "in", :default 0.0}
           {:name "freq", :default 440.0}
           {:name "rq", :default 1.0}]
    :doc "a second order lowpass filter"}

   {:name "MidEQ",
    :args [{:name "in", :default 0.0}
           {:name "freq", :default 440.0}
           {:name "rq", :default 1.0}
           {:name "db", :default 0.0}]
    :doc "attenuates or boosts a frequency band"}

   {:name "LPZ1", :args [{:name "in", :default 0.0}]
    :doc "two point average filter"}

   {:name "LPZ2", :args [{:name "in", :default 0.0}]
    :doc "two zero fixed lowpass"}

   {:name "HPZ1", :args [{:name "in", :default 0.0}]
    :doc "two point difference filter"}

   {:name "HPZ2", :args [{:name "in", :default 0.0}]
    :doc "two zero fixed highpass"}

   {:name "Slope", :args [{:name "in", :default 0.0}]
    :doc ""}

   {:name "BPZ2", :args [{:name "in", :default 0.0}]
    :doc "two zero fixed midpass"}

   {:name "BRZ2", :args [{:name "in", :default 0.0}]
    :doc "two zero fixed midcut"}

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

   {:name "Median",
    :args [{:name "length", :default 3.0}
           {:name "in", :default 0.0}]
    :doc "returns the median of the last length input points"}

   {:name "Slew",
    :args [{:name "in", :default 0.0}
           {:name "up", :default 1.0}
           {:name "dn", :default 1.0}]
    :doc "Smooth the curve by limiting the slope of the input signal to up and dn"}

   {:name "FOS",
    :args [{:name "in", :default 0.0}
           {:name "a0", :default 0.0}
           {:name "a1", :default 0.0}
           {:name "b1", :default 0.0}]
    :doc "first order filter section"}

   {:name "SOS",
    :args [{:name "in", :default 0.0}
           {:name "a0", :default 0.0}
           {:name "a1", :default 0.0}
           {:name "a2", :default 0.0}
           {:name "b1", :default 0.0}
           {:name "b2", :default 0.0}]
    :doc "second order filter section (biquad)"}

   {:name "Ringz",
    :args [{:name "in", :default 0.0}
           {:name "freq", :default 440.0}
           {:name "decaytime", :default 1.0}]
    :doc "Ringz is the same as Resonz, except that instead of a resonance parameter, the bandwidth is specified in a 60dB ring decay time"}

   {:name "Formlet",
    :args [{:name "in", :default 0.0}
           {:name "freq", :default 440.0}
           {:name "attacktime", :default 1.0}
           {:name "decaytime", :default 1.0}]
    :doc "a resonant filter whose impulse response is like that of a sine wave with a Decay2 envelope over it"}])

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
          {:name "action", :default 0 :map DONE-ACTIONS}],
   :num-outs 0
   :check-inputs same-rate-as-first-input})

(def specs
     (conj (map #(assoc % :muladd true
                        :check-inputs same-rate-as-first-input)
                muladd-specs)
           detect-silence))

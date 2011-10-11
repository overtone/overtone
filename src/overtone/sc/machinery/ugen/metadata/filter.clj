(ns overtone.sc.machinery.ugen.metadata.filter
  (:use [overtone.sc.machinery.ugen common check]))

;; Filter : UGen {
;;  	checkInputs { ^this.checkSameRateAsFirstInput }
;; }

(def muladd-specs
  [
   {:name "Resonz",
    :args [{:name "in", :default 0.0 :doc "input signal to be processed"}
           {:name "freq", :default 440.0 :doc "resonant frequency in Hertz"}
           {:name "bwr", :default 1.0 :doc "bandwidth ratio (reciprocal of Q). rq = bandwidth / centerFreq"}]
    :doc "A Note on Constant-Gain Digital Resonators,\" Computer Music Journal, vol 18, no. 4, pp. 8-10, Winter 1994.\" Computer Music Journal, vol 18, no. 4, pp. 8-10, Winter 1994."
    :auto-rate true}

   {:name "OnePole",
    :args [{:name "in", :default 0.0 :doc "input signal to be processed"}
           {:name "coef", :default 0.5 :doc "feedback coefficient. Should be between -1 and +1"}]
    :doc "A one pole filter. Implements the formula:
out(i) = ((1 - abs(coef)) * in(i)) + (coef * out(i-1))"
    :auto-rate true}

   {:name "OneZero",
    :args [{:name "in", :default 0.0 :doc "input signal to be processed"}
           {:name "coef", :default 0.5 :doc "feed forward coefficient. +0.5 makes a two point averaging filter (see also lpz1), -0.5 makes a differentiator (see also hpz1), +1 makes a single sample delay (see also delay1), -1 makes an inverted single sample delay."}]
    :doc "A one zero filter. Implements the formula :
out(i) = ((1 - abs(coef)) * in(i)) + (coef * in(i-1))"
    :auto-rate true}

   {:name "TwoPole",
    :args [{:name "in", :default 0.0 :doc "input signal to be processed"}
           {:name "freq", :default 440.0 :doc "frequency of pole angle"}
           {:name "radius", :default 0.8 :doc "radius of pole. Should be between 0 and +1"}]
    :doc "a two pole filter. This provides lower level access to setting of pole location. For general purposes Resonz is better."
    :auto-rate true}

   {:name "TwoZero",
    :args [{:name "in", :default 0.0 :doc "input signal to be processed"}
           {:name "freq", :default 440.0 :doc "frequency of zero angle"}
           {:name "radius", :default 0.8 :doc "radios of zero"}]
    :doc "a two zero filter"
    :auto-rate true}

   ;; APF : TwoPole {}
   {:name "APF",
    :args [{:name "in", :default 0.0}
           {:name "freq", :default 440.0}
           {:name "radius", :default 0.8}]
    :doc ""
    :auto-rate true}

   {:name "Integrator",
    :args [{:name "in", :default 0.0 :doc "input signal"}
           {:name "coef", :default 1.0 :doc "leak coefficient"}]
    :doc "leaky integrator. Integrates an input signal with a leak. The formula implemented is: out(0) = in(0) + (coef * out(-1))"
    :auto-rate true}

   {:name "Decay",
    :args [{:name "in", :default 0.0 :doc "input signal"}
           {:name "decay-time", :default 1.0 :doc "60 dB decay time in seconds"}]
    :doc "triggered exponential decay. This is essentially the same as integrator except that instead of supplying the coefficient directly, it is calculated from a 60 dB decay time. This is the time required for the integrator to lose 99.9 % of its value or -60dB. This is useful for exponential decaying envelopes triggered by impulses."
    :auto-rate true}

   {:name "Decay2",
    :args [{:name "in", :default 0.0 :doc "input signal"}
           {:name "attack-time", :default 0.01 :doc "60 dB attack time in seconds."}
           {:name "decay-time", :default 1.0 :doc "60 dB decay time in seconds."}]
    :doc "triggered exponential attack and exponential decay. Decay has a very sharp attack and can produce clicks. Decay2 rounds off the attack by subtracting one Decay from another. (decay in attack-time decay-time) equivalent to: (- (decay in attack-time decay-time) (decay in attack-time decay-time))"
    :auto-rate true}

   {:name "Lag",
    :args [{:name "in", :default 0.0 :doc "input signal"}
           {:name "lag-time", :default 0.1 :doc "60 dB lag time in seconds"}]
    :doc "exponential lag, useful for smoothing out control signals. This is essentially the same as OnePole except that instead of supplying the coefficient directly, it is calculated from a 60 dB lag time. This is the time required for the filter to converge to within 0.01 % of a value."
    :auto-rate true}

   {:name "Lag2",
    :args [{:name "in", :default 0.0 :doc "input signal"}
           {:name "lag-time", :default 0.1 :doc "60 dB lag time in seconds"} ]
    :doc "equivalent to Lag.kr(Lag.kr(in, time), time), resulting in a smoother transition. This saves on CPU as you only have to calculate the decay factor once instead of twice. See Lag for more details."
    :auto-rate true}

   {:name "Lag3",
    :args [{:name "in", :default 0.0 :doc "input signal"}
           {:name "lag-time", :default 0.1 :doc "60 dB lag time in seconds"}]
    :doc "Lag3 is equivalent to Lag.kr(Lag.kr(Lag.kr(in, time), time), time), thus resulting in a smoother transition. This saves on CPU as you only have to calculate the decay factor once instead of three times. See Lag for more details."
    :auto-rate true}

   {:name "Ramp",
    :args [{:name "in", :default 0.0 :doc "input signal"}
           {:name "lag-time", :default 0.1 :doc "60 dB lag time in seconds"}]
    :doc "similar to Lag but with a linear rather than exponential lag, useful for smoothing out control signals"
    :auto-rate true}

   {:name "LagUD",
    :args [{:name "in", :default 0.0 :doc "input signal"}
           {:name "lag-time-up", :default 0.1 :doc "60 dB lag time in seconds for the upgoing signal"}
           {:name "lag-time-down", :default 0.1 :doc "60 dB lag time in seconds for the downgoing signal"}]
    :doc "the same as Lag except that you can supply a different 60 dB time for when the signal goes up, from when the signal goes down"
    :auto-rate true}

   {:name "Lag2UD",
    :args [{:name "in", :default 0.0 :doc "input signal"}
           {:name "lag-time-up", :default 0.1 :doc "60 dB lag time in seconds for the upgoing signal"}
           {:name "lag-time-down", :default 0.1 :doc "60 dB lag time in seconds for the downgoing signal"}]
    :doc "Lag2 is equivalent to Lag.kr(Lag.kr(in, time), time)"
    :auto-rate true}

   {:name "Lag3UD",
    :args [{:name "in", :default 0.0 :doc "input signal"}
           {:name "lag-time-up", :default 0.1 :doc "60 dB lag time in seconds for the upgoing signal"}
           {:name "lag-time-down", :default 0.1 :doc "60 dB lag time in seconds for the downgoing signal"}]
    :doc "Lag3UD is equivalent to LagUD.kr(LagUD.kr(LagUD.kr(in, timeU, timeD), timeU, timeD), timeU, timeD)"
    :auto-rate true}

   {:name "LeakDC",
    :args [{:name "in", :default 0.0 :doc "input signal"}
           {:name "coef", :default 0.995 :doc "leak coefficient. Good starting point values are to 0.995 for audiorate and  0.9 for controlrate"}]
    :doc "removes a DC offset from signal"
    :auto-rate true}

   {:name "RLPF"
    :args [{:name "in", :default 0.0 :doc "input signal to be processed"}
           {:name "freq", :default 440.0 :doc "cutoff frequency"}
           {:name "rq", :default 1.0 :doc "the reciprocal of Q.  bandwidth / cutoffFreq"}]
    :doc "a resonant low pass filter"
    :auto-rate true}

   {:name "RHPF", :args [{:name "in", :default 0.0 :doc "input signal to be processed"}
                         {:name "freq", :default 440.0 :doc "cutoff frequency"}
                         {:name "rq", :default 1.0 :doc "the reciprocal of Q.  bandwidth / cutoffFreq"}]
    :doc "a resonant high pass filter"
    :auto-rate true}

   {:name "LPF",
    :args [{:name "in", :default 0.0 :doc "input signal to be processed"}
           {:name "freq", :default 440.0 :doc "cutoff frequency"}]
    :doc "a second order Butterworth low pass filter"
    :auto-rate true}

   {:name "HPF",
    :args [{:name "in", :default 0.0 :doc "input signal to be processed"}
           {:name "freq", :default 440.0 :doc "cutoff frequency"}]
    :doc "a second order high pass filter"
    :auto-rate true}

   {:name "BPF",
    :args [{:name "in", :default 0.0 :doc "input signal to be processed"}
           {:name "freq", :default 440.0 :doc "centre frequency in Hertz"}
           {:name "rq", :default 1.0 :doc "the reciprocal of Q.  bandwidth / cutoffFreq"}]
    :doc "a second order Butterworth bandpass filter"
    :auto-rate true}

   {:name "BRF",
    :args [{:name "in", :default 0.0 :doc "input signal to be processed"}
           {:name "freq", :default 440.0 :doc "centre frequency in Hertz"}
           {:name "rq", :default 1.0 :doc "the reciprocal of Q.  bandwidth / cutoffFreq"}]
    :doc "a second order lowpass filter"
    :auto-rate true}

   {:name "MidEQ",
    :args [{:name "in", :default 0.0 :doc "input signal to be processed"}
           {:name "freq", :default 440.0 :doc "center frequency of the band in Hertz"}
           {:name "rq", :default 1.0 :doc "the reciprocal of Q.  bandwidth / cutoffFreq"}
           {:name "db", :default 0.0 :doc "amount of boost (db > 0) or attenuation (db < 0) of the frequency band"}]
    :doc "attenuates or boosts a frequency band"
    :auto-rate true}

   {:name "LPZ1", :args [{:name "in", :default 0.0}]
    :doc "two point average filter. Implements the formula: out(i) = 0.5 * (in(i) + in(i-1))"
    :auto-rate true}

   {:name "LPZ2", :args [{:name "in", :default 0.0}]
    :doc "two zero fixed lowpass. Implements the formula: out(i) = 0.25 * (in(i) + (2*in(i-1)) + in(i-2))"
    :auto-rate true}

   {:name "HPZ1", :args [{:name "in", :default 0.0}]
    :doc "two point difference filter. Implements the formula: out(i) = 0.5 * (in(i) - in(i-1))"
    :auto-rate true}

   {:name "HPZ2", :args [{:name "in", :default 0.0}]
    :doc "two zero fixed highpass. Implements the formula: out(i) = 0.25 * (in(i) - (2*in(i-1)) + in(i-2))"
    :auto-rate true}

   {:name "Slope", :args [{:name "in", :default 0.0 :doc "input signal to measure"}]
    :doc "Measures the rate of change per second of a signal. Formula implemented is: out[i] = (in[i] - in[i-1]) * sampling_rate"
    :auto-rate true}

   {:name "BPZ2", :args [{:name "in", :default 0.0}]
    :doc "two zero fixed midpass which cuts out 0 Hz and the Nyquist frequency. Implements the formula: out(i) = 0.5 * (in(i) - in(i-2))"
    :auto-rate true}

   {:name "BRZ2", :args [{:name "in", :default 0.0}]
    :doc "two zero fixed midcut which cuts out frequencies around 1/2 of the Nyquist frequency. Implements the formula: out(i) = 0.5 * (in(i) + in(i-2))"
    :auto-rate true}

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
    :args [{:name "length", :default 3.0 :doc "number of input points in which to find the median. Must be an odd number from 1 to 31. If length is 1 then Median has no effect."}
           {:name "in", :default 0.0 :doc "Input signal to be processed"}]
    :doc "returns the median of the last length input points. This non linear filter is good at reducing impulse noise  from a signal."
    :auto-rate true}

   {:name "Slew",
    :args [{:name "in", :default 0.0 :doc "input signal"}
           {:name "up", :default 1.0 :doc "maximum upward slope"}
           {:name "dn", :default 1.0 :doc "maximum downward slope"}]
    :doc "Smooth the curve by limiting the slope of the input signal to up and dn"
    :auto-rate true}

   {:name "FOS",
    :args [{:name "in", :default 0.0 :doc "input signal"}
           {:name "a0", :default 0.0 :doc "first coefficient"}
           {:name "a1", :default 0.0 :doc "second coefficient"}
           {:name "b1", :default 0.0 :doc "third coefficient"}]
    :doc "first order filter section. Formula is equivalent to: out(i) = (a0 * in(i)) + (a1 * in(i-1)) + (b1 * out(i-1))"
    :auto-rate true}

   {:name "SOS",
    :args [{:name "in", :default 0.0 :doc "input signal"}
           {:name "a0", :default 0.0 :doc "1st coefficient"}
           {:name "a1", :default 0.0 :doc "2nd coefficient"}
           {:name "a2", :default 0.0 :doc "3rd coefficient"}
           {:name "b1", :default 0.0 :doc "4th coefficient"}
           {:name "b2", :default 0.0 :doc "5th coefficient"}]
    :doc "second order filter section (biquad). Formula is equivalent to: out(i) = (a0 * in(i)) + (a1 * in(i-1)) + (a2 * in(i-2)) + (b1 * out(i-1)) + (b2 * out(i-2))"
    :auto-rate true}

   {:name "Ringz",
    :args [{:name "in", :default 0.0 :doc "input signal to be processed"}
           {:name "freq", :default 440.0 :doc "resonant frequency in Hertz"}
           {:name "decay-time", :default 1.0 :doc "the 60 dB decay time of the filter"}]
    :doc "Ringz is the same as Resonz, except that instead of a resonance parameter, the bandwidth is specified in a 60dB ring decay time. One Ringz is equivalent to one component of the Klank UGen"
    :auto-rate true}

   {:name "Formlet",
    :args [{:name "in", :default 0.0 :doc "input signal to be processed"}
           {:name "freq", :default 440.0 :doc "resonant frequency in Hertz"}
           {:name "attack-time", :default 1.0 :doc "60 dB attack time in seconds"}
           {:name "decay-time", :default 1.0 :doc "60 dB decay time in seconds"}]
    :doc "a resonant filter whose impulse response is like that of a sine wave with a Decay2 envelope over it. The great advantage to this filter over FOF is that there is no limit to the number of overlapping
grains since the grain is just the impulse response of the filter. Note that if attacktime == decaytime then the signal cancels out and if attacktime > decaytime then the impulse response is inverted."
    :auto-rate true}])

(def detect-silence
  {:name "DetectSilence",
   :args [{:name "in", :default 0.0 :doc "any source"}
          {:name "amp", :default 0.0001 :doc "when input falls below this, evaluate doneAction"}
          {:name "time", :default 0.1 :doc "the minimum duration of the input signal which input must fall below thresh before this triggers. The default is 0.1 seconds"}
          {:name "action", :default 0 :doc "the action to perform when silence is detected. Default: NO-ACTION"}],
   :num-outs 1
   :check-inputs same-rate-as-first-input
   :doc "If the signal input starts with silence at the beginning of the synth's duration, then DetectSilence will wait indefinitely until the first sound before starting to monitor for silence. This UGen outputs 1 if silence is detected, otherwise 0."
   :rates #{:ar :kr}
   :auto-rate true})

(def specs
     (conj (map #(assoc % :check-inputs same-rate-as-first-input)
                muladd-specs)
           detect-silence))

(ns overtone.sc.ugen.beq-suite)

;; BEQSuite : Filter {}

(def specs
  [
   ;; BLowPass : BEQSuite {
   ;; 	*ar { arg in, freq = 1200.0, rq = 1.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('audio', in, freq, rq).madd(mul, add);
   ;; 	}

   ;; 	*sc { arg dummy, freq = 1200.0, rq = 1.0;
   ;; 		var w0, cos_w0, i, alpha, a0, a1, b0rz, b1, b2, sr;
   ;; 		sr  = SampleRate.ir;
   ;; 		w0 = pi * 2 * freq * SampleDur.ir;
   ;; 		cos_w0 = w0.cos; i = 1 - cos_w0;
   ;; 		alpha = w0.sin * 0.5 * rq;
   ;; 		b0rz = (1 + alpha).reciprocal;
   ;; 		a0 = i * 0.5 * b0rz;
   ;; 		a1 = i * b0rz;
   ;; 		b1 = cos_w0 * 2 * b0rz;
   ;; 		b2 = (1 - alpha) * b0rz.neg;
   ;; 		^[a0, a1, a0, b1, b2];
   ;; 	}
   ;; }

   {:name "BLowPass",
    :args [{:name "in" :doc "input signal to be processed"}
           {:name "freq", :default 1200.0 :doc "cutoff frequency"}
           {:name "rq", :default 1.0 :doc "the reciprocal of Q.  bandwidth / cutoffFreq"}],
    :rates #{:ar}
    :doc "12db/oct rolloff - 2nd order resonant Low Pass Filter based on the Second Order Section (SOS) biquad UGen"}

   ;; BHiPass : BEQSuite {
   ;; 	*ar { arg in, freq = 1200.0, rq = 1.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('audio', in, freq, rq).madd(mul, add);
   ;; 	}

   ;; 	*sc { arg dummy, freq = 1200.0, rq = 1.0;
   ;; 		var i, w0, cos_w0, alpha, a0, a1, b0rz, b1, b2, sr;
   ;; 		sr  = SampleRate.ir;
   ;; 		w0 =  pi * 2 * freq * SampleDur.ir;
   ;; 		cos_w0 = w0.cos; i = 1 + cos_w0;
   ;; 		alpha = w0.sin * 0.5 * rq;
   ;; 		b0rz = (1 + alpha).reciprocal;
   ;; 		a0 = i * 0.5 * b0rz;
   ;; 		a1 = i.neg * b0rz;
   ;; 		b1 = cos_w0 * 2 * b0rz;
   ;; 		b2 = (1 - alpha) * b0rz.neg;
   ;; 		^[a0, a1, a0, b1, b2];
   ;; 	}
   ;;                     }

   {:name "BHiPass",
    :args [{:name "in" :doc "input signal to be processed"}
           {:name "freq", :default 1200.0 :doc "cutoff frequency"}
           {:name "rq", :default 1.0 :doc "the reciprocal of Q. bandwidth / cutoffFreq"}],
    :rates #{:ar}
    :doc "12db/oct rolloff - 2nd order resonant  Hi Pass Filter based on the Second Order Section (SOS) biquad UGen."}

   ;; BAllPass : BEQSuite {
   ;; 	*ar { arg in, freq = 1200.0, rq = 1.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('audio', in, freq, rq).madd(mul, add);
   ;; 	}

   ;; 	*sc { arg dummy, freq = 1200.0, rq = 1.0;
   ;; 		var w0, alpha, a0, b1, b0rz, sr;
   ;; 		sr  = SampleRate.ir;
   ;; 		w0 = pi * 2 * freq * SampleDur.ir;
   ;; 		alpha = w0.sin * 0.5 * rq;
   ;; 		b0rz = (1 + alpha).reciprocal;
   ;; 		a0 = (1 - alpha) * b0rz;
   ;; 		b1 = 2.0 * w0.cos * b0rz;
   ;; 		^[a0, b1.neg, 1.0, b1, a0.neg];
   ;; 	}
   ;; }

   {:name "BAllPass",
    :args [{:name "in" :doc "input signal to be processed."}
           {:name "freq", :default 1200.0 :doc "center frequency."}
           {:name "rq", :default 1.0 :doc "the reciprocal of Q.  bandwidth / cutoffFreq."}],
    :rates #{:ar}
    :doc "All pass filter based on the Second Order Section (SOS) biquad UGen"}

   ;; BBandPass : BEQSuite {
   ;; 	*ar {arg in, freq = 1200.0, bw = 1.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('audio', in, freq, bw).madd(mul, add);
   ;; 	}

   ;; 	*sc { arg dummy, freq = 1200.0, bw = 1.0;
   ;; 		var w0, sin_w0, alpha, a0, b0rz, b1, b2, sr;
   ;; 		sr  = SampleRate.ir;
   ;; 		w0 = pi * 2 * freq * SampleDur.ir;
   ;; 		sin_w0 = w0.sin;
   ;; 	//	alpha = w0.sin * 0.5 * rq;
   ;; 		alpha = sin_w0 * sinh(0.34657359027997 * bw * w0 / sin_w0);
   ;; 		b0rz = (1 + alpha).reciprocal;
   ;; 		a0 = alpha * b0rz;
   ;; 		b1 = w0.cos * 2 * b0rz;
   ;; 		b2 = (1 - alpha) * b0rz.neg;
   ;; 		^[a0, 0.0, a0.neg, b1, b2];
   ;; 	}
   ;; }

   {:name "BBandPass",
    :args [{:name "in" :doc "input signal to be processed"}
           {:name "freq", :default 1200.0 :doc "center frequency"}
           {:name "bw", :default 1.0 :doc "the bandwidth in octaves between -3 dB frequencies"}],
    :rates #{:ar}
    :doc "Band pass filter based on the Second Order Section (SOS) biquad UGen"}


   ;; BBandStop : BEQSuite {
   ;; 	*ar {arg in, freq = 1200.0, bw = 1.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('audio', in, freq, bw).madd(mul, add);
   ;; 	}

   ;; 	*sc { arg dummy, freq = 1200.0, bw = 1.0;
   ;; 		var w0, sin_w0, alpha, b1, b2, b0rz, sr;
   ;; 		sr  = SampleRate.ir;
   ;; 		w0 = pi * 2 * freq * SampleDur.ir;
   ;; 		sin_w0 = w0.sin;
   ;; 	//	alpha = w0.sin * 0.5 * rq;
   ;; 		alpha = sin_w0 * sinh(0.34657359027997 * bw * w0 / sin_w0);
   ;; 		b0rz = (1 + alpha).reciprocal;
   ;; 		b1 = 2.0 * w0.cos * b0rz;
   ;; 		b2 = (1 - alpha) * b0rz.neg;
   ;; 		^[b0rz, b1.neg, b0rz, b1, b2];
   ;; 	}
   ;; }

   {:name "BBandStop",
    :args [{:name "in" :doc "input signal to be processed"}
           {:name "freq", :default 1200.0 :doc "center frequency"}
           {:name "bw", :default 1.0 :doc "the bandwidth in octaves between -3 dB frequencies"}],
    :rates #{:ar}
    :doc "Band reject filter based on the Second Order Section (SOS) biquad UGen"}

   ;; BPeakEQ : BEQSuite {
   ;; 	*ar {arg in, freq = 1200.0, rq = 1.0, db = 0.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('audio', in, freq, rq, db).madd(mul, add);
   ;; 	}

   ;; 	*sc { arg dummy, freq = 1200.0, rq = 1.0, db = 0.0;
   ;; 		var a, w0, alpha, a0, a2, b1, b2, b0rz, sr;
   ;; 		sr  = SampleRate.ir;
   ;; 		a = pow(10, db/40);
   ;; 		w0 = pi * 2 * freq * SampleDur.ir;
   ;; 		alpha = w0.sin * 0.5 * rq;
   ;; 		b0rz = (1 + (alpha / a)).reciprocal;
   ;; 		a0 = (1 + (alpha * a)) * b0rz;
   ;; 		a2 = (1 - (alpha * a)) * b0rz;
   ;; 		b1 = 2.0 * w0.cos * b0rz;
   ;; 		b2 = (1 - (alpha / a)) * b0rz.neg;
   ;; 		^[a0, b1.neg, a2, b1, b2];
   ;; 	}
   ;;                     }

   {:name "BPeakEQ",
    :args [{:name "in" :doc "input signal to be processed"}
           {:name "freq", :default 1200.0 :doc "center frequency"}
           {:name "rq", :default 1.0 :doc "the reciprocal of Q.  bandwidth / cutoffFreq"}
           {:name "db", :default 0.0 :doc "boost/cut the center frequency (in dBs)"}],
    :rates #{:ar}
    :doc "Parametric equalizer based on the Second Order Section (SOS) biquad UGen"}

   ;; BLowShelf : BEQSuite {
   ;; 	*ar {arg in, freq = 1200.0, rs = 1.0, db = 0.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('audio', in, freq, rs, db).madd(mul, add);
   ;; 	}

   ;; 	*sc { arg dummy, freq = 120.0, rs = 1.0, db = 0.0;
   ;; 		var a, w0, sin_w0, cos_w0, alpha, i, j, k, a0, a1, a2, b0rz, b1, b2, sr;
   ;; 		sr  = SampleRate.ir;
   ;; 		a = pow(10, db/40);
   ;; 		w0 = pi * 2 * freq * SampleDur.ir;
   ;; 		cos_w0 = w0.cos;
   ;; 		sin_w0 = w0.sin;
   ;; 		alpha = sin_w0 * 0.5 * sqrt((a + a.reciprocal) * (rs - 1) + 2.0);
   ;; 		i = (a+1) * cos_w0;
   ;; 		j = (a-1) * cos_w0;
   ;; 		k = 2 * sqrt(a) * alpha;
   ;; 		b0rz = ((a+1) + j + k).reciprocal;
   ;; 		a0 = a * ((a+1) - j + k) * b0rz;
   ;; 		a1 = 2 * a * ((a-1) - i) * b0rz;
   ;; 		a2 = a * ((a+1) - j - k) * b0rz;
   ;; 		b1 = 2.0 * ((a-1) + i) * b0rz;
   ;; 		b2 = ((a+1) + j - k) * b0rz.neg;
   ;; 		^[a0, a1, a2, b1, b2];
   ;; 	}
   ;; }

   {:name "BLowShelf",
    :args [{:name "in" :doc "input signal to be processed"}
           {:name "freq", :default 1200.0 :doc "center frequency"}
           {:name "rs", :default 1.0 :doc "the reciprocal of S.  Shell boost/cut slope. When S = 1, the shelf slope is as steep as it can be and remain monotonically increasing or decreasing gain with frequency.  The shelf slope, in  dB/octave, remains proportional to S for all other values for a fixed freq/SampleRate.ir and db."}
           {:name "db", :default 0.0 :doc "gain. boost/cut the center frequency in dBs"}],
    :rates #{:ar}
    :doc "Low shelf based on the Second Order Section (SOS) biquad UGen"}

   ;; BHiShelf : BEQSuite {
   ;; 	*ar {arg in, freq = 1200.0, rs = 1.0, db = 0.0, mul = 1.0, add = 0.0;
   ;; 		^this.multiNew('audio', in, freq, rs, db).madd(mul, add);
   ;; 	}

   ;; 	*sc { arg dummy, freq = 120.0, rs = 1.0, db = 0.0;
   ;; 		var a, w0, sin_w0, cos_w0, alpha, i, j, k, a0, a1, a2, b0rz, b1, b2, sr;
   ;; 		sr  = SampleRate.ir;
   ;; 		a = pow(10, db/40);
   ;; 		w0 = pi * 2 * freq * SampleDur.ir;
   ;; 		cos_w0 = w0.cos;
   ;; 		sin_w0 = w0.sin;
   ;; 		alpha = sin_w0 * 0.5 * sqrt((a + a.reciprocal) * (rs - 1) + 2.0);
   ;; 		i = (a+1) * cos_w0;
   ;; 		j = (a-1) * cos_w0;
   ;; 		k = 2 * sqrt(a) * alpha;
   ;; 		b0rz = ((a+1) - j + k).reciprocal;
   ;; 		a0 = a * ((a+1) + j + k) * b0rz;
   ;; 		a1 = -2.0 * a * ((a-1) + i) * b0rz;
   ;; 		a2 = a * ((a+1) + j - k) * b0rz;
   ;; 		b1 = -2.0 * ((a-1) - i) * b0rz;
   ;; 		b2 = ((a+1) - j - k) * b0rz.neg;
   ;; 		^[a0, a1, a2, b1, b2];
   ;; 	}
   ;; }

   {:name "BHiShelf",
    :args [{:name "in" :doc "input signal to be processed"}
           {:name "freq", :default 1200.0 :doc "center frequency"}
           {:name "rs", :default 1.0 :doc "the reciprocal of S.  Shell boost/cut slope. When S = 1, the shelf slope is as steep as it can be and remain monotonically increasing or decreasing gain with frequency.  The shelf slope, in  dB/octave, remains proportional to S for all other values for a fixed freq/SampleRate.ir and db."}
           {:name "db", :default 0.0 :doc "gain. boost/cut the center frequency in dBs"}],
    :rates #{:ar}
    :doc "Hi shelfbased on the Second Order Section (SOS) biquad UGen"}])




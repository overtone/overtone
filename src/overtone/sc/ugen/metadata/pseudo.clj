(ns overtone.sc.ugen.metadata.pseudo
  (:use [overtone.sc.ugen common constants]))
;;TODO - convert these to cgens

(comment def specs
     [
      ;; from BufIO.sc
      ;; Tap : UGen {
      ;; 	*ar { arg bufnum = 0, numChannels = 1, delaytime = 0.2;
      ;; 		var n;
      ;; 		n = delaytime * SampleRate.ir.neg; // this depends on the session sample rate, not buffer.
      ;; 		^PlayBuf.ar(numChannels, bufnum, 1, 0, n, 1);
      ;; 	}
      ;; }

      {:name "Tap"
       :args [{:name "bufnum", :default 0 :doc " the index of the buffer to use"}
              {:name "numChannels" :mode :num-outs, :default 1 :doc "number of channels of the buffer"}
              {:name "delaytime" :default 0.2 :doc "tap delay; cannot be modulated"}]
       :rates #{:ar}
       :doc "The Tap UGen allows a single tap at a delay into a buffer. "}
;       :pseudo-ugen {:ar (fn [bufnum num-channels delaytime]
;                           (let [n (* delaytime (- (sample-rate)))]
;                             (playbuf:ar num-channels bufnum 1 0 n 1)))}}
;
      ;; from Osc.sc
      ;; IndexL {
      ;; 	*ar { arg bufnum, in = 0.0, mul = 1.0, add = 0.0;
      ;; 		var val0 = Index.ar(bufnum, in);
      ;; 		var val1 = Index.ar(bufnum, in + 1);
      ;; 		if(in.rate != \audio) { in = K2A.ar(in) }; // for now.
      ;; 		^LinLin.ar(in.frac, 0.0, 1.0, val0, val1);
      ;; 	}
      ;; 	*kr { arg bufnum, in = 0.0, mul = 1.0, add = 0.0;
      ;; 		var val0 = Index.kr(bufnum, in);
      ;; 		var val1 = Index.kr(bufnum, in + 1);
      ;; 		^LinLin.kr(in.frac, 0.0, 1.0, val0, val1);
      ;; 	}
      ;; }

      {:name "IndexL"
       :args [{:name "bufnum"}
              {:name "in", :default 0.0}]
       :pseudo-ugen {:ar (fn [bufnum in]
                           (let [vals (index:ar bufnum [in (+ in 1)])]
                             ...))
                     :kr (fn)}}

      ;; from Osc.sc
      ;; TChoose {
      ;; 	*ar { arg trig, array;
      ;; 		^Select.ar(TIRand.kr(0, array.lastIndex, trig), array)

      ;; 	}
      ;; 	*kr { arg trig, array;
      ;; 		^Select.kr(TIRand.kr(0, array.lastIndex, trig), array)

      ;; 	}
      ;; }

      {:name "TChoose"
       :args [{:name "trig"}
              {:name "array"}]
       :pseudo-ugen {:ar (fn [trig array]
                           (select:ar (tirand:kr 0 (- (count array) 1) trig) array))
                     :kr (fn [trig array]
                           (select:kr (tirand:kr 0 (- (count array) 1) trig) array))}}

      ;; from Osc.sc
      ;; TWChoose : UGen {
      ;; 	*ar { arg trig, array, weights, normalize=0;
      ;; 		^Select.ar(TWindex.ar(trig, weights, normalize), array)
      ;; 	}
      ;; 	*kr { arg trig, array, weights, normalize=0;
      ;; 		^Select.kr(TWindex.kr(trig, weights, normalize), array)
      ;; 	}

      ;; }

      {:name "TWChoose"
       :args [{:name "trig"}
              {:name "array"}
              {:name "weights"}
              {:name "normalize", :default 0}]
       :pseudo-ugen {:ar (fn [trig array weights normalize]
                           (select:ar (twindex:ar trig weights normalize) array))
                     :kr (fn [trig array weights normalize]
                           (select:kr (twindex:kr trig weights normalize) array))}}

      ;; from BEQSuite.sc
      ;; BLowPass4 {
      ;; 	*ar { arg in, freq = 1200.0, rq = 1.0, mul = 1.0, add = 0.0;
      ;; 		var coefs;
      ;; 		rq = sqrt(rq);
      ;; 		coefs = BLowPass.sc(nil, freq, rq);
      ;; 		^SOS.ar(SOS.ar(in, *coefs), *coefs ++ [mul, add]);
      ;; 	}
      ;; }

      {:name "BLowPass4"
       :args [{:name "in"}
              {:name "freq", :default 1200.0}
              {:name "rq", :default 1.0}]
       :rates #{:ar}
       :pseudo-ugen {:ar (fn [freq rq mul add]
                           (let [coefs (blowpass:sc ????)]))}}

      ;; from BEQSuite.sc
      ;; BHiPass4 {
      ;; 	*ar { arg in, freq = 1200.0, rq = 1.0, mul = 1.0, add = 0.0;
      ;; 		var coefs;
      ;; 		rq = sqrt(rq);
      ;; 		coefs = BHiPass.sc(nil, freq, rq);
      ;; 		^SOS.ar(SOS.ar(in, *coefs), *coefs ++ [mul, add]);
      ;; 	}
      ;; }

      {:name "BHiPass4"
       :args [{:name "in"}
              {:name "freq", :default 1200.0}
              {:name "rq", :default 1.0}]
       :rates #{:ar}
       :pseudo-ugen {:ar (fn [freq rq mul add]
                           (let [coefs (bhipass:sc ????)]))}}


      ;; from DelayWR.sc
      ;; PingPong {
      ;; 	//your buffer should be the same numChannels as your inputs
      ;; 	*ar { arg  bufnum=0,  inputs, delayTime, feedback=0.7, rotate=1;
      ;; 		var trig, delayedSignals;
      ;; 		trig = Impulse.kr(delayTime.reciprocal);
      ;;
      ;; 		delayedSignals =
      ;; 			PlayBuf.ar(inputs.numChannels,bufnum,1.0,trig,
      ;; 				0,
      ;; 				0.0).rotate(rotate)
      ;; 			* feedback + inputs;
      ;;
      ;; 		RecordBuf.ar(delayedSignals,bufnum,0.0,1.0,0.0,1.0,0.0,trig);
      ;;
      ;; 		^delayedSignals
      ;; 	}
      ;; }

      {:name "PingPong" ...}

      ;; from FFSinOsc.sc (fixed-freq-osc.clj)
      ;; DynKlank : UGen {
      ;;  *ar { arg specificationsArrayRef, input, freqscale = 1.0, freqoffset = 0.0, decayscale = 1.0;
      ;;    var inputs = [specificationsArrayRef, input, freqscale, freqoffset, decayscale].flop;
      ;;    ^inputs.collect { arg item; this.ar1(*item) }.unbubble
      ;;  }

      ;;  *ar1 { arg specificationsArrayRef, input, freqscale = 1.0, freqoffset = 0.0, decayscale = 1.0;
      ;;    var spec = specificationsArrayRef.value;
      ;;    ^Ringz.ar(
      ;;        input,
      ;;        spec[0] ? #[440.0] * freqscale + freqoffset,
      ;;        spec[2] ? #[1.0] * decayscale,
      ;;        spec[1] ? #[1.0]
      ;;    ).sum
      ;;  }
      ;; }

      {:name "DynKlank"}

      ;; from FFSinOsc.sc
      ;; DynKlang : UGen {
      ;;  *ar { arg specificationsArrayRef, freqscale = 1.0, freqoffset = 0.0;
      ;;    var inputs = [specificationsArrayRef, freqscale, freqoffset].flop;
      ;;    ^inputs.collect { arg item; this.ar1(*item) }.unbubble
      ;;  }
      ;;  *ar1 { arg specificationsArrayRef, freqscale = 1.0, freqoffset = 0.0;
      ;;    var spec = specificationsArrayRef.value;
      ;;    ^SinOsc.ar(
      ;;        spec[0] ? #[440.0] * freqscale + freqoffset,
      ;;        spec[2] ? #[0.0],
      ;;        spec[1] ? #[1.0]
      ;;    ).sum
      ;;  }
      ;; }

      {:name "DynKlang"}

      ;; from Hilbert.sc
      ;; // class using FFT (with a delay) for better results than the above UGen
      ;; // buffer should be 2048 or 1024
      ;; // 2048, better results, more delay
      ;; // 1024, less delay, little choppier results

      ;; HilbertFIR : UGen {
      ;; 	*ar { arg in, buffer;
      ;; 		var fft, delay;
      ;; 		fft = FFT(buffer, in);
      ;; 		fft = PV_PhaseShift90(fft);
      ;; 		delay = BufDur.kr(buffer);
      ;; 		// return [source, shift90]
      ;; 		^[DelayN.ar(in, delay, delay), IFFT(fft)];
      ;; 	}
      ;; }

      ;; from AudioIn.sc
      ;; SoundIn  {

      ;; 	*ar { arg bus = 0, mul=1.0, add=0.0;
      ;; 		var chanOffset;
      ;; 		chanOffset = this.channelOffset;
      ;; 		if(bus.isArray.not,{
      ;; 			^In.ar(chanOffset + bus, 1).madd(mul,add)
      ;; 		});

      ;; 		// check to see if channels array is consecutive [n,n+1,n+2...]
      ;; 		if(bus.every({arg item, i;
      ;; 				(i==0) or: {item == (bus.at(i-1)+1)}
      ;; 			}),{
      ;; 			^In.ar(chanOffset + bus.first, bus.size).madd(mul,add)
      ;; 		},{
      ;; 			// allow In to multi channel expand
      ;; 			^In.ar(chanOffset + bus).madd(mul,add)
      ;; 		})
      ;; 	}

      ;; 	*channelOffset {
      ;; 		^NumOutputBuses.ir
      ;; 	}
      ;; }

      ;; // backward compatible version. Only difference: starts counting from channel 1
      ;; from AudioIn.sc
      ;; AudioIn : SoundIn  {
      ;; 	*ar { arg channel = 0, mul=1.0, add=0.0;
      ;; 		^super.ar(channel, mul, add)
      ;; 	}
      ;; 	*channelOffset {
      ;; 		^NumOutputBuses.ir - 1
      ;; 	}
      ;; }

      ;; from Compander.sc
      ;;// CompanderD passes the signal directly to the control input,
      ;;// but adds a delay to the process input so that the lag in the gain
      ;;// clamping will not lag the attacks in the input sound

      ;; CompanderD : UGen {
      ;;   *ar { arg in = 0.0, thresh = 0.5, slopeBelow = 1.0, slopeAbove = 1.0,
      ;;     clampTime = 0.01, relaxTime = 0.01, mul = 1.0, add = 0.0;

      ;;     ^Compander.ar(DelayN.ar(in, clampTime, clampTime), in, thresh,
      ;;         slopeBelow, slopeAbove, clampTime, relaxTime).madd(mul, add)
      ;;   }
      ;; }

      ;; from Splay.sc
      ;;       Splay {
      ;;                                                                     	*ar { arg inArray, spread=1, level=1, center=0.0, levelComp=true;
      ;; 		var n, n1; n = inArray.size.max(2); n1 = n-1;

      ;; 		if (levelComp, { level = level * n.reciprocal.sqrt });

      ;; 		^Pan2.ar(
      ;; 			inArray,
      ;; 			((0 .. n1) * (2 / n1) - 1) * spread + center
      ;; 		).sum * level;
      ;; 	}

      ;; 	*arFill { arg n, function, spread=1, level=1, center=0.0, levelComp=true;
      ;; 		^this.ar((function ! n), spread, level, center, levelComp)
      ;; 	}
      ;; }

      ;; SplayZ {
      ;; 	*ar { arg numChans=4, inArray, spread=1, level = 1, width = 2, center = 0.0,
      ;; 			orientation = 0.5, levelComp=true;

      ;; 		var n, n1; n = inArray.size.max(2); n1 = n-1;
      ;; 		if (levelComp, { level = level * n.reciprocal.sqrt });

      ;; 		"SplayZ is deprecated, because its geometry is wrong.
      ;; 		Please convert to SplayAz.".inform;

      ;; 		^PanAz.ar(
      ;; 			numChans,
      ;; 			inArray,
      ;; 			((0 .. n1) * (2 / n1) - 1) * spread + center,
      ;; 			1,
      ;; 			width,
      ;; 			orientation
      ;; 		).sum * level;
      ;; 	}

      ;; 	*arFill { arg numChans=4, n, function, spread=1, level=1, width = 2, center=0.0,
      ;; 		orientation = 0.5, levelComp=true;
      ;; 		^this.ar(numChans, function ! n, spread, level, width, center,
      ;; 		orientation, levelComp)
      ;; 	}
      ;; }


      ;; SplayAz {
      ;; 	*ar { arg numChans=4, inArray, spread=1, level = 1, width = 2, center = 0.0,
      ;; 			orientation = 0.5, levelComp=true;

      ;; 		var n = inArray.size.max(1);
      ;; 		var moreOuts = numChans > n;

      ;; 		if (levelComp, { level = level * n.reciprocal.sqrt });
      ;; 		if (moreOuts, { inArray = inArray * level });

      ;; 		^PanAz.ar(
      ;; 			numChans,
      ;; 			inArray,
      ;; 			((0 .. n-1) / n * 2).postln * spread + center,
      ;; 			1,
      ;; 			width,
      ;; 			orientation
      ;; 		).sum * if (moreOuts, 1, level);
      ;; 	}

      ;; 	*arFill { arg numChans=4, n, function, spread=1, level=1, width = 2, center=0.0,
      ;; 		orientation = 0.5, levelComp=true;
      ;; 		^this.ar(numChans, function ! n, spread, level, width, center,
      ;; 		orientation, levelComp)
      ;; 	}
      ;; }

      ])

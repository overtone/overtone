(ns overtone.core.ugens.fft2)

;; //third party FFT UGens

(def specs
     [
      ;; //sick lincoln remembers complex analysis courses
      ;; PV_ConformalMap : PV_ChainUGen 
      ;; {
      ;;  *new { arg buffer, areal = 0.0, aimag = 0.0;
      ;;    ^this.multiNew('control', buffer, areal, aimag)
      ;;  }
      ;; }

      {:name "PV_ConformalMap",
       :args [{:name "buffer"}
              {:name "areal", :default 0.0}
              {:name "aimag", :default 0.0}],
       :rates #{:kr}}
      
      ;; //in and kernel are both audio rate changing signals
      ;; Convolution : UGen
      ;; {
      ;;  *ar { arg in, kernel, framesize=512,mul = 1.0, add = 0.0;
      ;;    ^this.multiNew('audio', in, kernel, framesize).madd(mul, add);
      ;;  }
      ;; }

      {:name "Convolution", :args [{:name "in"} {:name "kernel"} {:name "framesize", :default 512.0}], :rates #{:ar}}

      ;; //fixed kernel convolver with fix by nescivi to update the kernel on receipt of a trigger message 
      ;; Convolution2 : UGen
      ;; {
      ;;  *ar { arg in, kernel, trigger, framesize=0,mul = 1.0, add = 0.0;
      ;;   ^this.multiNew('audio', in, kernel, trigger, framesize).madd(mul, add);
      ;;  }
      ;; }

      {:name "Convolution2", :args [{:name "in"} {:name "kernel"} {:name "trigger"} {:name "framesize", :default 512.0}], :rates #{:ar}}
      
      ;; //fixed kernel convolver with linear crossfade
      ;; Convolution2L : UGen
      ;; {
      ;;  *ar { arg in, kernel, trigger, framesize=0, crossfade=1, mul = 1.0, add = 0.0;
      ;;   ^this.multiNew('audio', in, kernel, trigger, framesize, crossfade).madd(mul, add);
      ;;  }
      ;; }

      {:name "Convolution2L", :args [{:name "in"} {:name "kernel"} {:name "trigger"} {:name "framesize", :default 512.0} {:name "crossfade", :default 1.0}], :rates #{:ar}}

      ;; //fixed kernel stereo convolver with linear crossfade
      ;; StereoConvolution2L : MultiOutUGen
      ;; {
      ;;  *ar { arg in, kernelL, kernelR, trigger, framesize=0, crossfade=1, mul = 1.0, add = 0.0;
      ;;    ^this.multiNew('audio', in, kernelL, kernelR, trigger, framesize, crossfade).madd(mul, add);
      ;;  }
      ;;  init { arg ... theInputs;
      ;;    inputs = theInputs;   
      ;;    channels = [ 
      ;;      OutputProxy(rate, this, 0), 
      ;;      OutputProxy(rate, this, 1) 
      ;;    ];
      ;;    ^channels
      ;;  }
      ;; }

      {:name "StereoConvolution2L", :args [{:name "in"} {:name "kernelL"} {:name "kernelR"} {:name "trigger"} {:name "framesize", :default 512.0} {:name "crossfade", :default 1.0}], :rates #{:ar}}

      ;; //time based convolution by nescivi
      ;; Convolution3 : UGen
      ;; {
      ;;  *ar { arg in, kernel, trigger=0, framesize=0, mul = 1.0, add = 0.0;
      ;;   ^this.multiNew('audio', in, kernel, trigger, framesize).madd(mul, add);
      ;;  }
      ;;  *kr { arg in, kernel, trigger=0, framesize=0, mul = 1.0, add = 0.0;
      ;;   ^this.multiNew('control', in, kernel, trigger, framesize).madd(mul, add);
      ;;  }
      ;; }


      {:name "Convolution3", :args [{:name "in"} {:name "kernel"} {:name "trigger", :default 0.0} {:name "framesize", :default 512.0}]}

      ;; //jensen andersen inspired FFT feature detector
      ;; PV_JensenAndersen : PV_ChainUGen
      ;; {
      ;;  *ar { arg buffer, propsc=0.25, prophfe=0.25, prophfc=0.25, propsf=0.25, threshold=1.0, waittime=0.04;
      ;;    ^this.multiNew('audio', buffer, propsc, prophfe, prophfc, propsf,  threshold, waittime);
      ;;  }
      ;; }

      {:name "PV_JensenAndersen", :args [{:name "buffer"} {:name "propsc", :default 0.25} {:name "prophfe", :default 0.25} {:name "prophfc", :default 0.25} {:name "propsf", :default 0.25} {:name "threshold", :default 1.0} {:name "waittime", :default 0.04}], :rates #{:ar}}

      ;; PV_HainsworthFoote : PV_ChainUGen
      ;; {
      ;;  *ar { arg buffer, proph=0.0, propf=0.0, threshold=1.0, waittime=0.04;
      ;;    ^this.multiNew('audio', buffer, proph, propf, threshold, waittime);
      ;;  }
      ;; }

      {:name "PV_HainsworthFoote", :args [{:name "buffer"} {:name "proph", :default 0.0} {:name "propf", :default 0.0} {:name "threshold", :default 1.0} {:name "waittime", :default 0.04}], :rates #{:ar}}

      ;; //not FFT but useful for time domain onset detection
      ;; RunningSum : UGen
      ;; {
      ;;  *ar { arg in, numsamp=40;
      ;;    ^this.multiNew('audio', in, numsamp);
      ;;  }
      ;;  *kr { arg in, numsamp=40;
      ;;    ^this.multiNew('control', in, numsamp);
      ;;  }
      
      ;;  *rms { arg in, numsamp=40;
      ;;    ^(RunningSum.ar(in.squared,numsamp)*(numsamp.reciprocal)).sqrt;
      ;;  }
      ;; }

      {:name "RunningSum", :args [{:name "in"} {:name "numsamp", :default 40.0}]}


      ])
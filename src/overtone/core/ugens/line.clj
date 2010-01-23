
(ns overtone.core.ugens.line
  (:use overtone.core.ugens.common))

(def specs
     [
      ;; Line : UGen {	
      ;;  *ar { arg start=0.0, end = 1.0, dur = 1.0, mul = 1.0, add = 0.0, doneAction = 0;
      ;;    ^this.multiNew('audio', start, end, dur, doneAction).madd(mul, add)
      ;;  }
      ;;  *kr { arg start=0.0, end = 1.0, dur = 1.0, mul = 1.0, add = 0.0, doneAction = 0;
      ;;    ^this.multiNew('control',  start, end, dur, doneAction).madd(mul, add)
      ;;  }
      ;; }

      {:name "Line",
       :args [{:name "start", :default 0.0}
              {:name "end", :default 1.0}
              {:name "dur", :default 1.0}
              {:name "doneAction", :default 0}]
       :muladd true} ;; TODO MAYBE? mul add offset
      
      ;; XLine : UGen { 
      ;;  *ar { arg start=1.0, end = 2.0, dur = 1.0, mul = 1.0, add = 0.0, doneAction = 0;
      ;;    ^this.multiNew('audio', start, end, dur, doneAction).madd(mul, add)
      ;;  }
      ;;  *kr { arg start=1.0, end = 2.0, dur = 1.0, mul = 1.0, add = 0.0, doneAction = 0;
      ;;    ^this.multiNew('control',  start, end, dur, doneAction).madd(mul, add)
      ;;  }
      ;; }

      {:name "XLine",
       :args [{:name "start", :default 1.0}
              {:name "end", :default 2.0}
              {:name "dur", :default 1.0}
              {:name "doneAction", :default 0}]
       :muladd true}  ;; TODO MAYBE? mul add offset
      
      ;; LinExp : UGen {
      ;;  *ar { arg in=0.0, srclo = 0.0, srchi = 1.0, dstlo = 1.0, dsthi = 2.0;
      ;;    ^this.multiNew('audio', in, srclo, srchi, dstlo, dsthi)
      ;;  }
      ;;  *kr { arg in=0.0, srclo = 0.0, srchi = 1.0, dstlo = 1.0, dsthi = 2.0;
      ;;    ^this.multiNew('control',  in, srclo, srchi, dstlo, dsthi)
      ;;  }
      ;; }  

      {:name "LinExp",
       :args [{:name "in", :default 0.0}
              {:name "srclo", :default 0.0}
              {:name "srchi", :default 1.0}
              {:name "dstlo", :default 1.0}
              {:name "dsthi", :default 2.0}]}

      ;; LinLin : UGen {
      ;;  *ar { arg in=0.0, srclo = 0.0, srchi = 1.0, dstlo = 1.0, dsthi = 2.0;
      ;;    ^this.multiNew('audio', in, srclo, srchi, dstlo, dsthi)
      ;;  }
      ;;  *kr { arg in=0.0, srclo = 0.0, srchi = 1.0, dstlo = 1.0, dsthi = 2.0;
      ;;    ^this.multiNew('control',  in, srclo, srchi, dstlo, dsthi)
      ;;  }
      ;; }

      {:name "LinLin",
       :args [{:name "in", :default 0.0}
              {:name "srclo", :default 0.0}
              {:name "srchi", :default 1.0}
              {:name "dstlo", :default 1.0}
              {:name "dsthi", :default 2.0}]}
      
      ;; AmpComp : UGen {
      ;;  *ir { arg freq = 60.midicps, root = 60.midicps, exp = 0.3333;
      ;;    ^this.multiNew('scalar', freq, root, exp)
      ;;  }
      ;;  *ar { arg freq = 60.midicps, root = 60.midicps, exp = 0.3333; 
      ;;    ^this.multiNew('audio', freq, root, exp)
      ;;  }
      ;;  *kr { arg freq = 60.midicps, root = 60.midicps, exp = 0.3333; 
      ;;    ^this.multiNew('control', freq, root, exp)
      ;;  }
      ;;  checkInputs { ^if(rate === \audio) { this.checkSameRateAsFirstInput } }
      ;; }

      {:name "AmpComp",
       :args [{:name "freq", :default 261.6256}
              {:name "root", :default 261.6256}
              {:name "exp", :default 0.3333}],
       :rates #{:ir :ar :kr}
       :check (when-ar (check-first-input-ar "freq must be audio rate"))}

      ;; AmpCompA : AmpComp {
      ;;  *ir { arg freq = 1000, root = 0, minAmp = 0.32, rootAmp = 1.0; 
      ;;    ^this.multiNew('scalar', freq, root, minAmp, rootAmp)
      ;;  }
      ;;  *ar { arg freq = 1000, root = 0, minAmp = 0.32, rootAmp = 1.0; 
      ;;    ^this.multiNew('audio', freq, root, minAmp, rootAmp)
      ;;  }
      ;;  *kr { arg freq = 1000, root = 0, minAmp = 0.32, rootAmp = 1.0;
      ;;    ^this.multiNew('control', freq, root, minAmp, rootAmp)
      ;;  }
      ;; }

      {:name "AmpCompA" :extends "AmpComp"
       :args [{:name "freq", :default 1000.0}
              {:name "root", :default 0}
              {:name "minAmp", :default 0.32}
              {:name "rootAmp", :default 1.0}]}

      ;; K2A : UGen { // control rate to audio rate converter
      ;;  *ar { arg in = 0.0;
      ;;    ^this.multiNew('audio', in)
      ;;  }
      ;; }

      {:name "K2A",
       :args [{:name "in", :default 0.0}],
       :rates #{:ar}}

      ;; A2K : UGen { // audio rate to control rate converter. only needed in specific cases
      ;;  *kr { arg in = 0.0;
      ;;    ^this.multiNew('control', in)
      ;;  }
      ;; }

      {:name "A2K",
       :args [{:name "in", :default 0.0}],
       :rates #{:kr}}

      ;; T2K : A2K { // audio rate to control rate trigger converter.
      ;;  checkInputs {
      ;;    if(inputs.at(0).rate != \audio) { 
      ;;      ^"first input is not audio rate"
      ;;    };
      ;;    ^nil
      ;;  }
      ;; }

      {:name "T2K" :extends "A2K"
       :check (check-first-input-ar)}

      ;; T2A : K2A { // control rate to audio rate trigger converter.
      ;;  *ar { arg in = 0.0, offset = 0;
      ;;    ^this.multiNew('audio', in, offset)
      ;;  }
      ;; }

      {:name "T2A", ; no need to derive
       :args [{:name "in", :default 0.0}
              {:name "offset", :default 0}],
       :rates #{:ar}}

      ;; DC : MultiOutUGen {
      ;;  *ar { arg in=0.0;
      ;;    ^this.multiNewList(['audio'] ++ in)
      ;;  }
      ;;  *kr { arg in=0.0;
      ;;    ^this.multiNewList(['control'] ++ in)
      ;;  }
      ;;  init { arg ... argInputs;
      ;;    inputs = argInputs;
      ;;    ^this.initOutputs(inputs.size, rate)
      ;;  }
      ;; }

      {:name "DC",
       :args [{:name "in", :mode :append-sequence-set-num-outs}]}
      
      ;; Silent : MultiOutUGen {
      ;;  *ar { arg numChannels = 1;
      ;;    ^this.multiNew('audio', numChannels)
      ;;  }
      ;;  init { arg numChannels;
      ;;    inputs = [];
      ;;    ^this.initOutputs(numChannels, rate)
      ;;  }
      ;; }

      {:name "Silent",
       :args [{:name "numChannels", :default 1}],
       :rates #{:ar}}])

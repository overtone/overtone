(ns overtone.core.ugens.pan
  (:use (overtone.core ugens-common)))

;; Panner : MultiOutUGen {
;;  checkNInputs { arg n;
;;    if (rate == 'audio') {
;;      n.do {| i |
;;        if (inputs.at(i).rate != 'audio') {
;;          //"failed".postln;
;;          ^("input " ++ i ++ " is not audio rate: " + inputs.at(i) + inputs.at(0).rate);
;;        };
;;      };
;;     };
;;      ^this.checkValidInputs
;;    }
;;    checkInputs { ^this.checkNInputs(1) }
;; }

;; XFade : UGen {
;;  checkNInputs { arg n;
;;    if (rate == 'audio') {
;;      n.do {| i |
;;        if (inputs.at(i).rate != 'audio') {
;;          ^("input " ++ i ++ " is not audio rate: " + inputs.at(i) + inputs.at(0).rate);
;;        };
;;      };
;;     };
;;      ^nil
;;    }
;; }

(def specs
     (map
       #(assoc % :check (when-ar (first-input-ar)))
      [
       ;; Pan2 : Panner {
       ;;  *ar { arg in, pos = 0.0, level = 1.0;
       ;;    ^this.multiNew('audio', in, pos, level )
       ;;  }
       ;;  *kr { arg in, pos = 0.0, level = 1.0;
       ;;    ^this.multiNew('control', in, pos, level )
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

       {:name "Pan2",
        :args [{:name "in"}
               {:name "pos", :default 0.0}
               {:name "level", :default 1.0}]
        :num-outs 2}
       
       ;; LinPan2 : Pan2 {}

       {:name "LinPan2" :extends "Pan2"}
       
       ;; Pan4 : Panner {
       
       ;;  *ar { arg in, xpos = 0.0, ypos = 0.0, level = 1.0;
       ;;    ^this.multiNew('audio', in, xpos, ypos, level )
       ;;  }
       ;;  *kr { arg in, xpos = 0.0, ypos = 0.0, level = 1.0;
       ;;    ^this.multiNew('control', in, xpos, ypos, level )
       ;;  }
       ;;  init { arg ... theInputs;
       ;;    inputs = theInputs;   
       ;;    channels = [ OutputProxy(rate,this, 0), OutputProxy(rate,this, 1),
       ;;          OutputProxy(rate,this, 2), OutputProxy(rate,this, 3) ];
       ;;    ^channels
       ;;  }
       ;; }

       {:name "Pan4",
        :args [{:name "in"}
               {:name "xpos", :default 0.0}
               {:name "ypos", :default 0.0}
               {:name "level", :default 1.0}]
        :num-outs 4}
       
       ;; Balance2 : Panner {
       ;;  *ar { arg left, right, pos = 0.0, level = 1.0;
       ;;    ^this.multiNew('audio', left, right, pos, level )
       ;;  }
       ;;  *kr { arg left, right, pos = 0.0, level = 1.0;
       ;;    ^this.multiNew('control', left, right, pos, level )
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

       {:name "Balance2",
        :args [{:name "left"}
               {:name "right"}
               {:name "pos", :default 0.0}
               {:name "level", :default 1.0}]
        :num-outs 2}
       
       ;; Rotate2 : Panner {
       ;;  *ar { arg x, y, pos = 0.0;
       ;;    ^this.multiNew('audio', x, y, pos )
       ;;  }
       ;;  *kr { arg x, y, pos = 0.0;
       ;;    ^this.multiNew('control', x, y, pos )
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

       {:name "Rotate2",
        :args [{:name "x"}
               {:name "y"}
               {:name "pos", :default 0.0}]
        :num-outs 2}

       ;; PanB : Panner {
       ;;  *ar { arg in, azimuth=0, elevation=0, gain=1;
       ;;    ^this.multiNew('audio', in, azimuth, elevation, gain )
       ;;  }
       ;;  *kr { arg in, azimuth=0, elevation=0, gain=1;
       ;;    ^this.multiNew('control', in, azimuth, elevation, gain )
       ;;  }
       ;;  init { arg ... theInputs;
       ;;    inputs = theInputs;   
       ;;    channels = [ OutputProxy(rate,this,0), OutputProxy(rate,this,1),
       ;;          OutputProxy(rate,this,2), OutputProxy(rate,this,3) ];
       ;;    ^channels
       ;;  }
       ;; }

       {:name "PanB",
        :args [{:name "in"}
               {:name "azimuth", :default 0.0}
               {:name "elevation", :default 0.0}
               {:name "gain", :default 1.0}]
        :num-outs 4}
       
       ;; PanB2 : Panner {
       ;;  *ar { arg in, azimuth=0, gain=1;
       ;;    ^this.multiNew('audio', in, azimuth, gain )
       ;;  }
       ;;  *kr { arg in, azimuth=0, gain=1;
       ;;    ^this.multiNew('control', in, azimuth, gain )
       ;;  }
       ;;  init { arg ... theInputs;
       ;;    inputs = theInputs;   
       ;;    channels = [ OutputProxy(rate,this,0), OutputProxy(rate,this,1),
       ;;          OutputProxy(rate,this,2) ];
       ;;    ^channels
       ;;  }
       ;; }

       {:name "PanB2",
        :args [{:name "in"}
               {:name "azimuth", :default 0.0}
               {:name "gain", :default 1.0}]
        :num-outs 3}
       
       ;; BiPanB2 : Panner {   
       ;;  *ar { arg inA, inB, azimuth, gain=1;
       ;;    ^this.multiNew('audio', inA, inB, azimuth, gain )
       ;;  }
       ;;  *kr { arg inA, inB, azimuth, gain=1;
       ;;    ^this.multiNew('control', inA, inB, azimuth, gain )
       ;;  }
       ;;  init { arg ... theInputs;
       ;;    inputs = theInputs;   
       ;;    channels = [ OutputProxy(rate,this,0), OutputProxy(rate,this,1),
       ;;          OutputProxy(rate,this,2) ];
       ;;    ^channels
       ;;  }
       ;;    checkInputs { ^this.checkNInputs(2) }
       ;; }

       {:name "BiPanB2",
        :args [{:name "inA"}
               {:name "inB"}
               {:name "azimuth"}
               {:name "gain", :default 1.0}]
        :num-outs 3
        :check (when-ar (first-n-inputs-ar 2))}
       
       ;; DecodeB2 : Panner {
       ;;  *ar { arg numChans, w, x, y, orientation = 0.5;
       ;;    ^this.multiNew('audio', numChans, w, x, y, orientation = 0.5 )
       ;;  }
       ;;  *kr { arg numChans, w, x, y, orientation = 0.5;
       ;;    ^this.multiNew('control', numChans, w, x, y, orientation = 0.5 )
       ;;  }
       ;;  init { arg numChans ... theInputs;
       ;;    inputs = theInputs;   
       ;;    channels = Array.fill(numChans, { arg i; OutputProxy(rate,this, i) });
       ;;    ^channels
       ;;  }
       ;;    checkInputs { ^this.checkNInputs(3) }
       ;; }

       {:name "DecodeB2",
        :args [{:name "numChannels" :mode :num-outs}
               {:name "w"}
               {:name "x"}
               {:name "y"}
               {:name "orientation", :default 0.5}]
        :check (when-ar (first-n-inputs-ar 3))}
       
       ;; PanAz : Panner {
       ;;  *ar { arg numChans, in, pos = 0.0, level = 1.0, width = 2.0, orientation = 0.5;
       ;;    ^this.multiNew('audio', numChans, in, pos, level, width, orientation )
       ;;  }
       ;;  *kr { arg numChans, in, pos = 0.0, level = 1.0, width = 2.0, orientation = 0.5;
       ;;    ^this.multiNew('control', numChans, in, pos, level, width, orientation )
       ;;  }
       ;;  init { arg numChans ... theInputs;
       ;;    inputs = theInputs;   
       ;;    channels = Array.fill(numChans, { arg i; OutputProxy(rate,this, i) });
       ;;    ^channels
       ;;  }
       ;; }

       {:name "PanAz",
        :args [{:name "numChannels" :mode :num-outs}
               {:name "in"}
               {:name "pos", :default 0.0}
               {:name "level", :default 1.0}
               {:name "width", :default 2.0}
               {:name "orientation", :default 0.5}]}  

       ;; XFade2 : XFade {
       ;;  // equal power two channel cross fade
       ;;  *ar { arg inA, inB = 0.0, pan = 0.0, level = 1.0;
       ;;    ^this.multiNew('audio', inA, inB, pan, level)
       ;;  }
       ;;  *kr { arg inA, inB = 0.0, pan = 0.0, level = 1.0;
       ;;    ^this.multiNew('control', inA, inB, pan, level)
       ;;  }
       ;;    checkInputs { ^this.checkNInputs(2) }
       ;; }

       ;; TODO why is inB default 0.0 in sc code if checkNInputs(2)?
       {:name "XFade2",
        :args [{:name "inA"}
               {:name "inB"}
               {:name "pan", :default 0.0}
               {:name "level", :default 1.0}]
        :check (when-ar (first-n-inputs-ar 2))}
       
       ;; LinXFade2 : XFade {
       ;;  // linear two channel cross fade
       ;;  *ar { arg inA, inB = 0.0, pan = 0.0, level = 1.0;
       ;;    ^this.multiNew('audio', inA, inB, pan) * level
       ;;  }
       ;;  *kr { arg inA, inB = 0.0, pan = 0.0, level = 1.0;
       ;;    ^this.multiNew('control', inA, inB, pan) * level
       ;;  }
       ;;    checkInputs { ^this.checkNInputs(2) }
       ;; }

       ;; TODO MAYBE? level not supported as of yet
       {:name "LinXFade2",
        :args [{:name "inA"}
               {:name "inB"}
               {:name "pan", :default 0.0}]
        :check (when-ar (first-n-inputs-ar 2))}]))

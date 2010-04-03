(ns overtone.core.ugen.fft
  (:use (overtone.core.ugen common)))

(def specs
     [
      ;; // fft uses a local buffer for holding the buffered audio.
      ;; // wintypes are defined in the C++ source. 0 is default, Welch; 1 is Hann; -1 is rect.

      ;; FFT : PV_ChainUGen 
      ;; {
      ;;   *new { | buffer, in = 0.0 , hop = 0.5, wintype = 0 , active = 1, winsize=0|
      ;;     ^this.multiNew('control', buffer, in, hop, wintype, active, winsize)
      ;;   }
      ;; }
      
      {:name "FFT",
       :args [{:name "buffer"}
              {:name "in", :default 0.0}
              {:name "hop", :default 0.5}
              {:name "wintype", 
               :default :welch, 
               :map {:welch 0, :hann 1, :rect -1}}
              {:name "active", :default 1}
              {:name "winsize", :default 0}],
       :rates #{:kr}
       :doc "fast fourier transform"}
      
      ;; IFFT : UGen 
      ;; {
      ;;   *new { | buffer, wintype = 0, winsize=0|
      ;;     ^this.ar(buffer, wintype, winsize)
      ;;   }      
      ;;   *ar { | buffer, wintype = 0, winsize=0|
      ;;     ^this.multiNew('audio', buffer, wintype, winsize)
      ;;   }
      ;;   *kr { | buffer, wintype = 0, winsize=0|
      ;;     ^this.multiNew('control', buffer, wintype, winsize)
      ;;   }
      ;; }

      {:name "IFFT",
       :args [{:name "buffer"}
              {:name "wintype", 
               :default :welch, 
               :map {:welch 0, :hann 1, :rect -1}}
              {:name "winsize", :default 0}]
       :doc "inverse fast fourier transform"}
      
      ;; PV_MagAbove : PV_ChainUGen
      ;; {
      ;;   *new { arg buffer, threshold = 0.0;
      ;;     ^this.multiNew('control', buffer, threshold)
      ;;   }
      ;; }

      {:name "PV_MagAbove",
       :args [{:name "buffer"}
              {:name "threshold", :default 0.0}],
       :rates #{:kr}
       :doc "passes only bins whose magnitude is above a threshold"}
      
      ;; PV_MagBelow : PV_MagAbove {}

      {:name "PV_MagBelow" :extends "PV_MagAbove"
       :doc "passes only bins whose magnitude is below a threshold"}
      
      ;; PV_MagClip : PV_MagAbove {}

      {:name "PV_MagClip" :extends "PV_MagAbove"
       :doc "clips bin magnitudes to a maximum threshold"}
      
      ;; PV_LocalMax : PV_MagAbove {}

      {:name "PV_LocalMaxw" :extends "PV_MagAbove"
       :doc "passes only bins whose magnitude is above a threshold and above their nearest neighbors"}
      
      ;; PV_MagSmear : PV_ChainUGen
      ;; {
      ;;   *new { arg buffer, bins = 0.0;
      ;;     ^this.multiNew('control', buffer, bins)
      ;;   }
      ;; }

      {:name "PV_MagSmear",
       :args [{:name "buffer"}
              {:name "bins", :default 0.0}],
       :rates #{:kr}
       :doc "average a bin's magnitude with its neighbors"}

      ;; PV_BinShift : PV_ChainUGen 
      ;; {
      ;;   *new { arg buffer, stretch = 1.0, shift = 0.0;
      ;;     ^this.multiNew('control', buffer, stretch, shift)
      ;;   }
      ;; }

      {:name "PV_BinShift",
       :args [{:name "buffer"}
              {:name "stretch", :default 1.0}
              {:name "shift", :default 0.0}],
       :rates #{:kr}
       :doc "shift and scale the positions of the bins"}
      
      ;; PV_MagShift : PV_BinShift {}

      {:name "PV_MagShift" :extends "PV_BinShift"
       :doc "shift and stretch the positions of only the magnitude of the bins"}
      
      ;; PV_MagSquared : PV_ChainUGen 
      ;; {
      ;;   *new { arg buffer;
      ;;     ^this.multiNew('control', buffer)
      ;;   }
      ;; }

      {:name "PV_MagSquared",
       :args [{:name "buffer"}],
       :rates #{:kr}
       :doc "squares the magnitudes and renormalizes to previous peak"}

      ;; PV_MagNoise : PV_MagSquared {}

      {:name "PV_MagNoise" :extends "PV_MagSquared"
       :doc "magnitudes are multiplied with noise"}
      
      ;; PV_PhaseShift90 : PV_MagSquared {}

      {:name "PV_PhaseShift90" :extends "PV_MagSquared"
       :doc "shift phase of all bins by 90 degrees"}
      
      ;; PV_PhaseShift270 : PV_MagSquared {}

      {:name "PV_PhaseShift270" :extends "PV_MagSquared"
       :doc "shift phase of all bins by 270 degrees"}
      
      ;; PV_Conj : PV_MagSquared {}

      {:name "PV_Conj" :extends "PV_MagSquared"
       :doc "converts the FFT frames to their complex conjugate"}
      
      ;; PV_PhaseShift : PV_ChainUGen 
      ;; {
      ;;   *new { arg buffer, shift;
      ;;     ^this.multiNew('control', buffer, shift)
      ;;   }
      ;; } 

      {:name "PV_PhaseShift",
       :args [{:name "buffer"}
              {:name "shift"}],
       :rates #{:kr}
       :doc "shift phase of all bins"}
      
      ;; PV_BrickWall : PV_ChainUGen
      ;; {
      ;;   *new { arg buffer, wipe = 0.0;
      ;;     ^this.multiNew('control', buffer, wipe)
      ;;   }
      ;; }

      {:name "PV_BrickWall",
       :args [{:name "buffer"}
              {:name "wipe", :default 0.0}],
       :rates #{:kr}
       :doc "clears bins above or below a cutoff point"}

      ;; PV_BinWipe : PV_ChainUGen 
      ;; {
      ;;   *new { arg bufferA, bufferB, wipe = 0.0;
      ;;     ^this.multiNew('control', bufferA, bufferB, wipe)
      ;;   }
      ;; }

      {:name "PV_BinWipe",
       :args [{:name "bufferA"}
              {:name "bufferB"}
              {:name "wipe", :default 0.0}],
       :rates #{:kr}
       :doc "copies low bins from one input and the high bins of the other"}
      
      ;; PV_MagMul : PV_ChainUGen
      ;; {
      ;;   *new { arg bufferA, bufferB;
      ;;     ^this.multiNew('control', bufferA, bufferB)
      ;;   }
      ;; }

      {:name "PV_MagMul",
       :args [{:name "bufferA"}
              {:name "bufferB"}],
       :rates #{:kr}
       :doc "multiplies magnitudes of two inputs and keeps the phases of the first input"}
      
      ;; PV_CopyPhase : PV_MagMul {}

      {:name "PV_CopyPhase" :extends "PV_MagMul"
       :doc "combines magnitudes of first input and phases of the second input"}
      
      ;; PV_Copy : PV_MagMul {}

      {:name "PV_Copy" :extends "PV_MagMul"
       :doc "copies the spectral frame in bufferA to bufferB at that point in the chain of PV UGens"}
      
      ;; PV_Max : PV_MagMul {}

      {:name "PV_Max" :extends "PV_MagMul"
       :doc "output copies bins with the maximum magnitude of the two inputs"}
      
      ;; PV_Min : PV_MagMul {}

      {:name "PV_Min" :extends "PV_MagMul"
       :doc "output copies bins with the minimum magnitude of the two inputs"}
      
      ;; PV_Mul : PV_MagMul {}

      {:name "PV_Mul" :extends "PV_MagMul"
       :doc "complex multiplication: (RealA * RealB) - (ImagA * ImagB), (ImagA * RealB) + (RealA * ImagB)"}
      
      ;; PV_Div : PV_MagMul {}

      {:name "PV_Div" :extends "PV_MagMul"
       :doc "complex division"}
      
      ;; PV_Add : PV_MagMul {}

      {:name "PV_Add" :extends "PV_MagMul"
       :doc "complex addition: RealA + RealB, ImagA + ImagB"}

      ;; PV_MagDiv : PV_ChainUGen
      ;; {
      ;;   *new { arg bufferA, bufferB, zeroed = 0.0001;
      ;;     ^this.multiNew('control', bufferA, bufferB, zeroed)
      ;;   }
      ;; }

      {:name "PV_MagDiv",
       :args [{:name "bufferA"}
              {:name "bufferB"}
              {:name "zeroed", :default 0.0001}],
       :rates #{:kr}
       :doc "divides magnitudes of two inputs and keeps the phases of the first input"}
      
      ;; PV_RandComb : PV_ChainUGen 
      ;; {
      ;;   *new { arg buffer, wipe = 0.0, trig = 0.0;
      ;;     ^this.multiNew('control', buffer, wipe, trig)
      ;;   }
      ;; }

      {:name "PV_RandComb",
       :args [{:name "buffer"}
              {:name "wipe", :default 0.0}
              {:name "trig", :default 0.0}],
       :rates #{:kr}
       :doc "randomly clear bins"}
      
      ;; PV_RectComb : PV_ChainUGen 
      ;; {
      ;;   *new { arg buffer, numTeeth = 0.0, phase = 0.0, width = 0.5;
      ;;     ^this.multiNew('control', buffer, numTeeth, phase, width)
      ;;   }
      ;; }

      {:name "PV_RectComb",
       :args [{:name "buffer"}
              {:name "numTeeth", :default 0.0}
              {:name "phase", :default 0.0}
              {:name "width", :default 0.5}],
       :rates #{:kr}
       :doc "makes a series of gaps in a spectrum"}

      ;; PV_RectComb2 : PV_ChainUGen 
      ;; {
      ;;   *new { arg bufferA, bufferB, numTeeth = 0.0, phase = 0.0, width = 0.5;
      ;;     ^this.multiNew('control', bufferA, bufferB, numTeeth, phase, width)
      ;;   }
      ;; }

      {:name "PV_RectComb2",
       :args [{:name "bufferA"}
              {:name "bufferB"}
              {:name "numTeeth", :default 0.0}
              {:name "phase", :default 0.0}
              {:name "width", :default 0.5}],
       :rates #{:kr}
       :doc "alternates blocks of bins between the two inputs"}
      
      ;; PV_RandWipe : PV_ChainUGen 
      ;; {
      ;;   *new { arg bufferA, bufferB, wipe = 0.0, trig = 0.0;
      ;;     ^this.multiNew('control', bufferA, bufferB, wipe, trig)
      ;;   }
      ;; }

      {:name "PV_RandWipe",
       :args [{:name "bufferA"}
              {:name "bufferB"}
              {:name "wipe", :default 0.0}
              {:name "trig", :default 0.0}],
       :rates #{:kr}
       :doc "cross fades between two sounds by copying bins in a random order"}
      
      ;; PV_Diffuser : PV_ChainUGen
      ;; {
      ;;   *new { arg buffer, trig = 0.0;
      ;;     ^this.multiNew('control', buffer, trig)
      ;;   }
      ;; }

      {:name "PV_Diffuser",
       :args [{:name "buffer"}
              {:name "trig", :default 0.0}],
       :rates #{:kr}
       :doc "adds a different constant random phase shift to each bin"}
      
      ;; PV_MagFreeze : PV_ChainUGen
      ;; {
      ;;   *new { arg buffer, freeze = 0.0;
      ;;     ^this.multiNew('control', buffer, freeze)
      ;;   }
      ;; }

      {:name "PV_MagFreeze",
       :args [{:name "buffer"}
              {:name "freeze", :default 0.0}],
       :rates #{:kr}
       :doc "freezes magnitudes at current levels when freeze > 0"}
      
      ;; PV_BinScramble : PV_ChainUGen
      ;; {
      ;;   *new { arg buffer, wipe = 0.0, width = 0.2, trig = 0.0;
      ;;     ^this.multiNew('control', buffer, wipe, width, trig)
      ;;   }
      ;; }

      {:name "PV_BinScramble",
       :args [{:name "buffer"}
              {:name "wipe", :default 0.0}
              {:name "width", :default 0.2}
              {:name "trig", :default 0.0}],
       :rates #{:kr}
       :doc "randomizes the order of the bins"}
      
      ;; FFTTrigger : PV_ChainUGen 
      ;; {
      ;;   *new { | buffer, hop = 0.5, polar = 0.0|
      ;;     ^this.multiNew('control', buffer, hop, polar)
      ;;   }
      ;; } 

      {:name "FFTTrigger",
       :args [{:name "buffer"}
              {:name "hop", :default 0.5}
              {:name "polar", :default 0.0}],
       :rates #{:kr}
       :doc "Outputs the necessary signal for FFT chains, without doing an FFT on a signal"}
      ])
;; ////////////////////////////////////////////////////
;; /*
;; PV_OscBank : PV_ChainUGen 
;; {
;;   *new { arg buffer, scale;
;;     ^this.multiNew('control', buffer)
;;   }
;; } 

;; PV_Scope : PV_ChainUGen {}

;; PV_TimeAverageScope : PV_Scope {}

;; PV_MagAllTimeAverage : PV_MagSquared {}





;; PV_MagOnePole : PV_ChainUGen
;; {
;;   *new { arg buffer, feedback = 0.0;
;;     ^this.multiNew('control', buffer, feedback)
;;   }
;; }

;; PV_MagPeakDecay : PV_ChainUGen
;; {
;;   *new { arg buffer, decay = 0.0;
;;     ^this.multiNew('control', buffer, decay)
;;   }
;; }

;; PV_TimeSmear : PV_MagSmear {}

;; PV_LoBitEncoder : PV_ChainUGen
;; {
;;   *new { arg buffer, levels = 4.0;
;;     ^this.multiNew('control', buffer, levels)
;;   }
;; }
;; */

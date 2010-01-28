(ns overtone.core.ugens.fft
  (:use (overtone.core ugens-common)))

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
       :rates #{:kr}}
      
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
              {:name "winsize", :default 0}]}
      
      ;; PV_MagAbove : PV_ChainUGen
      ;; {
      ;;   *new { arg buffer, threshold = 0.0;
      ;;     ^this.multiNew('control', buffer, threshold)
      ;;   }
      ;; }

      {:name "PV_MagAbove",
       :args [{:name "buffer"}
              {:name "threshold", :default 0.0}],
       :rates #{:kr}}
      
      ;; PV_MagBelow : PV_MagAbove {}

      {:name "PV_MagBelow" :extends "PV_MagAbove"}
      
      ;; PV_MagClip : PV_MagAbove {}

      {:name "PV_MagClip" :extends "PV_MagAbove"}
      
      ;; PV_LocalMax : PV_MagAbove {}

      {:name "PV_LocalMaxw" :extends "PV_MagAbove"}
      
      ;; PV_MagSmear : PV_ChainUGen
      ;; {
      ;;   *new { arg buffer, bins = 0.0;
      ;;     ^this.multiNew('control', buffer, bins)
      ;;   }
      ;; }

      {:name "PV_MagSmear",
       :args [{:name "buffer"}
              {:name "bins", :default 0.0}],
       :rates #{:kr}}

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
       :rates #{:kr}}
      
      ;; PV_MagShift : PV_BinShift {}

      {:name "PV_MagShift" :extends "PV_BinShift"}
      
      ;; PV_MagSquared : PV_ChainUGen 
      ;; {
      ;;   *new { arg buffer;
      ;;     ^this.multiNew('control', buffer)
      ;;   }
      ;; }

      {:name "PV_MagSquared",
       :args [{:name "buffer"}],
       :rates #{:kr}}

      ;; PV_MagNoise : PV_MagSquared {}

      {:name "PV_MagNoise" :extends "PV_MagSquared"}
      
      ;; PV_PhaseShift90 : PV_MagSquared {}

      {:name "PV_PhaseShift90" :extends "PV_MagSquared"}
      
      ;; PV_PhaseShift270 : PV_MagSquared {}

      {:name "PV_PhaseShift270" :extends "PV_MagSquared"}
      
      ;; PV_Conj : PV_MagSquared {}

      {:name "PV_Conj" :extends "PV_MagSquared"}
      
      ;; PV_PhaseShift : PV_ChainUGen 
      ;; {
      ;;   *new { arg buffer, shift;
      ;;     ^this.multiNew('control', buffer, shift)
      ;;   }
      ;; } 

      {:name "PV_PhaseShift",
       :args [{:name "buffer"}
              {:name "shift"}],
       :rates #{:kr}}
      
      ;; PV_BrickWall : PV_ChainUGen
      ;; {
      ;;   *new { arg buffer, wipe = 0.0;
      ;;     ^this.multiNew('control', buffer, wipe)
      ;;   }
      ;; }

      {:name "PV_BrickWall",
       :args [{:name "buffer"}
              {:name "wipe", :default 0.0}],
       :rates #{:kr}}

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
       :rates #{:kr}}
      
      ;; PV_MagMul : PV_ChainUGen
      ;; {
      ;;   *new { arg bufferA, bufferB;
      ;;     ^this.multiNew('control', bufferA, bufferB)
      ;;   }
      ;; }

      {:name "PV_MagMul",
       :args [{:name "bufferA"}
              {:name "bufferB"}],
       :rates #{:kr}}
      
      ;; PV_CopyPhase : PV_MagMul {}

      {:name "PV_CopyPhase" :extends "PV_MagMul"}
      
      ;; PV_Copy : PV_MagMul {}

      {:name "PV_Copy" :extends "PV_MagMul"}
      
      ;; PV_Max : PV_MagMul {}

      {:name "PV_Max" :extends "PV_MagMul"}
      
      ;; PV_Min : PV_MagMul {}

      {:name "PV_Min" :extends "PV_MagMul"}
      
      ;; PV_Mul : PV_MagMul {}

      {:name "PV_Mul" :extends "PV_MagMul"}
      
      ;; PV_Div : PV_MagMul {}

      {:name "PV_Div" :extends "PV_MagMul"}
      
      ;; PV_Add : PV_MagMul {}

      {:name "PV_Add" :extends "PV_MagMul"}

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
       :rates #{:kr}}
      
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
       :rates #{:kr}}
      
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
       :rates #{:kr}}

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
       :rates #{:kr}}
      
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
       :rates #{:kr}}
      
      ;; PV_Diffuser : PV_ChainUGen
      ;; {
      ;;   *new { arg buffer, trig = 0.0;
      ;;     ^this.multiNew('control', buffer, trig)
      ;;   }
      ;; }

      {:name "PV_Diffuser",
       :args [{:name "buffer"}
              {:name "trig", :default 0.0}],
       :rates #{:kr}}
      
      ;; PV_MagFreeze : PV_ChainUGen
      ;; {
      ;;   *new { arg buffer, freeze = 0.0;
      ;;     ^this.multiNew('control', buffer, freeze)
      ;;   }
      ;; }

      {:name "PV_MagFreeze",
       :args [{:name "buffer"}
              {:name "freeze", :default 0.0}],
       :rates #{:kr}}
      
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
       :rates #{:kr}}
      
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
       :rates #{:kr}}])

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

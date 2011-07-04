(ns overtone.sc.ugen.fft
  (:use (overtone.sc.ugen common)))

(def specs
     [
      {:name "FFT",
       :args [{:name "buffer"}
              {:name "in", :default 0.0}
              {:name "hop", :default 0.5}
              {:name "wintype", :default :welch, :map {:welch 0, :hann 1, :rect -1}}
              {:name "active", :default 1}
              {:name "winsize", :default 0}],
       :rates #{:kr}
       :doc "fast fourier transform, converts input data from the time to the frequency domain and stores the result in a buffer (audio waveform -> graph equalizer bands)

Output is -1 except when an FFT frame is ready, when the output is the buffer index. This creates a special kind of slower pseudo-rate (built on top of control rate) which all the pv-ugens understand."}

      {:name "IFFT",
       :args [{:name "buffer"}
              {:name "wintype",
               :default :welch,
               :map {:welch 0, :hann 1, :rect -1}}
              {:name "winsize", :default 0}]
       :doc "inverse fast fourier transform, converts buffer data from frequency domain to time domain"}

      {:name "PV_MagAbove",
       :args [{:name "buffer"}
              {:name "threshold", :default 0.0}],
       :rates #{:kr}
       :doc "passes only bins whose magnitude is above a threshold"}

      {:name "PV_MagBelow" :extends "PV_MagAbove"
       :doc "passes only bins whose magnitude is below a threshold"}

      {:name "PV_MagClip" :extends "PV_MagAbove"
       :doc "clips bin magnitudes to a maximum threshold"}

      {:name "PV_LocalMax" :extends "PV_MagAbove"
       :doc "passes only bins whose magnitude is above a threshold and above their nearest neighbors"}

      {:name "PV_MagSmear",
       :args [{:name "buffer"}
              {:name "bins", :default 0.0}],
       :rates #{:kr}
       :doc "average a bin's magnitude with its neighbors"}

      {:name "PV_BinShift",
       :args [{:name "buffer"}
              {:name "stretch", :default 1.0}
              {:name "shift", :default 0.0}],
       :rates #{:kr}
       :doc "shift and scale the positions of the bins"}

      {:name "PV_MagShift" :extends "PV_BinShift"
       :doc "shift and stretch the positions of only the magnitude of the bins"}

      {:name "PV_MagSquared",
       :args [{:name "buffer"}],
       :rates #{:kr}
       :doc "squares the magnitudes and renormalizes to previous peak"}

      {:name "PV_MagNoise" :extends "PV_MagSquared"
       :doc "magnitudes are multiplied with noise"}

      {:name "PV_PhaseShift90" :extends "PV_MagSquared"
       :doc "shift phase of all bins by 90 degrees"}

      {:name "PV_PhaseShift270" :extends "PV_MagSquared"
       :doc "shift phase of all bins by 270 degrees"}

      {:name "PV_Conj" :extends "PV_MagSquared"
       :doc "converts the FFT frames to their complex conjugate"}

      {:name "PV_PhaseShift",
       :args [{:name "buffer"}
              {:name "shift"}],
       :rates #{:kr}
       :doc "shift phase of all bins"}

      {:name "PV_BrickWall",
       :args [{:name "buffer"}
              {:name "wipe", :default 0.0}],
       :rates #{:kr}
       :doc "clears bins above or below a cutoff point"}

      {:name "PV_BinWipe",
       :args [{:name "bufferA"}
              {:name "bufferB"}
              {:name "wipe", :default 0.0}],
       :rates #{:kr}
       :doc "copies low bins from one input and the high bins of the other"}

      {:name "PV_MagMul",
       :args [{:name "bufferA"}
              {:name "bufferB"}],
       :rates #{:kr}
       :doc "multiplies magnitudes of two inputs and keeps the phases of the first input"}

      {:name "PV_CopyPhase" :extends "PV_MagMul"
       :doc "combines magnitudes of first input and phases of the second input"}

      {:name "PV_Copy" :extends "PV_MagMul"
       :doc "copies the spectral frame in bufferA to bufferB at that point in the chain of PV UGens"}

      {:name "PV_Max" :extends "PV_MagMul"
       :doc "output copies bins with the maximum magnitude of the two inputs"}

      {:name "PV_Min" :extends "PV_MagMul"
       :doc "output copies bins with the minimum magnitude of the two inputs"}

      {:name "PV_Mul" :extends "PV_MagMul"
       :doc "complex multiplication: (RealA * RealB) - (ImagA * ImagB), (ImagA * RealB) + (RealA * ImagB)"}

      {:name "PV_Div" :extends "PV_MagMul"
       :doc "complex division"}

      {:name "PV_Add" :extends "PV_MagMul"
       :doc "complex addition: RealA + RealB, ImagA + ImagB"}

      {:name "PV_MagDiv",
       :args [{:name "bufferA"}
              {:name "bufferB"}
              {:name "zeroed", :default 0.0001}],
       :rates #{:kr}
       :doc "divides magnitudes of two inputs and keeps the phases of the first input"}

      {:name "PV_RandComb",
       :args [{:name "buffer"}
              {:name "wipe", :default 0.0}
              {:name "trig", :default 0.0}],
       :rates #{:kr}
       :doc "randomly clear bins"}

      {:name "PV_RectComb",
       :args [{:name "buffer"}
              {:name "numTeeth", :default 0.0}
              {:name "phase", :default 0.0}
              {:name "width", :default 0.5}],
       :rates #{:kr}
       :doc "makes a series of gaps in a spectrum"}

      {:name "PV_RectComb2",
       :args [{:name "bufferA"}
              {:name "bufferB"}
              {:name "numTeeth", :default 0.0}
              {:name "phase", :default 0.0}
              {:name "width", :default 0.5}],
       :rates #{:kr}
       :doc "alternates blocks of bins between the two inputs"}

      {:name "PV_RandWipe",
       :args [{:name "bufferA"}
              {:name "bufferB"}
              {:name "wipe", :default 0.0}
              {:name "trig", :default 0.0}],
       :rates #{:kr}
       :doc "cross fades between two sounds by copying bins in a random order"}

            {:name "PV_Diffuser",
       :args [{:name "buffer"}
              {:name "trig", :default 0.0}],
       :rates #{:kr}
       :doc "adds a different constant random phase shift to each bin"}

      {:name "PV_MagFreeze",
       :args [{:name "buffer"}
              {:name "freeze", :default 0.0}],
       :rates #{:kr}
       :doc "freezes magnitudes at current levels when freeze > 0"}

           {:name "PV_BinScramble",
       :args [{:name "buffer"}
              {:name "wipe", :default 0.0}
              {:name "width", :default 0.2}
              {:name "trig", :default 0.0}],
       :rates #{:kr}
       :doc "randomizes the order of the bins"}

           {:name "FFTTrigger",
       :args [{:name "buffer"}
              {:name "hop", :default 0.5}
              {:name "polar", :default 0.0}],
       :rates #{:kr}
       :doc "Outputs the necessary signal for FFT chains, without doing an FFT on a signal"}
      ])

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

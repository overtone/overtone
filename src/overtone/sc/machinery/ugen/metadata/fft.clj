(ns overtone.sc.machinery.ugen.metadata.fft
  (:use [overtone.sc.machinery.ugen common check]))

(def specs
     [
      {:name "FFT",
       :args [{:name "buffer" :doc "The buffer where a frame will be held. Its size must be a power of two. local-buf is useful here, because processes should not share data between synths. (Note: most PV UGens operate on this data in place."}
              {:name "in", :default 0.0 :doc "the signal to be analyzed. The signal's rate determines the rate at which the input is read."}
              {:name "hop", :default 0.5 :doc "the amount of offset from one FFT analysis frame to the next, measured in multiples of the analysis frame size. This can range between zero and one, and the default is 0.5 (meaning each frame has a 50% overlap with the preceding/following frames)."}
              {:name "wintype", :default 0 :doc "defines how the data is windowed: RECT is for rectangular windowing, simple but typically not recommended; SINE (the default) is for Sine windowing, typically recommended for phase-vocoder work; HANN is for Hann windowing, typically recommended for analysis work."}
              {:name "active", :default 1 :doc "is a simple control allowing FFT analysis to be active (>0) or inactive (<=0). This is mainly useful for signal analysis processes which are only intended to analyse at specific times rather than continuously"}
              {:name "winsize", :default 0 :doc "the windowed audio frames are usually the same size as the buffer. If you wish the FFT to be zero-padded then you can specify a window size smaller than the actual buffer size (e.g. window size 1024 with buffer size 2048). Both values must still be a power of two. Leave this at its default of zero for no zero-padding."}],
       :rates #{:kr}
       :doc "fast fourier transform, converts input data from the time to the frequency domain and stores the result in a buffer (audio waveform -> graph equalizer bands)

Output is -1 except when an FFT frame is ready, when the output is the buffer index. This creates a special kind of slower pseudo-rate (built on top of control rate) which all the pv-ugens understand."}

      {:name "IFFT",
       :args [{:name "chain" :doc "The FFT chain signal coming originally from an FFT UGen, perhaps via other PV UGens."}
              {:name "wintype",
               :default 0
               :doc "defines how the data is windowed: RECT is for rectangular windowing, simple but typically not recommended;  SINE (the default) is for Sine windowing, typically recommended for phase-vocoder work; HANN is for Hann windowing, typically recommended for analysis work."}
              {:name "winsize", :default 0 :doc "can be used to account for zero-padding, in the same way as the FFT UGen."}]
       :doc "inverse fast fourier transform, converts buffer data from frequency domain to time domain

The IFFT UGen converts the FFT data in-place (in the original FFT buffer) and overlap-adds the result to produce a continuous signal at its output."}

      {:name "PV_MagAbove",
       :args [{:name "buffer" :doc "fft buffer"}
              {:name "threshold", :default 0.0 :doc "magnitude threshold."}],
       :rates #{:kr}
       :doc "passes only bins whose magnitude is above a threshold"}

      {:name "PV_MagBelow" :extends "PV_MagAbove"
       :doc "passes only bins whose magnitude is below a threshold"}

      {:name "PV_MagClip" :extends "PV_MagAbove"
       :doc "clips bin magnitudes to a maximum threshold"}

      {:name "PV_LocalMax" :extends "PV_MagAbove"
       :doc "passes only bins whose magnitude is above a threshold and above their nearest neighbors"}

      {:name "PV_MagSmear",
       :args [{:name "buffer" :doc "fft buffer"}
              {:name "bins", :default 0.0 :doc "number of bins to average on each side of bin. As this number rises, so will CPU usage."}],
       :rates #{:kr}
       :doc "average a bin's magnitude with its neighbors"}

      {:name "PV_BinShift",
       :args [{:name "buffer" :doc "fft buffer."}
              {:name "stretch", :default 1.0 :doc "scale bin location by factor."}
              {:name "shift", :default 0.0 :doc "add an offset to bin position."}],
       :rates #{:kr}
       :doc "shift and scale the positions of the bins. Can be used as a very crude frequency shifter/scaler."}

      {:name "PV_MagShift" :extends "PV_BinShift"
       :doc "shift and stretch the positions of only the magnitude of the bins. Can be used as a very crude frequency shifter/scaler."}

      {:name "PV_MagSquared",
       :args [{:name "buffer" :doc "fft buffer"}],
       :rates #{:kr}
       :doc "squares the magnitudes and renormalizes to previous peak. This makes weak bins weaker."}

      {:name "PV_MagNoise" :extends "PV_MagSquared"
       :doc "magnitudes are multiplied with noise"}

      {:name "PV_PhaseShift90" :extends "PV_MagSquared"
       :doc "shift phase of all bins by 90 degrees"}

      {:name "PV_PhaseShift270" :extends "PV_MagSquared"
       :doc "shift phase of all bins by 270 degrees"}

      {:name "PV_Conj" :extends "PV_MagSquared"
       :doc "converts the FFT frames to their complex conjugate (i.e. reverses the sign of their imaginary part). This is not usually a useful audio effect in itself, but may be a component of other analysis or transformation processes..."}

      {:name "PV_PhaseShift",
       :args [{:name "buffer" :doc "fft buffer"}
              {:name "shift" :doc "phase shift in radians"}],
       :rates #{:kr}
       :doc "shift phase of all bins"}

      {:name "PV_BrickWall",
       :args [{:name "buffer" :doc "fft buffer."}
              {:name "wipe", :default 0.0 :doc "can range between -1 and +1. if wipe == 0 then there is no effect; if  wipe > 0 then it acts like a high pass filter, clearing bins from the bottom up; if  wipe < 0 then it acts like a low pass filter, clearing bins from the top down.
"}],
       :rates #{:kr}
       :doc "clears bins above or below a cutoff point"}

      {:name "PV_BinWipe",
       :args [{:name "buffer-a" :doc "fft buffer A."}
              {:name "buffer-b" :doc "fft buffer B."}
              {:name "wipe", :default 0.0
               :doc "can range between -1 and +1; if wipe == 0 then the output is the same as inA; if  wipe > 0 then it begins replacing with bins from inB from the bottom up;if  wipe < 0 then it begins replacing with bins from inB from the top down."}],
       :rates #{:kr}
       :doc "copies low bins from one input and the high bins of the other"}

      {:name "PV_MagMul",
       :args [{:name "buffer-a" :doc "fft buffer A."}
              {:name "buffer-b" :doc "fft buffer B."}],
       :rates #{:kr}
       :doc "multiplies magnitudes of two inputs and keeps the phases of the first input"}

      {:name "PV_CopyPhase" :extends "PV_MagMul"
       :doc "combines magnitudes of first input and phases of the second input"}

      {:name "PV_Copy" :extends "PV_MagMul"
       :args [{:name "buffer-a" :doc "source buffer"}
              {:name "buffer-b" :doc "destination buffer"}]
       :doc "copies the spectral frame in bufferA to bufferB at that point in the chain of PV UGens. This allows for parallel processing of spectral data without the need for multiple FFT UGens, and to copy out data at that point in the chain for other purposes. bufferA and bufferB must be the same size."}

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
       :args [{:name "buffer-a" :doc "fft buffer A."}
              {:name "buffer-b" :doc "fft buffer B."}
              {:name "zeroed", :default 0.0001 :doc "number to use when bins are zeroed out, i.e. causing division by zero"}],
       :rates #{:kr}
       :doc "divides magnitudes of two inputs and keeps the phases of the first input"}

      {:name "PV_RandComb",
       :args [{:name "buffer" :doc "fft buffer."}
              {:name "wipe", :default 0.0 :doc "clears bins from input in a random order as wipe goes from 0 to 1."}
              {:name "trig", :default 0.0 :doc "a trigger selects a new random ordering."}],
       :rates #{:kr}
       :doc "randomly clear bins"}

      {:name "PV_RectComb",
       :args [{:name "buffer" :doc " fft buffer."}
              {:name "num-teeth", :default 0.0 :doc "number of teeth in the comb."}
              {:name "phase", :default 0.0 :doc "starting phase of comb pulse."}
              {:name "width", :default 0.5 :doc "pulse width of comb."}],
       :rates #{:kr}
       :doc "makes a series of gaps in a spectrum"}

      {:name "PV_RectComb2",
       :args [{:name "buffer-a" :doc "fft buffer A."}
              {:name "buffer-b" :doc "fft buffer B."}
              {:name "num-teeth", :default 0.0 :doc "number of teeth in the comb."}
              {:name "phase", :default 0.0 :doc "starting phase of comb pulse."}
              {:name "width", :default 0.5 :doc "pulse width of comb."}],
       :rates #{:kr}
       :doc "alternates blocks of bins between the two inputs"}

      {:name "PV_RandWipe",
       :args [{:name "buffer-a" :doc "fft buffer A."}
              {:name "buffer-b" :doc "fft buffer B."}
              {:name "wipe", :default 0.0 :doc "copies bins from bufferB in a random order as wipe goes from 0 to 1."}
              {:name "trig", :default 0.0 :doc "a trigger selects a new random ordering."}],
       :rates #{:kr}
       :doc "cross fades between two sounds by copying bins in a random order"}

            {:name "PV_Diffuser",
       :args [{:name "buffer" :doc "fft buffer"}
              {:name "trig", :default 0.0 :doc "a trigger selects a new set of random values."}],
       :rates #{:kr}
       :doc "adds a different constant random phase shift to each bin. The trigger will select a new set of random phases."}

      {:name "PV_MagFreeze",
       :args [{:name "buffer" :doc "fft buffer"}
              {:name "freeze", :default 0.0 :doc "if freeze > 0 then magnitudes are frozen at current levels."}],
       :rates #{:kr}
       :doc "freezes magnitudes at current levels when freeze > 0"}

           {:name "PV_BinScramble",
       :args [{:name "buffer" :doc "fft buffer"}
              {:name "wipe", :default 0.0 :doc "scrambles more bins as wipe moves from zero to one."}
              {:name "width", :default 0.2 :doc "a value from zero to one, indicating the maximum randomized distance of a bin from its
original location in the spectrum."}
              {:name "trig", :default 0.0 :doc "a trigger selects a new random ordering."}],
       :rates #{:kr}
       :doc "randomizes the order of the bins. The trigger will select a new random ordering."}

           {:name "FFTTrigger",
       :args [{:name "buffer" :doc "a buffer to condition for FFT use"}
              {:name "hop", :default 0.5 :doc "the hop size for timing triggers"}
              {:name "polar", :default 0.0 :doc "a flag. If 0.0, the buffer will be prepared for complex data, if > 0.0, polar data is set up."}],
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

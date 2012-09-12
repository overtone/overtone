(ns overtone.sc.machinery.ugen.metadata.extras.blackrain
  (:use [overtone.sc.machinery.ugen common check]))

(def specs
  [

   {:name "AmplitudeMod"
    :summary "Amplitude Follower"
    :args [{:name "input"
            :default 0
            :doc "Input signal"}

           {:name "attack-time"
            :default 0.01
            :doc "60dB convergence time for following attacks."}

           {:name "release-time"
            :default 0.01
            :doc "60dB convergence time for following decays."}]

    :rates #{:ar :kr}
    :doc "Tracks the peak amplitude of a signal.  As a opposed to
          Amplitude, AmplitudeMod, allows attack and release times to be
          modulated once the UGen has been instantiated. There is a tiny
          overhead associated to this."}

   {:name "BMoog"
    :summary "24db/oct Rolloff - 4nd Order Resonant Low/High/Band Pass Filter"
    :args [{:name "in"
            :default :none
            :doc "Input signal to be processed"}
           
           {:name "freq"
            :default 440.0
            :doc "Cutoff frequency"}

           {:name "q"
            :default 0.2
            :doc "Bandwidth/cutoff frequency. 0 < q > 1"}

           {:name "mode"
            :default 0.0
            :doc "Filter mode:
                < 1 - low pass filter.
                < 2 - high pass filter.
                < 3 - bandpass filter.
                Defaults to lowpass"}]
   :rates #{:ar}
   :doc "BlackRain's yet 'nother moog impersonation.  Oh yes.  See also: SOS RLPF RHPF BLowPass BLowPass4 BHiPass BHiPass4 BLowShelf BHiShelf BBandPass BBandStop BAllPass IIRFilter"}

   {:name "IIRFilter"
    :summary "24db/oct Rolloff, 4nd Order Resonant Low Pass Filter"
    :args [{:name "in"
            :default :none
            :doc "Input signal to be processed"}
          
           {:name "freq"
            :default 440.0
            :doc "Cutoff frequency"}

           {:name "rq"
            :default 1.0
            :doc  "The reciprocal of Q.  Bandwidth / cutoffFreq"}]
    :rates #{:ar}
    :doc "See also: SOS BLowPass BLowPass4 BHiPass BHiPass4 BLowShelf BHiShelf BBandPass BBandStop BAllPass BMoog"}

   {:name "SVF"
    :summary "12db/Oct State Variable Filter"
    :args [{:name "in"
            :default :none
            :doc "Audio in"}

           {:name "cutoff"
            :default 2200.0
            :doc "Cutoff frequency"}

           {:name "res"
            :default 0.1
            :doc "Resonance 0.0 - 1.0"}

           {:name "low"
            :default 1.0
            :doc "Lowpass filter output level 0.0 - 1.0"}

           {:name "band"
            :default 0.0
            :doc "Bandpass filter output level 0.0 - 1.0"}

           {:name "high"
            :default 0.0
            :doc "Highpass filter output level 0.0 - 1.0"}

           {:name "notch"
            :default 0.0
            :doc "Notch filter output level 0.0 - 1.0"}

           {:name "peak"
            :default 0.0
            :doc "Notch filter output level 0.0 - 1.0"}]
    :rates #{:ar :kr}
    :doc "Total, 100% Plastic..."
   }
  

])

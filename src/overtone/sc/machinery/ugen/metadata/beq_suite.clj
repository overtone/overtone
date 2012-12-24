(ns overtone.sc.machinery.ugen.metadata.beq-suite
  (:use [overtone.sc.machinery.ugen common check]))

;; BEQSuite : Filter {}

(def specs
  [

   {:name "BLowPass",
    :args [{:name "in"
            :doc "input signal to be processed"}

           {:name "freq"
            :default 1200.0
            :doc "cutoff frequency"}

           {:name "rq"
            :default 1.0
            :doc "the reciprocal of Q.  bandwidth / cutoffFreq"}]

    :rates #{:ar}
    :check (nth-input-stream? 0)
    :doc "12db/oct rolloff - 2nd order resonant Low Pass Filter based on
          the Second Order Section (SOS) biquad UGen"}


   {:name "BHiPass",
    :args [{:name "in"
            :doc "input signal to be processed"}

           {:name "freq"
            :default 1200.0
            :doc "cutoff frequency"}

           {:name "rq"
            :default 1.0
            :doc "the reciprocal of Q. bandwidth / cutoffFreq"}]
    :rates #{:ar}
    :check (nth-input-stream? 0)
    :doc "12db/oct rolloff - 2nd order resonant Hi Pass Filter based on
          the Second Order Section (SOS) biquad UGen." }


   {:name "BAllPass",
    :args [{:name "in"
            :doc "input signal to be processed."}

           {:name "freq"
            :default 1200.0
            :doc "center frequency."}

           {:name "rq"
            :default 1.0
            :doc "the reciprocal of Q.  bandwidth / cutoffFreq."}]

    :rates #{:ar}
    :check (nth-input-stream? 0)
    :doc "All pass filter based on the Second Order Section (SOS) biquad
          UGen"}


   {:name "BBandPass",
    :args [{:name "in"
            :doc "input signal to be processed"}

           {:name "freq"
            :default 1200.0 :doc "center frequency"}

           {:name "bw"
            :default 1.0
            :doc "the bandwidth in octaves between -3 dB frequencies"}]

    :rates #{:ar}
    :check (nth-input-stream? 0)
    :doc "Band pass filter based on the Second Order Section (SOS)
          biquad UGen"}


   {:name "BBandStop",
    :args [{:name "in"
            :doc "input signal to be processed"}

           {:name "freq"
            :default 1200.0
            :doc "center frequency"}

           {:name "bw"
            :default 1.0
            :doc "the bandwidth in octaves between -3 dB frequencies"}]

    :rates #{:ar}
    :check (nth-input-stream? 0)
    :doc "Band reject filter based on the Second Order Section (SOS)
          biquad UGen"}


   {:name "BPeakEQ",
    :args [{:name "in"
            :doc "input signal to be processed"}

           {:name "freq"
            :default 1200.0
            :doc "center frequency"}

           {:name "rq"
            :default 1.0
            :doc "the reciprocal of Q.  bandwidth / cutoffFreq"}

           {:name "db"
            :default 0.0
            :doc "boost/cut the center frequency (in dBs)"}]

    :rates #{:ar}
    :check (nth-input-stream? 0)
    :doc "Parametric equalizer based on the Second Order Section (SOS)
          biquad UGen"}


   {:name "BLowShelf",
    :args [{:name "in"
            :doc "input signal to be processed"}

           {:name "freq"
            :default 1200.0
            :doc "center frequency"}

           {:name "rs"
            :default 1.0
            :doc "the reciprocal of S.  Shell boost/cut slope. When S =
                  1, the shelf slope is as steep as it can be and remain
                  monotonically increasing or decreasing gain with
                  frequency.  The shelf slope, in dB/octave, remains
                  proportional to S for all other values for a fixed
                  freq/SampleRate.ir and db." }

           {:name "db"
            :default 0.0
            :doc "gain. boost/cut the center frequency in dBs"}]

    :rates #{:ar}
    :check (nth-input-stream? 0)
    :doc "Low shelf based on the Second Order Section (SOS) biquad UGen"}


   {:name "BHiShelf",
    :args [{:name "in"
            :doc "input signal to be processed"}

           {:name "freq"
            :default 1200.0
            :doc "center frequency"}

           {:name "rs"
            :default 1.0
            :doc "the reciprocal of S.  Shell boost/cut slope. When S =
                  1, the shelf slope is as steep as it can be and remain
                  monotonically increasing or decreasing gain with
                  frequency.  The shelf slope, in dB/octave, remains
                  proportional to S for all other values for a fixed
                  freq/SampleRate.ir and db." }
           {:name "db"
            :default 0.0
            :doc "gain. boost/cut the center frequency in dBs"}],
    :rates #{:ar}
    :check (nth-input-stream? 0)
    :doc "Hi shelfbased on the Second Order Section (SOS) biquad UGen"}])

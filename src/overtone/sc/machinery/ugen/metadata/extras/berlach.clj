(ns overtone.sc.machinery.ugen.metadata.extras.berlach
  (:use [overtone.sc.machinery.ugen common check]))

;;TODO: add documentation when available

(def specs
  [

   {:name "LPF1"
    :summary "Berlach LPF"
    :args [{:name "in"
            :doc "Input signal"}

           {:name "freq"
            :default 1000
            :doc "Cutoff frequency"}]
    :rates #{:ar :kr}
    :doc ""}

   {:name "LPFVS6"
    :summary "Berlach LPF"
    :args [{:name "in"
            :doc "Input signal"}

           {:name "freq"
            :default 1000
            :doc "Cutoff frequency"}

           {:name "slope"
            :default 0.5
            :doc ""}]
    :rates #{:ar :kr}
    :doc ""}

   {:name "LPF18"
    :summary "Berlach LPF"
    :args [{:name "in"
            :doc "Input signal"}

           {:name "res"
            :default 1
            :doc ""}

           {:name "dist"
            :default 0.4
            :doc ""}]
    :rates #{:ar}
    :doc ""}

   {:name "BLBufRd"
    :summary "Berlach Buffer Read"
    :args [{:name "bufnum"
            :default 0
            :doc "Buffer to read"}

           {:name "phase"
            :default 0
            :doc ""}

           {:name "ratio"
            :default 1
            :doc ""}]
    :rates #{:ar :kr}
    :doc ""}

   {:name "Clipper4"
    :summary "Berlach Clipper"
    :args [{:name "in"
            :doc "Input signal"}

           {:name "lo"
            :default -0.8
            :doc ""}

           {:name "hi"
            :default 0.8
            :doc ""}]
    :rates #{:ar}
    :doc ""}

   {:name "Clipper8"
    :summary "Berlach Clipper"
    :args [{:name "in"
            :doc "Input signal"}

           {:name "lo"
            :default -0.8
            :doc ""}

           {:name "hi"
            :default 0.8
            :doc ""}]
    :rates #{:ar}
    :doc ""}

   {:name "Clipper32"
    :summary "Berlach Clipper"
    :args [{:name "in"
            :doc "Input signal"}

           {:name "lo"
            :default -0.8
            :doc ""}

           {:name "hi"
            :default 0.8
            :doc ""}]
    :rates #{:ar}
    :doc ""}

   {:name "SoftClipper4"
    :summary "Berlach Soft Clipper"
    :args [{:name "in"
            :doc "Input signal"}]

    :rates #{:ar}
    :doc ""}

   {:name "SoftClipper8"
    :summary "Berlach Soft Clipper"
    :args [{:name "in"
            :doc "Input signal"}]

    :rates #{:ar}
    :doc ""}

   {:name "SoftClipAmp4"
    :summary "Berlach Soft Clip Amp"
    :args [{:name "in"
            :doc "Input signal"}

           {:name "pregain"
            :doc ""}]

    :rates #{:ar}
    :doc ""}

   {:name "SoftClipAmp8"
    :summary "Berlach Soft Clip Amp"
    :args [{:name "in"
            :doc "Input signal"}

           {:name "pregain"
            :doc ""}]

    :rates #{:ar}
    :doc ""}

   {:name "OSWrap4"
    :summary "Berlach OS Wrap"
    :args [{:name "in"
            :doc "Input signal"}

           {:name "lo"
            :doc ""}

           {:name "hi"
            :doc ""}]

    :rates #{:ar}
    :doc ""}

   {:name "OSWrap8"
    :summary "Berlach OS Wrap"
    :args [{:name "in"
            :doc "Input signal"}

           {:name "lo"
            :doc ""}

           {:name "hi"
            :doc ""}]

    :rates #{:ar}
    :doc ""}

   {:name "OSFold4"
    :summary "Berlach OS Wrap"
    :args [{:name "in"
            :doc "Input signal"}

           {:name "lo"
            :doc ""}

           {:name "hi"
            :doc ""}]

    :rates #{:ar}
    :doc ""}

   {:name "OSFold8"
    :summary "Berlach OS Wrap"
    :args [{:name "in"
            :doc "Input signal"}

           {:name "lo"
            :doc ""}

           {:name "hi"
            :doc ""}]

    :rates #{:ar}
    :doc ""}

   {:name "OSTrunc4"
    :summary "Berlach OS Truncator"
    :args [{:name "in"
            :doc "Input signal"}

           {:name "quant"
            :default 0.5
            :doc ""}]

    :rates #{:ar}
    :doc ""}

   {:name "OSTrunc8"
    :summary "Berlach OS Truncator"
    :args [{:name "in"
            :doc "Input signal"}

           {:name "quant"
            :default 0.5
            :doc ""}]

    :rates #{:ar}
    :doc ""}

   {:name "DriveNoise"
    :summary "Berlach OS Truncator"
    :args [{:name "in"
            :doc "Input signal"}

           {:name "amount"
            :default 1
            :doc ""}

           {:name "multi"
            :default 5
            :doc ""}]

    :rates #{:ar}
    :doc ""}

   {:name "PeakEQ4"
    :summary "Berlach Peak EQ"
    :args [{:name "in"
            :doc "Input signal"}

           {:name "freq"
            :default 1200
            :doc ""}

           {:name "rs"
            :default 1
            :doc ""}

           {:name "db"
            :default 0
            :doc ""}]

    :rates #{:ar}
    :doc ""}

   {:name "PeakEQ2"
    :summary "Berlach Peak EQ"
    :args [{:name "in"
            :doc "Input signal"}

           {:name "freq"
            :default 1200
            :doc ""}

           {:name "rs"
            :default 1
            :doc ""}

           {:name "db"
            :default 0
            :doc ""}]

    :rates #{:ar}
    :doc ""}])

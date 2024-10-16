(ns overtone.sc.machinery.ugen.metadata.extras.stk
  (:use [overtone.sc.machinery.ugen common check]))

; NOTE: You will need to have the sc3 plugins installed for this ugen to be available.

(def specs
  [
   {:name "StkPluck"
    :args [{:name "freq" :default 440.0 :doc ""}
           {:name "decay" :default 0.99 :doc ""}]
    :rates #{:ar :kr}
    :doc "Plucking string sound."
    }

   {:name "StkFlute"
    :args [{:name "freq" :default 440.0 :doc ""}
           {:name "jetDelay" :default 49.0 :doc ""}
           {:name "noisegain" :default 0.15 :doc ""}
           {:name "jetRatio" :default 0.32 :doc ""}]
    :rates #{:ar :kr}
    :doc ""
    }

   {:name "StkBowed"
    :args [{:name "freq" :default 220.0 :doc ""}
           {:name "bowpressure" :default 64.0 :doc ""}
           {:name "bowposition" :default 64.0 :doc ""}
           {:name "vibfreq" :default 64.0 :doc ""}
           {:name "vibgain" :default 64.0 :doc ""}
           {:name "loudness" :default 64.0 :doc ""}
           {:name "gate" :default 1.0 :doc ""}
           {:name "attackrate" :default 1.0 :doc ""}
           {:name "decayrate" :default 1.0 :doc ""}]
    :rates #{:ar :kr}
    :doc ""
    }

   {:name "StkMandolin"
    :args [{:name "freq" :default 520.0 :doc ""}
           {:name "bodysize" :default 64.0 :doc ""}
           {:name "pickposition" :default 64.0 :doc ""}
           {:name "stringdamping" :default 69 :doc ""}
           {:name "stringdetune" :default 10.0 :doc ""}
           {:name "aftertouch" :default 64.0 :doc ""}
           {:name "trig" :default 1.0 :doc ""}]
    :rates #{:ar :kr}
    :doc ""
    }

   {:name "StkSaxofony"
    :args [{:name "freq" :default 220.0 :doc ""}
           {:name "reedstiffness" :default 64.0 :doc ""}
           {:name "reedaperture" :default 64.0 :doc ""}
           {:name "noisegain" :default 20.0 :doc ""}
           {:name "blowposition" :default 26 :doc ""}
           {:name "vibratofrequency" :default 20.0 :doc ""}
           {:name "vibratogain" :default 20.0 :doc ""}
           {:name "breathpressure" :default 128.0 :doc ""}
           {:name "trig" :default 1.0 :doc ""}]
    :rates #{:ar :kr}
    :doc ""
    }

   {:name "StkShakers"
    :args [{:name "instr" :default 0.0 :doc ""}
           {:name "energy" :default 64.0 :doc ""}
           {:name "decay" :default 64.0 :doc ""}
           {:name "objects" :default 64.0 :doc ""}
           {:name "resfreq" :default 64.0 :doc ""}]
    :rates #{:ar :kr}
    :doc ""
    }

   {:name "StkBandedWG"
    :args [{:name "freq" :default 440.0 :doc ""}
           {:name "instr" :default 0.0 :doc ""}
           {:name "bowpressure" :default 0.0 :doc ""}
           {:name "bowmotion" :default 0.0 :doc ""}
           {:name "integration" :default 0.0 :doc ""}
           {:name "modalresonance" :default 64.0 :doc ""}
           {:name "bowvelocity" :default 0.0 :doc ""}
           {:name "setstriking" :default 0.0 :doc ""}
           {:name "trig" :default 1.0 :doc ""}]
    :rates #{:ar :kr}
    :doc ""
    }

   {:name "StkVoicForm"
    :args [{:name "freq" :default 440.0 :doc ""}
           {:name "vuvmix" :default 64.0 :doc ""}
           {:name "vowelphon" :default 64.0 :doc ""}
           {:name "vibfreq" :default 64.0 :doc ""}
           {:name "vibgain" :default 20.0 :doc ""}
           {:name "loudness" :default 64.0 :doc ""}
           {:name "trig" :default 1.0 :doc ""}]
    :rates #{:ar :kr}
    :doc ""
    }

   {:name "StkModalBar"
    :args [{:name "freq" :default 440.0 :doc ""}
           {:name "instrument" :default 0.0 :doc ""}
           {:name "stickhardness" :default 64.0 :doc ""}
           {:name "stickposition" :default 64.0 :doc ""}
           {:name "vibratogain" :default 20.0 :doc ""}
           {:name "vibratofreq" :default 20.0 :doc ""}
           {:name "directstickmix" :default 64.0 :doc ""}
           {:name "volume" :default 64.0 :doc ""}
           {:name "trig" :default 1.0 :doc ""}]
    :rates #{:ar :kr}
    :doc ""
    }

   {:name "StkClarinet"
    :args [{:name "freq" :default 440.0 :doc ""}
           {:name "reedstiffness" :default 64.0 :doc ""}
           {:name "noisegain" :default 4.0 :doc ""}
           {:name "vibfreq" :default 64.0 :doc ""}
           {:name "vibgain" :default 11.0 :doc ""}
           {:name "breathpressure" :default 64.0 :doc ""}
           {:name "trig" :default 1.0 :doc ""}]
    :rates #{:ar :kr}
    :doc ""
    }

   {:name "StkBlowHole"
    :args [{:name "freq" :default 440.0 :doc ""}
           {:name "reedstiffness" :default 64.0 :doc ""}
           {:name "noisegain" :default 20.0 :doc ""}
           {:name "tonehole" :default 64.0 :doc ""}
           {:name "register" :default 11.0 :doc ""}
           {:name "breathpressure" :default 64.0 :doc ""}]
    :rates #{:ar :kr}
    :doc ""
    }

   {:name "StkMoog"
    :args [{:name "freq" :default 440.0 :doc ""}
           {:name "filterQ" :default 10.0 :doc ""}
           {:name "sweeprate" :default 20.0 :doc ""}
           {:name "vibfreq" :default 64.0 :doc ""}
           {:name "vibgain" :default 0.0 :doc ""}
           {:name "gain" :default 64.0 :doc ""}
           {:name "trig" :default 1.0 :doc ""}]
    :rates #{:ar :kr}
    :doc ""
    }

   {:name "StkBeeThree"
    :args [{:name "freq" :default 440.0 :doc ""}
           {:name "op4gain" :default 10.0 :doc ""}
           {:name "op3gain" :default 20.0 :doc ""}
           {:name "lfospeed" :default 64.0 :doc ""}
           {:name "lfodepth" :default 0.0 :doc ""}
           {:name "adsrtarget" :default 64.0 :doc ""}
           {:name "trig" :default 1.0 :doc ""}]
    :rates #{:ar :kr}
    :doc ""
    }
])

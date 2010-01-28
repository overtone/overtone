
(ns overtone.core.ugen.info)

;; InfoUGenBase : UGen {
;; 	*ir {
;; 		^this.multiNew('scalar')
;; 	}
;; }

;; SampleRate : InfoUGenBase {}
;; SampleDur : InfoUGenBase {}
;; RadiansPerSample : InfoUGenBase {}
;; ControlRate : InfoUGenBase {}
;; ControlDur : InfoUGenBase {}
;; SubsampleOffset : InfoUGenBase {}

;; NumOutputBuses : InfoUGenBase {}
;; NumInputBuses : InfoUGenBase {}
;; NumAudioBuses : InfoUGenBase {}
;; NumControlBuses : InfoUGenBase {}
;; NumBuffers : InfoUGenBase {}
;; NumRunningSynths : InfoUGenBase { *kr { ^this.multiNew('control') }}

;; BufInfoUGenBase : UGen {
;; 	*kr { arg bufnum;
;; 		^this.multiNew('control', bufnum)
;; 	}	
;; 	// the .ir method is not the safest choice.
;;      //    Since a buffer can be reallocated at any time,
;; 	// using .ir will not track the changes.
;; 	*ir { arg bufnum;
;; 		^this.multiNew('scalar',bufnum)
;; 	}
;; }

;; BufSampleRate : BufInfoUGenBase {}
;; BufRateScale : BufInfoUGenBase {}
;; BufFrames : BufInfoUGenBase {}
;; BufSamples : BufInfoUGenBase {}
;; BufDur : BufInfoUGenBase {}
;; BufChannels : BufInfoUGenBase {}

(let [info-names ["SampleRate" "SampleDur" "RadiansPerSample" "ControlRate" "ControlDur"
                  "SubsampleOffset" "NumOutputBuses" "NumInputBuses" "NumAudioBuses"
                  "NumControlBuses" "NumBuffers"]
      info-specs (conj (for [name info-names]
                         {:name name :rates #{:ir}})
                       {:name "NumRunningSynths" :rates #{:ir :kr}})
      buf-info-names ["BufSampleRate" "BufRateScale" "BufFrames"
                      "BufSamples" "BufDur" "BufChannels"]
      buf-info-specs (for [name buf-info-names]
                       {:name name
                        :args [{:name "bufnum"}]
                        :rates #{:kr :ir}})]
  (def specs (concat info-specs buf-info-specs)))


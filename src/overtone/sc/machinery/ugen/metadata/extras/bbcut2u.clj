(ns overtone.sc.machinery.ugen.metadata.extras.bbcut2u
  (:use [overtone.sc.machinery.ugen common check]))

(def specs
  [
   ;;AutoTrack is now BeatTrack (included in the core set of ugens)

   {:name "AnalyseEvents2"
    :summary "On-the-fly event analyser"
    :args [{:name "in"
            :doc "Audio input to track"}

           {:name "bufnum"
            :default 0
            :doc "A buffer within which results of the analysis are placed"}

           {:name "threshold"
            :default 0.34
            :doc "A parameter acting as the onset detector threshold,
                  default of 0.34 was determined as the best performing
                  over a database of percussive onset, but you might
                  want to change this to change the sensitivity (though
                  you always increase the risk of false positives or
                  false negatives)"}

           {:name "triggerid"
            :default 101
            :doc "A trigger ID number used for communication from the
                  UGen to the Lang to mark that a new event was
                  received. Only passed for on-the-fly analysis." }

           {:name "circular"
            :default 0
            :doc "A flag to note on-the-fly analysis assuming a circular
                  buffer. If you only need a one-pass analysis on a
                  file, you won't use this. "}

           {:name "pitch"
            :default 0
            :doc "Can take a .kr pitch detection UGen as input. Will
                  take the median fundamental frequency over a note
                  event from values recorded from this pitch input."}]
    :rates #{:ar}
    :doc "On-the-fly event analyser, based on onset detection/on-the-fly
          analysis described in Nick Collins' academic papers. Best for
          percussive events. Recommended that you go via the
          Segmentation (for one-pass) and AnalyseEventsDatabase (for
          on-the-fly) classes in standard usage, don't use this
          directly."}])

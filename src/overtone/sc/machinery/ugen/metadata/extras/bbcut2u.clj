(ns overtone.sc.machinery.ugen.metadata.extras.bbcut2u
  (:use [overtone.sc.machinery.ugen common check]))

(def specs
  [
   {:name "AutoTrack"
    :summary "Autocorrelation beat tracker"
    :args [{:name "in"
            :doc "Audio input to track"}

           {:name "lock"
            :default 0
            :doc "If this argument is greater than 0.5, the tracker will
                  lock at its current periodicity and continue from the
                  current phase. Whilst it updates the model's phase and
                  period, this is not reflected in the output until lock
                  goes back below 0.5.  "} ]
    :rates #{:kr}
    :doc "Autocorrelation beat tracker by Nick Collins, following:

          M. E. P. Davies and M. D. Plumbley. Beat Tracking With A Two
          State Model. Proceedings of the IEEE International Conference
          on Acoustics, Speech and Signal Processing (ICASSP 2005),
          Philadelphia, USA, March 19-23, 2005

          There are four k-rate outputs, being ticks at quarter, eighth
          and sixteenth level from the determined beat, and the current
          detected tempo.

          Note the following restrictions:

          This beat tracker determines the beat, biased to the midtempo
          range by weighting functions. It does not determine the
          measure level, only a tactus. It is also slow reacting, using
          a 6 second temporal window for it's autocorrelation
          maneouvres. Don't expect human musician level predictive
          tracking.

          On the other hand, it is tireless, relatively general (though
          obviously best at transient 4/4 heavy material without much
          expressive tempo variation), and can form the basis of
          computer processing that is decidedly faster than human. "}

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

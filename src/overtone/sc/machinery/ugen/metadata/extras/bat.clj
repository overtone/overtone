(ns overtone.sc.machinery.ugen.metadata.extras.bat
  (:use [overtone.sc.machinery.ugen common check]))

(def specs
  [
   {:name "Coyote"
    :summary "an amplitude tracking based onset detector"
    :args [{:name "in"
            :default 0
            :doc "The input signal"}

           {:name "track-fall"
            :default 0.2
            :doc "60dB convergence time for the initial amplitude
                  tracker." }

           {:name "slow-lag"
            :default 0.2
            :doc "Lag time for the slow smoother. "}

           {:name "fast-lag"
            :default 0.01
            :doc "Lag time for the fast smoother."}

           {:name "fast-mul"
            :default 0.5
            :doc "Multiplier for the fast smoother. At the instant of
                  onsets, fast smoother output will exceed the slow
                  smoother and trigger an onset report. If you want to
                  tweak the sensitivity of the tracking, you should try
                  tweaking this value first. Higher values(approaching
                  to 1) makes the tracking more sensitive." }

           {:name "thresh"
            :default 0.05
            :doc "The minimum threshold for the input to begin tracking
                  onsets. "}

           {:name "min-dur"
            :default 0.1
            :doc "Minimum duration between events."}]

    :rates #{:kr}
    :doc "Coyote is an onset detector which tries to find onset attacks
          in a signal without using FFT processing. It tracks the
          amplitude changes in the incoming signal and sends a trigger
          when an onset is found. To get the best tracking for a
          particular signal by tweaking the arguments, one needs to
          understand how the onset detection works inside the UGen:

          Coyote compares three different analysis results in parallel
          and tries to report an onset event in the signal. The first
          phase is amplitude tracking. The trackFall argument is the
          60dB convergence time of the decaying signal(the attack time
          is constant: 0.001, the process is the same with the Amplitude
          UGen, trackFall is the releaseTime). The output of this
          tracking is divided to 3 inputs inside. The first two are
          smoothers(lowpass filters) with different lag times. slowLag
          is the lag time of the slow smoother, and the fastLag is the
          lag time of the fast one. The fast smoother is multiplied by a
          value(fastMul argument) which should be between 0 and 0.9 so
          its output is always below the slow smoother, except in
          onsets. So when an onset occurs, the fast smoother output
          rises quicker than the slow smoother, and when the fast one
          exceeds the slower at an instant(occurs only at onsets), a
          trigger is sent to the output from the UGen. For the next
          trigger to happen, a specified time should pass which is
          defined by the minDur parameter. So minDur defines the minimum
          time between events/triggers.

          This approach is extremely fast in response(compared to FFT
          based detectors) when detecting onsets and works well on most
          contexts(guitar, percussion, etc...). But it has a drawback
          when there are sustaining sounds present from the same
          instrument at the moment of an onset, so there is a third unit
          inside that averages the input beginning from the last trigger
          whose output is also smoothed by a smoother(lag time is also
          set to slowLag) and it too is compared with the output of fast
          smoother to make the tracking work better when there are
          sustaining sounds present at the moment of an onset.

          The default values are a good starting point and works well on
          many contexts." }


   {:name "TrigAvg"
    :summary "triggered signal averager"
    :args [{:name "in"
            :default 0
            :doc "The input signal"}

           {:name "trig"
            :default 0
            :doc "When triggered, TrigAvg forgets the past average and
                  starts averaging from zero." }]
    :rates #{:kr}
    :doc "Averages the absolute values of its input between triggers."}


   {:name "WAmp"
    :summary "windowed amplitude follower"
    :args [{:name "in"
            :default 0
            :doc "The input signal"}

           {:name "win-size"
            :default 0.1
            :doc "The window size in seconds. Not modulatable."}]
    :rates #{:kr}
    :doc "Averages and outputs the absolute value of incoming signals
          received between now and (now - winSize) seconds."}


   {:name "MarkovSynth"
    :summary "First order Markov Chain implementation for audio signals"
    :args [{:name "in"
            :default 0
            :doc "The input signal"}

           {:name "is-recording"
            :default 1
            :doc "if non-zero, MarkovSynth populates the internal table with its signal input."}

           {:name "wait-time"
            :default 2
            :doc "Defines the wait time of the UGen to start synthesizing the table, in seconds."}

           {:name "table-size"
            :default 10
            :doc "The probability table size for each sample. High values are memory hungry!"}]
    :rates #{:ar}
    :doc "MarkovSynth populates a sample to sample transition
          probability table with its signal input. Each possible sample
          value in an 16bit signal has its own transition probability
          table whose size is defined by the table-size argument at
          creation time. It waits and populates the table for wait-time
          seconds and then starts synthesizing audio by continuously
          outputting a random value selected from the probability table
          of the last synthesized sample. Once the end of table is
          reached for a single sample, its index wraps back to zero and
          populating continues in this fashion as long as is-recording
          argument is non-zero. The character of the input is mainly
          defined by the way its input signal changes. So input signals
          showing little difference in amplitude and periodicity has a
          similar quality in output. The output becomes less dynamic.

          If the tableSize is 1, the output is usually a reflection of
          the input. tableSize of 2 makes some funny blips and
          blops. When tableSize goes higher, older and older transition
          values are taken into account and the output changes
          accordingly. You should be careful with the table-size as it
          allocates all the memory for the tables beforehand so it may
          cause troubles.

          You may want to use leak-dc on its output as the output is
          offset agnostic, it just selects a past-recorded transition
          value at random."}


   {:name "FrameCompare"
    :summary "calculates spectral MSE distance of two fft chains"
    :args [{:name "buffer1"
            :doc "FFT chain 1"}

           {:name "buffer2"
            :doc "FFT chain 2"}

           {:name "w-amount"
            :default 0.5
            :doc "Influence of the weight matrix (should be between 0
                  and 1). Weight matrix helps to minimize errors on
                  regions with more energy. "}]
    :rates #{:kr}
    :doc "Given two FFT chains, this UGen calculates the MSE between the
          magnitudes of these two inputs and provides a continuous
          analytic similarity rating (lower the value, more similar the
          inputs). In it's current state, only hanning window should be
          used (wintype: 1)."}


   {:name "NeedleRect"
    :summary ""
    :args [{:name "rate"
            :default 1
            :doc ""}

           {:name "img-width"
            :default 100
            :doc ""}

           {:name "img-height"
            :default 100
            :doc ""}

           {:name "rect-x"
            :default 0
            :doc ""}

           {:name "rect-y"
            :default 0
            :doc ""}

           {:name "rect-w"
            :default 100
            :doc ""}

           {:name "rect-h"
            :default 100
            :doc ""}]
    :rates #{:ar}
    :doc ""}


   {:name "SkipNeedle"
    :summary ""
    :args [{:name "range"
            :default 44100
            :doc ""}

           {:name "rate"
            :default 10
            :doc ""}

           {:name "offset"
            :default 0
            :doc ""}]
    :rates #{:ar}
    :doc ""}])

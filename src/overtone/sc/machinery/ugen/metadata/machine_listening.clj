(ns overtone.sc.machinery.ugen.metadata.machine-listening
  (:use [overtone.sc.machinery.ugen common check]))

(def specs
  [
   {:name "BeatTrack",
    :args [{:name "chain"
            :doc "Audio input to track, already passed through an FFT
                     UGen; the expected size of FFT is 1024 for 44100
                     and 48000 sampling rate, and 2048 for double
                     those. No other sampling rates are supported. "}

           {:name "lock",
            :default 0
            :doc "If this argument is greater than 0.5, the tracker
                     will lock at its current periodicity and continue
                     from the current phase. Whilst it updates the
                     model's phase and period, this is not reflected in
                     the output until lock goes back below 0.5.  "}],
    :rates #{:kr}
    :num-outs 4
    :doc "Autocorrelation based beat tracker

             The underlying model assumes 4/4, but it should work on any
             isochronous beat structure, though there are biases to
             100-120 bpm; a fast 7/8 may not be tracked in that
             sense. There are four k-rate outputs, being ticks at
             quarter, eighth and sixteenth level from the determined
             beat, and the current detected tempo. Note that the
             sixteenth note output won't necessarily make much sense if
             the music being tracked has swing; it is provided just as a
             convenience.

             This beat tracker determines the beat, biased to the
             midtempo range by weighting functions. It does not
             determine the measure level, only a tactus. It is also slow
             reacting, using a 6 second temporal window for its
             autocorrelation maneouvres. Don't expect human musician
             level predictive tracking.

             On the other hand, it is tireless, relatively
             general (though obviously best at transient 4/4 heavy
             material without much expressive tempo variation), and can
             form the basis of computer processing that is decidedly
             faster than human. "}



   {:name "Loudness",
    :args [{:name "chain"
            :doc "Audio input to track, which has been pre-analysed by
                  the FFT UGen"}

           {:name "smask",
            :default 0.25
            :doc "Spectral masking param: lower bins mask higher bin
                  power within ERB bands, with a power falloff (leaky
                  integration multiplier) of smask per bin"}

           {:name "tmask",
            :default 1
            :doc "Temporal masking param: the phon level let through in
                  an ERB band is the maximum of the new measurement, and
                  the previous minus tmask phons"}]
    :rates #{:kr}
    :doc "A perceptual loudness function which outputs loudness in
             sones; this is a variant of an MP3 perceptual model,
             summing excitation in ERB bands. It models simple spectral
             and temporal masking, with equal loudness contour
             correction in ERB bands to obtain phons (relative dB), then
             a phon to sone transform. The final output is typically in
             the range of 0 to 64 sones, though higher values can occur
             with specific synthesised stimuli." }


   {:name "Onsets",
    :args [{:name "chain"
            :doc "an FFT chain"}

           {:name "threshold",
            :default 0.5
            :doc "the detection threshold, typically between 0 and 1,
                  although in rare cases you may find values outside
                  this range useful"}

           {:name "odftype"
            :default 3
            :doc "the function used to analyse the signal. Options:
                  nPOWER, MAGSUM, COMPLEX, RCOMPLEX (default), PHASE,
                  WPHASE and MKL. Default is RCOMPLEX." }

           {:name "relaxtime",
            :default 1
            :doc "specifies the time (in seconds) for the normalisation
                  to forget about a recent onset. If you find too much
                  re-triggering (e.g. as a note dies away unevenly) then
                  you might wish to increase this value." }

           {:name "floor",
            :default 0.1
            :doc "is a lower limit, connected to the idea of how quiet
                  the sound is expected to get without becoming
                  indistinguishable from noise. For some
                  cleanly-recorded classical music with wide dynamic
                  variations, I found it helpful to go down as far as
                  0.000001." }

           {:name "mingap",
            :default 10
            :doc "specifies a minimum gap (in FFT frames) between onset
                  detections, a brute-force way to prevent too many
                  doubled detections." }

           {:name "medianspan",
            :default 11.0
            :doc " specifies the size (in FFT frames) of the median
                   window used for smoothing the detection function
                   before triggering." }

           {:name "whtype", :default 1}

           {:name "rawodf", :default 0}],
    :rates #{:kr}
    :doc "An onset detector for musical audio signals - detects the
          beginning of notes/drumbeats/etc. Outputs a control-rate
          trigger signal which is 1 when an onset is detected, and 0
          otherwise.

          For the FFT chain, you should typically use a frame size of
          512 or 1024 (at 44.1 kHz sampling rate) and 50% hop
          size (which is the default setting in SC). For different
          sampling rates choose an FFT size to cover a similar
          time-span (around 10 to 20 ms).

          The onset detection should work well for a general range of
          monophonic and polyphonic audio signals. The onset detection
          is purely based on signal analysis and does not make use of
          any top-down inferences such as tempo." }


   {:name "KeyTrack",
    :args [{:name "chain"
            :doc "Audio input to track. This must have been pre-analysed
                  by a 4096 size FFT." }

           {:name "keydecay",
            :default 2.0
            :doc "Number of seconds for the influence of a window on the
                  final key decision to decay by 40dB (to 0.01 its
                  original value)"}

           {:name "chromaleak",
            :default 0.5
            :doc "Each frame, the chroma values are set to the previous
                  value multiplied by the chromadecay. 0.0 will start
                  each frame afresh with no memory."}],
    :rates #{:kr}
    :doc "A (12TET major/minor) key tracker based on a pitch class
          profile of energy across FFT bins and matching this to
          templates for major and minor scales in all transpositions. It
          assumes a 440 Hz concert A reference. Output is 0-11 C major
          to B major, 12-23 C minor to B minor"}


   {:name "MFCC",
    :args [{:name "chain"
            :doc "Audio input to track, which has been pre-analysed by
                  the FFT UGen"}
           {:name "numcoeff",
            :default 13
            :doc "Number of coefficients, defaults to 13, maximum of 42"}],
    :rates #{:kr}
    :num-outs :variable
    :doc "Generates a set of MFCCs; these are obtained from a band-based
          frequency representation (using the Mel scale by default), and
          then a discrete cosine transform (DCT). The DCT is an
          efficient approximation for principal components analysis, so
          that it allows a compression, or reduction of dimensionality,
          of the data, in this case reducing 42 band readings to a
          smaller set of MFCCs. A small number of features (the
          coefficients) end up describing the spectrum. The MFCCs are
          commonly used as timbral descriptors.

          Output values are somewhat normalised for the range 0.0 to
          1.0, but there are no guarantees on exact conformance to
          this. Commonly, the first coefficient will be the highest
          value. "
    ;;       :init (fn [rate args spec]
    ;;               {:args args
    ;;                :num-outs (args 1)})
    }


   {:name "BeatTrack2",
    :args [{:name "busindex"
            :doc "Audio input to track, already analysed into N
                  features, passed in via a control bus number from
                  which to retrieve consecutive streams. "}

           {:name "numfeatures"
            :doc "How many features (ie how many control buses) are
                  provided"}

           {:name "windowsize",
            :default 2.0
            :doc "Size of the temporal window desired (2.0 to 3.0
                  seconds models the human temporal window). You might
                  use longer values for stability of estimate at the
                  expense of reactiveness." }

           {:name "phaseaccuracy",
            :default 0.02
            :doc "Relates to how many different phases to test. At the
                  default, 50 different phases spaced by phaseaccuracy
                  seconds would be tried out for 60bpm; 16 would be
                  trialed for 180 bpm. Larger phaseaccuracy means more
                  tests and more CPU cost." }

           {:name "lock", :default 0
            :doc "If this argument is greater than 0.5, the tracker will
                  lock at its current periodicity and continue from the
                  current phase. Whilst it updates the model's phase and
                  period, this is not reflected in the output until lock
                  goes back below 0.5." }

           {:name "weightingscheme",
            :default -2.1
            :doc "Use (-2.5) for flat weighting of tempi, (-1.5) for
                  compensation weighting based on the number of events
                  tested (because different periods allow different
                  numbers of events within the temporal window) or
                  otherwise a bufnum from 0 upwards for passing an array
                  of 120 individual tempo weights; tempi go from 60 to
                  179 bpm in steps of one bpm, so you must have a buffer
                  of 120 values.  "}],
    :rates #{:kr},
    :num-outs 6
    :doc "Template matching beat tracker.

          This beat tracker is based on exhaustively testing particular
          template patterns against feature streams; the testing takes
          place every 0.5 seconds. The two basic templates are a
          straight (groove=0) and a swung triplet (groove=1) pattern of
          16th notes; this pattern is tried out at scalings
          corresponding to the tempi from 60 to 180 bpm. This is the
          cross-corellation method of beat tracking. A majority vote is
          taken on the best tempo detected, but this must be confirmed
          by a consistency check after a phase estimate. Such a
          consistency check helps to avoid wild fluctuating estimates,
          but is at the expense of an additional half second delay. The
          latency of the beat tracker with default settings is thus at
          least 2.5 seconds; because of block-based amortisation of
          calculation, it is actually around 2.8 seconds latency for a
          2.0 second temporal window.

          This beat tracker is designed to be flexible for user needs;
          you can try out different window sizes, tempo weights and
          combinations of features. However, there are no guarantees on
          stability and effectiveness, and you will need to explore such
          parameters for a particular situation.  "}

   {:name "SpecFlatness",
    :args [{:name "chain"
            :doc "An FFT chain"}],
    :rates #{:kr}
    :doc "Given an FFT chain this calculates the Spectral Flatness
          measure, defined as a power spectrum's geometric mean divided
          by its arithmetic mean. This gives a measure which ranges from
          approx 0 for a pure sinusoid, to approx 1 for white noise.

          The measure is calculated linearly. For some applications you
          may wish to convert the value to a decibel scale - an example
          of such conversion is shown below." }


   {:name "SpecPcile",
    :args [{:name "chain"
            :doc "An FFT chain"}

           {:name "fraction",
            :default 0.5
            :doc "percentage of the spectral energy you which to find
                  the frequency for"}

           {:name "interpolate"
            :default 0
            :doc "Interpolation toggle - 0 off 1 on."}],
    :rates #{:kr}
    :doc "Find a percentile of FFT magnitude spectrum

          Given an FFT chain this calculates the cumulative distribution
          of the frequency spectrum, and outputs the frequency value
          which corresponds to the desired percentile.

          For example, to find the frequency at which 90% of the
          spectral energy lies below that frequency, you want the
          90-percentile, which means the value of fraction should be
          0.9. The 90-percentile or 95-percentile is often used as a
          measure of spectral roll-off.

          The optional third argument interpolate specifies whether
          interpolation should be used to try and make the percentile
          frequency estimate more accurate, at the cost of a little
          higher CPU usage. Set it to 1 to enable this." }

   ;; SpecCentroid : UGen
   ;; {
   ;;   *kr { | buffer |
   ;;     ^this.multiNew('control', buffer)
   ;;   }
   ;; }

   {:name "SpecCentroid",
    :args [{:name "chain"
            :doc "An FFT chain"}],
    :rates #{:kr}
    :doc "Given an FFT chain, this measures the spectral centroid, which
          is the weighted mean frequency, or the centre of mass of the
          spectrum. (DC is ignored.)

          This can be a useful indicator of the perceptual brightness of
          a signal." }])

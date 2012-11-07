(ns overtone.sc.machinery.ugen.metadata.noise
  (:use [overtone.sc.machinery.ugen common check]))

(def specs
     [


      {:name "WhiteNoise"
       :summary "Noise whose spectrum has equal power at all frequencies."
       :args []
       :rates #{:ar :kr}
       :doc "Noise that contains equal amounts of energy at every
             frequency - comparable to radio static.

             Useful for generating percussive sounds such as snares and
             hand claps. Also useful for simulating wind or sea effects,
             for producing breath effects in wind instrument timbres or
             for producing the typical trance leads."}


      {:name "BrownNoise"
       :summary "Noise whose spectrum falls off in power by 6 dB per octave."
       :args []
       :rates #{:ar :kr}
       :doc "Useful for generating percussive sounds such as snares and
             hand claps. Also useful for simulating wind or sea
             effects, for producing breath effects in wind instrument
             timbres or for producing the typical trance leads." }


      {:name "PinkNoise"
       :summary "Noise whose spectrum falls off in power by 3 dB per octave."
       :args []
       :rates #{:ar :kr}
       :doc "Noise that gives equal power over the span of each octave.

             Useful for generating percussive sounds such as snares and
             hand claps. Also useful for simulating wind or sea effects,
             for producing breath effects in wind instrument timbres or
             for producing the typical trance leads.

             This version gives 8 octaves of pink noise."}


      {:name "ClipNoise"
       :summary "Noise whose values are either -1 or 1."
       :args []
       :rates #{:ar}
       :doc "This produces the maximum energy for the least peak to peak amplitude.

             Useful for generating percussive sounds such as snares and
             hand claps. Also useful for simulating wind or sea effects,
             for producing breath effects in wind instrument timbres or
             for producing the typical trance leads."}


      {:name "GrayNoise"
       :summary "Random impulses from -1 to +1 given a density "
       :args []
       :rates #{:ar}
       :doc "Given a density (average number of impulses per second)
             creates a sequence of random impulses from -1 to +1.

             Useful for generating percussive sounds such as snares and
             hand claps. Also useful for simulating wind or sea effects,
             for producing breath effects in wind instrument timbres or
             for producing the typical trance leads."}


      {:name "Crackle"
       :summary "Chaotic noise generator"
       :args [{:name "chaos-param"
               :default 1.5
               :doc "a parameter of the chaotic function with useful
                     values from just below 1.0 to just above
                     2.0. Towards 2.0 the sound crackles."}]
       :rates #{:ar :kr}
       :doc "A noise generator based on a chaotic function.

             Useful for generating percussive sounds such as snares and
             hand claps. Also useful for simulating wind or sea effects,
             for producing breath effects in wind instrument timbres or
             for producing the typical trance leads."}


      {:name "Logistic"
       :args [{:name "chaos-param"
               :default 3.0
               :doc "a parameter of the chaotic function with useful
                     values from 0.0 to 4.0. Chaos occurs from 3.57
                     up. Don't use values outside this range if you
                     don't want the UGen to blow up." }

              {:name "freq"
               :default 1000.0
               :doc "Frequency of calculation; if over the sampling
                     rate, this is clamped to the sampling rate"}

              {:name "init"
               :default 0.5
               :doc "Initial value of y (see equation below)"}]

       :rates #{:ar}
       :doc "A noise generator based on the logistic map:

             y = chaos-param * y * (1.0 - y)

             y will stay in the range of 0.0 to 1.0 for normal values of
             the chaos-param. This leads to a DC offset and may cause a
             pop when you stop the Synth. For output you might want to
             combine this UGen with a LeakDC or rescale around 0.0 via
             mul and add: see example below. "}


      {:name "LFNoise0"
       :args [{:name "freq",
               :default 500.0
               :doc "approximate rate at which to generate random
                     values." }]
       :rates #{:ar :kr}
       :doc "Generates random values at a rate (the rate is not
             guaranteed but approximate)"}



      {:name "LFNoise1"
       :args [{:name "freq",
               :default 500.0
               :doc "approximate rate at which to generate random
                     values." }]

       :rates #{:ar :kr}
       :doc "Generates linearly interpolated random values at the
             supplied rate (the rate is not guaranteed but
             approximate). "}



      {:name "LFNoise2"
       :args [{:name "freq",
               :default 500.0
               :doc "approximate rate at which to generate random
                     values." }]
       :rates #{:ar :kr}
       :doc "Generates quadratically interpolated random values at the
             supplied rate (the rate is not guaranteed but approximate).

             Note: quadratic interpolation means that the noise values
             can occasionally extend beyond the normal range of +-1, if
             the freq varies in certain ways. If this is undesirable
             then you might like to clip2 the values or use a
             linearly-interpolating unit instead." }


      {:name "LFClipNoise"
       :args [{:name "freq"
               :default 500.0
               :doc "approximate rate at which to generate random
                     values." }]
       :rates #{:ar :kr}
       :doc "Randomly generates the values -1 or +1 at a rate given by
             the nearest integer division of the sample rate by the freq
             argument. It is probably pretty hard on your speakers!" }


      {:name "LFDNoise0"
       :args [{:name "freq"
               :default 500.0
               :doc "rate at which to generate random values."}]
       :rates #{:ar :kr}
       :doc "Like lf-noise0, it generates random values at a rate given
             by the freq argument, with two differences:

             * no time quantization

             * fast recovery from low freq values.

             (lf-noise0,1,2 quantize to the nearest integer division of
             the samplerate and they poll the freq argument only when
             scheduled, and thus seem to hang when freqs get very low).

             If you don't need very high or very low freqs, or use fixed
             freqs lf-noise0 is more efficient." }


      {:name "LFDNoise1"
       :args [{:name "freq"
               :default 500.0
               :doc "rate at which to generate random values."}]
       :rates #{:ar :kr}
       :doc "Like lf-noise1, it generates linearly interpolated random
             values at a rate given by the freq argument, with two differences:

             * no time quantization

             * fast recovery from low freq values.

             (lf-noise0,1,2 quantize to the nearest integer division of
             the samplerate and they poll the freq argument only when
             scheduled, and thus seem to hang when freqs get very low).

             If you don't need very high or very low freqs, or use fixed
             freqs lf-noise1 is more efficient." }


      {:name "LFDNoise3"
       :args [{:name "freq"
               :default 500.0
               :doc "rate at which to generate random values."}]
       :rates #{:ar :kr}
       :doc "Similar to lf-noise2, it generates polynomially
             interpolated random values at a rate given by the freq
             argument, with 3 differences:

             * no time quantization

             * fast recovery from low freq values

             * cubic instead of quadratic interpolation

             (lf-noise0,1,2 quantize to the nearest integer division of
             the samplerate and they poll the freq argument only when
             scheduled, and thus seem to hang when freqs get very low).
             If you don't need very high or very low freqs, or use fixed
             freqs lf-noise2 is more efficient." }


      {:name "LFDClipNoise"
       :args [{:name "freq"
               :default 500.0
               :doc "rate at which to generate random values."}]
       :rates #{:ar :kr}
       :doc "Like lf-clip-noise, it generates the values -1 or +1 at a
             rate given by the freq argument, with two differences:

             * no time quantization

             * fast recovery from low freq values.

             (lf-clip-noise, as well as lf-noise0,1,2 quantize to the
             nearest integer division of the samplerate, and they poll
             the freq argument only when scheduled; thus they often seem
             to hang when freqs get very low).

             If you don't need very high or very low freqs, or use fixed
             freqs lf-noise0 is more efficient." }


      {:name "Hasher"
       :args [{:name "in"
               :default 0.0
               :doc "input signal"}]
       :rates #{:ar}
       :doc "Returns a unique output value from zero to one for each
             input value according to a hash function. The same input
             value will always produce the same output value. The input
             need not be from zero to one." }


      {:name "MantissaMask"
       :args [{:name "in"
               :default 0.0
               :doc "input signal"}

              {:name "bits"
               :default 3
               :doc "the number of mantissa bits to preserve. a number
                     from 0 to 23." }]
       :rates #{:ar}
       :doc "Masks off bits in the mantissa of the floating point sample
             value. This introduces a quantization noise, but is less
             severe than linearly quantizing the signal." }


      {:name "Dust"
       :args [{:name "density"
               :default 0.0
               :doc "average number of impulses per second"}]
       :rates #{:ar :kr}
       :doc "Generates random impulses from 0 to +1."}


      {:name "Dust2"
       :args [{:name "density"
               :default 0.0
               :doc "average number of impulses per second."}]
       :rates #{:ar :kr}
       :doc "Generates random impulses from -1 to +1."}])

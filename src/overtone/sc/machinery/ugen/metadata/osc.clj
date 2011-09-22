(ns overtone.sc.machinery.ugen.metadata.osc
  (:use [overtone.sc.machinery.ugen common]))

(def specs
     [
      {:name "Oscy",
       :args [{:name "buffer" :doc "Lookup buffer"}
              {:name "freq", :default 440.0 :doc "Frequency in Hertz"}
              {:name "phase", :default 0.0 :doc "Phase offset or modulator in radians"}],
       :doc "Linear interpolating wavetable lookup oscillator with frequency and phase modulation inputs.

This oscillator requires a buffer to be filled with a wavetable format signal.  This preprocesses the Signal into a form which can be used efficiently by the Oscillator.  The buffer size must be a power of 2.

This can be acheived by creating a Buffer object and sending it one of the b_gen  messages ( sine1, sine2, sine3 ) with the wavetable flag set to true.

This can also be acheived by creating a Signal object and sending it the 'asWavetable' message, saving it to disk, and having the server load it from there."}

            {:name "Osc",
       :args [{:name "buffer" :doc "Lookup buffer"}
              {:name "freq", :default 440.0 :doc "Frequency in Hertz"}
              {:name "phase", :default 0.0 :doc "Phase offset or modulator in radians"}],
       :doc "Linear interpolating wavetable lookup oscillator with frequency and phase modulation inputs.

This oscillator requires a buffer to be filled with a wavetable format signal.  This preprocesses the Signal into a form which can be used efficiently by the Oscillator.  The buffer size must be a power of 2.

This can be acheived by creating a Buffer object and sending it one of the b_gen  messages ( sine1, sine2, sine3 ) with the wavetable flag set to true.

This can also be acheived by creating a Signal object and sending it the 'asWavetable' message, saving it to disk, and having the server load it from there."}

      {:name "SinOsc",
       :args [{:name "freq", :default 440.0 :doc "Frequency in Hertz"}
              {:name "phase", :default 0.0 :doc "Phase offset or modulator in radians"}],
       :summary "Sine table lookup oscillator"
       :doc "Outputs a sine wave with values oscillating between -1 and 1 similar to osc except that the table has already been fixed as a sine table of 8192 entries."}

      {:name "SinOscFB",
       :args [{:name "freq", :default 440.0 :doc "Frequency of oscillator"}
              {:name "feedback", :default 0.0 :doc "amplitude of phase feedback in radians"}]
       :summary "Sine oscillator with phase modulation feedback"
       :doc "Different feedback values results in a modulation between a sine wave and a sawtooth like wave. Overmodulation causes chaotic oscillation."}

      {:name "OscN",
       :args [{:name "bufnum" :doc "Buffer index.  The buffer size must be a power of 2.  The buffer should NOT be filled using Wavetable format (b_gen commands should set wavetable flag to false.  Raw signals (not converted with asWavetable) can be saved to disk and loaded into the buffer."}
              {:name "freq", :default 440.0 :doc "Frequency in Hertz"}
              {:name "phase", :default 0.0 :doc "Phase offset or modulator in radians"}],
       :doc "Noninterpolating wavetable lookup oscillator with frequency and phase modulation inputs.

It is usually better to use the interpolating oscillator."}

      {:name "VOsc",
       :args [{:name "bufpos" :doc "Buffer index. Can be swept continuously among adjacent wavetable buffers of the same size."}
              {:name "freq", :default 440.0 :doc "Frequency in Hertz"}
              {:name "phase", :default 0.0 :doc "Phase offset of modulator in radians"}],
       :doc "A wavetable lookup oscillator which can be swept smoothly across wavetables. All the wavetables must be allocated to the same size. Fractional values of table will interpolate between two adjacent tables.

This oscillator requires at least two buffers to be filled with a wavetable format signal.  This preprocesses the Signal into a form which can be used efficiently by the Oscillator.  The buffer size must be a power of 2.
"}

      {:name "VOsc3",
       :args [{:name "bufpos" :doc "Buffer index. Can be swept continuously among adjacent wavetable buffers of the same size."}
              {:name "freq1", :default 110.0 :doc "Frequency in Hertz of first oscillator"}
              {:name "freq2", :default 220.0 :doc "Frequency in Hertz of second oscillator"}
              {:name "freq3", :default 440.0 :doc "Frequency in Hertz of third oscillator"}],
       :doc "Three variable wavetable oscillators.

A wavetable lookup oscillator which can be swept smoothly across wavetables. All the wavetables must be allocated to the same size. Fractional values of table will interpolate between two adjacent tables. This unit generator contains three oscillators at different frequencies, mixed together.

This oscillator requires at least two buffers to be filled with a wavetable format signal.  This preprocesses the Signal into a form which can be used efficiently by the Oscillator.  The buffer size must be a power of 2. "}

      {:name "COsc",
       :args [{:name "bufnum" :doc "The number of a buffer filled in wavetable format"}
              {:name "freq", :default 440.0 :doc "Frequency in Hertz"}
              {:name "beats", :default 0.5 :doc "Beat frequency in Hertz"}],
       :doc "Chorusing wavetable lookup oscillator. Produces sum of two signals at  (freq +/- (beats / 2)). Due to summing, the peak amplitude is twice that of the wavetable."}

      {:name "Formant",
       :args [{:name "fundfreq", :default 440.0 :doc "Fundamental frequency in Hertz (control rate)"}
              {:name "formfreq", :default 1760.0 :doc "Formant frequency in Hertz (control rate)"}
              {:name "bwfreq", :default 880.0 :doc "Pulse width frequency in Hertz. Controls the bandwidth of the formant (control rate)"}],
       :rates #{:ar},
       :doc "Generates a set of harmonics around a formant frequency at a given fundamental frequency.

The frequency inputs are read at control rate only, so if you use an audio rate UGen as an input, it will only be sampled at the start of each audio synthesis block.
"}

      {:name "LFSaw",
       :args [{:name "freq", :default 440.0 :doc "Frequency in Hertz"}
              {:name "iphase", :default 0.0 :doc "Initial phase offset. For efficiency reasons this is a value ranging from 0 to 2."}]
       :doc "low freq (i.e. not band limited) sawtooth oscillator"}

      {:name "LFPar" :extends "LFSaw"
       :doc "a non band-limited parabolic oscillator outputing a high of 1 and a low of zero."}

      {:name "LFCub" :extends "LFSaw"
       :doc "an oscillator outputting a sine like shape made of two cubic pieces"}

      {:name "LFTri" :extends "LFSaw"
       :doc "a non-band-limited triangle oscillator"}


      {:name "LFGauss",
       :args [{:name "duration", :default 1 :doc "Duration of one full cycle ( for freq input: dur = 1 / freq )
"}
              {:name "width", :default 0.1 :doc "Relative width of the bell. Best to keep below 0.25 when used as envelope."}
              {:name "iphase", :default 0.0 :doc "Initial offset "}
              {:name "loop", :default 1 :doc "If loop is > 0, UGen oscillates. Otherwise it calls doneAction after one cycle"}
              {:name "action", :default 0 :doc "Action to be evaluated after cycle completes. Default: NO-ACTION."}]
       :rates #{:ar :kr}
       :doc "A non-band-limited gaussian function oscillator. Output ranges from minval to 1.

LFGauss implements the formula: f(x) = exp(squared(x - iphase) / (-2.0 * squared(width)))
where x is to vary in the range -1 to 1 over the period dur. minval is the initial value at -1
"}

      {:name "LFPulse",
       :args [{:name "freq", :default 440.0 :doc "Frequency in Hertz"}
              {:name "iphase", :default 0.0 :doc "Initial phase offset in cycles ( 0..1 )"
               }
              {:name "width", :default 0.5 :doc "Pulse width duty cycle from zero to one"}]
       :signal-range :unipolar
       :rates #{:ar :kr}
       :default-rate :kr
       :doc "A non-band-limited pulse oscillator. Outputs a high value of one and a low value of zero.
"}

      {:name "VarSaw",
       :args [{:name "freq", :default 440.0 :doc "Frequency in Hertz"}
              {:name "iphase", :default 0.0 :doc "Initial phase offset in cycles ( 0..1 )"}
              {:name "width", :default 0.5 :doc "Duty cycle from zero to one."}]
       :rates #{:ar :kr}
       :doc "a variable duty cycle saw wave oscillator"}


      {:name "Impulse",
       :args [{:name "freq", :default 440.0 :doc "Frequency in Hertz"}
              {:name "phase", :default 0.0 :doc "Phase offset in cycles ( 0..1 )"}]
       :signal-range :unipolar
       :rates #{:ar :kr}
       :default-rate :kr
       :doc "non band limited impulse oscillator. Outputs a single 1 every freq cycles per second and 0 the rest of the time."}


      {:name "SyncSaw",
       :args [{:name "syncFreq", :default 440.0 :doc "Frequency of the fundamental."}
              {:name "sawFreq", :default 440.0 :doc "Frequency of the slave synched sawtooth wave. sawFreq should always be greater than syncFreq."}]
       :muladd true
       :rates #{:ar :kr}
       :doc "hard sync sawtooth wave oscillator

A sawtooth wave that is hard synched to a fundamental pitch. This produces an effect similar to  moving formants or pulse width modulation. The sawtooth oscillator has its phase reset when the sync oscillator completes a cycle. This is not a band limited waveform, so it may alias."}

      {:name "WrapIndex" :extends "Index"
       :doc "the input signal value is truncated to an integer value and used as an index into the table
            (out of range index values are wrapped)"}

      {:name "IndexInBetween" :extends "Index"
       :doc "finds the (lowest) point in the buffer at which the input signal lies in-between the two values, and returns the index"}

      {:name "DetectIndex" :extends "Index"
       :doc "search a buffer for a value"}

      {:name "Shaper" :extends "Index"
       :doc "performs waveshaping on the input signal by indexing into a table"}

      {:name "DegreeToKey",
       :args [{:name "bufnum" :doc "Index of the buffer which contains the steps for each scale degree."}
              {:name "in", :default 0.0 :doc "The input signal."}
              {:name "octave", :default 12.0 :doc "The number of steps per octave in the scale. The default is 12."}]

       :rates #{:ar :kr}
       :doc "the input signal value is truncated to an integer value and used as an index into an octave repeating table of note values
            (indices wrap around the table)"}


      {:name "Select",
       :args [{:name "which" :doc "Index of array to select"}
              {:name "array", :array true :doc "List of ugens to choose from"}]
       :rates #{:ar :kr}
       :doc "select the output signal from an array of inputs"}

      {:name "Vibrato",
       :args [{:name "freq", :default 440.0}
              {:name "rate", :default 6}
              {:name "depth", :default 0.02}
              {:name "delay", :default 0.0}
              {:name "onset", :default 0.0}
              {:name "rateVariation", :default 0.04}
              {:name "depthVariation", :default 0.1}
              {:name "iphase", :default 0.0}]
       :doc ""}
      ])

(def specs-collide
  [{:name "Index",
       :args [{:name "bufnum"}
              {:name "in", :default 0.0}]
       :doc "the input signal value is truncated to an integer and used as an index into the table"}])

(ns overtone.sc.ugen.osc
  (:use (overtone.sc.ugen common)))

(def specs
     [
      {:name "Osc",
       :args [{:name "buffer" :doc "lookup buffer"}
              {:name "freq", :default 440.0 :doc "frequency in Hertz"}
              {:name "phase", :default 0.0 :doc "phase offset or modulator in radians"}],
       :doc "Linear interpolating wavetable lookup oscillator with frequency and phase modulation inputs.

This oscillator requires a buffer to be filled with a wavetable format signal.  This preprocesses the Signal into a form which can be used efficiently by the Oscillator.  The buffer size must be a power of 2.

This can be acheived by creating a Buffer object and sending it one of the b_gen  messages ( sine1, sine2, sine3 ) with the wavetable flag set to true.

This can also be acheived by creating a Signal object and sending it the 'asWavetable' message, saving it to disk, and having the server load it from there."}

      {:name "SinOsc",
       :args [{:name "freq", :default 440.0 :doc "frequency in Hertz"}
              {:name "phase", :default 0.0 :doc "phase offset or modulator in radians"}],
       :doc "sine table lookup oscillator

Note: This is the same as Osc except that the table has already been fixed as a sine table of 8192 entries."}

      {:name "SinOscFB",
       :args [{:name "freq", :default 440.0}
              {:name "feedback", :default 0.0}],
       :doc "very fast sine oscillator"}

      {:name "OscN",
       :args [{:name "bufnum" :doc "buffer index.  The buffer size must be a power of 2.  The buffer should NOT be filled using Wavetable format (b_gen commands should set wavetable flag to false.  Raw signals (not converted with asWavetable) can be saved to disk and loaded into the buffer."}
              {:name "freq", :default 440.0 :doc "frequency in Hertz"}
              {:name "phase", :default 0.0 :doc "phase offset or modulator in radians"}],
       :doc "Noninterpolating wavetable lookup oscillator with frequency and phase modulation inputs.

It is usually better to use the interpolating oscillator."}

      {:name "VOsc",
       :args [{:name "bufpos" :doc " buffer index. Can be swept continuously among adjacent wavetable buffers of the same size."}
              {:name "freq", :default 440.0 :doc "frequency in Hertz"}
              {:name "phase", :default 0.0 :doc "phase offset of modulator in radians"}],
       :doc "A wavetable lookup oscillator which can be swept smoothly across wavetables. All the wavetables must be allocated to the same size. Fractional values of table will interpolate between two adjacent tables.

This oscillator requires at least two buffers to be filled with a wavetable format signal.  This preprocesses the Signal into a form which can be used efficiently by the Oscillator.  The buffer size must be a power of 2.
"}

      {:name "VOsc3",
       :args [{:name "bufpos" :doc "buffer index. Can be swept continuously among adjacent wavetable buffers of the same size."}
              {:name "freq1", :default 110.0 :doc "frequency in Hertz of first oscillator"}
              {:name "freq2", :default 220.0 :doc "frequency in Hertz of second oscillator"}
              {:name "freq3", :default 440.0 :doc "frequency in Hertz of third oscillator"}],
       :doc "Three variable wavetable oscillators.

A wavetable lookup oscillator which can be swept smoothly across wavetables. All the wavetables must be allocated to the same size. Fractional values of table will interpolate between two adjacent tables. This unit generator contains three oscillators at different frequencies, mixed together.

This oscillator requires at least two buffers to be filled with a wavetable format signal.  This preprocesses the Signal into a form which can be used efficiently by the Oscillator.  The buffer size must be a power of 2. "}

      {:name "COsc",
       :args [{:name "bufnum" :doc "the number of a buffer filled in wavetable format"}
              {:name "freq", :default 440.0 :doc "frequency in Hertz"}
              {:name "beats", :default 0.5 :doc "beat frequency in Hertz"}],
       :doc "Chorusing wavetable lookup oscillator. Produces sum of two signals at  (freq +/- (beats / 2)). Due to summing, the peak amplitude is twice that of the wavetable."}

      {:name "Formant",
       :args [{:name "fundfreq", :default 440.0 :doc "fundamental frequency in Hertz (control rate)"}
              {:name "formfreq", :default 1760.0 :doc "formant frequency in Hertz (control rate)"}
              {:name "bwfreq", :default 880.0 :doc "pulse width frequency in Hertz. Controls the bandwidth of the formant (control rate)"}],
       :rates #{:ar},
       :doc "Generates a set of harmonics around a formant frequency at a given fundamental frequency.

The frequency inputs are read at control rate only, so if you use an audio rate UGen as an input, it will only be sampled at the start of each audio synthesis block.
"}

      {:name "LFSaw",
       :args [{:name "freq", :default 440.0 :doc "frequency in Hertz"}
              {:name "iphase", :default 0.0 :doc "initial phase offset. For efficiency reasons this is a value ranging from 0 to 2."}]
       :doc "low freq (i.e. not band limited) sawtooth oscillator"}

      {:name "LFPar" :extends "LFSaw"
       :doc "a non band-limited parabolic oscillator outputing a high of 1 and a low of zero."}

      {:name "LFCub" :extends "LFSaw"
       :doc "an oscillator outputting a sine like shape made of two cubic pieces"}

      {:name "LFTri" :extends "LFSaw"
       :doc "a non-band-limited triangle oscillator"}


      {:name "LFGauss",
       :args [{:name "duration", :default 1 :doc "duration of one full cycle ( for freq input: dur = 1 / freq )
"}
              {:name "width", :default 0.1 :doc "relative width of the bell. Best to keep below 0.25 when used as envelope."}
              {:name "iphase", :default 0.0 :doc "initial offset "}
              {:name "loop", :default 1 :doc "if loop is > 0, UGen oscillates. Otherwise it calls doneAction after one cycle"}
              {:name "action", :default 0 :map DONE-ACTIONS :doc "doneAction, which is evaluated after cycle completes"}]
       :rates #{:ar :kr}
       :doc "A non-band-limited gaussian function oscillator. Output ranges from minval to 1.

LFGauss implements the formula: f(x) = exp(squared(x - iphase) / (-2.0 * squared(width)))
where x is to vary in the range -1 to 1 over the period dur. minval is the initial value at -1
"}

      {:name "LFPulse",
       :args [{:name "freq", :default 440.0 :doc "frequency in Hertz"}
              {:name "iphase", :default 0.0 :doc "initial phase offset in cycles ( 0..1 )"
               }
              {:name "width", :default 0.5 :doc "pulse width duty cycle from zero to one"}]
       :signal-range :unipolar
       :rates #{:ar :kr}
       :doc "A non-band-limited pulse oscillator. Outputs a high value of one and a low value of zero.
"}

      {:name "VarSaw",
       :args [{:name "freq", :default 440.0 :doc "frequency in Hertz"}
              {:name "iphase", :default 0.0 :doc "initial phase offset in cycles ( 0..1 )"}
              {:name "width", :default 0.5 :doc "duty cycle from zero to one."}]
       :rates #{:ar :kr}
       :doc "a variable duty cycle saw wave oscillator"}


      {:name "Impulse",
       :args [{:name "freq", :default 440.0 :doc "frequency in Hertz"}
              {:name "phase", :default 0.0 :doc "phase offset in cycles ( 0..1 )"}]
       :signal-range :unipolar
       :rates #{:ar :kr}
       :doc "non band limited impulse oscillator"}


      {:name "SyncSaw",
       :args [{:name "syncFreq", :default 440.0 :doc "frequency of the fundamental."}
              {:name "sawFreq", :default 440.0 :doc "frequency of the slave synched sawtooth wave. sawFreq should always be greater than syncFreq."}]
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
       :args [{:name "bufnum" :doc "index of the buffer which contains the steps for each scale degree."}
              {:name "in", :default 0.0 :doc "the input signal."}
              {:name "octave", :default 12.0 :doc "the number of steps per octave in the scale. The default is 12."}]

       :rates #{:ar :kr}
       :doc "the input signal value is truncated to an integer value and used as an index into an octave repeating table of note values
            (indices wrap around the table)"}


      {:name "Select",
       :args [{:name "which" :doc "index of array to select"}
              {:name "array", :array true :doc "list of ugens to choose from"}]
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

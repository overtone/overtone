(ns overtone.sc.machinery.ugen.metadata.ff-osc
  (:use [overtone.sc.machinery.ugen common check]))

(def specs
     [
      {:name "FSinOsc",
       :args [{:name "freq", :default 440.0 :doc "frequency in Hertz"}
              {:name "iphase", :default 0.0 :doc "phase offset or modulator in radians"}]
       :doc "Very fast sine wave generator (2 PowerPC instructions per output sample!) implemented using a ringing filter.  This generates a much cleane sine wave than a table lookup oscillator and is a lot faster. However, the amplitude of the wave will vary with frequency. Generally the amplitude will go down as you raise the frequency and go up as you lower the frequency.

  WARNING: In the current implementation, the amplitude can blow up if the frequency is modulated by certain alternating signals."}


      {:name "Klang",
       :args [{:name "specs", :mode :append-sequence :doc "An array of three arrays frequencies, amplitudes and phases:  1) an array of filter frequencies, 2) an Array of filter amplitudes, or nil. If nil, then amplitudes default to 1.0, 3) an Array of initial phases, or nil. If nil, then phases default to 0.0."}
              {:name "freqscale", :default 1.0 :doc "a scale factor multiplied by all frequencies at initialization time."}
              {:name "freqoffset", :default 0.0 :doc "an offset added to all frequencies at initialization time."}],
       :rates #{:ar}
       :init (fn [rate args spec]
               (let [[[ freqs amps times]] args
                     amps                  (or amps (repeat (count freqs) 1.0))
                     times                 (or times (repeat (count freqs) 1.0))
                     fats                  (map vector freqs amps times)
                     new-args              (concat [fats] (rest args))]
                 new-args))
       :doc "Klang is a bank of fixed frequency sine oscillators. Klang is more efficient than creating individual oscillators but offers less flexibility.

  The specs can't be changed after it has been started.
  For a modulatable but less efficient version, see dyn-klang."}



      {:name "Klank",
       :args [{:name "specs"
               :mode :append-sequence
               :doc "An array of three arrays: frequencies, amplitudes
                     and ring times: *all arrays should have the same
                     length* 1) an Array of filter frequencies. 2) an
                     Array of filter amplitudes, or nil. If nil, then
                     amplitudes default to 1.0 3) an Array of 60 dB
                     decay times for the filters." }

              {:name "input"
               :doc "the excitation input to the resonant filter bank."}

              {:name "freqscale",
               :default 1.0
               :doc "a scale factor multiplied by all frequencies at
                     initialization time."}

              {:name "freqoffset"
               :default 0.0
               :doc "an offset added to all frequencies at
                     initialization time."}

              {:name "decayscale"
               :default 1.0
               :doc "a scale factor multiplied by all ring times at
                     initialization time."}]
       :rates #{:ar}
       :init (fn [rate args spec]
               (let [[[ freqs amps times]] args
                     amps                  (or amps (repeat (count freqs) 1.0))
                     times                 (or times (repeat (count freqs) 1.0))
                     fats                  (map vector freqs amps times)
                     new-args              (concat [fats] (rest args))]
                 new-args))
       :check (nth-input-stream? 0)
       :doc "Klank is a bank of fixed frequency resonators which can be
             used to simulate the resonant modes of an object. Each mode
             is given a ring time, which is the time for the mode to
             decay by 60 dB.

             The specs can't be changed after it has been started.  For
             a modulatable but less efficient version, see dyn-klank." }

      {:name "Blip",
       :args [{:name "freq", :default 440.0 :doc "Frequency in Hertz (control rate)"}
              {:name "numharm", :default 200.0 :doc "Number of harmonics. This may be lowered internally if it would cause aliasing."}],
       :rates #{:ar}
       :doc "Band Limited Impulse generator. All harmonics have equal amplitude. This is the equivalent of buzz in MusicN languages.
  WARNING: This waveform in its raw form could be damaging to your ears at high amplitudes or for long periods.

  It is improved from other implementations in that it will crossfade in a control period when the number of harmonics changes, so that there are no audible pops. It also eliminates the divide in the formula by using a 1/sin table (with special precautions taken for 1/0).  The lookup tables are linearly interpolated for better quality.

  Synth-O-Matic (1990) had an impulse generator called blip, hence that name here rather than 'buzz'."}


      {:name "Saw",
       :args [{:name "freq", :default 440.0 :doc "Frequency in Hertz (control rate)."}],
       :rates #{:ar}
       :summary "band limited sawtooth wave generator"
       :doc "The sawtooth wave projeces even and odd harmonics in series
             and therefore produces a bright sound that is an excellent
             starting point for brassy, raspy sounds. It's also suitable
             for creating the gritty, bright sounds needed for leads and
             raspy basses. Due to its harmonic richness it's extremely
             suitable for use with sounds that will be filter swept." }

      {:name "Pulse",
       :args [{:name "freq", :default 440.0, :doc "Frequency in Hertz (control rate)"}
              {:name "width", :default 0.5, :doc "Pulse width ratio from zero to one. 0.5 makes a square wave (control rate)"}],
       :rates #{:ar}
       :summary "band limited pulse wave generator with pulse width modulation."
       :doc "Pulse waves are a general form of square wave that allow
             for the width of the pulses ot be varied. A square wave is
             therefore a pulse with a width of 0.5 i.e. the width of the
             high and low states is identical.

             Adjusting the ratio of the pulse width will vary the
             harmonic content of the sound. For example, reductions in
             the width allow you to produce thin reed-like tibres along
             with the wide, hollow sounds created by a square wave." }


      {:name "PSinGrain",
       :args [{:name "freq", :default 440.0 :doc "frequency in cycles per second. Must be a scalar"}
              {:name "dur", :default 0.2 :doc "grain duration"}
              {:name "amp", :default 1.0 :doc "amplitude of grain"}],
       :rates #{:ar}
       :doc "Fixed frequency sine oscillator

this ugen uses a very fast algorithm for generating a sine wave at a fixed frequency"}
      ])

(ns overtone.sc.machinery.ugen.metadata.filter
  (:use [overtone.sc.machinery.ugen common check]))

;; Filter : UGen {
;;      checkInputs { ^this.checkSameRateAsFirstInput }
;; }

(def muladd-specs
  [
   {:name "Resonz",
    :args [{:name "in"
            :default 0.0
            :doc "input signal to be processed"}

           {:name "freq"
            :default 440.0
            :doc "resonant frequency in Hertz"}

           {:name "bwr"
            :default 1.0
            :doc "bandwidth ratio (reciprocal of Q).
                  rq = bandwidth / centerFreq"}]
    :check (nth-input-stream? 0)
    :doc "A Note on Constant-Gain Digital Resonators,\" Computer Music
          Journal, vol 18, no. 4, pp. 8-10, Winter 1994.\" Computer
          Music Journal, vol 18, no. 4, pp. 8-10, Winter 1994."
    :auto-rate true}


   {:name "OnePole",
    :args [{:name "in"
            :default 0.0
            :doc "input signal to be processed"}

           {:name "coef"
            :default 0.5
            :doc "feedback coefficient. Should be between -1 and +1"}]
    :check (nth-input-stream? 0)
    :doc "A one pole filter. Implements the formula:

          out(i) = ((1 - abs(coef)) * in(i)) + (coef * out(i-1))"
    :auto-rate true}


   {:name "OneZero",
    :args [{:name "in"
            :default 0.0
            :doc "input signal to be processed"}

           {:name "coef"
            :default 0.5
            :doc "feed forward coefficient. +0.5 makes a two point
                  averaging filter (see also lpz1), -0.5 makes a
                  differentiator (see also hpz1), +1 makes a single
                  sample delay (see also delay1), -1 makes an inverted
                  single sample delay." }]
    :check (nth-input-stream? 0)
    :doc "A one zero filter. Implements the formula :

          out(i) = ((1 - abs(coef)) * in(i)) + (coef * in(i-1))"
    :auto-rate true}


   {:name "TwoPole",
    :args [{:name "in"
            :default 0.0
            :doc "input signal to be processed"}

           {:name "freq"
            :default 440.0
            :doc "frequency of pole angle"}

           {:name "radius"
            :default 0.8
            :doc "radius of pole. Should be between 0 and +1"}]

    :check (nth-input-stream? 0)
    :doc "a two pole filter. This provides lower level access to setting
          of pole location.

          For general purposes Resonz is better."
    :auto-rate true}


   {:name "TwoZero",
    :args [{:name "in"
            :default 0.0
            :doc "input signal to be processed"}

           {:name "freq"
            :default 440.0
            :doc "frequency of zero angle"}

           {:name "radius"
            :default 0.8
            :doc "radios of zero"}]

    :check (nth-input-stream? 0)
    :doc "a two zero filter"
    :auto-rate true}


   {:name "APF",
    :args [{:name "in"
            :default 0.0}

           {:name "freq"
            :default 440.0}

           {:name "radius"
            :default 0.8}]

    :check (nth-input-stream? 0)
    :doc ""
    :auto-rate true}


   {:name "Integrator",
    :args [{:name "in"
            :default 0.0
            :doc "input signal"}

           {:name "coef"
            :default 1.0
            :doc "leak coefficient"}]

    :check (nth-input-stream? 0)
    :doc "leaky integrator. Integrates an input signal with a leak. The
          formula implemented is:

          out(0) = in(0) + (coef * out(-1))"
    :auto-rate true}


   {:name "Decay"
    :summary "triggered exponential decay."
    :args [{:name "in"
            :default 0.0
            :doc "input signal"}

           {:name "decay-time"
            :default 1.0
            :doc "60 dB decay time in seconds"}]

    :check (nth-input-stream? 0)
    :doc "This is essentially the same as integrator except that
          instead of supplying the coefficient directly, it is
          calculated from a 60 dB decay time. This is the time required
          for the integrator to lose 99.9 % of its value or -60dB. This
          is useful for exponential decaying envelopes triggered by
          impulses."
    :auto-rate true}


   {:name "Decay2",
    :args [{:name "in"
            :default 0.0
            :doc "input signal"}

           {:name "attack-time"
            :default 0.01
            :doc "60 dB attack time in seconds."}

           {:name "decay-time"
            :default 1.0
            :doc "60 dB decay time in seconds."}]

    :check (nth-input-stream? 0)
    :doc "triggered exponential attack and exponential decay. Decay has
          a very sharp attack and can produce clicks. Decay2 rounds off
          the attack by subtracting one Decay from another. (decay in
          attack-time decay-time) equivalent to: (- (decay in
          attack-time decay-time) (decay in attack-time decay-time))"
    :auto-rate true}


   {:name "Lag",
    :args [{:name "in"
            :default 0.0
            :doc "input signal"}

           {:name "lag-time"
            :default 0.1
            :doc "60 dB lag time in seconds"}]

    :check (nth-input-stream? 0)
    :doc "exponential lag, useful for smoothing out control
          signals. This is essentially the same as OnePole except that
          instead of supplying the coefficient directly, it is
          calculated from a 60 dB lag time. This is the time required
          for the filter to converge to within 0.01 % of a value."
    :auto-rate true}


   {:name "Lag2",
    :args [{:name "in"
            :default 0.0
            :doc "input signal"}

           {:name "lag-time"
            :default 0.1
            :doc "60 dB lag time in seconds"}]

    :check (nth-input-stream? 0)
    :doc "equivalent to (lag (lag in time) time), resulting in a
          smoother transition. This saves on CPU as you only have to
          calculate the decay factor once instead of twice. See lag for
          more details."
    :auto-rate true}


   {:name "Lag3",
    :args [{:name "in"
            :default 0.0
            :doc "input signal"}

           {:name "lag-time"
            :default 0.1
            :doc "60 dB lag time in seconds"}]

    :check (nth-input-stream? 0)
    :doc "lag3 is equivalent to (lag (lag (lag in time) time) time),
          thus resulting in a smoother transition. This saves on CPU as
          you only have to calculate the decay factor once instead of
          three times. See Lag for more details."
    :auto-rate true}


   {:name "Ramp",
    :args [{:name "in"
            :default 0.0
            :doc "input signal"}

           {:name "lag-time"
            :default 0.1
            :doc "60 dB lag time in seconds"}]
    :check (nth-input-stream? 0)
    :doc "similar to lag but with a linear rather than exponential lag,
          useful for smoothing out control signals"
    :auto-rate true}


   {:name "LagUD",
    :args [{:name "in"
            :default 0.0
            :doc "input signal"}

           {:name "lag-time-up"
            :default 0.1
            :doc "60 dB lag time in seconds for the upgoing signal"}

           {:name "lag-time-down"
            :default 0.1
            :doc "60 dB lag time in seconds for the downgoing signal"}]

    :check (nth-input-stream? 0)
    :doc "the same as Lag except that you can supply a different 60 dB
          time for when the signal goes up, from when the signal goes down"
    :auto-rate true}


   {:name "Lag2UD",
    :args [{:name "in"
            :default 0.0
            :doc "input signal"}

           {:name "lag-time-up"
            :default 0.1
            :doc "60 dB lag time in seconds for the upgoing signal"}

           {:name "lag-time-down"
            :default 0.1
            :doc "60 dB lag time in seconds for the downgoing signal"}]

    :check (nth-input-stream? 0)
    :doc "equivalent to (lag-ud (lag-ud in up-t down-t) up-t down-t)
          thus resulting in a smoother transition. This saves on CPU as
          you only have to calculate the decay factor once instead of
          twice. See Lag for more details."
    :auto-rate true}


   {:name "Lag3UD",
    :args [{:name "in"
            :default 0.0
            :doc "input signal"}

           {:name "lag-time-up"
            :default 0.1
            :doc "60 dB lag time in seconds for the upgoing signal"}

           {:name "lag-time-down"
            :default 0.1
            :doc "60 dB lag time in seconds for the downgoing signal"}]

    :check (nth-input-stream? 0)
    :doc "equivalent to

         (lag-ud (lag-ud (lag-ud (in up-t down-t) up-t down-t) up-t, down-t)

         thus resulting in a smoother transition. This saves on CPU as
         you only have to calculate the decay factor once instead of
         three times."
    :auto-rate true}


   {:name "LeakDC",
    :args [{:name "in"
            :default 0.0
            :doc "input signal"}

           {:name "coef"
            :default 0.995
            :doc "leak coefficient. A value of 1 indicates no leakage
                  and 0 indicates high leakage - essentially the rate at
                  which the offset will return back to 0"}]
    :check (nth-input-stream? 0)
    :doc "removes a DC offset from signal. For example, a square wave
          contains prolonged sections of the cycle which are at +1 and
          -1 (the top and bottom of the square sections). If you were to
          pass this wave through leak-dc, then these top parts would
          taper back towards 0 with a greater slope as you move coef
          from 1 to 0..

          Good starting point coef values are to 0.995 for audio rate and 0.9
          for control rate"
    :auto-rate true}


   {:name "RLPF"
    :summary "resonant low pass filter"
    :args [{:name "in"
            :default 0.0
            :doc "input signal to be processed"}

           {:name "freq"
            :default 440.0
            :doc "cutoff frequency"}

           {:name "rq"
            :default 1.0
            :doc "the reciprocal of Q.  bandwidth / cutoffFreq. A lower
                  rq results in more resonance." }]
    :check (nth-input-stream? 0)
    :doc "A resonant low pass filter is a standard subtractive synthesis
          tool which removes frequencies above a defined cut-off
          point. This typically has the effect of making bright sounds
          duller. However, in addition to this behaviour, the resonant
          low pass filter also emphasises/resonates the frequencies
          around the cutoff point. The amount of emphasis is
          controlled by the rq param with a lower rq resulting in
          greater resonance. High amounts of resonance (rq ~0) can
          create a whistling sound around the cutoff frequency.

          Using a low pass filter allows you to have fine-grained
          control of the level of brightness/dullness to tune your
          timbre in addition to allowing you to modulate the effect in
          real time thus creating movement in the sound."
    :auto-rate true}


   {:name "RHPF"
    :summary "resonant high pass filter"
    :args [{:name "in"
            :default 0.0
            :doc "input signal to be processed"}

           {:name "freq"
            :default 440.0
            :doc "cutoff frequency"}

           {:name "rq"
            :default 1.0
            :doc "the reciprocal of Q.  bandwidth / cutoffFreq. A lower
                  rq results in more resonance"}]
    :check (nth-input-stream? 0)

    :doc "A resonant high pass filter lets through the frequencies above
          the cutoff point and successfily dampens the frequencies below
          the cutoff point. This effectively removes the fundamental
          frequency of the sound, leaving only the fizz harmonic
          overtones. However, in addition to this behaviour, the
          resonant low pass filter also emphasises/resonates the
          frequencies around the cutoff point. The amount of emphasis
          is controlled by the rq param with a lower rq resulting in
          greater resonance. High amounts of resonance (rq ~0) can
          create a whistling sound around the cutoff frequency.

          High pass filters are rarely used in the creation of
          instruments and are predominantly used to create effervescent
          sound effects of bright tibres that can be laid over the top
          of another low pass sound to increase the harmonic content."
    :auto-rate true}


   {:name "LPF",
    :summary "second order Butterworth low pass filter"
    :args [{:name "in"
            :default 0.0
            :doc "input signal to be processed"}

           {:name "freq"
            :default 440.0
            :doc "cutoff frequency"}]

    :check (nth-input-stream? 0)
    :doc "A low pass filter is a standard subtractive synthesis tool
          which removes frequencies above a defined cut-off point. This
          typically has the effect of making bright sounds
          duller. Using a low pass filter allows you to have fine-grained
          control of the level of brightness/dullness to tune your
          timbre in addition to allowing you to modulate the effect in
          real time thus creating movement in the sound."
    :auto-rate true}


   {:name "HPF",
    :summary "second order high pass filter"
    :args [{:name "in"
            :default 0.0
            :doc "input signal to be processed"}

           {:name "freq"
            :default 440.0
            :doc "cutoff frequency"}]

    :check (nth-input-stream? 0)
    :doc "A high pass filter lets through the frequencies above the
          cutoff point and successfily dampens the frequencies below the
          cutoff point. This effectively removes the fundamental
          frequency of the sound, leaving only the fizz harmonic
          overtones.

          High pass filters are rarely used in the creation of
          instruments and are predominantly used to create effervexcent
          sound effects of bright tibres that can be laid over the top
          of another low pass sound to increase the harmonic content."
    :auto-rate true}


   {:name "BPF",
    :summary "second order Butterworth bandpass filter"
    :args [{:name "in"
            :default 0.0
            :doc "input signal to be processed"}

           {:name "freq"
            :default 440.0
            :doc "centre frequency in Hertz"}

           {:name "rq"
            :default 1.0
            :doc "the reciprocal of Q.  bandwidth / cutoffFreq"}]

    :check (nth-input-stream? 0)
    :doc "A band pass filter permits the frequencies around a specified
          centre frequency to pass unaltered through the filter while
          the frequencies either side are attenuated. The frequences
          that pass through are known as the bandwidth or the band pass
          of the filter.

          Used to create timbres consisting of fizzy harmonics, lo-fi
          qualities or very thin sounds that may form the basis of sound
          effects."
    :auto-rate true}


   {:name "BRF"
    :summary "second order Butterworth band reject filter"
    :args [{:name "in"
            :default 0.0
            :doc "input signal to be processed"}

           {:name "freq"
            :default 440.0
            :doc "centre frequency in Hertz"}

           {:name "rq"
            :default 1.0
            :doc "the reciprocal of Q.  bandwidth / cutoffFreq"}]

    :check (nth-input-stream? 0)
    :doc "Band reject filters, also known as notch filters, attenuate a
          selected range of frequencies effectively creating a notch in
          the sound.

          This type of filter is handy for scooping out frequencies,
          thinning out a sound while leaving the fundamental intact,
          making them useful for creating timbres that contain a
          discernable pitch but do not have a high level of harmonic
          content."
    :auto-rate true}


   {:name "MidEQ",
    :args [{:name "in"
            :default 0.0
            :doc "input signal to be processed"}

           {:name "freq"
            :default 440.0
            :doc "center frequency of the band in Hertz"}

           {:name "rq"
            :default 1.0
            :doc "the reciprocal of Q.  bandwidth / cutoffFreq"}

           {:name "db"
            :default 0.0
            :doc "amount of boost (db > 0) or attenuation (db < 0) of
                  the frequency band"}]
    :check (nth-input-stream? 0)
    :doc "attenuates or boosts a frequency band"
    :auto-rate true}


   {:name "LPZ1"
    :args [{:name "in"
            :default 0.0}]

    :check (nth-input-stream? 0)
    :doc "two point average filter. Implements the formula:

          out(i) = 0.5 * (in(i) + in(i-1))"
    :auto-rate true}


   {:name "LPZ2"
    :args [{:name "in"
            :default 0.0}]

    :check (nth-input-stream? 0)
    :doc "two zero fixed lowpass. Implements the formula:

          out(i) = 0.25 * (in(i) + (2*in(i-1)) + in(i-2))"
    :auto-rate true}


   {:name "HPZ1"
    :args [{:name "in"
            :default 0.0}]

    :check (nth-input-stream? 0)
    :doc "two point difference filter. Implements the formula:

          out(i) = 0.5 * (in(i) - in(i-1))"
    :auto-rate true}


   {:name "HPZ2"
    :args [{:name "in"
            :default 0.0}]

    :check (nth-input-stream? 0)
    :doc "two zero fixed highpass. Implements the formula:

          out(i) = 0.25 * (in(i) - (2*in(i-1)) + in(i-2))"
    :auto-rate true}


   {:name "Slope"
    :args [{:name "in"
            :default 0.0
            :doc "input signal to measure"}]

    :check (nth-input-stream? 0)
    :doc "Measures the rate of change per second of a signal. Formula implemented is:

          out[i] = (in[i] - in[i-1]) * sampling_rate"
    :auto-rate true}


   {:name "BPZ2"
    :args [{:name "in"
            :default 0.0}]
    :check (nth-input-stream? 0)
    :doc "two zero fixed midpass which cuts out 0 Hz and the Nyquist
          frequency. Implements the formula:

          out(i) = 0.5 * (in(i) - in(i-2))"
    :auto-rate true}


   {:name "BRZ2"
    :args [{:name "in"
            :default 0.0}]

    :check (nth-input-stream? 0)
    :doc "two zero fixed midcut which cuts out frequencies around 1/2 of
          the Nyquist frequency. Implements the formula:

          out(i) = 0.5 * (in(i) + in(i-2))"
    :auto-rate true}


   {:name "Median",
    :args [{:name "length"
            :default 3.0
            :doc "number of input points in which to find the
                  median. Must be an odd number from 1 to 31. If length
                  is 1 then Median has no effect." }

           {:name "in"
            :default 0.0
            :doc "Input signal to be processed"}]

    :check (nth-input-stream? 1)
    :doc "returns the median of the last length input points. This non
          linear filter is good at reducing impulse noise from a
          signal."
    :rates #{:ar :kr}
    :auto-rate true}


   {:name "Slew",
    :args [{:name "in"
            :default 0.0
            :doc "input signal"}

           {:name "up"
            :default 1.0
            :doc "maximum upward slope"}

           {:name "dn"
            :default 1.0
            :doc "maximum downward slope"}]

    :check (nth-input-stream? 0)
    :doc "Smooth the curve by limiting the slope of the input signal to
          up and dn"
    :auto-rate true}


   {:name "FOS",
    :args [{:name "in"
            :default 0.0
            :doc "input signal"}

           {:name "a0"
            :default 0.0
            :doc "first coefficient"}

           {:name "a1"
            :default 0.0
            :doc "second coefficient"}

           {:name "b1"
            :default 0.0
            :doc "third coefficient"}]

    :check (nth-input-stream? 0)
    :doc "first order filter section. Formula is equivalent to:

          out(i) = (a0 * in(i)) + (a1 * in(i-1)) + (b1 * out(i-1))"
    :auto-rate true}


   {:name "SOS",
    :args [{:name "in"
            :default 0.0
            :doc "input signal"}

           {:name "a0"
            :default 0.0
            :doc "1st coefficient"}

           {:name "a1"
            :default 0.0
            :doc "2nd coefficient"}

           {:name "a2"
            :default 0.0
            :doc "3rd coefficient"}

           {:name "b1"
            :default 0.0
            :doc "4th coefficient"}

           {:name "b2"
            :default 0.0
            :doc "5th coefficient"}]

    :check (nth-input-stream? 0)
    :doc "second order filter section (biquad). Formula is equivalent
          to:

          out(i) = (a0 * in(i)) +
                   (a1 * in(i-1)) +
                   (a2 * in(i-2)) +
                   (b1 * out(i-1)) +
                   (b2 * out(i-2))"
    :auto-rate true}


   {:name "Ringz",
    :args [{:name "in"
            :default 0.0
            :doc "input signal to be processed"}

           {:name "freq"
            :default 440.0
            :doc "resonant frequency in Hertz"}

           {:name "decay-time"
            :default 1.0
            :doc "the 60 dB decay time of the filter"}]

    :check (nth-input-stream? 0)
    :doc "Ringz is the same as Resonz, except that instead of a
          resonance parameter, the bandwidth is specified in a 60dB ring
          decay time. One Ringz is equivalent to one component of the
          klank ugen"
    :auto-rate true}


   {:name "Formlet",
    :args [{:name "in"
            :default 0.0
            :doc "input signal to be processed"}

           {:name "freq"
            :default 440.0
            :doc "resonant frequency in Hertz"}

           {:name "attack-time"
            :default 1.0
            :doc "60 dB attack time in seconds"}

           {:name "decay-time"
            :default 1.0
            :doc "60 dB decay time in seconds"}]

    :check (nth-input-stream? 0)
    :doc "a resonant filter whose impulse response is like that of a
          sine wave with a Decay2 envelope over it. The great advantage
          to this filter over FOF is that there is no limit to the
          number of overlapping grains since the grain is just the
          impulse response of the filter. Note that if attacktime ==
          decaytime then the signal cancels out and if attacktime >
          decaytime then the impulse response is inverted."
    :auto-rate true}])

(def detect-silence
  {:name "DetectSilence",
   :args [{:name "in"
           :default 0.0
           :doc "any source"}

          {:name "amp"
           :default 0.0001
           :doc "when input falls below this, evaluate done action"}

          {:name "time"
           :default 0.1
           :doc "the minimum duration of the input signal which input
                 must fall below thresh before this triggers. The
                 default is 0.1 seconds"}

          {:name "action"
           :default 0
           :doc "the action to perform when silence is
                 detected. Default: NO-ACTION"}],
   :num-outs 1
   :check-inputs same-rate-as-first-input
   :check (nth-input-stream? 0)
   :doc "If the signal input starts with silence at the beginning of the
         synth's duration, then DetectSilence will wait indefinitely
         until the first sound before starting to monitor for
         silence. This UGen outputs 1 if silence is detected, otherwise
         0."
   :rates #{:ar :kr}
   :auto-rate true})

(def specs
     (conj (map #(assoc % :check-inputs same-rate-as-first-input)
                muladd-specs)
           detect-silence))

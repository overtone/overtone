(ns overtone.sc.machinery.ugen.metadata.extras.ay
  (:use [overtone.sc.machinery.ugen common check]))

(def specs
  [
   {:name "AY"
    :summary "Emulator of the AY (aka YM) soundchip, used in Spectrum/Atari"
    :args [{:name "tonea"
            :default 1777
            :doc "Integer tone of the first voice from 0 to
                  4095 (i.e. 12-bit range). Higher value = lower pitch."}

           {:name "toneb"
            :default 1666
            :doc "Integer tone of the second voice from 0 to
                  4095 (i.e. 12-bit range). Higher value = lower pitch."}

           {:name "tonec"
            :default 1555
            :doc "Integer tone of the third voice from 0 to
                  4095 (i.e. 12-bit range). Higher value = lower pitch."}

           {:name "noise"
            :default 1
            :doc "the period of the pseudo-random noise generator, 0 to 31"}

           {:name "control"
            :default 7
            :doc "controls how the noise is mixed into the tone(s), 0 to
                  32 (0 is mute). This is a binary mask value which
                  masks the noise/tone mixture in each channel, so it's
                  not linear." }

           {:name "vola"
            :default 15
            :doc "volume of the first channel -0 to 15 (or 0 to 31 if
                  using YM chiptype)"}

           {:name "volb"
            :default 15
            :doc "volume of the second channel -0 to 15 (or 0 to 31 if
                  using YM chiptype)"}

           {:name "volc"
            :default 15
            :doc "volume of the third channel -0 to 15 (or 0 to 31 if
                  using YM chiptype)"}

           {:name "envfreq"
            :default 4
            :doc "envelope frequency, 0 to 4095"}

           {:name "envstyle"
            :default 1
            :doc "type of envelope used, 0 to 15"}

           {:name "chiptype"
            :default 0
            :doc "0 for AY (default), 1 for YM. The models behave
                 slightly differently. This input cannot be modulated -
                 its value is only handled at the moment the UGen
                 starts." }]
    :rates #{:ar}
    :doc "Emulates the General Instrument AY-3-8910 (a.k.a. the Yamaha
          YM2149) 3-voice sound chip, as found in the ZX Spectrum 128,
          the Atari ST, and various other home computers during the
          1980s.

          The chip's inputs are integer values, so non-integer values
          will be rounded off.

          The emulation is provided by the libayemu library:
          http://sourceforge.net/projects/libayemu

          To turn a frequency to a compatable tone:
          (/ 110300 (- freq 0.5)"}])

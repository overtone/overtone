(ns overtone.sc.examples.blackrain
  (:use [overtone.sc.machinery defexample]
        [overtone.sc ugens]))

(defexamples iir-filter
  (:low-pass
   "Create a low pass filter"
   "Here we use blackrain's iir-filter to create a low pass filter.  The iir-filter has a 24db/oct rolloff.  That means the frequency one octave past the cutoff frequency is attenuated by 24dB.  Two octaves past the cutoff, the attenuation is 48dB, and so on.  However, near the frequency cutoff the frequency response is determined by rq.  Depending on your rq some frequencies near the cutoff may even be boosted while others may decrease more than the rolloff.  As you get farther from the cutoff frequency, you'll return to the 24dB/oct rolloff.  
   

For input signal, we'll use a saw wave because it has lots of harmonics.  For the cutoff frequency we'll use frequencies ranging from 20 to 20000 Hz exponentially, depending on the mouse-x coordinate.  The rq ranges from 0.1 to 1 linearly depending on the mouse-y coordinate so that moving the mouse up and down selects and boosts or attenuates frequencies differently."
   rate :ar
   [signal-in {:default (saw 440) :doc "Input signal to the filter.  The default here is a saw wave at 440Hz since that has nifty harmoics."}]
   "(iir-filter signal-in (mouse-x 20 20000 EXP) (mouse-y 0.1 1))"
   contributor "Colleen T"))

(defexamples b-moog
   (:compare-filters
   "Compare low, high, and bandpass"
   "This example demonstrates how three different filters with a variable cutoff frequency change the same input signal.  The y mouse coordinate will control which filter you are using by breaking the screen into three regions.  The top third is bandpass, the middle third is high pass, and the lower third is low pass.  The x-coordinate is controlling q, which is bandwidth/cutoff frequency.  Although bandwidth doesn't mean much for low and high pass filters, changing this parameter will change the filter characteristics.  Move the mouse around to explore and compare the filter properties."
   rate :ar
   [signal-in {:default (saw 265) :doc "The default is a saw wave with a frequency of 265 Hz.  Maybe you have a sound handy you'd like to try to filter?"}
    cutoff-frequency {:default 440 :doc "Try changing the cutoff frequency to see how that affects the input."}]
   "(b-moog signal-in cutoff-frequency (mouse-x 0 1) (mouse-y 0.01 2.99))"
   contributor "Colleen T"))

(defexamples svf
    (:state-filter
    "Create a state variable filter"
    "A state variable filter is many filters combined in one - low, high, band, notch.  In this example, we'll use a saw wave with the frequency ranging from 260 to 700 with the mouse-x coordinate.  A saw wave has many harmonics so it is ideal for this kind of filter.  We'll put the cutoff frequency at 440 and use the mouse-y, ranging from 0 to 1 to control the resonance.  Consider how you can use your current home setup (keyboard or other controller) to vary some of the parameters in time.  A state variable filter is ideal for modifying the filter properties on the fly to change the characteristics of the sound."
    rate :ar
    [signal-in {:default (saw (mouse-x 260 700)) :doc "The input signal to filter.  The example default here is a saw wave because a saw wave has interesting harmonics you can select with the filter."}
     cutoff {:default 440 :doc "The cutoff frequency the filter is centered on, try adjusting this to see how your selection from the input signal changes."}
     low-pass {:default 0.1 :doc "The amount of the low pass output to select, from 0 (none) to 1 (all)"}
     band-pass {:default 0.3 :doc "The amount of the band pass output to select."}
     high-pass {:default 0 :doc "The amount of the highpass output to use."}
     notch {:default 0.4 :doc "A notch filter is the opposite of a band-pass filter."}
    ]
    "(svf signal-in cutoff (mouse-y 0 1) low-pass band-pass high-pass notch)"
    contributor "Colleen T"))

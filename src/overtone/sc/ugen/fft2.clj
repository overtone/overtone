(ns overtone.sc.ugen.fft2
  (:use [overtone.sc.ugen common constants]))

;; //third party FFT UGens

(def specs
     [

      {:name "PV_ConformalMap",
       :args [{:name "buffer" :doc "buffer number of buffer to act on, passed in through a chain (see examples below)."}
              {:name "areal", :default 0.0 :doc "real part of a."}
              {:name "aimag", :default 0.0 :doc "imaginary part of a."}],
       :rates #{:kr}
       :doc "Applies the conformal mapping z -> (z-a)/(1-za*) to the phase vocoder bins z with a given by the real and imag inputs to the UGen.

ie, makes a transformation of the complex plane so the output is full of phase vocoder artifacts but may be musically fun. Usually keep |a|<1 but you can of course try bigger values to make it really noisy. a=0 should give back the input mostly unperturbed.

See http://mathworld.wolfram.com/ConformalMapping.html"}

      {:name "Convolution",
       :args [{:name "in" :doc "processing target"}
              {:name "kernel" :doc "processing kernel."}
              {:name "framesize", :default 512.0 :doc "size of FFT frame, must be a power of two"}],
       :rates #{:ar}
       :doc "Strict convolution of two continuously changing inputs. Also see convolution2 for a cheaper CPU cost alternative for the case of a fixed kernel which can be changed with a trigger message.

See Steven W Smith, The Scientist and Engineer's Guide to Digital Signal Processing: chapter 18:  http:// www.dspguide.com/ch18.htm"}

      {:name "Convolution2",
       :args [{:name "in" :doc "processing target"}
              {:name "kernel" :doc "buffer index for the fixed kernel, may be modulated in combination with the trigger"}
              {:name "trigger" :doc "update the kernel on a change from <= 0 to > 0"}
              {:name "framesize", :default 512.0 :doc "size of FFT frame, must be a power of two. Convolution uses twice this number internally, maximum value you can give this argument is 2^16 = 65536. Note that it gets progressively more expensive to run for higher powers! 512, 1024, 2048, 4096 standard."}],
       :rates #{:ar}
       :doc "Strict convolution with fixed kernel which can be updated using a trigger signal.

See Steven W Smith, The Scientist and Engineer's Guide to Digital Signal Processing: chapter 18: http:// www.dspguide.com/ch18.htm "}


      {:name "Convolution2L",
       :args [{:name "in" :doc "processing target"}
              {:name "kernel" :doc "buffer index for the fixed kernel, may be modulated in combination with the trigger"}
              {:name "trigger" :doc "update the kernel on a change from <= 0 to > 0"}
              {:name "framesize", :default 512.0 :doc "size of FFT frame, must be a power of two. Convolution uses twice this number internally, maximum value you can give this argument is 2^16=65536. Note that it gets progressively more expensive to run for higher powers! 512, 1024, 2048, 4096 standard."}
              {:name "crossfade", :default 1.0 :doc "The number of periods over which a crossfade is made. The default is 1. This must be an integer."}],
       :rates #{:ar}
       :doc "Strict convolution with fixed kernel which can be updated using a trigger signal. There is a linear crossfade between the buffers upon change.

See Steven W Smith, The Scientist and Engineer's Guide to Digital Signal Processing:
chapter 18: http:// www.dspguide.com/ch18.htm "}


      {:name "StereoConvolution2L",
       :args [{:name "in" :doc "processing target"}
              {:name "kernelL" :doc "buffer index for the fixed kernel of the left channel, may be modulated in combination with the trigger"}
              {:name "kernelR" :doc "buffer index for the fixed kernel of the right channel, may be modulated in combination with the trigger"}
              {:name "trigger" :doc "update the kernel on a change from <= 0 to > 0"}
              {:name "framesize", :default 512.0 :doc "size of FFT frame, must be a power of two. Convolution uses twice this number internally, maximum value you can give this argument is 2^16=65536. Note that it gets progressively more expensive to run for higher powers! 512, 1024, 2048, 4096 standard."}
              {:name "crossfade", :default 1.0 :doc "The number of periods over which a crossfade is made. This must be an integer."}],
       :rates #{:ar}
       :num-outs 2
       :doc "Strict convolution with fixed kernel which can be updated using a trigger signal. There is a linear crossfade between the buffers upon change.
Like convolution2L, but convolves with two buffers and outputs a stereo signal. This saves one FFT transformation per period, as compared to using two copies of convolution2L.

Useful applications could include stereo reverberation or HRTF convolution.

See Steven W Smith, The Scientist and Engineer's Guide to Digital Signal Processing:
chapter 18:  http:// www.dspguide.com/ch18.htm
"}


      {:name "Convolution3",
       :args [{:name "in" :doc "processing target"}
              {:name "kernel" :doc "buffer index for the fixed kernel, may be modulated in combination with the trigger"}
              {:name "trigger", :default 0.0 :doc "update the kernel on a change from <= 0 to > 0"}
              {:name "framesize", :default 512.0 :doc "size of FFT frame, does not have to be a power of two."}]
       :doc "Strict convolution with fixed kernel which can be updated using a trigger signal. The convolution is performed in the time domain, which is highly inefficient, and probably only useful for either very short kernel sizes, or for control rate signals. "}

      {:name "PV_JensenAndersen",
       :args [{:name "buffer" :doc "FFT buffer to read from."}
              {:name "propsc", :default 0.25 :doc "Proportion of spectral centroid feature."}
              {:name "prophfe", :default 0.25 :doc "Proportion of high frequency energy feature."}
              {:name "prophfc", :default 0.25 :doc "Proportion of high frequency content feature."}
              {:name "propsf", :default 0.25 :doc "Proportion of spectral flux feature."}
              {:name "threshold", :default 1.0 :doc "Threshold level for allowing a detection"}
              {:name "wait-time", :default 0.04 :doc "If triggered, minimum wait until a further frame can cause another spot (useful to stop multiple detects on heavy signals)"}],
       :rates #{:ar}
       :doc "FFT feature detector for onset detection based on work described in:

Jensen,K. & Andersen, T. H. (2003). Real-time Beat Estimation Using Feature Extraction.
In Proceedings of the Computer Music Modeling and Retrieval Symposium, Lecture Notes in Computer Science. Springer Verlag.

First order derivatives of the features are taken. Threshold may need to be set low to pick up on changes. "}


      {:name "PV_HainsworthFoote",
       :args [{:name "buffer" :doc "FFT buffer to read from"}
              {:name "proph", :default 0.0 :doc "What strength of detection signal from Hainsworth metric to use."}
              {:name "propf", :default 0.0 :doc "What strength of detection signal from Foote metric to use. The Foote metric is normalised to [0.0,1.0]"}
              {:name "threshold", :default 1.0 :doc "Threshold hold level for allowing a detection"}
              {:name "wait-time", :default 0.04 :doc "If triggered, minimum wait until a further frame can cause another spot (useful to stop multiple detects on heavy signals)
"}],
       :rates #{:ar}
       :doc "FFT onset detector based on work described in:

 Hainsworth, S. (2003) Techniques for the Automated Analysis of Musical Audio. PhD, University of Cambridge engineering dept. See especially p128. The Hainsworth metric is a modification of the Kullback Liebler distance.

The onset detector has general ability to spot spectral change, so may have some ability to track chord changes  aside from obvious transient jolts, but there's no guarantee it won't be confused by frequency modulation artifacts.

Hainsworth metric on it's own gives good results but Foote might be useful in some situations: experimental.
"}


      {:name "RunningSum",
       :args [{:name "in" :doc "Input signal"}
              {:name "numsamp", :default 40.0 :doc "How many samples to take the running sum over (initialisation time only, not modulatable."}]
       :doc "A running sum over a user specified number of samples, useful for running RMS power windowing. "}
      ])

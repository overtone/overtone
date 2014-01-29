(ns overtone.sc.examples.dyn
  (:use [overtone.sc.machinery defexample]
        [overtone.sc ugens]
        [overtone.sc.cgens dyn]))

(defexamples dyn-klang
  (:sin-osc
   "Use a sin-osc to change 3 running sine oscillators"
   "Starts 3 sine oscillators with different frequencies but fixed amplitudes
    and phases of 0.3 and PI respectively. Uses a further sine oscillator
    running at control rate to control the 3 oscillator's frequencies."
   rate :ar
   []
   "
   (* 0.1 (dyn-klang:ar [(+ [800 1000 1200] (* [13 24 12] (sin-osc:kr [2 3 4.2] 0)))
                         [0.3 0.3 0.3]
                         [Math/PI Math/PI Math/PI]]))"
   contributor "Joseph Wilk"))

(defexamples dyn-klank
  (:mouse
   "Use mouse to change 3 running frequency resonators"
   "Starts 3 ringz with varying frequencies and ring-times but a fixed
    amplitude of 1.0 (this is the default when nil is specified).
    The mouse X location effects the frequencies while the mouse Y location
    effects the ring-times (which is effectively the speed the sounds decay at)."
   rate :ar
   []
   "
   (let [freqs (* [800 1071 1153] (mouse-x:kr 0.5, 2, 1))
         ring-times (* [1 1 1] (mouse-y:kr 0.1, 10, 1))]
     (dyn-klank:ar [freqs nil ring-times] (* 0.1 (impulse:ar 2 0))))"
   contributor "Joseph Wilk"))

(ns overtone.sc.cgens.dyn
  (:use [overtone.sc defcgen ugens]
        [overtone.sc.cgens.mix]))

(defn- init-dyn-klang [specs]
  (let [[freqs amps phases] specs
        freq-size (count freqs)
        amps   (or amps (repeat freq-size 1.0))
        phases (or phases (repeat freq-size 0.0))]
    [freqs amps phases]))

(defcgen dyn-klang
  "bank of sine oscillators, params can change after starting"
  [specs       {:doc "An array of three arrays: frequencies, amplitudes
                     and phases: 1) an array of filter frequencies, 2)
                     an array of filter amplitudes, or nil. If nil, then
                     amplitudes default to 1.0, 3) an array of initial
                     phases, or nil. If nil, then phases default to
                     0.0."}
   freq-scale  {:doc "a scale factor multiplied by all frequencies at initialization time."
                :default 1.0}
   freq-offset {:doc "an offset added to all frequencies at initialization time."
                :default 0.0}]
  "A bank of sine oscillators. It is less efficient than klang, as it is basically a wrapper
   around sin-osc ugens in order to provide a similar interface to klang.
   Unlike klang, parameters in specs can be changed after it has been started."

  (:ar (let [[freqs amps phases] (init-dyn-klang specs)]
         (sum (map (fn [[f a p]] (* a (sin-osc:ar (+ 10 (* 2 f)) p)))
                   (map vector freqs amps phases)))))

  (:kr (let [[freqs amps phases] (init-dyn-klang specs)]
         (sum (map (fn [[f a p]] (* a (sin-osc:kr (+ 10 (* 2 f)) p)))
                   (map vector freqs amps phases))))))

(defn- init-dyn-klank [specs]
  (let [[freqs amps rings] specs
        freq-size (count freqs)
        amps  (or amps (repeat freq-size 1.0))
        rings (or rings (repeat freq-size 1.0))]
    [freqs amps rings]))

(defcgen dyn-klank
  "bank of frequency resonators, params can change after starting"
  [specs       {:doc "An array of three arrays: frequencies, amplitudes
                     and ring times: 1) an array of filter frequencies, 2)
                     an array of filter amplitudes, or nil. If nil, then
                     amplitudes default to 1.0, 3) an array of 60 dB decay
                     times for the filters, or nil. If nil, then decay
                     defaults to 1.0."}
   input       {:doc "input source."}
   freq-scale  {:doc "a scale factor multiplied by all frequencies at initialization time."
               :default 1.0}
   freq-offset {:doc "an offset added to all frequencies at initialization time."
               :default 0.0}
   decay-scale {:doc "a scale factor multiplied by all ring times at initialization time."
               :default 1.0}]
  "A bank of frequency resonators which can be used to simulate the resonant modes of an object.
   Each mode is given a ring time, which is the time for the mode to decay by 60 dB.
   Unlike klank, the frequencies in dyn-klank can be changed after it has been started."

  (:ar (let [[freqs amps rings] (init-dyn-klank specs)]
         (sum (map (fn [[f a r]] (* a (ringz:ar input (+ freq-offset (* freq-scale f)) (* r decay-scale))))
                   (map vector freqs amps rings)))))

  (:kr (let [[freqs amps rings] (init-dyn-klank specs)]
         (sum (map (fn [[f a r]] (* a (ringz:kr input (+ freq-offset (* freq-scale f)) (* r decay-scale))))
                   (map vector freqs amps rings))))))

(comment
  (defsynth dynklang-example []
    (out 0 (* 0.1 (dyn-klang:ar [(+ [800, 1000, 1200] (* [13 24 12] (sin-osc:kr [2 3 4.2] 0)))
                                 [0.3 0.3 0.3]
                                 [Math/PI Math/PI Math/PI]])))))

(comment
  (defsynth dynklank-example []
    (out 0 (dyn-klank:ar [[800, 1071, 1153, 1723], nil, [1, 1, 1, 1]] (*  0.1 (impulse:ar 2, 0))))))

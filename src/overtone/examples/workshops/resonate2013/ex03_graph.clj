(ns
  ^{:doc "Brief intro to Incanter and concept of overtones/harmonics.
          Displays some graphs of how harmonics are used to
          generate different wave forms."
    :author "Karsten Schmidt"}
  overtone.examples.workshops.resonate2013.ex03_graph
  (:use [incanter core charts]))

(defn simple-plot
  "Creates a graph of `f` in the interval `x1` .. `x2`.
  Accepts an optional title."
  ([f x1 x2] (simple-plot f x1 x2 ""))
  ([f x1 x2 title]
    (view (function-plot f x1 x2 :title title))))

(defn plot-harmonics
  "Creates a graph of summing oscillator fn `f` over `n` octaves,
  in the interval `x1` .. `x2`."
  ([f n title]
    (plot-harmonics f n -10 10 title))
  ([f n x1 x2 title]
    (simple-plot
      (fn [x] (apply + (map-indexed f (repeat n x))))
      x1 x2 title)))

(defn saw-wave
  "Sawtooth uses overtones in each octave with exponentially
  decreasing impact."
  [i x] (let [i (inc i)] (* (Math/sin (* i x)) (/ 1.0 i))))

(defn sq-wave
  "Sawtooth uses overtones in only every 2nd octave with
  exponentially decreasing impact."
  [i x] (let [i (inc (* i 2))] (* (Math/sin (* i x)) (/ 1.0 i))))

(defn comb-wave
  "Like sq-wave, but flips sign for every 2nd harmonic."
  [i x]
  (let [ii (inc (* i 2))]
    (* (Math/sin (* ii x)) (/ (if (odd? i) 1.0 -1.0) ii))))

;; draw pretty pictures
(plot-harmonics saw-wave 20 "sawtooth")
(plot-harmonics sq-wave 20 "square")
(plot-harmonics comb-wave 20 "comb")

;; this graph shows the amplitude of overtones in each octave
(simple-plot #(/ 1.0 %) 1 10 "harmonic falloff")

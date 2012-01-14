(ns overtone.studio.wavetable
  (:use [overtone.sc server buffer]))

(def ^{:private true} WAVEFORM-LENGTH 1024)

(defn signal->wavetable
  "Convert a seq of values (-1 to 1) into wavetable format, which is partially
  interpolated. The signal data must be a power of 2 in length (512, 1024), and the result
  will be twice as long.

  [a0, a1 ,a2, ...] => [(- (* 2 a0) a1), (- a1 a0), (- (* 2 a1) a2), (- a2 a1), ...]
  "
  [signal]
  (loop [sig (seq signal)
         res []]
    (let [a (float (first sig))
          bs (second sig)
          b (float (or bs (first signal)))
          next-res (conj res (- (* 2 a) b) (- b a))]
      (if (nil? bs)
        next-res
        (recur (next sig) next-res)))))

(defn wavetable->signal
  [table]
  (map (fn [[a b]] (+ a b)) (partition 2 table)))

(defn wavetable
  ([num-waves] (wavetable num-waves WAVEFORM-LENGTH))
  ([num-waves wavelength]
   (with-meta
     {:size num-waves
      :waveforms (repeatedly num-waves #(buffer wavelength))}
     {:type ::wave-table})))

(defn linear-interpolate
  [a b steps]
  (let [shift (/ (- b a) (float (dec steps)))]
    (concat (take (dec steps) (iterate #(+ shift %) a)) [b])))

(defn linear-interpolate-wavetable
  [table idx-a idx-b]
  (let [waves  (:waveforms table)
        steps (dec (- idx-b idx-a))
        data-a (seq (buffer-read (nth waves idx-a)))
        data-b (seq (buffer-read (nth waves idx-b)))
        step-data (drop 1 (drop-last
                            (map
                              (fn [[a b]]
                                (linear-interpolate a b (+ 2 steps)))
                              (partition 2 (interleave data-a data-b)))))
        step-data (map
                    (fn [step]
                      (map #(nth % step) step-data))
                    (range steps))]
    (dotimes [i steps]
      (buffer-write! (nth waves (+ (inc idx-a) i)) (nth step-data i)))))

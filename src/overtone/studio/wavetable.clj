(ns overtone.studio.wavetable
  (:use [overtone.helpers math]
        [overtone.sc server buffer]))

(def ^{:private true} WAVEFORM-LENGTH 1024)

(defn signal->wavetable
  "Convert a seq of values (-1 to 1) into wavetable format, which is partially
  interpolated. The signal data should typically be a power of 2 in length (512, 1024),
  and the result will be twice as long.  (This is not always the case when manipulating
  partial buffers, but full wavetables must be powers of 2 in length.)

  [a0, a1 ,a2, ...] => [(- (* 2 a0) a1), (- a1 a0), (- (* 2 a1) a2), (- a2 a1), ...]
  "
  [signal]
  (let [sig (seq signal)
        len (count sig)]
    (loop [sig sig
           res []]
      (let [a (float (first sig))
            bs (second sig)
            b (float (or bs (first signal)))
            next-res (conj res (- (* 2 a) b) (- b a))]
        (if (nil? bs)
          next-res
          (recur (next sig) next-res))))))

(defn wavetable->signal
  [table]
  (map (fn [[a b]] (+ a b)) (partition 2 table)))

(defn wavetable
  ([num-waves] (wavetable num-waves WAVEFORM-LENGTH))
  ([num-waves size]
     (when-not (power-of-two? size)
       (throw (IllegalArgumentException. (str "size is not a power of 2. Got: " size))))
   (with-meta
     {:size num-waves
      :waveforms (repeatedly num-waves #(buffer size))}
     {:type ::wave-table})))

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

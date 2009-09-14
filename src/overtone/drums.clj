(ns overtone.drums
  (:use (overtone sc music)))

(def *drums (ref []))
(def *drum-count (ref 0))

(defn drum [voice pattern]
  (dosync (alter *drums conj [voice pattern])))

(defn clear-drums []
  (dosync (ref-set *drums [])))

(defn play-drums [tempo beat-count]
  (periodic (fn []
              (let [num (rand)
                    i   @*drum-count]
                (doseq [[voice pattern] @*drums]
                  (if (< num (nth pattern i))
                    (note voice 50 200)))
                (dosync (ref-set *drum-count (mod (inc @*drum-count) beat-count)))))
            tempo))


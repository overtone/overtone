(ns overtone.util
  (:use clojure.contrib.def))

; Some generic counters 
(def id-counters* (ref {}))

(defn next-id [id-key]
  (dosync 
    (let [counter* (get @id-counters* id-key (ref 0))
          id @counter*]
      (alter counter* inc)
      (if (zero? id) (alter id-counters* assoc id-key counter*))
    id)))

(defn reset-counters []
  (dosync (ref-set id-counters* {})))

(defn print-classpath []
  (println (seq (.getURLs (java.lang.ClassLoader/getSystemClassLoader)))))

(defn as-str [s]
  (if (keyword? s) (name s) s))

(defn stringify 
  "Convert all keywords in col to strings without ':' prefixed."
  [col]
  (map #(as-str %1) col))

(defn floatify
  "Convert all numbers in col to floats." 
  [col]
  (map #(if (number? %1) (float %1) %1) col))

; Now available in recent Clojure versions as of Nov. 29, 2009...
;(defn byte-array [len]
;  (make-array (. Byte TYPE) len))

(def BYTE-ARRAY (byte-array 1))

(defn byte-array? [obj]
  (= (type BYTE-ARRAY) (type obj)))

(defn type-checker [t]
  (fn [obj] (and (map? obj) (= (:type obj) t))))

(defn uuid 
  "Creates a random, immutable UUID object that is comparable using the '=' function."
  [] (. java.util.UUID randomUUID))

(defn invert-map 
  "Make the keys the values and the values the keys.  Note, if there are
  duplicate values in the map they will result in key collisions and a smaller
  result map."
  [m] 
  (apply hash-map (interleave (vals m) (keys m))))

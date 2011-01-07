(ns
  ^{:doc "General purpose utility functions."
     :author "Jeff Rose"}
  overtone.util
  (:require [overtone.log :as log])
  (:use clojure.contrib.def
        clojure.stacktrace)
  (:import (java.util ArrayList Collections)))

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
  (map #(cond
          (number? %1) (float %1)
          (true? %1) (float 1)
          (false? %1) (float 0)
          :else %1)
       col))

(defn choose
  "Choose a random note from notes."
  [notes]
  (nth notes (rand-int (count notes))))

;(defn shuffle
;  "Shuffle a collection, returns a seq."
;  [coll]
;  (let [l (ArrayList. coll)]
;    (Collections/shuffle l)
;    (seq l)))

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

(defn cpu-count
  "Get the number of CPUs on this machine."
  []
  (.availableProcessors (Runtime/getRuntime)))

(defn arg-count
  "Get the arity of a function."
  [f]
  (let [m (first (filter #(= "invoke" (.getName %)) (.getDeclaredMethods (class f))))
        p (.getParameterTypes m)]
    (alength p)))

(defn run-handler [handler & args]
  ;;deref vars so arg-count works correctly
  (let [handler (if (var? handler) @handler handler)]
    (try
      (apply handler (take (arg-count handler) args))
      (catch Exception e
        (log/debug "Handler Exception - got args:" args"\n"
                   (with-out-str (.printStackTrace e)))))))

(defn map-vals [f m]
  (zipmap (keys m)
          (map f (vals m))))

(defn- map-entry [k v]
  (proxy [clojure.lang.IMapEntry] []
    (key [] k)
    (getKey [] k)
    (val [] v)
    (getValue [] v)))

(defn callable-map [m fun]
    (proxy [clojure.lang.Associative clojure.lang.IFn] []
      (count       [] (count m))
      (seq         [] (seq m))
      (cons        [[k v]] (callable-map (assoc m k v) fun))
      (empty       [] {})
      (equiv       [o] (= o m))
      (containsKey [k] (contains? m k))
      (entryAt     [k] (map-entry k (get m k)))
      (assoc       [k v] (callable-map (assoc m k v) fun))
      (valAt
                  ([k] (get m k))
                  ([k d] (get m k d)))
      (invoke      [& args] (apply fun args))
      (applyTo    ([args] (apply fun args)))))

(defn file-exists? [path]
  (.exists (java.io.File. path)))

(defn- syms-to-keywords [coll]
  (map #(if (symbol? %)
          (keyword %)
          %)
       coll))

(defn- keywords-to-syms [coll]
  (map #(if (keyword? %)
          (symbol (name %))
          %)
       coll))

(defn arg-mapper [args arg-names default-map]
  (loop [args args
         names arg-names
         arg-map default-map]
    (if (not (empty? args))
      (if (and
            (keyword? (first args))
            (even? (count args)))
        (conj arg-map (apply hash-map args))
        (recur (next args)
               (next names)
               (assoc arg-map
                      (first names)
                      (first args))))
      arg-map)))

(defn arg-lister [args arg-names default-map]
  (let [arg-map (arg-mapper args arg-names default-map)]
    (vec (map arg-map arg-names))))



; TODO: generate arglists and doc meta-data and attach to var
(defmacro defunk [name args & body]
  (let [arg-names (map first (partition 2 args))
        arg-keys (vec (map keyword arg-names))
        default-map (apply hash-map (syms-to-keywords args))]
  `(defn ~name [& args#]
     (let [{:keys [~@arg-names]}
           (arg-mapper args# ~arg-keys ~default-map)]
       ~@body))))


(defn invert-map
  "Takes a map m and returns a new map that's keys are the m's vals and that's vals are m's keys.
   Assumes that m's keys and vals are both sets (i.e. don't contain any duplicates). If there are duplicate values in the map they will result in key collisions and a smaller result map.

   (invert-map {:a 1, :b 2, :c 3}) ;=> {1 :a, 2 :b, 3 :c}"
  [m]
  (apply hash-map (interleave (vals m) (keys m))))

(defn mapply
  "Takes a fn and a seq of seqs and returns a seq representing the application of the fn on each sub-seq

   (mapply + [[1 2 3] [4 5 6] [7 8 9]]) ;=> [6 15 24]"
  [f coll-coll]
  (map #(apply f %) coll-coll))

(defn parallel-seqs
  "takes n seqs and returns a seq of vectors of length n, lazily
   (take 4 (parallel-seqs (repeat 5)
                          (cycle [1 2 3]))) => ([5 1] [5 2] [5 3] [5 1])"
  [seqs]
  (apply map vector seqs))

; Example:
;       (dissoc-in { :who { :me { 1 2 3 4 } } } [ :who :me ] 3 )
(defn dissoc-in
  "Dissociates the element [keys val] from map."
  [m keys val]
        (assoc-in m keys (dissoc (get-in m keys) val)))

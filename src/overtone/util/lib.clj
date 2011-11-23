(ns
  ^{:doc "Library of general purpose utility functions for Overtone internals."
    :author "Jeff Rose and Sam Aaron"}
  overtone.util.lib
  (:import [java.util ArrayList Collections]
           [java.util.concurrent TimeUnit TimeoutException])
  (:use [clojure.stacktrace]
        [clojure.pprint]
        [overtone.util doc]))

(defn indexed
  "Takes col and returns a list of index val pairs for each successive val in
  col. O(n) complexity. Prefer map-indexed or filter-indexed where possible."
  [col]
  (map-indexed (fn [i v] [i v]) col))

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

(defn print-classpath
  "Pretty print the classpath"
  []
  (let [paths (map (memfn getPath)
                   (seq (.getURLs (.getClassLoader clojure.lang.RT))))]
    (pprint paths)))

(defn to-str
  "If val is a keyword, return its name sans :, otherwise return val"
  [val]
  (if (keyword? val) (name val) val))

(defn to-float
  "If val is a number or bool, return its float equivalent otherwise return val"
  [val]
  (cond
   (number? val) (float val)
   (true? val)   (float 1)
   (false? val)  (float 0)
   :else val))

(defn stringify
  "Convert all keywords in col to strings without ':' prefixed."
  [col]
  (map to-str col))

(defn floatify
  "Convert all numbers in col to floats."
  [col]
  (map to-float col))


; Now available in recent Clojure versions as of Nov. 29, 2009...
;(defn byte-array [len]
;  (make-array (. Byte TYPE) len))

(def BYTE-ARRAY (byte-array 1))

(defn byte-array? [obj]
  (= (type BYTE-ARRAY) (type obj)))

(defn type-checker [t]
  (fn [obj] (and (map? obj) (= (:type obj) t))))

(defn uuid
  "Creates a random, immutable UUID object that is comparable using the '='
  function."
  [] (. java.util.UUID randomUUID))

(defn cpu-count
  "Get the number of CPUs on this machine."
  []
  (.availableProcessors (Runtime/getRuntime)))

(defn map-vals
  "Takes a map m and returns a new map with all of m's values mapped through f"
  [f m]
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
      (applyTo    ([args] (apply fun args)))
      (toString   [] "Callable Map")))

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

(defn arg-mapper
  "Takes a list of args, expected arg names and map of defaults.
   Creates a map of arg names to args using the defaults as the starting
   point.

   The args should be passed in the order 'ordered params', 'keyword params'
   such as the following:  [1 2 3 :d 4 :f 5]
   where 1 2 and 3 are ordered params and
   4 and 5 are named params (associated with :d and :f respectively).

   If the expected args is the list [:a :b :c :d :f] then the resulting map
   will look as follows: {:a 1 :b 2 :c 3 :d 5 :f 5}. If the defaults contains
   extra keys, these will be merged in with any clashes being overridden with
   the result map, so if the default map is {:a 99 :h 2} the final output will
   be {:a 1 :b 2 :c 3 :d 4 :f 5 :h 2}.

   It is assumed that the values passed in as the args are *not* keywords."
  [args arg-names default-map]
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

(defmacro defunk [name docstring args & body]
  (let [arg-names (map first (partition 2 args))
        arg-keys (vec (map keyword arg-names))
        default-map (apply hash-map (syms-to-keywords args))]
    (let [arg-pairs (map #(str (first %) " " (second %)) (partition 2 args))
          arg-pairs-str (apply str (interpose ", " arg-pairs))
          arg-string (str "[" arg-pairs-str "]")
          indented-doc   (indented-str-block docstring 55 2)
          full-docstring (str arg-string "\n\n  " indented-doc)]
      `(defn ~name
         ~full-docstring
         [& args#]
         (let [{:keys [~@arg-names]}
               (arg-mapper args# ~arg-keys ~default-map)]
           ~@body)))))

(defn invert-map
  "Takes a map m and returns a new map that's keys are the m's vals and that's
  vals are m's keys. Assumes that m's keys and vals are both sets (i.e. don't
  contain any duplicates). If there are duplicate values in the map they will
  result in key collisions and a smaller result map.

   (invert-map {:a 1, :b 2, :c 3}) ;=> {1 :a, 2 :b, 3 :c}"
  [m]
  (apply hash-map (interleave (vals m) (keys m))))

(defn mapply
  "Takes a fn and a seq of seqs and returns a seq representing the application
  of the fn on each sub-seq.

   (mapply + [[1 2 3] [4 5 6] [7 8 9]]) ;=> [6 15 24]"
  [f coll-coll]
  (map #(apply f %) coll-coll))

(defn update-all
  "Update a value retrieved with the key k in each element of a seq of
  maps by applying f to the current value.

  (update-all [{:a 1} {:a 2}] :a inc) ;=> ({:a 2} {:a 3})
  "
  [maps k f]
  (map (fn [elem]
         (assoc elem k (f (get elem k))))
       maps))

(defn update-every-n
  "Update every nth element in a seq of maps, optionally offset from the start, by
  applying f to the current value.

  (update-every-n [{:a 1} {:a 2} {:a 3} {:a 4} {:a 5}] 2 1 :a #(* 10 %))

  ;=> ({:a 1} {:a 20} {:a 3} {:a 40} {:a 5})
  "
  ([maps n k f]
   (update-every-n maps n 0 k f))
  ([maps n offset k f]
   (concat
     (take offset maps)
     (map-indexed
       (fn [i elem]
         (if (zero? (mod i n))
           (assoc elem k (f (get elem k)))
           elem))
       maps))))

(defn parallel-seqs
  "takes n seqs and returns a seq of vectors of length n, lazily
   (take 4 (parallel-seqs (repeat 5)
                          (cycle [1 2 3]))) => ([5 1] [5 2] [5 3] [5 1])"
  [seqs]
  (apply map vector seqs))

(defn index-of
  "Return the index of item in col."
  [col item]
  (first (first (filter (fn [[i v]]
                          (= v item))
                        (indexed col)))))

(def DEFAULT-PROMISE-TIMEOUT 5000)

(defn deref!
  "Read a future or promise waiting for timeout ms for it to be successfully
  dereferenced. Raises an exception if a timeout occurs"
  ([ref] (deref! ref DEFAULT-PROMISE-TIMEOUT))
  ([ref timeout]
     (let [timeout-indicator (gensym "deref-timeout")
           res               (deref ref timeout timeout-indicator)]
       (if (= timeout-indicator res)
         (throw (Exception. (str "deref! timeout error. Dereference took longer than " timeout " ms")))
         res))))

(defn system-user-name
  "returns the name of the current user"
  []
  (System/getProperty "user.name"))

(defn get-os []
  (let [os (System/getProperty "os.name")]
    (cond
      (re-find #"[Ww]indows" os) :windows
      (re-find #"[Ll]inux" os)   :linux
      (re-find #"[Mm]ac" os)     :mac)))

(defn stringify-map-vals
  "converts a map by running all its vals through str
  (or name if val is a keyword"
  [m]
  (into {} (map (fn [[k v]] [k (if (keyword? v) (name v) (str v))]) m)))

(defn print-ascii-art-overtone-logo
  [user-name version-str]
  (println (str "
          _____                 __
         / __  /_  _____  _____/ /_____  ____  ___
        / / / / | / / _ \\/ ___/ __/ __ \\/ __ \\/ _ \\
       / /_/ /| |/ /  __/ /  / /_/ /_/ / / / /  __/
       \\____/ |___/\\___/_/   \\__/\\____/_/ /_/\\___/

                          Programmable Music. "version-str"


Hello " user-name ", may this be the start of a beautiful music hacking session...")))

(defn normalize-ugen-name
  "Normalizes both SuperCollider and overtone-style names to squeezed lower-case.

  This produces strings that may be used to represent unique ugen keys that can be
  generated from both SC and Overtone names.

  (normalize-ugen-name \"SinOsc\")  ;=> \"sinosc\"
  (normalize-ugen-name \"sin-osc\") ;=> \"sinosc\""
  [n]
  (.replaceAll (.toLowerCase (str n)) "[-|_]" ""))

(defn overtone-ugen-name
  "A basic camelCase to with-dash name converter tuned to convert SuperCollider
  names to Overtone names. Most likely needs improvement.

  (overtone-ugen-name \"SinOsc\") ;=> \"sin-osc\""
  [n]
  (let [n (.replaceAll n "([a-z])([A-Z])" "$1-$2")
        n (.replaceAll n "([a-z-])([0-9])([A-Z])" "$1$2-$3")
        n (.replaceAll n "([A-Z])([A-Z][a-z])" "$1-$2")
        n (.replaceAll n "_" "-")
        n (.toLowerCase n)]
    n))

(defn resolve-gen-name
  "If the gen is a cgen or ugen returns the :name otherwise returns name
   unchanged assuming it's a keyword."
  [gen]
  (if (and (associative? gen)
           (or (= :overtone.sc.machinery.ugen.fn-gen/ugen (:type gen))
               (= :overtone.sc.machinery.defcgen/cgen (:type gen))))
    (keyword (:name gen))
    gen))

(defn consecutive-ints?
  "Checks whether seq s consists of consecutive integers
   (consecutive-ints? [1 2 3 4 5]) ;=> true
   (consecutive-ints? [1 2 3 5 4]) ;=> false"
  [s]
  (apply = (map - (rest s) (seq s))))

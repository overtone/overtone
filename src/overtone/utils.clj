(ns overtone.utils)

; TODO: Modify this function so we can also add namespace prefixes.
; (e.g. (immigrate [overtone.sc :as sc]))

; Thanks to James Reeves for this, taken from Compojure.
(defn immigrate
 "Create a public var in this namespace for each public var in the
 namespaces named by ns-names. The created vars have the same name, value,
 and metadata as the original except that their :ns metadata value is this
 namespace."
 [& ns-names]
 (doseq [ns ns-names]
   (require ns)
   (doseq [[sym var] (ns-publics ns)]
     (let [sym (with-meta sym (assoc (meta var) :ns *ns*))]
       (if (.isBound var)
         (intern *ns* sym (var-get var))
         (intern *ns* sym))))))

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

(defn type-checker [t]
  (fn [obj] (and (map? obj) (= (:type obj) t))))

(defn uuid 
  "Creates a random, immutable UUID object that is comparable using the '=' function."
  [] (. java.util.UUID randomUUID))

(defn now []
  (System/currentTimeMillis))

(defn invert-map 
  "Make the keys the values and the values the keys.  Note, if there are
  duplicate values in the map they will result in key collisions and a smaller
  result map."
  [m] 
  (apply hash-map (interleave (vals m) (keys m))))

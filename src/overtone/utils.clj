(ns overtone.utils)

(defn now []
  (System/currentTimeMillis))

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

(defn stringify [col]
  (map #(if (keyword? %1) (name %1) %1) col))

(defn type-checker [t]
  (fn [obj] (and (map? obj) (= (:type obj) t))))

(defn uuid 
  "Creates a random, immutable UUID object that is comparable using the '=' function."
  [] (. java.util.UUID randomUUID))


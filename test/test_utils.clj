(ns test-utils
  (:require [clojure.string :as string])
  (:import (java.io File)))

(defn refer-private [ns]
  (doseq [[symbol var] (ns-interns ns)]
    (when (:private (meta var))
      (intern *ns* symbol var))))

(def TEST-FILE-SUFFIX "_test.clj")

(defn remove-file-ext [file-name]
  (let [index (.lastIndexOf file-name ".")]
    (apply str (first (split-at index file-name)))))

(defn file-to-ns-string [f root-dir]
  (let [f-sep File/separator
        td-p (re-pattern (str f-sep root-dir f-sep))]
    (replace (re-pattern "_") "-"
             (replace (re-pattern f-sep) "."
                      (remove-file-ext
                        (last (string/split td-p (.getAbsolutePath f))))))))

(defn file-seq-map-filter [dir mf ff]
  (filter ff (map mf (file-seq (File. dir)))))

(defn- test-file? [file-info]
  (let [file-name (first file-info)
        re-p (re-pattern (str ".*" TEST-FILE-SUFFIX))]
    (re-matches re-p file-name)))

(defn test-namespaces [dir]
  (map #(symbol (last %))
    (file-seq-map-filter dir
                         (fn [f] [(.getName f)
                                  (file-to-ns-string f dir)])
                         test-file?)))

;TODO: WTF???!!!  Am I retarded here?  Why isn't this working correctly?
(defn without-sizes [obj]
  (reduce (fn [mem [k v]]
            (cond
              (and (vector? v) (map? (first v))) (assoc mem k (map without-sizes v))
              (map? v) (assoc mem k (without-sizes v))
              (not (.startsWith (name k) "n-")) (assoc mem k v)
              :default mem))
          {}
          obj))


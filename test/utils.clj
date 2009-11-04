(ns test.utils
  (:use [clojure.contrib.str-utils :only (re-split re-gsub)])
  (:import (java.io File)))

; Thanks to code from this blog post:
; http://formpluslogic.blogspot.com/2009/08/clojure-unit-testing-part-1.html

(def TEST-FILE-SUFFIX "_test.clj")

(defn remove-file-ext [file-name]
  (let [index (.lastIndexOf file-name ".")]
    (apply str (first (split-at index file-name)))))

(defn file-to-ns-string [f root-dir]
  (let [f-sep File/separator
        td-p (re-pattern (str f-sep root-dir f-sep))]
    (re-gsub (re-pattern "_") "-"
             (re-gsub (re-pattern f-sep) "."
                      (remove-file-ext
                        (last (re-split td-p (.getAbsolutePath f))))))))

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


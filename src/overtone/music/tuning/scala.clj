(ns
    ^{:doc "Functions that allow use of the Scala scale definition format."
      :author "David \"DAemon\" Allen"}
    overtone.music.tuning.scala
    (:use [overtone.music.tuning]
          [overtone.music.pitch]
          [clojure.java.io]))

(defn parse-scala-line [line]
    (cond
        (= (first line) "!") {:comment (rest line)}
        (re-find #"([0-9]+)/([0-9]+).*" line) (let [results (re-find #"([0-9]+)/([0-9]+)" line)] {:noteratio (/ (Integer/parseInt (nth results 1)) (Integer/parseInt (nth results 2)))})
        (re-find #"([0-9]+\.[0-9]+).*" line) (let [result (re-find #"([0-9]+\.[0-9]+).*" line)] {:noteratio (cents 1 (Float/parseFloat (nth result 1)))})
        :else (throw (Exception. "Failed to parse Scala file"))))

(defn loadscale [path]
    (with-open [rdr (reader path)]
        (let [description (first (line-seq rdr))
              number-of-notes (second (line-seq rdr))]
            (reduce
                #(merge-with conj (parse-scala-line %2) %1)
                {}
                (rest (rest (line-seq rdr)))))))
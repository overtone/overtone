(ns
    ^{:doc "Functions that allow use of the Scala scale definition format."
      :author "David \"DAemon\" Allen"}
  overtone.music.tuning.scala
  (:use [overtone.music.tuning]
        [overtone.music.pitch]
        [clojure.java.io]
        [clojure.set]
        [clojure.math.numeric-tower]))

(defn- comment? [line]
  (= (first line) \!))

(defn- parse-scala-line [line]
  (cond
                                        ;(comment? line) nil ;{:comment (rest line)}
    (re-find #"([0-9]+)/([0-9]+).*" line) (let [results (re-find #"([0-9]+)/([0-9]+)" line)] {:noteratio (/ (Integer/parseInt (nth results 1)) (Integer/parseInt (nth results 2)))})
    (re-find #"([0-9]+\.[0-9]+).*" line) (let [result (re-find #"([0-9]+\.[0-9]+).*" line)] {:noteratio (cents 1 (Float/parseFloat (nth result 1)))})
    (re-find #"([0-9]+)" line) (let [result (re-find #"([0-9]+)" line)] {:noteratio (Integer/parseInt (nth result 1))})
    :else (throw (Exception. (str "Failed to parse Scala file" line)))))

(defn- to-set [s]
  (if (set? s) s #{s}))

(defn- set-union [s1 s2]
  (union (to-set s1) (to-set s2)))

(defn loadscale [path]
  (with-open [rdr (reader path)]
    (let [lines (filter (complement comment?) (doall (line-seq rdr)))
          description (first lines)
          number-of-notes (second lines)
          notes (rest (rest lines))]
      (reduce #(merge-with set-union %1 %2) {:description description :number-of-notes number-of-notes :noteratio 1} (map parse-scala-line notes)))))

(defn note-set-from-scala [path]
  (let [notes (:noteratio (loadscale path))
        max (ceil (reduce max notes))]
    (sort (apply list (set (map #(collapse-to-ntave % max) notes))))))

(defmethod perfn :scala [[symb path initial freq]]
  (fn [note]
    (perfmap note initial freq (note-set-from-scala path))))

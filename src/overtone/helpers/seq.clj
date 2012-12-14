(ns
    ^{:doc "Helper functions for manipulating and testing sequences"
      :author "Sam Aaron"}
  overtone.helpers.seq
  (:require [clojure.zip :as zip]))

(defn consecutive-ints?
  "Checks whether seq s consists of consecutive integers
   (consecutive-ints? [1 2 3 4 5]) ;=> true
   (consecutive-ints? [1 2 3 5 4]) ;=> false"
  [s]
  (and
   (sequential? s)
   (every? integer? (seq s))
   (apply = (map - (rest s) (seq s)))))

(defn indexed
  "Takes a seq and returns a list of index val pairs for each successive val in
  col. O(n) complexity. Prefer map-indexed or filter-indexed where possible."
  [s]
  (map-indexed (fn [i v] [i v]) s))

(defn index-of
  "Return the index of item in seq."
  [s item]
  (first (first (filter (fn [[i v]]
                          (= v item))
                        (indexed s)))))

(defn mapply
  "Takes a fn and a seq of seqs and returns a seq representing the application
  of the fn on each sub-seq.

   (mapply + [[1 2 3] [4 5 6] [7 8 9]]) ;=> [6 15 24]"
  [f coll-coll]
  (map #(apply f %) coll-coll))

(defn parallel-seqs
  "takes n seqs and returns a seq of vectors of length n, lazily
   (take 4 (parallel-seqs (repeat 5)
                          (cycle [1 2 3]))) => ([5 1] [5 2] [5 3] [5 1])"
  [seqs]
  (apply map vector seqs))

(defn find-first
  "Finds first element of seq s for which pred returns true"
  [pred s]
  (first (filter pred s)))

(defn zipper-seq
  "Returns a lazy sequence of a depth-first traversal of zipper z"
  [z]
  (lazy-seq
    (when-not (zip/end? z)
      (cons (zip/node z) (zipper-seq (zip/next z))))))

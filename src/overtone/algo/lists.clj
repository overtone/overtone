(ns
    ^{:doc "Handy list (not sequence) util fns"
      :author "Sam Aaron"}
  overtone.algo.lists)

(defn rotate
  "Treat a list/vector as a circular data structure and rotate it by n
   places:

   (rotate 0  [1 2 3 4]) ;=> [1 2 3 4]
   (rotate 2  [1 2 3 4]) ;=> [3 4 1 2]
   (rotate -1 [1 2 3 4]) ;=> [4 1 2 3]

   Note, coll should be countable."
  [n coll]
  (let [size   (count coll)
        offset (mod n size)
        s      (cycle coll)
        s      (drop offset s)]
    (into [] (take size s))))

(defn fill
  "Create a new vector with the specified size containing either part of
   list ls, or ls repeated until size elements have been placed into result
   vector.

   (fill 5 [1])      ;=> [1 1 1 1 1]
   (fill 6 [1 2 3]   ;=> [1 2 3 1 2 3]
   (fill 7 [5 6]     ;=> [5 6 5 6 5 6 5]
   (fill 3 [1 2 3 4] ;=> [1 2 3]

   Note, coll should be non-empty and countable."
  [size coll]
  (assert (not (empty? coll)) "coll should not be empty")
  (let [cnt (count coll )]
    (if (>= cnt size)
      (into [] (take size coll))
      (let [rem (mod size cnt)
            num (int (/ size cnt))]
        (into [] (concat (apply concat (repeat num coll))
                         (take rem coll)))))))

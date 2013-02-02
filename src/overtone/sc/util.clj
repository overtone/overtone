(ns
    ^{:doc "Util fns useful for interacting with sc stuff (both internally and externally)"
      :author "Sam Aaron"}
    overtone.sc.util)

(defn id-mapper
  "Map all elements of col which are associative and have an :id key to the
  associated :id value. Useful for extracting ids from arglists containing
  buffers and buses. Also works if col is not a collection but a single
  value."
  [col]
  (let [map-fn (fn [el] (if (and (associative? el)
                                (:id el))
                         (:id el)
                         el))]
    (if (sequential? col)
      (map map-fn col)
      (map-fn col))))

(ns
    ^{:doc "Util fns useful for interacting with sc stuff (both internally and externally)"
      :author "Sam Aaron"}
    overtone.sc.util)

(defn map-to-ids
  "Map all elements of col which are associative and have an :id key to the
  associated :id value. Useful for extracting ids from arglists containing
  buffers and busses."
  [col]
  (map (fn [el]
         (if (and (associative? el)
                  (:id el))
           (:id el)
           el))
       col))

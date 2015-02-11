(ns ^{:doc "Temporary namespace for functions and macros we depend on
            from the old Clojure 1.2 contrib. This namespace should be
            removed when these have been relocated to the new contrib
            structure." }
  overtone.helpers.old-contrib)

;; name-with-attributes by Konrad Hinsen:
;;http://code.google.com/p/clojure-contrib/source/browse/trunk/src/clojure/contrib/def.clj?r=889
(defn name-with-attributes
  "To be used in macro definitions.
   Handles optional docstrings and attribute maps for a name to be
   defined in a list of macro arguments. If the first macro argument is
   a string, it is added as a docstring to name and removed from the
   macro argument list. If afterwards the first macro argument is a map,
   its entries are added to the name's metadata map and the map is
   removed from the macro argument list. The return value is a vector
   containing the name with its extended metadata map and the list of
   unprocessed macro arguments."
  [name macro-args]
  (let [[docstring macro-args] (if (string? (first macro-args))
                                 [(first macro-args) (next macro-args)]
                                 [nil macro-args])
        [attr macro-args]      (if (map? (first macro-args))
                                 [(first macro-args) (next macro-args)]
                                 [{} macro-args])
        attr                   (if docstring
                                 (assoc attr :doc docstring)
                                 attr)
        attr                   (if (meta name)
                                 (conj (meta name) attr)
                                 attr)]
    [(with-meta name attr) macro-args]))

;;from str-utils2
;;https://github.com/richhickey/clojure-contrib/blob/a1c66df5287776b4397cf3929a5f498fbb34ea32/src/main/clojure/clojure/contrib/str_utils2.clj
(defn #^String chop
  "Removes the last character of string, does nothing on a zero-length
  string."
  [#^String s]
  (let [size (count s)]
    (if (zero? size)
      s
      (subs s 0 (dec (count s))))))

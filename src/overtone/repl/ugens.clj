(ns overtone.repl.ugens
  (:use [clojure.repl]
        [overtone.sc.machinery.ugen fn-gen specs]
        [overtone.helpers lib doc string]))

(defn- map-terms-to-regexps
  "convert a list of patterns/objects to a list of patterns by not modifying
  the regex patterns and converting other objects to strings and the strings
  to regex patterns. Typically the other obects will be standard strings."
  [terms]
  (map (fn [term]
         (str->regex term))
       terms))

(defn- find-matching-ugen-specs
  "Find ugen specs by searching their :full-doc and :name strings for occurances
   of all the terms. Terms can either be strings or regexp patterns."
  [terms]
  (let [regexps (map-terms-to-regexps terms)]
    (sort-by
     #(:name %)
     (map #(second %)
          (filter (fn [[key spec]]
                    (let [names (str (:name spec) " "
                                     (.toLowerCase (:name spec)) " "
                                     (overtone-ugen-name (:name spec)))
                          search-str (str (:full-doc spec) " " names)
                          matches (filter #(re-find % search-str) regexps)]
                      (= matches regexps)))
                  (combined-specs))))))

(defn- print-ugen-summaries
  "Pretty print out a list of ugen specs by printing their name and summary on
  separate lines."
  [specs longest-name-len]
  (dorun
   (map
    (fn [spec]
      (let [n (str (overtone-ugen-name (:name spec)))
            n (gen-padding n (+ 2 (- longest-name-len (.length n))) " ")
            s (indented-str-block
               (get spec :summary "") DOC-WIDTH (+ 4 longest-name-len))]
        (println (str n "  " s))))

    specs)))

(defn pretty-ugen-doc-string
  "Returns a prettified string representing the documentation of a ugen
  collider. Matches default Clojure documentation format."
  ([ug-spec] (pretty-ugen-doc-string ug-spec ""))
  ([ug-spec ns-str]
     (let [ns-str (if (or
                       (empty? ns-str)
                       (.endsWith ns-str "/"))
                    ns-str
                    (str ns-str "/"))]
       (str "-------------------------"
            "\n"
            ns-str (overtone-ugen-name (:name ug-spec))
            "\n"
            (:full-doc ug-spec)
            "\n\n"))))

(defn print-ugen-docs
  "Pretty print out a list of ugen specs by printing out their names and
  full-doc strings."
  [specs]
  (dorun
   (map
    #(println (pretty-ugen-doc-string %))
    specs)))

(defmacro find-ugen
  "Find a ugen containing the specified terms which may be either strings or
  regexp patterns. Will search the ugen's docstrings for occurrances of all the
  specified terms. Prints out a list of summaries of each matching ugen.
  If only one matching ugen is found, prints out full docstring.

  (find-ugen foo)         ;=> finds all ugens containing the word foo
  (find-ugen foo \"bar\") ;=> finds all ugens containing the words foo AND bar
  (find-ugen #\"foo*\")   ;=> finds all ugens matching the regex foo*"
  [& search-terms]
  (let [search-terms     (map #(if (symbol? %) (str %) %) search-terms)
        specs            (find-matching-ugen-specs search-terms)
        names            (map #(overtone-ugen-name (:name %)) specs)
        longest-name-len (length-of-longest-string names)]
    (cond
     (empty? specs)
     (println "Sorry, unable to find a matching ugen.")

     (= 1 (count specs))
     (print-ugen-docs specs)

     :default
     (print-ugen-summaries specs longest-name-len))))

(defmacro find-ugen-doc
  "Find a ugen containing the specified terms which may be either strings or
  regexp patterns. Will search the ugen's docstrings for occurrances of all the
  specified terms. Prints out each ugens full docstring. Similar to find-doc.

  (find-ugen-doc foo)         ;=> finds all ugens containing the word foo
  (find-ugen-doc \"foo\" bar) ;=> finds all ugens containing the words foo
                                    AND bar
  (find-ugen-doc #\"foo*\")   ;=> finds all ugens matching the regex foo*"
  [& search-terms]
  (let [search-terms (map #(if (symbol? %) (str %) %) search-terms)
        specs        (find-matching-ugen-specs search-terms)]
    (if (empty? specs)
      (println "Sorry, unable to find a matching ugen.")
      (print-ugen-docs specs))))

(defmacro ugen-doc
  "Print documentation for ugen with name ug-name"
  [ug-name]
  `(if-let [spec# (fetch-ugen-spec '~ug-name)]
     (print-ugen-docs [spec#])
     (println "Sorry, unable to find ugen with name")))

(defmacro odoc
  "Prints Overtone documentation for a var or special form given its name.
  Accounts for colliding ugens"
  [name]

  `(let [std-doc#        (with-out-str (doc ~name))
         ug-spec#        (fetch-collider-ugen-spec ~(str name))
         nothing-found?# (and (empty? std-doc#)
                              (nil? ug-spec#))
         same?#          (and ug-spec#
                              (.contains std-doc# (:full-doc ug-spec#)))]

     (if nothing-found?#
       (println "Sorry, no documentation found for" '~name)
       (do
         (when-not (empty? std-doc#)
           (println std-doc#))
         (when (and (not same?#)
                    ug-spec#)
           (println (pretty-ugen-doc-string ug-spec# ugen-collide-ns-str)))))))

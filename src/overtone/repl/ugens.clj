(ns overtone.repl.ugens
  (:use [overtone.sc.ugen fn-gen specs]
        [overtone.util lib doc]))

(defn- map-terms-to-regexps
  "convert a list of patterns/objects to a list of patterns by not modifying
  the regex patterns and converting other objects to strings and the strings
  to regex patterns. Typically the other obects will be standard strings."
  [terms]
  (map (fn [term]
         (if (= java.util.regex.Pattern (type term))
           term
           (re-pattern (str term))))
       terms))

(defn- find-matching-ugen-specs
  "Find ugen specs by searching their :full-doc strings for occurances of all the
  terms. Terms can either be strings or regexp patterns."
  [terms]
  (let [regexps (map-terms-to-regexps terms)]
    (sort-by
     #(:name %)
     (map #(second %)
          (filter (fn [[key spec]]
                    (let [docstr  (get spec :full-doc)
                          matches (filter #(re-find % docstr) regexps)]
                      (= matches regexps)))
                  (combined-specs))))))

(defn- print-ug-summaries
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

(defn print-ug-docs
  "Pretty print out a list of ugen specs by printing out their names and
  full-doc strings."
  [specs]
  (dorun
   (map
    #(println (str "-------------------------"
                   "\n  "
                   (overtone-ugen-name (:name %))
                   "\n"
                   (:full-doc %)
                   "\n\n"))
    specs)))

(defn find-ug
  "Find a ugen containing the specified terms which may be either strings or
  regexp patterns. Will search the ugen's docstrings for occurrances of all the
  specified terms. Prints out a list of summaries of each matching ugen

  (find-ug \"foo\")         ;=> finds all ugens containing the word foo
  (find-ug \"foo\" \"bar\") ;=> finds all ugens containing the words foo AND bar
  (find-ug #\"foo*\")       ;=> finds all ugens matching the regex foo*"
  [& search-terms]
  (let [specs            (find-matching-ugen-specs search-terms)
        names            (map #(overtone-ugen-name (:name %)) specs)
        longest-name-len (length-of-longest-string names)]
    (if (empty? specs)
      (println "Sorry, unable to find a matching ugen.")
      (print-ug-summaries specs longest-name-len))))

(defn find-ug-doc
  "Find a ugen containing the specified terms which may be either strings or
  regexp patterns. Will search the ugen's docstrings for occurrances of all the
  specified terms. Prints out each ugens full docstring. Similar to find-doc.

  (find-ug-doc \"foo\")         ;=> finds all ugens containing the word foo
  (find-ug-doc \"foo\" \"bar\") ;=> finds all ugens containing the words foo
                                    AND bar
  (find-ug-doc #\"foo*\")       ;=> finds all ugens matching the regex foo*"
  [& search-terms]
  (let [specs (find-matching-ugen-specs search-terms)]
    (if (empty? specs)
      (println "Sorry, unable to find a matching ugen.")
      (print-ug-docs specs))))

(defmacro ug-doc
  [ug-name]
  `(let [ug-name# (normalize-ugen-name (str '~ug-name ))
         spec#    (get (combined-specs) ug-name#)]
     (if spec#
       (print-ug-docs [spec#])
       (println "Sorry, unable to find ugen with name"))))

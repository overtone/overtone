(ns overtone.repl.ugens
  (:use [overtone.sc.ugen specs]
        [overtone.util lib doc]))

(defn- map-terms-to-regexps
  [terms]
  (map (fn [term]
         (if (= java.util.regex.Pattern (type term))
           term
           (re-pattern (str term))))
       terms))

(defn find-ugen-specs
  [terms]
  (let [regexps (map-terms-to-regexps terms)]
    (sort-by
     #(:name %)
     (map #(second %)
          (filter (fn [[key spec]]
                    (let [docstr  (get spec :full-doc)
                          matches (filter #(re-find % docstr) regexps)]
                      (= matches regexps)))
                  (seq UGEN-SPECS))))))

(defn find-ug
  [& search-terms]
  (let [specs            (find-ugen-specs search-terms)
        names            (map #(overtone-ugen-name (:name %)) specs)
        longest-name-len (length-of-longest-string names)]
    (dorun
     (map
      (fn [spec]
        (let [n (str (overtone-ugen-name (:name spec)))
              n (gen-padding n (+ 2 (- longest-name-len (.length n))) " ")
              s (indented-str-block (get spec :summary "") DOC-WIDTH (+ 4 longest-name-len))]
          (println (str n "  " s))))

      specs))))

(defn find-ug-doc
  [& search-terms]
  (let [specs (find-ugen-specs search-terms)]
    (dorun
     (map #(println (str "  " (overtone-ugen-name (:name %)) "\n" (:full-doc %) "\n\n  -------------") ) specs))))

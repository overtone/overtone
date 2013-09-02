(ns
    ^{:doc "Utility functions for the generation of docstrings"
      :author "Sam Aaron"}
  overtone.helpers.doc
  (:require [clojure.string :as str]))

(def DOC-WIDTH 50)

(defn length-of-longest-key
  "Returns the length of the longest key of map m. Assumes m's keys are strings
   and returns 0 if map is empty:
   (length-of-longest-key {\"foo\" 1 \"barr\" 2 \"bazzz\" 3}) ;=> 5
   (length-of-longest-key {}) ;=> 0"

  [m]
  (or (last (sort (map #(.length %) (keys m))))
      0))

(defn length-of-longest-string
  "Returns the length of the longest string/symbol/keyword in list l. Returns 0
  if l is nil or empty"
  [l]
  (if (or (not l) (empty? l))
    0
    (let [longest (last (sort-by #(.length (name %)) l))]
      (.length (name longest)))))

(defn gen-padding
  "Generates a padding string starting concatting s with len times pad:
   (gen-padding \"\" 5 \"b\") ;=> \"bbbbb\"
   May be called without starting string s in which case it defaults to the
   empty string and also without pad in which case it defaults to a single space"
  ([len] (gen-padding "" len " "))
  ([len pad] (gen-padding "" len pad))
  ([s len pad]
     (if (> len 0)
       (gen-padding (str s pad) (dec len) pad)
       s)))

(defn- indented-str-block*
  [s ls cur-len max-len indent]
  (if (empty? ls)
    s
    (let [f-len (.length (first ls))]
      (if (.endsWith (first ls) "\n\n")
        (if (> (+ cur-len f-len) max-len)
          (indented-str-block* (str s "\n" (gen-padding indent) (first ls) (gen-padding indent)) (rest ls) 0 max-len indent)
          (indented-str-block* (str s (first ls) (gen-padding indent)) (rest ls) 0 max-len indent))
        (if (> (+ cur-len f-len) max-len)
          (if (> f-len max-len)
            (do
              (indented-str-block* (str s "\n" (gen-padding indent) (first ls)) (rest ls) f-len max-len indent))
            (indented-str-block* (str s "\n" (gen-padding indent)) ls 0 max-len indent))
          (indented-str-block* (str s (first ls) " ") (rest ls) (+ cur-len f-len 1) max-len indent))))))

(defn- indent-first-line
  "Ensure the first line isn't overly indented if it also happens to be
  too long"
  [s split-text max-len init-indent]
  (let [f (first split-text)
        s (str (gen-padding init-indent) f " ")]
    (cond
     (.endsWith s "\n")    [(str s (gen-padding init-indent)) 0]
     (> (count s) max-len) [(str s "\n" (gen-padding init-indent)) 0]
     :else                 [s (count s)])))

(defn indented-str-block
  "Appends a list ls of strings to string s in a formatted block with a
   specific width max-len and indentation indent. May be called with a
   basic text string and max-len and indent in which case text will be
   split on whitespace.

  Will clobber all single \n found but will honour two in a row. This allows
  docstrings to contain \n for formatting purposes in source form rather than
  require them to exist on one large line."
  ([txt max-len wrap-indent] (indented-str-block txt max-len wrap-indent 0))
  ([txt max-len wrap-indent init-indent]
     (let [id                   (str (java.util.UUID/randomUUID))
           txt                  (str/trim txt)
           txt                  (str/replace txt #"[\n]{2}" id)
           txt                  (str/replace txt "\n" " ")
           txt                  (str/replace txt id "\n\n ")
           txt                  (str/replace txt #" +" " ")
           txt                  (str/replace txt #"\A +" "")
           split-text           (str/split txt #" +")
           [first-line cur-len] (indent-first-line "" split-text max-len init-indent)]
       (indented-str-block* first-line (rest split-text) cur-len (- max-len wrap-indent) wrap-indent))))

(defn fs
  "Format string(s). Synonym for indented-str-block but with a default
   initial and wrap indentation of two characters. Also ensures that
   there is whitespace above and below the text."
  [txt & rest]
  (let [txt-list (apply list txt rest)
        txt-list (interpose " " txt-list)
        txt      (apply str txt-list)]
    (indented-str-block (str "\n\n" txt "\n\n") 70 7 2)))

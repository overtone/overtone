(ns
  ^{:doc "Functions used to manipulate and generate the documentation for ugens"
     :author "Sam Aaron & Jeff Rose"}
  overtone.sc.ugen.doc
  (:use
   overtone.sc.ugen.defaults
   [clojure.contrib.string :only (split)]))

(defn- length-of-longest-key
  "Returns the length of the longest key of map m. Assumes m's keys are strings and returns 0 if map is empty:
   (length-of-longest-key {\"foo\" 1 \"barr\" 2 \"bazzz\" 3}) ;=> 5
   (length-of-longest-key {}) ;=> 0"

  [m]
  (or (last (sort (map #(.length %) (keys m))))
      0))

(defn- gen-padding
  "Generates a padding string starting concatting s with len times pad:
   (gen-padding \"\" 5 \"b\") ;=> \"bbbbb\"
   May be called without starting string s in which case it defaults to the empty string."
  ([len pad] (gen-padding "" len pad))
  ([s len pad]
     (if (> len 0)
       (gen-padding (str s pad) (dec len) pad)
       s)))

(defn- indented-str-block
  "Appends a list ls of strings to string s in a formatted block with a specific width max-len
   and indentation indent. May be called with a basic text string and max-len and indent in which
   case text will be split on whitespace."
  ([txt max-len indent] (indented-str-block "" (split #" +" txt) 0 max-len indent))
  ([s ls cur-len max-len indent]
     (if (empty? ls)
       s
       (let [f-len (.length (first ls))]
         (if (> (+ cur-len f-len) max-len)
           (indented-str-block (str s "\n" (gen-padding "" indent " ")) ls 0 max-len indent)
           (indented-str-block (str s (first ls) " ") (rest ls) (+ cur-len f-len) max-len indent))))))

(defn- args-str
  "Returns a string representing the arguments of the ugen spec"
  [spec]
  (let [args (:args spec)
        name-vals (map #(str (:name %) " " (:default %)) args)
        line (apply str (interpose ", " name-vals))]
    (str "[" line "]")))

(defn- categories-str
  "Returns a string representing the categories of a ugen spec"
  [spec]
  (apply str (interpose ", " (map (fn [cat] (apply str (interpose " -> " cat))) (:categories spec)))))

(defn- rates-str
  "Returns a string representing the rates of a ugen spec"
  [spec]
  (let [rates (sort-by UGEN-RATE-SORT-FN (:rates spec))]
    (str "[ " (apply str (interpose ", " rates)) " ]")))

(defn- arg-doc-str
  "Returns a string representing the arg docs of a ugen spec"
  [spec]
  (let [args (:args spec)
        name:doc (fn [arg] [(or (:name arg) "NAME MISSING!")  (or (:doc arg) "DOC MISSING!")])
        doc-map (into {} (map name:doc args))
        arg-max-key-len (length-of-longest-key doc-map)
        indentation (+ 3 arg-max-key-len)]
    (apply str (map (fn [[name docs]]
                      (str "  "
                           name
                           (gen-padding (inc (- arg-max-key-len (.length name))) " ")
                           (indented-str-block docs 50 indentation)
                           "\n"))
                    doc-map))))

(defn- full-doc-str
  "Returns a string representing the full documentation for the given ugen spec"
  [spec]
  (let [doc (or (:doc spec) "No documentation has been defined for this ugen.")]
    (str
     (args-str spec)
     "\n"
     (arg-doc-str spec)
     "\n"
     (str "  " (indented-str-block doc  (+ 10 DOC-WIDTH) 2))
     "\n\n"
     (str "  Categories: " (categories-str spec))
     "\n"
     (str "  Rates: " (rates-str spec)))))

(defn- merge-arg-doc-default
  "Adds default doc to arg if doc string isn't present."
  [arg]
  (let [last-resort {:doc NO-ARG-DOC-FOUND}
        default-str (get DEFAULT-ARG-DOCS (:name arg))
        default (if default-str
                  {:doc default-str}
                  {})]
    (merge last-resort default arg)))

(defn with-arg-defaults
  "Manipulates the spec's arg key to add documentation strings for each argument. If the doc string is
   present it is left unmodified. Otherwise it will look up the argument in a list of default doc strings
   for common keys. If a default is present it will add it otherwise last resort default NO-ARG-DOC-FOUND
   will be used."
  [spec]
  (let [new-args (map merge-arg-doc-default (:args spec))]
    (assoc spec :args new-args)))

(defn with-full-doc
  "Adds an extra key to the spec representing the full documentation string specifically prepared to be the
   final ugen fn's docstring for printing on the REPL"
  [spec]
  (assoc spec :full-doc (full-doc-str spec)))

(ns
  ^{:doc "Functions used to manipulate and generate the documentation for ugens"
     :author "Sam Aaron & Jeff Rose"}
  overtone.sc.machinery.ugen.doc
  (:use [overtone.sc.machinery.ugen defaults]
        [overtone.helpers.string :only [capitalize]]
        [overtone.util lib doc]))

(defn- args-str
  "Returns a string representing the arguments of the ugen spec"
  [spec]
  (let [args (:args spec)
        name-vals (map #(str (:name %) " " (get % :default ":none")) args)
        line (apply str (interpose ", " name-vals))]
    (str "  [" line "]")))

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
        name:doc (fn [arg] [(or (:name arg) "NAME MISSING!")  (or (capitalize (:doc arg)) "DOC MISSING!")])
        arg-doc (map name:doc args)
        doc-map (into {} arg-doc)
        arg-max-key-len (length-of-longest-key doc-map)
        indentation (+ 5 arg-max-key-len)]
    (apply str (map (fn [[name docs]]
                      (str "  "
                           name
                           (gen-padding (inc (- arg-max-key-len (.length name))) " ")
                           "- "
                           (indented-str-block docs DOC-WIDTH indentation)
                           "\n"))
                    arg-doc))))

(defn- full-doc-str
  "Returns a string representing the full documentation for the given ugen spec"
  [spec]
  (let [doc    (or (:doc spec) "No documentation has been defined for this ugen.")
        doc    (capitalize doc)
        g-name (overtone-ugen-name (name (:name spec)))]
    (str
     (when (:summary spec)
       (str "\n  " (indented-str-block (:summary spec) (+ 10 DOC-WIDTH) 2) "\n"))

     "\n"
     (args-str spec)
     "\n\n"

     (arg-doc-str spec)
     "\n"
     (str "  " (indented-str-block doc  (+ 10 DOC-WIDTH) 2))
     "\n"
     (if (:src-str spec)
       (str "\n  Source:\n" (:src-str spec) "\n"))
     "\n"
     (str "  Categories: " (categories-str spec))
     "\n"
     (str "  Rates: " (rates-str spec))
     "\n"
     (str "  Default rate: " (:default-rate spec))
     (if (:contributor spec)
       (str "\n  Contributed by: " (:contributor spec))
       ""))))

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
  "Manipulates the spec's arg key to add documentation strings for each
  argument. If the doc string is present it is left unmodified. Otherwise it
  will look up the argument in a list of default doc strings for common keys.
  If a default is present it will add it otherwise last resort default
  NO-ARG-DOC-FOUND will be used."
  [spec]
  (let [new-args (map merge-arg-doc-default (:args spec))]
    (assoc spec :args new-args)))

(defn with-full-doc
  "Adds an extra key to the spec representing the full documentation string
   specifically prepared to be the final ugen fn's docstring for printing on
   the REPL"
  [spec]
  (assoc spec :full-doc (full-doc-str spec)))

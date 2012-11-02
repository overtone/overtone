(ns
    ^{:doc "Useful string manipulation fns"
      :author "Sam Aaron"}
  overtone.helpers.string
  (:use [overtone.helpers.hash :only [md5]]))

(defn chop-last
  "Removes the last char in str. Returns empty string unmodified"
  [str]
  (let [size (count str)]
    (if (zero? size)
      str
      (subs str 0 (dec size)))))

(defn chop-first
  "Removes the first char in str. Returns empty string unmodified"
  [str]
  (let [size (count str)]
    (if (zero? size)
      str
      (subs str 1 size))))

(defn chop-first-n
  "Removes the first n chars in str. Returns empty string if n is >= str length."
  [n str]
  (let [size (count str)]
    (if (< size n)
      ""
      (subs str n size))))

(defn chop-last-n
  "Removes the first n chars in str. Returns empty string if n is >= str length."
  [n str]
  (let [size (count str)]
    (if (< size n)
      ""
      (subs str 0 (- size n)))))

(defn str->regex
  "Converts term to regex. If term is already a regex, leaves it unchanged."
  [term]
  (if (= java.util.regex.Pattern (type term))
    term
    (re-pattern (str term))))

(defn capitalize
  "Make the first char of the text uppercase and leave the rest unmodified"
  [text]
  (let [first-char (.toUpperCase (str (first text)))
        rest-chars (apply str (rest text))]
    (str first-char rest-chars)))

(defn split-on-char
  "Splits a string on a char or single character string.
  (split-char \"foo/bar/baz\" \"/\") ;=> (\"foo\" \"bar\" \"baz\")"
  [s c]
  (let [c           (if (char? c) c (first (str c)))
        s-seq       (seq s)
        partitioned (partition-by #(= c %) s-seq)
        filtered    (filter #(not= [c] %) partitioned)]
    (map #(apply str %) filtered)))

(defn hash-shorten
  "Ensures that the resulting string is no longer than size chars by
   trimming the excess chars from prefix and replacing with simple
   hash. Postfix never gets modified."
  [max-size prefix postfix]
  (let [prefix  (str prefix)
        postfix (str postfix)
        hash    (subs (str (md5 prefix)) 0 3)
        cnt     (+ (count prefix) (count postfix))]
    (cond
     (< cnt max-size) (str prefix postfix)

     (> (+ (count hash) (count postfix)) max-size)
     (throw (Exception.
             (str "Cannot shorten string. The max-size you supplied to hash-shorten is too small. Try something larger than "
                  (+ (count hash) (count postfix)))))

     :else (let [num-allowed-chars (- max-size (count hash) (count postfix))
                 allowed-s         (subs prefix 0 num-allowed-chars)]
             (str allowed-s hash postfix)))))

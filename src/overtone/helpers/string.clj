(ns
    ^{:doc "Useful string manipulation fns"
      :author "Sam Aaron"}
  overtone.helpers.string)

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

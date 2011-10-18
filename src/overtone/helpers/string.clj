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

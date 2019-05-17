(ns overtone.osc.pattern
  (:use [overtone.osc.util])
  (:require [clojure.string :as string]))

;; Pattern-matched retrievel of handlers. Implements the following pattern
;; matching rules from the Open Sound Control Spec 1.0:
;; http://opensoundcontrol.org/spec-1_0
;;
;; When an OSC server receives an OSC Message, it must invoke the appropriate OSC
;; Methods in its OSC Address Space based on the OSC Message's OSC Address
;; Pattern. This process is called dispatching the OSC Message to the OSC Methods
;; that match its OSC Address Pattern. All the matching OSC Methods are invoked
;; with the same argument data, namely, the OSC Arguments in the OSC Message.
;;
;; The parts of an OSC Address or an OSC Address Pattern are the substrings
;; between adjacent pairs of forward slash characters and the substring after the
;; last forward slash character.
;;
;; A received OSC Message must be disptched to every OSC method in the current
;; OSC Address Space whose OSC Address matches the OSC Message's OSC Address
;; Pattern. An OSC Address Pattern matches an OSC Address if:
;;
;; 1. The OSC Address and the OSC Address Pattern contain the same number of
;;    parts; and
;; 2. Each part of the OSC Address Pattern matches the corresponding part of the
;;    OSC Address.
;;
;; A part of an OSC Address Pattern matches a part of an OSC Address if every
;; consecutive character in the OSC Address Pattern matches the next consecutive
;; substring of the OSC Address and every character in the OSC Address is matched
;; by something in the OSC Address Pattern. These are the matching rules for
;; characters in the OSC Address Pattern:
;;
;; 1. '?' in the OSC Address Pattern matches any single character
;; 2. '*' in the OSC Address Pattern matches any sequence of zero or more characters
;; 3. A string of characters in square brackets (e.g., \"[string]\") in the OSC
;;    Address Pattern matches any character in the string. Inside square
;;    brackets, the minus sign (-) and exclamation point (!) have special meanings:
;;      * two characters separated by a minus sign indicate the range of
;;        characters between the given two in ASCII collating sequence. (A minus
;;        sign at the end of the string has no special meaning.)
;;      * An exclamation point at the beginning of a bracketed string negates the
;;        sense of the list, meaning that the list matches any character not in
;;        the list. (An exclamation point anywhere besides the first character
;;        after the open bracket has no special meaning.)
;; 4. A comma-separated list of strings enclosed in curly braces (e.g.,
;;    \"{foo,bar}\") in the OSC Address Pattern matches any of the strings in the
;;    list.
;; 5. Any other character in an OSC Address Pattern can match only the same
;;    character.

(def MATCHER-CHARS [ \[ \{ \? \* ])

(defn- matcher-char?
  "Returns true if char is one of the MATCHER-CHARS"
  [char]
  (some #{char} MATCHER-CHARS))

(defn- match-question-mark
  "Match just one char from part"
  [pattern part]
  [(rest pattern) (rest part) (not (empty? part))])

(defn- expand-char-matcher
  "expand internal sequences within char matcher. Returns a list of chars
  a-d => abcd"
  [char-matcher]
  (string/replace char-matcher #"(.)-(.)"
                  (fn [m] (let [f (int (first (nth m 1)))
                                l (int (first (nth m 2)))]
                            (map char (range f (inc l)))))))

(defn- extract-neg-char-matcher
  "Pull out sequence of chars within a negative char matcher"
  [pattern]
  (let [pattern (drop 2 pattern)
        matcher (take-while #(not= % \]) pattern)]
    (seq (expand-char-matcher (apply str matcher)))))

(defn- extract-pos-char-matcher
  "Pull out sequence of chars within a positive char matcher"
  [pattern]
  (let [pattern (drop 1 pattern)
        matcher (take-while #(not= % \]) pattern)]
    (seq (expand-char-matcher (apply str matcher)))))

(defn- extract-match-strings
  "Pull out seq of char seqs representing possible string matches"
  [pattern]
  (let [pattern (drop 1 pattern)
        match-strings (take-while #(not= % \}) pattern)
        partitioned (partition-by #(= % \,) match-strings)]
    (remove #(= % (list \,)) partitioned)))

(defn- string-matches?
  "returns string if it matches part, else false"
  [str part]
  (if (= str (take (count str) part))
    str
    false))

(defn- valid-next-char-match-chars
  [pattern]
  (extract-pos-char-matcher pattern))

(defn- valid-next-string-match-chars
  [pattern]
  (let [pos-matches (extract-match-strings pattern)]
    (map first pos-matches)))

(defn- valid-next-chars
  "return a list of possible chars"
  [pattern]
  (case (first pattern)
    \[ (valid-next-char-match-chars pattern)
    \{ (valid-next-string-match-chars pattern)
    [(first pattern)]))

(defn- drop-word
  "drop one of the words from part. Returns empty list if no match found"
  [words part]
  (if-let [match (some #(string-matches? % part) words)]
    (drop (count match) part)
    []))

(defn- until-word-match
  "returns the number of chars up to the first char of the first word match. If
  more than one word matches - choose the smallest number to drop (non-greedy)"
  [pattern part]
  (let [word-matches (extract-match-strings pattern)
        str-matches (map #(apply str %) word-matches)
        str-part (apply str part)
        sliced (map #(string/split str-part (re-pattern %)) str-matches)
        counted (map #(count (first %)) sliced)]
    (first (sort counted))))

(defn- until-neg-char-match
  "returns the number of chars up to the first neg char match"
  [pattern part]
  (let [char-matches (extract-neg-char-matcher pattern)]
    (count (take-while #(some #{%} char-matches) part))))

(defn- until-pos-char-match
  "returns the number of chars up to the first pos char match"
  [pattern part]
  (let [char-matches (extract-pos-char-matcher pattern)]
    (count (take-while #(not (some #{%} char-matches)) part))))

(defn- drop-matched-star-chars
  "Drops chars in part up to the next known match in pattern. Returns remaining
  chars in part. If remaining chars list is empty then there's no match.
  Special cases when next known match is ? [ or {"
  [pattern part]
  (cond
    (and (= \[ (first pattern))

         (= \! (second pattern))) (drop (until-neg-char-match pattern part) part)

    (and (= \[ (first pattern))
         (not= \! (second pattern)))  (drop (until-pos-char-match pattern part) part)

    (= \{ (first pattern)) (drop (until-word-match pattern part) part)
    :else  (drop-while #(not (some #{%} (valid-next-chars pattern))) part)))

(defn- match-star
  "Match zero or more chars. Not being greedy.
  foo*bar  matches fooddddbar
  foo*[b] ;;;eeeeeeek!!!
"
  [pattern part]
  (let [next-in-pattern (second pattern)]
    (if (nil? next-in-pattern)
      [[] [] true]
      (let [remaining (drop-matched-star-chars (rest pattern) part)]
        (if (empty? remaining)
          [[] [] false]
          [(rest pattern) remaining true])))))

(defn- match-basic-chars
  "match all basic non matcher chars in pattern and part"
  [pattern part]
  (loop [pattern pattern
         part part]
    (if (matcher-char? (first pattern))
      [pattern part true]
      (if (and (empty? pattern)
               (empty? part))
        [[] [] true]
        (if (= (first pattern) (first part))
          (recur (rest pattern) (rest part))
          [[] [] false])))))

(defn- negative-bracket-matcher?
  "Returns true if a negative bracket matcher is at front of pattern"
  [pattern]
  (= \! (second pattern)))

(defn- match-positive-bracket
  "Match first postive bracket in pattern against part"
  [pattern part]
  (let [matcher (seq (extract-pos-char-matcher pattern))]
    (if (some #{(first part)} matcher)
      [(drop 1 (drop-while #(not= % \]) pattern)) (rest part) true]
      [[] [] false])))

(defn- match-negative-bracket
  "Match first negative bracket in pattern against part"
  [pattern part]
  (let [matcher (extract-neg-char-matcher pattern)]
    (if-not (some #{(first part)} matcher)
      [(drop 1 (drop-while #(not= % \]) pattern)) (rest part) true]
      [[] [] false])))

(defn- match-bracket
  "match one of the chars in the bracket. If the first char is a ! then
  negatively match."
  [pattern part]
  (if (negative-bracket-matcher? pattern)
    (match-negative-bracket pattern part)
    (match-positive-bracket pattern part)))

(defn- match-brace
  "match one of the strings in brace"
  [pattern part]
  (let [pos-matches (extract-match-strings pattern)]
    (if-let [match (some #(string-matches? % part) pos-matches)]
      [(drop 1 (drop-while #(not= % \}) pattern)) (drop (count match) part) true]
      [[] [] false])))

(defn- match-next-section
  "Examines the next section from pattern and attempts to match it against part."
  [pattern part]
  (case (first pattern)
    \? (match-question-mark pattern part)
    \* (match-star pattern part)
    \[ (match-bracket pattern part)
    \{ (match-brace pattern part)
    (match-basic-chars pattern part)))

(defn- normalize-pattern
  "manipulate pattern to simplify strange match-char sequences
  ab*******c => ab*c
  ab*??*?*c => \"ab???*c"
  [pattern-str]
  (let [pattern-str (string/replace pattern-str #"\*+" "*")
        pattern-str (string/replace pattern-str #"\*[*?]+" (fn [m]
                                                             (let [str-a (seq m)
                                                                   num (count (filter #(= \? %) str-a))]
                                                               (apply str (conj (vec (repeat num "?")) "*") ))))]
    pattern-str))

(defn- path-part-matches?
  "Match a path part with a pattern"
  [pattern part]
  (let [pattern (normalize-pattern pattern)]
    (if (empty? pattern)
      false ;;don't match an empty pattern
      (loop [pattern (seq pattern)
             part    (seq part)
             matching? true]
        (if (not matching?)
          false ;;short-circuit if there's no match
          (if (and (empty? pattern)
                   (empty? part)
                   true)
            true
            (let [[pattern part matching?] (match-next-section pattern part)]
              (recur pattern part matching?))))))))


(defn- sub-container-names
  "Return a list of sub-containers names in the current handler (sub)tree. These
  are all the keys which are strings."
  [handler-tree]
  (filter #(string? %) (keys handler-tree)))

(defn- children
  "Returns a seq of handler-tree's child [name sub-tree] pairs"
  [handler-tree]
  (map (fn [container-name]
         [container-name (get handler-tree container-name)])
       (sub-container-names handler-tree)))

(defn- find-all-pattern-matches
  [pattern-parts sub-tree path]
  (if (empty? pattern-parts)
    {path (:handler sub-tree)}
    (if (and (empty? pattern-parts)
             (not (empty? (sub-container-names sub-tree))))
      nil
      (map (fn [[child-name child]]
             (if (path-part-matches? (first pattern-parts) child-name)
               (find-all-pattern-matches (rest pattern-parts)
                                         child
                                         (str path "/" child-name))))
           (children sub-tree)))))

(defn- unfold-matches
  "takes matches in the form ({\"/foo\" {:key h1 :key2 h2}}) and converts to
  [[\"/foo\" :key h1] [\"/foo\" :key2 h2]]"
  [matches]
  (let [result (map (fn [match]
                      (map (fn [[path handlers]]
                             (map (fn [[key handler]] [path key handler]) handlers))
                           match))
                    matches)]
    (partition 3 (flatten result))))

(defn- pattern-match-handlers
  "pattern match the path and return a list of [path key handler] matches."
  [path handler-tree]
  (let [path-parts (split-path path)
        matches (find-all-pattern-matches path-parts handler-tree "")
        matches (remove nil? (flatten [matches]))]
    (unfold-matches matches)))

(defn- basic-match-handler
  "Basic non-pattern-matching retrieval of handler. Simply look up handler
  based on direct match with path. Returns a list of [path handler] match (or
  the empty list if no match found)."
  [path handlers]
  (let [path-parts (split-path path)
        handler-map (:handler (get-in handlers path-parts {}))]
    (if-let [method handler-map]
      [[path method]]
      [])))

(defn matching-handlers
  "Returns a seq of matching handlers in the form [path key handler] "
  [path handlers]
  (if (contains-pattern-match-chars? path)
    (pattern-match-handlers path handlers)
    (basic-match-handler path handlers)))

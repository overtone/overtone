(ns overtone.helpers.shell
  {:author "Arne Brasseur"}
  (:require [clojure.string :as str]))

(defn shellquote
  "Wrap a string in quotes, if necessary, so that a UNIX shell would interpret it
  as a single string. Can also take a sequence of arguments."
  [a]
  (cond
    (nil? a) ;; nil-pun as an empty seq
    ""

    (or (.isArray (class a)) (sequential? a))
    (str/join " " (map shellquote a))

    (string? a)
    (cond
      (and (str/includes? a "\"")
           (str/includes? a "'"))
      (str "'"
           (str/replace a "'" "'\"'\"'")
           "'")

      (str/includes? a "'")
      (str "\"" a "\"")

      (re-find #"\s|\"" a)
      (str "'" a "'")

      :else
      a)))

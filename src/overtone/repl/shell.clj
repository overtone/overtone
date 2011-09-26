(ns overtone.repl.shell
  (:use [overtone.helpers.file :only [ls-names]]
        [overtone.helpers.string :only [str->regex]])
  (:require [clojure.string :as str]))

(deftype ShellStringList [strlist]
  clojure.lang.IPersistentCollection
  (seq [self] (seq strlist))
  (cons [self o] (ShellStringList. (conj strlist o)))
  (empty [self] (ShellStringList. []))
  (equiv
   [self o]
   (and (instance? ShellStringList o)
        (= strlist (.strlist o))))
  clojure.lang.ISeq
  (first [self] (first strlist))
  (next [self] (next strlist))
  (more [self] (rest strlist))
  Object
  (toString [self] (str/join "\n" (.strlist self))))

(defmethod print-method ShellStringList [str-l w]
  (.write w (str str-l)))

(prefer-method print-method ShellStringList clojure.lang.IPersistentCollection)

(defn ls
  "Returns a listing of contents for the supplied path."
  [path]
  (let [names (ls-names (str path))]
    (ShellStringList. names)))

(defn grep
  "Returns a listing of contents which match."
  [stdin match]
  (let [match (str->regex match)
        res   (filter #(re-find match %) stdin)]
    (ShellStringList. res)))

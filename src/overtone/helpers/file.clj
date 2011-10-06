(ns
    ^{:doc "Useful file manipulation fns"
      :author "Sam Aaron"}
  overtone.helpers.file
  (:use [clojure.java.io]
        [overtone.helpers.string])
  (:require [org.satta.glob :as satta-glob]))

(defn- files->abs-paths
  "Given a seq of java.io.File objects, returns a seq of absolute paths for each
  file."
  [files]
  (map #(.getAbsolutePath %) files))

(defn- files->names
  "Given a seq of java.io.File objects, returns a seq of names for each
  file."
  [files]
  (map #(.getName %) files))

(defn file-separator
  "Returns the system's file separator"
  []
  java.io.File/separator)

(defn home-dir
  "Returns the user's home directory"
  []
  (System/getProperty "user.home"))

(defn mk-path
  "Takes a seq of strings and returns a string which is a concatanation of all
  the input strings separated by the system's default file separator."
  [parts]
  (apply str (interpose (file-separator) parts)))

(defn resolve-tilde-path
  [path]
  (cond
   (= "~" path)
   (home-dir)

   (.startsWith path (str "~" (file-separator)))
   (mk-path [(home-dir) (chop-first-n 2 path)])

   :default
   path))

(defn ls*
  "Given a path to a directory, returns a seq of java.io.File objects
  representing the directory contents"
  [path]
  (let [path (resolve-tilde-path path)
        f    (file path)]
    (if (.isDirectory f)
      (seq (.listFiles f))
      (satta-glob/glob path))))

(defn ls-paths
  "Given a path to a directory, returns a seq of strings representing the full
  paths of all files and dirs within."
  [path]
  (files->abs-paths (ls* path)))

(defn ls-names
  "Given a path to a directory, returns a seq of strings representing the names
   of all files and dirs within. Similar to ls in a shell."
  [path]
  (files->names (ls* path)))

(defn ls-file-paths
  "Given a path to a directory, returns a seq of strings representing the full
  paths of only the files within."
  [path]
  (let [files (filter #(.isFile %) (ls* path))]
    (files->abs-paths files)))

(defn ls-file-names
  "Given a path to a directory, returns a seq of strings representing the name
  of only the files within."
  [path]
  (let [files (filter #(.isFile %) (ls* path))]
    (files->names files)))

(defn ls-dir-paths
  "Given a path to a directory, returns a seq of strings representing the full
  paths of only the dirs within. "
  [path]
  (let [files (filter #(.isDirectory %) (ls* path))]
    (files->abs-paths files)))

(defn ls-dir-names
  "Given a path to a directory, returns a seq of strings representing the name
  of only the dirs within. "
  [path]
  (let [files (filter #(.isDirectory %) (ls* path))]
    (files->names files)))

(defn glob
  "Given a glob pattern returns a seq of java.io.File instances which match.
  Ignores dot files unless explicitly included.

  Examples: (glob \"*.{jpg,gif}\") (glob \".*\") (glob \"/usr/*/se*\")"
  [pattern]
  (let [pattern (resolve-tilde-path pattern)]
    (satta-glob/glob pattern)))

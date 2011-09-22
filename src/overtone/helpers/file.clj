(ns
    ^{:doc "Useful file manipulation fns"
      :author "Sam Aaron"}
  overtone.helpers.file
  (:use [clojure.java.io]))

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

(defn ls*
  "Given a path to a directory, returns a seq of java.io.File objects
  representing the directory contents"
  [path]
  (seq (.listFiles (file path))))

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
    (files->abs-paths files)))

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

(defn home-dir
  "Returns the user's home directory"
  []
  (System/getProperty "user.home"))

(defn file-separator
  "Returns the system's file separator"
  []
  java.io.File/separator)

(defn mk-path
  "Takes a seq of strings and returns a string which is a concatanation of all
  the input strings separated by the system's default file separator."
  [parts]
  (apply str (interpose (file-separator) parts)))

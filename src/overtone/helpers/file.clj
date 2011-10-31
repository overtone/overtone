(ns
    ^{:doc "Useful file manipulation fns"
      :author "Sam Aaron"}
  overtone.helpers.file
  (:import [java.net URL])
  (:use [clojure.java.io]
        [overtone.helpers.string])
  (:require [org.satta.glob :as satta-glob]
            [clojure.java.io :as io]))

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

(defn- download-file-without-timeout
  "Downloads remote file at url to local file specified by target path. Has
  potential to block current thread whilst reading data from remote host. See
  download-file-with-timeout for a non-blocking version."
  [url target-path]
  (let [target-path (resolve-tilde-path target-path)]
    (with-open [in  (io/input-stream url)
                out (io/output-stream target-path)]
      (io/copy in out))))

(defn- download-file-with-timeout
  "Downloads remote file at url to local file specified by target path. If data
  transfer stalls for more than timeout ms, throws a
  java.net.SocketTimeoutException"
  [url target-path timeout]
  (let [target-path (resolve-tilde-path target-path)
        url (URL. url)
        con  (.openConnection url)]
    (.setReadTimeout con timeout)
    (with-open [in (.getInputStream con)
                out (io/output-stream target-path)]
      (io/copy in out))))

(defn download-file
  "Downloads the file pointed to by URI to local path target-path. If no timeout
  is specified will use blocking io to transfer data. If timeout is specified,
  transfer will block for at most timeout ms before throwing a
  java.net.SocketTimeoutException if data transfer has stalled."
  ([url target-path] (download-file-without-timeout url target-path))
  ([url target-path timeout] (download-file-with-timeout url target-path timeout)))

(defn file-size
  "Returns the size of the file pointed to by path in bytes"
  [path]
  (let [path (resolve-tilde-path path)]
    (.length (file path))))

(defn mkdir!
  "Makes a dir at path if it doesn't already exist"
  [path]
  (let [path (resolve-tilde-path path)
        f (File. path)]
    (when-not (.exists f)
      (.mkdir f))))

(defn file-exists?
  "Returns true if the file specified by path exists"
  [path]
  (let [path (resolve-tilde-path path)
        f (File. path)]
    (.exists f)))

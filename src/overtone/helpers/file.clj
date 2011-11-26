(ns
    ^{:doc "Useful file manipulation fns"
      :author "Sam Aaron"}
  overtone.helpers.file
  (:import [java.net URL]
           [java.io StringWriter])
  (:use [overtone.helpers.string]
        [clojure.java.io]
        [overtone.helpers.system :only [windows-os?]])
  (:require [org.satta.glob :as satta-glob]
            [clojure.string :as str]))

(defn file?
  "Returns true if f is of type java.io.File"
  [f]
  (= java.io.File (type f)))

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

(declare mk-path)

(defn resolve-tilde-path
  "Returns a string which represents the resolution of paths starting with
   ~ to point to home directory."
  [path]
  (let [path (if (file? path)
               (.getCanonicalPath path)
               (str path))]
    (cond
     (= "~" path)
     (home-dir)

     (.startsWith path (str "~" (file-separator)))
     (mk-path (home-dir) (chop-first-n (inc (count (file-separator))) path))

     :default
     path)))

(defn ensure-trailing-file-separator
  "Returns a string representing the supplied path that ends with the
  appropriate file separator."
  [path]
  (let [path (resolve-tilde-path path)]
    (if (.endsWith path (file-separator))
      path
      (str path (file-separator)))))

(defn subdir?
  "Returns true if sdir is a subdirectory of dir"
  [sdir dir]
  (let [sdir (resolve-tilde-path sdir)
        sdir (ensure-trailing-file-separator sdir)
        dir  (resolve-tilde-path dir)
        dir  (ensure-trailing-file-separator dir)]
    (.startsWith sdir dir)))

(defn mk-path
  "Takes a seq of strings and returns a string which is a concatanation of all
  the input strings separated by the system's default file separator."
  [& parts]
  (let [path (apply str (interpose (file-separator) parts))]
    (resolve-tilde-path path)))

(defn canonical-path
  "Returns a string representing the canonical version of this path"
  [path]
  (let [path (resolve-tilde-path path)
        f    (file path)]
    (.getCanonicalPath f)))

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
    (with-open [in  (input-stream url)
                out (output-stream target-path)]
      (copy in out))
    target-path))

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
                out (output-stream target-path)]
      (copy in out))
    target-path))

(defn get-file-with-timeout
  "Returns a stringified version of the file pointed to by url. If download
  stalls for more than timeout ms an exception is thrown."
  [url timeout]
  (let [url (URL. url)
        con  (.openConnection url)]
    (.setReadTimeout con timeout)
    (with-open [in (.getInputStream con)
                out (StringWriter.)]
      (copy in out)
      (.toString out))))

(defn download-file
  "Downloads the file pointed to by URI to local path target-path. If no timeout
  is specified will use blocking io to transfer data. If timeout is specified,
  transfer will block for at most timeout ms before throwing a
  java.net.SocketTimeoutException if data transfer has stalled.

  It's also possible to specify num-retries to determine how many attempts to
  make to download the file and also the time-between-retries in ms (defaults to
  5000 ms)"
  ([url path]                          (download-file-without-timeout url path))
  ([url path timeout]                  (download-file-with-timeout url path timeout))
  ([url path timeout n-retries]        (download-file url path timeout n-retries 5000))
  ([url path timeout n-retries wait-t] (download-file url path timeout n-retries wait-t 0))
  ([url path timeout n-retries wait-t attempts-made]
     (when (>= attempts-made n-retries)
       (throw (Exception. (str "Aborting! Download failed after "
                               n-retries
                               " attempts. URL attempted to download: "
                               url ))))

     (let [path (resolve-tilde-path path)]
       (try
         (download-file-with-timeout url path timeout)
         (catch java.io.IOException e
           (rm-rf! path)
           (Thread/sleep wait-t)
           (download-file url path timeout n-retries wait-t (inc attempts-made)))))))


(defn file-size
  "Returns the size of the file pointed to by path in bytes"
  [path]
  (let [path (resolve-tilde-path path)]
    (.length (file path))))

(defn contains-parent-dir-shortcut?
  [path]
  (let [split (split-on-char path (file-separator))]
    (some #(= ".." (str/trim %)) split)))

(defn mkdir!
  "Makes a dir at path if it doesn't already exist."
  [path]
  (let [path (resolve-tilde-path path)
        f    (file path)]
    (when-not (.exists f)
      (.mkdir f))))

(defn absolute-path?
  "Returns true if the path is absolute. false otherwise."
  [path]
  (let [path (resolve-tilde-path path)
        f    (file path)]
    (.isAbsolute f)))

(defn mkdir-p!
  "Makes a dir at path if it doesn't already exist. Also creates all
  subdirectories if necessary"
  [path]
  (let [path (resolve-tilde-path path)
        f    (file path)]
    (.mkdirs f)))

(defn rm-rf!
  "Removes a file or dir and all its subdirectories. Similar to rm -rf on *NIX"
  [path]
  (let [path (resolve-tilde-path path)
        file (file path)]
    (if (.isDirectory file)
      (let [children (.list file)]
        (doall (map #(rm-rf! (mk-path path %)) children))
        (.delete file))
      (.delete file))))

(defn mv!
  "Moves a file from source to dest path"
  [src dest]
  (let [src    (resolve-tilde-path src)
        dest   (resolve-tilde-path dest)
        f-src  (file src)
        f-dest (file dest)]
    (.renameTo f-src f-dest)))

(defn path-exists?
  "Returns true if file or dir specified by path exists"
  [path]
  (let [path (resolve-tilde-path path)
        f (file path)]
    (.exists f)))

(defn file-exists?
  "Returns true if a file specified by path exists"
  [path]
  (let [path (resolve-tilde-path path)
        f (file path)]
    (and (.exists f) (.isFile f))))

(defn dir-exists?
  "Returns true if a directory specified by path exists"
  [path]
  (let [path (resolve-tilde-path path)
        f (file path)]
    (and (.exists f) (.isDirectory f))))

(defn mk-tmp-dir!
  "Creates a unique temporary directory on the filesystem. Typically in /tmp on
  *NIX systems. Returns a File object pointing to the new directory. Raises an
  exception if the directory couldn't be created after 10000 tries."
  []
  (let [base-dir (file (System/getProperty "java.io.tmpdir"))
        base-name (str (System/currentTimeMillis) "-" (long (rand 1000000000)) "-")
        max-attempts 10000]
    (loop [num-attempts 1]
      (if (= num-attempts max-attempts)
        (throw (Exception. (str "Failed to create temporary directory after " max-attempts " attempts.")))
        (let [tmp-dir-name (str base-dir base-name num-attempts)
              tmp-dir (file tmp-dir-name)]
          (if (.mkdir tmp-dir)
            tmp-dir
            (recur (inc num-attempts))))))))

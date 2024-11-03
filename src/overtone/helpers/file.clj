(ns overtone.helpers.file
  "Useful file manipulation fns"
  {:author "Sam Aaron"}
  (:require
   [clojure.java.io :refer :all]
   [clojure.string :as str]
   [org.satta.glob :as satta-glob]
   [overtone.helpers.string :refer :all]
   [overtone.helpers.system :refer [get-os]])
  (:import
   (java.io StringWriter)
   (java.net URL)
   (java.nio.file Files Paths
                  SimpleFileVisitor StandardCopyOption
                  FileVisitResult LinkOption CopyOption)
   (java.nio.file.attribute FileAttribute)))

(set! *warn-on-reflection* true)

(def ^{:dynamic true} *verbose-overtone-file-helpers* false)
(def ^{:dynamic true} *authorization-header* false)

(defn get-current-directory []
  (. (java.io.File. ".") getCanonicalPath))

(defn print-if-verbose
  "Prints the arguments if *verbose-overtone-file-helpers* is bound to true. If
  it is also bound to an integer, will print a corresponding number of spaces at
  the start of each line to indent the output."
  [& to-print]
  (when *verbose-overtone-file-helpers*
    (when (integer? *verbose-overtone-file-helpers*)
      (dotimes [_ *verbose-overtone-file-helpers*] (print " ")))
    (apply println to-print)))

(defn pretty-file-size
  "Takes number of bytes and returns a prettied string with an appropriate unit:
  b, kb or mb."
  [n-bytes]
  (let [n-kb (int (/ n-bytes 1024))
        n-mb (int (/ n-kb 1024))]
    (cond
     (< n-bytes 1024) (str n-bytes " B")
     (< n-kb 1024)    (str n-kb " KB")
     :else            (str n-mb " MB"))))

(defn file?
  "Returns true if f is of type java.io.File"
  [f]
  (= java.io.File (type f)))

(defn- files->abs-paths
  "Given a seq of java.io.File objects, returns a seq of absolute paths for each
  file."
  [files]
  (map #(.getAbsolutePath ^java.io.File %) files))

(defn- files->names
  "Given a seq of java.io.File objects, returns a seq of names for each
  file."
  [files]
  (map #(.getName ^java.io.File %) files))

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
               (.getCanonicalPath ^java.io.File path)
               (str path))]
    (cond
      (= "~" path)
      (home-dir)

      (.startsWith ^java.lang.String path (str "~" (file-separator)))
      (mk-path (home-dir) (chop-first-n (inc (count (file-separator))) path))

      :default
      path)))

(defn ensure-trailing-file-separator
  "Returns a string representing the supplied path that ends with the
  appropriate file separator."
  [path]
  (let [path (resolve-tilde-path path)]
    (if (.endsWith ^java.lang.String path (file-separator))
      path
      (str path (file-separator)))))

(defn subdir?
  "Returns true if sdir is a subdirectory of dir"
  [sdir dir]
  (let [sdir (resolve-tilde-path sdir)
        sdir (ensure-trailing-file-separator sdir)
        dir  (resolve-tilde-path dir)
        dir  (ensure-trailing-file-separator dir)]
    (.startsWith ^java.lang.String sdir dir)))

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
    (cond (.isFile f)      (seq (cons f nil))
          (.isDirectory f) (seq (.listFiles f))
          :else            (satta-glob/glob path))))

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
  (let [files (filter #(.isFile ^java.io.File %) (ls* path))]
    (files->abs-paths files)))

(defn ls-file-names
  "Given a path to a directory, returns a seq of strings representing the name
  of only the files within."
  [path]
  (let [files (filter #(.isFile ^java.io.File %) (ls* path))]
    (files->names files)))

(defn ls-dir-paths
  "Given a path to a directory, returns a seq of strings representing the full
  paths of only the dirs within. "
  [path]
  (let [files (filter #(.isDirectory ^java.io.File %) (ls* path))]
    (files->abs-paths files)))

(defn ls-dir-names
  "Given a path to a directory, returns a seq of strings representing the name
  of only the dirs within. "
  [path]
  (let [files (filter #(.isDirectory ^java.io.File %) (ls* path))]
    (files->names files)))

(defn glob
  "Given a glob pattern returns a seq of java.io.File instances which match.
  Ignores dot files unless explicitly included.

  Examples: (glob \"*.{jpg,gif}\") (glob \".*\") (glob \"/usr/*/se*\")"
  [pattern]
  (let [pattern (resolve-tilde-path pattern)]
    (satta-glob/glob pattern)))

(defn remote-file-size
  "Returns the size of the file referenced by url in bytes."
  [url]
  (let [^java.net.URL url (if (= URL (type url)) url (URL. url))
        ^java.net.URLConnection con (.openConnection url)]
    (when *authorization-header*
      (.setRequestProperty con "Authorization" (*authorization-header*)))
    (.getContentLength con)))

(defn- percentage-slices
  "Returns a seq of maps of length num-slices where each map represents the
  percentage and the associated percentage of size

  usage:
  (percentage-slices 1000 2) ;=> ({:perc 50N, :val 500N} {:perc 100, :val 1000})"
  [size num-slices]
  (map (fn [slice]
         (let [perc (/ (inc slice) num-slices)]
           {:perc (* 100 perc)
            :val  (* size perc)}))
       (range num-slices)))

(defn- print-file-copy-status
  "Print copy status in percentage - granularity times - evenly as
  num-copied-bytes approaches file-size"
  [num-copied-bytes buf-size file-size slices]
  (let [min    num-copied-bytes
        max    (+ buf-size num-copied-bytes)]
    (when-let [slice (some (fn [slice]
                             (when (and (> (:val slice) min)
                                        (< (:val slice) max))
                               slice))
                           slices)]
      (print-if-verbose (str (:perc slice) "% (" (pretty-file-size num-copied-bytes)  ") completed")))))

(defn- remote-file-copy
  "Similar to  the corresponding implementation of #'do-copy in 'clojure.java.io
  but also tracks how many bytes have been downloaded and prints percentage
  statements when *verbose-overtone-file-helpers* is bound to true."
  [in-stream out-stream file-size]
  (let [buf-size 2048
        buffer   (make-array Byte/TYPE buf-size)
        slices   (percentage-slices file-size 100)]
    (loop [bytes-copied 0
           iteration 0]
      (let [size (.read ^java.io.BufferedInputStream in-stream buffer)]
        (when (= 0 (mod iteration 10))
          (print-file-copy-status bytes-copied size file-size slices))
        (when (pos? size)
          (do (.write ^java.io.BufferedOutputStream out-stream buffer 0 size)
              (recur (+ size bytes-copied)
                     (inc iteration))))))
    (print-if-verbose "--> Download successful")))

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
        size        (remote-file-size url)
        url         (URL. url)
        con         (.openConnection url)]
    (when *authorization-header*
      (.setRequestProperty con "Authorization" (*authorization-header*)))
    (.setReadTimeout con timeout)
    (with-open [in (.getInputStream con)
                out (output-stream target-path)]
      (remote-file-copy in out size))
    target-path))

(defn get-file-with-timeout
  "Returns a stringified version of the file pointed to by url. If download
  stalls for more than timeout ms an exception is thrown."
  [url timeout]
  (let [url  (URL. url)
        con  (.openConnection url)
        size (remote-file-size url)]
    (when *authorization-header*
      (.setRequestProperty con "Authorization" (*authorization-header*)))
    (.setReadTimeout con timeout)
    (with-open [in (.getInputStream con)
                out (StringWriter.)]
      ;; NOTE: this seems broken when size is -1, which happens when getContentLength is absent
      (copy in out size)
      (.toString out))))

(defn file-size
  "Returns the size of the file pointed to by path in bytes"
  [path]
  (let [path (resolve-tilde-path path)]
    (.length (file path))))

(defn file-name
  "Returns the name of path-or-file."
  [path-or-file]
  (.getName (file path-or-file)))

(defn file-extension
  "Returns the file extension of path-or-file"
  [path-or-file]
  (let [name (file-name path-or-file)]
    (if (re-seq #"\.." name)
      (last (split-on-char name ".")))))

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
    (when-not (.renameTo f-src f-dest)
      (copy f-src f-dest)
      (delete-file f-src))))

(defn path-exists?
  "Returns true if file or dir specified by path exists"
  [path]
  (let [path (canonical-path path)
        f    (file path)]
    (.exists f)))

(defn ensure-path-exists!
  "Throws an exception if path does not exist."
  [path]
  (when-not (path-exists? path)
    (throw (Exception. (str "Error: unable locate path: " path)))))

(defn file-exists?
  "Returns true if a file specified by path exists"
  [path]
  (let [path (resolve-tilde-path path)
        f (file path)]
    (and (.exists f) (.isFile f))))

(defn file-can-execute?
  "Returns true if a file specified by path exists"
  [path]
  (let [path (resolve-tilde-path path)
        f (file path)]
    (and (.exists f) (.isFile f) (.canExecute f))))

(defn dir-exists?
  "Returns true if a directory specified by path exists"
  [path]
  (let [path (resolve-tilde-path path)
        f (file path)]
    (and (.exists f) (.isDirectory f))))

(defn dir-empty?
  "Returns true if the directory specified by path is empty or doesn't exist"
  [path]
  (let [contents (ls-names path)]
    (empty? contents)))

(defn mk-tmp-dir!
  "Creates a unique temporary directory on the filesystem. Typically in /tmp on
  *NIX systems. Returns a File object pointing to the new directory. Raises an
  exception if the directory couldn't be created after 10000 tries."
  []
  (let [base-dir     (file (System/getProperty "java.io.tmpdir"))
        base-name    (str (System/currentTimeMillis) "-" (long (rand 1000000000)) "-")
        tmp-base     (mk-path base-dir base-name)
        max-attempts 10000]
    (loop [num-attempts 1]
      (if (= num-attempts max-attempts)
        (throw (Exception. (str "Failed to create temporary directory after " max-attempts " attempts.")))
        (let [tmp-dir-name (str tmp-base num-attempts)
              tmp-dir      (file tmp-dir-name)]
          (if (.mkdir tmp-dir)
            tmp-dir
            (recur (inc num-attempts))))))))

(defn- copy-dir-visitor [^java.nio.file.Path from ^java.nio.file.Path to]
  (proxy [SimpleFileVisitor] []
    (preVisitDirectory [dir attrs]
      (let [target (.resolve to (.relativize from dir))]
        (if-not (Files/exists target (into-array LinkOption []))
          (Files/createDirectory target (into-array FileAttribute [])))
        FileVisitResult/CONTINUE))
    (visitFile [^java.nio.file.Path file attrs]
      (let [^java.nio.file.Path target (.resolve to (.relativize from file))]
        (Files/copy file target
                    ^"[Ljava.nio.file.CopyOption;" (into-array CopyOption [StandardCopyOption/REPLACE_EXISTING])))
      FileVisitResult/CONTINUE)))

(defn copy-dir!
  "Copies a directory recursively useing java7 functionality.
   Paths from and to must be strings."
  [from to]
  (letfn [(path [str-path] (Paths/get str-path (into-array String [])))]
    (let [from    (path from)
          to      (path to)
          visitor (copy-dir-visitor from to)]
      (Files/walkFileTree from visitor))))


(defn- download-file*
  ([url path]                          (download-file-without-timeout url path))
  ([url path timeout]                  (download-file-with-timeout url path timeout))
  ([url path timeout n-retries]        (download-file* url path timeout n-retries 5000))
  ([url path timeout n-retries wait-t] (download-file* url path timeout n-retries wait-t 0))
  ([url path timeout n-retries wait-t attempts-made]
   (when (>= attempts-made n-retries)
     (throw (Exception. (str "Aborting! Download failed after "
                             n-retries
                             " attempts. URL attempted to download: "
                             url ))))

   (let [wait-t (long wait-t)
         path (resolve-tilde-path path)]
     (try
       (download-file-with-timeout url path timeout)
       (catch java.io.FileNotFoundException e
         (rm-rf! path)
         (Thread/sleep wait-t)
         (print-if-verbose "  --> Download failed. File not found:" url)
         (throw (Exception. (str "  --> Aborting! Download failed. File not found: " url ))))
       (catch Exception e
         (rm-rf! path)
         (let [[_ code] (re-find #"HTTP response code: (\d+)" (.getMessage e))]
           (cond
             (= "429" code)
             ;; 429 Too Many Requests
             (let [wait-t (* 2 wait-t)]
               (print-if-verbose (format "  --> Too Many Requests, increasing wait time to %.1fs" (double wait-t)))
               (Thread/sleep wait-t)
               (download-file* url path timeout n-retries wait-t (inc attempts-made)))

             (= "401" code)
             (throw (ex-info (.getMessage e)
                             {:url url
                              :response-code 401}
                             e))
             :else
             (do
               (print-if-verbose (str "  --> Download timed out. Retry " (inc attempts-made) (when code (str " (" code ")"))))
               (Thread/sleep wait-t)
               (download-file* url path timeout n-retries wait-t (inc attempts-made))))))))))

(defn- print-download-file
  [url]
  (let [size     (remote-file-size url)
        p-size   (pretty-file-size size)
        size-str (if (<= size 0) "" (str "(" p-size ")"))]
    (print-if-verbose (str "--> Downloading file " size-str " - "  url))))

(defn download-file
  "Downloads the file pointed to by url to local path. If no timeout
  is specified will use blocking io to transfer data. If timeout is specified,
  transfer will block for at most timeout ms before throwing a
  java.net.SocketTimeoutException if data transfer has stalled.

  It's also possible to specify n-retries to determine how many attempts to
  make to download the file and also the wait-t between attempts in ms (defaults
  to 5000 ms)

  Verbose mode is enabled by binding *verbose-overtone-file-helpers* to true."
  ([url path]
   (print-download-file url)
   (download-file* url path))
  ([url path timeout]
   (print-download-file url)
   (download-file* url path timeout))
  ([url path timeout n-retries]
   (print-download-file url)
   (download-file* url path timeout n-retries))
  ([url path timeout n-retries wait-t]
   (print-download-file url)
   (download-file* url path timeout n-retries wait-t)))

(defn find-executable
  "Look for a file on the system's PATH."
  [program-name]
  (some
   (fn [dir]
     (let [f (java.io.File. ^String dir ^String program-name)]
       (when (file-can-execute? f)
         f)))
   (.split (System/getenv "PATH") java.io.File/pathSeparator)))

(defn find-well-known-sc-path
  "Find the path for SuperCollider by checking common locations."
  [default-paths]
  (let [os    (get-os)
        paths (get default-paths os)]
    (first (filter #(file-can-execute? %) paths))))

(ns
    ^{:doc "Useful zip manipulation fns"
      :author "Sam Aaron"}
  overtone.helpers.zip
  (:import [java.util.zip ZipFile ZipEntry ZipInputStream]
           [java.io StringWriter  FileInputStream FileOutputStream])
  (:use [clojure.java.io :only [file]]
        [overtone.helpers file])
  (:require [org.satta.glob :as satta-glob]
            [clojure.java.io :as io]))

(defn zip-file
  "Returns an open java.util.zip.ZipFile object representing the zip file
  pointed to by path."
  [path]
  (let [path (resolve-tilde-path path)
        file (file path)]
    (ZipFile. file)))

(defn zip-entry
  "Returns a java.util.zip.ZipEntry object representing the entry with name
  entry-name within the zipfile pointed to by path. Ensures zipfile is closed.
  Returns nil if entry-name not found within zipfile."
  [path entry-name]
  (let [zip   (zip-file path)
        entry (.getEntry zip entry-name)]
    (.close zip)
    entry))

(defn zip-ls
  "Returns a seq of java.util.zip.ZipEntry objects representing the contents of
   the zip file at the specified path. Ensures zipfile is closed"
  [path]
  (let [zip     (zip-file path)
        entries (.entries zip)
        entries (doall (enumeration-seq entries))]
    (.close zip)
    entries))

(defn zip-cat
  "Returns a string containing the contents of the specified entry within the
  zipfile pointed to by path. Ensures zipfile is closed. Returns nil if
  entry-name not found within zipfile."
  [path entry-name]
  (let [sw    (StringWriter.)
        zip   (zip-file path)
        entry (zip-entry path entry-name)]
    (if (and zip entry)
      (do
        (io/copy (.getInputStream zip entry) sw)
        (.close zip)
        (.toString sw))
      (do
        (.close zip)
        nil))))

(defn unzip
  "Unzip a zip file pointed to by zip-path into dest-path dir. Does not allow
  zip file content paths to contain .. parent shortcut for security reasons i.e.
  all unzipped files will be extracted beneath dest-path. If zip file contains
  compressed subdirectories, these will be created too."
  [zip-path dest-path]
  (let [zip-path  (resolve-tilde-path zip-path)
        dest-path (resolve-tilde-path dest-path)
        dest-path (canonical-path dest-path)]
    (when-not (dir-exists? dest-path)
      (throw (Exception. (str "Destination directory does not exist: " dest-path))))
    (when-not (file-exists? zip-path)
      (throw (Exception. (str "Source zip file does not exist: " zip-path))))

    (let [zip     (zip-file zip-path)
          entries (.entries zip)
          entries (doall (enumeration-seq entries))]
      (dorun
       (map
        (fn [entry]
          (let [name           (.getName entry)
                full-dest-path (mk-path dest-path name)
                full-dest-path (canonical-path full-dest-path)]
            (when-not (subdir? full-dest-path dest-path)
              (throw (Exception. "Security warning - unzip was requested to create a path which is not within original dest-path. Aborting operation.")))
            (if (.isDirectory entry)
              (mkdir-p! full-dest-path)
              (let [is (.getInputStream zip entry)
                    fs (FileOutputStream. full-dest-path)]
                (io/copy is fs)))))
        entries)))))

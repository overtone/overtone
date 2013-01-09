(ns ^{:doc "Provides a simple key/value store which will automatically
            persist to a file on disk. The file is serialized clojure
            code which can be easily edited as text."
      :author "Jeff Rose, Kevin Neaton"}
  overtone.config.file-store
  (:import [java.io FileOutputStream FileInputStream])
  (:use [clojure.pprint]))

;; This should be temporary...
(def storage (constantly :file))

;; Store interface:
(defmulti write-file-store storage)
(defmulti read-file-store  storage)

(def F-LOCK :lock)

;; Simple file-based storage
(defmethod write-file-store :file
  [path data]
  (locking F-LOCK
    (spit path (with-out-str (pprint data)))))

(defmethod read-file-store :file
  [path]
  (with-open [file (FileInputStream. path)]
    (read-string (slurp file))))

(defn live-file-store
  "Uses the file-store located at the given path. Restores the
   file-store if it already exists and persists any changes to disk as
   they occur.

   (def data (ref {}))
   (live-store data \"~/.app-data\")"
  [reference path & [initial-value]]
  (dosync
   (ref-set reference (or initial-value
                          (read-file-store path))))
  (add-watch reference ::live-file-store
             (fn [k r old-state new-state]
                (when-not (= old-state new-state)
                  (write-file-store path new-state)))))

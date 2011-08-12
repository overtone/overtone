(ns
    ^{:doc "Provides a simple key/value configuration system with support for automatically persisting to a file on disk.  The config file is serialized clojure code which is easily editable as a text file."
      :author "Jeff Rose"}
  overtone.config
  (:use [clojure.contrib.io :only (slurp*)])
  (:require [clojure.contrib.duck-streams :as ds])
  (:import (java.io FileOutputStream FileInputStream)))



(defonce config*  (ref {}))

(defonce STORE :file)
(defonce store-path* (ref false))
(defn storage [& args] STORE)

;; Store interface:
(defmulti save-config    storage)
(defmulti restore-config storage)

(def F-LOCK :lock)

; Simple flat-file based storage
(defmethod save-config :file
  [path data]
  (locking F-LOCK
      (ds/spit path data)))

(defmethod restore-config :file
  [path]
  (with-open [file (FileInputStream. path)]
    (read-string (slurp* file))))

(defn config-watcher [k r old-conf new-conf]
  (save-config @store-path* @config*))

(defn live-config
  "Use the configuration database located at the given path, restoring the current config
values if it already exists, and optionally persisting any config-value changes as they occur.

  (live-config \"~/.app-config\")

  ; Anytime the config* ref is modified it will be written to the config file.  Beyond that
  ; it's just a normal old ref.
  (:n-handlers @config*) ; get the current config setting
  (dosync (alter config* assoc :n-handlers 10)) ; set it to 10
  "
  [path & [initial-value]]
  (dosync
    (ref-set config* (or initial-value
                         (restore-config path)))
    (ref-set store-path* path))
  (add-watch config* :live-config config-watcher))

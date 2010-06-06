(ns overtone.core.config
  (:use clojure.contrib.io)
  (:import (java.io FileOutputStream FileInputStream)))

;; Provides a simple key/value configuration system with support for automatically
;; persisting to a file on disk.  The config file is serialized clojure code which
;; is easily editable as a text file.

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
      (spit path data)))

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
  [path]
  (dosync
    (ref-set config* (restore-config path))
    (ref-set store-path* path))
  (add-watch config* :live-config config-watcher))


; Tests
(comment
(ns config.test
  (:use clojure.contrib.test-is)
  (:require config))

(deftest test-basic []
         (config/set-all {:a 1 :b 2})
         (is (= 1 (config/value :a)))

         (config/value :foo "asdf")
         (is (= "asdf" (config/value :foo)))

         (config/defaults {:a 10 :b 20 :foo 30})
         (is (= 30 (config/value :foo))))

(defn delete-file [name]
  (.delete (java.io.File. name)))

(deftest test-persist []
         (config/set-all {:a 1 :b 2})
         (config/save "test")
         (config/value :a 10)
         (config/value :b 20)
         (is (= 10 (config/value :a)))

         (config/restore "test")
         (is (= 2 (config/value :b)))

         (delete-file "test"))
)

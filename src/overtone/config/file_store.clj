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
  "Returns an atom representing a file-store map located at the given
   path. Persists any changes to disk as they occur within the atom.

   (def data (live-file-store \"~/.app-data\"))
   (swap! data assoc :foo 1) ;=> persists key val pair to ~/.app-data"
  [path]
  (let [store-atom (atom (read-file-store path))]
    (add-watch store-atom ::live-file-store
               (fn [k r old-state new-state]
                 (when-not (= old-state new-state)
                   (write-file-store path new-state))))
    store-atom))

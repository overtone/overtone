(ns
  ^{:doc "Library initialization and configuration."
     :author "Jeff Rose"}
  overtone.config.store
  (:use [overtone.config file-store]
        [overtone.helpers.string :only [capitalize]]
        [overtone.util.lib :only [get-os system-user-name]]
        [overtone.helpers.file :only [mkdir! file-exists? mv!]]
        [clojure.java.io :only [delete-file]]))

(def CONFIG-DEFAULTS
  {:os (get-os)
   :user-name (capitalize (system-user-name))})

(defn config-get
  "Get config value"
  [key]
  (get @config* key))

(defn config-set!
  "Set config key to val"
  [key val]
  (dosync
   (alter config* assoc key val)))

(defn config
  "Get the full config map"
  []
  @config*)

(def OVERTONE-DIRS
  (let [root    (str (System/getProperty "user.home") "/.overtone")
        log     (str root "/log")
        samples (str root "/samples")]
      {:root root
       :log log
       :samples samples}))

(def OVERTONE-CONFIG-FILE (str (:root OVERTONE-DIRS) "/config.clj"))
(def OVERTONE-LOG-FILE (str (:log OVERTONE-DIRS) "/log.log"))

(defn- ensure-dir-structure
  []
  (dorun
   (map #(mkdir! %) (vals OVERTONE-DIRS))))

(defn- ensure-config
  "Creates empty config file if one doesn't already exist"
  []
  (when-not (file-exists? OVERTONE-CONFIG-FILE)
    (save-config OVERTONE-CONFIG-FILE {})))

(defn- load-config-defaults
  []
  (dosync
   (dorun
    (map (fn [[k v]]
           (when-not (contains? @config* k)
             (alter config* assoc k v)))
         CONFIG-DEFAULTS))))

(defonce __MOVE-OLD-ROOT-DIR__
  (let [root (:root OVERTONE-DIRS)]
      (when (file-exists? (str root "/config"))
        (println "Warning - old config directory detected. Moved to ~/.overtone-old and replaced with new, empty config.")
        (mv! root (str root "-old")))))

(defonce __ENSIRE-DIRS___
  (ensure-dir-structure))

(defonce __ENSURE-CONFIG__
  (ensure-config))

(defonce __LOAD-CONFIG__
  (try
    (do
      (live-config OVERTONE-CONFIG-FILE)
      (load-config-defaults))
    (catch Exception e
      (throw (Exception. (str "Unable to load config file - it doesn't appear to be valid clojure. Perhaps it has been modified externally? You may reset it by deleting " OVERTONE-CONFIG-FILE " and restarting Overtone. Error: " (.printStackTrace e)))))))

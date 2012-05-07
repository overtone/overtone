(ns
  ^{:doc "Library initialization and configuration."
     :author "Jeff Rose"}
  overtone.config.store
  (:use [overtone.config.file-store]
        [overtone.helpers.string :only [capitalize]]
        [overtone.helpers.system :only [get-os system-user-name]]
        [overtone.helpers.file :only [mkdir! file-exists? path-exists? mv!]]
        [overtone version]
        [clojure.java.io :only [delete-file]]))

(def CONFIG-DEFAULTS
  {:os (get-os)
   :user-name (capitalize (system-user-name))})

(def config* (ref {}))
(def live-config (partial live-file-store config*))

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
  (let [root   (str (System/getProperty "user.home") "/.overtone")
        log    (str root "/log")
        assets (str root "/assets")
        speech (str root "/speech")]
      {:root root
       :log log
       :assets assets
       :speech speech}))

(def OVERTONE-CONFIG-FILE (str (:root OVERTONE-DIRS) "/config.clj"))
(def OVERTONE-LOG-FILE    (str (:log  OVERTONE-DIRS) "/overtone.log"))

(defn- ensure-dir-structure
  []
  (dorun
   (map #(mkdir! %) (vals OVERTONE-DIRS))))

(defn- ensure-config
  "Creates empty config file if one doesn't already exist"
  []
  (when-not (file-exists? OVERTONE-CONFIG-FILE)
    (write-file-store OVERTONE-CONFIG-FILE {})))

(defn- load-config-defaults
  []
  (dosync
   (dorun
    (map (fn [[k v]]
           (when-not (contains? @config* k)
             (alter config* assoc k v)))
         CONFIG-DEFAULTS))))

(defn- update-seen-versions
  []
  (dosync
   (let [val (get @config* :versions-seen)
         val (or val #{})
         new-val (conj val OVERTONE-VERSION-STR)]

     (alter config* assoc :versions-seen new-val))))


(defonce __MOVE-OLD-ROOT-DIR__
  (let [root (:root OVERTONE-DIRS)]
      (when (path-exists? (str root "/config"))
        (println "Warning - old config directory detected. Moved to ~/.overtone-old and replaced with new, empty config.")
        (mv! root (str root "-old")))))

(defonce __ENSURE-DIRS___
  (ensure-dir-structure))

(defonce __ENSURE-CONFIG__
  (ensure-config))

(defonce __LOAD-CONFIG__
  (try
    (do
      (live-config OVERTONE-CONFIG-FILE)
      (load-config-defaults)
      (update-seen-versions))
    (catch Exception e
      (throw (Exception. (str "Unable to load config file - it doesn't appear to be valid clojure. Perhaps it has been modified externally? You may reset it by deleting " OVERTONE-CONFIG-FILE " and restarting Overtone. Error: " (.printStackTrace e)))))))

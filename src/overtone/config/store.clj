(ns overtone.config.store
  "Library initialization and configuration."
  {:author "Jeff Rose"}
  (:use [overtone.config.file-store]
        [overtone.helpers.string :only [capitalize]]
        [overtone.helpers.system :only [get-os system-user-name]]
        [overtone.helpers.file :only [mkdir! file-exists? path-exists? mv! mk-path]]
        [overtone version]
        [clojure.java.io :only [delete-file]]))

(declare live-store)
(declare live-config)

(def CONFIG-DEFAULTS
  {:os (get-os)
   :user-name (capitalize (system-user-name))
   :server :external
   :sc-args {}})

(defn- file-store-get
  "Get the file store reference"
  ([store-atom key]
   (if (keyword? key)
     (get @store-atom key)
     (get-in @store-atom key)))
  ([store-atom key not-found]
   (if (keyword? key)
     (get @store-atom key not-found)
     (get-in @store-atom key not-found))))

(defn- file-store-set!
  "Set file store reference key to val"
  [store-atom key val]
  (swap! store-atom assoc key val))

(defn config-get
  "Get config value. Returns default if specified and the config does
  not contain key."
  ([key] (file-store-get live-config key))
  ([key not-found] (file-store-get live-config key not-found) ))

(defn config-set!
  "Set config key to val"
  [key val]
  (file-store-set! live-config key val)
  (str "set " key))

(defn store-get
  "Get config value. Returns default if specified and the config does
  not contain key."
  ([key] (file-store-get live-store key))
  ([key not-found] (file-store-get live-store key not-found) ))

(defn store-set!
  "Set store key to val"
  [key val]
  (file-store-set! live-store key val)
  (str "set " key))

(defn config
  "Get the full config map"
  []
  @live-config)

(defn store
  "Get the full user store map"
  []
  @live-store)

(def OVERTONE-DIRS
  (let [root   (mk-path (System/getProperty "user.home") ".overtone")
        log    (mk-path root "log")
        assets (mk-path root "assets")
        speech (mk-path root "speech")]
      {:root root
       :log log
       :assets assets
       :speech speech}))

(def ^String OVERTONE-CONFIG-FILE     (mk-path (:root   OVERTONE-DIRS) "config.clj"))
(def ^String OVERTONE-USER-STORE-FILE (mk-path (:root   OVERTONE-DIRS) "user-store.clj"))
(def ^String OVERTONE-ASSETS-FILE     (mk-path (:assets OVERTONE-DIRS) "assets.clj"))
(def ^String OVERTONE-LOG-FILE        (mk-path (:log    OVERTONE-DIRS) "overtone.log"))

(defn- ensure-dir-structure
  []
  (dorun
   (map #(mkdir! %) (vals OVERTONE-DIRS))))

(defn- ensure-file
  "Creates an empty config file if one doesn't already exist"
  [path]
  (when-not (file-exists? path)
    (write-file-store path {})))

(defn- load-config-defaults
  []
  (swap! live-config (fn [config] (merge CONFIG-DEFAULTS config))))

(defn- update-seen-versions
  []
  (swap! live-config
         (fn [c]
           (let [val (get c :versions-seen #{})]
             (assoc c :versions-seen (conj val OVERTONE-VERSION-STR))))))

(defn- migrate-sc-args
  "Previously the sc-args default was [], it's now {}"
  []
  (swap! live-config
         (fn [c]
           (if (not (map? (get c :sc-args)))
             (assoc c :sc-args {})
             c))))

(defn- migrate-up
  "Migrate old configs gracefully."
  []
  (migrate-sc-args))

(defonce __MOVE-OLD-ROOT-DIR__
  (let [root (:root OVERTONE-DIRS)]
      (when (path-exists? (mk-path root "config"))
        (println "Warning - old config directory detected. Moved to ~/.overtone-old and replaced with new, empty config.")
        (mv! root (str root "-old")))))

(defonce __ENSURE-DIRS___
  (ensure-dir-structure))

(defonce __ENSURE-STORAGE-FILES__
  (do
    (ensure-file OVERTONE-CONFIG-FILE)
    (ensure-file OVERTONE-USER-STORE-FILE)))

(defonce live-config (live-file-store OVERTONE-CONFIG-FILE))
(defonce live-store (live-file-store OVERTONE-USER-STORE-FILE))

(defonce __LOAD-CONFIG__
  (try
    (do
      (load-config-defaults)
      (update-seen-versions)
      (migrate-up))
    (catch Exception e
      (throw (Exception. (str "Unable to load config file - it doesn't appear to be valid clojure. Perhaps it has been modified externally? You may reset it by deleting " OVERTONE-CONFIG-FILE " and restarting Overtone. Error: " (.printStackTrace e)))))))

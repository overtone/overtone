(ns
    ^{:doc "Local asset registry mechanism. Maintains a live file-store which maps asset 'keys' to local files."
      :author "Kevin Neaton"}
   overtone.libs.asset.store
  (:use [clojure.java.io :only [file]]
        [clojure.walk :only [postwalk]]
        [overtone.helpers file]
        [overtone.config.store :only [OVERTONE-ASSETS-FILE]]
        [overtone.config.file-store]))

(defn- ensure-assets-file
  "Creates empty assets file if one doesn't already exist"
  []
  (when-not (file-exists? OVERTONE-ASSETS-FILE)
    (write-file-store OVERTONE-ASSETS-FILE {})))

(defonce __ENSURE-LIVE-ASSET-STORE__
  (ensure-assets-file))

(defonce assets* (live-file-store OVERTONE-ASSETS-FILE))

(defn- vectorize
  "Returns a flat vector from the given args."
  [& args]
  (vec (flatten args)))

(defn- vectorize-values
  "Flatten and vectorize the value of each map-entry."
  [assets]
  (map (fn [[k v]] [k (vectorize v)])
       assets))

(defn- prune-assets
  "Prunes map-entries containing empty or nil keys or vals."
  [assets]
  (remove (fn [[k v]]
            (or (nil? k) (and (coll? k) (empty? k))
                (nil? v) (and (coll? v) (empty? v))))
          assets))

(defn- normalize-assets
  "Normalize assets* to ensure that reading and writing works as expected."
  [assets]
  (->> assets
      (prune-assets)
      (vectorize-values)
      (into {})))

(defn- update-assets*
  "Modify the in-transaction-value of assets* with the provided fn and
   args and normalizes the results before returning."
  [assets fun & args]
  (normalize-assets (apply fun assets args)))

(defn- resolve-paths
  "Returns a seq of canonical path strings. Relative paths, directory paths,
  tilde-paths, and glob strings will all be expanded relative to the current
  project's root directory, resulting in a list of canonical file paths."
  [paths]
  (->> (mapcat ls* paths)
       (filter #(.isFile ^java.io.File %))
       (map #(.getCanonicalPath ^java.io.File %))))

(defn register-assets!
  "Register the asset(s) at the given path(s) with the key provided. Directory
  paths, tilde paths, and glob strings will all be expanded. Asset(s) previously
  registered with the same key will be replaced. Returns the resulting entry."
  [key & paths]
  (let [paths (resolve-paths paths)]
    (assert (every? file-exists? paths))
    (let [new-assets (swap! assets* update-assets* assoc key paths)]
      (select-keys new-assets [key]))))

(defn unregister-assets!
  "Unregister all asset(s) registered with a given key. If paths are supplied
  only those paths will be unregistered. Returns the resulting entry."
  ([key]
     (swap! assets* dissoc key))
  ([key & paths]
     (let [new-assets (swap! assets* update-assets* update-in [key] #(when % (remove (set paths) %)))]
       (select-keys new-assets [key]))))

(defn registered-assets
  "Get all of the asset paths registered with the given key. Provide a name to
  filter by filename. Returns a seq of path strings or nil."
  ([key]
   (get @assets* key))
  ([key name]
   (when-let [paths (registered-assets key)]
     (if name
       (filter #(.endsWith ^java.lang.String % (str (file-separator) name))
               paths)
       paths))))

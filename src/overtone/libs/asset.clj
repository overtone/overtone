(ns
    ^{:doc "A simple local file system-cached asset management system. Assets
           are specified as URLs which are then cached by being copied to a
           centralised place in the file system if not already there. This
           allows assets to be shared by multiple projects without needing to
           duplicate them"
      :author "Sam Aaron"}
  overtone.libs.asset
  (:use [clojure.java.io :only [file]]
   [clojure.string :only [split]]
        [overtone.helpers.file]
        [overtone.config.store :only [OVERTONE-DIRS]]))

(def ^{:dynamic true} *cache-root* (:assets OVERTONE-DIRS))

(defn safe-url
  "Replace all non a-z A-Z 0-9 - chars in stringified version of url with _"
  [url]
  (let [url  (str url)
        safe (.replaceAll url "http://" "")
        safe (.replaceAll safe "https://" "")
        safe (.replaceAll safe "/" "-")
        safe (.replaceAll safe "[^a-zA-Z0-9-.]" "")]
    safe))

(defn url-hash
  "Return a hash for stringified version of url"
  [url]
  (let [url (str url)]
    (str (hash url))))

(defn cache-dir
  "Returns the name of a directory to cache an asset with the given url."
  [url]
  (let [safe (safe-url url)
        hsh  (url-hash url)]
    (mk-path *cache-root* (str safe "--" hsh))))

(defn mk-cache-dir!
  "Create a new cache dir for the specified url"
  [url]
  (let [dir (cache-dir url)]
    (mkdir! dir)))

(defn fetch-cached-path
  "Returns the path to the cached asset if present, otherwise nil."
  [url name]
  (let [path (mk-path (cache-dir url) name)]
    (when (path-exists? path)
      path)))

(defn download-and-cache-asset
  "Downloads the file pointed to by url and caches it on the local file system"
  [url name]
  (let [tmp-dir   (str (mk-tmp-dir!))
        tmp-file  (mk-path tmp-dir name)
        dest-dir  (cache-dir url)
        dest-file (mk-path dest-dir name)]
    (mk-cache-dir! url)
    (download-file url tmp-file)
    (mv! tmp-file dest-file )
    (rm-rf! tmp-dir)
    dest-file))

(defn asset-seq
  "Returns a seq of asset names for a specific url"
  [url]
  (let [dir (cache-dir url)
        dir (file dir)]
    (ls-names dir)))

(defn asset
  "Given a url will return a path to a copy of the asset on the local file
  system. Will download and persist the asset if necessary."
  ([url] (asset url (last (split url #"/"))))
  ([url name]
     (if-let [path (fetch-cached-path url name)]
       path
       (download-and-cache-asset url name))))

(ns
    ^{:doc "A simple local file system-cached asset management system. Assets
           are specified as URLs which are then cached by being copied to a
           centralised place in the file system if not already there. This
           allows assets to be shared by multiple projects on the same system
           without needing to duplicate them"
      :author "Sam Aaron"}
  overtone.libs.asset
  (:use [clojure.java.io :only [file]]
   [clojure.string :only [split]]
        [overtone.helpers file zip string]
        [overtone.config.store :only [OVERTONE-DIRS]]))

(def ^{:dynamic true} *cache-root* (:assets OVERTONE-DIRS))

(defn- download-asset-file
  "Download file at url to local filesystem at tmp-file verbosely."
  [url tmp-file]
  (println "Asset not cached - starting download...")
  (binding [*verbose-overtone-file-helpers* 2]
    (download-file url tmp-file 20000 100 5000)))

(defn- safe-url
  "Replace all non a-z A-Z 0-9 - chars in stringified version of url with _"
  [url]
  (let [url  (str url)
        safe (.replaceAll url "http://" "")
        safe (.replaceAll safe "https://" "")
        safe (.replaceAll safe "/" "-")
        safe (.replaceAll safe "[^a-zA-Z0-9-.]" "")]
    safe))

(defn- url-hash
  "Return a hash for stringified version of url"
  [url]
  (let [url (str url)]
    (str (hash url))))

(defn- cache-dir
  "Returns the name of a directory to cache an asset with the given url."
  [url]
  (let [safe (safe-url url)
        hsh  (url-hash url)]
    (mk-path *cache-root* (str safe "--" hsh))))

(defn- mk-cache-dir!
  "Create a new cache dir for the specified url"
  [url]
  (let [dir (cache-dir url)]
    (mkdir! dir)))

(defn- fetch-cached-path
  "Returns the path to the cached asset if present, otherwise nil."
  [url name]
  (let [path (mk-path (cache-dir url) name)]
    (when (path-exists? path)
      path)))

(defn- download-and-cache-asset
  "Downloads the file pointed to by url and caches it on the local file system"
  [url name]
  (let [tmp-dir   (str (mk-tmp-dir!))
        tmp-file  (mk-path tmp-dir name)
        dest-dir  (cache-dir url)
        dest-file (mk-path dest-dir name)]
    (try
      (mk-cache-dir! url)
      (download-asset-file url tmp-file)
      (mv! tmp-file dest-file )
      (rm-rf! tmp-dir)
      dest-file

      (catch Exception e
        (rm-rf! tmp-dir)
        (rm-rf! dest-dir)
        (throw e)))))

(defn asset-seq
  "Returns a seq of previously cached asset names for a specific url"
  [url]
  (let [dir (cache-dir url)
        dir (file dir)]
    (ls-names dir)))

(defn asset-path
  "Given a url will return a path to a copy of the asset on the local file
  system. Will download and persist the asset if necessary."
  ([url] (asset-path url (last (split url #"/"))))
  ([url name]
     (if-let [path (fetch-cached-path url name)]
       path
       (download-and-cache-asset url name))))

(defn- download-unzip-and-cache-bundled-asset
  "Downloads zip file referenced by url, unzips it safely, and then moves all
  contents to cache dir."
  [url]
  (let [tmp-dir         (str (mk-tmp-dir!))
        tmp-file        (mk-path tmp-dir "bundled-asset")
        tmp-extract-dir (mk-path tmp-dir "extraction")
        dest-dir        (cache-dir url)]
    (try
      (mkdir! tmp-extract-dir)
      (download-asset-file url tmp-file)
      (unzip tmp-file tmp-extract-dir)
      (mk-cache-dir! url)
      (let [asset-names (ls-names tmp-extract-dir)]
        (dorun
         (map #(mv! (mk-path tmp-extract-dir %) (mk-path dest-dir %))
              asset-names)))
      (rm-rf! tmp-dir)
      tmp-extract-dir

      (catch Exception e
        (rm-rf! tmp-dir)
        (rm-rf! dest-dir)
        (throw e)))))

(defn bundled-asset-path
  "Given a url to a remote zipfile and either a / separated internal path or seq
  of strings will return a path to a copy of the internal extracted asset on the
  local file system. Will download, extract and persist all the assets of the
  zipfile if necessary.

  usage:
  (bundled-asset \"http://foo.com/a.zip\" \"internal/asset.wav\")       ;=> path
  (bundled-asset \"http://foo.com/a.zip\" [\"internal\" \"asset.wav\"]) ;=> path"
  [url name]
  (let [name          (if (string? name)
                        (split-on-char name "/")
                        name)
        internal-path (apply mk-path name)]
    (if-let [path (fetch-cached-path url internal-path)]
      path
      (do
        (when (dir-empty? (cache-dir url))
          (download-unzip-and-cache-bundled-asset url))
        (fetch-cached-path url internal-path)))))

(defn bundled-asset-dir
  "Returns the cached directory of of the bundled asset. Will download, extract
  and persist all the assets of the zipfile referended by url if necessary"
  [url]
  (bundled-asset-path url "")
  (cache-dir url))

(ns overtone.samples.freesound
  "An API for interacting with the awesome free online sample resource
  freesound.org"
  {:author "Sam Aaron, Kevin Neaton"}
  (:require
   [clojure.data.json :as json]
   [clojure.java.browse]
   [clojure.pprint]
   [overtone.samples.freesound.search-results :refer :all]
   [overtone.samples.freesound.url :refer :all]
   [overtone.sc.node :refer :all]
   [overtone.config.store :as config]
   [overtone.helpers.file :refer [*authorization-header* file-extension]]
   [overtone.helpers.lib :refer [defrecord-ifn]]
   [overtone.libs.asset :as asset]
   [overtone.sc.buffer :as buffer]
   [overtone.sc.sample :as samp]))

(def ^:dynamic *client-id* "ea6297be42e9de76d47c")
(def ^:dynamic *api-key* "32da10a118819877ec041752680588c62684c0b2")
(def ^:dynamic *access-token* (atom (config/store-get :freesound-token)))

(defonce ^{:private true} __RECORDS__
  (do
    (defrecord-ifn FreesoundSample
      [id size n-channels rate status path args name freesound-id]
      samp/sample-player
      to-sc-id*
      (to-sc-id [this] (:id this)))))

(derive FreesoundSample :overtone.sc.sample/playable-sample)

(defmethod clojure.pprint/simple-dispatch FreesoundSample [b]
  (println
   (format "#<freesound[%s]: %d %s %fs %s %d>"
           (name @(:status b))
           (:freesound-id b)
           (:name b)
           (:duration b)
           (cond
             (= 1 (:n-channels b)) "mono"
             (= 2 (:n-channels b)) "stereo"
             :else (str (:n-channels b) " channels"))
           (:id b))))

(defmethod print-method FreesoundSample [b ^java.io.Writer w]
  (.write w (format "#<freesound[%s]: %d %s %fs %s %d>"
                    (name @(:status b))
                    (:freesound-id b)
                    (:name b)
                    (:duration b)
                    (cond
                      (= 1 (:n-channels b)) "mono"
                      (= 2 (:n-channels b)) "stereo"
                      :else (str (:n-channels b) " channels"))
                    (:id b))))

(def ^:private base-url "https://freesound.org/apiv2")

(defn- freesound-url
  "Generate a freesound.org api url. Accepts an optional map of query-params as the last argument."
  [& url-tail]
  (let [params   (if (map? (last url-tail)) (last url-tail))
        url-tail (if params (butlast url-tail) url-tail)]
    (build-url (apply str base-url url-tail) params)))

(defn- slurp-json
  "Slurp and read the json asset."
  [f]
  (json/read-json (slurp f)))

(def ^:private slurp-json-mem
  "A memoized version of slurp-json."
  (memoize slurp-json))

(defn- slurp-json-asset
  "Download, cache, and slurp-json."
  [url]
  (slurp-json (asset/asset-path url)))

;; a generic POST request, nothing specific to freesound
(defn- post-request [url params]
  (let [^java.net.URL url (java.net.URL. url)
        ^java.net.HttpURLConnection con (.openConnection url)]
    (.setDoOutput con true)
    (.setRequestMethod con "POST")
    (let [w (java.io.BufferedWriter. (java.io.OutputStreamWriter. (.getOutputStream con)))]
      (.write w ^java.lang.String (encode-query params))
      (.close w))
    (let [r (.getInputStream con)]
      r)))

(defn access-token [code]
  (let [r (:access_token
           (slurp-json
            (post-request
             (freesound-url "/oauth2/access_token/")
             {:client_id *client-id*
              :client_secret *api-key*
              :grant_type "authorization_code"
              :code code})))]
    (reset! *access-token* r)
    (config/store-set! :freesound-token r)))

(defn authorization-instructions []
  (let [url
        (freesound-url "/oauth2/authorize/"
                       {:client_id *client-id* :response_type "code"})]
    (println "Authorize in browser and paste code in Stdin.")
    (println url)
    (clojure.java.browse/browse-url url)
    (access-token (read-line))))

(defmacro with-authorization-header [b]
  `(binding [*authorization-header*
             (fn []
               (when (not @*access-token*)
                 (authorization-instructions))
               (str "Bearer " @*access-token*))]
     (let [do-request# #(do ~b)]
       (try
         (do-request#)
         (catch clojure.lang.ExceptionInfo e#
           (if (= 401 (:response-code (ex-data e#)))
             (do
               (println "Freesound responded with 401 Unauthorized, your token has likely expired.")
               (reset! *access-token* nil)
               (do-request#))
             (throw e#)))))))

;; ## Sound Info
(defn- info-url
  "Generate a freesound url for fetching a json datastructure representing the
  info for a given id."
  [id]
  (freesound-url "/sounds/" id "/" {:format "json"}))

(defn- freesound-filename
  "Returns filename string, and ensures that the filename
   ends with extension, in case it's not provided in the
   `:name` attribute."
  [info]
  (let [filename (str (or (:name info) (:id info)))
        filetype (str (:type info))]
    (if (file-extension filename)
      filename
      (str filename "." filetype))))

(defn freesound-info
  "Returns a map containing information pertaining to a particular freesound.
  The freesound id may be specified as an integer or string."
  [id]
  (with-authorization-header (slurp-json-asset (info-url id))))

;; ## Sound Serve
(defn- sound-serve-url
  "Generate a freesound url for fetching the original audio file by id."
  [id]
  (freesound-url "/sounds/" id "/download/"))

(defn freesound-path
  "Download, cache, and persist the freesound audio file specified by
  id. Returns the path to a cached local copy of the audio file."
  [id]
  (let [info (freesound-info id)
        name (freesound-filename info)
        url  (sound-serve-url id)]
    (with-authorization-header (asset/asset-path url name))))

(defn freesound-sample
  "Download, cache and persist the freesound audio file specified by id.
  Creates a buffer containing the sample loaded onto the server and returns a
  playable sample capable of playing the sample when called as a fn.

  Use the `:id` property to get the buffer id, to use directly with `play-buf`."
  [id & args]
  (let [path      (freesound-path id)
        smpl      (apply samp/load-sample path args)
        free-smpl (assoc smpl :freesound-id id) ]
    (map->FreesoundSample free-smpl)))

(defn freesound-samples
  "Download, cache and persist multiple freesound audio files specified by
   ids. Creates a vector of buffers containing the samples loaded onto the server and
   returns a vector of playable samples, each capable of being when called
   as a fn."
  [& ids]
  (let [paths      (mapv freesound-path ids)
        smpls      (apply samp/load-samples paths)
        free-smpls (mapv #(assoc %1 :freesound-id %2) smpls ids)]
    (mapv map->FreesoundSample free-smpls)))

(defn freesound
  "Download, cache and persist the freesound audio file specified by
   id. Creates a buffer containing the sample loaded onto the server and
   returns a playable sample capable of playing the sample when called
   as a fn."
  [id & args]
  (apply freesound-sample id args))

;; ## Pack Info
(defn- pack-info-url
  [id]
  (freesound-url "/packs/" id "/" {:format "json"}))

(defn freesound-pack-info
  "Get information about a freesound sample pack. Returns a map of pack
  properties for the given pack id."
  [id]
  (with-authorization-header (slurp-json-asset (pack-info-url id))))

;; ## Pack Serve
(defn- pack-serve-url
  "Freesound url for fetching a zipped sample pack by id."
  [id]
  (freesound-url "/packs/" id "/download/"))

(defn freesound-pack-dir
  "Download, cache, and persist all of the sounds in the freesound sample pack
  specified by id. Returns the path to a local directory containing the cached
  audio files."
  [id]
  (let [url (pack-serve-url id)]
    (with-authorization-header (asset/asset-bundle-dir url))))

(defn freesound-sample-pack
  "Download, cache, and persist all of the sounds in the freesound sample pack
  specified by id, then loads them into buffers in the SuperCollider server,
  ready to be played. Returns a map with the keys being the names of the samples
  as keywords, and the values being playable samples capable of playing the
  sample when called as a fn.

  Use the `:id` property to get the buffer id, to use directly with `play-buf`.
  "
  [id]
  (into
   {}
   (for [sample-file (file-seq (java.io.File. (freesound-pack-dir id)))
         :let [[_ id user sample-name] (re-find #"/(\d+)__([^/\.]+)__([^\.]+).wav" (str sample-file))]
         :when sample-name]
     [(keyword sample-name)
      (map->FreesoundSample
       (assoc (samp/load-sample sample-file)
              :sample-name sample-name
              :freesound-id (Long/parseLong id)))])))

;; ## Sound Search
(defn- search-url
  "Generate a freesound url for fetching a json datastructure representing the
  the results of a search."
  [params]
  (freesound-url "/sounds/search" params))

(defn- normalize-search-args
  "Takes a sequence of search args and returns a map of params for use with
  freesound-search*"
  ([] (normalize-search-args nil))
  ([args]
     (let [ks (first args)
           ks (when (coll? ks)
                ks)
           args (if ks
                  (rest args)
                  args)
           q (first args)
           q (when (string? q)
               q)
           args (if q
                  (rest args)
                  args)]
       (-> (apply hash-map args)
           (assoc :ks ks :sounds_per_page 100)
           (update-in [:q] #(or q % ""))))))

(defn- freesound-search*
  [params]
  (let [ks     (:ks params)
        params (if ks
                 (assoc params
                   :ks nil
                   :fields (name (first ks)))
                 params)
        url    (search-url params)
        next   (next-fn *api-key* params)
        resp   (api-seq url slurp-json-mem next)
        count  (:num_results (first resp))
        sounds (lazy-cats (map :sounds resp))
        sounds (if ks
                 (map #(get-in % ks) sounds)
                 sounds)]
    (search-results count sounds)))

(defn freesound-search
  "Search freesound.org. Returns an instance of SearchResults containing a
  LazySeq over the sounds matching your query. Makes a single api call to get
  the first page of the results and the total number of matches. Additional api
  calls will be made as necessary as the LazySeq is realized. Use #'count to get
  the number total number of sounds without realizing the entire seq.

  Examples:

  Search for sounds matching the query \"kick drum\"

   (freesound-search :q \"kick drum\")
   (freesound-search \"kick drum\") ;same as above.

  Use a keyseq to filter the results per #'clojure.core/get-in

   (freesound-search [:id] \"kick drum\")

  Get just the ids for all of the sounds in the pack called \"MISStereoPiano\"
  matching the query \"LOUD\".

   (freesound-search [:id] \"LOUD\" :f \"pack:MISStereoPiano\"

  For more information about search params see...
  http://www.freesound.org/docs/api/resources.(first ks)html#sound-search-resource"
  {:arglists '([ks* q* & params])}
  [& args]
  (freesound-search* (normalize-search-args args)))

(defmacro freesound-searchm
  "Search freesound.org and expand the results at macro expansion time."
  {:arglists '([ks* q* & params])}
  [& args]
  ;(println "Compiling freesound search results...")
  (let [params (normalize-search-args args)
        search (freesound-search* params)]
    (dorun search)
    `[~@search]))

(defn freesound-search-paths
  "Search and download. Downloads a caches the sound file matching your search
  query. Returns a collection of local file paths to the cached sound files."
  {:arglists '([query* & params])}
  [& args]
  (let [params (-> (normalize-search-args args)
                   (assoc :fields "id")
                   (dissoc :ks))]
    (map (fn [sound]
           (let [url  (sound-serve-url (:id sound))
                 name (:original_filename sound)]
             (with-authorization-header (asset/asset-path url name))))
         (freesound-search* params))))

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
   [overtone.helpers.gui :as gui]
   [overtone.helpers.lib :refer [defrecord-ifn]]
   [overtone.libs.asset :as asset]
   [overtone.sc.buffer :as buffer]
   [overtone.sc.sample :as samp])
  (:import
   (javax.swing JFrame JPanel JButton JOptionPane WindowConstants
                JPasswordField)
   java.awt.GraphicsEnvironment))

(set! *warn-on-reflection* true)

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

(defn handle-token-response [res]
  (let [{:keys [access_token refresh_token]} (slurp-json res)]
    (reset! *access-token* access_token)
    (config/store-set! :freesound-token access_token)
    (config/store-set! :freesound-refresh-token refresh_token)))

(defn access-token [code]
  (handle-token-response
   (post-request
    (freesound-url "/oauth2/access_token/")
    {:client_id *client-id*
     :client_secret *api-key*
     :grant_type "authorization_code"
     :code code})))

(defn refresh-token! []
  (let [refresh_token (or (config/store-get :freesound-refresh-token)
                          (throw (ex-info "Missing Freesound refresh token" {})))]
    (handle-token-response
      (post-request
        (freesound-url "/oauth2/access_token/")
        {:client_id *client-id*
         :client_secret *api-key*
         :grant_type "refresh_token"
         :refresh_token refresh_token}))))

(defn- dialog-box
  "Opens a window with a password field and a button to input an auth token.
  Takes a callback accepting the password as a string."
  [out-fn]
  (let [button (JButton. "Authorize")
        password-field (JPasswordField. 10)
        panel (doto (JPanel.)
                (.add password-field)
                (.add button))
        frame (doto (JFrame. "Freesound Authorization")
                (.setSize 200 200)
                (.setContentPane panel)
                ;; unsure if this leaks memory but DISPOSE_ON_CLOSE and
                ;; EXIT_ON_CLOSE both risk closing the VM
                (.setDefaultCloseOperation WindowConstants/HIDE_ON_CLOSE))]
    (.addActionListener
     button (gui/action-listener
             (fn [_event]
               ;; using .dispose may close the VM
               (.setVisible frame false)
               (let [pw (String/valueOf (.getPassword password-field))]
                 (out-fn pw)))))
    (.setVisible frame true)
    (fn [] (.dispose frame))))

(defn authorization-instructions
  "Prints the url of and opens a browser to the freesound oauth2 page.
  Listens to (read-line) and opens a Swing dialog box (if not headless).
  Prompts the user to paste token in either, and then uses whichever
  token was provided first to generate and cache a freesound access token."
  []
  (let [url
        (freesound-url "/oauth2/authorize/"
                       {:client_id *client-id* :response_type "code"})]
    (println "Authorize in browser and paste code in Stdin or dialog box.")
    (println url)
    (clojure.java.browse/browse-url url)
    (let [done (promise)
          auth (volatile! nil)
          interrupt-me (volatile! nil)
          close-dialog (volatile! (fn []))
          write (fn [s]
                  (locking auth
                    ;; first writer wins
                    (when (nil? @auth)
                      (vreset! auth s)
                      ;; if entered in dialog box, interrupt (read-line) to finish the future
                      (some-> ^Thread @interrupt-me .interrupt)
                      ;; if entered via (read-line), close dialog box
                      (@close-dialog)
                      (deliver done true))))
          ;; open dialog box if allowed
          _ (when (and (not (GraphicsEnvironment/isHeadless))
                       (not (= "false" (System/getProperty "overtone.samples.freesound.auth-dialog-box"))))
              (vreset! close-dialog (dialog-box write)))
          ;; wait for stdin
          _ (future
              (vreset! interrupt-me (Thread/currentThread))
              (try (let [s (read-line)]
                     (vreset! interrupt-me nil)
                     (write s))
                   (catch InterruptedException _)))]
      @done
      (access-token @auth))))

(defn with-authorization-header* [do-request]
  (binding [*authorization-header*
            (fn []
              (when (not @*access-token*)
                (authorization-instructions))
              (str "Bearer " @*access-token*))]
    (try
      (do-request)
      (catch clojure.lang.ExceptionInfo e
        (if (not= 401 (:response-code (ex-data e)))
          (throw e)
          (if (config/store-get :freesound-refresh-token)
            (do (println "Freesound token has expired, refreshing.")
                (try (refresh-token!)
                     (catch Exception e
                       (prn e)
                       (println "Error while refreshing token, asking for a new token.")
                       (do
                         (authorization-instructions)
                         (do-request))))
                (try
                  (do-request)
                  (catch clojure.lang.ExceptionInfo e
                    (if (not= 401 (:response-code (ex-data e)))
                      (throw e)
                      (do
                        (println "Refresh didn't help, asking for a new token.")
                        (authorization-instructions)
                        (do-request))))))
            (do
              (println "Freesound token has expired, but no refresh token present. Asking for a new token.")
              (authorization-instructions)
              (do-request))))))))

(defmacro with-authorization-header [b]
  `(with-authorization-header* #(do ~b)))

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
   (for [sample-file (file-seq (java.io.File. ^String (freesound-pack-dir id)))
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

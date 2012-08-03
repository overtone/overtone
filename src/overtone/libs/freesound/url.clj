(ns ^{:doc "Basic URL encoding and decoding. Various versions of these functions can found in other Clojure librarys."
      :author "Kevin Neaton"}
  overtone.libs.freesound.url
  (:use [clojure.walk :only [keywordize-keys]])
  (:require [clojure.string :as str])
  (import [java.net URLEncoder URLDecoder]))

(defn url-encode [s & [encoding]]
  (URLEncoder/encode s (or encoding "UTF-8")))

(defn url-decode [s & [encoding]]
  (URLDecoder/decode s (or encoding "UTF-8")))

(defn encode-query
  "Create a url encoded query string from a map of query paremeters."
  [m] (str/join "&"
        (for [[k v] m]
          (str (url-encode (name k)) "="
               (url-encode (str v))))))

(defn decode-query
  "Create a map of query parameters from a url encoded query string. Keywordizes
  map keys and assumes there are no duplicates."
  [s] (->> (str/split (url-decode s) #"[&=]")
           (apply hash-map)
           (keywordize-keys)))

(defn- split-url
  "Split url into three parts: base, query, and fragment."
  [url]
  (let [base  (second (re-find #"^([^\?#]+)" url))
        query (second (re-find #"\?([^\?#]+)" url))
        frag  (second (re-find #"#(.*)$" url))]
    [base query frag]))

(defn build-url
  "Build a url from three parts: base, params, and fragment. Params are
  converted to a url-encoded query string. If the given url contains a query
  string it will be decoded and merged with params. In case of duplicate keys,
  params wins."
  [url & [params frag]]
  (let [[base query f] (split-url url)
        params (if query
                 (merge (decode-query query) params)
                 params)
        frag (or frag f)]
    (str base
         (when params
           (str "?" (encode-query params)))
         (when frag
           (str "#" (url-encode frag))))))

(defn parse-url
  "Returns a map of url components: base, params, and fragment."
  [url]
  (let [[b q f] (split-url url)]
    {:base b
     :params (decode-query (or q ""))
     :fragment f}))

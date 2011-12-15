(ns
    ^{:doc "An API for interacting with the awesome free online sample resource
            freesound.org"}
  overtone.libs.freesound
  (:require [clojure.data.json :as json]
            [overtone.libs.asset :as asset]))

(def ^:dynamic *api-key* "47efd585321048819a2328721507ee23")

(defn- freesound-url
  "Generate a freesound.org url"
  [& url-tail]
  (apply str "http://www.freesound.org/" url-tail))

(defn- url-with-key
  "Appends the api_key to a url"
  [url]
  (str url "?api_key=" *api-key*))

(defn- info-url
  "Generate a freesound url for fetching a json datastructure representing the
  info for a given id."
  [id]
  (url-with-key (freesound-url "api/sounds/" (str id))))

(defn freesound-info
  "Returns a map containing information pertaining to a particular freesound.
  The freesound id may be specified as an integer or string."
  [id]
  (let [url  (info-url id)
        path (asset/asset-path url)
        jsn  (slurp path)]
    (json/read-json jsn)))

(defn freesound-path
  "Returns the path to a cached copy of the freesound audio file on the local
  filesystem. The freesound id may be specified as an integer or string."
  [id]
  (let [info (freesound-info id)
        url  (:serve info)
        url  (url-with-key url)]
    (asset/asset-path url)))

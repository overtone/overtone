(ns overtone.jna-path
  (:require [badigeon.bundle :as bundle]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.tools.deps.alpha.reader :as deps-reader]
            [clojure.walk :as walk]
            [overtone.helpers.file :refer [ensure-native]]
            [overtone.helpers.system :refer [get-os]]))

(defn map-keys
  "Apply f to each key in m"
  [m f]
  (reduce
   (fn [acc [k v]] (assoc acc (f k) v))
   {} m))

(defn- canonicalize-sym [s]
  (if (and (symbol? s) (nil? (namespace s)))
    (as-> (name s) n (symbol n n))
    s))

(defn- canonicalize-all-syms
  [deps-map]
  (walk/postwalk
   #(cond-> % (map? %) (map-keys canonicalize-sym))
   deps-map))

(defn- slurp-deps-edn []
  (if (.exists (io/file "deps.edn"))
    (deps-reader/slurp-deps "deps.edn")
    (-> "deps.edn"
        io/resource
        slurp
        edn/read-string
        canonicalize-all-syms)))

;; extract the native dependencies with badigeon
(bundle/extract-native-dependencies
 (System/getProperty "user.dir")
 {:deps-map (slurp-deps-edn)
  :allow-unstable-deps? true
  :native-path "native"
  :native-prefixes {'overtone/ableton-link ""
                    'overtone/scsynth "native"
                    'overtone/scsynth-extras "native"}})


;; set jna.library.path to point to native libraries
;; dependant on OS. No path merge to prevent clj-native
;; from pulling out third party lib files from path
(defonce __SET_JNA_PATH__
  (do (ensure-native)
      (case (get-os)
        :linux   (System/setProperty "jna.library.path" "native/linux/x86_64")
        :mac     (System/setProperty "jna.library.path" "native/macosx/x86_64")
        :windows (System/setProperty "jna.library.path" "native/windows/x86_64"))))

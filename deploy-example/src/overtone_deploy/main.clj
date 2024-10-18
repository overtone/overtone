(ns overtone-deploy.main
  (:gen-class)
  (:require [overtone.live :as o]
            [overtone.config.store :as store]
            [overtone.helpers.file :refer [home-dir]]
            [overtone.sc.machinery.server.connection :refer [scsynth-path]])
  (:import [java.io File]))

(defn canonical-path [^String s]
  (.getCanonicalPath (File. s)))

(defn -main [& args]
  (try (println "Overtone config file:" (canonical-path store/OVERTONE-CONFIG-FILE))
       (println "home dir" (canonical-path (home-dir)))
       (println "scsynth path" (canonical-path (scsynth-path)))
       (finally
         (System/exit 0))))

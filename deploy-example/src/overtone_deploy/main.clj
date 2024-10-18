(ns overtone-deploy.main
  (:gen-class)
  (:require [overtone.live :as o]
            [overtone.config.store :as store]
            [overtone.sc.machinery.server.connection :refer [scsynth-path]])
  (:import [java.io File]))

(defn canonical-path [^String s]
  (.getCanonicalPath (File. s)))

(defn -main [& args]
  (try (println "Overtone config file:" (canonical-path store/OVERTONE-CONFIG-FILE))
       (println "scsynth path" (scsynth-path))
       (println "scsynth executable" (-> (scsynth-path) first canonical-path))
       (catch Throwable e
         (println "Abnormal exit")
         (prn e)
         (System/exit 1))
       (finally
         (System/exit 0))))

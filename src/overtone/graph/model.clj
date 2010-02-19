(ns 
  #^{:doc "Functions to perform basic operations on the overtone graph model."
     :author "Jeff Rose"}
  overtone.graph.model
  (:use (overtone.graph base)
     (overtone.core setup config ugen synth synthdef)))

(config-default :db-path (str OVERTONE-DIR "/db")

(defonce db* (ref (graph (:db-path @config*))))

(defn load-ugen-spec [spec]
  (.add @db* spec))

(defn load-ugen-specs []
  (doseq [spec UGEN-SPECS]
    (load-ugen-spec spec)))


(defproject overtone "0.7.0-SNAPSHOT"
  :description "Programmable Music."
  :url "http://project-overtone.org"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [org.clojure/core.incubator "0.1.0"]
                 [org.clojure/data.json "0.1.2"]
                 [overtone/scsynth-jna "0.1.2-SNAPSHOT"]
                 [overtone/at-at "0.2.1"]
                 [overtone/osc-clj "0.7.1"]
                 [overtone/byte-spec "0.3.1"]
                 [overtone/midi-clj "0.2.1"]
                 [clj-glob "1.0.0"]
                 [org.clojure/core.match "0.2.0-alpha6"]
                 [seesaw "1.3.0"]]
  :jvm-opts ["-Xms256m" "-Xmx1g" "-XX:+UseConcMarkSweepGC"])

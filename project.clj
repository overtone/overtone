(defproject overtone "0.5.0"
  :description "Programmable Music."
  :url "http://project-overtone.org"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojure/core.incubator "0.1.0"]
                 [org.clojure/data.json "0.1.2"]
                 [overtone/scsynth-jna "0.1.2-SNAPSHOT"]
                 [overtone/at-at "0.2.1"]
                 [overtone/osc-clj "0.7.1"]
                 [overtone/byte-spec "0.3.1"]
                 [overtone/midi-clj "0.2.1"]
                 [clj-glob "1.0.0"]]
  :classpath "examples"
  :jvm-opts ["-Xms256m" "-Xmx1g" "-XX:+UseConcMarkSweepGC"])

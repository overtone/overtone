(defproject overtone "0.4.0"
  :description "An audio/musical experiment."
  :url "http://project-overtone.org"
  :autodoc {:load-except-list [#"/test/" #"/classes/" #"/devices/"]
            :namespaces-to-document ["overtone.core" "overtone.gui"
                                     "overtone.music" "overtone.studio"]
            :trim-prefix "overtone.",}
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojure/core.incubator "0.1.0"]
                 [org.clojure/data.json "0.1.2"]
                 [overtone/scsynth-jna "0.1.2-SNAPSHOT"]
                 [overtone/at-at "0.2.1"]
                 [overtone/osc-clj "0.7.1"]
                 [overtone/byte-spec "0.3.1"]
                 [overtone/midi-clj "0.2.1"]
                 [clj-glob "1.0.0"]]
  :dev-dependencies [[marginalia "0.2.0"]]
  :jvm-opts ["-Xms256m" "-Xmx1g" "-XX:+UseConcMarkSweepGC"])

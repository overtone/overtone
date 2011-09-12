(defproject overtone "0.3.0"
  :description "An audio/musical experiment."
  :url "http://project-overtone.org"
  :autodoc {:load-except-list [#"/test/" #"/classes/" #"/devices/"]
            :namespaces-to-document ["overtone.core" "overtone.gui"
                                     "overtone.music" "overtone.studio"]
            :trim-prefix "overtone.",}
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [overtone/scsynth-jna "0.1.2-SNAPSHOT"]
                 [overtone/at-at "0.1.1"]
                 [overtone/osc-clj "0.6.2"]
                 [overtone/byte-spec "0.2.0-SNAPSHOT"]
                 [overtone/midi-clj "0.2.0-SNAPSHOT"]
                 [org.clojars.overtone/vijual "0.2.1"]]
  :dev-dependencies [[marginalia "0.2.0"]]
  :jvm-opts ["-Xms256m" "-Xmx1g" "-XX:+UseConcMarkSweepGC"])

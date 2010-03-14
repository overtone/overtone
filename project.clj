(defproject overtone "0.1"
  :description "An audio/musical experiment."
  :dependencies [
                 [org.clojure/clojure "1.1.0"]
                 [org.clojure/clojure-contrib "1.1.0"]

                 [penumbra "0.5.0"]
                 [org.clojars.rosejn/jsyntaxpane "0.9.5-b27"]
                 [vijual "0.1.0-SNAPSHOT"]
                 [jline "0.9.94"]

                 [osc-clj "0.1"]
                 [byte-spec "0.1"]
                 [midi-clj "0.1"]

                 [overtone/clj-scsynth "0.0.1-SNAPSHOT"]
                 [overtone/clj-jack    "0.0.1-SNAPSHOT"]
                 [overtone/clj-repl    "0.0.1-SNAPSHOT"]
                 [overtone/clj-scenegraph "0.0.1-SNAPSHOT"]]
  :native-dependencies [[lwjgl "2.2.2"]
                        [overtone/scsynth "3.3.1"] 
                        [overtone/scsynth-jna "3.3.1"]]
  :dev-dependencies [[native-deps "1.0.0"]
                     [lein-clojars "0.5.0-SNAPSHOT"]
                     [autodoc "0.7.0"]
                     [org.clojars.ato/nailgun "0.7.1"]
                     [lein-nailgun "0.1.0"]
                     [swank-clojure "1.1.0-SNAPSHOT"]
                     [leiningen/lein-swank "1.1.0"]]
            :main overtone.app.repl)

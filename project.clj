(defproject overtone "0.1.1"
  :description "An audio/musical experiment."
  :url "http://project-overtone.org"
  :main overtone.app.main
  :autodoc {:load-except-list [#"/test/" #"/classes/" #"/devices/"]
            :namespaces-to-document ["overtone.core" "overtone.gui" "overtone.music" "overtone.studio"],
            :trim-prefix "overtone.",}
  :namespaces [overtone.app.main] ; ns to compile
  :dependencies [[org.clojure/clojure "1.2.0-master-SNAPSHOT"]
                 [org.clojure/clojure-contrib "1.2.0-SNAPSHOT"]
                 [org.freedesktop.tango/tango-icon-theme "0.8.90"]
                 [org.clojars.nakkaya/miglayout "3.7.3.1"]
                 [scenegraph "0.0.1-SNAPSHOT"]

                 [overtone/jsyntaxpane "0.9.5-b27"]
                 [overtone/osc-clj "0.1.4-SNAPSHOT"]
                 [overtone/byte-spec "0.1"]
                 [overtone/midi-clj "0.1"]
                 [overtone/substance "6.0"]
                 [overtone/javadocking "1.4.0"]

                 [overtone/scsynth-jna "3-SNAPSHOT"]
                 [vijual "0.1.0-SNAPSHOT"]

                 ;[scenegraph/decora-hw "0.0.1-SNAPSHOT"] ;; decora-hw and decora-jogl are not yet fully tested
                 ;[scenegraph/decora-jogl "0.0.1-SNAPSHOT"] ;;
                 ;[penumbra "0.5.0"]
                 ]
  :native-dependencies [
                        ;[lwjgl "2.2.2"]
                        ]
  :dev-dependencies [[native-deps "1.0.0"]
                     [leiningen-run "0.3"]
                     [lein-clojars "0.5.0-SNAPSHOT"]
                     [autodoc "0.7.1"]
                     [org.clojars.ato/nailgun "0.7.1"]
                     [org.clojars.rosejn/vimclojure "2.2.0-SNAPSHOT"]
                     [org.clojars.brandonw/lein-nailgun "1.0.0"]
                     [swank-clojure "1.1.0-SNAPSHOT"]
                     [leiningen/lein-swank "1.1.0"]])

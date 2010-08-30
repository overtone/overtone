(defproject overtone "0.1.2-SNAPSHOT"
  :description "An audio/musical experiment."
  :url "http://project-overtone.org"
;  :main overtone.app.main
  :autodoc {:load-except-list [#"/test/" #"/classes/" #"/devices/"]
            :namespaces-to-document ["overtone.core" "overtone.gui" "overtone.music" "overtone.studio"]
            :trim-prefix "overtone.",}
;  :namespaces [overtone.app.main] ; ns to compile
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [org.freedesktop.tango/tango-icon-theme "0.8.90"]
                 [org.clojars.nakkaya/miglayout "3.7.3.1"]
                 [scenegraph "0.0.1-SNAPSHOT"]

                 [overtone/jsyntaxpane "0.9.5-b27"]
                 [overtone/osc-clj "0.2.1-SNAPSHOT"]
                 [overtone/byte-spec "0.2.0-SNAPSHOT"]
                 [overtone/midi-clj "0.2.0-SNAPSHOT"]
                 [overtone/substance "6.0"]
                 [overtone/javadocking "1.4.0"]

                 [overtone/scsynth-jna "0.1.2-SNAPSHOT"]
                 [vijual "0.1.0-SNAPSHOT"]
                ])

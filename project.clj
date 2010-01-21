(defproject overtone "0.1"
  :description "An audio/musical experiment."

  :repositories [["java.net" "http://download.java.net/maven/2/"]]

  :dependencies [[org.clojure/clojure "1.1.0-alpha-SNAPSHOT"]
                 [org.clojure/clojure-contrib "1.0-SNAPSHOT"]
                 [net.java.dev.scenegraph/scenegraph "svn"]
                 [org.clojars.rosejn/jvi "0.7.1"]
                 [org.clojars.rosejn/jsyntaxpane "0.9.5-b27"]
                 [jfree/jfreechart "1.0.12"]
                 [jline "0.9.94"]
                 [osc-clj "0.1"]
                 [byte-spec "0.1"]
                 [midi-clj "0.1"]
                ]

  :dev-dependencies [[autodoc "0.7.0"]
                     [swank-clojure "1.1.0-SNAPSHOT"]
                     [org.clojars.ato/nailgun "0.7.1"]
                     [lein-nailgun "0.1.0"]
                     [lein-clojars "0.5.0-SNAPSHOT"]]

  :main overtone.studio.gui.main)

(defproject overtone "0.1.1"
  :description "An audio/musical experiment."
  :dependencies [[org.clojure/clojure "1.1.0"]
                 [org.clojure/clojure-contrib "1.1.0"]

                 [jline "0.9.94"]

                 ;                           [scenegraph/decora-hw  "0.0.1-SNAPSHOT"]     ;; decora-hw and decora-jogl are not yet fully tested
                 ;                           [scenegraph/decora-jogl "0.0.1-SNAPSHOT"]    ;; 
                 ;                           [scenegraph/decora-runtime "0.0.1-SNAPSHOT"] ;; scenegraph pulls in decora-runtime
                 [scenegraph "0.0.1-SNAPSHOT"]                     
                 ;[vijual "0.1.0-SNAPSHOT"]

                 ;[penumbra "0.5.0"]
                 [org.freedesktop.tango/tango-icon-theme "0.8.90"]

                 [org.ostermiller/utils "1.07.00"]
                 [org.clojars.nakkaya/miglayout "3.7"]
                 [overtone/clj-scsynth "0.0.2-SNAPSHOT"]

                 [overtone/jsyntaxpane "0.9.5-b27"]
                 [overtone/osc-clj "0.1.3-SNAPSHOT"]
                 [overtone/byte-spec "0.1"]
                 [overtone/midi-clj "0.1"]
                 [overtone/clj-scsynth "0.0.2-SNAPSHOT"]
                 [leiningen-run "0.3"]]
  :native-dependencies [[overtone/scsynth "3.3.1-SNAPSHOT"]
                        [overtone/scsynth-jna "3.3.1-SNAPSHOT"]
                        ;[lwjgl "2.2.2"]
                        ]
  :dev-dependencies [[native-deps "1.0.0"]
                     [lein-clojars "0.5.0-SNAPSHOT"]
                     [autodoc "0.7.0"]
                     [org.clojars.ato/nailgun "0.7.1"]
                     [org.clojars.rosejn/vimclojure "2.2.0-SNAPSHOT"]
                     [org.clojars.brandonw/lein-nailgun "1.0.0"]
                     [swank-clojure "1.1.0-SNAPSHOT"]
                     [leiningen/lein-swank "1.1.0"]]
  :main overtone.app.main)

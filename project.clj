(defproject overtone "0.8.0-SNAPSHOT"
  :description "Collaborative Programmable Music."
  :url "http://project-overtone.org"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojure/core.incubator "0.1.0"]
                 [org.clojure/data.json "0.1.2"]
                 [clj-native "0.9.2-SNAPSHOT"]
                 [overtone/at-at "1.0.0"]
                 [overtone/osc-clj "0.8.0"]
                 [overtone/byte-spec "0.3.1"]
                 [overtone/midi-clj "0.4.0"]
                 [overtone/libs.handlers "0.2.0"]
                 [overtone/scsynth "3.5.3-SNAPSHOT"]
                 [clj-glob "1.0.0"]
                 [org.clojure/core.match "0.2.0-alpha9"]
                 [seesaw "1.4.1"]]
  :native-path "native"
  :jvm-opts
  ["-Xms512m" "-Xmx1g"           ; Minimum and maximum sizes of the heap
   "-XX:+UseParNewGC"            ; Use the new parallel GC in conjunction with
   "-XX:+UseConcMarkSweepGC"     ;  the concurrent garbage collector
   "-XX:+CMSConcurrentMTEnabled" ; Enable multi-threaded concurrent gc work (ParNewGC)
   "-XX:MaxGCPauseMillis=20"     ; Specify a target of 20ms for max gc pauses
   "-XX:+CMSIncrementalMode"     ; Do many small GC cycles to minimize pauses
   "-XX:MaxNewSize=257m"         ; Specify the max and min size of the new
   "-XX:NewSize=256m"            ;  generation to be small
   "-XX:+UseTLAB"                ; Uses thread-local object allocation blocks. This
                                 ;  improves concurrency by reducing contention on
                                 ;  the shared heap lock.
   "-XX:MaxTenuringThreshold=0"  ; Makes the full NewSize available to every NewGC
                                 ;  cycle, and reduces the pause time by not
                                 ;  evaluating tenured objects. Technically, this
                                 ;  setting promotes all live objects to the older
                                        ;  generation, rather than copying them.
;;  "-XX:ConcGCThreads=2"        ; Use 2 threads with concurrent gc collections
;;  "-XX:TieredCompilation"      ; JVM7 - combine both client and server compilation
;;                               ;  strategies
;;  "-XX:CompileThreshold=1"     ; JIT each function after one execution
;;  "-XX:+PrintGC"               ; Print GC info to stdout
;;  "-XX:+PrintGCDetails"        ;  - with details
;;  "-XX:+PrintGCTimeStamps"     ;  - and timestamps
]
)

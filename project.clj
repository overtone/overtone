(require '[leiningen.core.utils :refer [get-os]])

(def JVMOPTS
  "Per os jvm options. Options common to all cases go under
  `:any`. Options specific to one OS go under the key returned by
  `leiningen.core.eval/get-os` for that system. Temporarily disabled
  options can be kept under `:disabled`."
  {:any
   ["-Xms512m" "-Xmx1g"           ; Minimum and maximum sizes of the heap
    "-XX:+UseParNewGC"            ; Use the new parallel GC in conjunction with
    "-XX:+UseConcMarkSweepGC"     ;  the concurrent garbage collector
    "-XX:+CMSConcurrentMTEnabled" ; Enable multi-threaded concurrent gc work (ParNewGC)
    "-XX:MaxGCPauseMillis=20"     ; Specify a target of 20ms for max gc pauses
    "-XX:MaxNewSize=257m"         ; Specify the max and min size of the new
    "-XX:NewSize=256m"            ;  generation to be small
    "-XX:+UseTLAB"                ; Uses thread-local object allocation blocks. This
                                        ;  improves concurrency by reducing contention on
                                        ;  the shared heap lock.
    "-XX:MaxTenuringThreshold=0"] ; Makes the full NewSize available to every NewGC
                                        ;  cycle, and reduces the pause time by not
                                        ;  evaluating tenured objects. Technically, this
                                        ;  setting promotes all live objects to the older
                                        ;  generation, rather than copying them.
   :macosx
   ["-Xdock:name=Overtone"]
   :disabled
   ["-XX:ConcGCThreads=2"         ; Use 2 threads with concurrent gc collections
    "-XX:TieredCompilation"       ; JVM7 - combine both client and server compilation
                                        ;  strategies
    "-XX:CompileThreshold=1"      ; JIT each function after one execution
    "-XX:+PrintGC"                ; Print GC info to stdout
    "-XX:+PrintGCDetails"         ;  - with details
    "-XX:+PrintGCTimeStamps"]})   ;  - and timestamps

(defn jvm-opts
  "Return a complete vector of jvm-opts for the current os."
  [] (let [os (get-os)]
       (vec (set (concat (get JVMOPTS :any)
                         (get JVMOPTS os))))))

(defproject overtone "0.11.0"
  :description "Collaborative Programmable Music."
  :url "http://overtone.github.io/"
  :mailing-list {:name "overtone"
                 :archive "https://groups.google.com/group/overtone"
                 :post "overtone@googlegroups.com"}
  :license {:name "The MIT License (MIT)"
            :url "http://opensource.org/licenses/MIT"
            :distribution :repo
            :comments "Please use Overtone for good"}

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/data.json "0.2.6"]
                 [clj-native "0.9.5"]
                 [overtone/at-at "1.2.0"]
                 [overtone/osc-clj "0.9.0"]
                 [overtone/byte-spec "0.3.1"]
                 [overtone/midi-clj "0.5.0"]
                 [overtone/libs.handlers "0.2.0"]
                 [overtone/ableton-link "1.0.0-alpha"]
                 [clj-glob "1.0.0"]
                 [net.java.dev.jna/jna "4.4.0"]
                 [overtone/scsynth "3.9.3-0"]
                 [overtone/scsynth-extras "3.9.3-0"]]
  :profiles {:test {:dependencies [[bultitude "0.2.0"]
                                   [polynome "0.2.2"]]}}
  :test-selectors {:core (fn [m] (not (some m [:gui :hw])))
                   :gui  :gui
                   :hw   :hw}
  :native-path "native"
  :min-lein-version "2.0.0"
  :jvm-opts ^:replace ~(jvm-opts))

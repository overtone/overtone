(ns run
  (:use 
     test-utils
     clojure.test))

(def TEST-NAMESPACES 
  [
   'osc-test
   'bytes-test
   'sc-test
   'synthdef-test
   ])

(defn run
  "Runs all defined tests"
  []
  (binding [*test-out* *out*] ; for vimclojure output
    (println "Loading tests: " TEST-NAMESPACES)
    (println "threads: " (Thread/activeCount))
    (apply require :reload-all TEST-NAMESPACES)
    (time (apply run-tests TEST-NAMESPACES))
    (println "threads: " (Thread/activeCount))
    ))

(run)

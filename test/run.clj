(ns test.run
  (:use 
     test-utils
     clojure.test))

(def TEST-DIR "test")

(def TEST-NAMESPACES (test-namespaces TEST-DIR))

(defn run
  "Runs all defined tests"
  []
  (binding [*test-out* *out*] ; for vimclojure output
    (println "Loading tests: " TEST-NAMESPACES)
    (apply require :reload-all TEST-NAMESPACES)
    (time (apply run-tests TEST-NAMESPACES))))

(run)

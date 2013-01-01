(ns overtone.smoke-test
  "Simple tests which exercise overtone and make few assertions.
  Ensures successful complilation of overtone and its examples."
  (:require [overtone.config.log :as log]
            [bultitude.core :as b])
  (:use overtone.core
        clojure.test
        test-helper))

;; Use a single internal server for all tests in this ns.
(use-fixtures :once with-internal-server)

;; Wait for all osc messages to be processed before moving on.
;; Trigger a synchronous reset to cleanup after each test
(use-fixtures :each with-sync-reset with-server-sync)


(deftest demo-test
  (eval-in-temp-ns
   (use '[clojure.test :only [is]])
   (use 'overtone.live)

   (is (and #'defsynth #'definst) "Core macros should be defined")
   (is (server-connected?) "Server should be connected")
   (is (mixer-booted?) "Mixer should be booted")

   (demo 0.1 (sin-osc))
   (Thread/sleep 100)))


;; Examples
;;
;; Create a test for each example ns under `overtone.examples.*` which
;; loads the ns and invokes the `-main` fn if it exists and logs *out*.
;;
;; TODO:
;;  * define a `-main` function for each example
;;  * make sure at-at scheduled functions are canceled or completed

(defn all-example-ns []
  (b/namespaces-on-classpath :prefix "overtone.examples"))

(defn example-test-fn
  [example-ns]
  (fn []
    (println "\nRunning example:" example-ns)
    (require example-ns)
    (when-let [-main (find-var (symbol (str example-ns) "-main"))]
      ((var-get -main)))))

(defn example-test-name [example-ns]
  (symbol (str (.replace (str example-ns) "." "-") "-test")))

(defn intern-example-tests []
  (doseq [example-ns (all-example-ns)]
    (let [test-name (example-test-name example-ns)
          test-fn (example-test-fn example-ns)]
      (doto (intern *ns* test-name test-fn)
        (alter-meta! assoc :test test-fn)))))

(when *load-tests* (intern-example-tests))


(let [ns (the-ns *ns*)]
  (defn test-ns-hook
    "Calls test-var on every var interned in this namespace, with
    fixtures. The order in which the tests are run is randomized to
    expose any inter-test dependencies that may exist."
    []
    (let [once-fixture-fn (join-fixtures (:clojure.test/once-fixtures (meta ns)))
          each-fixture-fn (join-fixtures (:clojure.test/each-fixtures (meta ns)))]
      (once-fixture-fn
       (fn []
         (doseq [v (shuffle (vals (ns-interns ns)))]
           (when (:test (meta v))
             (each-fixture-fn (fn [] (test-var v))))))))))

(comment
  (run-tests)
  )
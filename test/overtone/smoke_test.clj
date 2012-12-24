(ns overtone.smoke-test
  "Simple tests which exercise overtone and make few assertions.
  Ensures successful complilation of overtone and its examples."
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

(comment
  (run-tests)
  )
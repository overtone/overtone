(ns overtone.smoke-test
  (:require overtone.sc.server)
  (:use clojure.test))

;; borrowed from clojure.test-helper
(defmacro eval-in-temp-ns [& forms]
  `(binding [*ns* *ns*]
     (in-ns (gensym))
     (clojure.core/use 'clojure.core)
     (eval
            '(do ~@forms))))


;; Use overtone.live to smoke out obvious errors.
;; This may fail if you're audio card is unavailable.
(deftest use-overtone-live-test
  (overtone.sc.server/kill-server)

  (eval-in-temp-ns
   (use '[clojure.test :only [is]]
        '[overtone.live])
   (is (and #'defsynth #'definst) "Core macros should be defined")
   (is (server-connected?) "Server should be connected")
   (is (mixer-booted?) "Mixer should be booted"))

  ;; clean-up so we don't impact later tests!
  ;; does this do the trick?
  (overtone.sc.server/kill-server))
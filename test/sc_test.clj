(ns sc-test
  (:use 
     clojure.test
     clojure.contrib.seq-utils
     clj-backtrace.repl
     overtone.utils)
  (:require 
     [overtone.sc :as sc]
     [org.enclojure.commons.c-slf4j :as log]))

(log/ensure-logger)

(def ditty-notes [50 50 57 50 48 62 62 50])
(def ditty-durs  [0.25 0.25 0.5 0.125 0.125 0.25 0.25 0.5])

(defn ms [s]
  (* 1000.0 s))

(defn play [inst notes durs]
  (loop [notes notes
         durs durs
         t (now)]
    (when (and notes durs)
      (println t ": " (first notes) " -> " (first durs))
      (sc/hit t inst :pitch (first notes) :dur (ms (first durs)))
      (recur (next notes) (next durs) (+ t (ms (first durs)))))))

(deftest boot-test []
  (try 
    (sc/boot)
    (is (not (nil? @sc/server*)))
    (is (= 1 (:num-ugens (sc/status))))
    (play "sin" ditty-notes ditty-durs)
    (Thread/sleep 3000)
    (finally 
      (sc/quit))))


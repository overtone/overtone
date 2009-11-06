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

(def ditty-notes [40 40 47 40 38 52 52 40])
(def ditty-durs  [0.15 0.2 0.3 0.4 0.15 0.15 0.2 0.8])

(defn play [inst notes durs]
  (let [t (+ (now) 100)]
    (loop [notes notes
           durs durs]
      (when (and notes durs)
        (println "n: " (first notes) " d: " (first durs))
        (sc/hit (+ t (* (first durs) 1000.0)) inst :pitch (first notes) :dur (first durs))
        (recur (next notes) (next durs))))))

(deftest boot-test []
  (try 
    (sc/boot)
    (is (not (nil? @sc/server*)))
    (is (= 1 (:num-ugens (sc/status))))
    (play "sin" ditty-notes ditty-durs)
    (Thread/sleep 3000)
    (finally 
      (sc/quit))))


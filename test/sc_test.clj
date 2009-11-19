(ns sc-test
  (:use 
     clojure.test
     clojure.contrib.seq-utils
     clj-backtrace.repl
     overtone.utils)
  (:require 
     [overtone.sc :as sc]
     [overtone.log :as log]))

(def DEBUG? true)

(def ditty-notes [50 50 57 50 48 62 62 50])
(def ditty-durs  [250 250 500 125 125 250 250 500])

(defn play [inst notes durs]
  (loop [notes notes
         durs durs
         t (now)]
    (when (and notes durs)
      (println t ": " (first notes) " -> " (first durs))
      (sc/hit t inst :pitch (first notes) :dur (first durs))
      (recur (next notes) (next durs) (+ t (first durs))))))

(deftest boot-test []
  (try 
    (sc/boot)
    (is (not (nil? @sc/server*)))
    (is (= 1 (:n-groups (sc/status))))
    (play "sin" ditty-notes ditty-durs)
    (Thread/sleep 3000)
    (finally 
      (sc/quit))))

(defn groups-test []
  (sc/group :head sc/DEFAULT-GROUP)
  (sc/group :head sc/DEFAULT-GROUP)
  (sc/group :head sc/DEFAULT-GROUP)
  (is (= 4 (:n-groups (sc/status)))))

(defn nodes-test [])

(deftest server-messaging-test []
  (try
    (sc/boot)
;    (if DEBUG? (sc/notify))
    (groups-test)
    (nodes-test)
    (finally 
      (sc/quit))))


(ns overtone.sc.core-test
  (:require
    [clojure.test :refer [deftest is use-fixtures]]
    [overtone.sc.server :refer [boot-server status kill-server]]
    [overtone.sc.node :refer [group group-free]]
    [overtone.sc.machinery.server.connection :refer [connection-info*]]
    [overtone.music.time :refer [now]]
    [overtone.config.log :as log]
    [overtone.test-helper :as th]))

(use-fixtures :once th/ensure-server)

(deftest boot-test
  (is (some? @connection-info*))
  (is (some? (:n-groups (status)))))

(def DEFAULT-GROUP 1)

(deftest groups-test
  (let [init-groups (:n-groups (status))
        a (group :head DEFAULT-GROUP)
        b (group :head DEFAULT-GROUP)
        c (group :head DEFAULT-GROUP)]
    (is (= 3 (- (:n-groups (status)) init-groups)))
    (run! group-free [a b c])
    (is (= init-groups (:n-groups (status))))))

#_(deftest node-tree-test
    ;; (reset)
    (let [g1 (group :head 0)
          g2 (group :tail 0)]
      (hit :sin :dur 2000 :target g2)
      (Thread/sleep 100)
      (is (= 1 (:n-synths (status))))))

;; These are what the responses look like for a queryTree msg.  The first
;; without and the second with control information.
(def no-ctls [0 0 2 1 2 2 0 3 0 1001 -1 "sin"])
(def with-ctls [1 0 2 1 2 2 0 3 0 1001 -1 "sin" 3 "out" 0.0 "pitch" 40.0 "dur" 100000.0])

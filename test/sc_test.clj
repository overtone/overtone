(ns sc-test
  (:use 
     clojure.test
     clojure.contrib.seq-utils
     overtone)
  (:require 
      overtone.instrument.synth
     [overtone.log :as log]))

(def ditty-notes [50 50 57 50 48 62 62 50])
(def ditty-durs  [250 250 500 125 125 250 250 500])

(defn play-seqs [inst notes durs]
  (loop [notes notes
         durs durs
         t (now)]
    (when (and notes durs)
      (hit t inst :pitch (first notes) :dur (first durs))
      (recur (next notes) (next durs) (+ t (first durs))))))

(deftest boot-test []
  (try 
    (boot)
    (is (not (nil? @server*)))
    (is (= 1 (:n-groups (status))))
    (load-synth overtone.instrument.synth/sin)
    (Thread/sleep 1000)
    (play-seqs "sin" ditty-notes ditty-durs)
    (Thread/sleep 3000)
    (finally 
      (quit))))

(defn groups-test []
  (let [a (group :head DEFAULT-GROUP)
        b (group :head DEFAULT-GROUP)
        c (group :head DEFAULT-GROUP)]
    (is (= 4 (:n-groups (status))))
    (group-free a b c)
    (is (= 1 (:n-groups (status))))
    ; We should get the old ID again with the bitset allocator
    (is (= a (group :head DEFAULT-GROUP)))))

(defn node-tree-test []
  (reset)
  (let [g1 (group :head 0)
        g2 (group :tail 0)]
    (hit :sin :dur 2000 :target g2)
    (Thread/sleep 100)
    (is (= 1 (:n-synths (status))))))

; These are what the responses look like for a queryTree msg.  The first
; without and the second with control information.
(def no-ctls [0 0 2 1 2 2 0 3 0 1001 -1 "sin"])
(def with-ctls [1 0 2 1 2 2 0 3 0 1001 -1 "sin" 3 "out" 0.0 "pitch" 40.0 "dur" 100000.0])

(deftest server-messaging-test []
  (try
    (boot)
    (load-synth overtone.instrument.synth/sin)
    (groups-test)
    (node-tree-test)
    (reset)
    (finally 
      (quit))))

(defn sc-tests []
  (binding [*test-out* *out*]
    (run-tests 'sc-test)))

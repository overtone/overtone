(ns osc-test
  (:use (overtone osc)
     clojure.test
     clojure.contrib.seq-utils
     clj-backtrace.repl)
  (:require [org.enclojure.commons.c-slf4j :as log])
  (:import (java.nio ByteBuffer)))

(log/ensure-logger)

(def HOST "127.0.0.1")
(def PORT (+ 1000 (rand-int 10000)))

(def STR "test-string")

(deftest osc-msg-test []
  (let [buf (ByteBuffer/allocate 128)
        t-args [(make-array Byte/TYPE 20) 
              42 
              (float 4.2) 
              "qwerasdf" 
              (double 123.23) 
              (long 123412341234)]
        _ (osc-encode-msg buf (apply osc-msg "/asdf" "bifsdh" t-args))
        _ (.position buf 0)
        {:keys [path args] :as msg} (osc-decode-packet buf)
        ]
    (is (= "/asdf" path))
    (is (= (count t-args) (count args)))
    (is (= (ffirst t-args) (ffirst args)))
    (is (= (last (first t-args)) (last (first args))))
    (is (= (next t-args) (next args)))))

(deftest osc-basic-test []
  (let [server (osc-server PORT)
        client (osc-client HOST PORT)]
    (try
      (osc-snd client "/test" "i" 42)
      (is true)
      (finally 
        (osc-close server true)
        (osc-close client true)))))

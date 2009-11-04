(ns osc-test
  (:use (overtone osc)
     clojure.test
     clojure.contrib.seq-utils
     clj-backtrace.repl)
     
  (:import (java.nio ByteBuffer)))

(def HOST "127.0.0.1")
(def PORT 1234)

(def STR "test-string")

(deftest osc-msg-test []
  (let [buf (ByteBuffer/allocate 128)
        args [42 4.2 "qwerasdf"]
        _ (osc-encode-msg buf (apply osc-msg "/asdf" "ifs" args))
        _ (.position buf 0)
        msg (osc-decode-packet buf)]
    (println "msg: " msg)
    (is (= "/asdf" (:path msg)))
    (is (= (count args) (count (:args msg))))
    (is (= args (:args msg)))))

(deftest osc-basic-test [])
  (let [server (osc-server PORT)
        client (osc-client HOST PORT)]
    (osc-snd client "/test" "i" 42)

    (is true)))

(defn osc-tests []
  (binding [*test-out* *out*]
    (run-tests 'osc-test)))

(ns examples.sclang
  (:use overtone.live)
  (:use clj-sclang.core)
  (:use clojure.contrib.duck-streams))

;(boot-server :internal "127.0.0.1" 5300)
(connect-jack-ports)
(def cb (make-vpost-cb println))
(sclang-start cb)
;; wait ... until (isCompiledOk)
(sclang-connect-to-external "127.0.0.1" 5300)

(interpret "s.queryAllNodes;")


(interpret (slurp "src/example/hello.sc"))
;(node-free 1000)


(load-sample "samples/kick.wav")
(interpret (slurp "src/example/play_buf.sc"))

(defn kick []
  (interpret "
s.sendMsg(\"/s_new\", \"my_PlayBuf\", s.nextNodeID, 1, 1, \"out\", 0, \"bufnum\", 0);
"))

(kick)

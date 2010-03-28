(ns examples.sclang
  (:use overtone.live)
  (:use clj-sclang.core)
  (:use clojure.contrib.duck-streams))

(refer-ugens)
(boot :internal "127.0.0.1" 5300)
(def cb (make-vpost-cb println))        
(sclang-start cb)

;(interpret "\"Hello, World!\".postln")



(interpret "
s.queryAllNodes;
")
(interpret (slurp "src/examples/hello.sc"))

;(node-free 1003)

(interpret (slurp "src/examples/play_buf.sc"))

(load-sample "samples/kick.wav")

(defn kick []
  (interpret "
s.sendMsg(\"/s_new\", \"my_PlayBuf\", s.nextNodeID, 1, 1, \"out\", 0, \"bufnum\", 0);
"))

(kick)
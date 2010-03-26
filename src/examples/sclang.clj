(ns examples.sclang
  (:use overtone.live)
  (:use clj-sclang.core)
  (:use [clojure.contrib.duck-streams :only (slurp)]))

(refer-ugens)
(boot :internal 5300)
(def cb (make-vpost-cb println))        
(sclang-start cb)

(interpret "\"Hello, World!\".postln")


(interpret "
s = Server.new(\\localhost, NetAddr(\"127.0.0.1\", 5300));
")

(interpret "
s.remoteControlled = true;
")
(interpret "
s.boot;
s.notify;
")

(use 'clojure.contrib.duck-streams)

(interpret (slurp "hello.sc"))




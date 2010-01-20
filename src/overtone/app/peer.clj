(ns overtone.studio.peer
  (:import (org.jgroups JChannel Message ReceiverAdapter))
  (:use clojure.set
     clojure.contrib.seq-utils
     (overtone osc)))

(def DEFAULT-PORT 2345)
(def GROUP-NAME "overtone")

(defonce peer-server* (ref nil))
(defonce port* (ref nil))
(defonce peers* (ref #{}))

(def FILES ["a" "b" "c"])

(defn list-files []
  FILES)

(defn get-file [f]
  "This is file data...")

(defn get-peers []
  @peers*)

(defn add-peer [& peers]
  (dosync (alter peers* union (set peers))))

(defn clear-peers []
  (dosync (ref-set peers* #{})))

(defn chat-receiver []
  (proxy [ReceiverAdapter] []
    (viewAccepted [v] (println "view: " v))
    (receive [msg] (println (str "msg(" (.getSrc msg) "): " (.getObject msg))))))

(defn chat-loop [chan]
  (loop [input (read-line)]
    (println "chat-loop: " input)
      (when (not (= "quit" input))
        (.send chan (Message. nil nil input))
        (println "sent-message")
        (recur (read-line)))))
 
(defn start-peer []
  (let [chan (JChannel.)]
    (.connect chan "OvertoneChat")
    (.setReceiver chan (chat-receiver))
    (chat-loop chan)
    (.close chan)))

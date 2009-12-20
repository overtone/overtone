(ns overtone.peer
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

(def HANDLERS
  {"/list-files" list-files
   "/get-file" get-file
   "/get-peers" get-peers
   "/add-peer" add-peer
   })

(defn files []
  (doall (for [[host port] @peers*]
           (do 
             (osc-target @peer-server* host port)
             (osc-send @peer-server* "/list-files" "i" @port*)
             (osc-recv @peer-server* "/list-files.reply")))))

(defn- reply 
  "Reply to the host that sent msg, but substitute the first argument from the incoming
  message for the source port, since they are running peer-servers also."
  [msg & args]
  (let [args (flatten args)]
    (osc-target @peer-server* (:src-host msg) (first (:args msg)))
    (apply osc-send @peer-server* (str (:path msg) ".reply") (osc-type-tag args) args)))

; TODO: Pick up here...
; * make it so handler responses are automatically sent back
; to the requester with path.reply as the path.  Need to 
; implement a function like this: (osc-reply msg path args)
; that will look at the src address and send the reply.
(defn peer-start [& [port]]
  (let [port (or port DEFAULT-PORT)]
    (dosync 
      (ref-set peer-server* (osc-server port))
      (ref-set port* port))
    (doseq [[path handler] HANDLERS]
      (osc-handle @peer-server* path 
                  (fn [msg] 
                    (println port "got msg: " msg)
                    (if-let [res (apply handler (next (:args msg)))]
                      (apply reply msg res)))))))

(defn peer-quit []
  (osc-close @peer-server* true))

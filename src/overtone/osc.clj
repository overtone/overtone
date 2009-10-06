(ns overtone.osc
  (:import (java.net InetSocketAddress))
  (:import (de.sciss.net OSCClient OSCBundle OSCMessage OSCListener)))

(def DUMP-OFF 0)
(def DUMP-ON  1) 

;; TODO: Replace this frustrating OSC client or figure out how to get it
;; working correctly on remote servers also...
(defn osc-client 
 "Returns an OSC client ready to communicate with host on port.  
 Use :protocol in the options map to \"tcp\" if you don't want \"udp\"."
  [host port & [proto]]
  (let [protocol (or proto "udp")
        addr (InetSocketAddress. host port)
        client (OSCClient/newUsing protocol 0 true)]
    (.setTarget client addr)
    (.start client)
    client))

(defn osc-debug 
  "Turn OSC debug output to stderr on and off. [true or false]"
  [client debug]
  (let [debug (if debug DUMP-ON DUMP-OFF)]
    (.dumpOSC client debug System/err)))

(defn osc-bundle 
  "Wrap msgs in an OSC bundle to be executed simultaneously."
  [msgs & [timestamp]]
  (let [t (or timestamp (System/currentTimeMillis))
        bndl (OSCBundle. t)]
    (doseq [msg msgs]
      (.addPacket bndl msg))
    bndl))

(defn osc-msg 
  "Create an OSC message sending args to addr.  The type field is automatically created, so be sure to use the correct types or coerce them if necessary."
  [addr & args]
  (OSCMessage. addr (to-array args)))

(defn osc-snd 
  "Send OSC msg to client."
  [client msg]
  (.send client msg))

(defn- clj-msg [msg sender timestamp]
  {:sender sender
   :time timestamp
   :name (.getName msg)
   :args (doall (for [i (range (.getArgCount msg))] (.getArg msg i)))})

(defn osc-listen
  "Set a handler function for incoming osc messages."
  [client fun]
  (let [listener (proxy [OSCListener] []
                   (messageReceived [msg sender timestamp] 
                                    (fun (clj-msg msg sender timestamp))))]
    (.addOSCListener client listener)))

(defn print-msg [msg]
  (println (apply str "osc-msg: " (.getName msg)
                  (for [i (range (.getArgCount msg))] 
                    (str " " (.getArg msg i))))))

(ns overtone.osc
  (:import (java.net InetSocketAddress))
  (:import (de.sciss.net OSCClient OSCBundle OSCMessage)))

(def DUMP-OFF 0)
(def DUMP-ON  1) 

(defn osc-client 
 "Returns an OSC client ready to communicate with host on port.  
 Use :protocol in the options map to \"tcp\" if you don't want \"udp\"."
  [host port & argmap]
  (let [protocol (get argmap :protocol "udp")
        addr (InetSocketAddress. host port)
        client (OSCClient/newUsing protocol)]
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

(defn print-msg [msg]
  (println (apply str "osc-msg: " (.getName msg)
                  (for [i (range (.getArgCount msg))] 
                    (str " " (.getArg msg i))))))

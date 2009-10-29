(ns overtone.osc
  (:import 
     (java.net InetSocketAddress DatagramSocket DatagramPacket)
     (java.nio.channels DatagramChannel)
     (java.nio ByteBuffer ByteOrder))
  (:use (clojure.contrib fcase)))

; OSC Data Types:
; int => i
;  * 32-bit big-endian two's complement integer
;
; long => h
;  * 64-bit big-endian two's complement integer
;
; float => f
;  * 32-bit big-endian IEEE 754 floating point number
;
; string => s
;  * A sequence of non-null ASCII characters followed by a null, followed by 0-3 additional null characters to make the total number of bits a multiple of 32.
;
; blob => b
;  * An int32 size count, followed by that many 8-bit bytes of arbitrary binary data, followed by 0-3 additional zero bytes to make the total number of bits a multiple of 32.
;
; OSC-timetag 
;  * 64-bit big-endian fixed-point timestamp 

(def *buf (ByteBuffer/allocate 8192))
(def PAD (make-array Byte/TYPE 4))

(defn put-string [buf s]
  (.put buf (.getBytes s))
  (.put buf PAD 0 (- 4 (bit-and (.position *buf) 0x03))))

(defn osc-encode [buf address type-tag args]
  (.clear buf)
  (put-string buf address)
  (put-string buf (str "," type-tag))
  (doseq [[t arg] (map vector type-tag args)]
    (println "type: " t "arg: " arg)
    (case t
          \i (.putInt buf (int arg))
          \h (.putLong buf (long arg))
          \f (.putFloat buf (float arg))
          \d (.putDouble buf (double arg))
          \s (put-string buf arg))))

(defn get-string [])
(defn osc-decode [buf])

(defn osc-send 
  "Send OSC msg to client."
  ([client address] (osc-send client address nil))
  ([client address type-tag & args]
  (osc-encode *buf address type-tag args)
  (.send (:chan client) *buf (:addr client))))

(defn osc-client 
 "Returns an OSC client ready to communicate with host on port.  
 Use :protocol in the options map to \"tcp\" if you don't want \"udp\"."
  [host port]
  (let [chan (DatagramChannel/open)
        sock (.socket chan)]
  {:host host
   :port port
   :addr (InetSocketAddress. host port)
   :chan chan}))

(comment defn osc-bundle 
  "Wrap msgs in an OSC bundle to be executed simultaneously."
  [msgs & [timestamp]]
  (let [t (or timestamp (System/currentTimeMillis))
        bndl (OSCBundle. t)]
    (doseq [msg msgs]
      (.addPacket bndl msg))
    bndl))

(defn- clj-msg [msg sender timestamp]
  {:sender sender
   :time timestamp
   :name (.getName msg)
   :args (doall (for [i (range (.getArgCount msg))] (.getArg msg i)))})

(comment defn osc-listen
  "Set a handler function for incoming osc messages."
  [client fun]
  (let [listener (proxy [OSCListener] []
                   (messageReceived [msg sender timestamp] 
                                    (fun (clj-msg msg sender timestamp))))]
    (.addOSCListener client listener)))

(comment defn print-msg [msg]
  (println (apply str "osc-msg: " (.getName msg)
                  (for [i (range (.getArgCount msg))] 
                    (str " " (.getArg msg i))))))

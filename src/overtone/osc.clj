(ns overtone.osc
  (:import 
     (java.util.concurrent TimeUnit TimeoutException)
     (java.net InetSocketAddress DatagramSocket DatagramPacket)
     (java.nio.channels DatagramChannel AsynchronousCloseException)
     (java.nio ByteBuffer ByteOrder))
  (:require [overtone.log :as log])
  (:use (overtone utils)
     clojure.set
     (clojure.contrib fcase)))

(def OSC-TIMETAG-NOW 1) ; Timetag representing right now.
(def SEVENTY-YEAR-SECS 2208988800)

(def PAD (byte-array 4))

(defn- osc-now []
  (System/currentTimeMillis))

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

; TODO: Figure out how to detect a byte array correctly...
(defn osc-type-tag [args]
  (apply str 
    (map #(instance-case %1
            Integer "i"
            Long    "h"
            Float   "f"
            Double  "d"
            (type PAD) "b" ; This is lame... what is a byte array an instance of?
            String  "s") 
         args)))

(defn osc-msg
  [path & args]
  (with-meta {:path path
              :type-tag (first args)
              :args (next args)} 
             {:type :osc-msg}))

(defn osc-msg? [obj] (= :osc-msg (type obj)))

(defn osc-bundle
  [timestamp items]
  (with-meta {:timestamp timestamp
              :items items}
             {:type :osc-bundle}))

(defn osc-bundle? [obj] (= :osc-bundle (type obj)))

(defn- osc-pad 
  "Add 0-3 null bytes to make buffer position 32-bit aligned."
  [buf]
  (let [extra (mod (.position buf) 4)]
    (if (pos? extra)
      (.put buf PAD 0 (- 4 extra)))))

(defn- osc-align
  "Jump the current position to a 4 byte boundary for OSC compatible alignment."
  [buf]
  (.position buf (bit-and (bit-not 3) (+ 3 (.position buf)))))

(defn- encode-string [buf s]
  (.put buf (.getBytes s))
  (.put buf (byte 0))
  (osc-pad buf))

(defn- encode-blob [buf b]
  ;(log/debug (str "Encoding blob size: " (count b)))
  (.putInt buf (count b))
  (.put buf b)
  (osc-pad buf))

(defn- encode-timetag
  ([buf] (encode-timetag buf (osc-now)))
  ([buf timestamp]
   (let [secs (+ (/ timestamp 1000) ; secs since Jan. 1, 1970
                 SEVENTY-YEAR-SECS) ; to Jan. 1, 1900
         fracs (/ (bit-shift-left (long (mod timestamp 1000)) 32) 
                  1000)
         tag (bit-or (bit-shift-left (long secs) 32) (long fracs))]
     (.putLong buf (long tag)))))

(defn osc-encode-msg [buf msg]
  (let [{:keys [path type-tag args]} msg]
    ;(log/debug (str "osc-encode-msg " path type-tag))
    (encode-string buf path)
    (encode-string buf (str "," type-tag))
    (doseq [[t arg] (map vector type-tag args)]
      (case t
            \i (.putInt buf (int arg))
            \h (.putLong buf (long arg))
            \f (.putFloat buf (float arg))
            \d (.putDouble buf (double arg))
            \b (encode-blob buf arg)
            \s (encode-string buf arg))
      ;(log/debug (str "pos:" (.position buf) "type:" t "arg: " arg ))
      )))

(defn osc-encode-bundle [buf bundle] 
  ;(log/debug "osc-encode-bundle")
  (encode-string buf "#bundle")
  (encode-timetag buf (:timestamp bundle))
  (doseq [item (:items bundle)]
    ; A bit of a hack...
    ; Write an empty bundle element size into the buffer, then encode
    ; the actual bundle element, and then go back and write the correct
    ; size based on the new buffer position.
    (let [start-pos (.position buf)]
      (.putInt buf (int 0))
      (cond
        (osc-msg? item) (osc-encode-msg buf item)
        (osc-bundle? item) (osc-encode-bundle buf item))
      (let [end-pos (.position buf)]
        (.position buf start-pos)
        (.putInt buf (- end-pos start-pos 4))
        (.position buf end-pos)))))

(defn- decode-string [buf]
  (let [start (.position buf)]
    (while (not (zero? (.get buf))) nil)
    (let [end (.position buf)
          len (- end start)
          str-buf (byte-array len)]
      (.position buf start)
      (.get buf str-buf 0 len)
      (osc-align buf)
      (String. str-buf 0 (dec len)))))

(defn- decode-blob [buf]
  (let [size (.getInt buf)
        blob (byte-array size)]
    (.get buf blob 0 size)
    (osc-align buf)
    blob))

(defn- decode-msg
  [buf]
  ;(log/debug (str "decoding from pos: " (.position buf)))
  (let [path (decode-string buf)
        type-tag (decode-string buf)
        args (reduce (fn [mem t] 
                       ;(log/debug (str "decoded: " mem))
                       (conj mem 
                             (case t
                                   \i (.getInt buf)
                                   \h (.getLong buf)
                                   \f (.getFloat buf)
                                   \d (.getDouble buf)
                                   \b (decode-blob buf)
                                   \s (decode-string buf))))
                     []
                     (rest type-tag))]
    (log/debug "osc msg decoded: " path " " type-tag  " " args)
    {:path path
     :args args}))

(defn- decode-timetag [buf]
  (let [tag (.getLong buf)
        secs (- (bit-shift-right tag 32) SEVENTY-YEAR-SECS)
        ms-frac (bit-shift-right (* (bit-and tag (bit-shift-left 0xffffffff 32))
                                    1000) 32)]
    (+ (* secs 1000) ; secs as ms
       ms-frac)))

(defn- osc-bundle-buf? [buf]
  (let [start-char (.get buf)]
    (.position buf (- (.position buf) 1))
    (= \# start-char)))

(defn- decode-bundle [buf] 
  (let [b-tag (decode-string buf)
        timestamp (decode-timetag buf)]))

(defn osc-decode-packet
  "Decode an OSC packet buffer into a bundle or message map."
  [buf]
  (if (osc-bundle-buf? buf)
    (decode-bundle buf)
    (decode-msg buf)))

(defn recv-next-msg [chan buf]
  (.clear buf)
  (let [src-addr (.receive chan buf)]
    (log/debug "osc-msg-from: " src-addr)
    (when (pos? (.position buf))
      (.flip buf)
      (assoc (osc-decode-packet buf) :src-addr src-addr))))

(defn- listen-loop [chan buf running? listeners]
  (try
    (while @running?
      (let [pkt (recv-next-msg chan buf)
            msgs (if (osc-bundle? pkt)
                   (:items pkt)
                   [pkt])]
        (log/debug "listen-loop: " (count msgs) " messages for " (count @listeners) " listeners.")
        (doseq [msg msgs]
          (doseq [[id listener] @listeners] 
            (listener msg)))))
  (catch AsynchronousCloseException e nil) ; No problem if the connection is closed
  (catch Exception e
    (log/error "Exception in listen-loop: " e " \nstacktrace: " (.printStackTrace e)))
  (finally
    (.close chan))))

(defn- generic-listen-thread [chan buf running? listeners]
  (let [thread (Thread. #(listen-loop chan buf running? listeners))]
    (.start thread)
    thread))

(def listener-id-counter* (atom 0))

(defn- listener-id []
  (let [id @listener-id-counter*]
    (swap! listener-id-counter* inc)
    id))

(declare *osc-handlers*)
(declare *osc-handler*)
(declare *osc-path*)

(defn- msg-handler-dispatcher [handlers]
  (fn [msg]
    (doseq [handler (get @handlers (:path msg))]
      (binding [*osc-handlers* handlers
                *osc-handler* handler
                *osc-path* (:path msg)]
        (handler msg)))))

(defn osc-remove-handler []
  (reset! *osc-handlers* (assoc @*osc-handlers* *osc-path* 
                                (difference (get @*osc-handlers* *osc-path*) #{*osc-handler*}))))

(defn osc-handle
  "Receive on a connection, being either a client or a server object."
  [con path handler & [one-shot]]
  (let [handlers (:handlers con)
        phandlers (get @handlers path #{})
        handler (if one-shot
                  (fn [msg] 
                    (handler msg)
                    (osc-remove-handler))
                  handler)]
    (swap! handlers assoc path (union phandlers #{handler})))) ; save the handler

(defn osc-recv
  "Receive a message on an osc node with an optional timeout."
  [con path & [timeout]]
  (let [p (promise)]
    (osc-handle con path (fn [msg] 
                           (osc-remove-handler)
                           (deliver p msg)))
    (try 
      (if timeout 
        (.get (future @p) timeout TimeUnit/MILLISECONDS) ; Blocks until 
        @p)
      (catch TimeoutException t 
        (log/debug "osc-path-recv: " path " timed out.")
        nil))))

;; We use binding to *osc-msg-bundle* to bundle messages 
;; and send combined with an OSC timestamp.
(def *osc-msg-bundle* nil)

(defn- snd-client [client]
  (let [{:keys [snd-buf chan addr]} client]
    ; Flip sets limit to current position and resets position to start.
    (.flip snd-buf) 
    (.send chan snd-buf addr)
    (.clear snd-buf))) ; clear resets everything 

(defn osc-snd-msg 
  "Send OSC msg to client."
  [client msg]
  (if *osc-msg-bundle*
    (swap! *osc-msg-bundle* #(conj %1 msg))
    (do
      (osc-encode-msg (:snd-buf client) msg)
      (snd-client client))))

(defn osc-snd-bundle
  "Send OSC bundle to client."
  [client bundle]
  (osc-encode-bundle (:snd-buf client) bundle)
  (snd-client client))

(defn osc-snd 
  "Creates an OSC message and either sends it to the server immediately 
  or if a bundle is currently being formed it adds it to the list of messages."
  [client & args]
  (osc-snd-msg client (apply osc-msg args)))

(defmacro in-osc-bundle [client timestamp & body]
  `(binding [*osc-msg-bundle* (atom [])]
     ~@body
     ;(log/debug (str "in-osc-bundle (" (count @*osc-msg-bundle*) "): " @*osc-msg-bundle*))
     (osc-snd-bundle ~client (osc-bundle ~timestamp @*osc-msg-bundle*))))

; OSC peers have listeners and handlers.  A listener is sent every message received, and
; handlers are dispatched by OSC node (a.k.a. path).
  
(defn- osc-peer []
  (let [chan (DatagramChannel/open)
        rcv-buf (ByteBuffer/allocate 8192)
        running? (atom true)
        handlers (atom {})
        listeners (atom {(listener-id) (msg-handler-dispatcher handlers)})
        thread (generic-listen-thread chan rcv-buf running? listeners)]
    {:chan chan
     :rcv-buf rcv-buf
     :running? running?
     :thread thread
     :listeners listeners
     :handlers handlers}))

(defn osc-client 
 "Returns an OSC client ready to communicate with a host on a given port.  
 Use :protocol in the options map to \"tcp\" if you don't want \"udp\"."
  [host port]
  (let [peer (osc-peer)
        snd-buf (ByteBuffer/allocate 8192)]
    (assoc peer
           :host host
           :port port
           :addr (InetSocketAddress. host port)
           :snd-buf snd-buf)))

(defn osc-server
  "Returns a live OSC server ready to register handler functions."
  [port]
  (let [peer (osc-peer)
        sock (.socket (:chan peer))
        _    (.bind sock (InetSocketAddress. port))]
    peer))

(defn osc-close
  [server & hard]
  (reset! (:running? server) false)
  (Thread/sleep 200)
  (.close (:chan server))
  (if hard 
    (.interrupt (:thread server))))

(ns overtone.osc
  (:import 
     (java.util.concurrent TimeUnit TimeoutException)
     (java.net InetSocketAddress DatagramSocket DatagramPacket)
     (java.nio.channels DatagramChannel AsynchronousCloseException ClosedChannelException)
     (java.nio ByteBuffer ByteOrder))
  (:require [overtone.log :as log])
  (:use (overtone util)
     clojure.set
     (clojure.contrib fcase)))

(log/level :debug)

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
  (let [type-tag (first args)
        type-tag (if (.startsWith type-tag ",")
                   (.substring type-tag 1)
                   type-tag)]
    (with-meta {:path path
                :type-tag type-tag
                :args (next args)} 
               {:type :osc-msg})))

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
    ;(log/debug "osc-encode: " path type-tag args)
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
  "Pull data out of the message according to the type tag."
  [buf]
  (let [path (decode-string buf)
        type-tag (decode-string buf)
        args (reduce (fn [mem t] 
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
    (log/debug "osc-decoded: " path " " type-tag  " " args)
    (apply osc-msg path type-tag args)))

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

; TODO: complete implementation of receiving osc bundles 
; * We need to recursively go through the bundle and decode either
;   sub-bundles or a series of messages.
(defn osc-decode-packet
  "Decode an OSC packet buffer into a bundle or message map."
  [buf]
  (if (osc-bundle-buf? buf)
    (decode-bundle buf)
    (decode-msg buf)))

(defn recv-next-packet [chan buf]
  (.clear buf)
  (let [src-addr (.receive chan buf)]
    (when (pos? (.position buf))
      (.flip buf)
      [src-addr (osc-decode-packet buf)])))

(defn- handle-bundle [listeners src bundle]
  (log/error "Receiving OSC bundles not yet implemented!")
  (throw Exception "Receiving OSC bundles not yet implemented!"))

(defn- handle-msg [listeners src msg]
  (let [msg (assoc msg 
                   :src-host (.getHostName src) 
                   :src-port (.getPort src))]
    (log/debug "handling message: " msg)
    (log/debug "n-listeners: " (count @listeners))
    (doseq [[id listener] @listeners] 
      (listener msg))))

(defn- listen-loop [chan buf running? listeners]
  ;(log/debug "########\nlisten-loop: " (.getId (Thread/currentThread)))
  (while @running?
    (try
      (let [[src pkt] (recv-next-packet chan buf)]
        (log/debug "listen-loop msg: " src pkt)
        (cond 
          (osc-bundle? pkt) (handle-bundle listeners src pkt) 
          (osc-msg? pkt)    (handle-msg listeners src pkt)))
      (catch AsynchronousCloseException e 
        (log/debug "AsynchronousCloseException - running: " @running?) )
      (catch ClosedChannelException e 
        (log/debug "ClosedChannelException: - running: " @running?)
        (log/debug (.printStackTrace e)))
      (catch Exception e
        (log/error "Exception in listen-loop: " e " \nstacktrace: " 
                   (.printStackTrace e))
        (throw e))))
  ;(log/debug "########\ncompleted listen-loop: " (.getId (Thread/currentThread)))
  (if (.isOpen chan)
    (.close chan)))

(defn- listen-thread [chan buf running? listeners]
  (let [thread (Thread. #(listen-loop chan buf running? listeners))]
    ;(.setDaemon thread false)
    (.start thread)
    thread))

(def listener-id-counter* (atom 0))

(defn- listener-id []
  (let [id @listener-id-counter*]
    (swap! listener-id-counter* inc)
    id))

(declare *osc-handlers*)
(declare *current-handler*)
(declare *current-path*)

(defn- msg-handler-dispatcher [handlers]
  (fn [msg]
    (doseq [handler (get @handlers (:path msg))]
      (binding [*osc-handlers* handlers
                *current-handler* handler
                *current-path* (:path msg)]
        (log/debug "dispatching msg: " msg " to: " handler)
        (handler msg)))))

(defn osc-remove-handler []
  (dosync (alter *osc-handlers* assoc *current-path* 
                 (difference (get @*osc-handlers* *current-path*) #{*current-handler*}))))

(defn osc-handle
  "Attach a handler function to receive on the specified path.  (Works for both clients and servers.)

  (let [server (osc-server PORT)
        client (osc-client HOST PORT)
        flag (atom false)]
    (try
      (osc-handle server \"/test\" (fn [msg] (reset! flag true)))
      (osc-send client \"/test\" \"i\" 42)
      (Thread/sleep 200)
      (= true @flag)))

"
                                  
  [peer path handler & [one-shot]]
  (let [handlers (:handlers peer)
        phandlers (get @handlers path #{})
        handler (if one-shot
                  (fn [msg] 
                    (handler msg)
                    (osc-remove-handler))
                  handler)]
    (dosync (alter handlers assoc path (union phandlers #{handler}))))) ; save the handler

(defn osc-recv
  "Receive a single message on an osc path (node) with an optional timeout.

      ; Wait a max of 250 ms to receive the next incoming OSC message
      ; addressed to the /magic node.
      (osc-recv client \"/magic\" 250)
  "
  [peer path & [timeout]]
  (let [p (promise)]
    (osc-handle peer path (fn [msg] 
                           (deliver p msg)
                            (osc-remove-handler)))
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

(defn- peer-send [peer]
  (let [{:keys [snd-buf chan addr]} peer]
    ; Flip sets limit to current position and resets position to start.
    (.flip snd-buf) 
    (.send chan snd-buf addr)
    (.clear snd-buf))) ; clear resets everything 

(defn osc-send-msg 
  "Send OSC msg to peer."
  [peer msg]
  (if *osc-msg-bundle*
    (swap! *osc-msg-bundle* #(conj %1 msg))
    (do
      (osc-encode-msg (:snd-buf peer) msg)
      (peer-send peer))))

(defn osc-send-bundle
  "Send OSC bundle to peer."
  [peer bundle]
  (osc-encode-bundle (:snd-buf peer) bundle)
  (peer-send peer))

(defn osc-send 
  "Creates an OSC message and either sends it to the server immediately 
  or if a bundle is currently being formed it adds it to the list of messages."
  [client & args]
  (osc-send-msg client (apply osc-msg args)))

(defmacro in-osc-bundle [client timestamp & body]
  `(binding [*osc-msg-bundle* (atom [])]
     ~@body
     ;(log/debug (str "in-osc-bundle (" (count @*osc-msg-bundle*) "): " @*osc-msg-bundle*))
     (osc-send-bundle ~client (osc-bundle ~timestamp @*osc-msg-bundle*))))

; OSC peers have listeners and handlers.  A listener is sent every message received, and
; handlers are dispatched by OSC node (a.k.a. path).
  
(defn- osc-peer []
  (let [chan (DatagramChannel/open)
        rcv-buf (ByteBuffer/allocate 8192)
        snd-buf (ByteBuffer/allocate 8192)
        running? (ref true)
        handlers (ref {})
        listeners (ref {(listener-id) (msg-handler-dispatcher handlers)})
        thread (listen-thread chan rcv-buf running? listeners)]
    (.configureBlocking chan true)
    {:chan chan
     :rcv-buf rcv-buf
     :snd-buf snd-buf
     :running? running?
     :thread thread
     :listeners listeners
     :handlers handlers}))

(defn osc-client 
 "Returns an OSC client ready to communicate with a host on a given port.  
 Use :protocol in the options map to \"tcp\" if you don't want \"udp\"."
  [host port]
  (let [peer (osc-peer)
        sock (.socket (:chan peer))
        local (.getLocalPort sock)]
    (.bind sock (InetSocketAddress. local))
    (assoc peer
           :host host
           :port port
           :addr (InetSocketAddress. host port))))

(defn osc-target
  "Update the target address of an OSC client so future calls to osc-send
  will go to a new destination."
  [client host port]
  (assoc client 
         :host host
         :port port
         :addr (InetSocketAddress. host port)))

(defn osc-server
  "Returns a live OSC server ready to register handler functions."
  [port]
  (let [peer (osc-peer)
        sock (.socket (:chan peer))]
    (.bind sock (InetSocketAddress. port))
    peer))

(defn osc-close
  [peer & wait]
  (log/debug "closing osc-peer...")
  (dosync (ref-set (:running? peer) false))
  (.close (:chan peer))
  (if wait
    (.join (:thread peer))))

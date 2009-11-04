(ns overtone.osc
  (:import 
     (java.util.concurrent TimeUnit TimeoutException)
     (java.net InetSocketAddress DatagramSocket DatagramPacket)
     (java.nio.channels DatagramChannel)
     (java.nio ByteBuffer ByteOrder))
  (:use (clojure.contrib fcase)))

(defn osc-now []
  (System/currentTimeMillis))

(def OSC-TIMETAG-NOW 1) ; Timetag representing right now.
(def SEVENTY-YEAR-SECS 2208988800)

(defn- byte-array [size]
  (make-array Byte/TYPE size))

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

;(defmacro deftype [type-name arg-vec & body] 
;  `(defn ~type-name ~arg-vec
;     (assoc (do @body) :type (keyword type-name)))
;  `(defn (str ~type-name "?") [obj#] 
;     (= (keyword type-name) (get obj# :type))))
;
;(deftype osc-msg [path & args]
;  {:path path
;   :type-tag (first args)
;   :args (next args)})

(defn osc-msg
  "Construct an osc message."
  [path & args]
  {:type :osc-msg
   :path path
   :type-tag (first args)
   :args (next args)})

(defn osc-msg? [obj]
  (= :osc-msg (get obj :type)))

(defn osc-bundle
  [timestamp & items]
  {:type :osc-bundle
   :timestamp timestamp
   :items items})

(defn osc-bundle? [obj]
  (= :osc-bundle (get obj :type)))

(def PAD (byte-array 4))

(defn osc-pad 
  "Add zero bytes to make 4 byte aligned.  If already 4 byte aligned adds an additional
  4 bytes of zeros."
  [buf]
  (.put buf PAD 0 (- 4 (mod (.position buf) 4))))

(defn osc-align
  "Jump the current position to a 4 byte boundary for OSC compatible alignment."
  [buf]
  (.position buf (bit-and (bit-not 3) (+ 3 (.position buf)))))

(defn encode-string [buf s]
  (.put buf (.getBytes s))
  (.put buf (byte 0))
  (osc-pad buf))

(defn encode-blob [buf b]
  (.putInt buf (count b))
  (.put buf b)
  (osc-pad buf))

(defn encode-timetag
  ([] (encode-timetag (osc-now)))
  ([timestamp]
   (let [secs (+ (/ timestamp 1000) ; secs since Jan. 1, 1970
                 SEVENTY-YEAR-SECS) ; to Jan. 1, 1900
         fracs (/ (bit-shift-left (mod timestamp 1000) 32) 
                  1000)
         tag (bit-or (bit-shift-left secs 32) fracs)]
     (.putLong (long tag)))))

(defn osc-encode-msg [buf msg]
  (let [{:keys [path type-tag args]} msg]
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
      (println "type(" (type t)"): " t " arg: " arg " pos: " (.position buf)))
    (.flip buf)))

(defn osc-encode-bundle [buf bundle] 
  (encode-string buf "#bundle")
  (encode-timetag (:timestamp bundle))
  (doseq [item (:items bundle)]
    (cond
      (osc-msg? item) (osc-encode-msg buf item)
      (osc-bundle? item) (osc-encode-bundle buf item))))

(defn decode-string [buf]
  (let [start (.position buf)]
    (while (not (zero? (.get buf))) nil)
    (let [end (.position buf)
          len (- end start)
          str-buf (byte-array len)]
      (.position buf start)
      (.get buf str-buf 0 len)
      (osc-align buf)
      (String. str-buf 0 (dec len)))))

(defn decode-blob [buf]
  (let [size (.getInt buf)
        blob (byte-array size)]
    (.get buf blob 0 size)
    (osc-align buf)
    blob))


;(let [secs (+ (/ timestamp 1000) ; secs since Jan. 1, 1970
;              SEVENTY-YEAR-SECS) ; to Jan. 1, 1900
;      fracs (/ (bit-shift-left (mod timestamp 1000) 32) 
;               1000)
;      tag (bit-or (bit-shift-left secs 32) fracs)]
          
(defn decode-msg
  [buf]
  (println "decoding from pos: " (.position buf))
  (let [path (decode-string buf)
        _ (println "path: " path " pos: " (.position buf))
        type-tag (decode-string buf)
        args (reduce (fn [mem t] 
                       (println "decoded: " mem)
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
    {:path path
     :args args}))

(defn decode-timetag [buf]
  (let [tag (.getLong buf)
        secs (- (bit-shift-right tag 32) SEVENTY-YEAR-SECS)
        ms-frac (bit-shift-right (* (bit-and tag (bit-shift-left 0xffffffff 32))
                                    1000) 32)]
    (+ (* secs 1000) ; secs as ms
       ms-frac)))

(defn osc-bundle-buf? [buf]
  (let [start-char (.get buf)]
    (.position buf (- (.position buf) 1))
    (= \# start-char)))

(defn decode-bundle [buf] 
  (let [
        b-tag (decode-string buf)
        timestamp (decode-timetag buf)]))

(defn osc-decode-packet
  "Decode an OSC packet buffer into a bundle or message map."
  [buf]
  (if (osc-bundle-buf? buf)
    (decode-bundle buf)
    (decode-msg buf)))

;; We use binding to *osc-msg-bundle* to bundle messages 
;; and send combined with an OSC timestamp.
(def *osc-msg-bundle* nil)

(defn snd-client [client]
  (let [{:keys [buf chan addr]} client]
    (.send chan buf addr)
    (.clear buf)))

(defn osc-snd-msg 
  "Send OSC msg to client."
  [client msg]
  (osc-encode-msg (:buf client) msg)
  (snd-client client))

(defn osc-snd-bundle
  "Send OSC bundle to client."
  [client bundle]
  (osc-encode-bundle (:buf client) bundle)
  (snd-client client))

(defn osc-snd 
  "Creates an OSC message and either sends it to the server immediately 
  or if a bundle is currently being formed it adds it to the list of messages."
  [client & args]
  (let [msg (apply osc-msg args)]
    (if *osc-msg-bundle*
      (swap! *osc-msg-bundle* #(conj %1 msg))
      (osc-snd-msg client msg))))

(defmacro in-osc-bundle [client timestamp & body]
  `(binding [*osc-msg-bundle* (atom [])]
     ~@body
     (osc-snd-bundle ~client (osc-bundle @*osc-msg-bundle* ~timestamp))))

;TODO: use some real logging and get rid of this garbage...
(def msg-count (ref 0))
(def msgs (ref []))

(defn recv-next-msg [chan buf]
  (let [src-addr (.receive chan buf)
        _ (.flip buf)
        msg (osc-decode-packet buf)]
    (dosync 
      (alter msg-count inc)
      (alter msgs conj (str "(" src-addr "): " msg)))
    (assoc msg :src-addr src-addr)))

(defn- handle-packet [pkt listeners]
  (cond 
    (osc-bundle? pkt) (doseq [item (:items pkt)]
                        (handle-packet item listeners)) 
    (osc-msg? pkt) (do
                     ; Listeners receive every message
                     (doseq [[id listener] @listeners] 
                       (listener pkt)))))

                     ; Handlers are filtered by path
  ;                   (doseq [[id handler] (get @handlers (:path pkt))] 
   ;                    (handler pkt)))))

(defn- generic-listen-thread
  [chan buf running? listeners]
  (let [thread (Thread. #(try
                           (while @running?
                             (.clear buf)
                             (recv-next-msg chan buf)
                             (handle-packet (osc-decode-packet buf) listeners))
                           (catch Exception e
                             (dosync (alter msgs conj e)))
                           (finally
                             (.close chan))))]
    (.start thread)
    thread))

(def handler-id-counter* (atom 0))
(defn handler-id []
  (let [id @handler-id-counter*]
    (swap! handler-id-counter* inc)
    id))

(defn msg-dispatcher [msg-log handlers]
  (fn [msg]
    (if-let [handler (get @handlers (:path msg))]
      (handler msg)
      (swap! msg-log assoc (:path msg) msg))))

; TODO: Messages should timeout and get wiped from the msg buffer
 
(defn osc-recv 
  "Receive on a connection, being either a client or a server object."
  [con path & [timeout]]
  ; First check the 1-msg buffer
  (if-let [recvd (get @(:msg-log con) path)]
    (do (swap! @(:msg-log con) dissoc path)
      recvd))

  (let [handlers (:handlers con)
        p (promise)]
    (swap! handlers assoc path p) ; set the handler
    (try 
      (if timeout 
        (.get (future @p) timeout TimeUnit/MILLISECONDS) ; Blocks until 
        @p)
      (catch TimeoutException t 
        (println (str "Status request timed out after " 
                      timeout "ms."))
        nil)
      (finally  ; remove the handler
        (swap! handlers dissoc path)))))

(defn osc-client 
 "Returns an OSC client ready to communicate with host on port.  
 Use :protocol in the options map to \"tcp\" if you don't want \"udp\"."
  [host port]
  (let [chan (DatagramChannel/open)
        buf (ByteBuffer/allocate 8192)
        resp-buf (ByteBuffer/allocate 8192)
        listening? true
        msg-log (atom {})
        handlers (atom {})
        listeners (atom {(handler-id) (msg-dispatcher msg-log handlers)})
        response-thread (generic-listen-thread chan resp-buf listening? listeners)]
    {:host host
     :port port
     :addr (InetSocketAddress. host port)
     :chan chan
     :buf buf 
     :listeners listeners
     :handlers handlers
     :resp-buf resp-buf}))

(defn osc-server
  "Returns a live OSC server ready to register handler functions."
  [port]
  (let [chan (DatagramChannel/open)
        sock (.socket chan)
        buf (ByteBuffer/allocate 8192)
        out *out*
        running? (atom true)
        msg-log (atom {})
        handlers (atom {})
        listeners (atom {(handler-id) (msg-dispatcher msg-log handlers)})
        listener (Thread. #(binding [*out* out]
                             (generic-listen-thread chan buf running? listeners)))]
    (.bind sock (InetSocketAddress. port))
    (.start listener)
    {:chan chan
     :buf buf
     :running? running?
     :listeners listeners
     :msg-log msg-log
     :handlers handlers
     :thread listener}))

(defn osc-close
  [server & hard]
  (reset! (:running? server) false)
  (if hard 
    (.interrupt (:thread server))))

(ns overtone.sc.buffer
  (:use [overtone.util lib]
        [overtone.libs event]
        [overtone.sc server]
        [overtone.sc.machinery defaults allocator]
        [overtone.sc.machinery.server comms connection]
        [overtone.sc.util :only [id-mapper]]))

(defn buffer-info
  "Fetch the information for buffer associated with buf-id (either an integer or
  an associative with an :id key). Synchronous."
  [buf-id]
  (let [buf-id (id-mapper buf-id)
        prom   (recv "/b_info" (fn [msg]
                                 (= buf-id (first (:args msg)))))]
    (with-server-sync #(snd "/b_query" buf-id))
    (let [msg                               (deref! prom)
          [buf-id n-frames n-channels rate] (:args msg)]
      (with-meta     {:size n-frames
                      :n-channels n-channels
                      :rate rate
                      :id buf-id}
        {:type ::buffer-info}))))

(defn buffer
  "Synchronously allocate a new zero filled buffer for storing audio data with
  the specified size and num-channels."
  ([size] (buffer size 1))
  ([size num-channels]
     (let [id   (with-server-self-sync (fn [uid]
                                         (alloc-id :audio-buffer
                                                   1
                                                   (fn [id]
                                                     (snd "/b_alloc" id size num-channels)
                                                     (server-sync uid)))))
           info (buffer-info id)]
       (with-meta
         {:allocated-on-server (atom true)
          :size (:size info)
          :n-channels (:n-channels info)
          :rate (:rate info)
          :id (:id info)}
         {:type ::buffer}))))

(defn buffer? [buf]
  (isa? (type buf) ::buffer))

(defn buffer-free
  "Synchronously free an audio buffer and the memory it was consuming."
  [buf]
  (assert (buffer? buf))
  (let [id (:id buf)]
    (with-server-self-sync (fn [uid]
                             (free-id :audio-buffer
                                      id
                                      1
                                      #(do (snd "/b_free" id)
                                           (reset! (:allocated-on-server buf) false)
                                           (server-sync uid)))))
    buf))

(defn buffer-read
  "Read a section of an audio buffer. Defaults to reading the full buffer if no
  start and len vals are specified. Returns a float array of vals.

  This is extremely slow for large portions of data. For more efficient reading
  of buffer data with the internal server, see buffer-data."
  ([buf] (buffer-read buf 0 (:size buf)))
  ([buf start len]
     (assert (buffer? buf))
     (assert @(:allocated-on-server buf))
     (let [buf-id  (:id buf)
           samples (float-array len)]
       (loop [n-vals-read 0]
         (if (< n-vals-read len)
           (let [n-to-read (min MAX-OSC-SAMPLES (- len n-vals-read))
                 offset    (+ start n-vals-read)
                 prom (recv "/b_setn" (fn [msg]
                                        (let [[msg-buf-id msg-start msg-len & m-args] (:args msg)]

                                          (and (= msg-buf-id buf-id)
                                               (= msg-start offset)
                                               (= n-to-read (count m-args))))))]
             (snd "/b_getn" buf-id offset n-to-read)
             (let [m (deref! prom)
                   [buf-id bstart blen & samps] (:args m)]
               (dorun
                (map-indexed (fn [idx el]
                               (aset-float samples (+ bstart idx) el))
                             samps))
               (recur (+ n-vals-read blen))))
           samples)))))

(defn buffer-write!
  "Write into a section of an audio buffer which modifies the buffer in place on
  the server. Data can either be a single number or a collection of numbers.
  Accepts an optional param start-idx which specifies an initial offset into the
  buffer from which to start writing the data (defaults to 0)."
  ([buf data] (buffer-write! buf 0 data))
  ([buf start-idx data]
     (assert (buffer? buf))
     (when (> (count data) MAX-OSC-SAMPLES)
       (throw (Exception. (str "Error - the data you attempted to write to the buffer was too large to be sent via UDP."))))
     (let [data (if (number? data) [data] data)
           size (count data)
           doubles (map double data)]
       (if (> (+ start-idx size) size)
         (throw (Exception. (str "the data you attempted to write to buffer " (:id buf) "was too large for its capacity. Use a smaller data list and/or a lower start index.")))
         (apply snd "/b_setn" (:id buf) start-idx size doubles)))))

(defn buffer-fill!
  "Fill a buffer range with a single value. Modifies the buffer in place on the
  server. Defaults to filling in the full buffer unless start and len vals are
  specified. Asynchronous."
  ([buf val]
     (assert (buffer? buf))
     (buffer-fill! buf 0 (:size buf) val))
  ([buf start len val]
     (assert (buffer? buf))
     (snd "/b_fill" (:id buf) start len (double val))))

(defn buffer-set!
  "Write a single value into a buffer. Modifies the buffer in place on the
  server. Index defaults to 0 if not specified."
  ([buf val] (buffer-set! buf 0 val))
  ([buf index val]
     (assert (buffer? buf))
     (snd "/b_set" (:id buf) index (double val))))

(defn buffer-get
  "Read a single value from a buffer. Index defaults to 0 if not specified."
  ([buf] (buffer-get buf 0))
  ([buf index]
     (assert (buffer? buf))
     (let [buf-id (:id buf)
           prom   (recv "/b_set" (fn [msg]
                                   (let [[msg-buf-id msg-start _] (:args msg)]
                                     (and (= msg-buf-id buf-id)
                                          (= msg-start index)))))]

       (with-server-sync #(snd "/b_get" buf-id index))
       (last (:args (deref! prom))))))

(defn buffer-save
  "Save the float audio data in an audio buffer to a wav file."
  [buf path & args]
  (assert (buffer? buf))

  (let [arg-map (merge (apply hash-map args)
                       {:header "wav"
                        :samples "float"
                        :size -1
                        :start-frame 0
                        :leave-open 0})
        {:keys [header samples n-frames start-frame leave-open]} arg-map]

    (snd "/b_write" (:id buf) path header samples
         n-frames start-frame
         (if leave-open 1 0))
    :done))

(defmulti buffer-id type)
(defmethod buffer-id java.lang.Integer [id] id)
(defmethod buffer-id java.lang.Long [id] id)
(defmethod buffer-id ::buffer [buf] (:id buf))
(defmethod buffer-id ::buffer-info [buf-info] (:id buf-info))

(defmulti buffer-size type)
(defmethod buffer-size ::buffer [buf] (:size buf))
(defmethod buffer-size ::buffer-info [buf-info] (:size buf-info))

(defn buffer-data
  "Get the floating point data for a buffer on the internal server."
  [buf]
  (when-not (internal-server?)
    (throw (Exception. (str "Only able to fetch buffer data directly from an internal server. Try #'buffer-read instead."))))
  (let [buf-id (buffer-id buf)
        snd-buf (.getSndBufAsFloatArray @sc-world* buf-id)]
    snd-buf))

;;TODO Check to see if this can be removed
(defn sample-info [s]
  (buffer-info (:buf s)))

(defn num-frames
  [buf]
  (:size  buf))

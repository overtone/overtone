(ns overtone.sc.buffer
  (:use [clojure.java.io :only [file]]
        [overtone.libs event]
        [overtone.sc server info defaults]
        [overtone.sc.machinery allocator]
        [overtone.sc.machinery.server connection comms]
        [overtone.sc server info]
        [overtone.helpers audio-file lib file]
        [overtone.sc.util :only [id-mapper]]))

(defrecord BufferInfo [id size n-channels rate n-samples rate-scale duration])

(defn buffer-info
  "Fetch the information for buffer associated with buf-id (either an
  integer or an associative with an :id key). Synchronous.

  Information returned is as follows:

  :size       - number of frames in the buffer
  :n-channels - number of audio channels stored in the buffer
  :rate       - rate of the buffer (typical rate is 44100 samples per
                second)
  :n-samples  - total number of samples in the buffer (* size n-channels)
  :rate-scale - rate to specify in order to play the buffer correctly
                according
                to the server's sample rate (/ rate (server-sample-rate))
  :duration   - duration of the buffer in seconds
  :id         - unique id for the buffer"
  [buf-id]
  (let [buf-id (id-mapper buf-id)
        prom   (recv "/b_info" (fn [msg]
                                 (= buf-id (first (:args msg)))))]
    (with-server-sync #(snd "/b_query" buf-id))
    (let [[id n-frames n-chans rate] (:args (deref! prom))
          server-rate                (server-sample-rate)
          n-samples                  (* n-frames n-chans)
          rate-scale                 (when (> server-rate 0)
                                       (/ rate server-rate))
          duration                   (when (> rate 0)
                                       (/ n-frames rate))]

      (map->BufferInfo
       {:id id
        :size n-frames
        :n-channels n-chans
        :rate rate
        :n-samples n-samples
        :rate-scale rate-scale
        :duration duration}))))

(defrecord Buffer [id size n-channels rate allocated-on-server])

(defn buffer
  "Synchronously allocate a new zero filled buffer for storing audio
  data with the specified size and num-channels. Size will be
  automatically floored and converted to a Long - i.e. 2.7 -> 2"
  ([size] (buffer size 1))
  ([size num-channels]
     (let [size (long size)
           id   (with-server-self-sync
                  (fn [uid]
                    (alloc-id :audio-buffer
                              1
                              (fn [id]
                                (snd "/b_alloc" id size num-channels)
                                (server-sync uid)))))
           info (buffer-info id)]

       (map->Buffer
        (assoc info
          :allocated-on-server (atom true))))))

(defrecord BufferFile [id size n-channels rate allocated-on-server path])

(defn buffer-alloc-read
  "Synchronously allocates a buffer with the same number of channels as
  the audio file given by 'path'. Reads the number of samples
  requested ('n-frames') into the buffer, or fewer if the file is
  smaller than requested. Reads sound file data from the given starting
  frame ('start') in the file. If 'n-frames' is less than or equal to
  zero, the entire file is read.

  Ignores OSC scheduling via the at macro; all inner OSC calls are sent
  immediately."
  ([path]
     (buffer-alloc-read path 0 -1))
  ([path start]
     (buffer-alloc-read path start -1))
  ([path start n-frames]
     (ensure-path-exists! path)
     (let [path (canonical-path path)
           f    (file path)
           id   (alloc-id :audio-buffer)]
       (snd-immediately
         (with-server-sync  #(snd "/b_allocRead" id path start n-frames))
         (let [info                              (buffer-info id)
               {:keys [id size rate n-channels]} info]
           (when (every? zero? [size rate n-channels])
             (free-id :audio-buffer id)
             (throw (Exception. (str "Unable to read file - perhaps path is not a valid audio file: " path))))

           (map->BufferFile
            (assoc info
              :allocated-on-server (atom true))))))))

(derive BufferInfo ::buffer-info)
(derive Buffer     ::buffer)
(derive BufferFile ::file-buffer)

(derive ::buffer      ::buffer-info)
(derive ::file-buffer ::buffer)

(defn buffer-info?
  "Returns true if b-info is buffer information. This includes buffers
  themselves in addition to the return value from #'buffer-info"
  [b-info]
  (isa? (type b-info) ::buffer-info))

(defn buffer?
  "Returns true if buf is a buffer."
  [buf]
  (isa? (type buf) ::buffer))

(defn file-buffer?
  [buf]
  (isa? (type buf) ::file-buffer))

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
  "Read a section of an audio buffer. Defaults to reading the full
  buffer if no start and len vals are specified. Returns a float array
  of vals.

  This is extremely slow for large portions of data. For more efficient
  reading of buffer data with the internal server, see buffer-data."
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
  "Write into a section of an audio buffer which modifies the buffer in
  place on the server. Data can either be a single number or a
  collection of numbers.  Accepts an optional param start-idx which
  specifies an initial offset into the buffer from which to start
  writing the data (defaults to 0)."
  ([buf data] (buffer-write! buf 0 data))
  ([buf start-idx data]
     (assert (buffer? buf))
     (when (> (count data) MAX-OSC-SAMPLES)
       (throw (Exception. (str "Error - the data you attempted to write to the buffer was too large to be sent via UDP."))))
     (let [data (if (number? data) [data] data)
           size (count data)
           doubles (map double data)]
       (if (> (+ start-idx size) (:size buf))
         (throw (Exception. (str "the data you attempted to write to buffer " (:id buf) "was too large for its capacity. Use a smaller data list and/or a lower start index.")))
         (apply snd "/b_setn" (:id buf) start-idx size doubles)))
     buf))


(defn buffer-write-relay!
  "Similar to buffer-write! except it is capable of handling very large
  buffers by slicing them up and writing each slice separately. Can be
  very slow."
  ([buf data] (buffer-write-relay! buf 0 data))
  ([buf start-idx data]
     (assert (buffer? buf))
     (loop [data-left data
            idx       0]
       (let [to-write  (take MAX-OSC-SAMPLES data-left)
             data-left (drop MAX-OSC-SAMPLES data-left)]
         (when-not (empty? to-write)
           (buffer-write! buf idx to-write)
           (recur data-left (+ idx (count to-write))))))
     buf))

(defn buffer-fill!
  "Fill a buffer range with a single value. Modifies the buffer in place
  on the server. Defaults to filling in the full buffer unless start and
  len vals are specified. Asynchronous."
  ([buf val]
     (assert (buffer? buf))
     (buffer-fill! buf 0 (:size buf) val))
  ([buf start len val]
     (assert (buffer? buf))
     (snd "/b_fill" (:id buf) start len (double val))
     buf))

(defn buffer-set!
  "Write a single value into a buffer. Modifies the buffer in place on
  the server. Index defaults to 0 if not specified."
  ([buf val] (buffer-set! buf 0 val))
  ([buf index val]
     (assert (buffer? buf))
     (snd "/b_set" (:id buf) index (double val))
     buf))

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
  "Save the float audio data in buf to a file in the specified path on the
  filesystem. The following options are also available (note: not all header
  and sample combinations work - incorrect combinations will fail silently):

   :header      - Header format: \"aiff\", \"next\", \"wav\", \"ircam\", \"raw\"
                  Default \"wav\"
   :samples     - Sample format: \"int8\", \"int16\", \"int24\", \"int32\",
                                 \"float\", \"double\", \"mulaw\", \"alaw\"
                  Default \"int16\"
   :n-frames    - Number of frames to write. If < 0 then all frames from
                  start-frame to the end of the buffer are written
                  Default -1
   :start-frame - starting frame in buffer (0 is the start of the buffer)
                  Default 0

   Example usage:
   (buffer-save buf \"~/Desktop/foo.wav\" :header \"aiff\" :samples \"int32\"
                                          :start-frame 100)"
  [buf path & args]
  (assert (buffer? buf))

  (let [path (resolve-tilde-path path)
        arg-map (merge (apply hash-map args)
                       {:header "wav"
                        :samples "int16"
                        :n-frames -1
                        :start-frame 0})
        {:keys [header samples n-frames start-frame]} arg-map]

    (snd "/b_write" (:id buf) path header samples
                    n-frames start-frame 0)
    :buffer-saved))

(defrecord BufferOutStream [id size n-channels header samples rate allocated-on-server path open?])

(defn buffer-stream
  "Returns a buffer-stream which is similar to a regular buffer but may
  be used with the disk-out ugen to stream to a specific file on disk.
  Use #'buffer-stream-close to close the stream to finish recording to
  disk.

  Options:

  :n-chans     - Number of channels for the buffer
                 Default 2
  :size        - Buffer size
                 Default 65536
  :header      - Header format: \"aiff\", \"next\", \"wav\", \"ircam\", \"raw\"
                 Default \"wav\"
  :samples     - Sample format: \"int8\", \"int16\", \"int24\", \"int32\",
                                \"float\", \"double\", \"mulaw\", \"alaw\"
                 Default \"int16\"

  Example usage:
  (buffer-stream \"~/Desktop/foo.wav\" :n-chans 1 :header \"aiff\"
                                       :samples \"int32\")"

  [path & args]
  (let [path    (resolve-tilde-path path)
        f-ext   (file-extension path)
        arg-map (merge {:n-chans 2
                        :size 65536
                        :header (or f-ext "wav")
                        :samples "int16"}
                       (apply hash-map args))
        {:keys [n-chans size header samples]} arg-map
        buf (buffer size n-chans)]
    (snd "/b_write" (:id buf) path header samples -1 0 1)
    (map->BufferOutStream
     (assoc buf
       :path path
       :header header
       :samples samples
       :open? (atom true)))))

(derive BufferOutStream ::buffer-out-stream)
(derive ::buffer-out-stream ::file-buffer)

(defn buffer-out-stream?
  [bs]
  (isa? (type bs) ::buffer-out-stream))

(defn buffer-stream-close
  "Close a buffer stream created with #'buffer-stream. Also frees the
  internal buffer. Returns the path of the newly created file."
  [buf-stream]
  (assert (file-buffer? buf-stream))
  (when-not @(:open? buf-stream)
    (throw (Exception. "buffer-stream already closed.")))

  (snd "/b_close" (:id buf-stream))
  (buffer-free buf-stream)
  (reset! (:open? buf-stream) false)
  (:path buf-stream))

(defrecord BufferInStream [id size n-channels rate allocated-on-server path open?])

(defn buffer-cue
  "Returns a buffer-cue which is similar to a regular buffer but may be
  used with the disk-in ugen to stream from a specific file on disk.
  Use #'buffer-cue-close to close the stream when finished.

  Options:

  :start       - Start frame in file.
                 Default 0
  :size        - Buffer size
                 Default 65536

  Example usage:
  (buffer-cue \"~/Desktop/foo.wav\" :start (* 3 44100))"

  [path & args]
  (let [path (resolve-tilde-path path)
        arg-map (merge {:start 0
                        :size 65536}
                       (apply hash-map args))
        {:keys [start size]} arg-map
        buf (buffer-alloc-read path start size)]
    (snd "/b_read" (:id buf) path start -1 0 1)
    (map->BufferInStream
      (assoc buf
        :path path
        :start start
        :open? (atom true)))))

(derive BufferInStream ::buffer-in-stream)
(derive ::buffer-in-stream ::file-buffer)

(defn buffer-in-stream?
  [bc]
  (isa? (type bc) ::buffer-in-stream))

(defn buffer-cue-pos
  "Moves the start position of a buffer cue to the frame indicated by
  'pos'. Defaults to 0. Returns the buffer when done."
  ([buf-cue]
     (buffer-cue-pos buf-cue 0))
  ([buf-cue pos]
     (assert (buffer-in-stream? buf-cue))
     (when-not @(:open? buf-cue)
       (throw (Exception. "buffer-in-stream is closed.")))
     (let [{:keys [id path]} buf-cue]
       (snd "/b_close" id)
       (snd "/b_read" id path pos -1 0 1))
     buf-cue))

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
  "Returns the size of the buffer."
  [buf]
  (:size buf))

(def TWO-PI (* 2 Math/PI))

(defn create-buffer-data
  "Create a sequence of floats for use as a buffer.  Result will contain
   values obtained by calling f with values linearly interpolated
   between range-min (inclusive) and range-max (exclusive).  For most
   purposes size must be a power of 2.

   Examples:

   Just a line from -1 to 1:
    (create-buffer-data 32 identity -1 1)

   Sine-wave for (osc) ugen:
    (create-buffer-data 512 #(Math/sin %) 0 TWO-PI)

   Chebyshev polynomial for wave-shaping:
    (create-buffer-data 1024 #(- (* 2 % %) 1) -1 1)"
  [size f range-min range-max]
  (let [range-size (- range-max range-min)
        rangemap  #(+ range-min (/ (* % range-size) size))]
    (map #(float (f (rangemap %))) (range 0 size))))

(defn- resolve-data-type
  [& args]
  (let [data (first args)]
    (cond
     (= :overtone.sc.buffer/buffer (type data)) ::buffer
     (sequential? data) ::sequence)))


(defmulti write-wav
  "Write data as a wav file. Accepts either a buffer or a sequence of values.
  When passing a sequence, you also need to specify the frame-rate and
  n-channels.  For both, you need to pass the path of the new file as
  the 2nd arg.

  Required args:
  buffer [data path]
  seq    [data path frame-rate n-channels]"
  resolve-data-type)


(defmethod write-wav ::buffer
  [data path]
  (write-audio-file-from-seq (buffer-data data) path (:rate data) (:n-channels data)))

(defmethod write-wav ::sequence
  [data path frame-rate n-channels]
  (write-audio-file-from-seq data path frame-rate n-channels))

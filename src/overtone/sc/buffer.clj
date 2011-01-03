(ns overtone.sc.buffer
  (:use [overtone.sc core]))

; TODO: Look into multi-channel buffers.  Probably requires adding multi-id allocation
; support to the bit allocator too...
; size is in samples
(defn buffer
  "Allocate a new buffer for storing audio data."
  [size & [channels]]
  (let [channels (or channels 1)
        id (alloc-id :audio-buffer)
        ready? (atom false)]
    (on-done "/b_alloc" #(reset! ready? true))
    (snd "/b_alloc" id size channels)
    (with-meta {:id id
                :size size
                :ready? ready?}
               {:type ::buffer})))

(defn buffer-ready?
  "Check whether a sample or a buffer has completed allocating and/or loading data."
  [buf]
  @(:ready? buf))

(defn buffer? [buf]
  (isa? (type buf) ::buffer))

(defn- buf-or-id [b]
  (cond
    (buffer? b) (:id b)
    (number? b) b
    :default (throw (Exception. "Not a valid buffer: " b))))

(defn buffer-free
  "Free an audio buffer and the memory it was consuming."
  [buf]
  (snd "/b_free" (:id buf))
  (free-id :audio-buffer (:id buf))
  :done)

; TODO: Test me...
(defn buffer-read
  "Read a section of an audio buffer."
  [buf start len]
  (assert (buffer? buf))
  (loop [reqd 0]
    (when (< reqd len)
      (let [to-req (min MAX-OSC-SAMPLES (- len reqd))]
        (snd "/b_getn" (:id buf) (+ start reqd) to-req)
        (recur (+ reqd to-req)))))
  (let [samples (float-array len)]
    (loop [recvd 0]
      (if (= recvd len)
        samples
        (let [msg-p (recv "/b_setn")
              msg (await-promise! msg-p)
              ;_ (println "b_setn msg: " (take 3 (:args msg)))
              [buf-id bstart blen & samps] (:args msg)]
          (loop [idx bstart
                 samps samps]
            (when samps
              (aset-float samples idx (first samps))
              (recur (inc idx) (next samps))))
          (recur (+ recvd blen)))))))

;; TODO: test me...
(defn buffer-write
  "Write into a section of an audio buffer."
  [buf start len data]
  (assert (buffer? buf))
  (snd "/b_setn" (:id buf) start len data))

(defn buffer-save
  "Save the float audio data in an audio buffer to a wav file."
  [buf path & args]
  (assert (buffer? buf))
  (let [arg-map (merge (apply hash-map args)
                       {:header "wav"
                        :samples "float"
                        :n-frames -1
                        :start-frame 0
                        :leave-open 0})
        {:keys [header samples n-frames start-frame leave-open]} arg-map]
    (snd "/b_write" (:id buf) path header samples
         n-frames start-frame
         (if leave-open 1 0))
    :done))

(defmulti buffer-id type)
(defmethod buffer-id java.lang.Integer [id] id)
(defmethod buffer-id ::buffer [buf] (:id buf))

(defn buffer-data
  "Get the floating point data for a buffer on the internal server."
  [buf]
  (let [buf-id (buffer-id buf)
        snd-buf (.getSndBufAsFloatArray @sc-world* buf-id)]
    snd-buf))

(defn buffer-info [buf]

  (let [mesg-p (recv "/b_info")
        _   (snd "/b_query" (buffer-id buf))
        msg (await-promise! mesg-p)
        [buf-id n-frames n-channels rate] (:args msg)]
    {:n-frames n-frames
     :n-channels n-channels
     :rate rate}))

(defn sample-info [s]
  (buffer-info (:buf s)))


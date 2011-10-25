(ns
    ^{:doc "Audio file encoding functions"
      :author "Sam Aaron"}
  overtone.helpers.audio-file
  (:use [overtone.sc.buffer :only [buffer-data]])
  (:import [javax.sound.sampled AudioFormat AudioFileFormat AudioFormat$Encoding
                                AudioFileFormat$Type AudioInputStream AudioSystem]
           [java.io File ByteArrayInputStream]
           [java.nio ByteBuffer]))

(defn- fill-data-buffer!
  [^ByteBuffer b-data data sample-bytes]
  (loop [idx 0
         data data]
    (when-not (empty? data)
      (let [b-idx (* sample-bytes idx)]
        (.putShort b-data b-idx (short (* (first data) Short/MAX_VALUE))))
      (recur (inc idx) (rest data))))

  b-data)

(defn- write-audio-file-from-seq
  "Writes contents of data to a new wav file with path. Adds frame-rate and
  n-channels as file metadata for appropriate playback/consumption of the new
  audio file."
  [data path frame-rate n-channels]
  (when (some #(or (< % -1) (> % 1)) data)
    (throw (Exception. (str "Unable to write audio file with this data as it contains sample points either less than -1 or greater than 1."))))
  (let [frame-rate   (float frame-rate)
        n-channels   (int n-channels)
        sample-bytes (/ Short/SIZE 8)
        frame-bytes  (* sample-bytes n-channels)
        a-format     (AudioFormat. AudioFormat$Encoding/PCM_SIGNED
                                   frame-rate
                                   Short/SIZE
                                   n-channels
                                   frame-bytes
                                   frame-rate
                                   true)
        data-size    (count data)
        n-bytes      (* data-size sample-bytes)
        b-data       (ByteBuffer/allocate n-bytes)
        b-data       (fill-data-buffer! b-data data sample-bytes)
        stream       (AudioInputStream. (ByteArrayInputStream. (.array b-data))
                                        a-format
                                        data-size)
        f            (File. path)
        f-type       AudioFileFormat$Type/WAVE]
    (AudioSystem/write stream f-type f)))

(defn- resolve-data-type
  [& args]
  (let [data (first args)]
    (cond
     (= :overtone.sc.buffer/buffer (type data)) ::buffer
     (sequential? data) ::sequence)))


(defmulti write-wav
  "Write data as a wav file. Accepts either a buffer or a sequence of values.
  When passing a sequence, you also need to specify the frame-rate and n-channels.
  For both, you need to pass the path of the new file as the 2nd arg.

  Required args:
  buffer [data path]
  seq    [data path frame-rate n-channels]"
  resolve-data-type)

(defmethod write-wav ::buffer
  [data path]
  (write-audio-file-from-seq (buffer-data data) path (:rate data) (:n-channels data)))

(defmethod write-wav ::sequence
  [data path frame-rate n-channels]
  (write-audio-file-from-seq (buffer-data data) path frame-rate n-channels))

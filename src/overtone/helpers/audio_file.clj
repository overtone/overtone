(ns
    ^{:doc "Audio file encoding functions"
      :author "Sam Aaron"}
  overtone.helpers.audio-file
  (:use [clojure.java.io :only [file]]
        [overtone.helpers.file :only [resolve-tilde-path]])
  (:import [javax.sound.sampled AudioFormat AudioFileFormat AudioFormat$Encoding
                                AudioFileFormat$Type AudioInputStream AudioSystem]
           [java.io ByteArrayInputStream]
           [java.nio ByteBuffer]))

(defn fill-data-buffer!
  [^ByteBuffer b-data data sample-bytes]
  (loop [idx 0
         data data]
    (when-not (empty? data)
      (let [b-idx (* sample-bytes idx)]
        (.putShort b-data b-idx (short (* (first data) Short/MAX_VALUE))))
      (recur (inc idx) (rest data))))

  b-data)

(defn write-audio-file-from-seq
  "Writes contents of data to a new wav file with path. Adds frame-rate and
  n-channels as file metadata for appropriate playback/consumption of the new
  audio file."
  [data path frame-rate n-channels]
  (when (some #(or (< % -1) (> % 1)) data)
    (throw (Exception. (str "Unable to write audio file with this data as it contains sample points either less than -1 or greater than 1."))))
  (let [path         (resolve-tilde-path path)
        frame-rate   (float frame-rate)
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
        f            (file path)
        f-type       AudioFileFormat$Type/WAVE]
    (AudioSystem/write stream f-type f)))

(ns overtone.osc.decode
  (:require
   [overtone.osc.util :refer :all])
  (:import
   (org.apache.commons.net.ntp TimeStamp)
   (java.nio Buffer ByteBuffer)))

(set! *warn-on-reflection* true)

(defn osc-align
  "Jump the current position to a 4 byte boundary for OSC compatible alignment."
  [^Buffer buf]
  (.position buf (bit-and (bit-not 3) (+ 3 (.position buf)))))

(defn- decode-string
  "Decode string from current pos in buf. OSC strings are terminated by a null
  char."
  [^ByteBuffer buf]
  (let [start (.position buf)]
    (while (not (zero? (.get buf))) nil)
    (let [end (.position buf)
          len (- end start)
          str-buf (byte-array len)]
      (.position buf start)
      (.get buf str-buf 0 len)
      (osc-align buf)
      (String. str-buf 0 (dec len)))))

(defn- decode-blob
  "Decode binary blob from current pos in buf. Size of blob is determined by the
  first int found in buffer."
  [^ByteBuffer buf]
  (let [size (.getInt buf)
        blob (byte-array size)]
    (.get buf blob 0 size)
    (osc-align buf)
    blob))

(defn- decode-msg
  "Pull data out of the message according to the type tag."
  [^ByteBuffer buf]
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
    (apply mk-osc-msg path type-tag args)))

(defn- decode-timetag
  "Decode OSC timetag from current pos in buf."
  [^ByteBuffer buf]
  (let [tag (.getLong buf)]
    (if (= tag OSC-TIMETAG-NOW)
      OSC-TIMETAG-NOW
      (TimeStamp/getTime tag))))

(defn- osc-bundle-buf?
  "Check whether there is an osc bundle at the current position in buf."
  [^ByteBuffer buf]
  (let [start-char (char (.get buf))]
    (.position buf (- (.position buf) 1))
    (= \# start-char)))

(declare osc-decode-packet)

(defn- decode-bundle-items
  "Pull out all the message packets within bundle from current buf position."
  [^ByteBuffer buf]
  (loop [items []]
    (if (.hasRemaining buf)
      (let [item-size (.getInt buf)
            original-limit (.limit buf)
            item (do (.limit buf (+ (.position buf) item-size)) (osc-decode-packet buf))]
        (.limit buf original-limit)
        (recur (conj items item)))
      items)))

(defn- decode-bundle
  "Decode a bundle - ignore the first string as it simply identifies the bundle."
  [buf]
  (decode-string buf) ; #bundle
  (mk-osc-bundle (decode-timetag buf) (decode-bundle-items buf)))

(defn osc-decode-packet
  "Decode an OSC packet buffer into a bundle or message map."
  [buf]
  (if (osc-bundle-buf? buf)
    (decode-bundle buf)
    (decode-msg buf)))

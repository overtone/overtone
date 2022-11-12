(ns overtone.osc.encode
  (:import [org.apache.commons.net.ntp TimeStamp])
  (:use [overtone.osc.util]))

(defn osc-pad
  "Add 0-3 null bytes to make buf position 32-bit aligned."
  [buf]
  (let [extra (mod (.position buf) 4)]
    (if (pos? extra)
      (.put buf PAD 0 (- 4 extra)))))

(defn encode-string
  "Encode string s into buf. Ensures buffer is correctly padded."
  [buf s]
  (.put buf (.getBytes s))
  (.put buf (byte 0))
  (osc-pad buf))

(defn encode-blob
  "Encode binary blob b into buf. Ensures buffer is correctly padded."
  [buf b]
  (.putInt buf (count b))
  (.put buf b)
  (osc-pad buf))

(defn encode-timetag
  "Encode timetag into buf. Timestamp defaults to (now) if not specifically
  passed. Throws exception if timestamp isn't a number."
  ([buf] (encode-timetag buf OSC-TIMETAG-NOW))
  ([buf timestamp]
   (when-not (number? timestamp)
     (throw (IllegalArgumentException. (str "OSC bundle timestamp needs to be a number. Got: " (type timestamp) " - " timestamp))))
   (if (= timestamp OSC-TIMETAG-NOW)
     (doto buf (.putInt 0) (.putInt 1))
     (let [ntp-timestamp (TimeStamp/getNtpTime (long timestamp))
           ntp-long      (.ntpValue ntp-timestamp)]
       (.putLong buf ntp-long)))))

(defn osc-encode-msg
  "Encode OSC message msg into buf."
  [buf msg]
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
      ))
  buf)

(declare osc-encode-packet)

(defn osc-encode-bundle
  "Encode bundle into buf."
  [buf bundle send-nested-osc-bundles?]
  (encode-string buf "#bundle")
  (encode-timetag buf (:timestamp bundle))
  (doseq [item (:items bundle)]
    ;; A bit of a hack...
    ;; Write an empty bundle element size into the buffer, then encode
    ;; the actual bundle element, and then go back and write the correct
    ;; size based on the new buffer position.
    (let [start-pos (.position buf)]
      (.putInt buf (int 0))
      (if (osc-msg? item)
        (osc-encode-msg buf item)
        (if send-nested-osc-bundles?
          (osc-encode-bundle buf item true)
          (throw (Exception. "Error - nesting OSC bundles has been disabled. This is functionality is typically disabled to ensure compatibility with some OSC servers (such as SuperCollider) that don't have support for nested OSC bundles"))))
      (let [end-pos (.position buf)]
        (.position buf start-pos)
        (.putInt buf (- end-pos start-pos 4))
        (.position buf end-pos))))
  buf)

(defn osc-encode-packet
  "Encode OSC packet into buf. Handles both OSC messages and bundles."
  [buf packet]
  (if (osc-msg? packet) (osc-encode-msg buf packet) (osc-encode-bundle buf packet true)))

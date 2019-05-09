(ns overtone.byte-spec
  (:import (java.net URL)
           (java.io FileInputStream FileOutputStream
                    DataInputStream DataOutputStream
                    BufferedInputStream BufferedOutputStream
                    ByteArrayOutputStream ByteArrayInputStream)))

;; This file implements a DSL for specifying the layout of binary data formats.
;; Look at synthdef.clj that defines the format for SuperCollider
;; synthesizer definition (.scsyndef) files for an example of usage.

(def ^{:dynamic true} *spec-out* nil)
(def ^{:dynamic true} *spec-in*  nil)

(defn- bytes-to-int [bytes]
  (-> bytes (ByteArrayInputStream.) (DataInputStream.) (.readInt)))

(defn- read-pstring []
  (let [len   (.readByte ^DataInputStream *spec-in*)
        bytes (byte-array len)]
    (.readFully ^DataInputStream *spec-in* bytes)
    (String. bytes)))

(defn- write-pstring [^java.lang.String s]
  (.writeByte ^DataOutputStream *spec-out* (count s))
  (.write ^DataOutputStream *spec-out* (.getBytes s)))

;; Standard numeric types + Pascal style strings.
;; pstring => a byte giving the string length followed by the ascii bytes
(def READERS {
              :int8   #(.readByte ^DataInputStream *spec-in*)
              :int16  #(.readShort ^DataInputStream *spec-in*)
              :int32  #(.readInt ^DataInputStream *spec-in*)
              :int64  #(.readLong ^DataInputStream *spec-in*)
              :float32  #(.readFloat ^DataInputStream *spec-in*)
              :float64 #(.readDouble ^DataInputStream *spec-in*)

              :byte   #(.readByte ^DataInputStream *spec-in*)
              :short  #(.readShort ^DataInputStream *spec-in*)
              :int    #(.readInt ^DataInputStream *spec-in*)
              :long   #(.readLong ^DataInputStream *spec-in*)
              :float    #(.readFloat ^DataInputStream *spec-in*)
              :double  #(.readDouble ^DataInputStream *spec-in*)

              :string read-pstring
              })

(def WRITERS {
              :int8   #(.writeByte ^DataOutputStream *spec-out* %1)
              :int16  #(.writeShort ^DataOutputStream *spec-out* %1)
              :int32  #(.writeInt ^DataOutputStream *spec-out*  %1)
              :int64  #(.writeLong ^DataOutputStream *spec-out*  %1)
              :float32 #(.writeFloat ^DataOutputStream *spec-out* %1)
              :float64 #(.writeDouble ^DataOutputStream *spec-out* %1)

              :byte   #(.writeByte ^DataOutputStream *spec-out* %1)
              :short  #(.writeShort ^DataOutputStream *spec-out* %1)
              :int    #(.writeInt ^DataOutputStream *spec-out*  %1)
              :long   #(.writeLong ^DataOutputStream *spec-out*  %1)
              :float   #(.writeFloat ^DataOutputStream *spec-out* %1)
              :double  #(.writeDouble ^DataOutputStream *spec-out* %1)

              :string write-pstring
              })

;; TODO: Make this complete
;; For now it just does enough to handle SuperCollider oddity
(defn- coerce-default [value ftype]
  (if (and (string? value) (= :int32 ftype))
    (bytes-to-int (.getBytes ^java.lang.String value))
    value))

(defn make-spec [spec-name field-specs]
  (loop [specs field-specs
         fields []]
    (if specs
      (let [fname (first specs)
            ftype (second specs)
            fdefault (if (and (> (count specs) 2)
                              (not (keyword? (nth specs 2))))
                       (nth specs 2)
                       nil)
            fdefault (coerce-default fdefault ftype)
            spec {:fname fname
                  :ftype ftype
                  :fdefault fdefault}
            specs (if (nil? fdefault)
                    (next (next specs))
                    (next (next (next specs))))
            fields (conj fields spec)]
                                        ;(println (str "field: " spec))
        (recur specs fields))
      {:name (str spec-name)
       :specs fields})))

;; A spec is just a hash-map containing a named vector of field specs
(defmacro defspec [spec-name & field-specs]
  `(def ~spec-name (make-spec ~(str spec-name) [~@field-specs])))

(defn spec [s & data]
  (let [field-names (map #(:fname %1) (:specs s))]
    (apply hash-map (interleave field-names data))))

(declare spec-read)

(defn- spec-read-array [spec size]
  (loop [i size
         ary []]
    (if (pos? i)
      (let [next-val (if (contains? READERS spec)
                       ((spec READERS))
                       (spec-read spec))]
        (recur (dec i) (conj ary next-val)))
      ary)))

(defn spec-read
  "Returns an instantiation of the provided spec, with data read from
  a DataInputStream bound to *spec-in*."
  [spec]
  (loop [specs (:specs spec)
         data  {}]
    (if specs
      (let [{:keys [fname ftype fdefault]} (first specs)
            fval (cond
                   ;; basic type
                   (contains? READERS ftype) ((ftype READERS))

                   ;; sub-spec
                   (map? ftype) (spec-read ftype)

                   ;; array
                   (vector? ftype) (spec-read-array (first ftype)
                                                    ((keyword (str "n-" (name fname))) data)))]
        (recur (next specs) (assoc data fname fval)))
      data)))

(defn spec-read-bytes [spec bytes]
  (binding [*spec-in* (-> bytes (ByteArrayInputStream.) (BufferedInputStream.) (DataInputStream.))]
    (spec-read spec)))

(defn spec-read-url [spec ^java.net.URL url]
  (with-open [ins (.openStream url)]
    (binding [*spec-in* (-> ins (BufferedInputStream.) (DataInputStream.))]
      (spec-read spec))))

(declare spec-write)

(defn- spec-write-array [spec ary]
                                        ;(println (str "WRITE-A: [ "
                                        ;     (if (map? spec) (:name spec) spec) " ](" (count ary) ")"))
                                        ;(println "ary: " ary)
  (let [nxt-writer (cond
                     (contains? WRITERS spec) (spec WRITERS)
                     (map? spec) (partial spec-write spec)
                     true (throw (IllegalArgumentException.
                                  (str "Invalid spec: " spec))))]
    (doseq [item ary]
      (nxt-writer item))))

(defn spec-write-basic [ftype fname fval fdefault]
  (if-let [val (or fval fdefault)]
    ((ftype WRITERS) val)
    (throw (Exception. (str "No value was given for '" fname "' field and it has no default.")))))

(defn count-for [fname]
  (keyword (.substring (name fname) 2)))

(defn spec-write
  "Serializes the data according to spec, writing bytes onto *spec-out*."
  [spec data]
  (doseq [{:keys [fname ftype fdefault]} (:specs spec)]
    (cond
                                        ; count of another field starting with n-
      (.startsWith (name fname) "n-")
      (let [wrt (ftype WRITERS)
            c-field (get data (count-for fname))
            cnt (count c-field)]
        (wrt cnt))

                                        ; an array of sub-specs
      (vector? ftype) (spec-write-array (first ftype) (fname data))

                                        ; a single sub-spec
      (map? ftype) (spec-write ftype (fname data))

                                        ; a basic type
      (contains? WRITERS ftype) (spec-write-basic ftype fname (fname data) fdefault)

      true (throw (IllegalArgumentException.
                   (str "Invalid field spec: " fname " " ftype))))))

(defn spec-write-file [spec data ^java.lang.String path]
  (with-open [spec-out (-> path (FileOutputStream.) (BufferedOutputStream.) (DataOutputStream.))]
    (binding [*spec-out* spec-out]
      (spec-write spec data))))

(defn spec-write-bytes [spec data]
  (let [bos (ByteArrayOutputStream.)]
    (with-open [out (DataOutputStream. bos)]
      (binding [*spec-out* out]
        (spec-write spec data)))
    (.toByteArray bos)))

(defn bytes-and-back [spec obj]
  (spec-read-bytes spec (spec-write-bytes spec obj)))

(ns overtone.bytes
  (:import (java.net URL)
     (java.io FileInputStream FileOutputStream 
              DataInputStream DataOutputStream
              BufferedInputStream BufferedOutputStream 
              ByteArrayOutputStream ByteArrayInputStream)))

(def *spec-out* nil)
(def *spec-in*  nil)

(defn- byte-array [len]
  (make-array (. Byte TYPE) len))

(defn- bytes-to-int [bytes]
  (-> bytes (ByteArrayInputStream.) (DataInputStream.) (.readInt)))

(defn- read-pstring []
  (let [len   (.readByte *spec-in*)
        bytes (byte-array len)]
    (.readFully *spec-in* bytes)
    (String. bytes)))

(defn- write-pstring [s]
  (.writeByte *spec-out* (count s))
  (.write *spec-out* (.getBytes s)))

;; The server uses a binary file format containing these types:
;; int32, int16, int8, float32
;; pstring => a byte giving the string length followed by the ascii bytes 
(def READERS {
              :int8   #(.readByte *spec-in*)
							:int16  #(.readShort *spec-in*)
							:int32  #(.readInt *spec-in*)
							:int64  #(.readLong *spec-in*)
							:float32  #(.readFloat *spec-in*)
              :float64 #(.readDouble *spec-in*)
							:string read-pstring
              })

(def WRITERS {
              :int8   #(.writeByte *spec-out* %1)
							:int16  #(.writeShort *spec-out* %1)
							:int32  #(.writeInt *spec-out*  %1)
							:int64  #(.writeLong *spec-out*  %1)
              :float32 #(.writeFloat *spec-out* %1)
              :float64 #(.writeDouble *spec-out* %1)
							:string write-pstring
              })

; TODO: Make this complete
; For now it just does enough to handle SuperCollider oddity
(defn- coerce-default [value ftype]
  (if (and (string? value) (= :int32 ftype))
    (bytes-to-int (.getBytes value))
    value))

(defn make-spec [spec-name field-specs]
  (loop [specs field-specs
         fields []]
    (if specs
      (let [fname (first specs)
            ftype (second specs)
            default (if (and (> (count specs) 2)
                             (not (keyword? (nth specs 2))))
                      (nth specs 2)
                      nil)
            default (coerce-default default ftype)
            spec {:fname fname 
                   :ftype ftype
                   :default default}
            specs (if (nil? default)
                         (next (next specs))
                         (next (next (next specs))))
            fields (conj fields spec)]
        (recur specs fields))
      {:name (str spec-name)
       :specs fields})))

;; A spec is just a hash-map containing a named vector of field specs
(defmacro defspec [spec-name & field-specs]
  (let [spec (make-spec spec-name field-specs)]
	`(def ~spec-name ~spec)))

(defn spec [s & data]
  (let [field-names (map #(:fname %1) (:specs s))]
        (apply hash-map (interleave field-names data))))

(declare spec-read)

(defn- spec-read-array [spec size]
;  (print "READ-A: ")
;  (if (map? spec)
;    (println (str (:name spec) "[" size "]"))
;    (println (str spec "[" size "]")))
  (loop [i size
         ary []]
    (if (pos? i)
      (let [next-val (if (contains? READERS spec)
                       ((spec READERS))
                       (spec-read spec))]
;        (println spec ":" next-val)
        (recur (dec i) (conj ary next-val)))
      ary)))

(defn spec-read 
  "Returns an instantiation of the provided spec, with data read from 
  a DataInputStream bound to *spec-in*."
  [spec]
  (loop [specs (:specs spec)
         data  {}]
    ;  (print "READ [" (:name spec) "]")
    (if specs
      (let [{:keys [fname ftype default]} (first specs)
;            _ (println (str fname ": " 
;                            (if (vector? ftype) (str "[]") ftype)
;                            default))
            fval (cond
                   ; basic type
                   (contains? READERS ftype) ((ftype READERS))

                   ; array
                   (vector? ftype) (spec-read-array (first ftype)
                                                    ((keyword (str "n-" (name fname))) data)))]
        (recur (next specs) (assoc data fname fval)))
      data)))

(defn spec-read-bytes [spec bytes]
	(binding [*spec-in* (-> bytes (ByteArrayInputStream.) (BufferedInputStream.) (DataInputStream.))]
   (spec-read spec)))

(defn spec-read-url [spec url]
  (with-open [ins (.openStream url)]
    (binding [*spec-in* (-> ins (BufferedInputStream.) (DataInputStream.))]
      (spec-read spec))))

(declare spec-write)

(defn- spec-write-array [spec ary]
;  (print "WRITE-A: ")
;  (if (map? spec)
;    (println (str (:name spec) "[" (count ary) "]"))
;    (println (str spec "[" (count ary) "]")))
  (let [writer (cond
                 (contains? WRITERS spec) (spec WRITERS)
                 (map? spec) (partial spec-write spec)
                 true (throw (IllegalArgumentException.
                               (str "Invalid spec: " spec))))]
    (doseq [item ary]
      (writer item))))

(defn spec-write-basic [ftype fname fval fdefault]
  (if-let [val (or fval fdefault)]
    ((ftype WRITERS) val)
    (throw (Exception. (str "No value was given for '" fname "' field and it has no default.")))))

(defn spec-write 
  "Serializes the data according to spec, writing bytes onto *spec-out*."
  [spec data]
  (doseq [{:keys [fname ftype fdefault]} (:specs spec)]
    (print "WRITE (" (:name spec) ") ")
    (println (str fname ": " 
                  (if (vector? ftype) (str "[]") ftype) ": "
                  (if (contains? WRITERS ftype) (or (fname data) fdefault))))
    (cond 
      ; count of another field starting with n-
      (.startsWith (name fname) "n-")
      ((ftype WRITERS) (count ((keyword (.substring (name fname) 2)) data)))

      ; an array of sub-specs
      (vector? ftype) (spec-write-array (first ftype) (fname data))

      ; a single sub-spec
      (map? ftype) (spec-write ftype (fname data))

      ; a basic type
      (contains? WRITERS ftype) (spec-write-basic ftype fname (fname data) fdefault))))

(defn spec-write-file [spec data path]
	(with-open [spec-out (-> path (FileOutputStream.) (BufferedOutputStream.) (DataOutputStream.))]
   (binding [*spec-out* spec-out]
     (spec-write spec data))))

(defn spec-write-bytes [spec data]
  (let [bos (ByteArrayOutputStream.)]
    (with-open [out (DataOutputStream. bos)]
      (binding [*spec-out* out]
        (spec-write spec data)))
    (.toByteArray bos)))


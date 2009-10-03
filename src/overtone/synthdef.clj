(ns overtone.synthdef
  (:import (java.io FileInputStream FileOutputStream 
                    DataInputStream DataOutputStream
                    BufferedInputStream BufferedOutputStream 
                    ByteArrayOutputStream ByteArrayInputStream)
     (java.net URL))
  (:use (overtone synth osc)))

;; SuperCollider Synthesizer Definition 

(def *spec-out* nil)
(def *spec-in*  nil)

(defn byte-array [len]
  (make-array (. Byte TYPE) len))

(defn bytes-to-int [bytes]
  (-> bytes (ByteArrayInputStream.) (DataInputStream.) (.readInt)))

(defn read-pstring []
  (let [len   (.readByte *spec-in*)
        bytes (byte-array len)]
    (.readFully *spec-in* bytes)
    (String. bytes)))

(defn write-pstring [s]
  (.writeByte *spec-out* (count s))
  (.write *spec-out* (.getBytes s)))

;; The server uses a binary file format containing these types:
;; int32, int16, int8, float32
;; pstring => a byte giving the string length followed by the ascii bytes 
(def READERS {
              :int8   #(.readByte *spec-in*)
							:int16  #(.readShort *spec-in*)
							:int32  #(.readInt *spec-in*)
							:float32  #(.readFloat *spec-in*)
							:string read-pstring
              })

(def WRITERS {
							:int8   #(.writeByte *spec-out* %1)
							:int16  #(.writeShort *spec-out* %1)
							:int32  #(.writeInt *spec-out*  %1)
              :float32 #(.writeFloat *spec-out* %1)
							:string write-pstring
              })

; TODO: Make this complete
; For now it just does enough to handle SuperCollider oddity
(defn coerce-default [value ftype]
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

(declare spec-read)

(defn spec-read-array [spec size]
  (print "READ-A: ")
  (if (map? spec)
    (println (str (:name spec) "[" size "]"))
    (println (str spec "[" size "]")))
  (loop [i size
         ary []]
    (if (pos? i)
      (let [next-val (if (contains? READERS spec)
                       ((spec READERS))
                       (spec-read spec))]
        (println spec ":" next-val)
        (recur (dec i) (conj ary next-val)))
      ary)))

(defn spec-read [spec]
  (loop [specs (:specs spec)
         data  {}]
  (print "READ [" (:name spec) "]")
    (if specs
      (let [{:keys [fname ftype default]} (first specs)
            _ (println (str fname ": " 
                            (if (vector? ftype) (str "[]") ftype)
                            default))
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

(defn spec-write-array [spec ary]
  (print "WRITE-A: ")
  (if (map? spec)
    (println (str (:name spec) "[" (count ary) "]"))
    (println (str spec "[" (count ary) "]")))
  (let [writer (cond
                 (contains? WRITERS spec) (spec WRITERS)
                 (map? spec) (partial spec-write spec)
                 true (throw (IllegalArgumentException.
                               (str "Invalid spec: " spec))))]
    (doseq [item ary]
      (writer item))))

(defn spec-write [spec data]
  (doseq [{:keys [fname ftype default]} (:specs spec)]
    (print "WRITE (" (:name spec) ") ")
    (println (str fname ": " 
                  (if (vector? ftype) (str "[]") ftype) ": "
                  (if (contains? WRITERS ftype) (or (fname data) default))))
    (cond 
      ; count of another field starting with n-
      (.startsWith (name fname) "n-")
      ((ftype WRITERS) (count ((keyword (.substring (name fname) 2)) data)))

      ; an array of sub-specs
      (vector? ftype) (spec-write-array (first ftype) (fname data))

      ; a single sub-spec
      (map? ftype) (spec-write ftype (fname data))

      ; a basic type
      (contains? WRITERS ftype) ((ftype WRITERS) (or (fname data) default)))))

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

;; param-name is :
;;   pstring - the name of the parameter
;;   int16 - its index in the parameter array
(defspec param-spec
         :name  :string
         :index :int16)

;; input-spec is :
;;   int16 - index of unit generator or -1 for a constant
;;   if (unit generator index == -1) {
;;     int16 - index of constant
;;   } else {
;;     int16 - index of unit generator output
;;   }
;; end
(defspec input-spec
				 :src   :int16
				 :index :int16)
 
;; an output-spec is :
;;   int8 - calculation rate
;; end
(defspec output-spec
				 :rate :int8)

;; ugen-spec is :
;;   pstring - the name of the SC unit generator class
;;   int8 - calculation rate
;;   int16 - number of inputs (I)
;;   int16 - number of outputs (O)
;;   int16 - special index
;;   [input-spec] * I
;;   [output-spec] * O
;;
;;  * Outputs have a specified calculation rate
;;    0 = scalar rate - one sample is computed at initialization time only. 
;;    1 = control rate - one sample is computed each control period.
;;    2 = audio rate - one sample is computed for each sample of audio output.
;;
;;  * special index - custom argument used by some ugens
;;    - (e.g. UnaryOpUGen and BinaryOpUGen use it to indicate which operator to perform.)
;;    - If not used it should be set to zero.
(defspec ugen-spec
				 :name      :string
				 :rate      :int8
				 :n-inputs  :int16
				 :n-outputs :int16
         :special   :int16
				 :inputs    [input-spec]
         :outputs   [output-spec])

;; synth-definition (sdef):
;;   pstring - the name of the synth definition
;;   
;;   int16 - number of constants (K)
;;   [float32] * K - constant values
;;   
;;   int16 - number of parameters (P)
;;   [float32] * P - initial parameter values
;;   
;;   int16 - number of parameter names (N)
;;   [param-name] * N
;;   
;;   int16 - number of unit generators (U)
;;   [ugen-spec] * U
;;
;;  * constants are static floating point inputs
;;  * parameters are named input floats that can be dynamically controlled 
;;    - (/s.new, /n.set, /n.setn, /n.fill, /n.map)
(defspec sdef-spec
         :name         :string
         :n-constants  :int16
         :constants    [:float32]
         :n-params     :int16
         :params       [:float32]
         :n-pnames     :int16
         :pnames       [param-spec]
         :n-ugens      :int16
         :ugens        [ugen-spec])
 
;; a synth-definition-file is :
;;   int32 - four byte file type id containing the ASCII characters: "SCgf"
;;   int32 - file version, currently zero.
;;   int16 - number of synth definitions in this file (D).
;;   [synth-definition] * D
;; end
(defspec synthdef-spec
         :id      :int32 "SCgf"
         :version :int32 0
         :n-sdefs :int16 0
         :sdefs   [sdef-spec])

(defn synthdef-write-file [sdef path]
  (spec-write-file synthdef-spec sdef path))

(defn synthdef-write-bytes [sdef]
  (spec-write-bytes synthdef-spec sdef))

(defn synthdef-read-bytes [bytes]
  (spec-read-bytes synthdef-spec bytes))

(defn synthdef-read-url [url]
  (spec-read-url synthdef-spec url))

;;  * Unit generators are listed in the order they will be executed.  
;;  * Inputs must refer to constants or previous unit generators.  
;;  * No feedback loops are allowed. Feedback must be accomplished via delay lines 
;;  or through buses. 
;;
;;  * There should be no duplicate values in the constants table.
;;
;;  For greatest efficiency:
;;
;;  * Unit generators should be listed in an order that permits efficient reuse
;;  of connection buffers, so use a depth first topological sort of the graph. 
;;
(defn synthdef [name constants params ugens]
  {:name name
   :n-constants (count constants)
   :constants (map #(float %1) constants)
   :n-pvals params})

(defn bundle [sdefs]
  {:spec synthdef-spec
   :n-sdefs (count sdefs)
   :sdefs sdefs})


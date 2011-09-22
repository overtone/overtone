(ns
  ^{:doc "This is primarily a specification for SuperCollider synthesizer
          definition files.  Additionally there are functions for reading and
          writing to and from byte arrays, files, and URLs.  "
     :author "Jeff Rose"}
  overtone.sc.machinery.synthdef
  (:import [java.net URL])
  (:use [overtone.byte-spec]
        [overtone.util lib]
        [overtone.libs event deps]
        [overtone.sc server])
  (:require [overtone.util.log :as log]))

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
;;  * special index - custom argument used by some ugens
;;    - (e.g. UnaryOpUGen and BinaryOpUGen use it to indicate which operator to perform.)
;;    - If not used it should be set to zero.
(defspec ugen-spec
				 :name      :string
				 :rate      :int8
				 :n-inputs  :int16
				 :n-outputs :int16
         :special   :int16 0
				 :inputs    [input-spec]
         :outputs   [output-spec])

;; variants are a mechanism to store a number of presets for a synthdef
;;   pstring - name of the variant
;;   [float32] - an array of preset values, one for each synthdef parameter
(defspec variant-spec
         :name   :string
         :params [:float32])

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
(defspec synth-spec
         :name         :string
         :n-constants  :int16
         :constants    [:float32]
         :n-params     :int16
         :params       [:float32]
         :n-pnames     :int16
         :pnames       [param-spec]
         :n-ugens      :int16
         :ugens        [ugen-spec]
         :n-variants   :int16 0
         :variants     [variant-spec])

;; a synth-definition-file is :
;;   int32 - four byte file type id containing the ASCII characters: "SCgf"
;;   int32 - file version, currently zero.
;;   int16 - number of synth definitions in this file (D).
;;   [synth-definition] * D
;; end

(def SCGF-MAGIC "SCgf")
(def SCGF-VERSION 1)

(defspec synthdef-file-spec
         :id       :int32 SCGF-MAGIC
         :version  :int32 SCGF-VERSION
         :n-synths :int16 1
         :synths   [synth-spec])

(defn- synthdef-file [& sdefs]
  (with-meta {:n-synths (short (count sdefs))
              :synths sdefs}
             {:type ::synthdef-file}))

(defn- synthdef-file? [obj] (= ::synthdef-file (type obj)))

(defn- synthdef-file-bytes [sfile]
  (spec-write-bytes synthdef-file-spec sfile))

(defn synthdef? [obj] (= ::synthdef (type obj)))

(defn- supercollider-synthdef-path
  "Returns a constructed path to a named synthdef on the current platform"
  [synth-name]
  (case (get-os)
        :mac   (str "/Users/" (user-name) "/Library/Application Support/SuperCollider/synthdefs/" synth-name ".scsyndef")))

; TODO: byte array shouldn't really be the default here, but I don't
; know how to test for one correctly... (byte-array? data) please?
(defn synthdef-read
  "Reads synthdef data from either a file specified using a string path
  a URL, or a byte array."
  [data]
  (first (:synths
          (cond
           (keyword? data) (spec-read-url synthdef-file-spec (java.net.URL. (str "file:" (supercollider-synthdef-path (to-str data)))) )
           (string? data) (spec-read-url synthdef-file-spec (java.net.URL. (str "file:" data)))
           (instance? java.net.URL data) (spec-read-url synthdef-file-spec data)
           (byte-array? data) (spec-read-bytes synthdef-file-spec data)
           :default (throw (IllegalArgumentException. (str "synthdef-read expects either a string, a URL, or a byte-array argument.")))))))

(defn synthdef-write
  "Write a synth definition to a new file at the given path, which includes
  the name of the file itself.  (e.g. /home/rosejn/synths/bass.scsyndef)"
  [sdef path]
  (spec-write-file synthdef-file-spec (synthdef-file sdef) path))

(defn synthdef-bytes
  "Produces a serialized representation of the synth definition understood
  by SuperCollider, and returns it in a byte array."
  [sdef]
  (spec-write-bytes synthdef-file-spec
    (cond
      (synthdef? sdef) (synthdef-file sdef)
      (synthdef-file? sdef) sdef)))

(defn- ugen-print [u]
  (println
    "--"
    "\n    name: "      (:name u)
    "\n    rate: "      (:rate u)
    "\n    n-inputs: "  (:n-inputs u)
    "\n    n-outputs: " (:n-outputs u)
    "\n    special: "   (:special u)
    "\n    inputs: "    (:inputs u)
    "\n    outputs: "   (:outputs u)))

(declare synthdef-print)
(defn- synthdef-file-print [s]
  (println
    "id: "         (:id s)
    "\nversion: "  (:version s)
    "\nn-synths: " (:n-synths s)
    "\nsynths:")
  (doseq [synth (:synths s)]
    (synthdef-print synth)))

(defn synthdef-print [s]
  (println
    "  name: "          (:name s)
    "\n  n-constants: " (:n-constants s)
    "\n  constants: "   (:constants s)
    "\n  n-params: "    (:n-params s)
    "\n  params: "      (:params s)
    "\n  n-pnames: "    (:n-pnames s)
    "\n  pnames: "      (:pnames s)
    "\n  n-ugens: "     (:n-ugens s))
  (doseq [ugen (:ugens s)]
    (ugen-print ugen)))

(defn synth-controls
  "Returns the set of control parameter name/default-value pairs for a synth
  definition."
  [sdef]
  (let [names (map #(keyword (:name %1)) (:pnames sdef))
        vals (:params sdef)]
  (apply hash-map (interleave names vals))))

(defonce loaded-synthdefs* (ref {}))

;; ### Synth Definition
;;
;; Synths are created from Synth Definitions. Synth Definition files are
;; created by Overtone and then loaded into the synth server using the synth
;; and inst forms and their derivatives.
(defn load-synthdef
  "Load an Overtone synth definition onto the audio server. The synthdef is also
  stored so that it can be re-loaded if the server needs rebooted. If the server
  is currently not running, the synthdef loading is delayed until the server has
  succesfully connected."
  [sdef]
  (assert (synthdef? sdef))
  (dosync (alter loaded-synthdefs* assoc (:name sdef) sdef))

  (when (connected?)
    (with-server-sync
      #(snd "/d_recv" (synthdef-bytes sdef)))))

(defn- load-all-synthdefs []
  (doseq [[sname sdef] @loaded-synthdefs*]
    (snd "/d_recv" (synthdef-bytes sdef))
    (satisfy-deps :synthdefs-loaded)))

(on-deps :connected ::load-all-synthdefs load-all-synthdefs)

(defn load-synth-file
  "Load a synth definition file onto the audio server."
  [path]
  (snd "/d_recv" (synthdef-bytes (synthdef-read path))))

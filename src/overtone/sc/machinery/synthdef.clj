(ns
  ^{:doc "This is primarily a specification for SuperCollider synthesizer
          definition files.  Additionally there are functions for reading and
          writing to and from byte arrays, files, and URLs.  "
     :author "Jeff Rose"}
  overtone.sc.machinery.synthdef
  (:import [java.net URL])
  (:use [overtone.byte-spec]
        [overtone.helpers lib]
        [overtone.libs event deps]
        [overtone.sc server]
        [overtone.sc.machinery.server comms]
        [overtone.sc.machinery.ugen common specs]
        [overtone.helpers.file :only [resolve-tilde-path]]
        [overtone.helpers.system :only [get-os]])
  (:require [overtone.config.log :as log]))

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
    :mac   (str (resolve-tilde-path "~/Library/Application Support/SuperCollider/synthdefs/")
                synth-name
                ".scsyndef")))

; TODO: byte array shouldn't really be the default here, but I don't
; know how to test for one correctly... (byte-array? data) please?
(defn synthdef-read
  "Reads synthdef data from either a file specified using a string path
  a URL, or a byte array."
  [data]
  (first (:synths
          (cond
           (keyword? data) (spec-read-url synthdef-file-spec (java.net.URL. (str "file:" (supercollider-synthdef-path (to-str data)))) )
           (string? data) (spec-read-url synthdef-file-spec (java.net.URL. (str "file:" (resolve-tilde-path data))))
           (instance? java.net.URL data) (spec-read-url synthdef-file-spec data)
           (byte-array? data) (spec-read-bytes synthdef-file-spec data)
           :default (throw (IllegalArgumentException. (str "synthdef-read expects either a string, a URL, or a byte-array argument.")))))))

(defn synthdef-write
  "Write a synth definition to a new file at the given path, which includes
  the name of the file itself.  (e.g. /home/rosejn/synths/bass.scsyndef)"
  [sdef path]
  (let [path (resolve-tilde-path path)]
    (spec-write-file synthdef-file-spec (synthdef-file sdef) path)))

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
  stored so that it can be re-loaded if the server gets rebooted. If the server
  is currently not running, the synthdef loading is delayed until the server has
  succesfully connected."
  [sdef]
  (assert (synthdef? sdef))
  (dosync (alter loaded-synthdefs* assoc (:name sdef) sdef))

  (when (server-connected?)
    (with-server-sync
      #(snd "/d_recv" (synthdef-bytes sdef)))))

(defn- load-all-synthdefs []
  (doseq [[sname sdef] @loaded-synthdefs*]
    (snd "/d_recv" (synthdef-bytes sdef)))
  (satisfy-deps :synthdefs-loaded))

(on-deps :server-ready ::load-all-synthdefs load-all-synthdefs)

(defn load-synth-file
  "Load a synth definition file onto the audio server."
  [path]
  (let [path (resolve-tilde-path path)]
    (snd "/d_recv" (synthdef-bytes (synthdef-read path)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Synthdef de-compilation
;;   The eventual goal is to be able to take any SuperCollider scsyndef
;;   file, and produce equivalent clojure code that can be re-edited.

(defn- param-vector [params pnames]
  "Create a synthdef parameter vector."
  (vec (flatten
         (map #(list (symbol (:name %1))
                     (nth params (:index %1)))
              pnames))))

(defn- ugen-form
  "Create a ugen form."
  [ug]
  (let [uname (real-ugen-name ug)
        ugen  (get-ugen uname)
        uname (if (and
                   (zero? (:special ug))
                   (not= (:rate-name ug) (:default-rate ugen)))
                (str uname (:rate-name ug))
                uname)
        uname (symbol uname)]
    (apply list uname (:inputs ug))))

(defn- ugen-constant-inputs
  "Replace constant ugen inputs with the constant values."
  [constants ug]
  (assoc ug :inputs
         (map
           (fn [{:keys [src index] :as input}]
             (if (= src -1)
               (nth constants index)
               input))
           (:inputs ug))))

(defn- reverse-ugen-inputs
  "Replace ugen inputs that are other ugens with their generated
  symbolic name."
  [pnames ugens ug]
  (assoc ug :inputs
         (map
           (fn [{:keys [src index] :as input}]
             (if src
               (let [u-in (nth ugens src)]
                 (if (= "Control" (:name u-in))
                   (nth pnames index)
                   (:sname (nth ugens src))))
               input))
           (:inputs ug))))

; In order to do this correctly is a big project because you also have to
; reverse the process of the various ugen modes.  For example, you need
; to recognize the ugens that have array arguments which will be
; appended, and then you need to gather up the inputs and place them into
; an array at the correct argument location.
(defn synthdef-decompile
  "Decompile a parsed SuperCollider synth definition back into clojure
  code that could be used to generate an identical synth.

  While this probably won't create a synth definition that can
  directly compile, it can still be helpful when trying to reverse
  engineer a synth."
  [{:keys [name constants params pnames ugens] :as sdef}]
  (let [sname (symbol name)
        param-vec (param-vector params pnames)
        ugens (map #(assoc % :sname %2)
                   ugens
                   (map (comp symbol #(str "ug-" %) char) (range 97 200)))
        ugens (map (partial ugen-constant-inputs constants) ugens)
        pnames (map (comp symbol :name) pnames)
        ugens (map (partial reverse-ugen-inputs pnames ugens) ugens)
        ugens (filter #(not= "Control" (:name %)) ugens)
        ugen-forms (map vector
                     (map :sname ugens)
                     (map ugen-form ugens))]
      (print (format "(defsynth %s %s\n  (let [" sname param-vec))
      (println (ffirst ugen-forms) (second (first ugen-forms)))
      (doseq [[uname uform] (drop 1 ugen-forms)]
        (println "       " uname uform))
      (println (str "       ]\n   " (first (last ugen-forms)) ")"))))

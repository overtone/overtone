(ns overtone.synthdef
  (:import (java.net URL))
  (:require [overtone.log :as log])
  (:use
     (overtone utils ugens ops bytes)
     (clojure walk inspector)
     clojure.contrib.seq-utils))

;; SuperCollider Synthesizer Definition 

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
 
;; Outputs have a specified calculation rate
;;   0 = scalar rate - one sample is computed at initialization time only. 
;;   1 = control rate - one sample is computed each control period.
;;   2 = audio rate - one sample is computed for each sample of audio output.
(def RATES {:scalar  0
           :control 1
           :audio   2})

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
         :name :string
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
             {:type :synthdef-file}))

(defn- synthdef-file? [obj] (= :synthdef-file (type obj)))

(defn- synthdef-file-bytes [sfile]
  (spec-write-bytes synthdef-file-spec sfile))

(declare synthdef?)

; TODO: byte array shouldn't really be the default here, but I don't 
; know how to test for one correctly... (byte-array? data) please?
(defn synthdef-read 
  "Reads synthdef data from either a file specified using a string path,
  a URL, or a byte array."
  [data]
  (first (:synths 
    (cond 
      (string? data) 
      (spec-read-url synthdef-file-spec (java.net.URL. (str "file:" data)))
      (instance? java.net.URL data)
      (spec-read-url synthdef-file-spec data)
      (byte-array? data) (spec-read-bytes synthdef-file-spec data)
      :default (throw (IllegalArgumentException. (str "synthdef-read expects either a string, a URL, or a byte-array argument.")))))))

(defn synthdef-write 
  "Write a synth definition to a new file at the given path, which includes
  the name of the file itself.  (e.g. /home/rosejn/synths/bass.scsyndef)"
  [path sdef]
  (spec-write-file synthdef-file-spec (synthdef-file sdef) path))

(defn synthdef-bytes 
  "Produces a serialized representation of the synth definition understood
  by SuperCollider, and returns it in a byte array."
  [sdef]
  (spec-write-bytes synthdef-file-spec 
    (cond
      (synthdef? sdef) (synthdef-file sdef)
      (synthdef-file? sdef) sdef)))

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

(defn- normalize-ugen-name [n]
  (.replaceAll (.toLowerCase (str n)) "[-|_]" ""))

(def UGEN-MAP (reduce 
                (fn [mem ugen] 
                  (assoc mem (normalize-ugen-name (:name ugen)) ugen)) 
                UGENS))

(defn ugen-name [word]
  (:name (get UGEN-MAP (normalize-ugen-name word))))

(defn find-ugen [word]
  (get UGEN-MAP (normalize-ugen-name word)))

(defn ugen-search [regexp]
  (filter (fn [[k v]] (re-find (re-pattern regexp) (str k))) UGEN-MAP))

(defn ugen [name rate special & args]
  (let [info (ugen-name name)]
    (if (nil? info)
      (throw (IllegalArgumentException. (str "Unknown ugen: " name))))
    (with-meta {:id (uuid)
                :name name
                :rate (rate RATES)
                :special special
                :args args}
               {:type :ugen})))

(defn ugen? [obj] (= :ugen (type obj)))

;; -----------------------------------------------------------------

;; TODO: Figure out the complete list of control types
;; This is used to determine the controls we list in the synthdef, so we need
;; all ugens which should be specified as external controls.
(def CONTROLS #{"control"})

(defn- control-ugen [rate n-outputs]
  (with-meta {:id (uuid)
              :name "Control"
              :rate (rate RATES)
              :special 0
              :args nil
              :n-outputs n-outputs
              :outputs (repeat n-outputs {:rate (rate RATES)})
              :n-inputs 0
              :inputs []}
            {:type :ugen}))

(defn- control? [ugen]
  (and (map? ugen)
       (CONTROLS (normalize-ugen-name (:name ugen)))))

(def *ugens* nil)
(def *constants* nil)
(def *params* nil)

(defn- index-of [col item]
  (first (first (filter (fn [[i v]] 
                          (= v item)) 
                        (indexed col)))))

(defn- ugen-index [ugens ugen]
  (first (first (filter (fn [[i v]] 
                          (= (:id v) (:id ugen))) 
                        (indexed ugens)))))

; TODO: Ugh...  There must be a nicer way to do this.  I need to get the group
; number (source) and param index within the group (index) from a grouped
; parameter structure like this (2x2 in this example):
;
;[[{:name :freq :value 440.0 :rate  1} {:name :amp :value 0.4 :rate  1}]
; [{:name :adfs :value 20.23 :rate  2} {:name :bar :value 8.6 :rate  2}]]
(defn- param-input-spec [grouped-params param-name]
  (let [ctl-filter (fn [[idx ctl]] (= param-name (:name ctl)))
        [[src group] foo] (take 1 (filter 
                              (fn [[src grp]] 
                                (not (empty? 
                                       (filter ctl-filter (indexed grp)))))
                              (indexed grouped-params)))
        ;_ (println "src: " src "\ngrp: " group)
        [[idx param] bar] (take 1 (filter ctl-filter (indexed group)))]
    (if (or (nil? src) (nil? idx))
      (throw (IllegalArgumentException. (str "Invalid parameter name: " param-name ". Please make sure you have named all parameters in the param map in order to use them inside the synth definition."))))
    {:src src :index idx}))

; TODO: Figure out when we would have to connect to a different output index
; it probably has to do with multi-channel expansion...
(defn- with-inputs 
  "Returns ugen object with its input ports connected to constants and upstream 
  ugens according to the arguments in the initial definition."
  [ugen ugens constants grouped-params]
  (let [inputs (map (fn [arg]
                      (cond
                        ; constant
                        (number? arg) {:src -1 :index (index-of constants arg)}
                        
                        ; control
                        (keyword? arg) (param-input-spec grouped-params arg)

                        ; child ugen
                        (ugen? arg)   {:src (ugen-index ugens arg) :index 0}))
                    (:args ugen))]
    (assoc ugen :inputs inputs)))

(defn- with-outputs 
  "Returns a ugen with its output port connections setup according to the spec."
  [ugen]
  ; Don't modify controls or anything that comes with pre-setup outputs.
  (if (contains? ugen :outputs)
    ugen
    (let [spec (find-ugen (:name ugen))
          out-type (:out-type spec)
          num-outs (cond
                     (= out-type :fixed)    (:fixed-outs spec)
                     (= out-type :variable) (:fixed-outs spec)
                     (= out-type :array)    (:fixed-outs spec))]
;            (println "\n\ncur-outputs: " ugen num-outs (take num-outs (repeat {:rate (:rate ugen)})) "\n")
            (assoc ugen :outputs (take num-outs (repeat {:rate (:rate ugen)}))))))

(defn- detail-ugens 
  "Fill in all the input and output specs for each ugen."
  [ugens constants grouped-params]
  (map (fn [ugen]
         (-> ugen 
            (with-inputs ugens constants grouped-params)
            (with-outputs)))
       ugens))

(defn- collect-ugen-helper [ugen]
  (let [children (filter #(ugen? %1) (:args ugen))
        constants (filter #(number? %1) (:args ugen))]

    ; We want depth first (topological) ordering of ugens, so dig down to the
    ; child ugens before adding the current ugen to the list.
    (doseq [child children]
      (collect-ugen-helper child))
    
    ; Each constant value should appear only once in the set of constants
    (doseq [const constants]
      (if (not ((set *constants*) const))
        (set! *constants* (conj *constants* const))))

    ; Add the current ugen to the list
    (set! *ugens* (conj *ugens* ugen))))

(defn- collect-ugen-info 
  "Return a list of all the ugens in the ugen graph in topological, depth first order.  
  SuperCollider wants the ugens listed in the order they should be executed."
  [ugen] 
  (binding [*ugens*     []
            *constants* []]
    (collect-ugen-helper ugen)
    [*ugens* *constants*]))

(defn- make-control-ugens
  "Controls are grouped by rate, so that a single Control ugen represents
  each rate present in the params.  The Control ugens are always the top nodes
  in the graph, so they can be prepended to the topologically sorted tree."
  [grouped-params]
  (map #(control-ugen (:rate (first %1)) (count %1)) grouped-params))

(defn- group-params 
  "Groups params by rate.  Groups a list of parameters into a
   list of lists, one per rate."
  [params]
  (let [by-rate (reduce (fn [mem param] 
                          (let [rate (:rate param)
                                rate-group (get mem rate [])]
                            (assoc mem rate (conj rate-group param))))
                        {} params)]
    (filter #(not (nil? %1))
            (conj [] (:scalar by-rate) (:control by-rate) (:audio by-rate)))))

(def DEFAULT-RATE :control)

(defn- parse-params [params]
  (for [[p-name p-val] params] 
    (let [[p-val p-rate] (if (vector? p-val)
                           p-val
                           [p-val DEFAULT-RATE])]
      {:name  p-name
       :value (float p-val)
       :rate  p-rate})))

(defn- make-params 
  "Create the param value vector and parameter name vector."
  [grouped-params]
  (let [param-list (flatten grouped-params)
        pvals  (map #(:value %1) param-list)
        pnames (map (fn [[idx param]] 
                      {:name (as-str (:name param))
                       :index idx})
                    (indexed param-list))]
    [pvals pnames]))

(defn- to-ugen-name [word]
  (ugen-name (.substring word 0 (- (count word) 3))))

(defn- replace-name [l]
  (let [word (str (first l))]
    (concat 
      (cond 
        (.endsWith word ".ar") ['ugen (to-ugen-name word) :audio 0]
        (.endsWith word ".kr") ['ugen (to-ugen-name word) :control 0]
        (.endsWith word ".dr") ['ugen (to-ugen-name word) :scalar 0]
        :default [(symbol word)]) 
      (rest l))))

(defn- replace-ugens
  "Find all the forms starting with a valid ugen identifier, and convert it to a function argument to
  a ugen constructor."
  [form]
  (postwalk (fn [x] (if (and (seq? x) 
                             (symbol? (first x)))
                      (replace-name x) 
                      x)) 
            form))

(defn- ugen-form? [form]
  (and (seq? form)
       (= 'ugen (first form))))

(defn- unary-op? [form]
  (and (seq? form)
       (= 2 (count form))
       (contains? UNARY-OPS (str (first form)))
       (or (ugen-form? (second form))
           (keyword? (second form)))))

(defn- binary-op? [form]
  (and (seq? form)
       (= 3 (count form))
       (contains? BINARY-OPS (str (first form)))
       (or (ugen-form? (second form))
           (ugen-form? (nth form 2)))))

(defn- replace-unary [form]
  (concat 
    ['ugen  "UnaryOpUGen" (nth (fnext form) 2) (get UNARY-OPS (str (first form)))]
    (next form)))

(defn- replace-binary [form]
  (log/debug "replace-binary: " form)
  (let [ugen-arg (first (take 1 (filter #(ugen-form? %1) form)))
        _ (log/debug "ugen-arg:" ugen-arg)
        rate (nth ugen-arg 2)]
    (concat
      ['ugen  "BinaryOpUGen" rate (get BINARY-OPS (str (first form)))]
      (next form))))

(defn- replace-arithmetic
  "Replace all arithmetic operations on ugens with unary and binary ugen operators."
  [form]
  (postwalk (fn [x] (cond
                      (unary-op? x) (replace-unary x)
                      (binary-op? x) (replace-binary x)
                      :default x))
            form))

(defn- to-ugen-tree [form]
  (let [t1 (replace-ugens form)
        t2 (replace-arithmetic t1)]
    t2))

; TODO: Either add support for multi-channel expansion, or get rid of the idea
; and do it in a more lispy way.
            
; NOTES:
; * The ugen tree is turned into a ugen list that is sorted by the order in 
; which nodes should be processed.  (Depth first, starting at outermost leaf 
; of the first branch.
; * params are sorted by rate, and then a Control ugen per rate is created
; and prepended to the ugen list
; * finally, ugen inputs are specified using their index  
; in the sorted ugen list.
(defn build-synthdef
  [name params top-ugen]
  (let [[ugens constants] (collect-ugen-info top-ugen) 
        grouped-params (group-params (parse-params params))
        [params pnames] (make-params grouped-params)
        with-ctl-ugens (concat (make-control-ugens grouped-params) ugens)
        detailed (detail-ugens with-ctl-ugens constants grouped-params)]
    {:name name
     :constants constants
     :params params
     :pnames pnames
     :ugens detailed}))

(defmacro synth
  "Transforms a synth definition (ugen-tree) into a form that's ready to save 
  to disk or send to the server."
  [name params & body]
  (let [ugen-tree (to-ugen-tree body)]
    `(with-meta (build-synthdef ~(str name) ~params ~@ugen-tree)
            {:type :synthdef
             :src-code '~body})))

(defmacro defsynth [synth-name params & body]
  `(def ~synth-name (synth ~synth-name ~params ~@body))) 

(defn synthdef? [obj] (= :synthdef (type obj)))

(defn- ugen-print [u]
  (println
    "--"
    "\n    name: " (:name u)
    "\n    rate: " (:rate u)
    "\n    n-inputs: " (:n-inputs u)
    "\n    n-outputs: " (:n-outputs u)
    "\n    special: " (:special u)
    "\n    inputs: " (:inputs u)
    "\n    outputs: " (:outputs u)))

(declare synthdef-print)
(defn- synthdef-file-print [s]
  (println
    "id: " (:id s)
    "\nversion: " (:version s)
    "\nn-synths: " (:n-synths s)
    "\nsynths:")
  (doseq [synth (:synths s)] 
    (synthdef-print synth)))

(defn synthdef-print [s]
  (println
    "  name: " (:name s)
    "\n  n-constants: " (:n-constants s)
    "\n  constants: " (:constants s)
    "\n  n-params: " (:n-params s) 
    "\n  params: " (:params s)
    "\n  n-pnames: " (:n-pnames s) 
    "\n  pnames: " (:pnames s)
    "\n  n-ugens: " (:n-ugens s))
  (doseq [ugen (:ugens s)]
    (ugen-print ugen)))

(defn synth-controls 
  "Returns the set of control parameter name/default-value pairs for a synth definition."
  [sdef]
  (let [names (map #(keyword (:name %1)) (:pnames sdef))
        vals (:params sdef)]
  (apply hash-map (interleave names vals))))


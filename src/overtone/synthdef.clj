(ns overtone.synthdef
  (:import (java.net URL))
  (:use
     (overtone utils ugens bytes)
     (clojure walk inspector)
     clojure.contrib.seq-utils
     clojure.contrib.logging))

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

(defspec synthdef-spec
         :id       :int32 SCGF-MAGIC
         :version  :int32 SCGF-VERSION
         :n-synths :int16 1
         :synths   [synth-spec])

(defn synthdef-file [& sdefs]
  {:type :synthdef-file
   :n-synths (short (count sdefs))
   :synths sdefs})

(def synthdef-file? (type-checker :synthdef-file))

(defn synthdef-file-bytes [sfile]
  (spec-write-bytes synthdef-spec sfile))

(defn synthdef-bytes [sdef]
  (spec-write-bytes synthdef-spec 
    (cond
      (synthdef? sdef) (synthdef-file sdef)
      (synthdef-file? sdef) sdef)))

(defn synthdef-write-file [path sfile]
  (spec-write-file synthdef-spec sfile path))

(defn synthdef-read-bytes [bytes]
  (spec-read-bytes synthdef-spec bytes))

; TODO: Either figure out how to do it with duck-streams, or patch
; duck-streams so it will work.
(defn synthdef-read-url [url]
  (spec-read-url synthdef-spec url))

(defn synthdef-read-file [path]
  (spec-read-url synthdef-spec (java.net.URL. (str "file:" path))))

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

(defn normalize-ugen-name [n]
  (.replaceAll (.toLowerCase (str n)) "[-|_]" ""))

(def UGEN-MAP (reduce 
                (fn [mem ugen] 
                  (assoc mem (normalize-ugen-name (:name ugen)) ugen)) 
                UGENS))

(defn ugen-name [word]
  (:name (get UGEN-MAP (normalize-ugen-name word))))

(defn lookup-ugen [word]
  (get UGEN-MAP (normalize-ugen-name word)))

(defn ugen [name rate & args]
  (let [info (ugen-name name)]
    (if (nil? info)
      (throw (IllegalArgumentException. (str "Unknown ugen: " name))))
    (with-meta {:id (uuid)
                :name name
                :rate (rate RATES)
                :args args}
               {:type :ugen})))

(defn ugen? [obj] (= :ugen (type obj)))

;; TODO: Figure out the complete list of control types
;; This is used to determine the controls we list in the synthdef, so we need
;; all ugens which should be specified as external controls.
(def CONTROLS #{"control"})

(defn control? [ugen]
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

; TODO: Figure out when we would have to connect to a different output index
; it probably has to do with multi-channel expansion...
(defn with-inputs 
  "Returns ugen object with its input ports connected to constants and upstream 
  ugens according to the arguments in the initial definition."
  [ugen ugens constants]
  (let [inputs (map (fn [arg]
                      (cond
                        (number? arg) {:src -1 :index (index-of constants arg)}
                        (ugen? arg)   {:src (ugen-index ugens arg) :index 0}))
                    (:args ugen))]
    ;(println "ugens: " ugens)
    ;(println "ugen: " (:name ugen) " with-inputs: " inputs)
    (assoc ugen :inputs inputs)))

(def FIXED 0)
(def ARG   1)
(def ARY   2)
 
(defn with-outputs 
  "Returns a ugen with its output port connections setup according to the spec."
  [ugen]
  (let [spec (lookup-ugen (:name ugen))
        out-type (:out-type spec)
        num-outs (cond
                   (= out-type FIXED) (:fixed-outs spec)
                   (= out-type ARG)   (:fixed-outs spec)
                   (= out-type ARY)   (:fixed-outs spec))]
    (assoc ugen :outputs (take num-outs (repeat {:rate (:rate ugen)})))))

(defn with-params 
  ""
  [ugen] ugen)

(defn detail-ugens 
  "Fill in all the input and output specs for each ugen."
  [ugens constants params]
  (let [ins  (map #(with-inputs %1 ugens constants) ugens)
        outs (map #(with-outputs %1) ins)
        params (map #(with-params %1) outs)]
    params))

(defn- collect-ugen-helper [ugen]
  (let [children (filter #(ugen? %1) (:args ugen))
        constants (filter #(number? %1) (:args ugen))]
    (doseq [child children]
      (collect-ugen-helper child))
    (doseq [const constants]
      (if (not ((set *constants*) const))
        (set! *constants* (conj *constants* const))))
    (set! *ugens* (conj *ugens* ugen))))

(defn- collect-ugen-info 
  "Return a list of all the ugens in the ugen graph in topological, depth first order.  
  SuperCollider wants the ugens listed in the order they should be executed."
  [ugen] 
  (binding [*ugens*     []
            *constants* []
            *params*    []]
    (collect-ugen-helper ugen)
    [*ugens* *constants*]))

(def DEFAULT-RATE :control)

(defn- parse-params [params]
  (for [[p-name p-val] params] 
    (let [[p-val p-rate] (if (vector? p-val)
                           p-val
                           [p-val DEFAULT-RATE])]
      {:name (as-str p-name)
       :value (float p-val)
       :rate p-rate})))

; TODO: Figure out if we ever have multiple top-level ugens, or at least
; an array for multiple channels...
(defn synthdef 
  "Transforms a synth definition graph into a form that's ready to save to disk or 
  send to the server."
  [name params top-ugen]
  (let [[ugens constants] (collect-ugen-info top-ugen)
        params (sort #(compare (:rate %1) (:rate %2)) (parse-params params))
        pnames (map #(:name %1) params)
        detailed (detail-ugens ugens constants params)]
    {:type :synthdef
     :name name
     :constants constants
     :params params
     :pnames pnames
     :ugens (doall detailed)}))

(def synthdef? (type-checker :synthdef))

(defn to-ugen-name [word]
  (ugen-name (.substring word 0 (- (count word) 3))))

(defn replace-name [l]
  (let [word (str (first l))]
    (concat 
      (cond 
        (.endsWith word ".ar") ['ugen (to-ugen-name word) :audio]
        (.endsWith word ".kr") ['ugen (to-ugen-name word) :control]
        (.endsWith word ".dr") ['ugen (to-ugen-name word) :scalar]
        :default [(symbol word)]) 
      (rest l))))

(defn replace-ugens
  "Find all the forms starting with a valid ugen identifier, and convert it to a function argument to
  a ugen constructor."
  [form]
  (postwalk (fn [x] (if (and (seq? x) 
                             (symbol? (first x)))
                      (replace-name x) 
                      x)) 
            form))

(defmacro defsynth [name params & body]
  (let [renamed (replace-ugens body)]
    `(def ~(symbol (str name)) (with-meta (synthdef ~(str name) ~params ~@renamed)
                                          {:src '~body}))))

;(dosync (alter *synths assoc ~(str name) sdef#))

(defmacro syn [& body]
  (first (replace-ugens body)))

(defn ugen-print [u]
  (println
    "--"
    "\n    name: " (:name u)
    "\n    rate: " (:rate u)
    "\n    n-inputs: " (:n-inputs u)
    "\n    n-outputs: " (:n-outputs u)
    "\n    special: " (:special u)
    "\n    inputs: " (:inputs u)
    "\n    outputs: " (:outputs u)))

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

(defn synthdef-file-print [s]
  (println
    "id: " (:id s)
    "\nversion: " (:version s)
    "\nn-synths: " (:n-synths s)
    "\nsynths:")
  (doseq [synth (:synths s)] 
    (synthdef-print synth)))



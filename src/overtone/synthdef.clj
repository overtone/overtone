(ns overtone.synthdef
  (:import (java.net URL))
  (:use
     (overtone utils ugens bytes)
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

(def SCGF-MAGIC "SCgf")
(def SCGF-VERSION 1)

(defspec synthdef-spec
         :id      :int32 SCGF-MAGIC
         :version :int32 SCGF-VERSION
         :n-sdefs :int16 0
         :sdefs   [sdef-spec])

(defn synthdef-write-file [sdef path]
  (spec-write-file synthdef-spec sdef path))

(defn synthdef-write-bytes [sdef]
  (spec-write-bytes synthdef-spec sdef))

(defn synthdef-read-bytes [bytes]
  (spec-read-bytes synthdef-spec bytes))

; TODO: Either figure out how to do it with duck-streams, or patch
; duck-streams so it will work.
(defn synthdef-read-url [url]
  (spec-read-url synthdef-spec url))

(defn synthdef-read-file [url]
  (spec-read-url synthdef-spec (java.net.URL. (str "file:" url))))

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

(defn ugen-match [word]
  (get UGEN-MAP (normalize-ugen-name word)))

(defn ugen [name rate & args]
  (let [info (ugen-match name)]
    (if (nil? info)
      (throw (IllegalArgumentException. (str "Unknown ugen: " name))))
    {:type :ugen
    :name name
    :rate (rate RATES)
    :args args}))

(def ugen? (type-checker :ugen))

;; TODO: Figure out the complete list of control types
;; This is used to determine the controls we list in the synthdef, so we need
;; all ugens which should be specified as external controls.
(def CONTROLS #{"control"})

(defn control? [ugen]
  (and (map? ugen)
       (CONTROLS (normalize-ugen-name (:name ugen)))))

(def *synthdef* nil)

;(defn memory [init-val] 
;  (let [m (atom init-val)] 
;    {:next #(swap! c inc)
;    :reset #(reset! c init-val)}))

(def *ugens* nil)
(def *constants* nil)
(def *params* nil)
(defn do-col-ugens [ugen]
  (let [children (filter #(ugen? %1) (:args ugen))
        constants (filter #(number? %1) (:args ugen))
        params    (filter #(control? %1) (:args ugen))]
    (doseq [child children]
      (do-col-ugens child))
    (doseq [const constants]
      (if (not ((set *constants*) const))
        (set! *constants* (conj *constants* const))))
    (doseq [param params]
      (if (not ((set *params*) param))
        (set! *params* (conj *params* param))))
    (set! *ugens* (conj *ugens* ugen))))

(defn collect-ugen-info 
  "Return a list of all the ugens in the ugen graph."
  [ugen] 
  (binding [*ugens*     []
            *constants* []
            *params*    []]
    (do-col-ugens ugen)
    [*ugens* *constants* *params*]))

(defn index-of [col item]
  (first (first (filter (fn [[i v]] 
                          (= v item)) 
                        (indexed col)))))

(defn with-inputs [ugen ugens constants]
  (let [inputs (map (fn [arg]
                      (cond
                        (number? arg) {:src -1 :index (index-of constants arg)}
                        (ugen? arg)   {:src (index-of ugens ugen) :index 0}))
                    (:args ugen))]
    (assoc ugen :inputs inputs)))

(def FIXED 0)
(def ARG   1)
(def ARY   2)
 
; TODO: This seems nonsensical...  Need a better way to determine output information.
(defn with-outputs [ugen]
  (let [spec (ugen-match (:name ugen))
        out-type (:out-type spec)
        num-outs (cond
                   (= out-type FIXED) (:fixed-outs spec)
                   (= out-type ARG)   (:fixed-outs spec)
                   (= out-type ARY)   (:fixed-outs spec))]
    ;(println "with-outs (" (:name ugen) "): " out-type " - " num-outs)
    (assoc ugen :outputs (take num-outs (repeat {:rate (:rate ugen)})))))

(defn detail-ugens 
  "Fill in all the input and output details necessary for each ugen."
  [ugens constants params]
  (let [ins  (map #(with-inputs %1 ugens constants) ugens)
        outs (map #(with-outputs %1) ins)]
    outs))

(defn realize-synthdef 
  "Transform a constructed synth definition into a form that's ready to serialize
  and send to the server."
  [base-def]
  (let [[ugens constants params] (collect-ugen-info (:top base-def))
        pnames    (map #(:name %1) params)
        detailed  (detail-ugens ugens constants params)]
    (merge base-def
           {:constants constants
            :params params
            :pnames pnames
            :ugens (doall detailed)})))

; TODO: Figure out if we ever have multiple top-level ugens, or at least
; an array for multiple channels...
(defn synthdef [name top-ugen]
  (let [base-def {:type :synthdef
                 :name name
                 :top top-ugen}]
    (realize-synthdef base-def)))

(def synthdef? (type-checker :synthdef))

(defn synthdef-file [& sdefs]
  {:spec synthdef-spec
   :n-sdefs (count sdefs)
   :sdefs sdefs})

;; NOTES for synthdef processing
;; * Synth definition defines the nodes and each of their input values
;; * Process definition by iterating through tree and converting to a 
;; list of ugens.
;;
;; * find controls in ugen list and store info for ctl-header
;; * find constants in ugen list and store info
;; * sort list topologically
(defn trim [word]
  (.substring word 0 (- (count word) 3)))

(defn replace-name [l]
  (let [word (str (first l))]
    (concat 
      (cond 
        (.endsWith word ".ar") ['ugen (trim word) :audio]
        (.endsWith word ".kr") ['ugen (trim word) :control]
        (.endsWith word ".dr") ['ugen (trim word) :scalar]
        true [(symbol word)]) 
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

(defmacro defsynth [name & body]
  (let [renamed (replace-ugens body)]
    `(def ~(symbol (str name)) (synthdef ~(str name) ~@renamed))))

;(dosync (alter *synths assoc ~(str name) sdef#))

(defmacro syn [& body]
  (first (replace-ugens body)))


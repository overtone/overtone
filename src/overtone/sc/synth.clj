(ns
  ^{:doc "The ugen functions create a data structure representing a synthesizer
          graph that can be executed on the synthesis server.  This is the logic
          to \"compile\" these clojure data structures into a form that can be
          serialized by the byte-spec defined in synthdef.clj."
    :author "Jeff Rose"}
  overtone.sc.synth
  (:require [overtone.log :as log]
            [clojure.contrib.generic.arithmetic :as ga])
  (:use
     [overtone util event]
     [overtone.sc core ugen synthdef]
     [clojure walk inspector]
     [clojure.contrib.seq-utils :only (indexed)]))
;;TODO replace this with clojure.core/keep-indexed or map-indexed))

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
(defn- param-input-spec [grouped-params param-proxy]
  (let [param-name (:name param-proxy)
        ctl-filter (fn [[idx ctl]] (= param-name (:name ctl)))
        [[src group] foo] (take 1 (filter
                              (fn [[src grp]]
                                (not (empty?
                                       (filter ctl-filter (indexed grp)))))
                              (indexed grouped-params)))
        [[idx param] bar] (take 1 (filter ctl-filter (indexed group)))]
    (if (or (nil? src) (nil? idx))
      (throw (IllegalArgumentException. (str "Invalid parameter name: " param-name ". Please make sure you have named all parameters in the param map in order to use them inside the synth definition."))))
    {:src src :index idx}))

(defn- inputs-from-outputs [src src-ugen]
  (for [i (range (count (:outputs src-ugen)))]
    {:src src :index i}))

; NOTES:
; * *All* inputs must refer to either a constant or the output of another
;   UGen that is higher up in the list.
(defn- with-inputs
  "Returns ugen object with its input ports connected to constants and upstream
  ugens according to the arguments in the initial definition."
  [ugen ugens constants grouped-params]
  {:pre [
         ;(contains? ugen :args)
         (every? #(or (ugen? %) (number? %)) (:args ugen))]
   :post [(contains? % :inputs)
          (every? (fn [in] (not (nil? in))) (:inputs %))]}
  ;(println "with-inputs: " ugen)
  ;(println "arg types: " (map type (:args ugen)))
  (let [inputs (flatten
                 (map (fn [arg]
                        (cond
                          ; constant
                          (number? arg)
                          {:src -1 :index (index-of constants arg)}

                          ; control
                          (control-proxy? arg)
                          (param-input-spec grouped-params arg)

                          ; child ugen
                          (ugen? arg)
                          (let [idx (ugen-index ugens arg)
                                updated-ugen (nth ugens idx)]
                            (inputs-from-outputs idx updated-ugen))))
                      (:args ugen)))]
    (assoc ugen :inputs inputs)))

; TODO: Currently the output rate is hard coded to be the same as the
; computation rate of the ugen.  We probably need to have some meta-data
; capabilities for supporting varying output rates...
(defn- with-outputs
  "Returns a ugen with its output port connections setup according to the spec."
  [ugen]
  {:post [(every? (fn [val] (not (nil? val))) (:outputs %))]}
  (if (contains? ugen :outputs)
    ugen
    (let [spec (get-ugen (:name ugen))
          num-outs (or (:n-outputs ugen) 1)
          outputs (take num-outs (repeat {:rate (:rate ugen)}))]
      (assoc ugen :outputs outputs))))

; IMPORTANT NOTE: We need to add outputs before inputs, so that multi-channel
; outputs can be correctly connected.
(defn- detail-ugens
  "Fill in all the input and output specs for each ugen."
  [ugens constants grouped-params]
  (let [outs  (map with-outputs ugens)
        ins   (map #(with-inputs %1 outs constants grouped-params) outs)
        final (map #(assoc %1 :args nil) ins)]
    (doall final)))

(defn- collect-ugen-helper [ugen]
  (let [children (filter #(and (ugen? %1) (not (control-proxy? %1)))
                         (:args ugen))
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

; * There should be no duplicate values in the constants table.
(defn- collect-ugen-info
  "Return a list of all the ugens in the ugen graph in topological, depth first order.
  SuperCollider wants the ugens listed in the order they should be executed."
  [& ugens]
  (binding [*ugens*     []
            *constants* []]
    (doseq [ugen (flatten ugens)]
      (collect-ugen-helper ugen))
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
            (conj [] (:ir by-rate) (:kr by-rate) (:ar by-rate)))))

(def DEFAULT-RATE :kr)

; TODO: Figure out a good way to specify rates for synth parameters
; currently this is the syntax:
; (defsynth foo [freq 440] ...)
; (defsynth foo [freq [440 :ar]] ...)
; Should probably do this with a recursive function that can pull
; out argument pairs or triples, and get rid of the vector
(defn- parse-params [params]
  (for [[p-name p-val] (partition 2 params)]
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

(defn- ugen-form? [form]
  (and (seq? form)
       (= 'ugen (first form))))

(defn- fastest-rate [rates]
  (REVERSE-RATES (first (reverse (sort (map RATES rates))))))

(defn- special-op-args? [args]
  (some #(or (ugen? %1) (keyword? %1)) args))

(defn- find-rate [args]
  (fastest-rate (map #(cond
                        (ugen? %1) (REVERSE-RATES (:rate %1))
                        (keyword? %1) :kr)
                     args)))

;;  For greatest efficiency:
;;
;;  * Unit generators should be listed in an order that permits efficient reuse
;;  of connection buffers, so use a depth first topological sort of the graph.

; NOTES:
; * The ugen tree is turned into a ugen list that is sorted by the order in
; which nodes should be processed.  (Depth first, starting at outermost leaf
; of the first branch.
;
; * params are sorted by rate, and then a Control ugen per rate is created
; and prepended to the ugen list
;
; * finally, ugen inputs are specified using their index
; in the sorted ugen list.
;
;  * No feedback loops are allowed. Feedback must be accomplished via delay lines
;  or through buses.
;
(defn synthdef
  "Transforms a synth definition (ugen-graph) into a form that's ready to save
  to disk or send to the server.
  "
  [sname params top-ugen]
  ;(println "sname: " sname "\nparams: " params "\ntop-ugen: " top-ugen)
  (let [[ugens constants] (collect-ugen-info top-ugen)
        parsed-params (parse-params params)
        ;_ (println "parsed-params: " parsed-params)
        grouped-params (group-params parsed-params)
        ;_ (println "grouped-params: " grouped-params)
        [params pnames] (make-params grouped-params)
        ;_ (println "params: " params " pnames: " pnames)
        ;_ (println "ugens: " ugens)
        with-ctl-ugens (concat (make-control-ugens grouped-params) ugens)
        ;_ (println "with-ctl-ugens: " with-ctl-ugens)
        detailed (detail-ugens with-ctl-ugens constants grouped-params)]
    (with-meta {:name (str sname)
                :constants constants
                :params params
                :pnames pnames
                :ugens detailed}
               {:type :overtone.sc.synthdef/synthdef})))

; TODO: This should eventually handle optional rate specifiers, and possibly
; be extended with support for defining ranges of values, etc...
(defn- control-proxies [params]
  (reduce (fn [mem [pname pval]]
            (concat mem [(symbol pname) `(control-proxy ~pname ~pval)]))
          []
          (partition 2 params)))

; TODO: Figure out how to generate the let-bindings rather than having them
; hard coded here.
(defmacro pre-synth [& args]
  (let [[sname args] (cond
                       (or (string? (first args))
                           (symbol? (first args))) [(str (first args)) (rest args)]
                       :default                    [:no-name args])
        [params ugen-form] (if (vector? (first args))
                             [(first args) (rest args)]
                             [[] args])
        params (vec (map #(if (symbol? %) (str %) %) params))
        param-proxies (control-proxies params)]
    `(let [~@param-proxies
           sname# (if (= :no-name ~sname)
                    (str "anon-" (next-id :anonymous-synth))
                    ~sname)]
       (with-ugens
         [sname# ~params ~@ugen-form]))))

(defmacro synth
  "Define a SuperCollider synthesizer using the library of ugen functions
  provided by overtone.sc.ugen.  This will return an anonymous function which
  can be used to trigger the synthesizer.  If the synth has an audio rate ugen
  that is not an 'Out' ugen, then a prefix will be appended.  By default: (out
  (pan2 synth)).  You can change it by passing a function to
  (set-synth-prefix my-prefix-fn), which will then be called with a synth ugen
  tree as it's single argument.

  (synth (sin-osc (+ 300 (* 10 (sin-osc:kr 10)))))
  "
  [& args]
  `(let [[sname# params# ugens#] (pre-synth ~@args)
         sdef# (synthdef sname# params# ugens#)
         player# (synth-player sname# (map first (partition 2 params#)))
         smap# (callable-map {:name sname#
                              :ugens ugens#
                              :sdef sdef#
                              :doc "User defined synth..."
                              :player player#}
                             player#)]
     (load-synthdef sdef#)
     (event :new-synth :synth smap#)
     smap#))

(defn synth-form
  "Internal function used to prepare synth meta-data."
  [name sdecl]
  (let [md (if (string? (first sdecl))
             {:doc (first sdecl)}
             {})
        params    (first (take 1 (filter vector? sdecl)))
        arglists (list (vec (map first (partition 2 params))))
        md (assoc md
                  :name name
                  :arglists (list 'quote arglists))
        ugen-form (first (take 1 (filter list? sdecl)))]
    [md params ugen-form]))

(defmacro defsynth
  "Define a synthesizer and return a player function.  The synth definition
  will be loaded immediately, and a :new-synth event will be emitted.

  (defsynth foo [freq 440]
    (sin-osc freq))

  is equivalent to:

  (def foo
    (synth [freq 440] (sin-osc freq)))

  A doc string can also be included:
  (defsynth bar
    \"The phatest space pad ever!\"
    [] (...))
  "
  [name & sdecl]
  (let [[md params ugen-form] (synth-form name sdecl)]
;    (println "metadata: " md)
    (list 'def (with-meta name md)
          (list 'synth name params ugen-form))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Synthdef de-compilation
;;   The eventual goal is to be able to take any SuperCollider scsyndef
;;   file, and produce equivalent clojure code that can be re-edited.

(defn- decomp-params [s]
  (let [pvals (:params s)]
    (apply hash-map (flatten
      (map #(list (keyword (:name %1))
                  (nth pvals (:index %1)))
            (:pnames s))))))

;TODO: Finish this...  It will be really helpful for people who want to explore
; synths and effects, no matter which SC client they were generated with.
(comment defn synthdef-decompile
  "Decompile a parsed SuperCollider synth definition back into clojure code
  that could be used to generate an identical synth."
  [synth]
  (let [params decomp-params]
    params))


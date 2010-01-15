(ns overtone.core.synth
  "This file has two primary functions.  One is to define a language for
  creating SuperCollider synthesizer definitions in Clojure.  The second
  is to take these definitions and convert them into a correctly structured
  synthdef data structure that can be serialized to a SuperCollider compatible
  file."
  (:require [overtone.lib.log :as log])
  (:use
     (overtone.core util ops ugen sc synthdef)
     (clojure walk inspector)
     clojure.contrib.seq-utils))

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
        ;_ (println "src: " src "\ngrp: " group)
        [[idx param] bar] (take 1 (filter ctl-filter (indexed group)))]
    (if (or (nil? src) (nil? idx))
      (throw (IllegalArgumentException. (str "Invalid parameter name: " param-name ". Please make sure you have named all parameters in the param map in order to use them inside the synth definition."))))
    {:src src :index idx}))

(defn- inputs-from-outputs [src src-ugen]
  ;(println "inputs-from-outputs: " (:name src-ugen))
  (for [i (range (count (:outputs src-ugen)))]
    {:src src :index i}))

; NOTES: 
; * *All* inputs must refer to either a constant or the output of another
;   UGen that is higher up in the list. 
(defn- with-inputs 
  "Returns ugen object with its input ports connected to constants and upstream 
  ugens according to the arguments in the initial definition."
  [ugen ugens constants grouped-params]
  ;(println "with-inputs: " (:name ugen))
  (let [inputs (flatten 
                 (map (fn [arg]
                        (cond
                          ; constant
                          (number? arg) 
                          {:src -1 :index (index-of constants arg)}

                          ; control
                          (= ::control-proxy (type arg)) 
                          (param-input-spec grouped-params arg)

                          ; child ugen
                          (ugen? arg) 
                          (let [idx (ugen-index ugens arg)
                                updated-ugen (nth ugens idx)]
                            (inputs-from-outputs idx updated-ugen))))
                      (:args ugen)))]
    (assoc ugen :inputs inputs)))

; If the arity of the function call creating the ugen is greater than the
; expected position of the 'numChannels' argument, then we use the value
; from the arguments.  Otherwise it is expected that it was
; left out intentionally, so we use a default value.
(defn- num-channels-from-arg [ugen spec]
  (let [[idx arg-info] (first (filter (fn [[idx arg]] 
                                        (= "numChannels" (:name arg)))
                                      (indexed (:args spec))))
        default-num (:default arg-info)]
    (if (> (count (:args ugen)) idx)
      (nth (:args ugen) idx)
      default-num)))

(defn- with-outputs 
  "Returns a ugen with its output port connections setup according to the spec."
  [ugen]
  ; Don't modify controls or anything that comes with pre-setup outputs.
  (if (contains? ugen :outputs)
    ugen
    (let [spec (get-ugen (:name ugen))
          out-type (:out-type spec)
          num-outs (cond
                     (= out-type :fixed)    (:fixed-outs spec)
                     (= out-type :variable) (:fixed-outs spec)
                     (= out-type :from-arg) (num-channels-from-arg ugen spec))
          outputs (take num-outs (repeat {:rate (:rate ugen)}))]
            (assoc ugen :outputs outputs))))

; IMPORTANT NOTE: We need to add outputs before inputs, so that multi-channel
; outputs can be correctly connected.
(defn- detail-ugens 
  "Fill in all the input and output specs for each ugen."
  [ugens constants grouped-params]
  (let [outs (doall (map with-outputs ugens))
        ins (doall (map #(with-inputs %1 outs constants grouped-params) outs))
        final (map #(dissoc %1 :args) ins)]
    final))

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

; * There should be no duplicate values in the constants table.
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
                        (keyword? %1) :control) 
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
  (let [[ugens constants] (collect-ugen-info top-ugen) 
        ;_ (println "input-params: " params)
        parsed-params (parse-params params)
        ;_ (println "parsed-params: " parsed-params)
        grouped-params (group-params parsed-params)
        ;_ (println "grouped-params: " grouped-params)
        [params pnames] (make-params grouped-params)
        with-ctl-ugens (concat (make-control-ugens grouped-params) ugens)
        detailed (detail-ugens with-ctl-ugens constants grouped-params)]
    (with-meta {:name (str sname)
                :constants constants
                :params params
                :pnames pnames
                :ugens detailed}
               {:type :overtone.synthdef/synthdef})))

(comment defn synth
  [ugen]
  (let [sdef (if (= "Out" (:name ugen))
                 (synthdef ugen)
                 (synthdef (overtone.ugens/out 0 (overtone.ugens/pan2 ugen))))
        sname (:name sdef)]
    (load-synthdef sdef)
    (fn [& args] (apply hit sname args))))

(defn control-proxy [name]
  (with-meta {:name (str name)}
             {:type ::control-proxy}))

; TODO: This should eventually handle optional rate specifiers, and possibly
; be extended with support for defining ranges of values, etc...
(defn- parse-synth-params [params]
  (flatten (map (fn [[param default]] 
                  [param (control-proxy param)])
                (apply hash-map params))))

(defn- name-synth-args [args names]
  (loop [args args
         names names
         named []]
    (if args
      (recur (next args)
             (next names)
             (concat named [(first names) (first args)]))
      named)))

(def synth-groups* (ref {}))

(defn synth-player [sname arg-names]
  (let [sgroup (get @synth-groups* sname)
        controller (partial node-control sgroup)
        player (partial node sname :target sgroup)]
    (fn [& args] 
      (let [[tgt-fn args] (if (= :ctl (first args))
                            [controller (rest args)]
                            [player args])
            named-args (if (keyword? (first args))
                         args
                         (name-synth-args args arg-names))]
        ;(println (concat [sname] named-args))
        (apply tgt-fn named-args)))))

(defmacro synth [& args]
  (let [[sname args] (cond
                       (or (string? (first args))
                           (symbol? (first args))) [(str (first args)) (rest args)]
                       (keyword? (first args))     [(name (first args)) (rest args)]
                       :default                    [:no-name args])
        [params ugen-form] (if (vector? (first args))
                             [(first args) (rest args)]
                             [[] args])
        param-names (map #(keyword (first %)) (partition 2 params))
        param-proxies (parse-synth-params params)
        param-map (apply hash-map (map #(if (symbol? %) (str %) %) params))]
    `(do
       (let [~@param-proxies
             ugen-root# ~@ugen-form
             sname# (if (= :no-name ~sname)
                      (str "anon-" (next-id :anonymous-synth))
                      ~sname)
             ugens# (if (and (ugen? ugen-root#)
                             (= "Out" (:name ugen-root#)))
                      ugen-root#
                      (overtone.ugens/out 0 (overtone.ugens/pan2 ugen-root#)))
             sdef# (synthdef sname# ~param-map ugens#)]
         (load-synthdef sdef#)
         (dosync (alter synth-groups* assoc sname# (group :tail 0)))
         (synth-player sname# (quote ~param-names))))))

(defmacro defsynth [name params & ugen-form]
  `(def ~name (synth ~name
                     ~params
                     ~@ugen-form)))

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
(defn synthdef-decompile 
  "Decompile a parsed SuperCollider synth definition back into clojure code
  that could be used to generate an identical synth."
  [synth]
  (let [params decomp-params]
    params))


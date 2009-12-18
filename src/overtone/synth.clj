(ns overtone.synth
  "This file has two primary functions.  One is to define a language for
  creating SuperCollider synthesizer definitions in Clojure.  The second
  is to take these definitions and convert them into a correctly structured
  synthdef data structure that can be serialized to a SuperCollider compatible
  file."
  (:require [overtone.log :as log])
  (:use
     (overtone util ops sc synthdef)
     (clojure walk inspector)
     clojure.contrib.seq-utils))

(defn- normalize-name [n]
  (.replaceAll (.toLowerCase (str n)) "[-|_]" ""))

;; Done actions are typically executed when an envelope ends, or a sample ends
;; 0	do nothing when the UGen is finished
;; 1	pause the enclosing synth, but do not free it
;; 2	free the enclosing synth
;; 3	free both this synth and the preceding node
;; 4	free both this synth and the following node
;; 5	free this synth; if the preceding node is a group then do g_freeAll on it, else free it
;; 6	free this synth; if the following node is a group then do g_freeAll on it, else free it
;; 7	free this synth and all preceding nodes in this group
;; 8	free this synth and all following nodes in this group
;; 9	free this synth and pause the preceding node
;; 10	free this synth and pause the following node
;; 11	free this synth and if the preceding node is a group then do g_deepFree on it, else free it
;; 12	free this synth and if the following node is a group then do g_deepFree on it, else free it
;; 13	free this synth and all other nodes in this group (before and after)
;; 14	free the enclosing group and all nodes within it (including this synth)
(def DONE-ACTIONS  
  {:done-nothing 0	
   :done-pause 1	
   :done-free 2	
   :done-free-and-before 3	
   :done-free-and-after 4
   :done-free-and-group-before 5	
   :done-free-and-group-after 6
   :done-free-upto-this 7	
   :done-free-from-this-on 8	
   :done-free-pause-before 9
   :done-free-pause-after 10
   :done-free-and-group-before-deep 11
   :done-free-and-group-after-deep 12	
   :done-free-children 13	
   :done-free-group 14})

(def UGENS (read-string (slurp "src/overtone/ugen.clj")))

(def UGEN-MAP (reduce (fn [mem ugen] 
                        (assoc mem (normalize-name (:name ugen)) ugen)) 
                      UGENS))

(defn find-ugen [word]
  (get UGEN-MAP (normalize-name word)))

(defn ugen-name [word]
  (:name (find-ugen word)))

(defn ugen-search [regexp]
  (filter (fn [[k v]] (re-find (re-pattern regexp) (str k))) UGEN-MAP))

(defn ugen-print [& ugens]
  (doseq [[name ugen] ugens]
    (println "ugen:" (str "\"" (:name ugen) "\"")
             "\nargs: " (map #(:name %1) (:args ugen))
             "\nrates: " (seq (:rates ugen))
             "\nouts: " (:fixed-outs ugen) (str "(" (:out-type ugen) ")")
             "\n- - - - - - - - - -\n")))

(defn ugen-doc [word]
  (apply ugen-print (ugen-search word)))

(defn- add-default-args [spec args]
  (let [defaults (map #(:default %1) (:args spec))]
    ;(println (:name spec) " defaults: " defaults "\nargs: " args)
    (cond 
      ; Many ugens (e.g. EnvGen) have an array of values as their last argument,
      ; so when the last arg is a coll? we insert missing defaults between the passed
      ; args and the array.
      (and (< (count args) (count defaults))
           (and (coll? (last args)) (:array? (last (:args spec)))))
      (concat (drop-last args) (drop (count args) (drop-last defaults)) (last args))

      ; Replace regular missing args as long as they are all valid numbers
      (and (< (count args) (count defaults)) 
           (not-any? #(= Float/NaN %1) args))
      (concat args (drop (count args) defaults))

      ; Otherwise we just missed something
      (< (count args) (count defaults))
      (throw (IllegalArgumentException. 
        (str "Missing arguments to ugen: " (:name spec) " => "  
             (doall (drop (count args) (map #(%1 :name) (:args spec)))))))

      :default args)))

(defn- replace-action-args [args]
  (map #(get DONE-ACTIONS %1 %1) args))

; Since envelopes are used so often with done actions we make them
; as easy as possible.  In effect this lets you put the first and last
; argument only.
; TODO: Look into replacing this garbage with some kind of keyword argument system  
(defn- envelope-args [spec args]
  (replace-action-args 
    (if (= "EnvGen" (:name spec))
      (let [env-ary (first args)
            args (next args) 
            defaults (drop-last (map #(%1 :default) (:args spec)))
            with-defs (cond 
                        (and (< (count args) (count defaults))
                             (contains? DONE-ACTIONS (last args)))
                        (concat (drop-last args) 
                                (drop (dec (count args)) (drop-last defaults)) 
                                [(last args)])

                        (< (count args) (count defaults))
                        (concat args (drop (count args) defaults))

                        :default args)]
        (flatten (concat with-defs [env-ary])))
      args)))

(defn ugen [name rate special & args]
  ;(println "ugen: " name rate special)
  (let [spec (if-let [ug (find-ugen name)]
               ug
               (throw (IllegalArgumentException. (str "Unknown ugen: " name))))
        args (envelope-args spec args)
        args (add-default-args spec args)]
    (with-meta {:id (next-id :ugen)
                :name (:name spec)
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
  (with-meta {:id (next-id :ugen)
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
       (CONTROLS (normalize-name (:name ugen)))))

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
                          (number? arg) {:src -1 :index (index-of constants arg)}

                          ; control
                          (keyword? arg) (param-input-spec grouped-params arg)

                          ; child ugen
                          (ugen? arg) (let [idx (ugen-index ugens arg)
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
    (let [spec (find-ugen (:name ugen))
          out-type (:out-type spec)
          num-outs (cond
                     (= out-type :fixed)    (:fixed-outs spec)
                     (= out-type :variable) (:fixed-outs spec)
                     (= out-type :from-arg) (num-channels-from-arg ugen spec))
          outputs (take num-outs (repeat {:rate (:rate ugen)}))]
      ;(println "\noutputs: " (:name ugen) num-outs outputs)
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

(defn- to-ugen-name [word]
  (ugen-name (.substring word 0 (- (count word) 3))))

(defn- replace-name [l]
  (let [word (str (first l))]
    (concat 
      (cond 
        (.endsWith word ".ar") ['ugen (to-ugen-name word) :audio 0]
        (.endsWith word ".kr") ['ugen (to-ugen-name word) :control 0]
        (.endsWith word ".ir") ['ugen (to-ugen-name word) :scalar 0]
        (.endsWith word ".dr") ['ugen (to-ugen-name word) :demand 0]
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

(defn- fastest-rate [rates]
  (REVERSE-RATES (first (reverse (sort (map RATES rates))))))

(defn- special-op-args? [args]
  (some #(or (ugen? %1) (keyword? %1)) args))

(defn- find-rate [args]
  (fastest-rate (map #(cond
                        (ugen? %1) (REVERSE-RATES (:rate %1))
                        (keyword? %1) :control) 
                     args)))

(defn op-check 
  "Checks the types of the arguments after their evaluation to determine whether
  this should be a DSP ugen operator or just a function call to evaluate
  immediately."
  [op arity op-num & args]
  (if (special-op-args? args)
    (do ;(println "op-check: ugen -> " op op-num args)
    (apply ugen arity (find-rate args) op-num args))
    
    (do ;(println "op-check: operator -> " op args)
    (eval (apply list (symbol op) args)))))

(defn unary-op-num [name]
  (get UNARY-OPS (str name) false))

(defn binary-op-num [name]
  (get BINARY-OPS (str name) false))

(defn- expand-ops [form]
  (let [op (first form)
        op-prefix (list 'op-check (str op) "BinaryOpUGen" (binary-op-num op))]
    (loop [args (reverse (next form))
           i 0]
      (if (or (= i 5) (= 1 (count args)))
        (first args)
        (recur (cons (concat op-prefix [(second args) (first args)]) (drop 2 args)) (inc i))))))

(defn- replace-basic-ops
  "Replace all basic operations on ugens with unary and binary ugen operators."
  [form]
  (postwalk (fn [x] 
              (if (seq? x)
                (cond
                  (and (unary-op-num (first x)) (= 2 (count x))) 
                  (list 'op-check (str (first x)) "UnaryOpUGen" (unary-op-num (first x)) (second x))

                  (and (binary-op-num (first x)) (= 3 (count x)))
                  (list 'op-check (str (first x)) "BinaryOpUGen" (binary-op-num (first x)) (second x) (nth x 2))

                  ; Expand addition and multiplication of more than 2 elements so we can do things in a lispy way, 
                  ; even when operating on UGens.  (e.g. (* snd env 0.2) => (* snd (* env 0.2)))
                  (and (or (= '+ (first x)) (= '* (first x))))
                  (expand-ops x)

                  :default x)
                x))
            form))

(defn- to-ugen-tree [form]
  (let [t1 (replace-ugens form)
        t2 (replace-basic-ops t1)]
    t2))

; TODO: Either add support for multi-channel expansion, or get rid of the idea
; and do it in a more lispy way.
            
;;
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
(defn build-synthdef
  [name params top-ugen]
  (let [[ugens constants] (collect-ugen-info top-ugen) 
        grouped-params (group-params (parse-params params))
        [params pnames] (make-params grouped-params)
        with-ctl-ugens (concat (make-control-ugens grouped-params) ugens)
        detailed (detail-ugens with-ctl-ugens constants grouped-params)]
    {:name (str name)
     :constants constants
     :params params
     :pnames pnames
     :ugens detailed}))

; TODO: Need to abstract a bit from synth, and put auto-wrapping only here.
;(defmacro check
;  "Evaluates the synth definition, sends it to the server, and runs it immediately 
;  for 1 second."
;  [& args]
;  (let [
;        wrapped (if (= 'out.ar (first body))
;                  body
;                  `(out.ar 0 (pan2.ar ~body 0)))

(defmacro synth
  "Transforms a synth definition (ugen-tree) into a form that's ready to save 
  to disk or send to the server.
  
  Stereo output instruments are the default.  If the root ugen is not out.ar, 
  then the synth is wrapped in pan2.ar and out.ar.  To create mono synths or
  effects make sure to define your own out.ar.

  Synth definitions with no controllable parameters:
      (synth (saw.ar 100))                 ; An anonymous synth
      (synth :foo (saw.ar 200))
      (synth :foo (out.ar 0 (pan2.ar (saw.ar 200)))) 
      (synth \"foo\" {} (out.ar 0 (pan2.ar (saw.ar 200)))) 

  A map of parameter-name to default-value is an optional second argument, so
  these are the same:

      (synth :foo {:freq 200 :amp 0.4} 
        (out.ar 0 (pan2.ar (* :amp 
                              (saw.ar :freq)))))

  "
  [& args]
  (let [[sname args] (cond 
                       (string? (first args)) [(first args) (next args)]
                       (symbol? (first args)) [(str (first args)) (next args)]
                       (keyword? (first args)) [(name (first args)) (next args)]
                       :default [(str "anon-" (next-id :anonymous-synth)) args])
        [params body] (if (map? (first args))
                        [(first args) (second args)]
                        [{} (first args)])
        kname (keyword sname)
        ugen-tree (to-ugen-tree body)]
    `(with-meta (build-synthdef ~sname ~params ~ugen-tree)
                         {:type :synthdef
                          :src-code '~body})))

(defn with-stereo-out 
  "Appends pan2 and out ugens to the synthdef to make it output in stereo."
  [sdef]
  (if (= "Out" (:name (last (:ugens sdef))))
    sdef
    (let [constants (concat (:constants sdef) [1.0 0])
          zero-idx (dec (count constants))
          one-idx (- (count constants) 2)
          ugens (:ugens sdef)
          last-idx (dec (count ugens))
          pan {:inputs [{:src last-idx :index 0} {:src -1 :index zero-idx} {:src -1 :index one-idx}], :outputs [{:rate 2} {:rate 2}], :id (next-id :ugen), :name "Pan2", :rate 2, :special 0}
          pan-idx (count (:ugens sdef))
          out {:inputs [{:src -1, :index zero-idx} {:src pan-idx, :index 0} {:src pan-idx, :index 1}], :outputs [], :id (next-id :ugen), :name "Out", :rate 2, :special 0}]
      (assoc sdef 
             :constants constants
             :ugens (concat ugens [pan out])))))

(defmacro play
  "Define an anonymous synth, load it and play it immediately."
  [body]
  `(let [sdef# (with-stereo-out (synth ~body))]
     (load-synth sdef#)
     (hit (:name sdef#))))

(defmacro syn
  "Useful for making synth definition helpers..."
  [body]
  (let [b (to-ugen-tree body)]
    `(do ~b)))


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


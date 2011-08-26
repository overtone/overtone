(ns
  ^{:doc "The ugen functions create a data structure representing a synthesizer
         graph that can be executed on the synthesis server.  This is the logic
         to \"compile\" these clojure data structures into a form that can be
         serialized by the byte-spec defined in synthdef.clj."
    :author "Jeff Rose"}
  overtone.sc.synth
  (:require
    [overtone.log :as log]
    [clojure.contrib.generic.arithmetic :as ga]
    [at-at])
  (:use
    [overtone util event time-utils]
    [overtone.sc.ugen defaults common specs sc-ugen]
    [overtone.sc core ugen synthdef node buffer]
    [clojure.contrib [def :only [name-with-attributes]]]
    [clojure.contrib.seq-utils :only (indexed)]))
;;TODO replace this with clojure.core/keep-indexed or map-indexed))

;; ### Synth
;;
;; A Synth is a collection of unit generators that run together. They can be
;; addressed and controlled by commands to the synthesis engine. They read
;; input and write output to global audio and control buses. Synths can have
;; their own local controls that are set via commands to the server.

(def *params* nil)

(defn- ugen-index [ugens ugen]
  (first (first (filter (fn [[i v]]
                          (= (:id v) (:id ugen)))
                        (indexed ugens)))))

; TODO: Ugh...  There must be a nicer way to do this.  I need to get the group
; number (source) and param index within the group (index) from a grouped
; parameter structure like this (2x2 in this example):
;
;[[{:name :freq :default 440.0 :rate  1} {:name :amp :default 0.4 :rate  1}]
; [{:name :adfs :default 20.23 :rate  2} {:name :bar :default 8.6 :rate  2}]]
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
  (when-not (contains? ugen :args)
    (throw (IllegalArgumentException.
             (format "The %s ugen does not have any arguments."
                     (:name ugen)))))
  (when-not (every? #(or (sc-ugen? %) (number? %) (string? %)) (:args ugen))
    (throw (IllegalArgumentException.
             (format "The %s ugen has an invalid argument: %s"
                     (:name ugen)
                     (first (filter
                              #(not (or (sc-ugen? %) (number? %)))
                              (:args ugen)))))))

  (let [inputs (flatten
                 (map (fn [arg]
                        (cond
                          ; constant
                          (number? arg)
                          {:src -1 :index (index-of constants (float arg))}

                          ; control
                          (control-proxy? arg)
                          (param-input-spec grouped-params arg)

                          ; output proxy
                          (output-proxy? arg)
                          (let [src (ugen-index ugens (:ugen arg))]
                            {:src src :index (:index arg)})

                          ; child ugen
                          (sc-ugen? arg)
                          (let [src (ugen-index ugens arg)
                                updated-ugen (nth ugens src)]
                            (inputs-from-outputs src updated-ugen))))
                      (:args ugen)))
        ugen (assoc ugen :inputs inputs)]
    (when-not (every? (fn [{:keys [src index]}]
                    (and (not (nil? src))
                         (not (nil? index))))
                  (:inputs ugen))
      (throw (Exception.
               (format "Cannot connect ugen arguments for %s ugen with args: %s" (:name ugen) (str (seq (:args ugen)))))))

    ugen))

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
  (let [constants (map float constants)
        outs  (map with-outputs ugens)
        ins   (map #(with-inputs %1 outs constants grouped-params) outs)
        final (map #(assoc %1 :args nil) ins)]
    (doall final)))

(defn- make-control-ugens
  "Controls are grouped by rate, so that a single Control ugen represents
  each rate present in the params.  The Control ugens are always the top nodes
  in the graph, so they can be prepended to the topologically sorted tree.

  Specifically handles control proxies at :tr, :ar, :kr and :ir"
  [grouped-params]
  (loop [done   {}
         todo   grouped-params
         offset 0]
    (if (empty? todo)
      (filter #(not (nil? %))
              [(:ir done) (:tr done) (:ar done) (:kr done)])
      (let [group      (first todo)
            group-rate (:rate (first group))
            group-size (count group)
            ctl-proxy  (case group-rate
                             :tr (trig-control-ugen group-size offset)
                             :ar (audio-control-ugen group-size offset)
                             :kr (control-ugen group-size offset)
                             :ir (inst-control-ugen group-size offset))]

        (recur (assoc done group-rate ctl-proxy) (rest todo) (+ offset group-size))))))

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
            [(:ir by-rate) (:tr by-rate) (:ar by-rate) (:kr by-rate)])))

(def DEFAULT-RATE :kr)

(defn- ensure-param-keys!
  "throws an error if map m doesn't contain the correct keys: :name, :default and :rate"
  [m]
  (when-not (and
             (contains? m :name)
             (contains? m :default)
             (contains? m :rate))
    (throw (IllegalArgumentException. (str "Invalid synth param map. Expected to find the keys :name, :default, :rate, got" m)))))

(defn- ensure-paired-params!
  "throws an error if list l does not contain an even number of elements"
  [l]
  (when-not (even? (count l))
    (throw (IllegalArgumentException. (str "A synth requires either an even number of arguments in the form [control default]* i.e. [freq 440 vol 0.5] or a list of maps. You passed " (count l) " args:" l)))))

(defn- ensure-vec!
  "throws an error if list l is not a vector"
  [l]
  (when-not (vector? l)
    (throw (IllegalArgumentException. (str "Your synth argument list is not a vector. Instead I found " (type l) ": " l)))))

(defn- mapify-params
  "converts a list of param name val pairs to a param map. If the val of a param
  is a vector, it assumes it's a pair of [val rate] and sets the rate of the
  param accordingly. If the val is a plain number, it sets the rate to
  DEFAULT-RATE. All names are converted to strings"
  [params]
  (for [[p-name p-val] (partition 2 params)]
    (let [[p-val p-rate] (if (vector? p-val)
                           (do (when-not (= 2 (count p-val))
                                 (throw (IllegalArgumentException. (str "When specifiying the rate of a control, you need to use a vector of two args - default and rate i.e. [0.2 :ar]. Got: " p-val))))
                               p-val)
                           [p-val DEFAULT-RATE])]
      {:name  (str p-name)
       :default (float p-val)
       :rate  p-rate})))

(defn- stringify-names
  "takes a map and converts the val of key :name to a string"
  [m]
  (into {} (for [[k v] m] (if (= :name k) [k (str v)] [k v]))))

;; TODO: Figure out a better way to specify rates for synth parameters
;; perhaps using name post-fixes such as [freq:kr 440]
(defn- parse-params
  "Used by defsynth to parse the param list. Accepts either a vector of
   name default pairs, name [default rate] pairs or a vector of maps:

  (defsynth foo [freq 440] ...)
  (defsynth foo [freq [440 :ar]] ...)
  (defsynth foo [{:name \"freq\" :default 440 :rate :ar}] ...)

  Returns a vec of param maps"

  [params]
  (if (associative? (first params))
    (do
      (dorun (map ensure-param-keys! params))
      (vec (map stringify-names params)))
    (do
      (ensure-vec! params)
      (ensure-paired-params! params)
      (vec (mapify-params params)))))

(defn- make-params
  "Create the param value vector and parameter name vector."
  [grouped-params]
  (let [param-list (flatten grouped-params)
        pvals  (map #(:default %1) param-list)
        pnames (map (fn [[idx param]]
                      {:name (to-str (:name param))
                       :index idx})
                    (indexed param-list))]
    [pvals pnames]))

(defn- ugen-form? [form]
  (and (seq? form)
       (= 'ugen (first form))))

(defn- fastest-rate [rates]
  (REVERSE-RATES (first (reverse (sort (map RATES rates))))))

(defn- special-op-args? [args]
  (some #(or (sc-ugen? %1) (keyword? %1)) args))

(defn- find-rate [args]
  (fastest-rate (map #(cond
                        (sc-ugen? %1) (REVERSE-RATES (:rate %1))
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

    (synthdef \"pad-z\" [
  "
  [sname params ugens constants]
  (let [grouped-params (group-params params)
       [params pnames] (make-params grouped-params)
        with-ctl-ugens (concat (make-control-ugens grouped-params) ugens)
        detailed (detail-ugens with-ctl-ugens constants grouped-params)]
    (with-meta {:name (str sname)
                :constants constants
                :params params
                :pnames pnames
                :ugens detailed}
               {:type :overtone.sc.synthdef/synthdef})))

; TODO: This should eventually handle optional rate specifiers, and possibly
; be extended with support for defining ranges of values, etc...
(defn- control-proxies
  "Returns a list of param name symbols and control proxies"
  [params]
  (mapcat (fn [param] [(symbol (:name param))
                      `(control-proxy ~(:name param) ~(:default param) ~(:rate param))])
          params))

(defn- gen-synth-name
  "Auto generate an anonymous synth name. Intended for use in synths that have not
   been defined with an explicit name. Has the form \"anon-id\" where id is a unique
   integer across all anonymous synths."
  []
  (str "anon-" (next-id :anonymous-synth)))

(defn synth-player
  "Returns a player function for a named synth.  Used by (synth ...)
  internally, but can be used to generate a player for a pre-compiled
  synth.  The function generated will accept two optional arguments that
  must come first, the :position and :target (see the node function docs).

      (foo)
      (foo :position :tail :target 0)

  or if foo has two arguments:
      (foo 440 0.3)
      (foo :position :tail :target 0 440 0.3)
  at the head of group 2:
      (foo :position :head :target 2 440 0.3)

  These can also be abbreviated:
      (foo :tgt 2 :pos :head)
  "
  [sname arg-names]
  (fn [& args]
    (let [args (if (map? args)
                 (flatten (seq args))
                 args)
          [args sgroup] (if (or (= :target (first args))
                                (= :tgt    (first args)))
                          [(drop 2 args) (second args)]
                          [args @synth-group*])
          [args pos]    (if (or (= :position (first args))
                                (= :pos      (first args)))
                          [(drop 2 args) (second args)]
                          [args :tail])
          [args sgroup] (if (or (= :target (first args))
                                (= :tgt    (first args)))
                          [(drop 2 args) (second args)]
                          [args sgroup])
          player        #(node sname % {:position pos :target sgroup })
          args          (map #(if (or (isa? (type %) :overtone.sc.buffer/buffer)
                                      (isa? (type %) :overtone.sc.sample/sample)
                                      (isa? (type %) :overtone.sc.bus/audio-bus)
                                      (isa? (type %) :overtone.sc.bus/control-bus))
                                (:id %) %) args)

          arg-map       (arg-mapper args arg-names {})
]
      (player arg-map))))

(defn- normalize-synth-args
  "Pull out and normalize the synth name, parameters, control proxies and the ugen form
   from the supplied arglist resorting to defaults if necessary."
  [args]
  (let [[sname args] (cond
                       (or (string? (first args))
                           (symbol? (first args))) [(str (first args)) (rest args)]
                           :default                    [(gen-synth-name) args])

        [params ugen-form] (if (vector? (first args))
                             [(first args) (rest args)]
                             [[] args])
        param-proxies (control-proxies params)]
    [sname params param-proxies ugen-form]))

; TODO: Figure out how to generate the let-bindings rather than having them
; hard coded here.
(defmacro pre-synth
  "Resolve a synth def to a list of its name, params, ugens (nested if necessary) and
   constants. Sets the lexical bindings of the param names to control proxies within
   the synth definition"
  [& args]
  (let [[sname params param-proxies ugen-form] (normalize-synth-args args)]
    `(let [~@param-proxies]
       (binding [*ugens* []
                 *constants* #{}]
         (with-ugens
           (do
             ~@ugen-form)
           [~sname ~params *ugens* (into [] *constants*)])))))

(defmacro synth
  "Define a SuperCollider synthesizer using the library of ugen functions
  provided by overtone.sc.ugen.  This will return an anonymous function which
  can be used to trigger the synthesizer.
  "
  [& args]
  `(let [[sname# params# ugens# constants#] (pre-synth ~@args)
         sdef# (synthdef sname# params# ugens# constants#)
         arg-names# (map :name params#)
         player# (synth-player sname# arg-names#)
         smap# (callable-map {:name sname#
                              :ugens ugens#
                              :sdef sdef#
                              :player player#
                              :args arg-names#
                              :params params#
                              :type ::synth}
                             player#)]
     (load-synthdef sdef#)
     (event :new-synth :synth smap#)
     smap#))

(defn synth-form
  "Internal function used to prepare synth meta-data."
  [s-name s-form]
  (let [[s-name s-form] (name-with-attributes s-name s-form)
        _               (when (not (symbol? s-name))
                          (throw (IllegalArgumentException. (str "You need to specify a name for your synth using a symbol"))))
        params          (first s-form)
        params          (parse-params params)
        ugen-form       (second s-form)
        param-names     (list (vec (map :name params)))
        md              (assoc (meta s-name)
                          :name s-name
                          :type ::synth
                          :arglists (list 'quote param-names))]
    [(with-meta s-name md) params ugen-form]))

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
  [s-name & s-form]
  (let [[s-name params ugen-form] (synth-form s-name s-form)]
    `(def ~s-name (synth ~s-name ~params ~ugen-form))))

(defmethod print-method ::synth [syn w]
  (let [info (meta syn)]
    (.write w (format "#<synth: %s>" (:name info)))))

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
        ugen (get-ugen uname)
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
  "Replace ugen inputs that are other ugens with their generated symbolic name."
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
  "Decompile a parsed SuperCollider synth definition back into clojure code
  that could be used to generate an identical synth.

  While this probably won't create a synth definition that can directly compile, it can still be helpful when trying to reverse engineer a synth."
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

(def *demo-time* 2000)

(defmacro demo
  "Try out an anonymous synth definition.  Useful for experimentation.  If the
  root node is not an out ugen, then it will add one automatically.
  You can specify a timeout in seconds as the first argument otherwise it
  defaults to *demo-time* ms.

  (demo (sin-osc 440))      ;=> plays a sine wave for *demo-time* ms
  (demo 0.5 (sin-osc 440))  ;=> plays a sine wave for half a second"
  [& body]
  (let [[demo-time body] (if (number? (first body))
                           [(* 1000 (first body)) (second body)]
                           [*demo-time* (first body)])
        b2 (if (= 'out (first body))
             body
             (list 'out 0 body))]
    `(let [s# (synth "audition-synth" ~b2)
           note# (s#)]
       (at-at/at (+ (now) ~demo-time) #(node-free note#))
       note#)))

(defn active-synths
  ([] (active-synths (node-tree)))
  ([root]
   (let [synths (if (= :synth (:type root))
                  #{root}
                  #{})
         children (mapcat active-synths (:children root))]
     (into [] (if (empty? children)
       synths
       (set (concat synths children)))))))

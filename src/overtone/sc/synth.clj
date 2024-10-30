(ns overtone.sc.synth
  "The ugen functions create a data structure representing a synthesizer graph
  that can be executed on the synthesis server. This is the logic to \"compile\"
  these clojure data structures into a form that can be serialized by the
  byte-spec defined in synthdef.clj."
  {:author "Jeff Rose"}
  (:use
   [clojure.walk :as walk]
   [overtone.helpers lib old-contrib synth]
   [overtone.libs event counters]
   [overtone.music time]
   [overtone.sc.machinery.ugen fn-gen defaults common specs sc-ugen]
   [overtone.sc.machinery synthdef]
   [overtone.sc bindings ugens server node foundation-groups dyn-vars]
   [overtone.helpers seq]
   [clojure.pprint]
   [overtone.helpers.string :only [hash-shorten]])
  (:require
   [overtone.sc.machinery.synthdef :as synthdef]
   [overtone.config.log]
   [clojure.set :as set]
   [overtone.sc.cgens.env :refer [hold]]
   [overtone.sc.protocols :as protocols]))

(declare synth-player)

(defonce ^{:private true} __RECORDS__
  (do
    (defrecord-ifn Synth [name ugens sdef args params instance-fn]
      (partial synth-player sdef params))))

(defn- valid-control-proxy-rate?
  [rate]
  (some #{rate} CONTROL-PROXY-RATES))

;; ### Synth
;;
;; A Synth is a collection of unit generators that run together. They can be
;; addressed and controlled by commands to the synthesis engine. They read
;; input and write output to global audio and control buses. Synths can have
;; their own local controls that are set via commands to the server.

(defn- ugen-index [ugens ugen]
  (ffirst (filter (fn [[i v]]
                    (= (:id v) (:id ugen)))
                  (indexed ugens))))

; Gets the group number (source) and param index within the group (index)
; from the params that are grouped by rate like this:
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
  "Returns ugen object with its input ports connected to constants and
  upstream ugens according to the arguments in the initial definition."
  [ugen ugens constants grouped-params]
  (when-not (contains? ugen :args)
    (if-not (sc-ugen? ugen)
      (throw (IllegalArgumentException.
              (str "Error: synth expected a ugen. Got: " ugen)))
      (throw (IllegalArgumentException.
              (format "The %s ugen does not have any arguments."
                      (:name ugen))))))
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

    ;;Add link back to MaxLocalBufs ugen (always at root of tree) if
    ;;ugen is a local-buf.
    (if (= "LocalBuf" (:name ugen))
      (assoc ugen :inputs (concat (:inputs ugen) [{:src 0 :index 0}]))
      ugen)))

; TODO: Currently the output rate is hard coded to be the same as the
; computation rate of the ugen.  We probably need to have some meta-data
; capabilities for supporting varying output rates...
(defn- with-outputs
  "Returns a ugen with its output port connections setup according to
  the spec."
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
        ins   (map #(with-inputs %1 outs constants grouped-params) outs)]
    (doall ins)))

(defn- make-control-ugens
  "Controls are grouped by rate, so that a single Control ugen
  represents each rate present in the params.  The Control ugens are
  always the top nodes in the graph, so they can be prepended to the
  topologically sorted tree.

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
  "Groups params by rate.  Groups a list of parameters into a list of
   lists, one per rate."
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
  "Throws an error if map m doesn't contain the correct
  keys: :name, :default and :rate"
  [m]
  (when-not (and
             (contains? m :name)
             (contains? m :default)
             (contains? m :rate))
    (throw (IllegalArgumentException. (str "Invalid synth param map. Expected to find the keys :name, :default, :rate, got: " m)))))

(defn- ensure-paired-params!
  "Throws an error if list l does not contain an even number of
  elements"
  [l]
  (when-not (even? (count l))
    (throw (IllegalArgumentException. (str "A synth requires either an even number of arguments in the form [control default]* i.e. [freq 440 amp 0.5] or a list of maps. You passed " (count l) " args: " l)))))

(defn- ensure-vec!
  "Throws an error if list l is not a vector"
  [l]
  (when-not (vector? l)
    (throw (IllegalArgumentException. (str "Your synth argument list is not a vector. Instead I found " (type l) ": " l)))))

(defn- ensure-valid-control-proxy-vec!
  [val]
  (when-not (= 2 (count val))
    (throw (IllegalArgumentException. (str "Control Proxy vector must have only 2 elements i.e. [0 :tr]"))))
  (when-not (number? (first val))
    (throw (IllegalArgumentException. (str "Control Proxy vector must have a number as the first element i.e. [0 :tr]"))))
  (when-not (valid-control-proxy-rate? (second val))
    (throw (IllegalArgumentException. (str "Control Proxy rate " (second val) " not valid. Expecting one of " CONTROL-PROXY-RATES))))
  val)

(defn- mapify-params
  "Converts a list of param name val pairs to a param map. If the val of
  a param is a vector, it assumes it's a pair of [val rate] and sets the
  rate of the param accordingly. If the val is a plain number, it sets
  the rate to DEFAULT-RATE. All names are converted to strings"
  [params]
  (for [[p-name p-val] (partition 2 params)]
    (let [param-map
          (cond
           (vector? p-val) (do
                             (ensure-valid-control-proxy-vec! p-val)
                             {:name (str p-name)
                              :default (first p-val)
                              :rate (second p-val)})

           (associative? p-val) (merge
                                 {:name  (str p-name)
                                  :rate  DEFAULT-RATE} p-val)

           :else {:name (str p-name)
                  :default `(float (to-id ~p-val))
                  :rate DEFAULT-RATE})]
      (ensure-param-keys! param-map)
      param-map)))

(defn- stringify-names
  "Takes a map and converts the val of key :name to a string"
  [m]
  (into {} (for [[k v] m] (if (= :name k) [k (str v)] [k v]))))

;; TODO: Figure out a better way to specify rates for synth parameters
;; perhaps using name post-fixes such as [freq:kr 440]
(defn- parse-params
  "Used by defsynth to parse the param list. Accepts either a vector of
   name default pairs, name [default rate] pairs or a vector of maps:

  (defsynth foo [freq 440] ...)
  (defsynth foo [freq [440 :ar]] ...)
  (defsynth foo [freq {:default 440 :rate :ar}] ...)

  Returns a vec of param maps"

  [params]
  (ensure-vec! params)
  (ensure-paired-params! params)
  (vec (mapify-params params)))

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
  "Transforms a synth definition (ugen-graph) into a form that's ready
  to save to disk or send to the server.

    (synthdef \"pad-z\" [
  "
  [sname params ugens constants]
  (let [grouped-params  (group-params params)
        [params pnames] (make-params grouped-params)
        with-ctl-ugens  (concat (make-control-ugens grouped-params) ugens)
        detailed        (detail-ugens with-ctl-ugens constants grouped-params)
        full-name (if (add-current-namespace-to-synth-name?)
                    (hash-shorten 31 (ns-name *ns*) (str "/" sname))
                    sname)]
    (with-meta {:name full-name
                :constants constants
                :params params
                :pnames pnames
                :ugens detailed}
               {:type :overtone.sc.machinery.synthdef/synthdef})))

(defn- control-proxies
  "Returns a list of param name symbols and control proxies"
  [params]
  (mapcat (fn [param] [(symbol (:name param))
                      `(control-proxy ~(:name param) ~(:default param) ~(:rate param))])
          params))

(defn- gen-synth-name
  "Auto generate an anonymous synth name. Intended for use in synths
   that have not been defined with an explicit name. Has the form
   \"anon-id\" where id is a unique integer across all anonymous
   synths."
  []
  (str "anon-" (next-id ::anonymous-synth)))

(defn normalize-synth-args
  "Pull out and normalize the synth name, parameters, control proxies
   and the ugen form from the supplied arglist resorting to defaults if
   necessary."
  [args]
  (let [[sname args]       (if (or (string? (first args))
                                   (symbol? (first args)))
                             [(str (first args)) (rest args)]
                             [(gen-synth-name) args])
        [params ugen-form] (if (vector? (first args))
                             [(first args) (rest args)]
                             [[] args])
        params (parse-params params)
        param-proxies (control-proxies params)]
    [sname params param-proxies ugen-form]))

(defn gather-ugens-and-constants
  "Traverses a ugen tree and returns a vector of two sets [#{ugens}
  #{constants}]."
  [root]
  (if (seq? root)
    (reduce
      (fn [[ugens constants] ugen]
        (let [[us cs] (gather-ugens-and-constants ugen)]
          [(set/union ugens us)
           (set/union constants cs)]))
      [#{} #{}]
      root)
    (let [args (:args root)
          cur-ugens (filter sc-ugen? args)
          cur-ugens (filter (comp not control-proxy?) cur-ugens)
          cur-ugens (map #(if (output-proxy? %)
                            (:ugen %)
                            %)
                         cur-ugens)
          cur-consts (filter number? args)
          [child-ugens child-consts] (gather-ugens-and-constants cur-ugens)
          ugens (conj (set child-ugens) root)
          constants (set/union (set cur-consts) child-consts)]
      [ugens (vec constants)])))

(defn- ugen-children
  "Returns the children (arguments) of this ugen that are themselves
  upstream ugens."
  [ug]
  (mapcat
    #(cond
       (seq? %) %
       (output-proxy? %) [(:ugen %)]
       :default [%])
    (filter
      (fn [arg]
        (and (not (control-proxy? arg))
             (or (sc-ugen? arg)
                 (and (seq? arg)
                      (every? sc-ugen? arg)))))
      (:args ug))))

(defn topological-sort-ugens
  "Sort into a vector where each node in the directed graph of ugens
   will always be preceded by its upstream dependencies.  Depth first,
   from:

   http://en.wikipedia.org/wiki/Topological_sorting,

   following the advice here:

   http://supercollider.svn.sourceforge.net/viewvc/supercollider/trunk/common/build/Help/ServerArchitecture/Synth-Definition-File-Format.html

   'For greatest efficiency:

   Unit generators should be listed in an order that permits efficient
   reuse of connection buffers, which means that a depth first
   topological sort of the graph is preferable to breadth first.'"
  [ugens]
  (let [visit (fn visit [[ret visited path :as acc] ug]
                (cond
                 (visited ug) acc
                 (path ug)    (throw (Exception. "ugen graph contains cycle"))
                 :else
                 (let [[ret visited path :as acc]
                       (reduce visit [ret visited (conj path ug)] (ugen-children ug))]
                   [(conj ret ug) (conj visited ug) path])))]
    (first (reduce visit [[] #{} #{}] ugens))))

#_:clj-kondo/ignore
(comment
  ; Some test synths, while shaking out the bugs...
(defsynth foo [] (out 0 (rlpf (saw [220 663]) (x-line:kr 20000 2 1 FREE))))
(defsynth bar [freq 220] (out 0 (rlpf (saw [freq (* 3.013 freq)]) (x-line:kr 20000 2 1 FREE))))
(definst faz [] (rlpf (saw [220 663]) (x-line:kr 20000 2 1 FREE)))
(definst baz [freq 220] (rlpf (saw [freq (* 3.013 freq)]) (x-line:kr 20000 2 1 FREE)))
(run 1 (out 184 (saw (x-line:kr 10000 10 1 FREE)))))

(defn count-ugens
  [ug-tree ug-name]
  (let [ugens      (flatten  ug-tree)
        local-bufs (filter #(= ug-name (:name %)) ugens)
        ids        (set (map :id local-bufs))]
    (count ids)))

(defmacro pre-synth
  "Resolve a synth def to a list of its name, params, ugens (nested if necessary)
  and constants. Sets the lexical bindings of the param names to control proxies
  within the synth definition"
  [& args]
  (let [[sname params param-proxies ugen-form] (normalize-synth-args args)]
    `(let [~@param-proxies]
          (binding [*ugens* []
                    *constants* #{}]
            (let [[ugens# consts#] (gather-ugens-and-constants
                                    (with-overloaded-ugens ~@ugen-form))
                  ugens#           (topological-sort-ugens ugens#)
                  main-tree#       (set ugens#)
                  side-tree#       (filter #(not (main-tree# %)) *ugens*)
                  ugens#           (concat ugens# side-tree#)
                  n-local-bufs#    (count-ugens ugens# "LocalBuf")
                  ugens#           (if (> n-local-bufs# 0)
                                     (cons (max-local-bufs n-local-bufs#) ugens#)
                                     ugens#)
                  consts#          (if (> n-local-bufs# 0)
                                     (cons n-local-bufs# consts#)
                                     consts#)
                  consts#       (into [] (set (concat consts# *constants*)))]
              [~sname ~params ugens# consts#])))))

(defn synth-player
  "Returns a player function for a named synth.  Used by (synth ...)
    internally, but can be used to generate a player for a pre-compiled
    synth.  The function generated will accept a target and position
    vector of two values that must come first (see the node function
    docs).

    ;; call foo player with default args:
    (foo)

    ;; call foo player specifying node should be at the tail of group 0
    (foo [:tail 0])

    ;; call foo player with positional arguments
    (foo 440 0.3)

    ;; target node to be at the tail of group 0 with positional args
    (foo [:tail 0] 440 0.3)

    ;; or target node to be at the head of group 2
    (foo [:head 2] 440 0.3)

    ;; you may also use keyword args
    (foo :freq 440 :amp 0.3)

    ;; which allows you to re-order the args
    (foo :amp 0.3 :freq 440 )

    ;; you can also combine a target vector with keyword args
    (foo [:head 2] :amp 0.3 :freq 440)

    ;; finally, you can combine target vector, keywords args and
    ;; positional args. Positional args must go first.
    (foo [:head 2] 440 :amp 0.3)
    "
  [sdef params this & args]
  (let [arg-names         (map keyword (map :name params))
        args              (or args [])
        [target pos args] (extract-target-pos-args args (foundation-default-group) :tail)
        args              (idify args)
        args              (map (fn [arg] (if-let [id (:id arg)]
                                           id
                                           arg))
                               args)
        defaults          (into {} (map (fn [{:keys [name value]}]
                                          [(keyword name) @value])
                                        params))
        arg-map           (arg-mapper args arg-names defaults)
        synth-node        (node (:name sdef) arg-map {:position pos :target target} sdef)
        synth-node        (if (:instance-fn this)
                            ((:instance-fn this) synth-node)
                            synth-node)]
    (when (:instance-fn this)
      (swap! active-synth-nodes* assoc (:id synth-node) synth-node))
    synth-node))


(defn update-tap-data
  [msg]
  (let [[node-id label-id val] (:args msg)
        node                     (get @active-synth-nodes* node-id)
        label                    (get (:tap-labels node) label-id)
        tap-atom                 (get (:taps node) label)]
    (reset! tap-atom val)))

(on-event "/overtone/tap" #'update-tap-data ::handle-incoming-tap-data)

(defonce control-proxy-cache
  (atom {}))

(defn control-proxy-value-atom
  "Provide the `:value` for a ControlProxy, which is an atom. Attempts to reuse
  existing atoms in case a synth gets redefined."
  [full-name param]
  (let [ref (or (get-in @control-proxy-cache [full-name (:name param)])
                (atom nil))]
    (swap! control-proxy-cache assoc-in [full-name (:name param)] ref)
    (when (or (nil? @ref)
              (not (<= (:min param 0) @ref (:max param Double/MAX_VALUE))))
      (reset! ref (:default param)))
    ref))

(defmacro synth
  "Define a SuperCollider synthesizer using the library of ugen
  functions provided by overtone.sc.ugen. This will return callable
  record which can be used to trigger the synthesizer.
  "
  [sname & args]
  `(let [full-name# '~(symbol (str *ns*) (str sname))
         [sname# params# ugens# constants#] (pre-synth ~sname ~@args)
         sdef#             (synthdef sname# params# ugens# constants#)
         arg-names#        (map :name params#)
         params-with-vals# (map #(assoc % :value (control-proxy-value-atom full-name# %)) params#)
         instance-fn#      (apply comp (map :instance-fn (filter :instance-fn (map meta ugens#))))
         smap# (with-meta
                 (map->Synth
                  {:name sname#
                   :sdef sdef#
                   :args arg-names#
                   :params params-with-vals#
                   :instance-fn instance-fn#})
                 {:overtone.helpers.lib/to-string #(str (name (:type %)) ":" (:name %))})]
     (load-synthdef sdef#)
     (event :new-synth :synth smap#)
     smap#))

(defn synth-form
  "Internal function used to prepare synth meta-data."
  ([s-name s-form]
   (synth-form s-name s-form {}))
  ([s-name s-form {:keys [compile-time?]
                   :or {compile-time? true}}]
   (let [[s-name s-form] (name-with-attributes s-name s-form)
         _               (when (not (symbol? s-name))
                           (throw (IllegalArgumentException. (str "You need to specify a name for your synth using a symbol"))))
         params          (first s-form)
         ugen-form       (concat '(do) (next s-form))
         param-names     (list (vec (map #(symbol (:name %)) (parse-params params))))
         md              (assoc (meta s-name)
                                :name s-name
                                :type ::synth
                                ;; At run-time, we don't need the extra `quote`.
                                :arglists (if compile-time?
                                            (list 'quote param-names)
                                            param-names))]
     [(with-meta s-name md) params ugen-form])))

(defmacro defsynth
  "Define a synthesizer and return a player function. The synth
  definition will be loaded immediately, and a :new-synth event will be
  emitted. Expects a name, an optional doc-string, a vector of synth
  params, and a ugen-form as its arguments.

  (defsynth foo [freq 440]
    (out 0 (sin-osc freq)))

  is equivalent to:

  (def foo
    (synth [freq 440] (out 0 (sin-osc freq))))

  Params can also be given rates. By default, they are :kr, however
  another rate can be specified by using either a pair of [default rate]
  or a map with keys :default and rate:

  (defsynth foo [freq [440 :kr] gate [0 :tr]] ...)
  (defsynth foo [freq {:default 440 :rate :kr}] ...)

  A doc string can also be included:
  (defsynth bar
    \"The phatest space pad ever!\"
    [] (...))

  The function generated will accept a target vector argument that
  must come first, containing position and target as elements (see the
  node function docs).

  ;; call foo player with default args:
  (foo)

  ;; call foo player specifying node should be at the tail of group 0
  (foo [:tail 0])

  ;; call foo player with positional arguments
  (foo 440 0.3)

  ;; target node to be at the tail of group 0 with positional args
  (foo [:tail 0] 440 0.3)

  ;; or target node to be at the head of group 2
  (foo [:head 2] 440 0.3)

  ;; you may also use keyword args
  (foo :freq 440 :amp 0.3)

  ;; which allows you to re-order the args
  (foo :amp 0.3 :freq 440 )

  ;; you can also combine a target vector with keyword args
  (foo [:head 2] :amp 0.3 :freq 440)

  ;; finally, you can combine target vector, keywords args and
  ;; positional args. Positional args must go first.
  (foo [:head 2] 440 :amp 0.3)"
  [s-name & s-form]
  {:arglists '([name doc-string? params ugen-form])}
  (let [[s-name params ugen-form] (synth-form s-name s-form)]
    `(def ~s-name (synth ~s-name ~params ~ugen-form))))

(defn synth-load
  "Load synthdef data from either a file specified using a string path
  a URL (e.g. resource), or a byte array."
  [data]
  (let [{:keys [pnames params] :as sdef} (if (string? data)
                                           (synthdef/load-synth-file data)
                                           (let [sdef (synthdef/synthdef-read data)]
                                             (synthdef/load-synthdef sdef)
                                             sdef))
        [s-name params _ugen-form] (synth-form (symbol (:name sdef))
                                               (list (vec (mapcat (fn [pname default-value]
                                                                    [(symbol (:name pname)) default-value])
                                                                  pnames params))
                                                     nil)
                                               {:compile-time? false})]
    (with-meta
      (map->Synth
       {:name s-name
        :sdef sdef
        :args params})
      (merge {:overtone.helpers.lib/to-string #(str (name (:type %)) ":" (:name %))}
             (meta s-name)))))

(defmacro defsynth-load
  "Load a synth from a compiled Synthdef file string, URL (e.g. resource) or
  byte array.

  E.g.
  (defsynth-load my-beep
   \"/Users/paulo.feodrippe/dev/sonic-pi/etc/synthdefs/compiled/sonic-pi-beep.scsyndef\")

  (my-beep :note 40)

    or, with an example using a resource,

  (defsynth-load my-synth
    (io/resource \"event.scsyndef\"))"
  [def-name data]
  `(let [smap# (synth-load ~data)]
     (def ~def-name
       smap#)
     (alter-meta! (var ~def-name) merge (meta ~def-name))
     (var ~def-name)))

(defn synth?
  "Returns true if s is a synth, false otherwise."
  [s]
  (= overtone.sc.synth.Synth (type s)))

(def ^{:dynamic true} *demo-time* 2000)

(defmacro run
  "Run an anonymous synth definition for a fixed period of time.
  Useful for experimentation. Does NOT add an out ugen - see #'demo for
  that. You can specify a timeout in seconds as the first argument
  otherwise it defaults to *demo-time* ms.

  (run (send-reply (impulse 1) \"/foo\" [1] 43)) ;=> send OSC messages"
  [& body]
  (let [[demo-time body] (if (number? (first body))
                           [(* 1000 (first body)) (second body)]
                           [*demo-time* (first body)])]
    `(let [s# (synth "audition-synth" ~body)
           note# (s#)]
       (after-delay ~demo-time #(node-free note#))
       note#)))

(defmacro demo
  "Listen to an anonymous synth definition for a fixed period of time.
  Useful for experimentation.  If the root node is not an out ugen, then
  it will add one automatically.  You can specify a timeout in seconds
  as the first argument otherwise it defaults to *demo-time* ms. See
  #'run for a version of demo that does not add an out ugen.

  (demo (sin-osc 440))      ;=> plays a sine wave for *demo-time* ms
  (demo 0.5 (sin-osc 440))  ;=> plays a sine wave for half a second"
  [& body]
  (let [[demo-time body] (if (number? (first body))
                           [(first body) (second body)]
                           [(* 0.001 *demo-time*) (first body)])
        [out-bus body]   (if (= 'out (first body))
                           [(second body) (nth body 2)]
                           [0 body])

        body (list `out out-bus (list `hold body demo-time :done `FREE))]
    `((synth "audition-synth" ~body))))

(defn active-synths
  "Return a seq of the actively running synth nodes.  If a synth or inst
  are passed as the filter it will only return nodes of that type.

  (active-synths) ;=> [{:type synth :name \"mixer\" :id 12} {:type
                        synth :name \"my-synth\" :id 24}]
  (active-synths my-synth) ;=>[{:type synth :name \"my-synth\" :id 24}]
  "
  [& [synth-filter]]
  (let [active-nodes (filter #(= overtone.sc.node.SynthNode (type %))
                             (vals @active-synth-nodes*))]
    (if synth-filter
      (filter #(= (:name synth-filter) (:name %)) active-nodes)
      active-nodes)))

(defmethod print-method ::synth [syn ^java.io.Writer w]
  (let [info (meta syn)]
    (.write w (format "#<synth: %s>" (:name info)))))

; TODO: pull out the default param atom stuff into a separate mechanism
(defn modify-synth-params
  "Update synth parameter value atoms storing the current default
  settings."
  [s & params-vals]
  (let [params (:params s)]
    (for [[param value] (partition 2 params-vals)]
      (let [val-atom (:value (first (filter #(= (:name %) (name param)) params)))]
        (if val-atom
          (reset! val-atom value)
          (throw (IllegalArgumentException. (str "Invalid control parameter: " param))))))))

(defn reset-synth-defaults
  "Reset a synth to its default settings defined at definition time."
  [synth]
  (doseq [param (:params synth)]
    (reset! (:value param) (:default param))))


(defmacro with-no-ugen-checks [& body]
  `(binding [overtone.sc.machinery.ugen.specs/*checking* false]
     ~@body))

(defmacro with-ugen-debugging [& body]
  `(binding [overtone.sc.machinery.ugen.specs/*debugging* true]
     ~@body))

(defn synth-args
  "Returns a seq of the synth's args as keywords"
  [synth]
  (map keyword (:args synth)))

(defn synth-arg-index
  "Returns an integer index of synth's argument with arg-name.

   For example:

   (defsynth foo [freq 440 amp 0.5] (out 0 (* amp (sin-osc freq))))

   (synth-arg-index foo :amp) #=> 1
   (synth-arg-index foo \"freq\") #=> 0
   (synth-arg-index foo :baz) #=> nil"
  [synth arg-name]
  (let [arg-name (name arg-name)]
    (index-of (:args synth) arg-name)))


(extend Synth
  protocols/IKillable
  {:kill* (fn [s]
            (kill (node-tree-matching-synth-ids (:name (:sdef s)))))})

(extend java.util.regex.Pattern
  protocols/IKillable
  {:kill* (fn [s]
            (kill (node-tree-matching-synth-ids (:name (:sdef s)))))})

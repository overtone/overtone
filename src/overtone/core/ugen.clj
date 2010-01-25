(ns
  #^{:doc "UGens, or Unit Generators, are the functions that act as DSP nodes in the synthesizer definitions used by SuperCollider.  We generate most of the UGen functions for clojure based on metadata about each ugen, and eventually we hope to get this information dynamically from the server itself."
     :author "Jeff Rose & Christophe McKeon"}
  overtone.core.ugen
  (:use (overtone.core util ops ugens-common ugen-categories)
     clojure.contrib.seq-utils
     clojure.contrib.pprint))

;; Outputs have a specified calculation rate
;;   0 = scalar rate - one sample is computed at initialization time only. 
;;   1 = control rate - one sample is computed each control period.
;;   2 = audio rate - one sample is computed for each sample of audio output.
(def RATES {:scalar  0
            :control 1
            :audio   2
            :demand  3})

(def REVERSE-RATES (invert-map RATES))

(def UGEN-SPEC-EXPANSION-MODES
  {:not-expanded false
   :append-sequence :false
   :append-sequence-set-num-outs false
   :num-outs false
   :done-action false
   :as-ar true ;; This should still expand right?
   :standard true
   })

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

(defn normalize-ugen-name 
  "Normalizes SuperCollider and overtone-style names to squeezed lower-case."
  [n]
  (.replaceAll (.toLowerCase (str n)) "[-|_]" ""))

(defn overtone-ugen-name 
  "A basic camelCase to with-dash name converter tuned to convert SuperCollider names to Overtone names.  
  Most likely needs improvement."
  [n]
  (let [n (.replaceAll n "([a-z])([A-Z])" "$1-$2") 
        n (.replaceAll n "([A-Z])([A-Z][a-z])" "$1-$2") 
        n (.replaceAll n "_" "-") 
        n (.toLowerCase n)]
  n))

(defn derived? [spec]
  (contains? spec :extends))

(defn derive-ugen-specs
  "Merge the ugen spec maps to give children their parent's attributes.
  
  Recursively reduces the specs to support arbitrary levels of derivation."
  ([specs] (derive-ugen-specs specs {} 0))
  ([children adults depth]
   (assert (< depth 8))
   (println (format "top:  %d - %d" (count adults) (count children)))
   (let [[adults children] 
         (reduce (fn [[full-specs new-children] spec]
                   (if (derived? spec) 
                     (if (contains? full-specs (:extends spec))
                       [(assoc full-specs (:name spec) 
                               (merge (get full-specs (:extends spec)) spec)) 
                        new-children]
                       [full-specs (conj new-children spec)])
                     [(assoc full-specs (:name spec) spec) new-children]))
                 [adults []]
                 children)]
     (println (format "bottom: %d - %d" (count adults) (count children)))
     (println (map #(:name %) children))
     (if (empty? children)
       adults
       (recur children adults (inc depth))))))

(defn with-categories
  "Adds a :categories attribute to a ugen-spec for later use in documentation,
  GUI and REPL interaction."
  [spec]
  (let [cats (get UGEN-CATEGORY-MAP (:name spec) [])]
    (println "categories: " (:name spec) " : " cats)
    (assoc spec :categories cats)))

(defn with-expands 
  "Sets the :expands? attribute for ugen-spec arguments, which will inform the
  automatic channel expansion system when to expand argument."
  [spec]
  (assoc spec :args 
         (map (fn [arg] 
                (assoc arg :expands? 
                       (get UGEN-SPEC-EXPANSION-MODES (get arg :mode :standard))))
              (:args spec))))

(def UGEN-DEFAULT-RATES #{:ar :kr})
(def UGEN-RATE-PRECEDENCE [:ir :dr :ar :kr])

(defn with-fn-names
  "Generates all the function names for this ugen and adds a :fn-names map 
  that maps function names to rates, representing the output rate.

  All available rates get an explicit function name of the form <fn-name>:<rate>
  like this:
    * (env-gen:ar ...)
    * (env-gen:kr ...)

  UGens will also have a base-name without a rate suffix that uses the default rate
  for that ugen:
    * (env-gen ...)   ;; Uses :kr, control rate for EnvGen
  
  The default rate is determined by the rate precedence:
    [:ir :dr :ar :kr]

  or a :default-rate attribute can override the default precedence order."
  [spec]
  (let [rates (get spec :rates UGEN-DEFAULT-RATES)
        base-name (overtone-ugen-name (:name spec))
        base-rate (cond
                    (contains? spec :default-rate) (:default-rate spec)
                    (= 1 (count rates)) (first rates)
                    :default (first (filter rates UGEN-RATE-PRECEDENCE)))
        name-rates (reduce (fn [nr rate] (assoc nr (str base-name rate) rate))
                           {}
                           (:rates spec))]
    (assoc spec
           :rates rates
           :fn-names (assoc name-rates base-name base-rate))))

; TODO
(defn map-ugen-args 
  "Perform any argument mappings that needs to be done."
  [spec args]
  args)

; TODO
(defn do-ugen-arg-modes 
  "Apply whatever mode specific functions need to be performed on the argument
  list."
  [spec args]
  args)

;  - build a new :init function which will later be called
;    by the ugen function (after MCE), over-writing :init if it exists.
;    note that, if an :init was given, but there are no args with :map
;    properties AND all modes are :standard, then no new function need be
;    defined. also if all modes are :standard, there are no args with :map,
;    and no :init function was given, then :init can just be left
;    undefined and the ugen function will check that and not try to call one.
;    the function should take the same args as :init does
;    and do the following:
;    - for any arg with a :map property, resolves the mapping
;      the map property can be any arbitrary function, including a map.
;      the function or map should just return the argument, if there is no
;      defined mapping.
;    - calls the original :init on the args, if any was given
;    - rearanges the args according to the modes, and returns them (jeff, i
;      can't remember which order we decided on for this and the previous item.
;      i'm tired)
;    - instead of calling :check from the ugen function, it could be done from
;      in here instead? 
(defn with-init-fn 
  "Creates the final argument initialization function which is applied to arguments
  at runtime to do things like re-ordering and automatic filling in of arguments.  
  Typically appending input arrays as the last argument and filling in the number of 
  in or out channels for those ugens that need it.
  
  If an init function is already present it will get called after doing the mapping and
  mode transformations." 
  [spec]
  (let [map-fn (partial map-ugen-args spec)
        mode-fn (partial do-ugen-arg-modes spec)
        init-fn (if (contains? spec :init)
                  (comp mode-fn (:init spec) map-fn)
                  (comp mode-fn map-fn))]
    (assoc spec :init init-fn)))

(defn init-ugen-specs 
  "Perform the derivation and setup defaults for rates, names, 
  argument initialization functions, and channel expansion flags."
  [specs]
  (let [derived (derive-ugen-specs specs)]
    (map (fn [[ugen-name spec]] 
           (println "..." ugen-name) 
           (-> spec
             (with-categories)
             (with-expands)
             (with-fn-names)
             (with-init-fn)))
         derived)))

(defn load-ugen-specs [namespaces]
  (mapcat (fn [ns]
            (let [full-ns (symbol (str "overtone.core.ugens." ns))]
              (require [full-ns :only 'specs])
              (var-get (ns-resolve full-ns 'specs))))
          namespaces))
  
; not including: pseudo, filter
(def UGEN-NAMESPACES 
  '[basicops buf-io compander delay envgen fft2 fft-unpacking grain])
;    io machine-listening misc osc beq-suite chaos control demand 
;    ff-osc fft info line mac-ugens noise pan trig])

(def UGEN-SPECS (init-ugen-specs (load-ugen-specs UGEN-NAMESPACES)))
(def UGEN-SPEC-MAP 
  (doall (reduce (fn [mem spec] 
                   (assoc mem (normalize-ugen-name (:name spec)) spec))
                 {}
                 UGEN-SPECS)))

(defn get-ugen [word]
  (get UGEN-SPEC-MAP (normalize-ugen-name word)))

(defn find-ugen [regexp]
  (map #(second %)
       (filter (fn [[k v]] (re-find (re-pattern regexp) (str k)))
               UGEN-SPEC-MAP)))

(defn- print-ugen-args [args]
  (println "args: (defaults)")
  (doseq [arg args]
    (print (str "\t" (:name arg) ": " (if (contains? arg :default)
                                        (:default arg)
                                        "<no-default>\n")))))

(defn print-ugen [& ugens]
  (doseq [ugen ugens]
    (println "UGen:" (str "\"" (:name ugen) "\""))
    (print-ugen-args (:args ugen))
    (println "outputs: " 
             (str "[" (apply str (interpose ", "(:rates ugen))) "]"))))

(defn ugen-doc [word]
  (apply print-ugen (find-ugen word)))

(defn inf!
  "users use this to tag infinite sequences for use as
   expanded arguments. needed or else multichannel
   expansion will blow up" 
  [sq]
  (with-meta sq
    (merge (meta sq) {:infinite-sequence true})))

(defn inf? [obj]
  (:infinite-sequence (meta obj)))

(defn mapply [f coll-coll]
  (map #(apply f %) coll-coll))

(defn parallel-seqs
  "takes n seqs and returns a seq of vectors of length n, lazily
   (take 4 (parallel-seqs (repeat 5) (cycle [1 2 3]))) ->
     ([5 1] [5 2] [5 3] [5 1])"
  [& seqs]
  (apply map vector seqs))

(defn cycle-vals [coll]
  (cycle (if (map? coll) (vals coll) coll)))

; because i am using maps as csproxies (cient side ugens)
; and because i wanted to expand maps, it checks if it's a csproxy before it expands.
; to test this code you could just use this dummy function 
(defn csproxy? [_] false)

(defn multichannel-expand
  "Does sc style multichannel expansion.
  * does not expand seqs flagged infinite
  * note that this returns a list even in the case of a single expansion
  "
  [args]
  (if (zero? (count args))
    [[]]
    (let [gc-seqs (fn [[gcount seqs] arg]
                    (cond 
                      (inf? arg) [gcount (conj seqs arg)]

                      (and (coll? arg) (not (or (map? arg) (csproxy? arg))))
                      [(max gcount (count arg)) (conj seqs (cycle-vals arg))]

                      :else 
                      [gcount (conj seqs (repeat arg))]))
          [greatest-count seqs] (reduce gc-seqs [1 []] args)]
      (take greatest-count (apply parallel-seqs seqs)))))

(defn make-expanding
  "Takes a function and returns a multi-channel-expanding version of the function."
  [f expand-flags]
  (fn [& args]
    (let [ary (last args)
          expanded (if (and has-ary? (coll? ary) (not (map? ary)))
                     (map #(concat % ary) (multichannel-expand (drop-last args)))
                     (multichannel-expand args))
          applied (mapply f expanded)]
      (if (= (count applied) 1)
        (first applied)
        applied))))

(defn- replace-action-args [args]
  (map #(get DONE-ACTIONS %1 %1) args))

(defn- envelope-args [spec args]
  (replace-action-args 
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
      (flatten (concat with-defs [env-ary])))))

(defn- add-default-args [spec args]
  (let [defaults (map #(:default %1) (:args spec))
        defaults (drop (count args) defaults)]
    (if (some #(= :none %) defaults)
      (throw (IllegalArgumentException. 
        (str "Missing arguments to ugen: " (:name spec) " => "  
             (doall (drop (count args) (map #(%1 :name) (:args spec)))))))
      (concat args defaults))))

(defn make-ugen 
  "Returns a function representing the given ugen that will fill in default arguments, rates, etc."
  [spec]
  (let [expand-flags (map #(:expands %) (:args spec))]
    (make-expanding (fn [& args]
                 (let [args (add-default-args spec args)
                       uname (:name spec)
                       [uname special] (cond
                                         (unary-op-num uname) 
                                         ["UnaryOpUGen" (unary-op-num uname)]

                                         (binary-op-num uname) 
                                         ["BinaryOpUGen" (binary-op-num uname)]

                                         :default [uname 0])]
                   (with-meta {:id (next-id :ugen)
                               :name uname 
                               :rate (rate RATES)
                               :special special
                               :args args}
                              {:type ::ugen})))
               expand-flags)))

(defn ugen? [obj] (= ::ugen (type obj)))

(defn overload-ugen-op [ns ugen-name ugen-fn]
  (let [original-fn (ns-resolve ns ugen-name)]
    (ns-unmap ns ugen-name)
    (intern ns ugen-name (fn [& args]
                           (if (some #(or (ugen? %) (not (number? %))) args)
                             (apply ugen-fn args)
                             (apply original-fn args))))))

;; TODO:
;; * Need to write a function that takes a ugen-spec, and generates a set
;; of ugen functions for that spec.  Each of these functions will automatically
;; set the rate for the ugen.
;;
;; * Need a function that iterates over all ugen-specs and generates ugen-fns
;; * Need a function that generates all the special-ops functions
  
(defn refer-ugens [ns]
  (let [core-ns (find-ns 'clojure.core)]
    (doseq [ugen UGEN-SPECS]
      (let [ugen-name (symbol (overtone-ugen-name (:name ugen)))
            ugen-name (with-meta ugen-name {:doc (with-out-str (print-ugen ugen))})
            ugen-fn (make-ugen ugen)]
        (cond
          (ns-resolve core-ns ugen-name) (overload-ugen-op ns ugen-name ugen-fn)
          (ns-resolve ns ugen-name) nil
          :default (intern ns ugen-name ugen-fn))))))

(refer-ugens (create-ns 'overtone.ugens))

;; TODO: Figure out the complete list of control types
;; This is used to determine the controls we list in the synthdef, so we need
;; all ugens which should be specified as external controls.
(def CONTROLS #{"control"})

(defn control-ugen [rate n-outputs]
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

(defn control? [obj]
  (isa? (type obj) ::control))


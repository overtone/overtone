(ns overtone.linter
  "Helpers for linting overtone vars correctly (using clj-kondo)."
  (:require
   [clojure.string :as str]
   [clojure.java.io :as io]
   [clojure.pprint :as pp]
   [overtone.api]))

(defn find-vars
  "Find vars in your project according to one or more of the following queries:
  - `:namespaces` (collection of namespaces in symbol or string format)
  - `:ns-meta` (namespaces which contain this metadata)
  - `:ns-prefix` (namespaces with this prefix)
  - `:vars` (collection of vars in symbol or string format)
  - `:var-meta` (vars which contain this metadata)"
  [{:keys [namespaces ns-meta ns-prefix vars var-meta]}]
  (when (or (seq namespaces) ns-meta (seq ns-prefix) (seq vars) var-meta)
    (let [namespaces-set (set (mapv str namespaces))
          vars-set (set (mapv (comp str symbol) vars))]
      (cond->> (all-ns)
        (seq ns-prefix)  (filter #(str/starts-with? % ns-prefix))
        (seq namespaces) (filter #(contains? namespaces-set (str %)))
        ns-meta          (filter #(-> % meta ns-meta))
        true             (mapv ns-interns)
        true             (mapcat vals)
        (seq vars)       (filter #(contains? vars-set (str (symbol %))))
        var-meta         (filter #(-> % meta var-meta))
        true             (sort-by symbol)))))

(defn- linter-hooks
  "Generate hook ns from vars.
  All the arguments and returns are of "
  [vars]
  ['(ns overtone.overtone.clj-kondo-hooks
      (:require [clj-kondo.hooks-api :as api]))

   '(def overloaded-ugens
      '[= < <= * min not= > mod - or / >= + max and])

   '(defn defsynth [{:keys [node]}]
      (let [var-sym (second (:children node))
            binding-vec-or-docstring+body (drop 2 (:children node))
            [_docstring binding-vec body] (if (api/vector-node? (first binding-vec-or-docstring+body))
                                            [nil
                                             (first binding-vec-or-docstring+body)
                                             (vec (rest binding-vec-or-docstring+body))]
                                            [(first binding-vec-or-docstring+body)
                                             (second binding-vec-or-docstring+body)
                                             (vec (drop 2 binding-vec-or-docstring+body))])
            new-node (api/list-node
                      (list
                       (api/token-node 'defn)
                       var-sym
                       (api/coerce '[& _])
                       (api/list-node
                        (list*
                         (api/token-node 'let)
                         (api/coerce (vec (concat (mapcat (fn [v]
                                                            [v (constantly nil)])
                                                          ;; See  `with-overloaded-ugens`.
                                                          overloaded-ugens)
                                                  (:children binding-vec))))
                         ;; "Use" the overloaded ugens so we don't have unused bindings
                         ;; warnings.
                         (api/coerce overloaded-ugens)
                         body))))]
        {:node new-node}))

   '(defn demo [{:keys [node]}]
      (let [body (rest (:children node))
            new-node (api/list-node
                      (list*
                       (api/token-node 'let)
                       (api/coerce (vec (mapcat (fn [v]
                                                  [v (constantly nil)])
                                                ;; See  `with-overloaded-ugens`.
                                                overloaded-ugens)))
                       ;; "Use" the overloaded ugens so we don't have unused bindings
                       ;; warnings.
                       (api/coerce overloaded-ugens)
                       body))]
        {:node new-node}))

   '(defn defunk [{:keys [node]}]
      (let [[var-sym _docstring binding-vec & body] (rest (:children node))
            new-node (api/list-node
                      (list
                       (api/token-node 'defn)
                       var-sym
                       (api/coerce '[& _])
                       (api/list-node
                        (list*
                         (api/token-node 'let)
                         binding-vec
                         body))))]
        {:node new-node}))

   '(defn defunk-env [{:keys [node]}]
      (let [[var-sym docstring binding-vec & body] (rest (:children node))
            new-node (api/list-node
                      (list
                       (api/token-node 'do)
                       (api/list-node
                        (list*
                         (api/token-node 'overtone.helpers.lib/defunk)
                         var-sym
                         docstring
                         binding-vec
                         body))
                       (api/list-node
                        (list*
                         (api/token-node 'overtone.helpers.lib/defunk)
                         (api/token-node (symbol (str "env-" (:string-value var-sym))))
                         docstring
                         binding-vec
                         body))))]
        {:node new-node}))

   '(defn defcgen [{:keys [node]}]
      (let [var-sym (second (:children node))
            [_docstring binding-vec & body] (drop 2 (:children node))
            rates (api/sexpr (->> body
                                  (filter #(and (= (api/tag %) :list)
                                                (contains? #{:ar :ir :kr :dr}
                                                           (:k (first (:children %))))))
                                  (mapv #(:k (first (:children %))))
                                  set))
            new-node (api/list-node
                      (list*
                       (api/token-node 'do)
                       ;; Create one defn for each defined rate + default.
                       (->> (conj rates nil)
                            (mapv (fn [rate]
                                    (api/list-node
                                     (list
                                      (api/token-node 'defn)
                                      (api/token-node (symbol (str (:string-value var-sym)
                                                                   (when rate
                                                                     (str ":" (name rate))))))
                                      (api/coerce '[& _])
                                      (api/list-node
                                       (list*
                                        (api/token-node 'let)
                                        (api/coerce (api/vector-node
                                                     (->> (vec (concat overloaded-ugens (:children binding-vec)))
                                                          api/coerce
                                                          :children
                                                          (filter #(= (api/tag %) :token))
                                                          (mapcat (fn [token]
                                                                    [token
                                                                     (api/list-node
                                                                      (list
                                                                       (api/token-node 'eval)
                                                                       (api/token-node nil)))]))
                                                          vec)))
                                        ;; "Use" the overloaded ugens so we don't have unused bindings
                                        ;; warnings.
                                        (api/coerce overloaded-ugens)
                                        body)))))))))]
        {:node new-node}))

   '(comment

      (-> {:node (api/parse-string
                  (str '(defcgen varlag
                          "Variable shaped lag"
                          [in     {:default 0 :doc "Input to lag"}
                           time   {:default 0.1 :doc "Lag time in seconds"}
                           curvature {:default 0 :doc "Control curvature if shape input is 5 (default). 0 means linear, positive and negative numbers curve the segment up and down."}
                           shape  {:default 5 :doc "Shape of curve. 0: step, 1: linear, 2: exponential, 3: sine, 4: welch, 5: custom (use curvature param), 6: squared, 7: cubed, 8: hold"}

                           ]
                          "Similar to Lag but with other curve shapes than exponential. A change on the input will take the specified time to reach the new value. Useful for smoothing out control signals."
                          (:kr
                           (let [gate (+ (+ (impulse:kr 0 0) (> (abs (hpz1 in)) 0))
                                         (> (abs (hpz1 time)) 0))]
                             (env-gen [in 1 -99 -99 in time shape curvature] gate))))))}
          defcgen
          :node
          api/sexpr)

      ())

   '(defn defrecord-ifn [{:keys [node]}]
      (let [var-sym (second (:children node))
            [fields invoke-fn & body] (drop 2 (:children node))
            new-node (api/list-node
                      (list*
                       (api/token-node 'defrecord)
                       var-sym
                       fields
                       (list*
                        (api/token-node 'Object)
                        invoke-fn
                        body)))]
        {:node new-node}))

   '(defn defsynth-load [{:keys [node]}]
      (let [var-sym (second (:children node))
            string (first (drop 2 (:children node)))
            new-node (api/list-node
                      (list
                       (api/token-node 'defn)
                       var-sym
                       (api/coerce '[& _])
                       string))]
        {:node new-node}))

   '(defn gen-stringed-synth [{:keys [node]}]
      (let [var-sym (second (:children node))
            binding-vec-or-docstring+body (drop 2 (:children node))
            new-node (api/list-node
                      (list
                       (api/token-node 'defn)
                       var-sym
                       (api/coerce '[& _])
                       (api/list-node
                        (list
                         (api/token-node 'eval)
                         (api/token-node nil)))))]
        {:node new-node}))

   '(defn defcheck [{:keys [node]}]
      (let [[var-sym params default-message & body] (rest (:children node))
            body-node (api/list-node
                       (list*
                        (api/token-node 'let)
                        (api/coerce (vec
                                     (mapcat (fn [v] [v (constantly nil)])
                                             '[rate num-outs inputs ugen spec])))
                        ;; "Use" the overloaded ugens so we don't have unused bindings
                        ;; warnings.
                        (api/coerce '[rate num-outs inputs ugen spec])
                        default-message
                        body))
            new-node (api/list-node
                      (list
                       (api/token-node 'defn)
                       var-sym
                       (api/list-node
                        (list
                         params
                         body-node))
                       (api/list-node
                        (list
                         (api/coerce (vec (conj (:children params) 'message)))
                         (api/token-node 'message)
                         body-node))))]
        {:node new-node}))

   '(defn defspec [{:keys [node]}]
      (let [[var-sym & body] (rest (:children node))
            new-node (api/list-node
                      (list
                       (api/token-node 'def)
                       var-sym
                       (api/vector-node body)))]
        {:node new-node}))

   '(defn defexamples [{:keys [node]}]
      (let [[var-sym & body] (rest (:children node))
            new-node (api/list-node
                      (list*
                       (api/token-node 'def)
                       var-sym
                       (map (fn [{:keys [children]}]
                              (let [[name _summary _long-doc _rate-sym _rate _params _body-str _contributor-sym _contributor]
                                    children]
                                name))
                            body)))]
        {:node new-node}))

   '(comment

      (-> {:node (api/parse-string
                  "(defexamples send-reply
        (:count
         \"Short\"
         \"Full\"

         rate :kr
         [rate {:default 3 :doc \"Rate of count in times per second. Increase to up the count rate\"}]
         \"
  (let [tr   (impulse rate)
        step (stepper tr 0 0 12)]
    (send-reply tr \\\"/count\\\" [step] 42))\"
         contributor \"Sam Aaron\"))")}
          defexamples
          :node
          str)

      ())

   '(defn defclib [{:keys [node]}]
      (let [[var-sym & body] (rest (:children node))
            new-node (api/list-node
                      (list
                       (api/token-node 'def)
                       var-sym
                       (api/list-node
                        (list
                         'quote
                         body))))]
        {:node new-node}))

   '(comment

      (str
       (:node
        (defsynth
          {:node
           (api/parse-string "
(defsynth ppp2
  [bus 0
   eita 3
   amp 1]
  (out bus (* amp (mda-piano :freq (midicps 50)
                             #_ #_ #_ #_:release 0.5
                             :decay 0))))")})))

      ())

   (list 'def 'vars (list 'quote
                          (->> vars
                               (mapv (fn [v]
                                       (let [var-meta (meta v)
                                             var-value (deref v)]
                                         [(symbol v)
                                          (merge (select-keys var-meta [:arglists])
                                                 (when (:macro var-meta) {:macro (:macro var-meta)})
                                                 ;; Arglists for synths, insts, ugens and similars
                                                 ;; do not follow Clojure conventions, so for these
                                                 ;; we define a variable arity.
                                                 (when (or (:arglists-modified? var-meta)
                                                           (contains? #{:overtone.sc.machinery.ugen.fn-gen/ugen
                                                                        :overtone.sc.defcgen/cgen}
                                                                      (:type var-value))
                                                           (contains? #{:overtone.helpers.lib/unk}
                                                                      (:type var-meta))
                                                           (instance? overtone.sc.synth.Synth var-value)
                                                           (instance? overtone.studio.inst.Inst var-value))
                                                   {:arglists '([& _])
                                                    :special? true}))])))
                               (into {}))))

   '(defn intern-vars [vars]
      (let [new-node (api/list-node
                      (list*
                       (api/token-node 'do)
                       (->> (keys vars)
                            (group-by namespace)
                            (mapv (fn [[namespace' var-syms]]
                                    (api/list-node
                                     (list*
                                      (api/token-node 'do)
                                      ;; Require namespace.
                                      (api/list-node
                                       (list
                                        (api/token-node 'require)
                                        (api/list-node
                                         (list
                                          (api/token-node 'quote)
                                          (symbol namespace')))))
                                      ;; Define all the vars from a namespace for the current
                                      ;; namespace.
                                      (->> var-syms
                                           (mapv (fn [var-sym]
                                                   ;; Consider macros as `def`s as they have to be
                                                   ;; handled case by case.
                                                   (if (and (not (:macro (get vars var-sym)))
                                                            (seq (:arglists (get vars var-sym))))
                                                     ;; defn
                                                     (api/list-node
                                                      (if (> (count (:arglists (get vars var-sym)))
                                                             1)
                                                        (list*
                                                         (api/token-node 'defn)
                                                         (api/token-node (symbol (name var-sym)))
                                                         (->> (:arglists (get vars var-sym))
                                                              (mapv (fn [arity]
                                                                      (api/list-node
                                                                       (list
                                                                        (api/coerce
                                                                         (if (some #{'&} arity)
                                                                           ['& '_]
                                                                           (vec (repeat (count arity) '_))))))))))
                                                        (list
                                                         (api/token-node 'defn)
                                                         (api/token-node (symbol (name var-sym)))
                                                         (->> (:arglists (get vars var-sym))
                                                              (mapv (fn [arity]
                                                                      (api/coerce
                                                                       (if (some #{'&} arity)
                                                                         ['& '_]
                                                                         (vec (repeat (count arity) '_))))))
                                                              first)
                                                         ;; Use `eval` so we don't have clj-kondo
                                                         ;; trying to infer the return of the
                                                         ;; functions.
                                                         (api/list-node
                                                          (list
                                                           (api/token-node 'eval)
                                                           (api/token-node nil))))))
                                                     ;; def
                                                     (api/list-node
                                                      (list
                                                       (api/token-node 'def)
                                                       (api/token-node (symbol (name var-sym))))))))))))))))]
        {:node new-node}))

   '(defn immigrate-overtone-api [_]
      (intern-vars vars))

   '(defn intern-ugens [_]
      (intern-vars (->> vars
                        (filter (comp #{"overtone.sc.ugens"} namespace first))
                        (into {}))))

   '(comment

      (->> (-> (str (:node (immigrate-overtone-api nil)))
               (clojure.string/split #"require"))
           (remove empty?)
           (mapv #(clojure.string/replace % #"\(do" ""))
           (mapv clojure.string/trim)
           (remove empty?)
           (mapv #(->> (clojure.string/split % #"defn")
                       (mapv clojure.string/trim))))

      ())])

(defn emit!
  "Generate clj-kondo files.

  The generated files should NOT be version-controlled as they are generated
  dynamically and may be huge."
  []
  (let [hooks (linter-hooks
               (->> (find-vars {:namespaces overtone.api/immigrated-namespaces})
                    (remove (comp :private meta))
                    #_(take 10)))
        config '{:lint-as
                 {overtone.music.pitch/defratio
                  clojure.core/def}

                 :hooks
                 {:analyze-call
                  {overtone.api/immigrate-overtone-api
                   overtone.overtone.clj-kondo-hooks/immigrate-overtone-api

                   overtone.sc.synth/demo
                   overtone.overtone.clj-kondo-hooks/demo

                   overtone.core/demo
                   overtone.overtone.clj-kondo-hooks/demo

                   overtone.live/demo
                   overtone.overtone.clj-kondo-hooks/demo

                   overtone.core/defsynth
                   overtone.overtone.clj-kondo-hooks/defsynth

                   overtone.live/defsynth
                   overtone.overtone.clj-kondo-hooks/defsynth

                   overtone.sc.synth/defsynth
                   overtone.overtone.clj-kondo-hooks/defsynth

                   overtone.core/definst
                   overtone.overtone.clj-kondo-hooks/defsynth

                   overtone.live/definst
                   overtone.overtone.clj-kondo-hooks/defsynth

                   overtone.studio.inst/definst
                   overtone.overtone.clj-kondo-hooks/defsynth

                   overtone.core/defsynth-load
                   overtone.overtone.clj-kondo-hooks/defsynth-load

                   overtone.live/defsynth-load
                   overtone.overtone.clj-kondo-hooks/defsynth-load

                   overtone.sc.synth/defsynth-load
                   overtone.overtone.clj-kondo-hooks/defsynth-load

                   overtone.sc.machinery.ugen.fn-gen/intern-ugens
                   overtone.overtone.clj-kondo-hooks/intern-ugens

                   overtone.synth.stringed/gen-stringed-synth
                   overtone.overtone.clj-kondo-hooks/gen-stringed-synth

                   overtone.sc.defcgen/defcgen
                   overtone.overtone.clj-kondo-hooks/defcgen

                   overtone.core/defcgen
                   overtone.overtone.clj-kondo-hooks/defcgen

                   overtone.live/defcgen
                   overtone.overtone.clj-kondo-hooks/defcgen

                   overtone.helpers.lib/defrecord-ifn
                   overtone.overtone.clj-kondo-hooks/defrecord-ifn

                   overtone.sc.machinery.ugen.check/defcheck
                   overtone.overtone.clj-kondo-hooks/defcheck

                   overtone.byte-spec/defspec
                   overtone.overtone.clj-kondo-hooks/defspec

                   clj-native.direct/defclib
                   overtone.overtone.clj-kondo-hooks/defclib

                   overtone.sc.machinery.defexample/defexamples
                   overtone.overtone.clj-kondo-hooks/defexamples

                   overtone.helpers.lib/defunk
                   overtone.overtone.clj-kondo-hooks/defunk

                   overtone.sc.envelope/defunk-env
                   overtone.overtone.clj-kondo-hooks/defunk-env}}

                 :config-in-ns
                 {overtone.studio.util
                  {:linters {:inline-def {:level :off}}}

                  overtone.studio.mixer
                  {:linters {:inline-def {:level :off}}}

                  overtone.sc.ugens
                  {:linters {:inline-def {:level :off}}}}}
        cfg-file (io/file ".clj-kondo" "overtone" "do_not_commit_me" "config.edn")
        hooks-file (io/file ".clj-kondo" "overtone" "do_not_commit_me" "overtone" "overtone" "clj_kondo_hooks.clj")]
    (io/make-parents cfg-file)
    (io/make-parents hooks-file)
    (spit cfg-file (with-out-str (pp/pprint config)))
    (spit hooks-file (->> hooks
                          (mapv #(with-out-str (pp/pprint %)))
                          (str/join "\n")))
    (str cfg-file)))

#_(emit!)

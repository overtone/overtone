(ns overtone.helpers.ns)

(set! *warn-on-reflection* true)

(defn immigrate
  "Create a public var in this namespace for each public var in the
  namespaces named by ns-names. The created vars have the same name, value
  and metadata as the original except that their :ns metadata value is this
  namespace."
  [& ns-names]
  (doseq [ns ns-names]
    (doseq [[sym ^clojure.lang.Var var] (ns-publics ns)]
      (when-some [^clojure.lang.Var prev (resolve sym)]
        (when-not (var? prev)
          (throw (Exception. (str "Overriding non-var " prev " in " (ns-name *ns*)))))
        (let [prev-ns (-> prev meta :orig-ns)]
          (when-not (= prev-ns ns)
            (println (str "WARNING: replacing " sym " in " (ns-name *ns*)
                          " with " (symbol (name ns) (name sym)) 
                          ", previously "
                          (if prev-ns
                            (symbol (name prev-ns) (name sym))
                            (str " an unknown var "
                                 (when (.isBound prev)
                                   (str " with binding " @prev)))))))))
      (let [sym (with-meta sym (assoc (meta var) :orig-ns ns))]
        (when-not (.isBound var)
          (throw (Exception. (str var " not bound!"))))
        (intern *ns* sym (if (fn? (var-get var))
                           var
                           (var-get var)))))))

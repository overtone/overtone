(ns overtone.helpers.ns)

(defn immigrate
  "Create a public var in this namespace for each public var in the
  namespaces named by ns-names. The created vars have the same name, value
  and metadata as the original except that their :ns metadata value is this
  namespace."
  [& ns-names]
  (doseq [ns ns-names]
    (doseq [[sym ^clojure.lang.Var var] (ns-publics ns)]
      (let [sym (with-meta sym (assoc (meta var) :orig-ns ns))]
        (if (.isBound var)
          (intern *ns* sym (if (fn? (var-get var))
                             var
                             (var-get var)))
          (intern *ns* sym))))))

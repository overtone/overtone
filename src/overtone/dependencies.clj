(ns
    ^{:doc "A basic dependency system for specifying the execution of fns on or after the
            dependency has been met."
      :author "Sam Aaron"}
  overtone.dependencies)

(defonce completed-dependencies* (ref #{}))
(defonce registered-handlers* (ref {}))

(defn on-dependency
  "Specify that a given fun should only be called after a specific dependency has been satisfied. If the
   dependency has been satisfied, the fun is immediately executed. If not, it is registered to be executed
   within the thread that satisfies the dependency with satisfy-dependency"
  [dep fun]
  (dosync
   (ensure completed-dependencies*)
   (if (some #{dep} @completed-dependencies*)
     (fun)
     (let [handlers (get @registered-handlers* dep [])]
       (alter registered-handlers* assoc dep (conj handlers fun))))))

(defn satisfy-dependency
  "Specifies that a given dependency has been satisfied. This will also synchronously execute all handlers waiting
   for this dependency to be satisfied."
  [dep]
  (let [handlers (dosync
                  (alter completed-dependencies* conj dep)
                  (let [deps-handlers (get @registered-handlers* dep [])]
                    (alter registered-handlers* dissoc dep)
                    deps-handlers))]
    (doseq [h handlers] (h))))

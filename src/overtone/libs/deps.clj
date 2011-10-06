(ns
    ^{:doc "A basic dependency system for specifying the execution of
           fns once dependencies have been met."
      :author "Sam Aaron & Jeff Rose"}
  overtone.libs.deps
  (:require [clojure.set :as set]))

(defonce dep-state* (agent {:satisfied #{}
                            :todo []
                            :done []}))

(defn- process-handler
  "Returns a new deps map containing either processed handler or it placed in
  the todo list"
  [dep-state key deps task]
  (apply assoc dep-state
         (if (set/superset? (:satisfied dep-state) deps)
           (do
             (task)
             [:done (conj (:done dep-state)
                          [key deps task])])

           [:todo (conj (:todo dep-state)
                        [key deps task])])))

(defn- replace-handler
  "Replace all occurances of handers with the given key with the new handler
  and deps set"
  [dep-state key deps task]
  (let [replacer-fn #(if (= key (first %))
                       [key deps task]
                       %)]
    {:satisfied (dep-state :satisfied)
     :todo (map replacer-fn (dep-state :todo))
     :done (map replacer-fn (dep-state :done))}))

(defn- key-known?
  "Returns true or false depending on whether this key is associated with a
  handler in either the completed or todo lists."
  [dep-state key]
  (some #(= key (first %)) (concat (:done dep-state) (:todo dep-state))))

(defn- on-deps*
  "If a handler with this key has already been registered, just replace the
  handler - either in todo or completed. If the key is unknown, then either
  execute the handler if the deps are satisfied or add it to the todo list"
  [dep-state key deps task]
  (if (key-known? dep-state key)
    (replace-handler dep-state key deps task)
    (process-handler dep-state key deps task)))

(defn- satisfy*
  [{:keys [satisfied todo done]} new-deps]
  (let [satisfied (set/union satisfied new-deps)
        execute-tasks (fn [[final-done final-todo] [key deps task]]
                        (if (set/superset? satisfied deps)
                          (do
                            (task)
                            [(conj final-done [key deps task]) final-todo])
                          [final-done (conj final-todo [key deps task])]))
        [t-done t-todo] (reduce execute-tasks [done []] todo)]
    {:satisfied satisfied
     :done t-done
     :todo t-todo}))

(defn on-deps
  "Specify that a function should be called once one or more dependencies
  have been satisfied. The function is run immediately if the deps have
  already been satisfied, otherwise it will run as soon as they are.

  If a dep handler has already been registered with the same key, a second
  registration with just replace the first.

  Uses an agent so it's safe to call this from within a transaction."
  [deps key handler]
  (let [deps (if (coll? deps)
               (set deps)
               (set [deps]))]
    (send-off dep-state* on-deps* key deps handler)))

(defn satisfy-deps
  "Specifies that a given dependency has been satisfied. Uses an agent so it's
  safe to call this from within a transaction."
  [& deps]
  (send-off dep-state* satisfy* (set deps)))

(defn reset-deps
  "Reset the dependency system. Uses an agent so it's safe to call this from
  within a transaction."
  []
  (send dep-state* (fn [& args]
                     {:satisfied #{}
                      :todo []
                      :done []})))

(defn unsatisfy-all-dependencies
  "Unsatisfy all deps and reset completed tasks as todo tasks. Uses an agent so
  it's safe to call this from within a transaction."
  []
  (send dep-state* (fn [deps]
                     {:satisfied #{}
                      :todo (deps :done)
                      :done []})))

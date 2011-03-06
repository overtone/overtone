(ns
    ^{:doc "A basic dependency system for specifying the execution of fns on or after the
            dependency has been met."
      :author "Sam Aaron & Jeff Rose"}
  overtone.deps
  (:require [clojure.set :as set]))

(defonce completed-dependencies* (ref #{}))
(defonce registered-handlers* (ref {}))

(defn on-dependency
  "Specify that a given fun should only be called after a specific dependency has been satisfied. If the
   dependency has been satisfied, the fun is immediately executed. If not, it is registered to be executed
   within the thread that satisfies the dependency with satisfy-dependency"
  [dep fun]
  (dosync
   (ensure completed-dependencies*)
   (if (contains? @completed-dependencies* dep)
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

(defonce deps* (agent {:satisfied #{}
                       :tasks []
                       :completed []}))

(defn- on-dep*
  [cur-deps task-deps task]
  (apply assoc cur-deps
         (if (set/superset? cur-deps task-deps)
           (do
             (task)
             [:completed (conj (:completed cur-deps)
                               [task-deps task])])
           [:tasks (conj (:tasks cur-deps)
                         [task-deps task])])))

(defn- satisfy*
  [{:keys [satisfied tasks]} new-deps]
  (let [satisfied (set/union satisfied new-deps)
        [t-done t-todo]
        (reduce
          (fn [[done todo] [task-deps task]]
                (if (set/superset? satisfied task-deps)
                  (do
                    (println "running: " task-deps)
                    (task)
                    [(conj done [task-deps task]) todo])
                  [done (conj todo [task-deps task])]))
          [[] []]
          tasks)]
    {:satisfied satisfied
     :tasks t-todo
     :completed t-done}))

(defn on-deps
  [deps handler]
  (send-off deps* on-dep*
            (if (coll? deps)
              (set deps)
              (set [deps]))
              handler))

(defn satisfy-deps
  [& deps]
  (send-off deps* satisfy* (set deps)))

(defn reset-deps
  []
  (send deps* (fn [& args]
                {:satisfied #{}
                 :tasks []
                 :completed []})))

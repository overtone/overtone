(ns
    ^{:doc "A basic dependency system for specifying the execution of 
           fns once dependencies have been met."
      :author "Sam Aaron & Jeff Rose"}
  overtone.deps
  (:require [clojure.set :as set]))

(defonce deps* (agent {:satisfied #{}
                       :tasks []
                       :completed []}))

(defn- on-deps*
  [cur-deps task-deps task]
  (apply assoc cur-deps
         (if (set/superset? (:satisfied cur-deps) task-deps)
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
                    (task)
                    [(conj done [task-deps task]) todo])
                  [done (conj todo [task-deps task])]))
          [[] []]
          tasks)]
    {:satisfied satisfied
     :tasks t-todo
     :completed t-done}))

(defn on-deps
  "Specify that a function should be called once one or more dependencies 
  have been satisfied. The function is run immediately if the deps have 
  already been satisfied, otherwise it will run as soon as they are."
  [deps handler]
  (send-off deps* on-deps*
            (if (coll? deps)
              (set deps)
              (set [deps]))
              handler))

(defn satisfy-deps
  "Specifies that a given dependency has been satisfied."
  [& deps]
  (send-off deps* satisfy* (set deps)))

(defn reset-deps
  "Reset the dependency system."
  []
  (send deps* (fn [& args]
                {:satisfied #{}
                 :tasks []
                 :completed []})))

(defn unsatisfy-all-dependencies
  "Unsatisfy all deps and reset completed tasks as todo tasks"
  []
  (send deps* (fn [deps]
                {:satisfied #{}
                 :tasks (deps :completed)
                 :completed []})))

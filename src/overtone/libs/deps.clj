(ns
    ^{:doc "A basic dependency system for specifying the execution of
           fns once dependencies have been met."
      :author "Sam Aaron & Jeff Rose"}
  overtone.libs.deps
  (:require [clojure.set :as set]
            [overtone.config.log :as log]))

;; A representation of the state of the dependencies:
;; :satisified - a set of keywords representing ids for each of the satisfied
;;               dependencies
;; :todo       - a list of functions with associated dependencies which need
;;               to be executed when those dependencies are met
;; :done       - a list of functions with associated dependencies which have
;;               already been executed as their dependencies have been met
(defonce dep-state* (agent {:satisfied #{}
                            :todo      []
                            :done      []
                            :history   []}))

(defn- now
  []
  (System/currentTimeMillis))

(defn- process-handler
  "Returns a new deps map containing either processed handler or it
  placed in the todo list"
  [dep-state key deps task]
  (apply assoc dep-state
         (if (set/superset? (:satisfied dep-state) deps)
           (do
             (task)
             [:done (conj (:done dep-state)
                          [key deps task])
              :history (conj (:history dep-state)
                             {:action :processed
                              :ts     (now)
                              :key    key
                              :deps   deps})])

           [:todo (conj (:todo dep-state)
                        [key deps task])
            :history (conj (:history dep-state)
                           {:action :registered-todo
                            :ts     (now)
                            :key    key
                            :deps   deps})])))

(defn- replace-handler
  "Replace all occurances of handers with the given key with the new
  handler and deps set"
  [dep-state key deps task]
  (let [replacer-fn #(if (= key (first %))
                       [key deps task]
                       %)]
    {:satisfied (:satisfied dep-state)
     :todo      (map replacer-fn (:todo dep-state))
     :done      (map replacer-fn (:done dep-state))
     :history   (:history dep-state)}))

(defn- key-known?
  "Returns true or false depending on whether this key is associated
  with a handler in either the completed or todo lists."
  [dep-state key]
  (some #(= key (first %)) (concat (:done dep-state) (:todo dep-state))))

(defn- on-deps*
  "If a handler with this key has already been registered, just replace
  the handler - either in todo or completed. If the key is unknown, then
  either execute the handler if the deps are satisfied or add it to the
  todo list"
  [dep-state key deps task]
  (if (key-known? dep-state key)
    (replace-handler dep-state key deps task)
    (process-handler dep-state key deps task)))

(defn- satisfy*
  [{:keys [satisfied todo done] :as dep-state} new-deps]
  (let [satisfied       (set/union satisfied new-deps)
        execute-tasks   (fn [[final-done final-todo new-history] [key deps task]]
                          (if (set/superset? satisfied deps)
                            (do
                              (log/info "Running dep handler: " key)
                              (try
                                (task)
                                (catch Exception e
                                  (log/error (format "Exception in dependency handler: %s\n%s"
                                                     key
                                                     (with-out-str (.printStackTrace e))))))
                              [(conj final-done [key deps task]) final-todo (conj new-history {:ts (now)
                                                                                               :action :satisfied-and-processed
                                                                                               :key key
                                                                                               :deps deps})])
                            [final-done (conj final-todo [key deps task]) new-history]))
        [t-done t-todo new-history] (reduce execute-tasks [done [] []] todo)]
    (log/info "deps-satisfied: " satisfied)
    {:satisfied satisfied
     :done      t-done
     :todo      t-todo
     :history (concat (:history dep-state)
                      new-history)}))

(defn- deps->set
  "Converts deps to a deps-set. Deals with single elements or
   collections."
  [deps]
  (if (coll? deps)
    (set deps)
    (set [deps])))

(defn on-deps
  "Specify that a function should be called once one or more
   dependencies have been satisfied. The function is run immediately if
   the deps have already been satisfied, otherwise it will run as soon
   as they are.

   If a dep handler has already been registered with the same key, a
   second registration with just replace the first.

   Uses an agent so it's safe to call this from within a transaction."
  [deps key handler]
  (let [deps (deps->set deps)]
    (send-off dep-state* on-deps* key deps handler)))

(defn satisfy-deps
  "Specifies that a list of dependencies have been satisfied. Uses an
   agent so it's safe to call this from within a transaction."
  [& deps]
  (log/info (format "satisfying deps: %s" deps))
  (send-off dep-state* satisfy* (set deps)))

(defn reset-deps
  "Reset the dependency system. Uses an agent so it's safe to call this
   from within a transaction."
  []
  (send dep-state* (fn [dep-state]
                     {:satisfied #{}
                      :todo      []
                      :done      []
                      :history   (conj (:history dep-state)
                                       {:ts (now)
                                        :action :reset})})))

(defn unsatisfy-all-dependencies
  "Unsatisfy all deps and reset completed tasks as todo tasks. Uses an
   agent so it's safe to call this from within a transaction."
  []
  (send dep-state* (fn [deps]
                     {:satisfied #{}
                      :todo      (concat (deps :todo) (deps :done))
                      :done      []
                      :history   (conj (:history deps)
                                       {:ts (now)
                                        :action :unsatisfy-all-dependencies})})))

(defn satisfied-deps
  "Returns a set of all satisfied deps"
  []
  (:satisfied @dep-state*))

(defn deps-satisfied?
  "Returns true if all the deps (specified either as a single dep or a
  collection of deps) have been satisfied."
  [deps]
  (let [deps (deps->set deps)]
    (set/superset? (satisfied-deps) deps)))

(defn wait-until-deps-satisfied
  "Makes the current thread sleep until specified deps have been satisfied.
   Thread enters a sleep cycle sleeping for wait-time seconds before
   each dep check.  If timeout is a positive value throws timeout
   exception if deps haven't been satisfied by timeout secs. The default
   wait-time is 0.1 seconds, and the default timeout is 20 seconds."
  ([deps] (wait-until-deps-satisfied deps 20 0.1))
  ([deps timeout] (wait-until-deps-satisfied deps timeout 0.1))
  ([deps timeout wait-time]
     (let [timeout-ms (* 1000 timeout)
           wait-time  (* 1000 wait-time)]
       (if (<= timeout-ms 0)
         (while (not (deps-satisfied? deps))
           (Thread/sleep wait-time))
         (loop [sleep-time 0]
           (when (> sleep-time timeout-ms)
             (throw (Exception. (str "The following deps took too long (" timeout
                                     " seconds) to be satisfied: " deps))))
           (when-not (deps-satisfied? deps)
             (Thread/sleep wait-time)
             (recur (+ sleep-time wait-time))))))))

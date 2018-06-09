(ns
    ^{:doc "Basic stateful keyword indexed integer counters"
      :author "Jeff Rose and Sam Aaron"}
    overtone.libs.counters)

(defonce counters* (atom {}))

(defn- inc-or-set-cnt
  "Either increment the count associated with the specified key in map counters
  or create a new key with val 0"
  [counters key]
  (let [count (or (get counters key) -1)]
    (assoc counters key (inc count))))

(defn next-id
  "Increments and returns the next integer for the specified key. ids start at 0
  and each key's id is independent of the other ids."
  [key]
  (let [updated (swap! counters* inc-or-set-cnt key)]
    (get updated key)))

(defn reset-counter!
  "Reset specified counter"
  [key]
  (swap! counters* dissoc key))

(defn reset-all-counters!
  "Reset all counters"
  []
  (reset! counters* {}))

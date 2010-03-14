(ns overtone.core.event
  (:import (java.util.concurrent Executors LinkedBlockingQueue))
  (:use (overtone.core util)))

(def NUM-THREADS (cpu-count))
(defonce thread-pool (Executors/newFixedThreadPool NUM-THREADS))
(defonce event-handlers* (ref {}))

(defn on [event-type handler]
  (dosync 
    (let [handlers (get @event-handlers* event-type [])]
      (alter event-handlers* assoc event-type (conj handlers handler)))))

(defn remove-handler [event-type handler]
  (dosync
    (let [handlers (get @event-handlers* event-type [])]
      (alter event-handlers* assoc event-type (filter #(not (= handler %)) handlers)))))

(defn clear-handlers [event-type]
  (dosync (alter event-handlers* dissoc event-type)))

(defn- handle-event [event]
  (doseq [handler (get @event-handlers* (:event event) [])]
    (if (zero? (arg-count handler))
      (handler)
      (handler event))))

(defn event [event-type & args]
  (.execute thread-pool #(handle-event (apply hash-map :event event-type args))))

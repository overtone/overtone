(ns overtone.core.event
  (:import (java.util.concurrent Executors LinkedBlockingQueue))
  (:use (overtone.core util)))

(def NUM-THREADS (cpu-count))
(defonce thread-pool (Executors/newFixedThreadPool NUM-THREADS))
(defonce event-handlers* (ref {}))

(defn on 
  "Runs handler whenever events of type event-type are fired.  The handler can 
  optionally except a single event argument, which is a map containing the 
  :event-type property and any other properties specified when it was fired.
  
  (on ::booted #(do-stuff))
  (on ::midi-note-down (fn [event] (funky-bass (:note event))))"
  [event-type handler]
  (dosync 
    (let [handlers (get @event-handlers* event-type [])]
      (alter event-handlers* assoc event-type (conj handlers handler)))))

(defn remove-handler 
  "Remove an event handler previously registered to handle events of event-type.

  (defn my-foo-handler [event] (do-stuff (:val event)))
  
  (on ::foo my-foo-handler)
  (event ::foo :val 200) ; my-foo-handler gets called with {:event-type ::foo :val 200}
  (remove-handler ::foo my-foo-handler)
  (event ::foo :val 200) ; my-foo-handler no longer called
  "
  [event-type handler]
  (dosync
    (let [handlers (get @event-handlers* event-type [])]
      (alter event-handlers* assoc event-type (filter #(not (= handler %)) handlers)))))

(defn clear-handlers 
  "Remove all handlers for events of type event-type."
  [event-type]
  (dosync (alter event-handlers* dissoc event-type)))

(defn- handle-event [event]
  (doseq [handler (get @event-handlers* (:event-type event) [])]
    (if (zero? (arg-count handler))
      (handler)
      (handler event))))

(defn event 
  "Fire an event of type event-type with any number of additional properties.

  (event ::my-event)
  (event ::filter-sweep-done :instrument :phat-bass)"
  [event-type & args]
  (.execute thread-pool #(handle-event (apply hash-map :event-type event-type args))))

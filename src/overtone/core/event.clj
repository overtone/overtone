(ns overtone.core.event
  (:import (java.util.concurrent Executors LinkedBlockingQueue))
  (:use (overtone.core util)
        [clojure.set :only [intersection difference]]))

(def NUM-THREADS (cpu-count))
(defonce thread-pool (Executors/newFixedThreadPool NUM-THREADS))
(defonce event-handlers* (ref {}))

(defn on 
  "Runs handler whenever events of type event-type are fired.  The handler can 
  optionally except a single event argument, which is a map containing the 
  :event-type property and any other properties specified when it was fired.
  
  (on ::booted #(do-stuff))
  (on ::midi-note-down (fn [event] (funky-bass (:note event))))
  
  Handlers can return :done to be removed from the handler list after execution."
  [event-type handler]
  ;(println "adding-handler for " event-type)
  (dosync 
    (let [handlers (get @event-handlers* event-type #{})]
      (alter event-handlers* assoc event-type (conj handlers handler))
      true)))

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
    (let [handlers (get @event-handlers* event-type #{})]
      (alter event-handlers* assoc event-type (difference handlers #{handler})))))

(defn clear-handlers 
  "Remove all handlers for events of type event-type."
  [event-type]
  (dosync (alter event-handlers* dissoc event-type)))

(defn- run-handler [handler event]
  (if (zero? (arg-count handler))
    (handler)
    (handler event)))

(defn- handle-event 
  "Runs the event handlers for the given event, and removes any handler that returns :done."
  [event]
  (let [event-type (:event-type event)
        handlers (get @event-handlers* event-type #{})
        ;_ (println (format "handle-event[%d]: %s" (count handlers) event-type) (keys event))
        keepers  (set (doall (filter #(not (= :done (run-handler % event))) handlers)))]
    ;(println "handled with " (count keepers) "keepers")
    (dosync (alter event-handlers* assoc event-type 
                   (intersection keepers (get @event-handlers* event-type #{}))))
    ;(println "finished handling " event-type "with" (count (get @event-handlers* event-type #{})))
    ))

(defn event 
  "Fire an event of type event-type with any number of additional properties.
  NOTE: an event requires key/value pairs, and everything gets wrapped into an
  event map.  It will not work if you just pass values.

  (event ::my-event)
  (event ::filter-sweep-done :instrument :phat-bass)"
  [event-type & args]
  {:pre [(even? (count args))]}
  (.execute thread-pool #(handle-event (apply hash-map :event-type event-type args))))

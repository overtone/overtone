(ns overtone.sc.trig
  (:use [overtone.libs event counters]
        [overtone.sc.node]))

(on-sync-event "/tr"
               (fn [{path :path args :args}]
                 (let [[node-id trig-id val] args]
                   (event [:overtone :trigger trig-id] :val val)
                   (event [:overtone :trigger node-id trig-id] :val val)))
               ::forward-trigger-event)

(defn trig-id
  "Returns a new globally unique id useful for feeding into send-trig
   and matching on the event stream.

   (def uid (trig-id))
   (defsynth foo [] (send-trig (impulse 10) uid (sin-osc)))
   (on-trigger uid
               (fn [val] (println \"trig val:\" val)))
   (foo)"
  []
  (next-id ::trig-id))

(defn- add-handler
  [handler-type event-pattern handler-fn key]
  (handler-type event-pattern
                (fn [{val :val}]
                  (handler-fn val))
                key))

(defn on-trigger
  "Registers a standard on-event handler with key which will call
   handler when matching triggers are recieved. Triggers are created
   with the send-trig ugen. Handler should be a fn which takes one
   argument - the latest trigger value."
  ([trig-id handler key]
     (add-handler on-event [:overtone :trigger trig-id] handler key))
  ([node trig-id handler key]
     (add-handler on-event [:overtone :trigger (to-sc-id node) trig-id] handler key)))

(defn on-latest-trigger
  "Registers a standard on-latest-event handler with key which will call
   handler when matching triggers are recieved. Triggers are created
   with the send-trig ugen. Handler should be a fn which takes one
   argument - the latest trigger value."
  ([trig-id handler key]
     (add-handler on-latest-event [:overtone :trigger trig-id] handler key))
  ([node trig-id handler key]
     (add-handler on-latest-event [:overtone :trigger (to-sc-id node) trig-id] handler key)))

(defn on-sync-trigger
  "Registers a standard on-sync-event handler with key which will call
   handler when matching triggers are recieved. Triggers are created
   with the send-trig ugen. Handler should be a fn which takes one
   argument - the latest trigger value."
  ([trig-id handler key]
     (add-handler on-sync-event [:overtone :trigger trig-id] handler key))
  ([node trig-id handler key]
     (add-handler on-sync-event [:overtone :trigger (to-sc-id node) trig-id] handler key)))

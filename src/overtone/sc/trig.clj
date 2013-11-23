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

   See on-trigger docstring for usage example."
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
   argument - the latest trigger value. Triggers registered with the
   same key as another trigger or standard handler will remove and
   replace the old handler.

   Consider using trig-id to create a unique trigger id

    ;; create new id
    (def uid (trig-id))

    ;; define a synth which uses send-trig
    (defsynth foo
              [t-id 0]
              (send-trig (impulse 10) t-id (sin-osc)))

    ;; register a handler fn
    (on-trigger uid
                (fn [val] (println \"trig val:\" val))
                ::debug)

    ;; create a new instance of synth foo with trigger id as a
    ;; param
    (foo uid)

    ;;Trigger handler can be removed with:
    (remove-event-handler ::debug)"
  ([trig-id handler key]
     (add-handler on-event [:overtone :trigger trig-id] handler key))
  ([node trig-id handler key]
     (add-handler on-event [:overtone :trigger (to-sc-id node) trig-id] handler key)))

(defn on-latest-trigger
  "Registers a standard on-latest-event handler with key which will call
   handler when matching triggers are recieved. Triggers are created
   with the send-trig ugen. Handler should be a fn which takes one
   argument - the latest trigger value. Triggers registered with the
   same key as another trigger or standard handler will remove and
   replace the old handler.

   Consider using trig-id to create a unique trigger id. See on-trigger
   docstring for usage example.

   Trigger handler can be removed with remove-event-handler."
  ([trig-id handler key]
     (add-handler on-latest-event [:overtone :trigger trig-id] handler key))
  ([node trig-id handler key]
     (add-handler on-latest-event [:overtone :trigger (to-sc-id node) trig-id] handler key)))

(defn on-sync-trigger
  "Registers a standard on-sync-event handler with key which will call
   handler when matching triggers are recieved. Triggers are created
   with the send-trig ugen. Handler should be a fn which takes one
   argument - the latest trigger value. Triggers registered with the
   same key as another trigger or standard handler will remove and
   replace the old handler.

   Consider using trig-id to create a unique trigger id. See on-trigger
   docstring for usage example.

   Trigger handler can be removed with remove-event-handler."
  ([trig-id handler key]
     (add-handler on-sync-event [:overtone :trigger trig-id] handler key))
  ([node trig-id handler key]
     (add-handler on-sync-event [:overtone :trigger (to-sc-id node) trig-id] handler key)))

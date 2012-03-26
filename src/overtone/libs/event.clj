(ns
  ^{:doc "A simple event system that processes fired events in a thread pool."
     :author "Jeff Rose, Sam Aaron"}
  overtone.libs.event
  (:require [overtone.util.log :as log]
            [overtone.libs.handlers :as handlers]))

(defonce handler-pool (handlers/mk-handler-pool "Overtone Event Handlers"))

(defn on-event
  "Asynchronously runs handler whenever events of event-type are
  fired. This asynchronous behaviour can be overridden if required -
  see sync-event for more information. Events may be triggered with
  the fns event and sync-event.

  Takes an event-type (name of the event), a handler fn and a key (to
  refer back to this handler in the future). The handler can
  optionally accept a single event argument, which is a map containing
  the :event-type property and any other properties specified when it
  was fired.

  (on-event \"/tr\" handler ::status-check )
  (on-event :midi-note-down (fn [event]
                              (funky-bass (:note event)))
                            ::midi-note-down-hdlr)

  Handlers can return :overtone/remove-handler to be removed from the
  handler list after execution."
  [event-type handler key]
  (log/debug "Registering async event handler:: " event-type key)
  (handlers/add-handler handler-pool event-type key handler ))

(defn on-sync-event
  "Synchronously runs handler whenever events of type event-type are
  fired on the event handling thread i.e. causes the event handling
  thread to block until all sync events have been handled. Events may
  be triggered with the fns event and sync-event.

  Takes an event-type (name of the event), a handler fn and a key (to
  refer back to this handler in the future). The handler can
  optionally accept a single event argument, which is a map containing
  the :event-type property and any other properties specified when it
  was fired.

  (on-event \"/tr\" handler ::status-check )
  (on-event :midi-note-down (fn [event]
                              (funky-bass (:note event)))
                            ::midi-note-down-hdlr)

  Handlers can return :overtone/remove-handler to be removed from the
  handler list after execution."
  [event-type handler key]
  (log/debug "Registering sync event handler:: " event-type key)
  (handlers/add-sync-handler handler-pool event-type key handler))

(defn remove-handler
  "Remove an event handler previously registered to handle events of
  event-type.  Removes both sync and async handlers with a given key
  for a particular event type.

  (defn my-foo-handler [event] (do-stuff (:val event))

  (on-event :foo my-foo-handler ::bar-key)
  (event :foo :val 200) ; my-foo-handler gets called with:
                        ; {:event-type :foo :val 200}
  (remove-handler :foo ::bar-key)
  (event :foo :val 200) ; my-foo-handler no longer called"
  [event-type key]
  (handlers/remove-handler handler-pool event-type key))

(defn remove-all-handlers
  "Remove all handlers (both sync and async) for events of type
  event-type."
  [event-type]
  (handlers/remove-event-handlers handler-pool event-type))

(defn event
  "Fire an event of type event-type with any number of additional
  properties.

  NOTE: an event requires key/value pairs, and everything gets wrapped
  into an event map.  It will not work if you just pass values.

  (event ::my-event)
  (event ::filter-sweep-done :instrument :phat-bass)"
  [event-type & args]
  (log/debug "event: " event-type " " args)
  (binding [overtone.libs.handlers/*log-fn* log/error]
    (apply handlers/event handler-pool event-type args)))

(defn sync-event
  "Runs all event handlers synchronously of type event-tye regardless
  of whether they were declared as async or not. If handlers create
  new threads which generate events, these will revert back to the
  default behaviour of event (i.e. not forced sync). See event."
  [event-type & args]
  (log/debug "sync-event: " event-type args)
  (binding [overtone.libs.handlers/*log-fn* log/error]
    (apply handlers/sync-event handler-pool event-type args)))

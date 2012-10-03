(ns
  ^{:doc "A simple event system that processes fired events in a thread pool."
     :author "Jeff Rose, Sam Aaron"}
  overtone.libs.event
  (:import [java.util.concurrent LinkedBlockingQueue])
  (:use [overtone.helpers.ref :only [swap-returning-prev!]])
  (:require [overtone.config.log :as log]
            [overtone.libs.handlers :as handlers]))

(defonce ^:private handler-pool (handlers/mk-handler-pool "Overtone Event Handlers"))
(defonce ^:private event-debug* (atom false))
(defonce ^:private monitoring?* (atom false))
(defonce ^:private monitor* (atom {}))
(defonce ^:private lossy-workers* (atom {}))
(defonce ^:private log-events? (atom false))

(defn- log-event
  "Log event on separate thread to ensure logging doesn't interfere with
  event handling latency"
  [msg & args]
  (future (apply log/debug msg args)))

(defrecord LossyWorker [queue worker current-val])

(defn- lossy-worker
  "Create a lossy worker which will call update-fn on a separate thread
  when lossy-send is called. Calls to update-fn happen sequentially,
  however unlike an agent, update-fn is not guaranteed to be called for
  every lossy-send. Whilst update-fn is executing, if n lossy-sends are
  received, only the latest lossy-send results in a new call to
  update-fn - all the intermediate lossy-sends are dropped. update-fn is
  also not called if the new-val sent with lossy-send is the same as the
  previous value. This allows update-fn to be always responsive of the
  latest value.

  Do not place any watchers on the current-val atom of the returned
  LossyWorker as the intention is to not block the calling thread any
  more than necessary."
  [update-fn]
  (let [current-val* (atom nil)
        last-val*    (atom (gensym))
        queue        (LinkedBlockingQueue.)
        work         (fn []
                       (while (not= (.take queue) :die)
                         (let [current @current-val*
                               last @last-val*]
                           (when (not= current last)
                             (update-fn current)
                             (reset! last-val* current))))
                       (log-event "Killing Lossy worker"))
        worker       (Thread. work)]
    (.start worker)
    (LossyWorker. queue worker current-val*)))

(defn- lossy-send
  "Send a new value to a lossy worker. May or may not result in the
  lossy worker calling its update-fn depending on its current load as
  intermediate calls may be dropped. Also, duplicate new-vals will also
  be dropped. The last non-duplicate val sent to the lossy-worker is
  guaranteed to trigger the update-fn provided it isn't perpetually
  blocked."
  [lossy-worker new-val]
  (reset! (:current-val lossy-worker) new-val)
  (.put (:queue lossy-worker) :job))

(defn on-event
  "Asynchronously runs handler whenever events of event-type are
  fired. This asynchronous behaviour can be overridden if required -
  see sync-event for more information. Events may be triggered with
  the fns event and sync-event.

  Takes an event-type (name of the event), a handler fn and a key (to
  refer back to this handler in the future). The handler must accept a
  single event argument, which is a map containing the :event-type
  property and any other properties specified when it was fired.

  (on-event \"/tr\" handler ::status-check )
  (on-event :midi-note-down (fn [event]
                              (funky-bass (:note event)))
                            ::midi-note-down-hdlr)

  Handlers can return :overtone/remove-handler to be removed from the
  handler list after execution."
  [event-type handler key]
  (log-event "Registering async event handler:: " event-type " with key: " key)
  (handlers/add-handler! handler-pool event-type key handler ))

(defn on-sync-event
  "Synchronously runs handler whenever events of type event-type are
  fired on the thread that generated the event (by calling ether event
  or event-sync). Note, this causes the event-generating thread to block
  whilst this handler is being handled. For a non-blocking event handler
  see on-event.


  Takes an event-type (name of the event), a handler fn and a key (to
  refer back to this handler in the future). The handler must accept a
  single event argument, which is a map containing the :event-type
  property and any other properties specified when it was fired.

  (on-event \"/tr\" handler ::status-check )
  (on-event :midi-note-down (fn [event]
                              (funky-bass (:note event)))
                            ::midi-note-down-hdlr)

  Handlers can return :overtone/remove-handler to be removed from the
  handler list after execution."
  [event-type handler key]
  (log-event "Registering sync event handler:: " event-type " with key: " key)
  (handlers/add-sync-handler! handler-pool event-type key handler))

(defn on-latest-event
  "Runs handler on a separate thread to the thread that generated the
  event - however event order is preserved per thread similar to
  on-sync-event. However, only the last matching event will trigger the
  handler with all intermediate events being dropped if the handler fn
  is still busy executing.

  *Warning* - is not guaranteed to be triggered for all matching events.

  Useful for low-latency sequential handling of events despite
  potentially long-running handler fns where handling the most recent
  event is all that matters."
  [event-type handler key]
  (log-event "Registering lossy event handler:: " event-type " with key:" key)
  (let [worker (lossy-worker (fn [val]
                               (handler val)))
        [old _] (swap-returning-prev! lossy-workers* assoc key worker)]
    (when-let [old-worker (get old key)]
      (.put (:queue old-worker) :die))
    (on-sync-event event-type
                   (fn [msg]
                     (lossy-send worker msg))
                   key)))

(defn oneshot-event
  ""
  [event-type handler key]
  (log-event "Registering async self-removing event handler:: " event-type " with key: " key)
  (handlers/add-one-shot-handler! handler-pool event-type key handler))

(defn oneshot-sync-event
  ""
  [event-type handler key]
  (log-event "Registering sync self-removing event handler:: " event-type " with key: " key)
  (handlers/add-one-shot-sync-handler! handler-pool event-type key handler))

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
  [key]
  (let [[old new] (swap-returning-prev! lossy-workers* dissoc key)]
    (when-let [old-worker (get old key)]
      (.put (:queue old-worker) :die)))
  (log-event "Removing event handler associated with key: " key)
  (handlers/remove-handler! handler-pool key))

(defn remove-all-handlers
  "Remove all handlers."
  []
  (let [[old new] (swap-returning-prev! lossy-workers* (fn [_] {}))]
    (doseq [old-worker (vals old)]
      (.put (:queue old-worker) :die)))
  (log-event "Removing all event handlers!")
  (handlers/remove-all-handlers! handler-pool))

(defn event
  "Fire an event of type event-type with any number of additional
  properties.

  NOTE: an event requires key/value pairs, and everything gets wrapped
  into an event map.  It will not work if you just pass values.

  (event ::my-event)
  (event ::filter-sweep-done :instrument :phat-bass)"
  [event-type & args]
  (when @log-events?
    (log-event "event: " event-type " " args))
  (when @event-debug*
    (println "event: " event-type " " args "\n"))
  (when @monitoring?*
    (swap! monitor* assoc event-type args))
  (binding [overtone.libs.handlers/*log-fn* log/error]
    (let [event-info (if (and (= 1 (count args))
                              (map? (first args)))
                       (first args)
                       (apply hash-map args))]
      (handlers/event handler-pool event-type event-info))))

(defn sync-event
  "Runs all event handlers synchronously of type event-tye regardless
  of whether they were declared as async or not. If handlers create
  new threads which generate events, these will revert back to the
  default behaviour of event (i.e. not forced sync). See event."
  [event-type & args]
  (when @log-events?
    (log-event "sync-event: " event-type " " args))
  (when @event-debug*
    (println "sync-event: " event-type " " args "\n"))
  (when @monitoring?*
    (swap! monitor* assoc event-type args))
  (binding [overtone.libs.handlers/*log-fn* log/error]
    (let [event-info (apply hash-map args)]
      (apply handlers/sync-event handler-pool event-type event-info))))

(defn event-debug-on
  "Prints out all incoming events to stdout. May slow things down."
  []
  (reset! event-debug* true))

(defn event-debug-off
  "Stops debug info from being printed out."
  []
  (reset! event-debug* false))

(defn event-monitor-on
  "Start recording new incoming events into a map which can be examined
  with #'event-monitor"
  []
  (reset! monitor* {})
  (println "Event monitoring enabled")
  (reset! monitoring?* true))

(defn event-monitor-off
  "Stop recording new incoming events"
  []
  (println "Event monitoring disabled")
  (reset! monitoring?* false))

(defn event-monitor-timer
  "Record events for a specific period of time in seconds (defaults to
  5)."
  ([] (event-monitor-timer 5))
  ([seconds]
     (event-monitor-on)
     (loop [i seconds]
       (when (and @monitoring?* (pos? i))
         (println (str "Event monitor activated for "
                       i
                       " more second"
                       (when (> i 1)
                         "s")))
         (Thread/sleep 1000)
         (recur (dec i))))
     (event-monitor-off)))

(defn event-monitor
  "Return a map of the most recently seen events. This is reset every
  time #'event-monitor-on is called."
  ([] @monitor*)
  ([event-key] (get @monitor* event-key)))

(defn event-monitor-keys
  "Return a seq of all the keys of most recently seen events."
  []
  (keys @monitor*))

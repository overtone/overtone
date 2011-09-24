(ns overtone.sc.machinery.server.comms
  (:use [overtone.sc.machinery.server osc-validator]
        [overtone.libs.event]
        [overtone.util.lib :only [uuid await-promise!]])
  (:require [overtone.util.log :as log]))

(defonce server-sync-id* (atom 0))
(defonce osc-debug*     (atom false))
(defonce server-osc-peer*        (ref nil))


;; The base handler for receiving osc messages just forwards the message on
;; as an event using the osc path as the event key.
(on-sync-event :osc-msg-received
               (fn [{{path :path args :args} :msg}]
                 (event path :path path :args args))
               ::osc-receiver)

(defn- massage-numerical-args
  "Massage numerical args to the form SC would like them. Currently this just
  casts all Longs to Integers."
  [args]
  (map (fn [arg]
         (if (instance? Long arg)
           (Integer. arg)
           arg))
       args))

(defn server-snd
  "Sends an OSC message to the server. If the message path is a known scsynth
  path, then the types of the arguments will be checked according to what
  scsynth is expecting. Automatically converts any args which are longs to ints.

  (server-snd \"/foo\" 1 2.0 \"eggs\")"
  [path & args]
  (let [args (massage-numerical-args args)]
    (when @osc-debug*
      (println "Sending: " path [args])
      (log/debug (str "Sending: " path [args])))
    (apply validated-snd @server-osc-peer* path args)))

(defn- update-server-sync-id
  "update osc-sync-id*. Increments by 1 unless it has maxed out
  in which case it resets it to 0."
  []
  (swap! server-sync-id* (fn [cur] (if (= Integer/MAX_VALUE cur)
                                    0
                                    (inc cur)))))

(defn on-server-sync
  "Registers the handler to be executed when all the osc messages generated
   by executing the action-fn have completed. Returns result of action-fn."
  [action-fn handler-fn]
  (let [id (update-server-sync-id)
        key (uuid)]
    (on-event "/synced"
              (fn [msg] (when (= id (first (:args msg)))
                         (do
                           (handler-fn)
                           :done)))
              key)

    (let [res (action-fn)]
      (server-snd "/sync" id)
      res)))

(defn server-sync
  "Send a sync message to the server with the specified id. Server will reply
  with a synced message when all incoming messages up to the sync message have
  been handled. See with-server-sync and on-server-sync for more typical
  usage."
  [id]
  (server-snd "/sync" id))

(defn with-server-self-sync
  "Blocks the current thread until the action-fn explicitly sends a server sync.
  The action-fn is assumed to have one argument which will be the unique sync id.
  This is useful when the action-fn is itself asynchronous yet you wish to
  synchronise with its completion. The action-fn can sync using the fn server-sync.
  Returns the result of action-fn."
  [action-fn]
  (let [id (update-server-sync-id)
        prom (promise)
        key (uuid)]
    (on-event "/synced"
              (fn [msg] (when (= id (first (:args msg)))
                         (do
                           (deliver prom true)
                           :done)))
              key)
    (let [res (action-fn id)]
<      (await-promise! prom)
      res)))

(defn with-server-sync
  "Blocks current thread until all osc messages in action-fn have completed.
  Returns result of action-fn."
  [action-fn]
  (let [id (update-server-sync-id)
        prom (promise)
        key (uuid)]
    (on-event "/synced"
              (fn [msg] (when (= id (first (:args msg)))
                         (do
                           (deliver prom true)
                           :done)))
              key)
    (let [res (action-fn)]
      (server-snd "/sync" id)
      (await-promise! prom)
      res)))

(defn server-recv
  "Register your intent to wait for a message associated with given path to be
  received from the server. Returns a promise that will contain the message once
  it has been received. Does not block current thread (this only happens once
  you try and look inside the promise and the reply has not yet been received).

  If an optional matcher-fn is specified, will only deliver the promise when
  the matcher-fn returns true. The matcher-fn should accept one arg which is
  the incoming event info."
  ([path] (server-recv path nil))
  ([path matcher-fn]
     (let [p (promise)
           key (uuid)]
       (on-sync-event path
                      (fn [info]
                        (when (or (nil? matcher-fn)
                                  (matcher-fn info))
                          (deliver p info)
                          :done))
                      key)
    p)))

(defn sc-osc-debug-on
  "Log and print out all outgoing OSC messages"
  []
  (reset! osc-debug* true ))

(defn sc-osc-debug-off
  "Turns off OSC debug messages (see sc-osc-debug-on)"
  []
  (reset! osc-debug* false))

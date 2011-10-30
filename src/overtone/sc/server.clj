(ns
    ^{:doc "An interface to the SuperCollider synthesis server.
          This is at heart an OSC client library for the SuperCollider
          scsynth DSP engine."
      :author "Jeff Rose"}
  overtone.sc.server
  (:import [java.util.concurrent TimeoutException])
  (:use [overtone.libs event deps]
        [overtone.sc.machinery allocator]
        [overtone.sc.machinery.server connection comms]
        [overtone.util.lib :only [deref!]]
        [overtone.osc :only [in-osc-bundle]])
  (:require [overtone.util.log :as log]))

(defonce synth-group* (ref nil))
(defonce osc-log*     (atom []))
(defonce core-groups* (ref {}))


(defn connection-info
  "Returns connection information regarding the currently connected server"
  []
  @connection-info*)

(defn server-connected?
  "Returns true if the server is currently connected"
  []
  (= :connected @connection-status*))

(defn server-disconnected?
  "Returns true if the server is currently disconnected"
  []
  (not (server-connected?)))

(defn internal-server?
  "Returns true if the server is internal"
  []
  (= :internal (:connection (connection-info))))

(defn external-server?
  "Returns true if the server is external"
  []
  (= :external (:connection (connection-info))))

(defmacro at
  "All messages sent within the body will be sent in the same timestamped OSC
  bundle.  This bundling is thread-local, so you don't have to worry about
  accidentally scheduling packets into a bundle started on another thread."
  [time-ms & body]
  `(in-osc-bundle @server-osc-peer* ~time-ms (do ~@body)))

(defn snd
  "Sends an OSC message to the server. If the message path is a known scsynth
  path, then the types of the arguments will be checked according to what
  scsynth is expecting. Automatically converts any args which are longs to ints.

  (snd \"/foo\" 1 2.0 \"eggs\")"
  [path & args]
  (when (server-disconnected?)
    (throw (Exception. "Unable to send messages to a disconnected server. Please boot or connect to a server.")))
  (apply server-snd path args))

(defn recv
  "Register your intent to wait for a message associated with given path to be
  received from the server. Returns a promise that will contain the message once
  it has been received. Does not block current thread (this only happens once
  you try and look inside the promise and the reply has not yet been received).

  If an optional matcher-fn is specified, will only deliver the promise when
  the matcher-fn returns true. The matcher-fn should accept one arg which is
  the incoming event info."
  ([path] (recv path nil))
  ([path matcher-fn]
     (when-not (server-connected?)
       (throw (Exception. "Unable to receive messages from a disconnected server. Please boot or connect to a server.")))
     (server-recv path matcher-fn)))

(defn connect-external-server
  "Connect to an externally running SC audio server listening to port on host.
  Host defaults to localhost.

  (connect-external-server 57710)                  ;=> Connect to an external
                                                       server on the localhost
                                                       listening to port 57710
  (connect-external-server \"192.168.1.23\" 57110) ;=> Connect to an external
                                                       server with ip address
                                                       192.168.1.23 listening to
                                                       port 57110"
  ([port] (connect-external-server "127.0.0.1" port))
  ([host port]
     (connect host port)
     :connected-to-external-server))

(defn boot-external-server
  "Boot an external server by starting up an external process and connecting to
  it. Requires SuperCollider to be installed in the standard location for your
  OS."
  ([] (boot-external-server (+ (rand-int 50000) 2000)))
  ([port]
     (boot :external port)
     :booted-external-server))

(defn boot-server
  "Boot an internal server."
  []
  (boot :internal)
  :booted-internal-server)

(defn kill-server
  "Shutdown the running server"
  []
  (shutdown-server)
  :server-killed)

(defn external-server-log
  "Print the external server log."
  []
  (doseq [msg @external-server-log*]
    (print msg)))

(defn- parse-status
  "Returns a map representing the server status"
  [_ ugens synths groups loaded avg peak nominal actual]
    {:n-ugens ugens
     :n-synths synths
     :n-groups groups
     :n-loaded-synths loaded
     :avg-cpu avg
     :peak-cpu peak
     :nominal-sample-rate nominal
     :actual-sample-rate actual})

(defn server-status
  "Check the status of the audio server."
  []
  (if (server-connected?)
    (let [p (server-recv "/status.reply")]
      (snd "/status")
      (try
        (apply parse-status (:args (deref! p)))
        (catch TimeoutException t
          :timeout)))
    :disconnected))

(defn clear-msg-queue
  "Remove any scheduled OSC messages from the run queue."
  []
  (snd "/clearSched"))

(defn stop
  "Stop all running synths and metronomes. This does not remove any synths/insts
  you may have defined, rather it just stops any of them that are currently
  playing."
  []
  (event :reset))

(defn sc-osc-log-on
  "Turn osc logging on"
  []
  (on-sync-event :osc-msg-received
                 (fn [{:keys [path args] :as msg}]
                   (swap! osc-log* #(conj % msg)))
                 ::osc-logger))

(defn sc-osc-log-off
  "Turn osc logging off"
  []
  (remove-handler :osc-msg-received ::osc-logger))

(defn sc-osc-log
  "Return the current status of the osc log"
  []
  @osc-log*)

(defn sc-osc-debug-on
  "Log and print out all outgoing OSC messages"
  []
  (reset! osc-debug* true ))

(defn sc-osc-debug-off
  "Turns off OSC debug messages (see sc-osc-debug-on)"
  []
  (reset! osc-debug* false))

(defn sc-debug-on
  "Turn on output from both the Overtone and the audio server."
  []
  (log/level :debug)
  (sc-osc-debug-on)
  (snd "/dumpOSC" 1))

(defn sc-debug-off
  "Turn off debug output from both the Overtone and the audio server."
  []
  (log/level :error)
  (sc-osc-debug-off)
  (snd "/dumpOSC" 0))

(defn ensure-connected!
  "Throws an exception if the server isn't currently connected"
  []
  (when-not (server-connected?)
    (throw (Exception. "Server needs to be connected before you can perform this action."))))

(defn root-group
  []
  (ensure-connected!)
  (:root @core-groups*))

(defn main-mixer-group
  []
  (ensure-connected!)
  (:mixer @core-groups*))

(defn main-monitor-group
  []
  (ensure-connected!)
  (:monitor @core-groups*))

(defn main-input-group
  []
  (ensure-connected!)
  (:input @core-groups*))

(defn- setup-core-groups
  []
  (let [inpt-id (alloc-id :node)
        root-id (alloc-id :node)
        mixr-id (alloc-id :node)
        mont-id (alloc-id :node)]
    (with-server-sync #(snd "/g_new" inpt-id 0 0))
    (with-server-sync #(snd "/g_new" root-id 3 inpt-id))
    (with-server-sync #(snd "/g_new" mixr-id 3 root-id))
    (with-server-sync #(snd "/g_new" mont-id 3 mixr-id))
    (dosync
     (alter core-groups* assoc :input inpt-id
                               :root root-id
                               :mixer mixr-id
                               :monitor mont-id))
    (satisfy-deps :core-groups-created)))

(on-deps :server-connected ::setup-core-groups setup-core-groups)
(on-sync-event :shutdown ::reset-core-groups #(dosync
                                               (ref-set core-groups* {})))

(on-deps [:server-connected :core-groups-created] ::signal-server-ready #(satisfy-deps :server-ready))

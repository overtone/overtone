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
        [overtone.helpers.lib :only [deref!]]
        [overtone.osc :only [in-osc-bundle without-osc-bundle in-unested-osc-bundle]])
  (:require [overtone.config.log :as log]))

(defn connection-info
  "Returns connection information regarding the currently connected
  server"
  []
  @connection-info*)

(defn server-opts
  "Returns options for currently connected server (if available)"
  []
  (:opts @connection-info*))

(defn server-connected?
  "Returns true if the server is currently connected"
  []
  (= :connected @connection-status*))

(defn server-connecting?
  "Returns true if the server is connecting"
  []
  (= :connecting @connection-status*))

(defn server-disconnected?
  "Returns true if the server is currently disconnected"
  []
  (= :disconnected @connection-status*))

(defn internal-server?
  "Returns true if the server is internal"
  []
  (= :internal (:connection-type (connection-info))))

(defn external-server?
  "Returns true if the server is external"
  []
  (= :external (:connection-type (connection-info))))

(defmacro at
  "All messages sent within the body will be sent in the same
  timestamped OSC bundle.  This bundling is thread-local, so you don't
  have to worry about accidentally scheduling packets into a bundle
  started on another thread."
  [time-ms & body]
  `(in-unested-osc-bundle @server-osc-peer* ~time-ms (do ~@body)))

(defmacro snd-immediately
  [& body]
  `(without-osc-bundle ~@body))

(defn snd
  "Sends an OSC message to the server. If the message path is a known
  scsynth path, then the types of the arguments will be checked
  according to what scsynth is expecting. Automatically converts any
  args which are longs to ints.

  (snd \"/foo\" 1 2.0 \"eggs\")"
  [path & args]
  (when (server-disconnected?)
    (throw (Exception. "Unable to send messages to a disconnected server. Please boot or connect to a server.")))
  (apply server-snd path args))

(defn recv
  "Register your intent to wait for a message associated with given
  path to be received from the server. Returns a promise that will
  contain the message once it has been received. Does not block
  current thread (this only happens once you try and look inside the
  promise and the reply has not yet been received).

  If an optional matcher-fn is specified, will only deliver the
  promise when the matcher-fn returns true. The matcher-fn should
  accept one arg which is the incoming event info."
  ([path] (recv path nil))
  ([path matcher-fn]
     (when-not (server-connected?)
       (throw (Exception. "Unable to receive messages from a disconnected server. Please boot or connect to a server.")))
     (server-recv path matcher-fn)))

(defn connect-external-server
  "Connect to an externally running SC audio server listening to port
  on host.  Host defaults to localhost and port defaults to 57110."
  ([] (connect-external-server 57110))
  ([port] (connect-external-server "127.0.0.1" port))
  ([host port]
     (connect host port)
     (wait-until-deps-satisfied :server-ready)
     :happy-hacking))

(defn boot-external-server
  "Boot an external server by starting up an external process and connecting to
  it. Requires SuperCollider to be installed in the standard location for your
  OS."
  ([] (boot-external-server (+ (rand-int 50000) 2000)))
  ([port] (boot-external-server port {}))
  ([port opts]
     (boot :external port opts)
     :happy-hacking))

(defn boot-internal-server
  "Boot an internal server in the same process as overtone itself. Not
  currently available on all platforms"
  []
  (boot :internal)
  :happy-hacking)

(defn boot-server
  "Boot the default server."
  []
  (boot)
  :happy-hacking)

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

(def status server-status)

(defn clear-msg-queue
  "Remove any scheduled OSC messages from the run queue."
  []
  (snd "/clearSched"))

(defn stop
  "Stop all running synths and metronomes. This does not remove any
  synths/insts you may have defined, rather it just stops any of them
  that are currently playing."
  []
  (event :reset))

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
  (log/set-level! :debug)
  (sc-osc-debug-on)
  (snd "/dumpOSC" 1))

(defn sc-debug-off
  "Turn off debug output from both the Overtone and the audio server."
  []
  (log/set-level! :error)
  (sc-osc-debug-off)
  (snd "/dumpOSC" 0))

(defn ensure-connected!
  "Throws an exception if the server isn't currently connected"
  []
  (when-not (server-connected?)
    (throw (Exception. "Server needs to be connected before you can perform this action."))))

(on-sync-event [:overtone :osc-msg-received]
               (fn [{{path :path args :args} :msg}]
                 (let [poll-path "/overtone/internal/poll/"]
                   (when (.startsWith path poll-path)
                     (println "-->" (.substring path (count poll-path)) (nth args 2)))))
               ::handle-incoming-poll-messages)

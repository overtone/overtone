(ns
    ^{:doc "An interface to the SuperCollider synthesis server.
          This is at heart an OSC client library for the SuperCollider
          scsynth DSP engine."
      :author "Jeff Rose"}
  overtone.sc.server
  (:import [java.util.concurrent TimeoutException])
  (:use [overtone.sc.machinery.server connection comms ]
   [overtone.util.lib :only [await-promise!]]
        [overtone.libs event]
;;        [overtone.sc.machinery defaults allocator]
;;
        )
  (:require [overtone.util.log :as log]))

(def OVERTONE-VERSION {:major 0
                       :minor 4
                       :patch 0
                       :snapshot true})

(defonce synth-group*   (ref nil))
(defonce osc-log*       (atom []))

(defn server-status
  []
  @server-status*)

(defn connected? []
  (= :connected (server-status)))

(defn disconnected? []
  (= :disconnected (server-status)))

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
  (when-not (connected?)
    (throw (Exception. "Unable to send messages to an disconnected server. Please boot or connect to a server.")))
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
     (server-recv path matcher-fn)))

(defn connect-external-server
  "Connect to an externally running SC audio server listening to port on host.
  Host defaults to localhost.

  (connect 57710)                  ;=> connect to an external server on the
                                       localhost listening to port 57710
  (connect \"192.168.1.23\" 57110) ;=> connect to an external server with ip
                                       address 192.168.1.23 listening to port
                                       57110"
  ([port] (connect-external-server "127.0.0.1" port))
  ([host port] (connect host port)))

(defn boot-external-server
  ([] (boot :external (+ (rand-int 50000) 2000)))
  ([port] (boot :external port)))

(defn boot-server
  []
  (boot :internal))

(defn kill-server
  []
  (shutdown-server))

(defn external-server-log
  "Print the server log."
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

(defn status
  "Check the status of the audio server."
  []
  (if (connected?)
    (let [p (server-recv "/status.reply")]
      (snd "/status")
      (try
        (apply parse-status (:args (await-promise! p)))
        (catch TimeoutException t
          :timeout)))
    @server-status*))

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

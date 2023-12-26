(ns overtone.sc.machinery.server.connection
  (:require
   [clojure.java.io :as io]
   [clojure.java.shell :as shell]
   [clojure.string :as str]
   [overtone.config.log :as log]
   [overtone.config.store :as config]
   [overtone.helpers.file :as file :refer [dir-exists? resolve-tilde-path]]
   [overtone.helpers.lib :refer [deref! print-ascii-art-overtone-logo windows-sc-path]]
   [overtone.helpers.process-info :as process-info]
   [overtone.helpers.shell :refer [shellquote]]
   [overtone.helpers.system :refer [get-os linux-os? windows-os?]]
   [overtone.libs.deps :as deps]
   [overtone.libs.event :as event]
   [overtone.osc :as osc]
   [overtone.osc.decode :refer [osc-decode-packet]]
   [overtone.sc.defaults :as defaults]
   [overtone.sc.machinery.server.args :as args]
   [overtone.sc.machinery.server.comms :refer [server-osc-peer* server-recv server-snd]]
   [overtone.version :as version])
  (:import
   (java.io BufferedInputStream File InputStream OutputStream)
   (java.lang Process ProcessBuilder)))

(set! *warn-on-reflection* true)

(defonce server-thread*       (ref nil))
(defonce sc-world*            (ref nil))
(defonce connection-info*     (ref {}))
(defonce connection-status*   (ref :disconnected))

(defonce external-server-log* (atom []))


(defn transient-server?
  "Return true if the server was booted by us, whether internally or
  externally."
  []
  (when (not-empty @connection-info*) true))

(defn- server-notifications-on
  "Turn on notification messages from the audio server.  This lets us
  free synth IDs when they are automatically freed with envelope
  triggers.  It also lets us receive custom messages from various
  trigger ugens.

  These messages are sent as notification of some event to all clients
  who have registered via the /notify command .

  All of these have the same arguments:
   int - node ID
   int - the node's parent group ID
   int - previous node ID, -1 if no previous node.
   int - next node ID, -1 if no next node.
   int - 1 if the node is a group, 0 if it is a synth

  The following two arguments are only sent if the node is a group:
   int - the ID of the head node, -1 if there is no head node.
   int - the ID of the tail node, -1 if there is no tail node.

   /n_go   - a node was created
   /n_end  - a node was destroyed
   /n_on   - a node was turned on
   /n_off  - a node was turned off
   /n_move - a node was moved
   /n_info - in reply to /n_query"
  []
  (let [notifications-enabled (server-recv "/done" (fn [msg]
                                                     (= (first (:args msg))
                                                        "/notify")))]
    (server-snd "/notify" 1)
    (deref! notifications-enabled "whilst turning server notifications on")))

(defn- logged-sh
  "Run a shell command and log any errors. Returns stdout."
  [cmd & args]
  (let [res (apply shell/sh cmd args)]
    (when-not (zero? (:exit res))
      (log/error "Subprocess error: " (:err res)))
    (:out res)))

(defn- check-for-jack-or-pipewire!
  "Query the jack ports to see if it's running.
  This is useful to do before attemting external server connection on Linux, as
  not to fail silently"
  []
  (let [ps (filter #(some->> % :command (re-find #"/(pipewire|jackd|jackdbus)$")) (process-info/ps))]
    (if (seq ps)
      (do
        (log/info "Found Jack-compatible server process:")
        (doseq [{:keys [command user pid arguments]} ps]
          (log/info (format "%7d %8s %s %s"
                            pid
                            user
                            (shellquote command)
                            (shellquote arguments)))))
      (do
        (log/warn "Could not find Jack or Pipewire, starting SuperCollider (scsynth) will likely fail")))))

(defn- connect-jack-ports
  "Connect the jack input and output ports as best we can.  If jack
  ports are always different names with different drivers or hardware
  then we need to find a better strategy to auto-connect. (For Linux
  users)"
  ([] (connect-jack-ports 2))
  ([n-channels]
   (try
     (let [port-list      (logged-sh "jack_lsp")
           sc-ins         (re-seq #"Overtone.*:in_[0-9]*" port-list)
           sc-outs        (re-seq #"Overtone.*:out_[0-9]*" port-list)
           system-ins     (re-seq #"system:capture_[0-9]*" port-list)
           system-outs    (re-seq #"system:playback_[0-9]*" port-list)
           interface-ins  (re-seq #"system:AC[0-9]*_dev[0-9]*_.*In.*" port-list)
           interface-outs (re-seq #"system:AP[0-9]*_dev[0-9]*_LineOut.*" port-list)
           connections    (partition 2 (concat
                                        (interleave sc-outs system-outs)
                                        (interleave sc-outs interface-outs)
                                        (interleave system-ins sc-ins)
                                        (interleave interface-ins sc-ins)))]
       (doseq [[src dest] connections]
         (logged-sh "jack_connect" src dest)
         (log/info "jack_connect " src " " dest)))
     (catch Exception _))))

(when (linux-os?)
  (deps/on-deps :server-connected
                ::connect-jack-ports
                #(when (transient-server?)
                   (connect-jack-ports))))

;; We have to do this to handle the change in SC, where they added a "/" to the
;; status.reply messsage, which it should have had in the first place.
(defn- setup-connect-handlers []
  (let [handler-fn
        (fn [event-info]
          (event/remove-event-handler ::connected-handler1)
          (event/remove-event-handler ::connected-handler2)
          (dosync
           (ref-set connection-status* :connected))
          (server-notifications-on) ; turn on notifications now that we can communicate
          (deps/satisfy-deps :server-connected)
          (event/event :connection-complete)
          (log/debug "Server connection established")
          (println "--> Connection established"))]
    (event/oneshot-event "status.reply" handler-fn ::connected-handler1)
    (event/oneshot-event "/status.reply" handler-fn ::connected-handler2)))

(defn- external-connection-runner
  [host port]
  (when (linux-os?)
    (check-for-jack-or-pipewire!))
  (println  "--> Connecting to external SuperCollider server:" (str host ":" port))
  (log/debug "Connecting to external SuperCollider server: " host ":" port)
  (let [sc-server (osc/osc-client host port false)]
    (osc/osc-listen sc-server #(event/event [:overtone :osc-msg-received] :msg %))
    (dosync
     (ref-set server-osc-peer* sc-server))

    (setup-connect-handlers)
    (server-snd "/status")

    ;; Send /status in a loop until we get a reply
    (loop [cnt 0]
      (log/debug "connect loop...")
      (when-not (= :connected @connection-status*)
        (if (< cnt defaults/N-RETRIES)
          (do
            (log/debug (str "sending status... (" cnt ")"  ))
            (server-snd "/status")
            (Thread/sleep 100)
            (recur (inc cnt)))
          (do
            (deps/reset-deps)
            (throw
             (Exception. (str "Error: unable to connect to externally booted server after "
                              defaults/N-RETRIES " attempts.\n"
                              "Make sure that you have Server.local.options.maxLogins set to greater than 1 in startup file (startup.scd).\n"
                              "Or if you're on Windows, make sure that the Windows defender isn't blocking the scsynth.exe\n")))))))))

;; TODO: setup an error-handler in the case that we can't connect to the server
(defn connect
  "Connect to an externally running SC audio server.

  (connect)                        ;=> connect to an external server on
                                       localhost listening to the default
                                        port for scsynth 57711
  (connect 55555)                  ;=> connect to an external server on
                                       the localhost listening to port
                                       55555
  (connect \"192.168.1.23\" 57711) ;=> connect to an external server with
                                       ip address 192.168.1.23 listening to
                                       port 57711"
  ([] (connect "127.0.0.1" 57711))
  ([port] (connect "127.0.0.1" port))
  ([host port]
     (when-not (or (= :booting @connection-status*)
                   (= :disconnected @connection-status*))
       (dosync
        (ref-set connection-status* :disconnected))
       (throw (Exception. "Can't connect as a server is already connected/connecting!")))
     (.run (Thread. #(external-connection-runner host port)))))

(defn- osc-msg-decoder
  "Decodes incoming osc message buffers and then sends them as overtone events."
  [buf]
  (event/event [:overtone :osc-msg-received] :msg (osc-decode-packet buf)))

(defn pipe-scsynth-output
  "Prefix output from scsynth with `[scsynth]`"
  [^java.lang.Process proc]
  (let [out (.getInputStream proc)
        err (.getErrorStream proc)
        newline? (volatile! true)
        ^bytes buffer (make-array Byte/TYPE 1024)]
    (doseq [[^InputStream from ^OutputStream to] [[out System/out] [err System/err]]]
      (future
        (loop []
          (let [size (.read from buffer)]
            (when (pos? size)
              (dotimes [i size]
                (when @newline?
                  (.write to (.getBytes "[scynth] "))
                  (vreset! newline? false))
                (let [b (aget buffer i)]
                  (.write to (int b))
                  (when (= (int \newline) b)
                    (vreset! newline? true))))))
          (recur))))
    proc))

(defn- external-booter
  "Boot thread to start the external audio server process and hook up to
  STDOUT for log messages."
  ([^"[Ljava.lang.String;" cmd] (external-booter cmd "."))
  ([^"[Ljava.lang.String;" cmd ^java.lang.String working-dir]
   (log/info "Booting external audio server with cmd: " (seq cmd) ", and working directory: " working-dir)
   (let [working-dir  (File. working-dir)
         proc-builder (doto (ProcessBuilder. cmd)
                        (.directory (io/file working-dir)))
         proc         (pipe-scsynth-output (.start proc-builder))]
     (while (not (= :disconnected @connection-status*))
       (Thread/sleep 250))
     (.destroy ^Process proc))))

(defn- find-well-known-sc-path
  "Find the path for SuperCollider by checking common locations."
  []
  (let [os    (get-os)
        paths (defaults/SC-PATHS os)]
    (first (filter #(file/file-can-execute? %) paths))))

(defn- find-sc-arg-flag!
  "Retrieves the SC argument flag for sc-arg. Throws exception if flag
   can't be found."
  [sc-arg]
  (let [flag (-> sc-arg args/SC-ARG-INFO :flag)]
    (when-not flag
      (throw (Exception. (str "Error booting external SuperCollider server: unable to find flag for SC argument: " sc-arg))))
    flag))

(defn- scsynth-arglist
  "Returns a sequence of args suitable for use as arguments to the scsynth command"
  [args]
  (when (not= 1 (:realtime? args))
    (throw (Exception. "Non-realtime server mode not currently supported. Patches accepted - please contact the mailing list.")))
  (let [udp?        (:udp? args)
        port        (:port args)
        ugens-paths (or (:ugens-paths args) [])
        args        (select-keys args (keys args/SC-ARG-INFO))
        args        (dissoc args :udp? :port :realtime? :nrt-cmd-filename :nrt-output-filename :nrt-output-header-format :nrt-output-sample-format)
        args        (args/linux-jack-device-name args)
        port-arg    (if (= 1 udp?)
                      ["-u" port]
                      ["-t" port])
        ugens-paths (map resolve-tilde-path ugens-paths)
        ugens-paths (filter dir-exists? ugens-paths)
        ugens-paths (apply str (interpose ":" ugens-paths))
        args        (if (empty? ugens-paths)
                      (dissoc args :ugens-paths)
                      (assoc args :ugens-paths ugens-paths))
        arg-list    (reduce
                     (fn [res [flag val]] (if val
                                            (concat res [(find-sc-arg-flag! flag) val])
                                            res))
                     []
                     args)]
    (map str (concat port-arg arg-list))))

(defn scsynth-path []
  (let [sc-config (config/config-get :sc-path)
        sc-path (file/find-executable "scsynth")
        sc-wellknown (find-well-known-sc-path)
        match (or sc-config sc-path sc-wellknown)]
    (when-not match
      (throw (ex-info (str "Failed to find SuperCollider server executable (scsynth). The file does not exist or is not executable. Places I've looked:\n"
                           "- `:sc-path` in " config/OVERTONE-CONFIG-FILE " (" (pr-str sc-config) ")\n"
                           "- The current PATH (" (System/getenv "PATH") ")\n"
                           "- Well-known locations " (seq (defaults/SC-PATHS (get-os)))"")
                      {})))
    (log/info "Found SuperCollider server: " match " (" (cond
                                                          sc-config
                                                          (str "configured in " config/OVERTONE-CONFIG-FILE)
                                                          sc-path
                                                          "PATH"
                                                          sc-wellknown
                                                          (str "well-known location for " (name (get-os))))
              ")")
    (str match)))

(defn- has-pipewire? []
  (some #(some->> % :command (re-find #"/pipewire$")) (process-info/ps)))
(defn- has-jackd? []
  (some #(some->> % :command (re-find #"/(jackd|jackdbus)$")) (process-info/ps)))
(defn- has-pw-jack? []
  (file/find-executable "pw-jack"))

(defn- sc-command
  "Creates a string array representing the sc command to execute in an external
  process (typically with #'external-booter)."
  [opts]
  (into-array String
              (cond->> (cons (scsynth-path) (scsynth-arglist opts))
                ;; pw-jack adds PipeWire's Jack implementation to the
                ;; LD_LIBRARY_PATH, so that Jack applications work with PipeWire
                ;; instead of looking for original jackd. This might save some
                ;; people some head scratchers.
                (and (linux-os?) (has-pipewire?) (has-pw-jack?) (not (has-jackd?)))
                (cons "pw-jack"))))

(defn- boot-server
  "Boot the audio server in an external process and tell it to listen on a
  specific port."
  ([port opts]
   (when-not (= :booting @connection-status*)
     (throw (Exception. "Can't boot external server as a server is already connected/connecting!")))
   (log/debug "booting external server")
   (let [full-opts (args/merge-sc-args opts {:port port})
         cmd       (sc-command full-opts)

         sc-thread (if (windows-os?)
                     (Thread. #(external-booter cmd (windows-sc-path)))
                     (Thread. #(external-booter cmd)))]
     (.setDaemon sc-thread true)
     (println "--> Booting external SuperCollider server...")
     (log/info (str "Booting SuperCollider server (scsynth) with cmd: " (shellquote cmd)))
     (.start sc-thread)
     (dosync (ref-set server-thread* sc-thread)
             (alter connection-info* assoc :opts full-opts))
     (connect "127.0.0.1" port)
     :booting)))

(defn ^:deprecated boot-internal-server [& args]
  (log/warn "Overtone no longer contains an internal server, booting external server instead.")
  (apply boot-server args))

(defn- transient-connection-info
  "Build the connection-info for booting an internal or external server."
  [connection-type port]
  (merge {:connection-type connection-type}
         (case connection-type
           :internal {}
           :external {:port port :host "127.0.0.1"})))

(defn boot
  "Boot either the internal or external audio server. If specified port
  is nil will choose a random port.

   (boot) ; uses the default settings defined in your config
   (boot :internal)       ; boots the internal server
   (boot :external)       ; boots an external server on a random port
   (boot :external 57711) ; boots an external server listening on port
                            577111"
  ([] (boot (or (config/config-get :server) :internal) defaults/SERVER-PORT))
  ([connection-type] (boot connection-type defaults/SERVER-PORT))
  ([connection-type port] (boot connection-type port {}))
  ([connection-type port opts]
   (locking connection-info*
     (when-not (= :disconnected @connection-status*)
       (throw (Exception. "Can't boot as a server is already connected/connecting!")))

     (dosync
      (ref-set connection-status* :booting))

     (dosync
      (ref-set connection-info*
               (transient-connection-info connection-type port)))

     (let [port (if (nil? port) (+ (rand-int 50000) 2000) port)]
       (when (not= :external connection-type)
         (log/warn "Only :external connection type is supported, :connection-type " connection-type " ignored. (" config/OVERTONE-CONFIG-FILE ")"))
       (boot-server port opts)
       (deps/wait-until-deps-satisfied :server-ready)))
   (print-ascii-art-overtone-logo
    (config/config-get :user-name)
    version/OVERTONE-VERSION-STR)))

(defn shutdown-server
  "Quit the SuperCollider synth process."
  []
  (locking connection-info*

    (log/info "Shutting down...")
    (event/sync-event :shutdown)

    (when (transient-server?)
      (log/info "Quitting...")
      (try
        (server-snd "/quit")
        (catch Exception e
          (log/error "Can't quit server gracefully with /quit"))))

    (when @server-osc-peer*
      (log/info "Closing OSC peer...")
      (osc/osc-close @server-osc-peer* true))

    (log/info "Resetting server state and unsatisfying all deps...")
    (dosync
     (ref-set server-osc-peer* nil)
     (ref-set connection-info* {})
     (ref-set connection-status* :disconnected)
     (deps/unsatisfy-all-dependencies))))

(defonce _shutdown-hook
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. (fn []
                               (println "--> Initiating shutdown")
                               (locking connection-info*
                                 (when (= :connected @connection-status*)
                                   (shutdown-server)))))))

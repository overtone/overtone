(ns overtone.config.log
  "Basic logging functionality."
  {:author "Jeff Rose"}
  (:require
   [clojure.pprint :refer [pprint]]
   [overtone.config.store :as store])
  (:import
   (java.util Date)
   (java.util.logging ConsoleHandler FileHandler Formatter Level Logger LogManager LogRecord StreamHandler)))

(def ^:private LOG-APPEND true)
(def ^:private LOG-LIMIT 5000000)
(def ^:private LOG-COUNT 2)
(def ^:private DEFAULT-LOG-LEVEL :warn)
(def ^:private LOG-LEVELS {:debug Level/FINE
                           :info  Level/INFO
                           :warn  Level/WARNING
                           :error Level/SEVERE})
(def ^:private REVERSE-LOG-LEVELS (apply hash-map (flatten (map reverse LOG-LEVELS))))

(defonce ^:private LOGGER (Logger/getLogger "overtone"))
(defonce ^:private LOG-CONSOLE (ConsoleHandler.))
(defonce ^:private LOG-FILE-HANDLER (FileHandler. store/OVERTONE-LOG-FILE LOG-LIMIT LOG-COUNT LOG-APPEND))

(defn- initial-log-level
  []
  (or (store/config-get :log-level)
      DEFAULT-LOG-LEVEL))

(defn- log-formatter []
  (proxy [Formatter] []
    (format [^LogRecord log-rec]
      (let [lvl (REVERSE-LOG-LEVELS (.getLevel log-rec))
            msg (.getMessage log-rec)
            ts (.getMillis log-rec)]
        (with-out-str (pprint {:level lvl
                               :timestamp ts
                               :date (Date. (long ts))
                               :msg msg}))))))

(defn- print-handler []
  (let [formatter (log-formatter)]
    (proxy [StreamHandler] []
      (publish [msg] (println (.format formatter msg))))))

(defn console []
  (.addHandler LOGGER (print-handler)))

(defn log-level
  "Returns the current log level"
  []
  (.getLevel LOGGER))

(defn set-level!
  "Set the log level for this session. Use one of :debug, :info, :warn
  or :error"
  [level]
  (assert (contains? LOG-LEVELS level))
  (.setLevel LOGGER (get LOG-LEVELS level)))

(defn set-default-level!
  "Set the log level for this and future sessions - stores level in
  config. Use one of :debug, :info, :warn or :error"
  [level]
  (set-level! level)
  (store/config-set! :log-level level)
  level)

(defn debug
  "Log msg with level debug"
  [& msg]
  (.log LOGGER Level/FINE (apply str msg)))

(defn info
  "Log msg with level info"
  [& msg]
  (.log LOGGER Level/INFO (apply str msg)))

(defn warn
  "Log msg with level warn"
  [& msg]
  (.log LOGGER Level/WARNING (apply str msg)))

(defn error
  "Log msg with level error"
  [& msg]
  (.log LOGGER Level/SEVERE (apply str msg)))

(defmacro with-error-log
  "Wrap body with a try/catch form, and log exceptions (using warning)."
  [message & body]
  `(try
     ~@body
     (catch Exception ex#
       (println "Exception: " ex#)
       (warn (str ~message "\nException: " ex#
                     (with-out-str (clojure.stacktrace/print-stack-trace ex#)))))))

;;setup logger
(defonce ^:private __setup-logs__
  (do
    (.reset (LogManager/getLogManager))
    (set-level! (initial-log-level))
    (.setFormatter LOG-FILE-HANDLER (log-formatter))
    (.addHandler LOGGER LOG-FILE-HANDLER)))

(defonce ^:private __cleanup-logger-on-shutdown__
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. (fn []
                               (info "Shutting down - cleaning up logger")
                               (.removeHandler LOGGER LOG-FILE-HANDLER)
                               (.close LOG-FILE-HANDLER)))))
